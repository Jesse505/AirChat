package com.leadcore.sms.socket.tcp;

import com.leadcore.sms.file.FileState;


public interface TcpServiceListener {
	
	public void onImageReceived(String imageSavePath);
	
	public void onFileReceive(FileState fileState);
	
	public void onFileReceiveSuccess(FileState fileState);
	
	public void onFileReceiveFailed(FileState fileState);
	
}
