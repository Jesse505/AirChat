package com.leadcore.sms.socket.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.sipdroid.sipua.BaseApplication;
import org.sipdroid.sipua.ui.Receiver;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.utils.SessionUtils;

import android.R.integer;
import android.content.Context;

import com.leadcore.sms.entity.Message;
import com.leadcore.sms.file.Constant;
import com.leadcore.sms.socket.udp.IPMSGConst;
import com.leadcore.sms.socket.udp.IPMSGProtocol.ADDITION_TYPE;

/**
 * used to send file by tcp protocol
 * 
 * @author zhaoyifei
 * @since 2016/12/26
 */
public class TcpFileServer implements Runnable {

	private static final String TAG = "groupFile";
	private static TcpFileServer instance;
	private static Context mContext;
	private ServerSocket mServerSocket;
	private Thread mThread;

	public boolean send_flag = false; // 控制server端发送文件
	public boolean stop = true;

	private TcpFileServer() {
		try {
			mServerSocket = new ServerSocket(Constant.TCP_FILE_SENDER_PORT);
			MyLog.i(TAG, "new ServerSocket succerss");
		} catch (IOException e) {
			MyLog.e(TAG, "new ServerSocket failed ", e);
		}
		mThread = new Thread(this);
	}

	private TcpFileServer(Context context) {
		this();
		mContext = context;
	}

	public static TcpFileServer getInstance(Context context) {
		if (instance == null) {
			synchronized (TcpFileServer.class) {
				if (instance == null) {
					instance = new TcpFileServer(context);
				}
			}
		}
		return instance;
	}

	@Override
	public void run() {
		while (!stop) {
			if (send_flag) {
				try {
					Socket socket = mServerSocket.accept();
					MyLog.i(TAG, "mServerSocket.accept()");
//					new Thread(new SendServerThread(socket)).start();
					BaseApplication.GROUP_IMAGE_RECEIVER.execute(new SendServerThread(socket));
				} catch (Exception e) {
					MyLog.e(TAG, "Client connect failed ", e);
					send_flag = false;
				}
			}
		}
	}

	public void startServer() {
		stop = false;
		send_flag = true;
		if (!mThread.isAlive()) {
			mThread = new Thread(this);
			mThread.start();
		}
	}

	public void release() {
		send_flag = false;
		stop = true;
		Socket socket = null;
		OutputStream outs = null;
		DataOutputStream dataOutput = null;
		if (null != mServerSocket && !mServerSocket.isClosed()) {
			try {
				// 新建一个socket让ServerSocket跳出accept 再分别关闭该socket和ServerSocket
				socket = new Socket("localhost", Constant.TCP_FILE_SENDER_PORT);
				outs = socket.getOutputStream();
				dataOutput = new DataOutputStream(outs);
				dataOutput.writeUTF("");
				dataOutput.flush();
				dataOutput.close();
				outs.close();
				if (null != socket && !socket.isClosed()) {
					socket.close();
					socket = null;
				}
				mServerSocket.close();
				mServerSocket = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		instance = null;

	}

	class SendServerThread implements Runnable {

		private Socket mSocket;
		private InputStream ins;
		private DataInputStream dataInput;
		private OutputStream outs;
		private DataOutputStream dataOutput;
		private FileInputStream input;
		private byte[] mBuffer = new byte[Constant.READ_BUFFER_SIZE];

		public SendServerThread(Socket socket) {
			mSocket = socket;
		}

		@Override
		public void run() {
			try {
				MyLog.i("groupFile", "TcpFileServer SendServerThread run start");
				ins = mSocket.getInputStream();
				dataInput = new DataInputStream(ins);
				String filePath = dataInput.readUTF().toString();
				if (filePath.equals("")) {
					return;
				}
				MyLog.i("groupFile", "TcpFileServer SendServerThread filePath:"
						+ filePath);
				input = new FileInputStream(new File(filePath));
				outs = mSocket.getOutputStream();
				dataOutput = new DataOutputStream(outs);
				int readSize = -1;
				while ((readSize = input.read(mBuffer)) != -1) {
					MyLog.i("groupFile",
							"TcpFileServer SendServerThread run write");
					dataOutput.write(mBuffer, 0, readSize);
				}
				dataOutput.flush();
				dataOutput.close();
				outs.close();
				input.close();
				dataInput.close();
				ins.close();
				mSocket.close();
				MyLog.i("group", "TcpFileServer SendServerThread run end");
			} catch (IOException e) {
				MyLog.e(TAG, "SendServerThread run", e);
				e.printStackTrace();
				send_flag = false;
			}

		}

	}

}
