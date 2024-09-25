package application;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

public class IntermediatorConnection {
	
	public IntermediatorInterface intermediatorConnection;
	private Main main;
	private String intermediatorUrl;
	
	public IntermediatorConnection(Main main) {
		this.main = main;
	}
	
	// Definir a url do intermediador
	public void setUrl(String ip, String port, String name) {
		String url = String.format("rmi://%s:%s/%s", ip, port, name);
		this.intermediatorUrl = url;
	}
	
	// Registrar o cliente logado no intermediador
	public void registerUser(String Name) {
		try {
			this.intermediatorConnection = (IntermediatorInterface) Naming.lookup(intermediatorUrl);
			this.intermediatorConnection.registerNewClient(Name);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
	
	// Checar se o servidor que o cliente está utilizando está online
	public Boolean checkConnection(int serverNumber) {	
		try {
			this.intermediatorConnection = (IntermediatorInterface) Naming.lookup(intermediatorUrl);
			return this.intermediatorConnection.isServerRunning(serverNumber);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			return false;
		}	
	}
	
	// Buscar a lista de contatos do servidor conectado
	public List<Contact> getContactList(int serverNumber){
		try {
			this.intermediatorConnection = (IntermediatorInterface) Naming.lookup(intermediatorUrl);
			return this.intermediatorConnection.getContactList(serverNumber);
		} catch (RemoteException | MalformedURLException | NotBoundException e) {
			return null;
		}
	}
	
	// Adicionar um novo contato ao cliente
	public Boolean addNewContact(Contact contact) {
		try {
			this.intermediatorConnection = (IntermediatorInterface) Naming.lookup(intermediatorUrl);
			return this.intermediatorConnection.addNewContact(contact);
		} catch (RemoteException | MalformedURLException | NotBoundException e) {
			return true;
		}
	}
	
	// Atualizar um contato do cliente
	public Boolean updateContact(Contact oldContact, Contact newContact) {
		try {
			this.intermediatorConnection = (IntermediatorInterface) Naming.lookup(intermediatorUrl);
			return this.intermediatorConnection.updateContact(oldContact,newContact);
		} catch (RemoteException | MalformedURLException | NotBoundException e) {
			return false;
		}
	}
	
	// Deletar um contato do cliente
	public void deleteContact(Contact contact) {
		try {
			this.intermediatorConnection = (IntermediatorInterface) Naming.lookup(intermediatorUrl);
			this.intermediatorConnection.deleteContact(contact);
		} catch (RemoteException | MalformedURLException | NotBoundException e) {
			e.printStackTrace();
		}
	}

}
