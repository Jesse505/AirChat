package org.zoolu.net;


import java.io.IOException;
import java.io.InterruptedIOException;

import org.sipdroid.sipua.utils.MyLog;

public class MultiUdpProvider extends Thread {

	/** The reading buffer size */
	public static final int BUFFER_SIZE = 65535;

	/**
	 * Default value for the maximum time that the UDP receiver can remain
	 * active after been halted (in milliseconds)
	 */
	public static final int DEFAULT_SOCKET_TIMEOUT = 2000; // 2sec

	/** Muti UDP socket */
	MultiUdpSocket socket;

	/**
	 * Maximum time that the UDP receiver can remain active after been halted
	 * (in milliseconds)
	 */
	int socket_timeout;

	/**
	 * Maximum time that the UDP receiver remains active without receiving UDP
	 * datagrams (in milliseconds)
	 */
	long alive_time;

	/**
	 * Minimum size for received packets. Shorter packets are silently
	 * discarded.
	 */
	int minimum_length;

	/** Whether it has been halted */
	boolean stop;

	/** Whether it is running */
	boolean is_running;

	/** UdpProvider listener */
	UdpProviderListener listener;

	/** Creates a new MutiUdpProvider */
	public MultiUdpProvider(MultiUdpSocket socket, UdpProviderListener listener) {
		init(socket, 0, listener);
		start();
	}

	/** Creates a new MutiUdpProvider */
	public MultiUdpProvider(MultiUdpSocket socket, long alive_time,
			UdpProviderListener listener) {
		init(socket, alive_time, listener);
		start();
	}

	/** Inits the MutiUdpProvider */
	private void init(MultiUdpSocket socket, long alive_time,
			UdpProviderListener listener) {
		this.listener = listener;
		this.socket = socket;
		this.socket_timeout = DEFAULT_SOCKET_TIMEOUT;
		this.alive_time = alive_time;
		this.minimum_length = 0;
		this.stop = false;
		this.is_running = true;
	}

	/** Gets the MutiUdpSocket */
	public MultiUdpSocket getMultiUdpSocket() {
		return socket;
	}

	/** Sets a new UdpSocket */
	/*
	 * public void setUdpSocket(UdpSocket socket) { this.socket=socket; }
	 */

	/** Whether the service is running */
	public boolean isRunning() {
		return is_running;
	}

	/**
	 * Sets the maximum time that the UDP service can remain active after been
	 * halted
	 */
	public void setSoTimeout(int timeout) {
		socket_timeout = timeout;
	}

	/**
	 * Gets the maximum time that the UDP service can remain active after been
	 * halted
	 */
	public int getSoTimeout() {
		return socket_timeout;
	}

	/**
	 * Sets the minimum size for received packets. Packets shorter than that are
	 * silently discarded.
	 */
	public void setMinimumReceivedDataLength(int len) {
		minimum_length = len;
	}

	/**
	 * Gets the minimum size for received packets. Packets shorter than that are
	 * silently discarded.
	 */
	public int getMinimumReceivedDataLength() {
		return minimum_length;
	}

	/** Sends a UdpPacket */
	public void send(UdpPacket packet) throws IOException {
		if (!stop)
			socket.send(packet);
	}

	/** Stops running */
	public void halt() {
		stop = true;
		socket.close(); // modified
	}

	/** The main thread */
	public void run() {
		byte[] buf = new byte[BUFFER_SIZE];
		UdpPacket packet = new UdpPacket(buf, buf.length);

		Exception error = null;
		long expire = 0;
		if (alive_time > 0)
			expire = System.currentTimeMillis() + alive_time;
		try {
			MyLog.e("group", "MutiUdpProvider run()");
//			socket.setSoTimeout(socket_timeout); modified
			// loop
			while (!stop) {
				
				try {
					MyLog.i("group", "MutiUdpProvider socket.receive start");
					socket.receive(packet);
					MyLog.i("group", "MutiUdpProvider socket.receive end");
				} catch (InterruptedIOException ie) {
					MyLog.e("group", "MutiUdpProvider InterruptedIOException",ie);
					if (alive_time > 0 && System.currentTimeMillis() > expire)
						halt();
					continue;
				}
				if (packet.getLength() >= minimum_length) {
					if (listener != null)
						listener.onReceivedMultiPacket(this, packet);
					if (alive_time > 0)
						expire = System.currentTimeMillis() + alive_time;
				}
				packet = new UdpPacket(buf, buf.length);
			}
		} catch (Exception e) {
			MyLog.e("group", "MultiUdpProvider receive ",e);
			error = e;
			stop = true;
		}
		is_running = false;
		if (listener != null)
			listener.onMultiServiceTerminated(this, error);
		listener = null;
	}

	/** Gets a String representation of the Object */
	public String toString() {
		return "Multiudp:" + socket.getLocalAddress() + ":" + socket.getLocalPort();
	}
}