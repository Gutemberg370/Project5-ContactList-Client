package application;
	
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;


public class Main extends Application{
	
	public GridPane gridpane = new GridPane();
	
	public Label errorMessage;
	
	public Label selectedContactName = new Label();
	
	public TextField contNameInput = new TextField();
	
	public TextField contNumberInput = new TextField();
	
	public Button updateContact;
	
	public List<Contact> contactList = new ArrayList<>();
	
	public Contact selectedContact;
	
	public int serverUsed;
	
	private Registry rmiRegistry;
	
	public IntermediatorConnection intermediatorConnection = new IntermediatorConnection(this);
	
	public SupportFunctions supportFunctions = new SupportFunctions(this);
	
	// Serviço responsável por executar uma thread mais de uma vez
	final ExecutorService service = Executors.newCachedThreadPool();
	
	// Thread responsável por controlar a adição e remoção de uma mensagem de erro
	final class showErrorMessage implements Runnable {
		
		public String errMessage;
		
		public showErrorMessage(String errorMessage) {
			this.errMessage = errorMessage;
		}
		
	    @Override
	    public void run() {
	    	
	    	// Fazer a mensagem de erro aparecer
	        Runnable beginMessageError = () -> {
	            Platform.runLater(() -> {
	    	    	errorMessage.setText(this.errMessage);
	    	    	updateContact.setDisable(true);
	            });
	        };
	        Thread beginMessageErrorThread = new Thread(beginMessageError);
	        beginMessageErrorThread.setDaemon(true);
	        beginMessageErrorThread.start();
	        
	        // Esperar 4 segundos
	    	try {
	    		Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    	
	    	// Retirar mensagem de erro
	        Runnable endMessageError = () -> {
	            Platform.runLater(() -> {
	            	errorMessage.setText("");
	            	if(selectedContact != null) {
	            		updateContact.setDisable(false);
	            	}
	            });
	        };
	        Thread endMessageErrorThread = new Thread(endMessageError);
	        endMessageErrorThread.setDaemon(true);
	        endMessageErrorThread.start();
	        
	    }
	}; 
	
	public void callshowErrorMessage(String errorMessage) {
		service.submit(new showErrorMessage(errorMessage));
	}
	
		
	// Criar página de login no intermediador
    private Parent createIntermediatorLogin() {
    	
    	Pane root = new Pane();
    	
    	BackgroundFill backgroundFill = new BackgroundFill(Color.valueOf("#E27D60"), new CornerRadii(10), new Insets(10));

    	Background background = new Background(backgroundFill);
    	
    	root.setBackground(background);
    	
    	root.setPrefSize(544, 492);
    	
    	Label intermediatorLabel = new Label("LOGIN NO INTERMEDIADOR");
    	intermediatorLabel.setFont(new Font("Monaco",36));
    	intermediatorLabel.setLayoutX(50);
    	intermediatorLabel.setLayoutY(40);
    	
    	Label title = new Label("Insira o seu nome, o ip do intermediador a se \n conectar, o nome do intermediador e a porta. \n Então clique no botão abaixo para fazer o login.");
    	title.setFont(new Font("Arial",18));
    	title.setLayoutX(80);
    	title.setLayoutY(120);
    	title.setTextAlignment(TextAlignment.CENTER);
    	
    	Label userName = new Label("Nome :");
    	userName.setFont(new Font("Arial",13));
    	userName.setLayoutX(165);
    	userName.setLayoutY(225);
    	
    	TextField userNameInput = new TextField();
    	userNameInput.setLayoutX(215);
    	userNameInput.setLayoutY(220);
    	userNameInput.setMinWidth(220);
    	
    	Label intermediatorIp = new Label("Ip do Intermediador :");
    	intermediatorIp.setFont(new Font("Arial",13));
    	intermediatorIp.setLayoutX(88);
    	intermediatorIp.setLayoutY(265);
    	
    	TextField intermediatorIpInput = new TextField("192.168.0.14");
    	intermediatorIpInput.setLayoutX(215);
    	intermediatorIpInput.setLayoutY(260);
    	intermediatorIpInput.setMinWidth(220);
    	
    	Label intermediatorName = new Label("Nome do Intermediador :");
    	intermediatorName.setFont(new Font("Arial",13));
    	intermediatorName.setLayoutX(65);
    	intermediatorName.setLayoutY(305);
    	
    	TextField intermediatorNameInput = new TextField("Intermediator");
    	intermediatorNameInput.setLayoutX(215);
    	intermediatorNameInput.setLayoutY(300);
    	intermediatorNameInput.setMinWidth(220);
    	
    	Label intermediatorPort = new Label("Porta do Intermediador :");
    	intermediatorPort.setFont(new Font("Arial",13));
    	intermediatorPort.setLayoutX(68);
    	intermediatorPort.setLayoutY(345);
    	
    	TextField intermediatorPortInput = new TextField("6000");
    	intermediatorPortInput.setLayoutX(215);
    	intermediatorPortInput.setLayoutY(340);
    	intermediatorPortInput.setMinWidth(220);
    	
    	Button loginButton = new Button("Fazer Login no Intermediador");
    	loginButton.setLayoutX(185);
    	loginButton.setLayoutY(410);
    	loginButton.setMinWidth(150);
    	loginButton.setOnAction(event -> {
    		this.intermediatorConnection.setUrl(intermediatorIpInput.getText(), intermediatorPortInput.getText(), intermediatorNameInput.getText());
    		
    		// Criar o registro RMI do cliente no servidor de nomes
    		try {
				this.rmiRegistry = LocateRegistry.getRegistry(Integer.valueOf(intermediatorPortInput.getText()));
				this.rmiRegistry.bind(userNameInput.getText(), new User(this));
			} catch (RemoteException | AlreadyBoundException e) {
				e.printStackTrace();
			}
    		
    		// Registrar o usuário no intermediador de mensagens
    		this.intermediatorConnection.registerUser(userNameInput.getText());
    		
        	Stage window = (Stage)loginButton.getScene().getWindow();
        	Scene scene = new Scene(createServerSelectionPage());
        	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        	window.setScene(scene);
        	window.setResizable(false);
			
        });
    	
    	root.getChildren().addAll(intermediatorLabel, title, userName, userNameInput, intermediatorIp, intermediatorIpInput, intermediatorName, intermediatorNameInput,
    			intermediatorPort, intermediatorPortInput, loginButton);
    	
    	return root;
    }
    
