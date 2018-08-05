package com.leadcore.sms;


import com.leadcore.sms.socket.udp.IPMSGProtocol;

public interface SmsListener {
	
	public void onReceivedSMSdata(Sms sms, IPMSGProtocol ipmsgProtocol,String senderIP);
	
	public void onReceivedMultiSMSdata(Sms sms, IPMSGProtocol ipmsgProtocol,String senderIP);
}
