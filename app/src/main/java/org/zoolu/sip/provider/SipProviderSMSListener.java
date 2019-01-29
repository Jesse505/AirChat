package org.zoolu.sip.provider;

import com.leadcore.sms.socket.udp.IPMSGProtocol;

public interface SipProviderSMSListener {
	
	public void onReceivedSMSdata(IPMSGProtocol ipmsgProtocol,String senderIP);
	
	public void onReceivedMultiSMSdata(IPMSGProtocol ipmsgProtocol,String senderIP);
}
