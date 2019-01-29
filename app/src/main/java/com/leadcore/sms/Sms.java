package com.leadcore.sms;

import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipProviderSMSListener;

import com.leadcore.sms.socket.udp.IPMSGProtocol;

public class Sms implements SipProviderSMSListener{

	
	protected SipProvider sipProvider;
	SmsListener listener;
	
	
	public Sms(SipProvider sipProvider, SmsListener listener) {
		super();
		this.sipProvider = sipProvider;
		this.listener = listener;
	}

	public void listen(){
		sipProvider.addSMSListener(this);
	}
	
	public void sendSMSdata(IPMSGProtocol ipmsgProtocol,String targetIP){
		sipProvider.sendSMS(ipmsgProtocol, targetIP);
	}

	public void sendMultiSMSdata(IPMSGProtocol ipmsgProtocol,String targetIP){
		sipProvider.sendMultiSMS(ipmsgProtocol, targetIP);
	}
	
	public void stopMultiProvider(String groupIp){
		sipProvider.stopMultiProvider(groupIp);
	}

	@Override
	public void onReceivedSMSdata(IPMSGProtocol ipmsgProtocol, String senderIP) {
		// TODO Auto-generated method stub
		listener.onReceivedSMSdata(this, ipmsgProtocol, senderIP);
	}

	@Override
	public void onReceivedMultiSMSdata(IPMSGProtocol ipmsgProtocol,
			String senderIP) {
		// TODO Auto-generated method stub
		listener.onReceivedMultiSMSdata(this, ipmsgProtocol, senderIP);
	}

}