    // Criar página de seleção de servidor
    private Parent createServerSelectionPage() {
    	
    	Pane root = new Pane();
    	
    	BackgroundFill backgroundFill = new BackgroundFill(Color.valueOf("#E27D60"), new CornerRadii(10), new Insets(10));

    	Background background = new Background(backgroundFill);
    	
    	root.setBackground(background);
    	
    	root.setPrefSize(524, 402);
    	
    	Label title = new Label("ESCOLHA UMA DAS AGENDAS");
    	title.setFont(new Font("Monaco",30));
    	title.setLayoutX(65);
    	title.setLayoutY(50);
    	   	
    	Button firstContactList = new Button("Primeira Agenda de Contatos");
    	firstContactList.setLayoutX(160);
    	firstContactList.setLayoutY(160);
    	firstContactList.setMinWidth(200);
    	firstContactList.setOnAction(event -> {	
    		
    		// Definindo o servidor do cliente como o primeiro e solicitando a lista de contatos dele
    		this.serverUsed = 0;
    		this.contactList = this.intermediatorConnection.getContactList(0);
    		
    		// Caso a lista não tenha retornado null, prosseguir
    		if(this.contactList != null) {
    			Stage window = (Stage)firstContactList.getScene().getWindow();
            	Scene scene = new Scene(createClientPage());
            	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            	window.setScene(scene);
            	window.setResizable(false);	
            	
    		}
    		
    		// Caso a lista tenha retornado null, o servidor está indisponível
    		else{
    			Stage window = (Stage)firstContactList.getScene().getWindow();
            	Scene scene = new Scene(createServerErrorPage());
            	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            	window.setScene(scene);
            	window.setResizable(false);	
    		}
			
        });
    	
    	Button secondContactList = new Button("Segunda Agenda de Contatos");
    	secondContactList.setLayoutX(160);
    	secondContactList.setLayoutY(220);
    	secondContactList.setMinWidth(200);
    	secondContactList.setOnAction(event -> {	
    		  	
    		// Definindo o servidor do cliente como o segundo e solicitando a lista de contatos dele
    		this.serverUsed = 1;
    		this.contactList = this.intermediatorConnection.getContactList(1);
    		
    		// Caso a lista não tenha retornado null, prosseguir
    		if(this.contactList != null) {
    			Stage window = (Stage)secondContactList.getScene().getWindow();
            	Scene scene = new Scene(createClientPage());
            	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            	window.setScene(scene);
            	window.setResizable(false);	
            	
    		}
    		
    		// Caso a lista tenha retornado null, o servidor está indisponível
    		else{
    			Stage window = (Stage)secondContactList.getScene().getWindow();
            	Scene scene = new Scene(createServerErrorPage());
            	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            	window.setScene(scene);
            	window.setResizable(false);	
    		}
        	
			
        });
    	
    	Button thirdContactList = new Button("Terceira Agenda de Contatos");
    	thirdContactList.setLayoutX(160);
    	thirdContactList.setLayoutY(280);
    	thirdContactList.setMinWidth(200);
    	thirdContactList.setOnAction(event -> {	
    		  		
    		// Definindo o servidor do cliente como o terceiro e solicitando a lista de contatos dele
    		this.serverUsed = 2;
    		this.contactList = this.intermediatorConnection.getContactList(2);
    		
    		// Caso a lista não tenha retornado null, prosseguir
    		if(this.contactList != null) {
    			Stage window = (Stage)thirdContactList.getScene().getWindow();
            	Scene scene = new Scene(createClientPage());
            	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            	window.setScene(scene);
            	window.setResizable(false);	
            	
    		}
    		
    		// Caso a lista tenha retornado null, o servidor está indisponível
    		else{
    			Stage window = (Stage)thirdContactList.getScene().getWindow();
            	Scene scene = new Scene(createServerErrorPage());
            	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            	window.setScene(scene);
            	window.setResizable(false);	
    		}
        	
			
        });
    	
    	root.getChildren().addAll(title, firstContactList, secondContactList, thirdContactList);
    	
    	return root;
    }
	
