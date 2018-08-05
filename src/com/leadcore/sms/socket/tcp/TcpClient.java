package com.leadcore.sms.socket.tcp;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.http.impl.client.TunnelRefusedException;
import org.sipdroid.sipua.BaseApplication;
import org.sipdroid.sipua.ui.Receiver;
import org.sipdroid.sipua.utils.DateUtils;
import org.sipdroid.sipua.utils.FileUtils;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.utils.SessionUtils;

import com.leadcore.sms.entity.Message;
import com.leadcore.sms.entity.Message.CONTENT_TYPE;
import com.leadcore.sms.file.Constant;
import com.leadcore.sms.file.FileState;
import com.leadcore.sms.file.FileStyle;
import com.leadcore.sms.socket.udp.IPMSGConst;
import com.leadcore.sms.sql.ChattingInfo;
import com.leadcore.sms.sql.SqlDBOperate;

import android.content.Context;
import android.os.Handler;
/**
 * File send Client using tcp protocol
 * @author zhaoyifei
 *
 */
public class TcpClient {
    private static final String TAG = "TcpClient";

    private static TcpClient instance;
    private static Context mContext;
    private ArrayList<ClientRunnable> clientRunnables;

    private int mRecordTime;
    private TcpClientListener mClientListener;
    
    private TcpClient() {
    	clientRunnables = new ArrayList<ClientRunnable>();
        MyLog.i(TAG, "new TcpClient success");
    }


    /**
     * <p>
     * 获取TcpService实例
     * <p>
     * 单例模式，返回唯一实例
     */
    public static TcpClient getInstance(Context context) {
        if (instance == null) {
        	synchronized (TcpClient.class) {
				if (instance == null) {
					instance = new TcpClient(context);
				}
			}
        }
        return instance;
    }



    private TcpClient(Context context) {
        this();
        mContext = context;
    }



    public void sendFile(String filePath, String target_IP, Message.CONTENT_TYPE type) {
    	ClientRunnable clientRunnable = new ClientRunnable(target_IP, filePath, type);
    	clientRunnables.add(clientRunnable);
    	BaseApplication.FILE_SEND_EXECUTOR.execute(clientRunnable);
    }
    
    public void sendFile(String filePath, String target_IP, Message.CONTENT_TYPE type, int recordTime){
    	sendFile(filePath, target_IP, type);
    	mRecordTime =recordTime;
    }
    
    public void sendFile(String filePath, String target_IP, Message.CONTENT_TYPE type,
    		long localId, long remoteId) {
    	ClientRunnable clientRunnable = new ClientRunnable(target_IP, filePath, type, localId, remoteId);
    	clientRunnables.add(clientRunnable);
    	BaseApplication.FILE_SEND_EXECUTOR.execute(clientRunnable);
    }

    public void release() {
    	for (ClientRunnable clientRunnable : clientRunnables) {
			clientRunnable.stopSend();
		}
    	clientRunnables.clear();
    	instance = null;
    }
    
    public void setListener(TcpClientListener listener){
    	mClientListener = listener;
    }
    
    public boolean hasFileSending(){
    	for (ClientRunnable clientRunnable : clientRunnables) {
			if (clientRunnable.isRunning()) {
				return true;
			}
		}
    	return false;
    }

    public class ClientRunnable implements Runnable{
        private boolean SEND_FLAG = true; // 
        private byte[] mBuffer = new byte[Constant.READ_BUFFER_SIZE]; // 数据报内容
        private OutputStream output = null;
        private DataOutputStream dataOutput;
        private FileInputStream fileInputStream;
        private Socket socket = null;
        private String target_IP;
        private String filePath;
        private Message.CONTENT_TYPE type;
        
        private long localId;  //the local ID in DB of the sending file
        private long remoteId; //the remote ID in DB of the sending file
        private boolean isRunning = true;
        private boolean stop    = false;
        private SqlDBOperate mDBOperate;
        
        

        public ClientRunnable(String target_IP, String filePath) {
            this.target_IP = target_IP;
            this.filePath = filePath;
            mDBOperate = new SqlDBOperate(mContext);
        }

        public ClientRunnable(String target_IP, String filePath, Message.CONTENT_TYPE type) {
            this(target_IP, filePath);
            this.type = type;
        }
        
        public ClientRunnable(String target_IP, String filePath, Message.CONTENT_TYPE type,
        		long localId, long remoteId) {
            this(target_IP, filePath, type);
            this.localId = localId;
            this.remoteId = remoteId;
        }
        
