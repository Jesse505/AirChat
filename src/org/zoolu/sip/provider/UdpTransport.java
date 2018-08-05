/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.zoolu.sip.provider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sipdroid.sipua.BaseActivity;
import org.sipdroid.sipua.ui.Receiver;
import org.sipdroid.sipua.utils.ByteUtils;
import org.sipdroid.sipua.utils.MyLog;
import org.zoolu.net.IpAddress;
import org.zoolu.net.MultiUdpProvider;
import org.zoolu.net.MultiUdpSocket;
import org.zoolu.net.UdpPacket;
import org.zoolu.net.UdpProvider;
import org.zoolu.net.UdpProviderListener;
import org.zoolu.net.UdpSocket;
import org.zoolu.sip.message.Message;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;

import com.leadcore.sms.entity.Group;
import com.leadcore.sms.socket.udp.IPMSGConst;
import com.leadcore.sms.socket.udp.IPMSGProtocol;

/**
 * UdpTransport provides an UDP transport service for SIP and SMS.
 */
class UdpTransport implements Transport, UdpProviderListener {
	/** UDP protocol type */
	public static final String PROTO_UDP = "udp";

	/** UDP provider */
	UdpProvider udp_provider;

	/** The protocol type */
	String proto;

	/** Transport listener */
	TransportListener listener;
	int port; // modified
	
	HashMap<String, MultiUdpProvider> mutiProviders = new HashMap<String, MultiUdpProvider>();
	MulticastLock multicastLock;
	
	/** Creates a new UdpTransport */
	public UdpTransport(int port, TransportListener listener)
			throws IOException {
		this.listener = listener;
		UdpSocket socket = new UdpSocket(port);
		udp_provider = new UdpProvider(socket, this);
		this.port = socket.getLocalPort();
		if (port == IPMSGConst.PORT) {
			initMutiProviders();
		}
	}

	/** Creates a new UdpTransport */
	public UdpTransport(int port, IpAddress ipaddr, TransportListener listener)
			throws IOException {
		this.listener = listener;
		UdpSocket socket = new UdpSocket(port, ipaddr);
		udp_provider = new UdpProvider(socket, this);
		this.port = socket.getLocalPort();
		if (port == IPMSGConst.PORT) {
			initMutiProviders();
		}
	}

	/** Creates a new UdpTransport 
	 * @throws IOException */
	public UdpTransport(UdpSocket socket, TransportListener listener) throws IOException {
		this.listener = listener;
		udp_provider = new UdpProvider(socket, this);
		this.port = socket.getLocalPort();
		initMutiProviders();
	}

	/** Gets protocol type */
	public String getProtocol() {
		return PROTO_UDP;
	}

	public int getPort() {
		return port;
	}
	
	/** Sends a Message to a destination address and port */
	public void sendMessage(Message msg, IpAddress dest_ipaddr, int dest_port)
			throws IOException {
		if (udp_provider != null) {
			byte[] data = msg.toString().getBytes();
			UdpPacket packet = new UdpPacket(data, data.length);
			packet.setIpAddress(dest_ipaddr);
			packet.setPort(dest_port);
			udp_provider.send(packet);
		}
	}

	/**Sends SMS to a destination address and port */
	public void sendSMS(IPMSGProtocol ipmsgProtocol, IpAddress dest_ipaddr, int dest_port)
	throws IOException{
		if (udp_provider != null) {
			MyLog.i("UdpTransport","sendSMS toString>>>" + ipmsgProtocol.toString());
//			MyLog.i("UdpTransport","sendSMS" + ipmsgProtocol.getProtocolJSON());
//			byte[] data = ipmsgProtocol.getProtocolJSON().getBytes();
			byte[] data = ByteUtils.ObjectToByte(ipmsgProtocol);
			UdpPacket packet = new UdpPacket(data, data.length);
			packet.setIpAddress(dest_ipaddr);
			packet.setPort(dest_port);
			udp_provider.send(packet);
		}
	}
	
	/**Sends MutiSMS to a destination address and port */
	public void sendMultiSMS(IPMSGProtocol ipmsgProtocol, IpAddress dest_ipaddr, int dest_port)
	throws IOException{
		MyLog.i("group", "UdpTransport_sendMultiSMS mutiProviders.size(): " + mutiProviders.size());
		if (mutiProviders.size() != 0) {
			MyLog.e("group", "UdpTransport_sendMutiSMS mutiProviders.size: " + mutiProviders.size() + " dest_ip: " + dest_ipaddr.toString());
			byte[] data = ByteUtils.ObjectToByte(ipmsgProtocol);
			UdpPacket packet = new UdpPacket(data, data.length,dest_ipaddr,dest_port);
//			packet.setIpAddress(dest_ipaddr);
//			packet.setPort(dest_port);
//			udp_provider.send(packet);
			(mutiProviders.get(dest_ipaddr.toString())).send(packet);
		}
	}
	
	/**Sends Muti msg protocol to a destination address and port when use ptt*/
	public void sendMultiSMS(String msg, IpAddress dest_ipaddr, int dest_port)
	throws IOException{
		if (mutiProviders.size() != 0) {
			byte[] data = msg.getBytes();
			UdpPacket packet = new UdpPacket(data, data.length,dest_ipaddr,dest_port);
//			packet.setIpAddress(dest_ipaddr);
//			packet.setPort(dest_port);
//			udp_provider.send(packet);
			(mutiProviders.get(dest_ipaddr.toString())).send(packet);
		}
	}
	
