package com.leadcore.sms.socket.tcp;

import com.leadcore.sms.file.FileState;

public interface TcpClientListener {
	public void onFileSend(FileState fileState);
	public void onFileSendSuccess(FileState fileState);
	public void onFileSendFailed(FileState fileState);
}
