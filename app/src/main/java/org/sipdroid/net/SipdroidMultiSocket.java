package org.sipdroid.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketOptions;

import org.sipdroid.net.impl.OSNetworkSystem;
import org.sipdroid.net.impl.PlainDatagramSocketImpl;
import org.sipdroid.sipua.utils.MyLog;

public class SipdroidMultiSocket extends MulticastSocket {

	PlainDatagramSocketImpl impl;
	public static boolean loaded = false;
	
	public SipdroidMultiSocket(int port) throws IOException {
		super(!loaded?port:0);
		if (loaded) {
			impl = new PlainDatagramSocketImpl();
			impl.create();
			impl.bind(port,InetAddress.getByName("0"));
		}
	}
	
	public void close() {
		super.close();
		if (loaded) impl.close();
	}
	
	public void setSoTimeout(int val) throws SocketException {
		if (loaded) impl.setOption(SocketOptions.SO_TIMEOUT, val);
		else super.setSoTimeout(val);
	}
	
	public void receive(DatagramPacket pack) throws IOException {
		if (loaded) impl.receive(pack);
		else super.receive(pack);
	}
	
	public void send(DatagramPacket pack) throws IOException {
		if (loaded) impl.send(pack);
		else super.send(pack);
	}
	
	public boolean isConnected() {
		if (loaded) return true;
		else return super.isConnected();
	}
	
	public void disconnect() {
		if (!loaded) super.disconnect();
	}
	
	public void connect(InetAddress addr,int port) {
		if (!loaded) super.connect(addr,port);
	}

	static {
			try {
		        System.loadLibrary("OSNetworkSystem");
		        OSNetworkSystem.getOSNetworkSystem().oneTimeInitialization(true);
		        SipdroidSocket.loaded = true;
			} catch (Throwable e) {
				MyLog.e("ptt", "OSNetworkSystem", e);
			}
	}
}
