package proxyserver;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import account.Email;
import account.POP3Account;

/**
 * Klasse zur Kommunikation mit Mailclients. Bearbeitet die Anfragen der Clients.
 * 
 * @author Fabian Reiber, Francis Opoku
 * TODO: serveradress setzen, aber unbekannt -> wie also?
 * TODO: sequenzdiagramm anpassen -> nur noch ein proxyclient
 * TODO: CAPA und AUTH fehlen noch
 * 
 */
public class ProxyServer extends Thread {

	private Socket connection;
	private Command cObj;
	private boolean confirmed;
	private static String USER, PASS;
	private POP3Account ownAccount;
	private List<Email> mailList;
	
	/**
	 * Liste der Accounts von denen die Mails abegrufen werden sollen
	 * 
	 * nicht noetig, da email client sich connected, dann authentifiziert, die mails
	 * holt, diese löscht, und dann wieder disconnected. besitzt email client nun noch einen
	 * 2. pop3account, dann baut dieser wieder eine verbindung mit dem pop3_proxy auf und
	 * die prozedur faengt wieder von vorne an ---> die verbindung bleibt nicht die ganze
	 * zeit ueber bestehen, sondern wird nur zum mails holen aufgebaut.
	 */
	//private List<POP3Account> accountList;
	
	/**
	 * Konstruktor
	 * @param c Der Socket auf dem der MainServer einen Client akzeptiert hat.
	 */
	public ProxyServer(Socket c) {
		this.connection = c;
		this.cObj = new Command();
		this.confirmed = false;
		this.ownAccount = null;
		this.mailList = null;
		//this.accountList = new ArrayList<POP3Account>();		
	}