    // Página de servidor indisponível
	public Parent createServerErrorPage() {
	
		Pane root = new Pane();
    	
    	BackgroundFill backgroundFill = new BackgroundFill(Color.valueOf("#FF4B33"), new CornerRadii(10), new Insets(10));

    	Background background = new Background(backgroundFill);
    	
    	root.setBackground(background);
    	
    	root.setPrefSize(504, 322);
    	              	
    	Label connectionErrorLabel = new Label("AGENDA INDISPONÍVEL, POR FAVOR \n UTILIZE OUTRA AGENDA.");
    	connectionErrorLabel.setFont(new Font("Monaco",28));
    	connectionErrorLabel.setLayoutX(25);
    	connectionErrorLabel.setLayoutY(55);
    	connectionErrorLabel.setTextAlignment(TextAlignment.CENTER);
    	
    	Button returnContactList = new Button("Selecionar Outra Agenda");
    	returnContactList.setLayoutX(160);
    	returnContactList.setLayoutY(205);
    	returnContactList.setMinWidth(200);
    	returnContactList.setOnAction(event -> {	
    		  		

        	Stage window = (Stage)returnContactList.getScene().getWindow();
        	Scene scene = new Scene(createServerSelectionPage());
        	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        	window.setScene(scene);
        	window.setResizable(false);
        	
			
        });
    	
  	
    	root.getChildren().addAll(connectionErrorLabel, returnContactList);
    	
    	return root;
    }

