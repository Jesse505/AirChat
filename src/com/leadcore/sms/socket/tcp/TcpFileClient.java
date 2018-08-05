package com.leadcore.sms.socket.tcp;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.sipdroid.sipua.BaseApplication;
import org.sipdroid.sipua.ui.Receiver;
import org.sipdroid.sipua.utils.FileUtils;
import org.sipdroid.sipua.utils.MyLog;

import android.content.Context;

import com.leadcore.sms.entity.Message;
import com.leadcore.sms.file.Constant;
import com.leadcore.sms.socket.udp.IPMSGConst;
import com.leadcore.sms.socket.udp.IPMSGProtocol;

/**
 * used to receive file by tcp protocol
 * 
 * @author zhaoyifei
 * @since 2016/12/26
 */
public class TcpFileClient {

	private static final String TAG = "TcpFileClient";
	private ArrayList<ClientRunable> clientThreads;
	private static TcpFileClient mTcpFileClient;
	private static Context mContext;

	private TcpFileClient(Context context) {
		clientThreads = new ArrayList<TcpFileClient.ClientRunable>();
		mContext = context;
		MyLog.i(TAG, "new TcpFileClient success");
	}

	public static TcpFileClient getInstance(Context context) {
		if (mTcpFileClient == null) {
			synchronized (TcpFileClient.class) {
				if (mTcpFileClient == null) {
					mTcpFileClient = new TcpFileClient(context);
				}
			}
		}
		return mTcpFileClient;
	}


	public void release() {
		clientThreads.clear();
		mTcpFileClient = null;
	}

	/**
	 * 
	 * @param filePath
	 *            文件需要保存的目录
	 * @param targetIp
	 *            监听发送文件的目标IP
	 */
	public void receiveFile(String filePath, String targetIp, Message msg) {
		ClientRunable clientThread = new ClientRunable(targetIp, filePath, msg);
		clientThreads.add(clientThread);
		BaseApplication.GROUP_IMAGE_RECEIVER.execute(clientThread);
	}


	/**
	 * 客户端线程，创建客户端Socket连接远程ServerSocket,将需要下载的文件路径传给Server端,并从Server端下载文件
	 * 
	 * @author zhaoyifei
	 * 
	 */
	class ClientRunable implements Runnable {

		private String mTargetIp;
		private Message mMsg; // 发送端传过来的message消息
		private String mPath; // Server端的文件地址
		private String mGroupIp;
		private String mSavePath; // 本地保存目录
		private Socket mSocket;
		
		private OutputStream output;
		private DataOutputStream dataOutput;
		
		private InputStream ins;
		private DataInputStream dataInput;
		private FileOutputStream fileOutputStream;
		private BufferedOutputStream bufferOutput;
		private byte[] mBuffer = new byte[Constant.READ_BUFFER_SIZE];

		/**
		 * 
		 * @param targetIp
		 * @param msg
		 *            包括Server端的文件地址和组IP,中间用"!"分割
		 * @param savePath
		 *            本地保存目录
		 */
		public ClientRunable(String targetIp, String savePath, Message msg) {
			mTargetIp = targetIp;
			mMsg = msg;
			mSavePath = savePath;
			mPath = mMsg.getMsgContent();
			mGroupIp = mMsg.getReceiveIP();
		}

		@Override
		public void run() {
			try {
				mSocket = new Socket(mTargetIp, Constant.TCP_FILE_SENDER_PORT);
			} catch (UnknownHostException e) {
				MyLog.e(TAG, "ClientRunable run", e);
				e.printStackTrace();
			} catch (IOException e) {
				MyLog.e(TAG, "ClientRunable run", e);
				e.printStackTrace();
			}
			
			try {
				output = mSocket.getOutputStream();
				dataOutput = new DataOutputStream(output);
				dataOutput.writeUTF(mPath);
				dataOutput.flush();
				// dataOutput.close();
				// output.close();
			} catch (IOException e) {
				MyLog.e(TAG, "ClientRunable run", e);
				e.printStackTrace();
			}
			
			try {
				ins = mSocket.getInputStream();
				dataInput = new DataInputStream(ins);
				String fileSavePath = mSavePath + File.separator + mGroupIp
						+ File.separator + mPath.substring(mPath
								.lastIndexOf(File.separator) + 1); //文件全路径
				//modify by zyf
                String fileDir = mSavePath + File.separator + mGroupIp; //文件目录
                if (!FileUtils.isFileExists(fileDir)) {
					FileUtils.createDirFile(fileDir);
				}
				fileOutputStream = new FileOutputStream(new File(
						fileSavePath));
				bufferOutput = new BufferedOutputStream(fileOutputStream);
				int readSize = -1;
				MyLog.i(TAG,
						"TcpFileClient ClientRunable read start");
				while (-1 != (readSize = dataInput.read(mBuffer))) {
					bufferOutput.write(mBuffer, 0, readSize);
				}
				MyLog.i(TAG, "TcpFileClient ClientRunable read end");
				bufferOutput.flush();
				bufferOutput.close();
				fileOutputStream.close();
				dataInput.close();
				ins.close();
				mSocket.close();
				// when file is received , notify the UI
				IPMSGProtocol ipmsgProtocol = new IPMSGProtocol();
				ipmsgProtocol.setCommandNo(IPMSGConst.IPMSG_GROUP_SENDMSG);
				ipmsgProtocol.setAddObject(mMsg);
				Receiver.engine(mContext).onReceivedMultiSMSdata(null,
						ipmsgProtocol, mTargetIp);
			} catch (IOException e) {
				MyLog.e(TAG, "ClientRunable run", e);
				e.printStackTrace();
			}
		}
	}

}