	/** Stops running */
	public void halt() {
		MyLog.i("zhaoyf", "halt " );
		if (udp_provider != null)
			udp_provider.halt();
		if (mutiProviders != null && mutiProviders.size() != 0) {
			Iterator iter = mutiProviders.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, MultiUdpProvider> entry = (Map.Entry<String, MultiUdpProvider>) iter.next();
				MultiUdpProvider mutiUdpProvider = entry.getValue();
				mutiUdpProvider.halt();
			}
		}
		if (null != multicastLock) {
			multicastLock.release();
			multicastLock = null;
		}
	}

	/** Gets a String representation of the Object */
	public String toString() {
		if (udp_provider != null)
			return udp_provider.toString();
		else
			return null;
	}

	private void newMultiProvider(String groupIP) throws IOException{
		MyLog.e("group", "UdpTransport_newMutiProvider");
		if (mutiProviders.containsKey(groupIP)) {
			return;
		}
		if (null == multicastLock) {
			WifiManager wm = (WifiManager) Receiver.mContext.getSystemService(Context.WIFI_SERVICE);
			multicastLock = wm.createMulticastLock("sipdroid.UdpTransport");
			multicastLock.acquire();
		}
		MultiUdpSocket mutiUdpSocket = new MultiUdpSocket(IPMSGConst.GROUP_PORT);
		mutiUdpSocket.setGroupIp(groupIP);
		MultiUdpProvider mutiUdpProvider = new MultiUdpProvider(mutiUdpSocket, this);
		mutiProviders.put(groupIP, mutiUdpProvider);
	}
	
	// ************************* Callback methods *************************

	/** When a new UDP datagram is received. */
	public void onReceivedPacket(UdpProvider udp, UdpPacket packet) {
		//modify by zyf 
		if (packet.getPort() == IPMSGConst.PORT) {
			byte[] data = packet.getData();
//			String dataString = new String(data, 0, packet.getLength());
//			IPMSGProtocol ipmsgProtocol = new IPMSGProtocol(dataString);
//			MyLog.i("UdpTransport","receive" + ipmsgProtocol.getProtocolJSON());
			IPMSGProtocol ipmsgProtocol = (IPMSGProtocol) ByteUtils.ByteToObject(data);
			String senderIP = packet.getIpAddress().toString();
			if (ipmsgProtocol.getCommandNo() == IPMSGConst.IPMSG_NEW_GROUP
					|| ipmsgProtocol.getCommandNo() == IPMSGConst.IPMSG_GROUP_ADD_MEM) {
				Group group = (Group)ipmsgProtocol.getAddObject();
				try {
					newMultiProvider(group.getStrIP());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (listener != null) 
				listener.onReceivedMessage(this, ipmsgProtocol, senderIP);
			
		}else {
			Message msg = new Message(packet);
			MyLog.i("UdpTransport","UdpTransport onReceivedPacket : \r\n" + msg.toString());
			msg.setRemoteAddress(packet.getIpAddress().toString());
			msg.setRemotePort(packet.getPort());
			MyLog.i("UdpTransport", "UdpTransport getIpAddress:"+packet.getIpAddress().toString()+" port:"+packet.getPort());
			msg.setTransport(PROTO_UDP);
			if (listener != null)
				listener.onReceivedMessage(this, msg);
		}
		
	}

	/** When DatagramService stops receiving UDP datagrams. */
	public void onServiceTerminated(UdpProvider udp, Exception error) {
		if (listener != null)
			listener.onTransportTerminated(this, error);
		UdpSocket socket = udp.getUdpSocket();
		if (socket != null)
			try {
				socket.close();
			} catch (Exception e) {
			}
		this.udp_provider = null;
		this.listener = null;
	}

	/** When a new Muti UDP datagram is received. */
	@Override
	public void onReceivedMultiPacket(MultiUdpProvider udp, UdpPacket packet) {
		byte[] data = packet.getData();
		IPMSGProtocol ipmsgProtocol = (IPMSGProtocol) ByteUtils.ByteToObject(data);
		String senderIP = packet.getIpAddress().toString();
		if (null != listener) {
			listener.onReceivedMultiMessage(this, ipmsgProtocol, senderIP);
		}
		MyLog.i("group", "UdpTransport_onReceivedMultiPacket senderIP:" + senderIP);
	}

	/** When DatagramService stops receiving Muti UDP datagrams. */
	@Override
	public void onMultiServiceTerminated(MultiUdpProvider udp, Exception error) {
		MyLog.e("group", "onMultiServiceTerminated");
		if (listener != null)
			listener.onTransportTerminated(this, error);
		MultiUdpSocket socket = udp.getMultiUdpSocket();
		if (socket != null)
			try {
				socket.close();
			} catch (Exception e) {
			}
	}
	
	public void initMutiProviders() throws IOException{
		MyLog.i("group", "initMutiProviders");
		if (null != BaseActivity.mApplication.getOnlineGroupIPs()) {
			for (String ip : BaseActivity.mApplication.getOnlineGroupIPs()) {
				MyLog.i("group", "UdpTransport_initMutiProviders ip: " + ip);
				newMultiProvider(ip);
			}
		}
	}

	public void stopMultiProvider(String groupIp){
		//modify by zyf
		if (mutiProviders.get(groupIp) != null) {
			mutiProviders.get(groupIp).halt();
		}
		mutiProviders.remove(groupIp);
	}

}
