package org.sipdroid.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class MultiRtpSocket {

	/** UDP MulticastSocket */
	SipdroidMultiSocket socket;
	DatagramPacket datagram;
	
	/** Remote address */
	InetAddress r_addr;

	/** Remote port */
	int r_port;
	
	/** Group IP */
	String groupIP;

	/** Creates a new RTP MultiSocket (only receiver) */
	public MultiRtpSocket(SipdroidMultiSocket datagram_socket) {
		socket = datagram_socket;
		r_addr = null;
		r_port = 0;
		datagram = new DatagramPacket(new byte[1],1);
	}

	/** Creates a new RTP MultiSocket (sender and receiver) */
	public MultiRtpSocket(SipdroidMultiSocket datagram_socket,
			InetAddress remote_address, int remote_port) {
		socket = datagram_socket;
		r_addr = remote_address;
		r_port = remote_port;
		datagram = new DatagramPacket(new byte[1],1);
	}

	/** Returns the RTP SipdroidMultiSocket */
	public SipdroidMultiSocket getDatagramSocket() {
		return socket;
	}

	/** Receives a RTP packet from this socket */
	public void receive(RtpPacket rtpp) throws IOException {
		datagram.setData(rtpp.packet);
		datagram.setLength(rtpp.packet.length);
		socket.receive(datagram);
//		if (!socket.isConnected())
//			socket.connect(datagram.getAddress(),datagram.getPort());
		rtpp.packet_len = datagram.getLength();
	}

	/** Sends a RTP packet from this socket */
	public void send(RtpPacket rtpp) throws IOException {
		datagram.setData(rtpp.packet);
		datagram.setLength(rtpp.packet_len);
		datagram.setAddress(r_addr);
		datagram.setPort(r_port);
		socket.setTimeToLive(4);
		socket.send(datagram);
	}

	/** Closes this socket */
	public void close() { // socket.close();
	}
	
	public InetAddress getDstAddress(){
		return r_addr;
	}
	/**
	 * 
	 * @return The sourceAddress of this datagramPacket
	 */
	public InetAddress getSourceAddress(){
		return datagram.getAddress();
	}
	
	public int getDstPort(){
		return r_port;
	}
	
	public void setGroupIp(String groupIP) throws IOException{
		InetAddress group = InetAddress.getByName(groupIP);
		socket.joinGroup(group);
		this.groupIP = groupIP;
	}
	
	public String getGroupIp() throws IOException{
//		InetAddress group = InetAddress.getByName(groupIP);
//		socket.joinGroup(group);
		return groupIP;
	}
	
}
