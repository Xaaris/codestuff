package mware_lib;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 * @author Fabian
 * 
 *         Ein Stellvertreter-Objekt des Namensdienstes
 */
public class NameServiceProxy extends NameService {

	private ServerSocket serverSocket;
	private Socket socket;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private String serviceHost;
	private int servicePort;
	private static final int LISTENPORT = 50004;

	public NameServiceProxy(String serviceHost, int port) {
		this.serviceHost = serviceHost;
		this.servicePort = port;
		CommunicationModule.debugPrint(this.getClass(), "initialized");
	}

	@Override
	public void rebind(Object servant, String name) {
		RemoteObjectRef rof = ReferenceModule.createNewRemoteRef(servant);
		NameServiceRequest n = new NameServiceRequest("rebind", name, rof);
		try {
			this.socket = new Socket(this.serviceHost, this.servicePort);
			this.output = new ObjectOutputStream(this.socket.getOutputStream());
			this.output.writeObject(n);

			CommunicationModule.debugPrint(this.getClass(),
					"send request (rebind) to nameservice");

			this.output.close();
			this.socket.close();
		} catch (IOException e) {
			System.out.println(this.getClass()
					+ ": cannot send a request (rebind) to nameservice!");
		}
		CommunicationModule.debugPrint(this.getClass(), "new Servant: "
				+ servant + "with name: " + name + " rebinded");
	}

	@Override
	public Object resolve(String name) {
		NameServiceRequest request = null;
		NameServiceRequest n = new NameServiceRequest("resolve", name, null);
		try {
			this.socket = new Socket(this.serviceHost, this.servicePort);
			this.output = new ObjectOutputStream(this.socket.getOutputStream());
			CommunicationModule.debugPrint(this.getClass(),
					"send request (resolve) to nameservice");
			this.output.writeObject(n);

			// TODO zum lokalen testen einen listenport angelegt
			this.serverSocket = new ServerSocket(LISTENPORT);
			// this.serverSocket = new ServerSocket(this.servicePort);

			this.socket = this.serverSocket.accept();
			this.input = new ObjectInputStream(this.socket.getInputStream());
			request = (NameServiceRequest) this.input.readObject();

			this.input.close();
			this.output.close();
			this.socket.close();
			this.serverSocket.close();
		} catch (IOException e) {
			System.out.println(this.getClass()
					+ ": cannot send a request (resolve) to nameservice");
		} catch (ClassNotFoundException e) {
			System.out.println(this.getClass()
					+ ": cannot get object from nameservice");
		}

		CommunicationModule.debugPrint(this.getClass(), "Service: " + name
				+ " resolved");

		return request.getObjectRef();
	}
	

}