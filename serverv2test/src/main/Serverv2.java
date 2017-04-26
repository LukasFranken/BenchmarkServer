package main;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Serverv2 extends javax.swing.JFrame {
	
    private JScrollPane jScrollPane1;
    private JTextArea outputPane;
    private JButton startButton;
    private JButton stopButton;
    
	private String temppath = "lulpath";
    
    DBHandler dbhandler = new DBHandler();
	
	ArrayList clientOutputStreams;
    ArrayList<String> onlineUsers;
    ArrayList<String> node;

	public class ClientHandler implements Runnable	{
		String currentUser; 
		ObjectInputStream isReader;
		Socket sock;
        LabeledObjectOutputStream client;


		public ClientHandler(Socket clientSocket, LabeledObjectOutputStream user) throws IOException {
                        client = user;
                        client.flush();
			try {
				sock = clientSocket;
				isReader = new ObjectInputStream(sock.getInputStream());
			} 
			catch (Exception ex) {
				outputPane.append("* Error beginning StreamReader. \n");
			} 
			
			client.setPipeType("unvalidated");
			client.setUsername("undefined");

		} 

		public void run() {
			
			String message = " You are now Connected! \n";
			sendMessage(message, client);
			try {
				while(isReader.readObject() != null){
					try{
						message = (String) isReader.readObject();
						outputPane.append("\n# " + message);
						System.out.println("message received:" + message);
						if(message.startsWith("!")){
							System.out.println("command triggered");
							if(message.startsWith("!login")){
								
								// turn bool into strign with currentUSer
								String loginSuccess = "nope";
								String userData[];
								loginSuccess = operateLogin(message, client);
								if(!loginSuccess.startsWith("nope")){
									
									userData = loginSuccess.split(" ");
									
									System.out.println("userADD: " + userData[0] + " " + userData[1]);
									userAdd(userData[0] + " " + userData[1]);
									client.setUsername(userData[0]);
									client.setPipeType(userData[1]);
									
									
									
								}
								System.out.println("Successful login: " + loginSuccess);
							}else{
								operateCommand(message, client);
							}
							
						}
						
					}catch(ClassNotFoundException classNotFoundException){
						outputPane.append("\n * idk wtf that user sent!");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//client remove from online users!
			} catch (ClassNotFoundException e) {
				System.out.println("\n user: " + currentUser + " disconnected1.");
				e.printStackTrace();
			} catch (IOException e) {
				outputPane.append("* User removed: " + currentUser);
				userRemove(client.getUsername() + " " + client.getPipeType());
				System.out.println("\n user: " + currentUser + " disconnected2.");
				e.printStackTrace();
			}
		} 
	}
                        
    public Serverv2() {

        jScrollPane1 = new javax.swing.JScrollPane();
        outputPane = new javax.swing.JTextArea();
        startButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("House Server");

        outputPane.setColumns(20);
        outputPane.setEditable(false);
        outputPane.setLineWrap(true);
        outputPane.setRows(5);
        outputPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane1.setViewportView(outputPane);

        startButton.setText("Start");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        stopButton.setText("Stop");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(startButton, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(stopButton, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startButton)
                    .addComponent(stopButton))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        pack();
        
        setupDatabase();
    }                      

    private void startButtonActionPerformed(ActionEvent evt) {                                            
        Thread starter = new Thread(new ServerStart());
        starter.start();

        outputPane.append("* Server started at port: " + dbhandler.globalPort + "\n");
    }                                           

    private void stopButtonActionPerformed(ActionEvent evt) {                                           

        tellEveryone("Server: is stopping and all users will be disconnected.\n:Chat");
        outputPane.append("* Server stopping... \n");

    }    
    
	private void setupDatabase(){
		
		String databaseCheck = dbhandler.callData();
		
		if(databaseCheck.equals("")){
			databaseCheck = " No problems detected.";
		}
		
		outputPane.append("* Database check returned:" + databaseCheck + "\n");
		
		if(databaseCheck.startsWith("- dbinitvar")){
			databaseCheck = dbhandler.callData();
			if(databaseCheck.equals("")){
				databaseCheck = " No problems detected.";
			}
			outputPane.append("* Reinitialized Database check returned:" + databaseCheck + "\n");
		}
		
		if(dbhandler.isConnectedToDB){
			outputPane.append("* Server connected successfully to existing Database at: " + dbhandler.postProcessDefPath + "! \n");
		}else{
			outputPane.append("* DB-Error: Connection to Database failed.\n");
		}

	}

    public static void main(String args[]) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Serverv2().setVisible(true);
            }
        });
    }


    public class ServerStart implements Runnable {
        public void run() {
                    clientOutputStreams = new ArrayList();
                    onlineUsers = new ArrayList();  
                    node = new ArrayList();

                    try {
                    	ServerSocket serverSock = new ServerSocket(dbhandler.globalPort);

                    	while (true) {

				Socket clientSock = serverSock.accept();
				LabeledObjectOutputStream Ostream = new LabeledObjectOutputStream(clientSock.getOutputStream());
				clientOutputStreams.add(Ostream);


				Thread listener = new Thread(new ClientHandler(clientSock, Ostream));
				listener.start();
				outputPane.append("Got a connection. \n");
			} 
		} 
		catch (Exception ex)
		{
			outputPane.append("Error making a connection. \n");
		} 

	}
    }

    public void userAdd (String data) {

        onlineUsers.add(data);
            

	}


	public void userRemove (String data) {

        onlineUsers.remove(data);
 
        pushOnlineUserUpdate();
	}
	
	public void pushOnlineUserUpdate(){
		String currentOnlineUsers = "!updateOnlineUsers";
		
		for(int i = 0; i < onlineUsers.size(); i++){
			currentOnlineUsers = currentOnlineUsers + " " + onlineUsers.get(i);
		}
		
		tellEveryone(currentOnlineUsers);
	}
	
	public void pushDirectoryUpdate(){
		String currentDirectoryStructure = "!updateDirectory";
		
		currentDirectoryStructure = currentDirectoryStructure + dbhandler.createDirectoryString();
		
		tellEveryone(currentDirectoryStructure);
	}

     public void tellEveryone(String message) {  //obsolete

		Iterator it = clientOutputStreams.iterator();

		while (it.hasNext()) {
			System.out.println(clientOutputStreams);
			try {
				LabeledObjectOutputStream OstreamTemp = (LabeledObjectOutputStream) it.next();
				OstreamTemp.writeObject(message);
				outputPane.append("\nSending to all: " + message + "\n");
                                OstreamTemp.flush();
                                outputPane.setCaretPosition(outputPane.getDocument().getLength());

			} 
			catch (Exception ex) {
				outputPane.append("Error telling everyone. \n");
			} 
		} 
	}
     
    public void sendMessage(String message, LabeledObjectOutputStream userStream){
    	
    	
    	
    	try {
			userStream.writeObject(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	outputPane.append("Sending to user: " + message + "\n");
    	try {
			userStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    private void operateCommand(String command, LabeledObjectOutputStream userStream){
		
		if(command.equals("!respond")){
			sendMessage("Server responds successfully.", userStream);
		}
		
		if(command.startsWith("!createDirectory")){
			String[] path;
			path = command.split(" ");
			System.out.println(path[1]);
			dbhandler.setDefaultDBPath(path[1]);
			outputPane.append("Directory created! ");
		}
		
		if(command.startsWith("!setPort")){
			String[] portString;
			int port;
			portString = command.split(" ");
			port = Integer.parseInt(portString[1]);
			dbhandler.setPort(port);
			outputPane.append("Default Port set. Please restart to redirect.");
		}
		
		if(command.startsWith("!createTable")){
			String[] tablename;
			tablename = command.split(" ");
			if(dbhandler.checkTable(tablename[1])){
				outputPane.append("\n Table already exists. If you want to overwrite an existing table, please use the !overwriteTable command. Table not created.");
			}else{
				if(dbhandler.createTable(tablename[1])){
					outputPane.append("\n Table successfully added to the Database.");
				}else{
					outputPane.append("\n Error: Table could not be added to the Filesystem.");
				}
			}
		}
		
		if(command.startsWith("!overwriteTable")){
			String[] tablename;
			tablename = command.split(" ");
			if(dbhandler.checkTable(tablename[1])){
				outputPane.append("\n Table already exists. Overwriting initiated.");
				if(dbhandler.overwriteTable(tablename[1])){
					outputPane.append("\n Table successfully overwritten.");
				}else{
					outputPane.append("\n Error: Table could not be added to the Filesystem.");
				}
			}else{
				outputPane.append("\n Table does not Exist. Please use the !createTable command.");
			}
		}
		
		if(command.startsWith("!createUser")){
			String[] username;
			username = command.split(" ");
			if(dbhandler.checkUser(username[1])){
				outputPane.append("\n User already exists.");
			}else{
				
				String name = username[1];
				String pass = username[2];
				String priviledge = username[3];
				
				if(dbhandler.createUser(name, pass, priviledge)){
					outputPane.append("\n* User successfully added to the Database.");
				}else{
					outputPane.append("\n* Error: User could not be added to the Database.");
				}
			}
		}
		
		if(command.startsWith("!requestUserlistUpdate")){
	        pushOnlineUserUpdate();
		}
		
		if(command.startsWith("!requestDirectoryUpdate")){
			pushDirectoryUpdate();
		}
		
		
		if(command.startsWith("!appendTableLine")){
			
		}
		
	}
    
    public boolean isOnlineClient(String credentialCheck){
    	boolean updateSuccess = false;
    	
    	//stub, return true, if successfully added logindata on server
    	
    	for(int i = 0; i < onlineUsers.size(); i++){
    		System.out.println(onlineUsers.get(i) + " + " + credentialCheck);
    		if(onlineUsers.get(i).equals(credentialCheck)){
    			updateSuccess = true;
    		}
    	}
    	
    	return updateSuccess;
    }
    
    public boolean updateClientStream(LabeledObjectOutputStream oldStream, LabeledObjectOutputStream newStream){
    	boolean updateSuccessful = false;
    	for(int i = 0; i < clientOutputStreams.size(); i++){
    		if(clientOutputStreams.get(i) == oldStream){
    			clientOutputStreams.remove(i);
    			clientOutputStreams.add(newStream);
    			updateSuccessful = true;
    		}
    	}
    	return updateSuccessful;
    }
    
    public String operateLogin(String command, LabeledObjectOutputStream userStream){
    	
    	String accepted = "nope";
    	
			String[] username;
			username = command.split(" ");
			if(!dbhandler.checkUser(username[1])){
				outputPane.append("\n* User doesn't exist.");
				sendMessage("!invalidUser", userStream);
			}else{
				
				if(dbhandler.loginUser(username[1], username[3])){
					outputPane.append("\n* User " + username[1] + " successfully logged in.");
					accepted = username[1] + " " + dbhandler.getUserPriviledges(username[1]);
					sendMessage("!SuccessfulUserLogin: " + accepted, userStream);
				}else{
					sendMessage("!wrongPass", userStream);
					outputPane.append("\n* Error: User Password wrong.");
				}

			}
			
		return accepted;

    }
                 

}