	// Função que cria a página do cliente
	private Parent createClientPage() {
		
    	Pane root = new Pane();
    	
    	BackgroundFill backgroundFill = new BackgroundFill(Color.valueOf("#ADD8E6"), new CornerRadii(10), new Insets(10));

    	Background background = new Background(backgroundFill);
    	
    	root.setBackground(background);
    	
    	root.setPrefSize(644, 592);
    	
    	this.selectedContact = null;
    	
    	this.errorMessage = new Label();
    	errorMessage.setFont(new Font("Arial",18));
    	errorMessage.setTextFill(Color.RED);
    	errorMessage.setLayoutX(280);
    	errorMessage.setLayoutY(40);
    	errorMessage.setTextAlignment(TextAlignment.CENTER);
    	
    	Rectangle r1 = new Rectangle();
    	r1.setX(210);
    	r1.setY(10);
    	r1.setWidth(10);
    	r1.setHeight(573);
    	r1.setFill(Color.BLUE);
    	
    	Label contactCatalogLabel = new Label("Catálogo de contatos");
    	contactCatalogLabel.setFont(new Font("Arial",18));
    	contactCatalogLabel.setLayoutX(20);
    	contactCatalogLabel.setLayoutY(30);
    	
    	Button addNewContactButton = new Button("Adicionar novo contato");
    	addNewContactButton.setMinWidth(150);
    	addNewContactButton.setLayoutX(35);
    	addNewContactButton.setLayoutY(530);
    	addNewContactButton.setOnAction(event -> {
        	Stage window = (Stage)addNewContactButton.getScene().getWindow();
        	Scene scene = new Scene(addNewContactPage());
        	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        	window.setScene(scene);
        	window.setResizable(false);
        });
    	
    	// Criar a lista de contatos na interface gráfica
    	this.gridpane.getChildren().clear();  	
    	
    	for(int i = 0; i < contactList.size(); i++) {
    		   		   		
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
            	
            	if(this.selectedContact != null) {
                	this.supportFunctions.setContactColor(Color.BLACK);
            	}
            	this.selectedContact = contactList.get(contactId);
            	this.selectedContactName.setText(this.selectedContact.getName());
            	this.contNameInput.setText(this.selectedContact.getName());
            	this.contNumberInput.setText(this.selectedContact.getNumber());
            	this.contNameInput.setDisable(false);
            	this.contNumberInput.setDisable(false);
            	this.updateContact.setDisable(false);
            	this.supportFunctions.setContactColor(Color.GREEN);
            	
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
            	if(this.intermediatorConnection.checkConnection(serverUsed)) {
            		
    		        Runnable deleteUserContact = () -> {
    		            Platform.runLater(() -> {
    		            	this.gridpane.getChildren().removeIf(node -> GridPane.getRowIndex(node) == contactId);
    		            	this.supportFunctions.updateGridpane();
    		            });
    		        };
    		        Thread deleteUserContactThread = new Thread(deleteUserContact);
    		        deleteUserContactThread.setDaemon(true);
    		        deleteUserContactThread.start();
                	
                	// O intermediador realizará a propagação da ação de deletar o contato
    		        this.intermediatorConnection.deleteContact(this.contactList.get(contactId));
            		
            	}
            	
            	// Caso o servidor esteja offline, a tela de erro será mostrada
            	else {
        			Stage window = (Stage)deleteContact.getScene().getWindow();
                	Scene scene = new Scene(createServerErrorPage());
                	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
                	window.setScene(scene);
                	window.setResizable(false);	
            	}

            });
            
            // Criar label nome do contato
            Label contactLabel = new Label(contactList.get(i).getName());
            contactLabel.setFont(new Font("Arial",14));
            
            // Adicionar tudo no gridpane
    		this.gridpane.add(contactLabel, 0, i);  
    		this.gridpane.add(selectContact, 1, i); 
    		this.gridpane.add(deleteContact, 2, i); 
    	}
    	    	
    	this.gridpane.setHgap(10); 
    	this.gridpane.setVgap(10);
    	this.gridpane.setLayoutX(25);
    	this.gridpane.setLayoutY(65);   	
    	
    	
    	Label selectedContactLabel = new Label("Contato Selecionado: ");
    	selectedContactLabel.setFont(new Font("Arial",18));
    	selectedContactLabel.setLayoutX(250);
    	selectedContactLabel.setLayoutY(200);
    	
    	this.selectedContactName = new Label("");
    	this.selectedContactName.setFont(new Font("Arial",18));
    	this.selectedContactName.setLayoutX(430);
    	this.selectedContactName.setLayoutY(200);
    	
    	Label selectedContactNameLabel = new Label("Nome: ");
    	selectedContactNameLabel.setFont(new Font("Arial",18));
    	selectedContactNameLabel.setLayoutX(250);
    	selectedContactNameLabel.setLayoutY(260);
    	
    	this.contNameInput = new TextField();
    	this.contNameInput.setLayoutX(330);
    	this.contNameInput.setLayoutY(259);
    	this.contNameInput.setMinWidth(220);
    	this.contNameInput.setDisable(true);
    	
    	Label selectedContactNumberLabel = new Label("Telefone: ");
    	selectedContactNumberLabel.setFont(new Font("Arial",18));
    	selectedContactNumberLabel.setLayoutX(250);
    	selectedContactNumberLabel.setLayoutY(320);
    	
    	this.contNumberInput = new TextField();
    	this.contNumberInput.setLayoutX(330);
    	this.contNumberInput.setLayoutY(319);
    	this.contNumberInput.setMinWidth(220);
    	this.contNumberInput.setDisable(true);
    	
    	this.updateContact = new Button("Atualizar Contato");
    	this.updateContact.setLayoutX(350);
    	this.updateContact.setLayoutY(400);
    	this.updateContact.setDisable(true);
        
