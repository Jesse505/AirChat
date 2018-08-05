package com.leadcore.sms.socket.tcp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;


import com.leadcore.sms.file.Constant;

public class TcpSocketManager {
	
	public static TcpSocketManager instance;
	public Vector<Socket> sockets = new Vector<Socket>();
	private byte[] mBuffer = new byte[Constant.READ_BUFFER_SIZE];
    private OutputStream output = null;
    private DataOutputStream dataOutput;
	
	private TcpSocketManager(){
		
	}
	
	public static TcpSocketManager getInstance(){
		if (instance == null) {
			synchronized (TcpSocketManager.class) {
				if (instance == null) {
					instance = new TcpSocketManager();
				}
			}
		}	
		return instance;
	}
	
	public void add(Socket socket){
		sockets.add(socket);
	}
	
	public void remove(Socket socket){
		sockets.remove(socket);
	}
	
	public synchronized void publish(Socket currentSocket, InputStream in){
		for (int i = 0; i < sockets.size(); i++) {
			Socket socket = sockets.get(i);
			if (!currentSocket.equals(socket)) {
				try {
					output = socket.getOutputStream();
					dataOutput = new DataOutputStream(output);
					int readSize = -1;
					while ((readSize = in.read(mBuffer)) != -1) {
						dataOutput.write(mBuffer, 0, readSize);
						dataOutput.flush();
					}
					output.close();
					dataOutput.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//分发完文件流之后，清空连接socket集合，等待下一波连接socket
//		sockets.clear();
	}
}
