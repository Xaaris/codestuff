package account;

import java.util.ArrayList;
import java.util.List;

//gaendert, da klasse Email erzeugt wurde und die Verwaltung damit deutlich
//einfacher faellt. alter code siehe unten


/**
 * Repraesentiert den Abholaccount fuer jedes konfigurierte Konto
 * Fuehrt eine Liste aller Mails zum zugehoerigen Account
 * @author Fabian Reiber und Francis Opoku
 *
 */
public class Collectionaccount {
	
	private List<Email> mailList;
	
	public Collectionaccount(){
		this.mailList = new ArrayList<Email>();
	}

	public void addMails(List<Email> mailList){
		this.mailList.addAll(mailList);
	}
	
	public void clearMails(){
		this.mailList.clear();
	}
	
	public void removeMail(int index){
		this.mailList.remove(index);
	}
	
	public List<Email> getMailList() {
		return this.mailList;
	}

	public void setMailList(List<Email> mailList) {
		this.mailList = mailList;
	}
	
	
	
	/*OLD
	//liste von mails
	private List<String> mailList;
	
	public Collectionaccount(){
		this.mailList = new ArrayList<String>();
	}

	public void addMails(List<String> mailList){
		this.mailList.addAll(mailList);
	}
	
	public void clearMails(){
		this.mailList.clear();
	}
	
	public void removeMail(int index){
		this.mailList.remove(index);
	}
	
	public List<String> getMailList() {
		return mailList;
	}

	public void setMailList(List<String> mailList) {
		this.mailList = mailList;
	}
	*/
	
	
}
