package application;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

//Interface do intermediador de mensagens
public interface IntermediatorInterface extends Remote{
	
	public void registerNewClient(String name) throws RemoteException;
	
	public void registerNewServer(String name) throws RemoteException;
	
	public List<Contact> updateServerContactList() throws RemoteException;
	
	public List<Contact> getContactList(int serverNumber) throws RemoteException;
	
	public Boolean addNewContact(Contact newContact) throws RemoteException;
	
	public Boolean updateContact(Contact oldContact, Contact newContact) throws RemoteException;
	
	public void deleteContact(Contact contact) throws RemoteException;
	
	public Boolean isServerRunning(int serverNumber) throws RemoteException;

}
