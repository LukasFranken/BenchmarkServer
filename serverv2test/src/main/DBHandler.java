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

import main.WinRegistry;

import java.sql.*;
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
				createCatTable();
				returnMessage += "- Category Table not found. Created new one.";
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
	
	private void createCatTable(){
		
		try {
			Statement statement = con.createStatement();
			
		    String sql = "CREATE TABLE TABLEDATA " +
	                     "(ID INTEGER not NULL AUTO_INCREMENT, " +
	                     " folder VARCHAR(255), ";
		    
		    for(int i = 0; i <= 100; i++){
		    	sql += "subfolder"+i+" VARCHAR(255), ";
		    }
		    
		    sql += " PRIMARY KEY ( ID ))"; 
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
	
	public ArrayList<String> generateTableNameList(){
		ArrayList<String> tableNames = new ArrayList<String>();
		
		try {
			Statement statement = con.createStatement();
			//System.out.println(tableName);
			ResultSet set = statement.executeQuery("SELECT * FROM testdb.tabledata");
			
			String tablePointer = "";
			
			while(set.next()){
				for(int i = 0; i < 30; i++){
					if(set.getString("subfolder" + i) != null){
						tablePointer = set.getString("subfolder" + i);
						System.out.println("result set tablenamelistgen tableadd: " + set.getString("folder").toLowerCase() + "#" + tablePointer.toLowerCase());
						tableNames.add(set.getString("folder").toLowerCase() + "#" + tablePointer.toLowerCase());
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
		System.out.println("table exists: " + tableExists);
		
		String response = "";
		String finaltablename = subfoldername.toLowerCase() + "#" + tablename;
		
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
				
				while(res.next()){
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
				}
				
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
			createTable(tablename, subfoldername,  columns);
		}
		}else{
			createTable(tablename + "1", subfoldername,  columns);
			response += "- table existed! added substring identifier.";
		}
		
		return response;
		
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
			
			while(res.next()){
				directoryString += " * " + res.getString("folder");
				
				int i = 0;
				while(i < 30){
					System.out.println(res.getString("subfolder" + Integer.toString(i)));
					if(res.getString("subfolder" + Integer.toString(i)) != null){
						directoryString += " " + res.getString("subfolder" + Integer.toString(i));
						tableNames.add(res.getString("subfolder" + Integer.toString(i)));
					}
					i++;
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		/*File tableDirectory = new File("C:" + File.separator + "Users" + File.separator + "Instinct" + File.separator + "BenchmarkingDatabase" + File.separator + "Tables");
		String[] folderNames = tableDirectory.list();
		int folderAmount = folderNames.length;
		String tempString;
		File pointerFile;
		
		for(int i = 0; i < folderNames.length; i++){
			tempString = new String();
			tempString += " * " + folderNames[i];
			pointerFile = new File("C:" + File.separator + "Users" + File.separator + "Instinct" + File.separator + "BenchmarkingDatabase" + File.separator + "Tables" + File.separator + folderNames[i]);
			for(int i2 = 0; i2 < pointerFile.list().length; i2++){
				tempString += " " + pointerFile.list()[i2];
			}
			System.out.println(tempString);
			directoryString += tempString;
		}*/
		
		directoryString += " **";
		
		System.out.println(directoryString);
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
			
			while(res.next()){
				
				if(res.getString("folder") != null){
					if(res.getString("folder").equals(name)){
						exists = true;
						break;
					}
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(!exists){
			try {
				Statement statement = con.createStatement();
			
				String sql = "INSERT INTO testdb.tabledata(folder) " +
							 "VALUES ('" + name + "')"; 
			
				int result = statement.executeUpdate(sql);
			
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return exists;
		
	}

	public void appendTableLine(String[] tableLineData, String name) {
		
		//add columnname section
		ArrayList<String> columns = new ArrayList<String>();
		try {
			Statement statement = con.createStatement();
			ResultSet set = statement.executeQuery("SELECT * FROM testdb.`" + tableLineData[1] + "`");
			
			ResultSetMetaData md = set.getMetaData();
			for (int i=2; i<=md.getColumnCount(); i++)
			{
			    System.out.println("columnLabel: " + md.getColumnLabel(i));
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

			for(int i = 0; i < columns.size(); i++){
				if(columns.get(i).equals("Begründung")){
					columns.remove(i);
				}
			}
			
			System.out.println("columns new size after removal: " + columns.size());
			
			String stoffString = "";
			for(int i = 0; i < columns.size(); i++){
				if(columns.get(i).equals(tableLineData[2])){
					stoffString += tableLineData[3] + "','";
				}else{
					stoffString += " ','";
				}
			}
			
			System.out.println("stoffstring final: " + stoffString);
			
		    String sql = "INSERT INTO testdb." + "`" + tableLineData[1] + "`" + columnLabelSqlString +
	                     "VALUES ('" + tableLineData[4] + "','" + tableLineData[5] + "','" + name + "','" + stoffString + tableLineData[6] + "')"; 
			
		    System.out.println("final SQL apenndline: " + sql);
		    
			int result = statement.executeUpdate(sql);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	public String generateTableDataString(String tableName) {
		
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
		
		//add columnname section
		ArrayList<String> columns = new ArrayList<String>();
		try {
			Statement statement = con.createStatement();
			//System.out.println(tableName);
			ResultSet set = statement.executeQuery("SELECT * FROM testdb.`" + tableName + "`");
			
			ResultSetMetaData md = set.getMetaData();
			for (int i=2; i<=md.getColumnCount(); i++)
			{
				if(i != 2){
					message += "*";
				}
			    System.out.println(md.getColumnLabel(i));
			    message += md.getColumnLabel(i);
			    columns.add(md.getColumnLabel(i));
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
		
		System.out.println("finalmsg tabledata: " + message);
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
		
		System.out.println("userdatadeletition triggered!");
		
		try {
			Statement statement = con.createStatement();
		
			String sql = "DELETE FROM testdb.userdata " +
						 "WHERE username = '" + username + "'";
		
			int result = statement.executeUpdate(sql);
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
		
}
