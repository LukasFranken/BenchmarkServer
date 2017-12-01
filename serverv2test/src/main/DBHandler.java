package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;

import main.WinRegistry;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class DBHandler {
	
	static WinRegistry regedit = new WinRegistry();
	
	private int osType = 32;
	private String defaultPath = "jdbc:mysql://localhost:";
	private int defaultDBPort = 3306;
	private String defaultDBName = "testdb";
	private String maintenanceVarsDB = "?verifyServerCertificate=false&useSSL=true";
	private boolean wasInstanciatedThisSession = false;
	
	public static ArrayList<String> tableNames = new ArrayList<String>();
	
	static String postProcessDefPath = "default";
	static int wasInitialized = 0;
	static int globalPort = 5000;
	static boolean isConnectedToDB = false;
	static Connection con;
	
	private DateFormat dateFormatTime = new SimpleDateFormat("HH:mm");
	private DateFormat dateFormatDate = new SimpleDateFormat("dd.MM.yyyy");
	
	static ArrayList<String> tablesToCreate;
	
	public DBHandler(){
		//for future reference
	}
	
	private void instanciate(String requestedPath){
		
		//detect 64-bit systems for future optimizing purposes
		
		if(System.getProperty("os.arch").equals("amd64")){
			osType = 64;
		}
		
		try {
			regedit.writeStringValue(0x80000001, "SOFTWARE", "BenchmarkDefDBPath", requestedPath);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		try {
			regedit.writeStringValue(0x80000001, "SOFTWARE", "BenchmarkInitialized", "1");
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		try {
			regedit.writeStringValue(0x80000001, "SOFTWARE", "DefaultPort", "5000");
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		wasInstanciatedThisSession = true;
		
	}

	
	public String callData() {
		
		tablesToCreate = new ArrayList<String>();
		
		String returnMessage = "";
		
		try {
			wasInitialized = Integer.parseInt(regedit.readString(0x80000001, "SOFTWARE", "BenchmarkInitialized"));
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			returnMessage = returnMessage + "- dbinitvar failure, restarting calldata.";
			instanciate(defaultPath + defaultDBPort + "/" + defaultDBName + maintenanceVarsDB);
		}
		
		try {
			postProcessDefPath = regedit.readString(0x80000001, "SOFTWARE", "BenchmarkDefDBPath");
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			returnMessage = returnMessage + "- defpath read failure ";
		}
		
		if(getPort().equals("failure")){
			returnMessage = returnMessage + "- port key failure ";
		}
		
		if(connectToDB(postProcessDefPath).equals("failure")){
			returnMessage = returnMessage + "- dbconnection failure ";
		}else{
			
			if(!checkTable("userdata")){
				createUserTable();
				returnMessage += "- userdata Table not found. Created new one.";
			}
			
			if(!checkTable("tabledata")){
				System.out.println("cat table called!");
				createCatTable();
				returnMessage += "- Category Table not found. Created new one.";
			}
			
			if(!checkTable("begrunddata")){
				System.out.println("begrund table called!");
				createBegrundTable();
				returnMessage += "- Begründungs Table not found. Created new one.";
			}
			
			if(!checkTable("metadata")){
				System.out.println("meta table called!");
				createMetaTable();
				returnMessage += "- Meta Table not found. Created new one.";
			}
			
			if(!checkTable("hiddendata")){
				System.out.println("hidden table called!");
				createHiddenTable();
				returnMessage += "- Hidden Table not found. Created new one.";
			}
			
			if(!checkTable("deletedata")){
				System.out.println("hidden table called!");
				createDeleteTable();
				returnMessage += "- Delete Table not found. Created new one.";
			}
			
			if(!checkTable("historydata")){
				System.out.println("history table called!");
				createHistoryTable();
				returnMessage += "- History Table not found. Created new one.";
			}
			
		}
		
		return returnMessage;
	}
	
	private String connectToDB(String path){
		
		String returnMessage = "";
		
		try {
			con = DriverManager.getConnection(path, "root" , "Spahiton1");
			isConnectedToDB = true;
		} catch (SQLException e) {
			returnMessage = "failure";
			e.printStackTrace();
		}
		
		return returnMessage;
	}
	
	private void createBegrundTable(){
		try {
			Statement statement = con.createStatement();
			
		    String sql = "CREATE TABLE BEGRUNDDATA " +
	                     "(ID INTEGER not NULL AUTO_INCREMENT, " +
	                     " global VARCHAR(255), " + 
	                     " PRIMARY KEY ( ID ))"; 
			
			int result = statement.executeUpdate(sql);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void createUserTable(){
		try {
			Statement statement = con.createStatement();
			
		    String sql = "CREATE TABLE USERDATA " +
	                     "(ID INTEGER not NULL AUTO_INCREMENT, " +
	                     " username VARCHAR(255), " + 
	                     " passwort VARCHAR(255), " + 
	                     " privilegien VARCHAR(255), " + 
	                     " PRIMARY KEY ( ID ))"; 
			
			int result = statement.executeUpdate(sql);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void createHiddenTable(){
		try {
			Statement statement = con.createStatement();
			
		    String sql = "CREATE TABLE HIDDENDATA " +
	                     "(ID INTEGER not NULL AUTO_INCREMENT, " +
	                     " tables VARCHAR(255), " + 
	                     " substances VARCHAR(255), " + 
	                     " PRIMARY KEY ( ID ))"; 
			
			int result = statement.executeUpdate(sql);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void createDeleteTable(){
		try {
			Statement statement = con.createStatement();
			
		    String sql = "CREATE TABLE DELETEDATA " +
	                     "(ID INTEGER not NULL AUTO_INCREMENT, " +
	                     " tablename VARCHAR(255), " + 
	                     " identification VARCHAR(255), " + 
	                     " begrundung VARCHAR(255), " + 
	                     " requestinguser VARCHAR(255), " + 
	                     " PRIMARY KEY ( ID ))"; 
			
			int result = statement.executeUpdate(sql);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void createHistoryTable(){
		try {
			Statement statement = con.createStatement();
			
		    String sql = "CREATE TABLE HISTORYDATA " +
	                     "(ID INTEGER not NULL AUTO_INCREMENT, " +
	                     " Tabelle VARCHAR(255), " + 
	                     " Eintragsdatum VARCHAR(255), " + 
	                     " Eintragsuhrzeit VARCHAR(255), " + 
	                     " Absendedatum VARCHAR(255), " + 
	                     " Absendeuhrzeit VARCHAR(255), " + 
	                     " Name VARCHAR(255), " + 
	                     " EintragsID VARCHAR(255), " + 
	                     " Stoffe VARCHAR(255), " + 
	                     " Begründung VARCHAR(255), " + 
	                     " PRIMARY KEY ( ID ))"; 
			
			int result = statement.executeUpdate(sql);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void createMetaTable(){
		try {
			Statement statement = con.createStatement();
			
		    String sql = "CREATE TABLE METADATA " +
	                     "(ID INTEGER not NULL AUTO_INCREMENT, " +
	                     " PRIMARY KEY ( ID ))"; 
			
			int result = statement.executeUpdate(sql);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void createCatTable(){
		
		try {
			Statement statement = con.createStatement();
			
		    String sql = "CREATE TABLE TABLEDATA " +
	                     "(ID INTEGER not NULL AUTO_INCREMENT, " +
		     			 " PRIMARY KEY ( ID ))"; 
	        System.out.println(sql);
			
			int result = statement.executeUpdate(sql);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public String getPort(){
		
		String returnMessage = "";
		
		try {
			globalPort = Integer.parseInt(regedit.readString(0x80000001, "SOFTWARE", "DefaultPort"));
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			returnMessage = "fail";
			e.printStackTrace();
		}
		
		return returnMessage;
	}
	
	//old
	public void initData(String path){
		instanciate(path);
	}
	
	public void setDefaultDBPath(String path){
		
	// function to set the default Database path and write it to registry for longtime purposes
		
		try {
			regedit.writeStringValue(0x80000001, "SOFTWARE", "BenchmarkDefDBPath", path);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
	}
	

	
	public void setPort(int port){
		
		//write custom port to listen on in registry.
		
		try {
			regedit.writeStringValue(0x80000001, "SOFTWARE", "DefaultPort", Integer.toString(port));
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public boolean checkTable(String tablename){
		
		//check, if a tablename exists already
		
		boolean exists = false;
		
		try {
			DatabaseMetaData meta = con.getMetaData();
			ResultSet res = meta.getTables(null, null, tablename, 
					  				       new String[] {"TABLE"});
			 
			while (res.next()) {
				System.out.println("comparing: " + res.getString("TABLE_NAME") + "with: " + tablename.toLowerCase());
				if(res.getString("TABLE_NAME").equals(tablename.toLowerCase())){
					System.out.println("check table true!!!");
				    exists = true;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return exists;
	}
	
	public ArrayList<String> generateTableNameList(String priviledge){
		ArrayList<String> tableNames = new ArrayList<String>();
		
		//if priviledge != Admin, check for hidden substances and leave them out
		ArrayList<String> hiddenTables = new ArrayList<String>();
		if(!priviledge.equals("Admin")){
			
			try {
				Statement statement = con.createStatement();
				//System.out.println(tableName);
				ResultSet set = statement.executeQuery("SELECT * FROM testdb.`" + "hiddendata" + "`");
			    while(set.next()){
			    	if(set.getString("tables") != null){
			    		hiddenTables.add(set.getString("tables"));
			    	}
			    }
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		try {
			Statement statement = con.createStatement();
			//System.out.println(tableName);
			ResultSet set = statement.executeQuery("SELECT * FROM testdb.tabledata");
			
			String tablePointer = "";
			
			for(int i = 2; i <= set.getMetaData().getColumnCount(); i++){
				
				set.beforeFirst();
				while(set.next()){
					
					tablePointer = set.getString(set.getMetaData().getColumnName(i));
					
					if(tablePointer != null){
						
						
						if(!priviledge.equals("Admin")){
							
							if(!hiddenTables.contains(set.getMetaData().getColumnName(i).toLowerCase() + "#" + tablePointer.toLowerCase())){
								tableNames.add(set.getMetaData().getColumnName(i).toLowerCase() + "#" + tablePointer.toLowerCase());
								
							}
							
						}else{
							tableNames.add(set.getMetaData().getColumnName(i).toLowerCase() + "#" + tablePointer.toLowerCase());
							
						}
						
					}
					
				}
				
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tableNames;
	}
	
	//Obsolete
	public String createTable(String tablename, String subfoldername,  ArrayList<String> columns){
		boolean tableExists = checkTable(tablename);
		
		String response = "";
		String finaltablename = subfoldername.toLowerCase() + "#" + tablename.toLowerCase();
		
		if(!tableExists){
			try {
				Statement statement = con.createStatement();
				
			    String sql = "CREATE TABLE `" + finaltablename + "` " +
		                     "(ID INTEGER not NULL AUTO_INCREMENT, ";
			    
			    for(int i = 0; i < columns.size(); i++){
			    	sql += " `"+ columns.get(i) + "` VARCHAR(255), ";
			    }
			    
			    sql += " PRIMARY KEY ( ID ))"; 
		        System.out.println(sql);
				
				int result = statement.executeUpdate(sql);
				System.out.println("table didnt exist. created new one.");

			} catch (SQLException e) {
				System.out.println("table existed. no new created!");
				e.printStackTrace();
			}
			
		
		
		boolean exists = createSubfolder(subfoldername);
		if(exists){
			String tempString = "";
			int sfid = 0;
			try {
				Statement statement = con.createStatement();
				
			    String sql = "select * from testdb.tabledata"; 
				
				ResultSet res = statement.executeQuery(sql);
				
				
				
				try {
					Statement stmnt = con.createStatement();
				
					String sql2 = "INSERT INTO testdb." + "`tabledata`" + "(`" + subfoldername + "`)" + "VALUES " + "('" + tablename.toLowerCase() + "')";
				
					int result = stmnt.executeUpdate(sql2);
					System.out.println("subfolder didnt exist. created new one. inserted successfully into tabledata!");
					response += "- table created!";
				} catch (SQLException e) {
					response += "- unknown error table creation!";
					e.printStackTrace();
				}
				
				
				
				/*while(res.next()){
					System.out.println(res.getString("folder"));
					if(res.getString("folder") != null){
						if(res.getString("folder").equals(subfoldername)){
							for(int i = 0; i <= 30; i++){
								tempString = res.getString("subfolder" + i);
								System.out.println("subfolder" + i + ":" + tempString);
								if(tempString == null){
									//add to subfolder i chain
									
									try {
										Statement stmnt = con.createStatement();
									
										String sql2 = "UPDATE testdb.tabledata SET subfolder" + i + " = '" + tablename + "' WHERE folder = '" + subfoldername +"'";
									
										int result = stmnt.executeUpdate(sql2);
										System.out.println("subfolder didnt exist. created new one. inserted successfully into tabledata!");
										response += "- table created!";
									} catch (SQLException e) {
										response += "- unknown error table creation!";
										e.printStackTrace();
									}
									
									break;
								}
							}
							
							//TODO Handle maximum capacity
						}
					}
				}*/
				
			} catch (SQLException e) {
				response += "- couldnt get tabledata!";
				e.printStackTrace();
			}
			
		}else{
			try {
				Statement statement = con.createStatement();
			
				System.out.println("subfolder didnt exist. created new one. inserted successfully into tabledata!");
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			//createTable(tablename, subfoldername,  columns);
			addToMetaData(tablename, subfoldername);
		}
		}else{
			//createTable(tablename + "1", subfoldername,  columns);
			response += "- table existed! added substring identifier.";
		}
		
		appendEmptyLine(tablename, subfoldername);
		
		
		String finalTableName = subfoldername.toLowerCase() + "#" + tablename.toLowerCase();
		if(addTableToBegrundung(finalTableName)){
			response += "- table added successfully to begrundung.";
		}else{
			response += "- table add to begrundung unsuccessful!";
		}
		
		return response;
		
	}
	
	public void addToMetaData(String tablename, String foldername){
		
		
		
	}
	
	public void appendEmptyLine(String tableName, String subfolderName){
		
		System.out.println("appending empty line for: " + tableName);
		
		ArrayList<String> columns = new ArrayList<String>();
		
		String finalTableName = "";
		
		finalTableName = subfolderName.toLowerCase() + "#" + tableName.toLowerCase();
		
		try {
			Statement statement = con.createStatement();
			ResultSet set = statement.executeQuery("SELECT * FROM testdb.`" + finalTableName + "`");
			
			ResultSetMetaData md = set.getMetaData();
			for (int i=2; i<=md.getColumnCount(); i++)
			{
			    System.out.println("columnLabel: " + md.getColumnLabel(i));
			    columns.add(md.getColumnLabel(i));
			}

			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			Statement statement = con.createStatement();
			
			String columnLabelSqlString = "(";
			
			for(int i = 0; i < columns.size(); i++){
				if(i == 0){
					columnLabelSqlString += "`"+columns.get(i)+"`";
				}else{
					columnLabelSqlString += ", " + "`"+columns.get(i)+"`";
				}
			}
			
			columnLabelSqlString += ") ";
			
			String columnEntrySqlString = "(";
			
			for(int i = 0; i < columns.size(); i++){
				
				if(i == 0){
					columnEntrySqlString +="'"+" "+"'";
				}else{
					columnEntrySqlString += ", " + "'"+" "+"'";
				}
				
			}
			
			columnEntrySqlString += ") ";
			
			
		    String sql = "INSERT INTO testdb." + "`" + finalTableName + "`" + columnLabelSqlString +
	                     "VALUES " + columnEntrySqlString; 
			
		    System.out.println("final SQL apenndemptyline: " + sql);
		    
			int result = statement.executeUpdate(sql);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	//Obsolete
	public boolean overwriteTable(String tablename){
		
		//create new file and delete already existing table with same name.
		//CAUTION HERE WHEN USING! for data safety purposes this function will be changed and might be replaced.
		
		return true;
		
	}
	
	
	public boolean createUser(String name, String pass, String priviledges){
		
		//create a User file that will represent the userdata related to a user account
		boolean created = false;
		
		try {
			Statement statement = con.createStatement();
			
		    String sql = "INSERT INTO testdb.userdata(username, passwort, privilegien) " +
	                     "VALUES ('" + name + "','" + pass + "','" + priviledges + "')"; 
			
			int result = statement.executeUpdate(sql);

			created = true;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return created;
		

	}
	
	public boolean checkUser(String username){
		
		//check if a userfile exists for given name
		
		boolean exists = false;
		
		try {
			Statement statement = con.createStatement();
			
		    String sql = "SELECT username FROM testdb.userdata"; 
			
			ResultSet res = statement.executeQuery(sql);
			
			while(res.next()){
				if(res.getString("username").equals(username)){
					exists = true;
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return exists;

	}
	
	
	//Obsolete
	public boolean loginUser(String name, String password){
		
		//login routine for an encoded user account.
		
		boolean accepted = false;
		
		try {
			Statement statement = con.createStatement();
			
		    String sql = "SELECT * FROM testdb.userdata"; 
			
			ResultSet res = statement.executeQuery(sql);
			
			while(res.next()){
				System.out.println("login while name: " + res.getString("username"));
				if(res.getString("username").equals(name)){
					System.out.println("login while trigered to pass request: " + res.getString("passwort"));
					if(res.getString("passwort").equals(password)){
						System.out.println("login while actually accepted!");
						accepted = true;	
						break;
					}
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return accepted;
	}
	
	
	public String createDirectoryString(){
		String directoryString = "";
		
		try {
			Statement statement = con.createStatement();
			
		    String sql = "SELECT * FROM testdb.tabledata"; 
			
			ResultSet res = statement.executeQuery(sql);
			
			ResultSetMetaData md = res.getMetaData();
			
			for (int i=2; i<=md.getColumnCount(); i++)
			{
			    
			    
				directoryString += " * " + md.getColumnLabel(i);
				
				res = statement.executeQuery(sql);
				while(res.next()){
					
					if(res.getString(i) != null){
						directoryString += " " + res.getString(i);
						tableNames.add(res.getString(i));
					}
				}
				
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		directoryString += " #";
		
		
		
		System.out.println("directory String final: " + directoryString);
		return directoryString;
	}

	public String getUserPriviledges(String name) {
		String priviledgeID = "";
		
		try {
			Statement statement = con.createStatement();
			
		    String sql = "SELECT * FROM testdb.userdata"; 
			
			ResultSet res = statement.executeQuery(sql);
			
			while(res.next()){
				if(res.getString("username").equals(name)){
					priviledgeID = res.getString("privilegien");
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return priviledgeID;
	}

	public boolean createSubfolder(String name) {
		
		boolean exists = false;
		
		try {
			Statement statement = con.createStatement();
			
		    String sql = "SELECT * FROM testdb.tabledata"; 
			
			ResultSet res = statement.executeQuery(sql);
			ResultSetMetaData md = res.getMetaData();
			
			for(int i = 1; i <= md.getColumnCount(); i++){
				
				System.out.println("COLUMN comparing: " + name + " " + md.getColumnName(i));
				if(md.getColumnName(i).equals(name)){
					exists = true;
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(!exists){
			try {
				Statement statement = con.createStatement();

				String sql = "ALTER TABLE `testdb`.`tabledata` ADD COLUMN `" + name + "` VARCHAR(255) NULL DEFAULT NULL;";
				
				System.out.println(sql);
			
				int result = statement.executeUpdate(sql);
			
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return exists;
		
	}

	public void appendTableLine(String[] tableLineData, String name) {
		
		Date date = new Date();
		
		//add columnname section
		ArrayList<String> columns = new ArrayList<String>();
		try {
			Statement statement = con.createStatement();
			ResultSet set = statement.executeQuery("SELECT * FROM testdb.`" + tableLineData[1] + "`");
			
			ResultSetMetaData md = set.getMetaData();
			for (int i=2; i<=md.getColumnCount(); i++)
			{
			    columns.add(md.getColumnLabel(i));
			}

			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		//String timeStampTime = new SimpleDateFormat("HH").format(new Date(System.currentTimeMillis())) + ":" + new SimpleDateFormat("mm").format(new Date(System.currentTimeMillis()));
		//String timeStampDate = new SimpleDateFormat("dd.MM.yyyy").format(new Date(System.currentTimeMillis()));
		
		try {
			Statement statement = con.createStatement();
			
			String columnLabelSqlString = "(";
			
			for(int i = 0; i < columns.size(); i++){
				if(i == 0){
					columnLabelSqlString += "`"+columns.get(i)+"`";
				}else{
					columnLabelSqlString += ", " + "`"+columns.get(i)+"`";
				}
			}
			
			columnLabelSqlString += ") ";
			
			System.out.println("final CLSS: " + columnLabelSqlString);
			
			columns.remove(0);
			columns.remove(0);
			columns.remove(0);
			columns.remove(0);
			columns.remove(0);

			for(int i = 0; i < columns.size(); i++){
				if(columns.get(i).equals("Begründung")){
					columns.remove(i);
				}
			}
			
			
			String stoffString = "";
			for(int i = 0; i < columns.size(); i++){
				if(columns.get(i).equals(tableLineData[2])){
					stoffString += tableLineData[3] + "','";
				}else{
					stoffString += " ','";
				}
			}
			
		    String sql = "INSERT INTO testdb." + "`" + tableLineData[1] + "`" + columnLabelSqlString +
	                     "VALUES ('" + tableLineData[4] + "','" + tableLineData[5] + "','" + name + "','" + dateFormatDate.format(date) + "','" + dateFormatTime.format(date) + "','" + stoffString + tableLineData[6] + "')"; 
			
		    
			int result = statement.executeUpdate(sql);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	public String generateTableDataString(String tableName, String priviledge) {
		
		if(tableName.equals("userdata")){
			tableName = "";
		}
		
		if(tableName.equals("happyhippounicorn")){
			tableName = "userdata";
		}
		
		//add tablename section
		String message = "!tableData&";
		if(checkTable(tableName)){
			message += tableName + "&&";
		}
		
		//if priviledge != Admin, check for hidden substances and leave them out
		ArrayList<String> hiddenSubstances = new ArrayList<String>();
		if(!priviledge.equals("Admin")){
			hiddenSubstances.add(tableName + "#Absendedatum");
			hiddenSubstances.add(tableName + "#Absendeuhrzeit");
			try {
				Statement statement = con.createStatement();
				//System.out.println(tableName);
				ResultSet set = statement.executeQuery("SELECT * FROM testdb.`" + "hiddendata" + "`");
			    while(set.next()){
			    	if(set.getString("substances") != null){
			    		hiddenSubstances.add(set.getString("substances"));
			    	}
			    }
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		//add columnname section
		ArrayList<String> columns = new ArrayList<String>();
		try {
			Statement statement = con.createStatement();
			//System.out.println(tableName);
			ResultSet set = statement.executeQuery("SELECT * FROM testdb.`" + tableName + "`");
			
			ResultSetMetaData md = set.getMetaData();
			for (int i=1; i<=md.getColumnCount(); i++)
			{
			    
			    if(hiddenSubstances.size() > 0){
			    	if(!hiddenSubstances.contains(tableName + "#" + md.getColumnLabel(i))){
						if(i != 1){
							message += "*";
						}
						message += md.getColumnLabel(i);
				    	columns.add(md.getColumnLabel(i));
			    	}
			    }else{
					if(i != 1){
						message += "*";
					}
					message += md.getColumnLabel(i);
			    	columns.add(md.getColumnLabel(i));
			    }
			}

			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//add tabledata section
		try {
			Statement statement = con.createStatement();
			ResultSet set = statement.executeQuery("SELECT * FROM testdb.`" + tableName + "`");
			
			while(set.next()){
				
				message += "&&&";
				
				for(int i = 0; i < columns.size(); i++){
					if(i != 0){
						message += "*";
					}
					message += set.getString(columns.get(i));
				}
				
			}

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return message;
	}

	public String getUsers() {
		String message = "!updateExistingUsers";
		
		try {
			Statement statement = con.createStatement();
			ResultSet set = statement.executeQuery("SELECT * FROM testdb.userdata");
			
			while(set.next()){
				
				message += "*" + set.getString("username");
				
			}

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return message;
	}

	public void changeUserData(String[] userChangeDataArray) {
		
		System.out.println("userdatachange triggered!");
		
		try {
			Statement statement = con.createStatement();
		
			String sql = "UPDATE testdb.userdata " +
						 "SET " + userChangeDataArray[1] +
						 " = " + "'" + userChangeDataArray[3] + "'" +
						 " WHERE " + "username" +
						 " = " + "'" +userChangeDataArray[2] + "'"; 
		
			int result = statement.executeUpdate(sql);
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void deleteUserData(String username){
		
		System.out.println("userdatadelete triggered!");
		
		try {
			Statement statement = con.createStatement();
		
			String sql = "DELETE FROM testdb.userdata " +
						 "WHERE username = '" + username + "'";
		
			int result = statement.executeUpdate(sql);
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
    public boolean addTableToBegrundung(String tablename){
    	boolean accepted = false;
    	
		try {
			Statement statement = con.createStatement();
		
			String sql = "ALTER TABLE `testdb`.`begrunddata` ADD COLUMN `" + tablename + "` VARCHAR(255) NULL DEFAULT NULL;";
		
			int result = statement.executeUpdate(sql);
			
			accepted = true;
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return accepted;
    }
    
    public boolean removeTableFromBegrundung(String tablename){
    	boolean accepted = false;
    	
		try {
			Statement statement = con.createStatement();
		
			String sql = "ALTER TABLE `testdb`.`begrunddata` DROP COLUMN `" + tablename + "`;";
		
			int result = statement.executeUpdate(sql);
			
			accepted = true;
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return accepted;
    }
            
    public boolean addTableBegrundung(String tablename, String begrundung){
    	boolean accepted = false;
    	
		try {
			Statement statement = con.createStatement();
			
			
			//TODO begrundung sql #etc sicher machen
		    String sql = "INSERT INTO testdb.begrunddata(`" + tablename + "`) " +
	                     "VALUES ('" + begrundung + "+')"; 
			
			int result = statement.executeUpdate(sql);
			
			accepted = true;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return accepted;
    }
    
    public boolean changeTableBegrundungActivity(String tablename, String begrundung){
    	boolean accepted = false;
    	
    	String begrundungNew = begrundung.substring(0, begrundung.length()-1);
    	
    	if(begrundung.substring(begrundung.length()-1).equals("-")){
    		begrundungNew += "+";
    	}else if(begrundung.substring(begrundung.length()-1).equals("+")){
    		begrundungNew += "-";
    	}else{
    		System.out.println("ERROR IN LINE 961+ AT dbhandler.CHANGETABLEBEGRUNDACTIVITY()! INVALID ACTIVITY IDENTIFIER!");
    	}
    	
		try {
			Statement statement = con.createStatement();
		
			String sql = "UPDATE testdb.begrunddata " +
						 "SET " + "`" + tablename + "`" +
						 " = " + "'" + begrundungNew + "'" +
						 " WHERE " + "`" + tablename + "`" +
						 " = " + "'" + begrundung + "'"; 
		
			int result = statement.executeUpdate(sql);
			
		accepted = true;
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return accepted;
    }
    
    public String getBegrundungString(){
    	
    	String message = "!begrunddata&";
    	
    	//add columnname section
    			ArrayList<String> columns = new ArrayList<String>();
    			try {
    				Statement statement = con.createStatement();
    				//System.out.println(tableName);
    				ResultSet set = statement.executeQuery("SELECT * FROM testdb.`" + "begrunddata" + "`");
    				
    				ResultSetMetaData md = set.getMetaData();
    				for (int i=2; i<=md.getColumnCount(); i++)
    				{
    					if(i != 2){
    						message += "&";
    					}
    				    System.out.println(md.getColumnLabel(i));
    				    message += md.getColumnLabel(i);
    				    
    				    while(set.next()){
    				    	if(set.getString(md.getColumnLabel(i)) != null){
        				    	message += "=";
        				    	message += set.getString(md.getColumnLabel(i));
    				    	}
    				    }
    				    
    				    set.beforeFirst();
    				    
    				    columns.add(md.getColumnLabel(i));
    				}

    				
    			} catch (SQLException e) {
    				e.printStackTrace();
    			}
    	
    	return message;
    	
    }
    
    //for future reference but core
    public boolean addSubstance(String command){
    	
    	/*
    	 * data[0] = !substancadd
    	 * data[1] = tablename in folder#name format
    	 * data[2] = substance name
    	 * data[3] = after this substance
    	 */
    	String[] data = command.split(" ");
    	
    	boolean accepted = false;
    	
		try {
			Statement statement = con.createStatement();
		
			String sql = "ALTER TABLE `testdb`.`" + data[1] + "` add COLUMN `" + data[2] + "` VARCHAR(255) NULL DEFAULT ' ' " + "AFTER `" + data[3] + "`;";
			
			int result = statement.executeUpdate(sql);
			
			accepted = true;
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return accepted;
    }
    
    //core
    public boolean renameSubstance(String command){
    	
    	/*
    	 * data[0] = !substancerename
    	 * data[1] = tablename in folder#name format
    	 * data[2] = substance name
    	 * data[3] = requested new substance name
    	 */
    	String[] data = command.split(" ");
    	
    	boolean accepted = false;
    	
		try {
			Statement statement = con.createStatement();
		
			String sql = "ALTER TABLE `testdb`.`" + data[1] + "` CHANGE COLUMN `" + data[2] + "` `" + data[3] + "` VARCHAR(255) NULL DEFAULT NULL;";
		
			int result = statement.executeUpdate(sql);
			
			accepted = true;
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return accepted;
    	
    }
    
    //core - might replace -delete for good
    public boolean hideSubstance(String command){
    	
    	//when hiding substances, consider hide table implementation - extra table hiddenData?
    	
    	/*
    	 * data[0] = !substancehide
    	 * data[1] = tablename in folder#name format
    	 * data[2] = substance name
    	 */
    	String[] data = command.split(" ");
    	
    	boolean accepted = false;
    	
    	boolean isHidden = false;
    	
    	try {
			Statement statement = con.createStatement();
			//System.out.println(tableName);
			ResultSet set = statement.executeQuery("SELECT * FROM testdb.`" + "hiddendata" + "`");
		    while(set.next()){
		    	if(set.getString("substances") != null){
		    		if(set.getString("substances").equals(data[1] + "#" + data[2])){
		    			isHidden = true;
		    		}
		    	}
		    }

			
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	
    	//if substance isnt in hidden table
    	if(!isHidden){
    		try {
    			Statement statement = con.createStatement();
    		
    			String sql = "INSERT INTO testdb.hiddendata(`" + "substances" + "`) " + "VALUES ('" + data[1] + "#" + data[2] + "')"; 
    			
    			int result = statement.executeUpdate(sql);
    			
    			accepted = true;
    		
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    	}else{
    		try {
    			Statement statement = con.createStatement();
    		
    			String sql = "UPDATE testdb.hiddendata " +
    						 "SET " + "`" + "substances" + "`" +
    						 " = " +  "NULL"  +
    						 " WHERE " + "`" + "substances" + "`" +
    						 " = " + "'" + data[1] + "#" + data[2] + "'"; 
    			
    		
    			int result = statement.executeUpdate(sql);
    			
    		accepted = true;
    		
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    	}
    	
    	return accepted;
    	
    	//return successvar - bool or string?
    }
    
    public boolean hideTable(String command){
    	
    	//when hiding substances, consider hide table implementation - extra table hiddenData?
    	
    	/*
    	 * data[0] = !tablehide
    	 * data[1] = tablename in folder#name format
    	 */
    	String[] data = command.split(" ");
    	
    	boolean accepted = false;
    	
    	boolean isHidden = false;
    	
    	try {
			Statement statement = con.createStatement();
			//System.out.println(tableName);
			ResultSet set = statement.executeQuery("SELECT * FROM testdb.`" + "hiddendata" + "`");
		    while(set.next()){
		    	if(set.getString("tables") != null){
		    		if(set.getString("tables").equals(data[1])){
		    			isHidden = true;
		    		}
		    	}
		    }

			
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	
    	//if substance isnt in hidden table
    	if(!isHidden){
    		try {
    			Statement statement = con.createStatement();
    		
    			String sql = "INSERT INTO testdb.hiddendata(`" + "tables" + "`) " + "VALUES ('" + data[1] + "')"; 
    			
    			int result = statement.executeUpdate(sql);
    			
    			accepted = true;
    		
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    	}else{
    		try {
    			Statement statement = con.createStatement();
    		
    			String sql = "UPDATE testdb.hiddendata " +
    						 "SET " + "`" + "tables" + "`" +
    						 " = " +  "NULL"  +
    						 " WHERE " + "`" + "tables" + "`" +
    						 " = " + "'" + data[1] + "'"; 
    		
    			int result = statement.executeUpdate(sql);
    			
    		accepted = true;
    		
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    	}
    	
    	return accepted;
    	
    	//return successvar - bool or string?
    }
    
    //questionable to use at all. using hiding instead prevents possible dataloss on errors- wether human or technical.
    public void deleteSubstance(String command){
    	
    	
    	//return successvar - bool or sting?
    }
    
    //not core but essential feature
    public void moveSubstance(String command){
    	
    	/*
    	 * data[0] = !substancemove
    	 * data[1] = tablename in folder#name format
    	 * data[2] = substance name
    	 * data[3] = after this substance
    	 */
    	String[] data = command.split(" ");
    	
    	boolean accepted = false;
    	
    	boolean isHidden = false;
    	
    	try {
			Statement statement = con.createStatement();
			//System.out.println(tableName);
			int result = statement.executeUpdate("ALTER TABLE `testdb`.`" + data[1] + "` CHANGE COLUMN `" + data[2] + "` `" + data[2] + "` VARCHAR(255) NULL DEFAULT ' ' AFTER `" + data[3] + "`;");
			
			

			
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	//return bool true/false
    }

	public String createHiddenDataString() {
		String string = new String("!hiddenData");
		
		ArrayList<String> hiddenTables = new ArrayList<String>();
		ArrayList<String> hiddenSubstances = new ArrayList<String>();
		
		try {
			Statement statement = con.createStatement();
			//System.out.println(tableName);
			ResultSet set = statement.executeQuery("SELECT * FROM testdb.`" + "hiddendata" + "`");
		    while(set.next()){
		    	if(set.getString("substances") != null){
		    		hiddenSubstances.add(set.getString("substances"));
		    	}
		    }
		    set.beforeFirst();
		    while(set.next()){
		    	if(set.getString("tables") != null){
		    		hiddenTables.add(set.getString("tables"));
		    	}
		    }

			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		for(int i = 0; i < hiddenTables.size(); i++){
			string += " " + hiddenTables.get(i);
		}
		string += " %";
		
		for(int i = 0; i < hiddenSubstances.size(); i++){
			string += " " + hiddenSubstances.get(i);
		}
		
		return string;
	}
	
	public boolean moveTable(String command){
		boolean success = true;
		
		/*
		 * data[0] = !tableMove
		 * data[1] = tablename
		 * data[2] = aftertable
		 */
		String[] data = command.split(" ");
		
		//find ID of AFTERtable, =0 when aftertable=START
		
		int idAftertable = 0;
		
		if(data[2].equals("START")){
			
		}else{
			
			try {
				Statement statement = con.createStatement();
				//System.out.println(tableName);
				ResultSet set = statement.executeQuery("SELECT * FROM testdb.`" + "tabledata" + "`");
			    while(set.next()){
			    	if(set.getString(data[1].split("#")[0]) != null){
			    		
				    	if(set.getString(data[1].split("#")[0]).toLowerCase().equals(data[2].split("#")[1])){
				    		idAftertable = Integer.parseInt(set.getString("ID"));
				    	}
			    		
			    	}
			    }

				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		
		//ID+1 for all > aftertable,
		String sql = "UPDATE testdb.tabledata SET ID = ID + 1 where ID > " + idAftertable +" ORDER BY ID DESC;";
    	try {
			Statement statement = con.createStatement();
			//System.out.println(tableName);
			int result = statement.executeUpdate(sql);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
		//update old table cell with NULL
		
		String sql3 = "UPDATE `testdb`.`tabledata` SET `" + data[1].split("#")[0] + "`=NULL WHERE `" + data[1].split("#")[0] + "`='" + data[1].split("#")[1] + "';";
    	try {
			Statement statement = con.createStatement();
			//System.out.println(tableName);
			int result = statement.executeUpdate(sql3);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//insert table with ID = aftertable+1
		String sql2 = "INSERT INTO testdb.tabledata (ID, " + data[1].split("#")[0] + ") VALUES ('" + (idAftertable+1) + "', '" + data[1].split("#")[1] + "'); ";
    	try {
			Statement statement = con.createStatement();
			//System.out.println(tableName);
			int result = statement.executeUpdate(sql2);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
		return success;
	}
	
	/**
	 * @param String command - command will be splitted by regex "&" and to sue the following String array:
	 * 
	 * data[0] = !deleteLine
	 * data[1] = tablename
	 * data[2] = ID of line
	 * 
	 * @return boolean success - TRUE, if method executed without errors.
	 */
	public boolean deleteLine(String command){
		boolean success = true;
		
		String data[] = command.split("&");
		
		String sql2 = "DELETE FROM `testdb`.`" + data[1] +"` WHERE `ID`='" + data[2] + "';";
    	try {
			Statement statement = con.createStatement();
			int result = statement.executeUpdate(sql2);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return success;
	}
	
	/**
	 * @param String command - command will be splitted by regex "&" and to sue the following String array:
	 * 
	 * data[0] = !deleteLine
	 * data[1] = tablename
	 * data[2] = ID of line
	 * data[3] = begründung
	 * data[4] = requestinguser
	 * 
	 * @return boolean success - TRUE, if method executed without errors.
	 */
	public boolean requestDeleteLine(String command){
		boolean success = true;
		
		String data[] = command.split("&");
		
		String sql2 = "INSERT INTO testdb.deletedata (tablename, identification, begrundung, requestinguser) VALUES ('" + data[1] + "', '" + data[2] + "', '" + data[3] + "', '" + data[4] + "'); ";
    	try {
			Statement statement = con.createStatement();
			int result = statement.executeUpdate(sql2);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return success;
	}
	
	public boolean appendToHistory(String[] data){
		boolean success = true;
		
		
		
		return success;
	}

}