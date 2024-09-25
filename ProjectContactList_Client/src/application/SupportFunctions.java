package application;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

// Funções relacionadas com a atualização da interface gráfica
public class SupportFunctions {
	
	private Main main;
	
	SupportFunctions(Main main){
		this.main = main;
	}
	
	// Função que define a cor para o contato selecionado pelo usuário
	public void setContactColor(Color color) {
		
		for(int i = 0; i < this.main.contactList.size(); i++) {
    		if(this.main.contactList.get(i).getName().equals(this.main.selectedContact.getName())) {
    			for (Node child : this.main.gridpane.getChildren()) {
    			    Integer r = GridPane.getRowIndex(child);
    			    Integer c = GridPane.getColumnIndex(child);
    			    int row = r == null ? 0 : r;
    			    int column = c == null ? 0 : c;
    			    if (row == i && column == 0 && (child instanceof Label)) {
    			        ((Labeled) child).setTextFill(color);              			        
    			        break;
    			    }
    			}
    			
    		} 
		}
		
	}
	
	// Função que reconstrói a lista de contatos para fins de atualização
	public void updateGridpane() {
		
		Runnable updateGrid = () -> {
            Platform.runLater(() -> {	
            	
            	this.main.gridpane.getChildren().clear(); 
            	
            	for(int i = 0; i < this.main.contactList.size(); i++) {
    		   		
            		int contactId = i;
            		
            		// Criar botão de selecionar contato
                	Button selectContact = new Button();
                	       	
                    Image imgSelect = new Image(getClass().getResourceAsStream("arrow.png"));
                    ImageView imgViewSelect = new ImageView(imgSelect);
                    imgViewSelect.setFitHeight(20);
                    imgViewSelect.setFitWidth(35);
                    imgViewSelect.setPreserveRatio(true);
                    selectContact.setGraphic(imgViewSelect);
                    
                    selectContact.setPadding(new Insets(-1, -1, -1, -1));
                    
                    selectContact.setOnAction(event -> {
                    	
                    	if(this.main.selectedContact != null) {
                        	setContactColor(Color.BLACK);
                    	}
                    	this.main.selectedContact = this.main.contactList.get(contactId);
                    	this.main.selectedContactName.setText(this.main.selectedContact.getName());
                    	this.main.contNameInput.setText(this.main.selectedContact.getName());
                    	this.main.contNumberInput.setText(this.main.selectedContact.getNumber());
                    	this.main.contNameInput.setDisable(false);
                    	this.main.contNumberInput.setDisable(false);
                    	this.main.updateContact.setDisable(false);
                    	setContactColor(Color.GREEN);
                    	
                    });
                    
                    // Criar botão de deletar contato
                	Button deleteContact = new Button();
        	       	
                    Image imgDelete = new Image(getClass().getResourceAsStream("delete.jpg"));
                    ImageView imgViewDelete = new ImageView(imgDelete);
                    imgViewDelete.setFitHeight(20);
                    imgViewDelete.setFitWidth(35);
                    imgViewDelete.setPreserveRatio(true);
                    deleteContact.setGraphic(imgViewDelete);
                    
                    deleteContact.setPadding(new Insets(-1, -1, -1, -1));
                    
                    deleteContact.setOnAction(event -> {
                    	// A ação de deletar um contato ocorre somente se o servidor estiver online
                    	if(this.main.intermediatorConnection.checkConnection(this.main.serverUsed)) {
                    		
	        		        Runnable deleteUserContact = () -> {
	        		            Platform.runLater(() -> {
	        		            	this.main.gridpane.getChildren().removeIf(node -> GridPane.getRowIndex(node) == contactId);
	        		            	updateGridpane();
	        		            });
	        		        };
	        		        Thread deleteUserContactThread = new Thread(deleteUserContact);
	        		        deleteUserContactThread.setDaemon(true);
	        		        deleteUserContactThread.start();
	                    	
	                    	// O intermediador realizará a propagação da ação de deletar o contato
	        		        this.main.intermediatorConnection.deleteContact(this.main.contactList.get(contactId));
        		        
                        }
                    	
                    	// Caso o servidor esteja offline, a tela de erro será mostrada
                    	else {
                			Stage window = (Stage)deleteContact.getScene().getWindow();
                        	Scene scene = new Scene(this.main.createServerErrorPage());
                        	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
                        	window.setScene(scene);
                        	window.setResizable(false);	
                    	}
                    });
                    
                    // Criar label nome do contato
                    Label contactLabel = new Label(this.main.contactList.get(i).getName());
                    contactLabel.setFont(new Font("Arial",14));
                    
                    // Adicionar tudo no gridpane
                    this.main.gridpane.add(contactLabel, 0, i);  
                    this.main.gridpane.add(selectContact, 1, i); 
                    this.main.gridpane.add(deleteContact, 2, i);
            	}
            });
    };
    Thread updateGridThread = new Thread(updateGrid);
    updateGridThread.setDaemon(true);
    updateGridThread.start();

	}

}
