package application;

import java.io.Serializable;

// Classe que representa um contato da aplicação
public class Contact implements Serializable{
	
	private String name;
	private String number;
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getNumber() {
		return number;
	}
	
	public void setNumber(String number) {
		this.number = number;
	}
	
	public Contact(String name, String number) {
		this.name = name;
		this.number = number;
	}

}