	/**
	 * Die Kommunikationsreihenfolge ist folgende: Solange der Socket auf dem
	 * der ProxyServer mit dem Client kommuniziert nicht geschlossen ist 
	 * 1. Wartet er auf dem Socket auf eine ankommende Zeile von maximal 256 Zeichen. 
	 * 2. Übergibt diese der Methode handleClientRequest die die
	 * Eingabezeichenkette analysiert.
	 * 3. Löscht sich aus der Liste der vom MainServer verwalteten ProxyServer.
	 */
	public void run() {
		Scanner readFromClient = null;
		PrintWriter writeToClient = null;
		//DataInputStream dis = null;
		//DataOutputStream dos = null;
		while (!connection.isClosed()) {
			try {
				
				//BufferedReader stdin = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				readFromClient = new Scanner(
						connection.getInputStream(),
						StandardCharsets.UTF_8.name());
				writeToClient = new PrintWriter(
						new OutputStreamWriter(connection.getOutputStream(),
								StandardCharsets.UTF_8), true);
			
				//dis = new DataInputStream(connection.getInputStream());
				//dos = new DataOutputStream(connection.getOutputStream());
				
				/*
				int test;
				while(stdin.ready()){
					test = stdin.read();
					System.out.println("int: " + test);
					System.out.println("char: " + (char)test);
					System.out.println("string: " + Integer.toString(test));
				}
				*/

				
				String clientRequest = readFromClient.findInLine(".{0,254}");

				//String clientRequest = readFromClient.next();
			//	String clientRequest = readFromClient.nextLine();
				//String clientRequest = dis.readUTF();
				System.out.println("request: " + clientRequest);
				handleClientRequest(clientRequest, writeToClient);				
			} catch (Exception e) {
				try {
					connection.close();
				} catch (IOException e1) {
					System.out.println("couldn't close connection");
				}
			}

		}
		POP3Proxy.deleteMe(this);
		try {
			if(readFromClient != null){
				readFromClient.close();
			}
			if(writeToClient != null){
				writeToClient.close();
			}
			connection.close();
		} catch (IOException e) {
			System.out.println("couldn't close connection");
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param command Eingabezeichenfolge des Clients.
	 * @param writeToClient Der OutputStream des Sockets als PrintWriter um das Ergebnis
	 *            			zum Cliet zu schreiben.
	 */
	private void handleClientRequest(String command, PrintWriter writeToClient) {
	
		String[] splitedCommand = command.split(" ", 2);
		String result = "";
		
		if(cObj.isValid(command)){
			//solange noch keine Authentifizierung erfolgte,
			//weiterhin nur USER, PASS oder QUIT moeglich
			if(!this.confirmed){
				switch(splitedCommand[0].toUpperCase()){
				case "USER":
					result = doUser(splitedCommand[1]);
					break;
				case "PASS":
					doPass(splitedCommand[1], writeToClient);
					break;
				case "QUIT":
					writeToClient.println("+OK dewey POP3 server signing off (maildrop empty)");
					closeConnection();
					break;
				case "CAPA":
					writeToClient.println("-ERR");
					break;
				case "AUTH":
					writeToClient.println("-ERR");
					break;
				default:
					writeToClient.println("-ERR authentication failed");
					closeConnection();
					break;
				}		
			}
			else{
				//List<String> mailList = this.ownAccount.getMails();	
				//mailList = this.ownAccount.getMails();	
				if(this.mailList != null){
					switch(splitedCommand[0].toUpperCase()){
					case "STAT":
						result = doStat();
						break;
					case "LIST":
						doList(splitedCommand, writeToClient);
						break;
					case "RETR":
						result = doRetr(splitedCommand[1], writeToClient);
						break;
					case "DELE":
						result = doDele(splitedCommand[1]);
						break;
					case "NOOP":
						result = "+OK";
						break;
					case "RSET":
						result = doRset();
						break;
					case "UIDL":
						doUidl(splitedCommand, writeToClient);
						break;
					case "QUIT":
						String quitResult = doQuit();
						writeToClient.println(quitResult);
						try {
							this.connection.close();
						} catch (IOException e) {
							System.out.println("couldn't close connection");
							e.printStackTrace();
						}
						break;
					default:
						break;
					}	
				}
				else{
					result = "-ERR coulnd't read mails";
				}
			}
		}
		else{
			result = "-ERR not a valid command";
		}
		if(!result.isEmpty()){
			writeToClient.println(result);
		}
	}
	
	/**
	 * Setzt den Usernamen
	 * @param splitedCommand username aus dem uebergebenen Befehl
	 * @return Antwort an Email-Client
	 */
	private String doUser(String splitedCommand){
		USER = splitedCommand;
		return "+OK password required for user \"" + USER + "\"";
	}
	
	/**
	 * Setzt das Passwort und schreibt auf den OutPutStream des Sockets, ob die
	 * Authentifizierung erfolgreich war. War sie erfolgreich, so wird der Zustand
	 * der Mailbox an den Client geschickt
	 * @param splitedCommand passwort aus dem uebergebenen Befehl
	 * @param writeToClient Printer, um auf den OutPutStream des Sockets zu schreiben
	 */
	private void doPass(String splitedCommand, PrintWriter writeToClient){
		PASS = splitedCommand;
		if(!checkAccount(USER, PASS)){
			writeToClient.println("-ERR authentication failed");
			closeConnection();
		}
		else{
			this.mailList = this.ownAccount.getMails();
			if(this.mailList != null){
				String[] statResult = doStat().split(" ");
				writeToClient.println("+OK mailbox \"" + USER + "\" has " + statResult[1]
						+ " messages (" + statResult[2] + " octets)");
			}
			else{
				writeToClient.println("-ERR coulnd't read mails");
				closeConnection();
			}
		}
	}
	
	/**
	 * 
	 * @param user Username
	 * @param pass Passwort
	 * @return konnte der POP3 Account in der Liste der vorkonfigurierten POP3 Accounts gefunden
	 * 		und angelegt werden true, sonst false
	 */
	private boolean checkAccount(String user, String pass){
		for(POP3Account item : POP3Proxy.getKnownAccounts()){
			if(item.getUser().equals(user) && item.getPass().equals(pass)){
				this.ownAccount = new POP3Account(item);
				/*this.ownAccount = new POP3Account(user, pass,
						item.getServeraddress(), item.getPort());*/
				this.confirmed = true;
				return true;
			}
		}
		/*
		this.ownAccount = new POP3Account(user, pass,
				this.connection.getInetAddress().getHostName(), 
				this.connection.getLocalPort());
		if(POP3Proxy.getKnownAccounts().contains(this.ownAccount)){
			this.confirmed = true;
			return true;
			}*/
		return false;
	}
	
	/**
	 * Ermittelt den Status der Mailbox auf dem Server: "+OK nn mm"
	 * nn := Anzahl der vorhandenen Mails auf dem Server 
	 * mm := Groesse aller Mails auf dem Server
	 * @return Antwort an Email-Client
	 */
	//private String doStat(List<String> mailList){
	private String doStat(){
		return "+OK " + this.mailList.size() + " " + getByteSize(this.mailList);
	}
	
	/**
	 * Wurde LIST ohne Parameter abgegeben, so wird der Status der Mailbox ermittelt,
	 * indem Anzahl der gesamten Emails und die Gesamtgroesse ermittelt und an Email-Client
	 * geschickt wird.
	 * Wurde LIST mit Parameter abgegeben, so wird fuer die n-te Mail die Groesse ermittelt
	 * und zurueckgegeben.
	 * Gibt es unter n keine Mail, wird eine entsprechende Error-Meldung zurueckgegeben
	 * @param splitedCommand angekommene Befehlt vom Email-Client
	 * @param writeToClient Printer, um auf den OutPutStream des Sockets zu schreiben
	 */
	private void doList(String[] splitedCommand, PrintWriter writeToClient){
		if(splitedCommand.length == 1){
			writeToClient.println("+OK " + this.mailList.size() + " messages ("
					+ getByteSize(this.mailList) + " octets)");
			for(int i = 0; i < this.mailList.size(); i++){
				writeToClient.println("+OK " + (i + 1) + " " 
					+ this.mailList.get(i).getSize());
			}
		}
		else{
			int n = Integer.parseInt(splitedCommand[1]);
			if(n <= this.mailList.size()){
				writeToClient.println("+OK " + splitedCommand[1] + " " 
					+ this.mailList.get(n - 1).getSize());
			}
			else{
				writeToClient.println("-ERR no such message, only " + this.mailList.size()
						+ " messages in maildrop");
			}
		}
	}
	
	/**
	 * Holt die gesamte Email aus der List und gibt sie zurueck. Wurde unter entsprechenedem
	 * n keine Mail gefunden, dann Error-Nachricht
	 * @param splitedCommand Parameter n aus dem uebergebenen Befehl
	 * @param writeToClient Printer, um auf den OutPutStream des Sockets zu schreiben
	 * @return Antwort an Email-Client
	 */
	private String doRetr(String splitedCommand, PrintWriter writeToClient){
		int n = Integer.parseInt(splitedCommand);
		if(n <= this.mailList.size()){
			writeToClient.println("+OK " + this.mailList.get(n - 1).getSize() + " octets");
			return this.mailList.get(n - 1).getText();
		}
		return "-ERR no such message";
	}
	
	/**
	 * Markiert die n-te unmarkierte Email und gibt die Anzahl der markierten mit entsprechender
	 * Nachricht an Email-Client zurueck. Wurde unter n keine Mail gefunden, Rueckgabe
	 * einer entsprechenden Error-Nachricht
	 * @param splitedCommand Parameter n aus dem uebergebenen Befehl
	 * @return Antwort an Email-Client
	 */
	private String doDele(String splitedCommand){
		int n = Integer.parseInt(splitedCommand);
		if(n <= this.mailList.size()){
			if(this.mailList.get(n - 1).isChecked()){
				return "-ERR message " + n + " already deleted";
			}
			else{
				this.mailList.get(n - 1).setChecked(true);
				return "+OK message " + n + " deleted";
			}
		}
		return "-ERR no such message";
	}
	
	/**
	 * Setzt alle markierten Emails auf unmarkiert und gibt eine entsprechende Ausgabe,
	 * sowie die Anzahl an neu unmarkierten Emails an Email-Client zurueck
	 * @return Antwort an Email-Client
	 */
	private String doRset(){
		int counter = 0;
		int byteSize = 0;
		for(Email item : this.mailList){
			if(item.isChecked()){
				item.setChecked(false);
				counter++;
				byteSize += item.getSize();
			}
		}
		return "+OK maildrop has " + counter + " messages (" + byteSize + " octets)";
	}
	
	/**Wurde UIDL ohne Parameter aufgerufen, so werden die IDs aller Emails an den
	 * Email-Client geschickt
	 * Wurde UIDL mit Parameter aufgerufen, so wird die ID der entsprechend gewaehlten
	 * Email zurueckgegeben
	 * Wurde keine Email gefunden, wird eine entsprechende Error Nachricht zurueckgegeben
	 * @param splitedCommand angekommene Befehlt vom Email-Client
	 * @param writeToClient Printer, um auf den OutPutStream des Sockets zu schreiben
	 */
	private void doUidl(String[] splitedCommand, PrintWriter writeToClient){
		if(splitedCommand.length == 1){
			if(this.mailList.size() != 0){
				writeToClient.println("+OK");
				for(int i = 0; i < this.mailList.size(); i++){
					writeToClient.println((i + 1) + " " + this.mailList.get(i).getUidl());
				}
			}
			else{
				writeToClient.println("-ERR no such message");
			}
		}
		else{
			int n = Integer.parseInt(splitedCommand[1]);
			if(n <= this.mailList.size()){
				writeToClient.println("+OK " + splitedCommand[1] + " " 
					+ this.mailList.get(n - 1).getUidl());
			}
			else{
				writeToClient.println("-ERR no such message, only " + this.mailList.size()
						+ " messages in maildrop");
			}
		}
	}
	
	/**
	 * Loescht alle markierten Emails und schliesst die Verbindung mit dem Socket
	 * Wurden nicht alle Emails gesloescht, da sie nicht markiert waren, wird dies 
	 * dem Email-Client mitgeteilt
	 * @return Antwort an Email-Client
	 */
	private String doQuit() {
		int counter = this.mailList.size();
		for(int i = 0; i < this.mailList.size(); i++){
			Email mail = this.mailList.get(i);
			if(mail.isChecked()){
				ownAccount.removeMail(i);
				counter--;
			}
		}
		if(counter == 0){
			return "+OK dewey POP3 server signing off (maildrop empty)";
		}
		return "+OK dewey POP3 server signing off (" + counter + " messages left)";
	}
	
	/**
	 * Ermittelt die Groesse der gesamten Email-Liste
	 * @return Byte-Groesse der gesamten Email-Liste
	 */
	private static int getByteSize(List<Email> mailList){
		int byteSize = 0;
		for(Email item : mailList){
			byteSize += item.getSize();
		}
		return byteSize;
	}
	
	/**
	 * Schliesst die Verbindung zum Socket
	 */
	private void closeConnection(){
		try {
			this.connection.close();
		} catch (IOException e) {
			System.out.println("couldn't close connection");
			e.printStackTrace();
		}
	}
	
	/* OLD
	 
	private void doList(String[] splitedCommand, List<String> mailList, PrintWriter writeToClient){
		if(splitedCommand.length == 1){
			writeToClient.println("+OK " + mailList.size() + " messages ("
					+ getByteSizeOfMails(mailList) + " octets)");
			for(int i = 0;i < mailList.size(); i++){
				writeToClient.println("+OK " + i + " " 
					+ getByteSizeOfMail(mailList.get(i)));
			}
		}
		else{
			int n = Integer.parseInt(splitedCommand[1]);
			if(n == mailList.size()){
				writeToClient.println("+OK " + splitedCommand[1] + " " 
					+ getByteSizeOfMail(mailList.get(n)));
			}
			else{
				writeToClient.println("-ERR no such message, only " + mailList.size()
						+ " messages in maildrop");
			}
		}
	}
	
	private static int getByteSizeOfMails(List<String> list){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out;
		int byteSize = 0;
		try {
			out = new ObjectOutputStream(baos);
			out.writeObject(list);
			out.close();
			byteSize = baos.toByteArray().length;
			System.out.println(list.getClass().getSimpleName() +
			      " used " + byteSize + " bytes");
			} catch (IOException e) {
				e.printStackTrace();
				}
		return byteSize;
	}
	
	private static int getByteSizeOfMail(String mail){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out;
		int byteSize = 0;
		try {
			out = new ObjectOutputStream(baos);
			out.writeObject(mail);
			out.close();
			byteSize = baos.toByteArray().length;
			System.out.println(mail.getClass().getSimpleName() +
			      " used " + byteSize + " bytes");
			} catch (IOException e) {
				e.printStackTrace();
				}
		return byteSize;
	}
	*/


}
