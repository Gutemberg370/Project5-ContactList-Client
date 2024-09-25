package application;

import java.rmi.Remote;
import java.rmi.RemoteException;

//Interface do cliente
public interface UserInterface extends Remote{
	
	public Boolean addNewContact(Contact contact) throws RemoteException;
	
	public Boolean updateContact(Contact oldContact, Contact newContact) throws RemoteException;
	
	public void deleteContact(Contact contact) throws RemoteException;

}
