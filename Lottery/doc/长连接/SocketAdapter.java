package cn.itcast.lottery.util.platform;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.util.Log;

/**
 * This class is a proxy to java.net.Socket to provide a common access to a socket resource on all mobile platforms.
 * 
 * A portable code must use this class only to use sockets, and must take care of closing the SocketAdapter when not used anymore.
 * 
 * <pre>
 * Example:
 * 
 *   void socketAccessExample(String host, int port) {
 *      SocketAdapter sa = new SocketAdapter(host, port); // opens the SocketConnection
 *      InputStream is = sa.openInputStream();  // opens the InputStream
 *      while( (char c = is.read()) != -1) {    // read till the end of the file
 *         System.out.print(c);
 *      }
 *      is.close();                             // closes the InputStream
 *      sa.close();                             // closes the SocketConnection
 * </pre>
 */
public class SocketAdapter {
	public static final byte DELAY = 0;
	public static final byte KEEPALIVE = 1;
	public static final byte LINGER = 2;
	public static final byte RCVBUF = 3;
	public static final byte SNDBUF = 4;
	public static final int READ_WRITE = 0;

	private static final String TAG = "SocketAdapter";

	/** The underlying Socket */
	private Socket socket;
	private boolean sendSwitch=true;
	

	public boolean isSendSwitch() {
		return sendSwitch;
	}
	
	public void setSendSwitch(boolean sendSwitch) {
		this.sendSwitch = sendSwitch;
	}
	// -------------------------------------------------------------
	// Constructors


	/**
	 * Creates a FileAdapter instance, opening the underlying SocketConnection.
	 * 
	 * @param server
	 *            is the remote server (without protocol specification)
	 * @param port
	 *            is the remote port
	 * 
	 * @throws IOException
	 *             if the connection cannot be established
	 */
	private SocketAdapter(String host, int port) throws IOException {
		socket = new Socket(host, port);
		socket.setKeepAlive(true);
	}

	private static SocketAdapter socketAdapter;

	public static synchronized SocketAdapter getSocketAdapterInstance(String host, int port) throws IOException {
		if (socketAdapter == null) {
			socketAdapter = new SocketAdapter(host, port);
			Log.i(TAG, "new socket");
		}
		return socketAdapter;
	}

	public static synchronized SocketAdapter getSocketAdapterInstance() {
		return socketAdapter;
	}

	/**
	 * Open and return an input stream for this Socket.
	 */
	public InputStream openInputStream() throws IOException {
		return socket.getInputStream();
	}

	/**
	 * Open and return an output stream for this Socket.
	 */
	public OutputStream openOutputStream() throws IOException {
		return socket.getOutputStream();
	}

	/**
	 * Close this Socket
	 */
	public void close() {
		try {
			Thread.sleep(100);
			socket.close();
			socketAdapter = null;
		} catch (Exception e) {
			cn.itcast.lottery.util.Log.info(Log.getStackTraceString(e));
		}
		socket = null;
	}

	public void setSocketOption(byte option, int value) throws IOException {
		if (socket == null) {
			throw new IOException("Socket not available");
		}
		switch (option) {
			case DELAY:
				socket.setTcpNoDelay(value == 0);
				break;
			case KEEPALIVE:
				socket.setKeepAlive(value == 0);
				break;
			case LINGER:
				socket.setSoLinger(true, value);
				break;
			case RCVBUF:
				socket.setReceiveBufferSize(value);
				break;
			case SNDBUF:
				socket.setSendBufferSize(value);
				break;
			default:
		}
	}
}

