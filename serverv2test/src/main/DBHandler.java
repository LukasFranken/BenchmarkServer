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

public class DBHandler {
	
	static WinRegistry regedit = new WinRegistry();
	
	private int osType = 32;
	private String defaultPath = "jdbc:mysql://localhost:";
	private int defaultDBPort = 3306;
	private String defaultDBName = "testdb";
	private String maintenanceVarsDB = "?verifyServerCertificate=false&useSSL=true";
	private boolean wasInstanciatedThisSession = false;
	
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
				if(res.getString("TABLE_NAME").equals(tablename)){
				    exists = true;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return exists;
	}
	
	//Obsolete
	public boolean createTable(String tablename){
		
		//create a table file in database
		
		return true;
		
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
		
		System.out.println(postProcessDefPath);
		
		File tableDirectory = new File("C:" + File.separator + "Users" + File.separator + "Instinct" + File.separator + "BenchmarkingDatabase" + File.separator + "Tables");
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
		}
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
		
}
