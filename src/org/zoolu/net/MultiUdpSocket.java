package org.zoolu.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import org.sipdroid.sipua.utils.MyLog;

public class MultiUdpSocket {

	/** MulticastSocket */
	MulticastSocket socket;
	
	String groupIP;

	/** Creates a new UdpSocket 
	 * @throws IOException */
	public MultiUdpSocket() throws IOException {
		socket = new MulticastSocket();
	}

	/** Creates a new UdpSocket 
	 * @throws IOException */
	public MultiUdpSocket(int port) throws IOException {
		socket = new MulticastSocket(port);
	}

	/** Creates a new UdpSocket */
	MultiUdpSocket(MulticastSocket sock) {
		socket = sock;
	}

	/** Creates a new UdpSocket 
	 * @throws IOException */
	public MultiUdpSocket(int port, IpAddress ipaddr)
			throws IOException {
		socket = new MulticastSocket(port);
//		socket.joinGroup(ipaddr.getInetAddress());
	}

	/** Closes this datagram socket. */
	public void close() {
		socket.close();
	}

	/** Gets the local address to which the socket is bound. */
	public IpAddress getLocalAddress() {
		return new IpAddress(socket.getInetAddress());
	}

	/** Gets the port number on the local host to which this socket is bound. */
	public int getLocalPort() {
		return socket.getLocalPort();
	}

	/** Gets the socket timeout. */
	public int getSoTimeout() throws java.net.SocketException {
		return socket.getSoTimeout();
	}

	/**
	 * Enables/disables socket timeout with the specified timeout, in
	 * milliseconds.
	 */
	public void setSoTimeout(int timeout) throws java.net.SocketException {
		socket.setSoTimeout(timeout);
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

	/** Receives a datagram packet from this socket. */
	public void receive(UdpPacket pkt) throws java.io.IOException {
		DatagramPacket dgram = pkt.getDatagramPacket();
//		InetAddress group = InetAddress.getByName(getGroupIp());
//		socket.joinGroup(group);
		socket.receive(dgram);
		pkt.setDatagramPacket(dgram);
	}

	/** Sends an UDP packet from this socket. */
	public void send(UdpPacket pkt) throws java.io.IOException {
//		InetAddress group = InetAddress.getByName(getGroupIp());
//		socket.joinGroup(group);
		socket.setTimeToLive(4);
		socket.send(pkt.getDatagramPacket());
		
	}

	/** Converts this object to a String. */
	public String toString() {
		return socket.toString();
	}
}
