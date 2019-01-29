package com.leadcore.sms.socket.tcp;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.sipdroid.sipua.BaseApplication;
import org.sipdroid.sipua.utils.FileUtils;
import org.sipdroid.sipua.utils.MyLog;

import com.leadcore.sms.entity.Message;
import com.leadcore.sms.entity.Message.CONTENT_TYPE;
import com.leadcore.sms.file.Constant;
import com.leadcore.sms.file.FileState;
import com.leadcore.sms.sql.ChattingInfo;
import com.leadcore.sms.sql.SqlDBOperate;

import android.content.Context;
import android.os.Handler;
/**
 * File send service using TCP protocol
 * @author zhaoyifei
 *
 */
public class TcpService implements Runnable {
    private static final String TAG = "TcpService";

    private ServerSocket serviceSocket;
    private boolean SCAN_FLAG = false; // 接收扫描标识
    private Thread mThread;
    ArrayList<FileState> receivedFileNames;
    ArrayList<SaveFileToDisk> saveFileToDisks;
    private String filePath = null; // 存放接收文件的路径

    private static Context mContext;
    private static TcpService instance; // 唯一实例

    private boolean IS_THREAD_STOP = false; // 是否线程开始标志
    
    
    private TcpServiceListener mServiceListener;

    private TcpService() {
        try {            
            serviceSocket = new ServerSocket(Constant.TCP_SERVER_RECEIVE_PORT);
            saveFileToDisks = new ArrayList<TcpService.SaveFileToDisk>();
            MyLog.i(TAG, "new Serversocket success");
        }
        catch (IOException e) {
            MyLog.e(TAG, "failed to new ServerSocket ", e);
        }
        mThread = new Thread(this);
    }

    /**
     * <p>
     * 获取TcpService实例
     * <p>
     * 单例模式，返回唯一实例
     */
    public static TcpService getInstance(Context context) {
        if (instance == null) {
        	synchronized (TcpService.class) {
				if (instance == null) {
					instance = new TcpService(context);
				}
			}
        }
        return instance;
    }
    
    public void setSavePath(String fileSavePath) {
        this.filePath = fileSavePath;
    }

    public TcpService(Context context) {
        this();
        mContext = context;
    }

    private void scan_recv() {
        try {
            Socket socket = serviceSocket.accept(); 
            MyLog.i(TAG, "Client connect success getInetAddress():" + socket.getInetAddress().getHostAddress()
            		+" getLocalAddress():" +socket.getLocalAddress().getHostAddress());
            SaveFileToDisk fileToDisk = new SaveFileToDisk(socket, filePath);
            saveFileToDisks.add(fileToDisk);
            BaseApplication.FILE_RECE_EXECUTOR.execute(fileToDisk);
        }
        catch (IOException e) {
            MyLog.e(TAG, "Client connect failed ",e);
            SCAN_FLAG = false;
        }
    }

    @Override
    public void run() {
        MyLog.i(TAG, "TcpService run()");
        while (!IS_THREAD_STOP) {
            if (SCAN_FLAG) {
                scan_recv();
            }
        }
    }

