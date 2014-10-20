package com.actuate.developer;
	
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.AWTException;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.mysql.jdbc.Connection;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class Main extends Application implements MqttCallback {
	private String username;
	private String password;
	private String connurl;
	private String tablename;
	private Connection conn;
	private MqttClient client;
	private String     driver = "com.mysql.jdbc.Driver";
	
	private String[] topics = {"$SYS/broker/bytes/received",
			 "$SYS/broker/bytes/sent",
			 "$SYS/broker/changeset",
			 "$SYS/broker/clients/active",
			 "$SYS/broker/clients/expired",
			 "$SYS/broker/clients/inactive",
			 "$SYS/broker/clients/maximum",
			 "$SYS/broker/clients/total",
			 "$SYS/broker/connection/m2m.cosm-data/state",
			 "$SYS/broker/connection/zenzium3.m2m/state",
			 "$SYS/broker/connection/zenzium3.zenzium1/state",
			 "$SYS/broker/heap/current",
			 "$SYS/broker/heap/maximum",
			 "$SYS/broker/load/bytes/received/15min",
			 "$SYS/broker/load/bytes/received/1min",
			 "$SYS/broker/load/bytes/received/5min",
			 "$SYS/broker/load/bytes/sent/15min",
			 "$SYS/broker/load/bytes/sent/1min",
			 "$SYS/broker/load/bytes/sent/5min",
			 "$SYS/broker/load/connections/15min",
			 "$SYS/broker/load/connections/1min",
			 "$SYS/broker/load/connections/5min",
			 "$SYS/broker/load/messages/received/15min",
			 "$SYS/broker/load/messages/received/1min",
			 "$SYS/broker/load/messages/received/5min",
			 "$SYS/broker/load/messages/sent/15min",
			 "$SYS/broker/load/messages/sent/1min",
			 "$SYS/broker/load/messages/sent/5min",
			 "$SYS/broker/load/publish/dropped/15min",
			 "$SYS/broker/load/publish/dropped/1min",
			 "$SYS/broker/load/publish/dropped/5min",
			 "$SYS/broker/load/publish/received/15min",
			 "$SYS/broker/load/publish/received/1min",
			 "$SYS/broker/load/publish/received/5min",
			 "$SYS/broker/load/publish/sent/15min",
			 "$SYS/broker/load/publish/sent/1min",
			 "$SYS/broker/load/publish/sent/5min",
			 "$SYS/broker/load/sockets/15min",
			 "$SYS/broker/load/sockets/1min",
			 "$SYS/broker/load/sockets/5min",
			 "$SYS/broker/messages/receive",
			 "$SYS/broker/messages/sent",
			 "$SYS/broker/messages/stored",
			 "$SYS/broker/publish/bytes/received",
			 "$SYS/broker/publish/bytes/sent",
			 "$SYS/broker/publish/messages/dropped",
			 "$SYS/broker/publish/messages/received",
			 "$SYS/broker/publish/messages/sent",
			 "$SYS/broker/retained messages/count",
			 "$SYS/broker/subscriptions/count",
			 "$SYS/broker/timestamp",
			 "$SYS/broker/uptime",
			 "$SYS/broker/version"};
	
	private String newClientID(int min, int max) {
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return "TestApplicationClient" + randomNum;
	}
	
	private void connectMQTT() {
		try {
			Class.forName(driver);
			conn = (Connection) DriverManager.getConnection(connurl, username, password);
			
			client = new MqttClient("tcp://iot.eclipse.org:1883", newClientID(1, 1000000));
			client.connect();
			client.setCallback(this);
			
			for(int i=0; i<topics.length; i++){
				client.subscribe(topics[i]);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			// Create the UI
			MenuBar menuBar = new MenuBar();
			Menu menuFile   = new Menu("File");
			
			MenuItem fileExit  = new MenuItem("Exit");
			
			fileExit.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent t) {
					System.exit(0);
				}
			});

			Image birtLogo = new Image("http://localhost/other/BIRT-Logo_Purple.png");
			ImageView imgView = new ImageView(birtLogo);
			
			final Button btnMqtt = new Button();
			btnMqtt.setText("Connect to eclipse.mqttbridge.com");

			menuFile.getItems().addAll(fileExit);
			menuBar.getMenus().addAll(menuFile);
			
			GridPane grid = new GridPane();
			GridPane browserGrid = new GridPane();
			
			GridPane formGrid = new GridPane();
			
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(25,25,25,25));
			
			grid.add(imgView, 0, 0);
			
			formGrid.setAlignment(Pos.CENTER);
			formGrid.setHgap(10);
			formGrid.setVgap(10);
			formGrid.setPadding(new Insets(25,25,25,25));
			
			final Label lblUsername  = new Label("MySQL Username:");
			final Label lblPassword  = new Label("MySQL Password:");
			final Label lblConnurl   = new Label("MySQL URL:");
			final Label lblTablename = new Label("MySQL Table:");
			
			final TextField     txtUsername  = new TextField();
			final PasswordField txtPassword  = new PasswordField();
			final TextField     txtConnurl   = new TextField();
			final TextField     txtTablename = new TextField();
			
			WebView browser = new WebView();
			WebEngine webEngine = browser.getEngine();
			browser.autosize();
			webEngine.load("http://localhost/stats.html");
			
			formGrid.add(lblUsername,  0, 0);
			formGrid.add(lblPassword,  0, 1);
			formGrid.add(lblConnurl,   0, 2);
			formGrid.add(lblTablename, 0, 3);
			
			formGrid.add(txtUsername,  1, 0);
			formGrid.add(txtPassword,  1, 1);
			formGrid.add(txtConnurl,   1, 2);
			formGrid.add(txtTablename, 1, 3);
			
			formGrid.add(btnMqtt, 0, 4, 2, 1);
			//grid.add(browser, 3, 0, 2, 4);
			browserGrid.add(browser, 0, 0);
			grid.add(formGrid, 0, 0);
			grid.add(browserGrid, 3, 0);
			
			
			// Listeners #########################################################
			btnMqtt.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {					
					if(txtUsername.getText().isEmpty()) {
						lblUsername.setTextFill(Color.RED);
					}else{
						lblUsername.setTextFill(Color.BLACK);
						username = txtUsername.getText();
					}
					
					if(txtPassword.getText().isEmpty()) {
						lblPassword.setTextFill(Color.RED);
					}else{
						lblPassword.setTextFill(Color.BLACK);
						password = txtPassword.getText();
					}
					
					if(txtConnurl.getText().isEmpty()) {
						lblConnurl.setTextFill(Color.RED);
					}else{
						lblConnurl.setTextFill(Color.BLACK);
						connurl = txtConnurl.getText();
					}
					
					if(txtTablename.getText().isEmpty()) {
						lblTablename.setTextFill(Color.RED);
					}else{
						lblTablename.setTextFill(Color.BLACK);
						tablename = txtTablename.getText();
					}
					
					if(lblUsername.getTextFill() != Color.RED &&
							lblPassword.getTextFill() != Color.RED &&
							lblConnurl.getTextFill() != Color.RED &&
							lblTablename.getTextFill() != Color.RED &&
							btnMqtt.getText().contains("Connect")) {

						
						if(client == null) {
							btnMqtt.setText("Disconnect from eclipse.mqttbridge.org");
							connectMQTT();
						}else{
							try{
								client.connect();
								btnMqtt.setText("Disconnect from eclipse.mqttbridge.org");
							}catch(MqttException ex){
								ex.printStackTrace();
							}
						}
					}else{
						try {
							client.disconnect();
							btnMqtt.setText("Connect to eclipse.mqttbridge.org");
						}catch(MqttException ex){
							ex.printStackTrace();
						}
					}
				}
			});
			
			Scene primaryScene = new Scene(new VBox(), 1300, 700);
			((VBox) primaryScene.getRoot()).getChildren().addAll(menuBar, grid);
			primaryStage.setScene(primaryScene);
			primaryStage.setTitle("BIRT Eclipse IoT Bridge Example");
			primaryStage.getIcons().add(new Image("file:res/BIRT-Logo_Purple.png"));
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void connectionLost(Throwable cause) {
		System.out.println("Connection lost!");		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println("Delivery complete!");
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			
			String query = "insert into " + tablename + " (topic, received, message)" + " values (?, ?, ?)";
			
			PreparedStatement preparedStmt = (PreparedStatement) conn.prepareStatement(query);
			preparedStmt.setString(1, topic);
			preparedStmt.setString(2, dateFormat.format(cal.getTime()));
			preparedStmt.setString(3, message.toString());
			
			preparedStmt.execute();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		System.out.println("Topic: " + message.toString());
	}
}