    	this.updateContact.setOnAction(event -> {
        	
    		// A ação de atualizar um contato ocorre somente se o servidor estiver online
    		if(this.intermediatorConnection.checkConnection(serverUsed)) {
        		Contact newContact = new Contact(this.contNameInput.getText(),this.contNumberInput.getText());
            	Boolean hasContact = this.intermediatorConnection.updateContact(selectedContact,newContact);
            	
            	// Se a atualização do contato tiver falhado, a mensagem de erro é utilizada
            	if(hasContact) {
            		this.callshowErrorMessage("Novo nome de contato modificado já \n existe na lista de contatos.");
            	}

    		}
    		
    		// Caso o servidor esteja offline, a tela de erro será mostrada
    		else {
    			Stage window = (Stage)updateContact.getScene().getWindow();
            	Scene scene = new Scene(createServerErrorPage());
            	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            	window.setScene(scene);
            	window.setResizable(false);	
    		}
        	
        });
  	
    	root.getChildren().addAll(errorMessage, r1, contactCatalogLabel, gridpane, addNewContactButton, selectedContactLabel, selectedContactName,
    							  selectedContactNameLabel, contNameInput, selectedContactNumberLabel, contNumberInput, updateContact);
    	
    	return root;
	}
	
	// Página para adicionar um novo contato na lista de contatos
	private Parent addNewContactPage() {
    	Pane root = new Pane();
    	
    	BackgroundFill backgroundFill = new BackgroundFill(Color.valueOf("#E27D60"), new CornerRadii(10), new Insets(10));

    	Background background = new Background(backgroundFill);
    	
    	root.setBackground(background);
    	
    	root.setPrefSize(544, 400);
    	
    	Label contactLabel = new Label("CRIAR NOVO CONTATO");
    	contactLabel.setFont(new Font("Monaco",36));
    	contactLabel.setLayoutX(80);
    	contactLabel.setLayoutY(40);
    	
    	Label title = new Label("Insira o nome do contato e o telefone do contato.\n Então clique no botão abaixo para criar o contato.");
    	title.setFont(new Font("Arial",18));
    	title.setLayoutX(70);
    	title.setLayoutY(120);
    	title.setTextAlignment(TextAlignment.CENTER);
    	
    	Label contactName = new Label("Nome do Contato :");
    	contactName.setFont(new Font("Arial",13));
    	contactName.setLayoutX(90);
    	contactName.setLayoutY(205);
    	
    	TextField contactNameInput = new TextField();
    	contactNameInput.setLayoutX(210);
    	contactNameInput.setLayoutY(202);
    	contactNameInput.setMinWidth(220);
    	
    	Label contactNumber = new Label("Telefone do Contato :");
    	contactNumber.setFont(new Font("Arial",13));
    	contactNumber.setLayoutX(75);
    	contactNumber.setLayoutY(265);
    	
    	TextField contactNumberInput = new TextField("1234-5678");
    	contactNumberInput.setLayoutX(210);
    	contactNumberInput.setLayoutY(262);
    	contactNumberInput.setMinWidth(220);
    	
    	Button createContactButton = new Button("Criar Contato");
    	createContactButton.setLayoutX(195);
    	createContactButton.setLayoutY(320);
    	createContactButton.setMinWidth(150);
    	createContactButton.setOnAction(event -> {	
    		
    		
        	Contact newContact = new Contact(contactNameInput.getText(),contactNumberInput.getText());
        	
        	Boolean hasContact = false;
        	
        	// A ação de criar um contato ocorre somente se o servidor estiver online
        	if(this.intermediatorConnection.checkConnection(serverUsed)) {
        		hasContact = this.intermediatorConnection.addNewContact(newContact);
        		
        		// Se o contato já existir na lista do servidor, a mensagem de erro é utilizada
            	if(hasContact) {
            		this.callshowErrorMessage("Nome de contato adicionado já existe \n na lista de contatos.");
            	}
            	
            	Stage window = (Stage)createContactButton.getScene().getWindow();
            	Scene scene = new Scene(createClientPage());
            	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            	window.setScene(scene);
            	window.setResizable(false);
        	}
        	
        	// Caso o servidor esteja offline, a tela de erro será mostrada
        	else {
    			Stage window = (Stage)createContactButton.getScene().getWindow();
            	Scene scene = new Scene(createServerErrorPage());
            	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            	window.setScene(scene);
            	window.setResizable(false);	
        	}
        	
			
        });
    	
    	root.getChildren().addAll(contactLabel, title, contactName, contactNameInput, contactNumber, contactNumberInput, createContactButton);
    	
    	return root;
    }
	
	@Override
	public void start(Stage primaryStage) {
		try {
			Scene clientPage = new Scene(createIntermediatorLogin());
			primaryStage.setTitle("Cliente");;
			primaryStage.setScene(clientPage);
			primaryStage.setResizable(false);
			primaryStage.show();
			clientPage.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