    public void release() {
    	for (SaveFileToDisk saveFileToDisk : saveFileToDisks) {
			saveFileToDisk.stopRece();
		}
        if (null != serviceSocket && !serviceSocket.isClosed())
            try {
                serviceSocket.close();
                serviceSocket = null;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        while (SCAN_FLAG == true)
            ;// 直到SCAN_FLAG为false的时候退出循环
        SCAN_FLAG = false;
        IS_THREAD_STOP = true;
        saveFileToDisks.clear();
        instance = null;
    }

    public void startReceive() {
        SCAN_FLAG = true; // 使能扫描接收标识
        if (!mThread.isAlive())
            mThread.start(); // 开启线程
    }
    
    public boolean hasFileReceving(){
    	for (SaveFileToDisk saveFileToDisk : saveFileToDisks) {
			if (saveFileToDisk.isRunning()) {
				return true;
			}
		}
    	return false;
    }

    public class SaveFileToDisk implements Runnable {
        private boolean SCAN_RECIEVE = true;
        private InputStream input = null;
        private DataInputStream dataInput;
        private byte[] mBuffer = new byte[Constant.READ_BUFFER_SIZE];// 声明接收数组
        private String savePath;
        private String type[] = { "TEXT", "IMAGE", "FILE", "VOICE" };
        
        private int percent;
        private boolean isRunning = true;
        private boolean stop      = false;
        private SqlDBOperate mDBOperate;

        public SaveFileToDisk(Socket socket) {
        	mDBOperate = new SqlDBOperate(mContext);
            try {
                input = socket.getInputStream();
                dataInput = new DataInputStream(input);
                MyLog.i(TAG, "get net InputStream success");
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                MyLog.e(TAG, "failed to get net InputStream ", e);
                SCAN_RECIEVE = false;
            }
        }

        public SaveFileToDisk(Socket socket, String savePath) {
            this(socket);
            this.savePath = savePath;
        }
        
        public boolean isRunning(){
        	return isRunning;
        }
        
        public void stopRece(){
        	stop = true;
        }

        public void recieveFile() {
            int readSize = 0;
            FileOutputStream fileOutputStream = null;
            BufferedOutputStream bufferOutput = null;
            String strFiledata;
            String[] strData = null;
            String fileSavePath;
            FileState fs = null;

            try {
                strFiledata = dataInput.readUTF().toString();
                strData = strFiledata.split("!");
                long length = Long.parseLong(strData[1]);// 文件大小

                MyLog.i(TAG, "the type of transfer file>>>" + strData[3]);
                fileSavePath = savePath + File.separator + strData[2] + File.separator + strData[0];//文件全路径
                //modify by zyf
                String fileDir = savePath + File.separator + strData[2]; //文件目录
                if (!FileUtils.isFileExists(fileDir)) {
					FileUtils.createDirFile(fileDir);
				}
                fileOutputStream = new FileOutputStream(new File(fileSavePath));// 创建文件流
                MyLog.i(TAG, "the savedpath of transfer file>>>" + fileSavePath);
                fs = new FileState(length, 0, fileSavePath, getType(strData[3]));
                if (fs.type == CONTENT_TYPE.FILE) {
                	fs.id = Long.parseLong(strData[4]);
				}
                bufferOutput = new BufferedOutputStream(fileOutputStream);// 创建带缓冲区的文件流
                long currentLength = 0;
                while (-1 != (readSize = dataInput.read(mBuffer))) {
                	if (stop) {
                		ChattingInfo chattingInfo = new ChattingInfo();
                		chattingInfo.setID(fs.id);
                		chattingInfo.setPercent(-1);
                		if (mDBOperate != null) {
                			mDBOperate.updateChattingInfo(chattingInfo);
                		}
						break;
					}
                    bufferOutput.write(mBuffer, 0, readSize);
                    currentLength += readSize;
                        fs.percent = (int) ((float) currentLength*100 / (float) length );

                        switch (fs.type) {
                            case IMAGE:
                            	
                                break;

                            case VOICE:                             
                                break;

                            case FILE:
                            	if (percent != fs.percent) {
                            		mServiceListener.onFileReceive(fs);
								}
                            	percent = fs.percent;
                                break;

                            default:
                                break;
                        }
                }

                // 将byte数组的数据写进指定路径
                bufferOutput.flush();
                if (mDBOperate != null) {
					mDBOperate.close();
					mDBOperate = null;
				}
                input.close();
                dataInput.close();
                bufferOutput.close();
                fileOutputStream.close();
                
                switch (fs.type) {
                    case IMAGE:
                    	MyLog.e("TcpService", "received image success fileSavePath>>>" + fileSavePath);
                    	mServiceListener.onImageReceived(fileSavePath);
                        break;
                        
                    case VOICE:          
                        break;

                    case FILE:
                    	if (fs.percent == 100) {
                    		mServiceListener.onFileReceiveSuccess(fs); 
						}else {
							mServiceListener.onFileReceiveFailed(fs);
						}
                    	MyLog.i(TAG, "onFileReceive percent >>> " + fs.percent);
                        break;

                    default:
                        break;
                }
            }
            catch (IOException e) {
                MyLog.e(TAG, "failed to write to file ", e);
                if (fs != null) {
					if (fs.type == CONTENT_TYPE.FILE) {
						mServiceListener.onFileReceiveFailed(fs);
					}
				}
            }
            isRunning = false;
        }

        private Message.CONTENT_TYPE getType(String string) {
            if (string.equals(type[0]))
                return CONTENT_TYPE.TEXT;
            else if (string.equals(type[1]))
                return CONTENT_TYPE.IMAGE;
            else if (string.equals(type[2]))
                return CONTENT_TYPE.FILE;
            else if (string.equals(type[3]))
                return CONTENT_TYPE.VOICE;
            return null;

        }

        @Override
        public void run() {
            MyLog.i(TAG, "SaveFileToDisk run()");
            if (SCAN_RECIEVE)
                recieveFile();
        }
    }
    
    public void setListener(TcpServiceListener listener){
    	mServiceListener = listener;
    }

}
