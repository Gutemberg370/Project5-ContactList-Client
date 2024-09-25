package application;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javafx.application.Platform;

//Implementação da interface de conexão RMI do cliente
public class User extends UnicastRemoteObject implements UserInterface{
	
	private Main main;

	protected User(Main main) throws RemoteException {
		super();
		this.main = main;
	}

	// Adicionar um novo contato para o cliente
	public Boolean addNewContact(Contact contact) throws RemoteException {
		
        Boolean hasContact = false;
		
        // Caso o contato já exista, ele não é adicionado
		for(int i = 0; i < this.main.contactList.size(); i++) {
			if(this.main.contactList.get(i).getName().equals(contact.getName())) {
				hasContact = true;
				return hasContact;
			}
		}
		
		// Caso o contato não exista, ele é adicionado
		this.main.contactList.add(contact);
		this.main.supportFunctions.updateGridpane();
		return hasContact;
		
	}

	// Atualizar um contato na lista de contatos do cliente
	public Boolean updateContact(Contact oldContact, Contact newContact) throws RemoteException {
		
		Boolean hasContact = false;
		
		// Primeiro checar se o nome atualizado do contato já existe
		if(!oldContact.getName().equals(newContact.getName())) {
			for(int i = 0; i < this.main.contactList.size(); i++) {
				if(this.main.contactList.get(i).getName().equals(newContact.getName())) {
					hasContact = true;
					return hasContact;
				}
			}
		}
		
		// Se chegou aqui, o novo nome do contato não existe e o contato é atualizado
		for(int i = 0; i < this.main.contactList.size(); i++) {
			if(this.main.contactList.get(i).getName().equals(oldContact.getName())) {
				
            	if(this.main.selectedContact != null && this.main.contactList.get(i).getName().equals(this.main.selectedContact.getName())) {
            		
    		        Runnable updateSelectedContactArea = () -> {
    		            Platform.runLater(() -> {
    	            		this.main.selectedContactName.setText(newContact.getName());
    	            		this.main.contNameInput.setText(newContact.getName());
    	            		this.main.contNumberInput.setText(newContact.getNumber());
    		            });
    		        };
    		        Thread updateSelectedContactAreaThread = new Thread(updateSelectedContactArea);
    		        updateSelectedContactAreaThread.setDaemon(true);
    		        updateSelectedContactAreaThread.start();  		        
            	}
            	
				this.main.contactList.get(i).setName(newContact.getName());
				this.main.contactList.get(i).setNumber(newContact.getNumber());
				this.main.supportFunctions.updateGridpane();
				return hasContact;
			}
		}	
		
		
		return hasContact;
		
	}

	// Deletar um contato da lista de contatos do cliente
	public void deleteContact(Contact contact) throws RemoteException {
		for(int i = 0; i < this.main.contactList.size(); i++) {
			if(this.main.contactList.get(i).getName().equals(contact.getName())) {
				
				
            	if(this.main.selectedContact != null && this.main.contactList.get(i).getName().equals(this.main.selectedContact.getName())) {
            		this.main.selectedContact = null;
            		
    		        Runnable updateSelectedContactArea = () -> {
    		            Platform.runLater(() -> {
    	            		this.main.selectedContactName.setText(null);
    	            		this.main.contNameInput.setText(null);
    	            		this.main.contNumberInput.setText(null);
    	                	this.main.contNameInput.setDisable(true);
    	                	this.main.contNumberInput.setDisable(true);
    	                	this.main.updateContact.setDisable(true);
    		            });
    		        };
    		        Thread updateSelectedContactAreaThread = new Thread(updateSelectedContactArea);
    		        updateSelectedContactAreaThread.setDaemon(true);
    		        updateSelectedContactAreaThread.start();  		        
            	}
            	
            	
				this.main.contactList.remove(i);
				this.main.supportFunctions.updateGridpane();
				break;
			}
		}	
		
	}

}