        public boolean isRunning(){
        	return isRunning;
        }
        
        public void stopSend(){
        	stop = true;
        }

        private void sendFile() {
        	MyLog.i(TAG, "sendFile start 1");
            int readSize = 0;
            FileState fs = null;
            try {
                socket = new Socket(target_IP, Constant.TCP_SERVER_RECEIVE_PORT);
                fileInputStream = new FileInputStream(new File(filePath));
                output = socket.getOutputStream();
                dataOutput = new DataOutputStream(output);
                int fileSize = fileInputStream.available();
                //put the fileName, fileSize, fileSender's IMEI and the fileType to socket
                dataOutput.writeUTF(filePath.substring(filePath.lastIndexOf(File.separator) + 1)
                        + "!" + fileSize + "!" + SessionUtils.getIMEI() + "!" + type + "!" + remoteId);
                long length = 0;

                fs = new FileState(filePath);
                fs.fileSize = fileSize;
                fs.type = type;
                if (fs.type == CONTENT_TYPE.FILE) {
                	fs.id  = localId;
				}
                while (-1 != (readSize = fileInputStream.read(mBuffer))) {
                	if (stop) {
                		ChattingInfo chattingInfo = new ChattingInfo();
                		chattingInfo.setID(fs.id);
                		chattingInfo.setPercent(-1);
                		if (mDBOperate != null) {
                			mDBOperate.updateChattingInfo(chattingInfo);
                		}
						break;
					}
                	MyLog.i(TAG, "sendFile start 2");
                    length += readSize;
                    dataOutput.write(mBuffer, 0, readSize);
                    fs.percent = (int) ((float)length * 100 / (float)fileSize);

                    switch (type) {
                        case IMAGE:
                            break;

                        case VOICE:
                            break;

                        case FILE:
                        	mClientListener.onFileSend(fs);
                        	MyLog.i(TAG, "sendFile>>>" + fs.toString());

                            break;

                        default:
                            break;
                    }
                    dataOutput.flush();
                }
                MyLog.i(TAG, "send completed" + fs.toString());
                if (mDBOperate != null) {
					mDBOperate.close();
					mDBOperate = null;
				}
                output.close();
                dataOutput.close();
                socket.close();
                MyLog.i(TAG, "sendFile start 3");
                switch (type) {
                    case IMAGE:
                        Message imageMsg = new Message(SessionUtils.getIMEI(),
                                DateUtils.getNowtime(), fs.fileName, type);
                        imageMsg.setMsgContent(FileUtils.getNameByPath(imageMsg.getMsgContent()));
                        Receiver.engine(mContext).sendSMSdata(IPMSGConst.IPMSG_SENDMSG, target_IP, imageMsg);
                        MyLog.i(TAG, "Pictures sent successfully");
                        break;

                    case VOICE:
                        Message voiceMsg = new Message(SessionUtils.getIMEI(),
                                DateUtils.getNowtime(), fs.fileName, type, mRecordTime);
                        voiceMsg.setMsgContent(FileUtils.getNameByPath(voiceMsg.getMsgContent()));
                        Receiver.engine(mContext).sendSMSdata(IPMSGConst.IPMSG_SENDMSG, target_IP, voiceMsg);
                        MyLog.i(TAG, "Voices sent successfully ");
                        break;

                    case FILE:
                    	if (fs.percent == 100) {
                    		mClientListener.onFileSendSuccess(fs);
						}else {
							mClientListener.onFileSendFailed(fs);
						}
                        break;

                    default:
                        break;
                }
                
            }
            catch (UnknownHostException e) {
                MyLog.e(TAG, "failed to new client socket ",e);
                SEND_FLAG = false;
                if (type == CONTENT_TYPE.FILE && fs != null) {
					mClientListener.onFileSendFailed(fs);
				}
            }
            catch (IOException e) {
            	MyLog.e(TAG, "failed to new client socket ",e);
                SEND_FLAG = false;
                if (type == CONTENT_TYPE.FILE && fs != null) {
					mClientListener.onFileSendFailed(fs);
				}
            }
            finally {
                // IS_THREAD_STOP=true;
            }
            isRunning = false;
        }

        @Override
        public void run() {
            MyLog.i(TAG, "SendFileThread run()");
            if (SEND_FLAG) {
                sendFile();
            }
        }
        
    }
    

}
