package mware_lib;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 
 * @author Francis und Fabian
 * 
 *         Hilfs-Thread der verwendet wird um von der Client- zur Server-Seite
 *         einen Request zu senden und sobald ein Reply zurueck kommt vom
 *         lokalen Kommunikations-Modul geweckt wird, dass ein Reply
 *         eingetroffen ist
 */
public class CommunicationModuleThread extends Thread {

	private MessageADT sendMessage;
	private MessageADT receivedMessage;
	private Socket socket;
	private ObjectOutputStream output;

	public CommunicationModuleThread(MessageADT m) {
		CommunicationModule.debugPrint(this.getClass(), "initialized");
		this.sendMessage = m;
		try {
			// TODO zum lokalen testen hier der port des servers angegeben
			this.socket = new Socket(m.getObjectRef().getInetAddress(), 50001);
			// this.socket = new Socket(m.getObjectRef().getInetAddress(),
			// CommunicationModule.getCommunicationmoduleport());

			this.output = new ObjectOutputStream(this.socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		synchronized (this) {
			try {
				CommunicationModule.debugPrint(this.getClass(),
						"send message to remote server");

				this.output.writeObject(sendMessage);

				CommunicationModule.debugPrint(this.getClass(),
						"waiting for reply of own communication module");
				this.wait();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				CommunicationModule.debugPrint(this.getClass(),
						"someone interrupted me");
				notify();
			}
		}
		CommunicationModule.removeThreadFromList(this);
	}

	public MessageADT getReceivedMessage() {
		return receivedMessage;
	}

	/**
	 * Setzt die empfangene Nachricht die an den Proxy weitergeleitet wird
	 * 
	 * @param m
	 *            empfangene MessageADT
	 */
	public void setReceivedMessage(MessageADT m) {
		this.receivedMessage = m;
	}

	public MessageADT getSendMessage() {
		return sendMessage;
	}
}