package multithreaded_server.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import multithreaded_server.packet_handler.PacketElement;
import multithreaded_server.packet_handler.PacketHandler;

/**
 * BasicClient
 * <p>
 * This class implements a client object.
 * 
 * @since 0.1.0
 * @version 0.3.2
 * @author Peter Voigt
 *
 */
public abstract class BasicClient {

	private int port = 7070;
	private String ip = "localhost";
	private Socket clientSocket;
	public boolean isActive = false;
	public int currentID = -1;
	private ServerListener serverListener;
	public PacketHandler packetHandler;
	private DataOutputStream dataOutputStream;

	/**
	 * This method is called when the client receives a packet from the server
	 * 
	 * @param elements
	 *            The packets
	 */
	public abstract void messageFromServer(PacketElement[] elements);

	/**
	 * This method is called when the client disconnects from the server
	 */
	public abstract void disconnectedFromServer();

	/**
	 * This method is called when the client is unable to connect to the server
	 */
	public abstract void unableToConnect();

	/**
	 * This method attempts to start a connection to the server
	 * 
	 * @param port
	 *            The server port
	 * @param ip
	 *            The server IP
	 * 
	 * @return TRUE if the client successfully connected to the server; FALSE if
	 *         something went wrong and the client didn't connect to the server
	 */
	public boolean connectToServer(int port, String ip) {
		this.port = port;
		this.ip = ip;

		packetHandler = new PacketHandler();

		try {
			clientSocket = new Socket(this.ip, this.port);
		} catch (UnknownHostException e) {
			unableToConnect();
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			unableToConnect();
			e.printStackTrace();
			return false;
		}
		isActive = true;
		serverListener = new ServerListener(this);
		Thread t = new Thread(serverListener);
		t.start();
		return true;
	}

	/**
	 * This method stops the client and disconnects it from the server
	 */
	public void stopClient() {
		serverListener.stopServerListener();
		PacketElement[] element = { new PacketElement("[disconnect]".getBytes(), PacketElement.SERVER_MESSAGE) };
		sendToServer(element);
		isActive = false;
		if (clientSocket != null) {
			try {
				clientSocket.close();
				clientSocket = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		disconnectedFromServer();
	}

	/**
	 * This method sends data to the server
	 * 
	 * @param elements
	 *            The data
	 */
	public void sendToServer(PacketElement[] elements) {
		if (isActive == true) {
			try {
				dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
				for (int i = 0; i < elements.length; i++) {
					packetHandler.addElement(elements[i]);
				}
				byte[] data = packetHandler.createPacket();
				dataOutputStream.writeInt(data.length);
				dataOutputStream.write(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method returns the server IP
	 * 
	 * @return The server IP
	 */
	public String getIP() {
		return ip;
	}

	/**
	 * This method returns the socket object of the client
	 * 
	 * @return The socket object
	 */
	public Socket getClientSocket() {
		return clientSocket;
	}

}
