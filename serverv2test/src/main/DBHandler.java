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
import java.util.ArrayList;

import main.WinRegistry;

import java.sql.*;

public class DBHandler {
	
	static WinRegistry regedit = new WinRegistry();
	
	FileReader fr = null;
	private boolean doesExist = false;
	File Ofile = new File(System.getProperty("user.home") + File.separator + "benchInit.bat");
	File Ofile2 = new File(System.getProperty("user.home") + File.separator + "benchRead.bat");
	File OfileD = new File(System.getProperty("user.home") + File.separator + "filename.txt");
	File OfileTemp = new File(System.getProperty("user.home") + File.separator + "tempfile.bat");
	File OfileTemp2 = new File(System.getProperty("user.home") + File.separator + "tempfile2.bat");
	File OfileTempPortBat = new File(System.getProperty("user.home") + File.separator + "porttempfile.bat");
	File OfileTempPort = new File(System.getProperty("user.home") + File.separator + "porttemp.txt");

	private int decodeKey = 44651;
	private int[] decodeFragment = { 6, 7, 2, 5, 8, 9, 0, 6, 7, 5, 6, 2, 0, 3, 3, 6, 5, 1, 6, 7 };
	
	private int osType = 32;
	private String defaultPath = "jdbc:mysql://localhost:";
	private int defaultDBPort = 3306;
	private String defaultDBName = "benchmarking";
	
	private boolean repeatRequested = false;
	
	static boolean noRegData = false;
	static boolean noRegDataPort = false;
	static boolean gotData = false;
	static String postProcessDefPath = "default";
	static int firstRun = 0;
	static int globalPort = 6789;
	static boolean isConnectedToDB = false;
	static Connection con;
	
	public DBHandler(){
		//for future reference
	}
	
	private void instanciate(String requestedPath){
		
		//detect 64-bit systems for future optimizing purposes
		
		if(System.getProperty("os.arch").equals("amd64")){
			
			osType = 64;
			
		}
		
		//create batchfiles, that convert registry keys related to this program
		//into a .txt file, that can be read by the buffered filereader.
		//If there is no registry info, create firstrun default registry data.
		
		try {
			try {
				System.out.println("Instanciation request initiated.");
				doesExist = !Ofile.createNewFile();
				if(doesExist){
					System.out.println("Instanciation not required. Savefile localized.");
				}else{
					System.out.println("Instanciation required and Completed!");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			fr = new FileReader(Ofile);
		} catch (FileNotFoundException e) {
			System.out.println("Instanciation of Savefile failed!");
			doesExist = false;
			e.printStackTrace();
		}
		System.out.println("Instanciation Request Successfully executed.");
		if(!doesExist){
			System.out.println("Writing default values to new Savefile Instance.");
			writeInitBatch(osType, requestedPath);
		}
		
			System.out.println("Reading default values from Savefile Instance.");
			getInitBatch();
		
	}
	
	private void writeInitBatch(int osArch, String defPath){
		
		//function to create the default program initiation batchfile to write to registry data.
		
	try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream(Ofile), "utf-8"))) {
	   writer.write("@echo off");
	   ((BufferedWriter) writer).newLine();
	   writer.write("REG ADD HKCU\\SOFTWARE /v BenchmarkInitialized /t REG_SZ /d 1");
	   ((BufferedWriter) writer).newLine();
	   writer.write("REG ADD HKCU\\SOFTWARE /v BenchmarkDefDBPath /t REG_SZ /d " + defPath);
	   ((BufferedWriter) writer).newLine();
	   writer.write("exit");
	} catch (IOException e) {
		e.printStackTrace();
	}
	
	try {
		Runtime.getRuntime().exec("cmd /c start C:\\Users\\%USERNAME%\\benchInit.bat");
	} catch (IOException e) {
		e.printStackTrace();
	}
	
	}
	
	private void getInitBatch(){
		
		//function to create the batchfile to read out system defaults.
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream(Ofile2), "utf-8"))) {
			writer.write("FOR /F \"skip=2 tokens=2,*\" %%A IN ('reg.exe query \"HKCU\\SOFTWARE\" /v \"BenchmarkInitialized\"') DO set \"DFMT=%%B\"");
			((BufferedWriter) writer).newLine();
			writer.write("ECHO InitBool >C:\\Users\\%USERNAME%\\filename.txt");
			((BufferedWriter) writer).newLine();
			writer.write("ECHO %DFMT% >>C:\\Users\\%USERNAME%\\filename.txt");
			((BufferedWriter) writer).newLine();
			writer.write("FOR /F \"skip=2 tokens=2,*\" %%A IN ('reg.exe query \"HKCU\\SOFTWARE\" /v \"BenchmarkDefDBPath\"') DO set \"DFMG=%%B\"");
			((BufferedWriter) writer).newLine();
			writer.write("ECHO DefPath >>C:\\Users\\%USERNAME%\\filename.txt");
			((BufferedWriter) writer).newLine();
			writer.write("ECHO %DFMG%>>C:\\Users\\%USERNAME%\\filename.txt");
			((BufferedWriter) writer).newLine();
			writer.write("FOR /F \"skip=2 tokens=2,*\" %%A IN ('reg.exe query \"HKCU\\SOFTWARE\" /v \"DefaultPort\"') DO set \"DFMG=%%B\"");
			((BufferedWriter) writer).newLine();
			writer.write("ECHO DefPort >>C:\\Users\\%USERNAME%\\filename.txt");
			((BufferedWriter) writer).newLine();
			writer.write("ECHO %DFMG%>>C:\\Users\\%USERNAME%\\filename.txt");
			((BufferedWriter) writer).newLine();
			writer.write("exit");

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println("triggered last runtime execution");
			Runtime.getRuntime().exec("cmd /c start C:\\Users\\%USERNAME%\\benchRead.bat");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void getData(){
		
		//function to read out the .txt file created previously in getInitBatch().
		
		boolean repeatReader = false;
		
		if(OfileD.exists()){

		try {
			fr = new FileReader(OfileD);
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't load file saves!");
			e.printStackTrace();
		}
		
		BufferedReader reader = new BufferedReader(fr);
		String line = "first";
		String[] templine;
		
		while (true) {
			
			System.out.println("pre:" + line);
			
			if(line == null){
				repeatReader = true;
			}
			
			if(repeatReader){
				
				try {
					fr = new FileReader(OfileD);
				} catch (FileNotFoundException e) {
					System.err.println("Couldn't load file saves!");
					e.printStackTrace();
				}
				
				reader = new BufferedReader(fr);
				
				repeatReader = false;
				
			}
			
			if(!repeatReader){
			
			if(line.startsWith("InitBool"))
			{
				try {
					line = reader.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(line != null){
				if(line.startsWith("ECHO")){
					noRegData = true;
					break;
				}else{
				System.out.println("post:" + line);
				templine = line.split(" ");
				firstRun = Integer.parseInt(templine[0]);
				try {
					reader.mark(500);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
				}
				}else{
					repeatReader = true;
				}
			}else{
				try {
					line = reader.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	
	  }
		do{
			
			try {
				fr = new FileReader(OfileD);
			} catch (FileNotFoundException e) {
				System.err.println("Couldn't load file saves!");
				e.printStackTrace();
			}
			
		if(!noRegData){
		while (true) {
			System.out.println("2pre:" + line);
			
			if(line == null){
				idle(1000);
				
				try {
					fr = new FileReader(OfileD);
				} catch (FileNotFoundException e) {
					System.err.println("Couldn't load file saves!");
					e.printStackTrace();
				}
				
				reader = new BufferedReader(fr);
				
				try {
					line = reader.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				repeatRequested = true;
				break;
			}

			if(line.startsWith("DefPath"))
			{
				try {
					line = reader.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("2post:" + line);
				postProcessDefPath = line;
				repeatRequested = false;
				gotData = true;
				break;
			}else{
				try {
					line = reader.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
	
		}
	}else{
			
		 gotData = false;
	}
		}while(repeatRequested);
		
		}
			
	}
	
	public void callData(){
		getData();
	}
	
	private void connectToDB(String path){
		try {
			con = DriverManager.getConnection(path, "root" , "Spahiton1");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void initData(String path){
		instanciate(path);
	}
	
	public void setDefaultDBPath(String path){
		
		// function to set the default Database path and write it to registry for longtime purposes
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream(OfileTemp), "utf-8"))) {
	   writer.write("REG ADD HKCU\\SOFTWARE /f /v BenchmarkDefDBPath /t REG_SZ /d " + path);
	   ((BufferedWriter) writer).newLine();
	   writer.write("exit");
	   
	} catch (IOException e) {
		e.printStackTrace();
	}
		
		try {
			System.out.println("triggered tempfile execution");
			Runtime.getRuntime().exec("cmd /c start C:\\Users\\%USERNAME%\\tempfile.bat");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void createDBDirectory(String path){
		
		//function to create the database directory
		
		boolean success = (new File(path + "\\BenchmarkingDatabase\\Tables")).mkdirs();
		success = (new File(path + "\\BenchmarkingDatabase\\UserData")).mkdirs();
		success = (new File(path + "\\BenchmarkingDatabase\\Backup")).mkdirs();
		try {
			success = new File(path + "\\BenchmarkingDatabase\\ChangeLog.txt").createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void cleanUp(){
		
		// cleaning up temporary batch and textfiles created at DB initiation.
		
		Boolean isDeleted1 = Ofile.delete();
		Boolean isDeleted2 = Ofile2.delete();
		Boolean isDeleted3 = OfileD.delete();
		Boolean isDeleted4 = OfileTemp.delete();
		Boolean isDeleted5 = OfileTemp2.delete();
		Boolean isDeleted6 = OfileTempPortBat.delete();
	}
	
	public void setPort(int port){
		
		//write custom port to listen on in registry.
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream(OfileTemp2), "utf-8"))) {
	   writer.write("@echo off");
	   ((BufferedWriter) writer).newLine();
	   writer.write("REG ADD HKCU\\SOFTWARE /f /v DefaultPort /t REG_SZ /d " + port);
	   ((BufferedWriter) writer).newLine();
	   writer.write("exit");
	} catch (IOException e) {
		e.printStackTrace();
	}
		
		try {
			System.out.println("triggered last tempfile2port execution");
			Runtime.getRuntime().exec("cmd /c start C:\\Users\\%USERNAME%\\tempfile2.bat");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void getPortBatch(){
		
		//create batchfile to convert registry data about the custom port into a readable .txt file
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream(OfileTempPortBat), "utf-8"))) {
			writer.write("FOR /F \"skip=2 tokens=2,*\" %%A IN ('reg.exe query \"HKCU\\SOFTWARE\" /v \"DefaultPort\"') DO set \"DFMT=%%B\"");
			((BufferedWriter) writer).newLine();
			writer.write("ECHO Port >C:\\Users\\%USERNAME%\\porttemp.txt");
			((BufferedWriter) writer).newLine();
			writer.write("ECHO %DFMT% >>C:\\Users\\%USERNAME%\\porttemp.txt");
			((BufferedWriter) writer).newLine();
			writer.write("exit");

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println("triggered last runtime execution");
			Runtime.getRuntime().exec("cmd /c start C:\\Users\\%USERNAME%\\porttempfile.bat");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void getPort(){
		
		//read out .txt file created in getPortBatch().
		
		getPortBatch();
		
		System.out.println("getport triggered and throught getbatch");
		
		
		
		while(!OfileTempPort.exists()){
			System.out.println("waiting for portfile creation...");
		}
		
		BufferedReader reader;
		String line = "first";
		String[] templine;

		do{
			
		try {
			fr = new FileReader(OfileTempPort);
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't load file saves!");
			e.printStackTrace();
		}
		
		reader = new BufferedReader(fr);
		while(true){
			try {
				line = reader.readLine();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(line == null){
				idle(500);
				
				repeatRequested = true;
				break;
			}
			
			System.out.println("pre3:" + line);
			if(line.startsWith("Port"))
			{
				try {
					line = reader.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(line == null){
					reader = new BufferedReader(fr);
					try {
						line = reader.readLine();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				if(line.startsWith("ECHO")){
					System.out.println("portbat called echo");
					noRegDataPort = true;
					repeatRequested = false;
					break;
				}else{
				System.out.println("post3:" + line);
				templine = line.split(" ");
				globalPort = Integer.parseInt(templine[0]);
				repeatRequested=false;
				break;
				}
			}else{
				try {
					line = reader.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	
	  }while(repeatRequested);

		

	}
	
	public boolean checkTable(String tablename){
		
		//check, if a tablename exists already
		
		File requestedTable = new File(postProcessDefPath + File.separator + "BenchmarkingDatabase" + File.separator + "Tables" + File.separator + tablename + ".txt");
		
		return requestedTable.exists();
	}
	
	//Obsolete
	public boolean createTable(String tablename){
		
		//create a table file in database
		
		boolean created = false;
		
		File requestedTable = new File(postProcessDefPath + File.separator + "BenchmarkingDatabase" + File.separator + "Tables" + File.separator + tablename + ".txt");
		
		try{
			created = requestedTable.createNewFile();
		}catch(IOException ioexception) {
			ioexception.printStackTrace();
		}
		
		if(created){
			return true;
		}else{
			return false;
		}
		
		
	}
	
	//Obsolete
	public boolean overwriteTable(String tablename){
		
		//create new file and delete already existing table with same name.
		//CAUTION HERE WHEN USING! for data safety purposes this function will be changed and might be replaced.
		
		boolean created = false;
		
		File requestedTable = new File(postProcessDefPath + File.separator + "BenchmarkingDatabase" + File.separator + "Tables" + File.separator + tablename + ".txt");
		
		requestedTable.delete();
		
		try{
			created = requestedTable.createNewFile();
		}catch(IOException ioexception) {
			ioexception.printStackTrace();
		}
		
		if(created){
			return true;
		}else{
			return false;
		}
		
		
	}
	
	public void idle(int mstime){
		
		//poor temporary function to idle the thread. if needed.
		
		long timeStart = System.currentTimeMillis();
		
		while(true){
			if (timeStart - System.currentTimeMillis() <= - mstime){
				break;
			}
		}
		
	}
	
	public boolean createAdmin(String name, int[] codedPass){
		
		//create an admin file that will represent the userdata related to an admin account
		
		boolean created = false;
		
		File requestedUser = new File(postProcessDefPath + File.separator + "BenchmarkingDatabase" + File.separator + "UserData" + File.separator + name + ".txt");
		
		try{
			created = requestedUser.createNewFile();
		}catch(IOException ioexception) {
			ioexception.printStackTrace();
		}
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream(requestedUser), "utf-8"))) {
			writer.write(name);
			for(int i = 0; i < codedPass.length; i++){
				writer.write(" " + Integer.toString(codedPass[i]));
			}


		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(created){
			return true;
		}else{
			return false;
		}
		

	}
	
	public boolean createUser(String name, String pass){
		
		//create a User file that will represent the userdata related to a user account
		
		boolean created = false;
		
		File requestedUser = new File(postProcessDefPath + File.separator + "BenchmarkingDatabase" + File.separator + "UserData" + File.separator + name + ".txt");
		
		try{
			created = requestedUser.createNewFile();
		}catch(IOException ioexception) {
			ioexception.printStackTrace();
		}
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream(requestedUser), "utf-8"))) {
			writer.write(name);

			writer.write(" " + pass);



		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(created){
			return true;
		}else{
			return false;
		}
		

	}
	
	public boolean checkUser(String username){
		
		//check if a userfile exists for given name
		
		File requestedUser = new File(postProcessDefPath + File.separator + "BenchmarkingDatabase" + File.separator + "UserData" + File.separator + username + ".txt");
		
		return requestedUser.exists();
	}
	
	public boolean loginAdmin(String username, int[] codedpass){
		
		//login check routine for a decoded admin account.
		
		boolean accepted = false;
			
		File requestedUser = new File(postProcessDefPath + File.separator + "BenchmarkingDatabase" + File.separator + "UserData" + File.separator + username + ".txt");
			
		try {
			fr = new FileReader(requestedUser);
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't load file saves!");
			e.printStackTrace();
		}
		
		BufferedReader reader = new BufferedReader(fr);
		String line = "first";
		String userDataRaw = new String();
			
		try {
			userDataRaw = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(userDataRaw);
		
		String[] userDataExtractArray = userDataRaw.split(" ");

		if(Character.isDigit(userDataExtractArray[1].charAt(0))){
		
			int[] userDataPasswordArray = new int[userDataExtractArray.length-1];
		
			for(int i = 0; i < userDataPasswordArray.length; i++){
				userDataPasswordArray[i] = Integer.parseInt(userDataExtractArray[i+1]);
			}
		
			System.out.println(userDataPasswordArray.length);
		
			//password conversion
			String receivedPassword = decode(codedpass);
			String databasePassword = decode(userDataPasswordArray);
		
			System.out.println("DB: " + databasePassword + " received: " + receivedPassword);
		
			if(databasePassword.equals(receivedPassword)){
				accepted = true;
			}
		
		}
		
		return accepted;
	}
	
	//Obsolete
	public boolean loginUser(String name, String password){
		
		//login routine for an unencoded user account.
		
		boolean accepted = false;
		
		File requestedUser = new File(postProcessDefPath + File.separator + "BenchmarkingDatabase" + File.separator + "UserData" + File.separator + name + ".txt");
		
		try {
			fr = new FileReader(requestedUser);
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't load file saves!");
			e.printStackTrace();
		}
		
		BufferedReader reader = new BufferedReader(fr);
		String line = "first";
		String userDataRaw = new String();
			
		try {
			userDataRaw = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(userDataRaw);
		
		String[] userDataExtractArray = userDataRaw.split(" ");
		
		System.out.println("DB: " + userDataExtractArray[1] + " received: " + password);
		
		if(userDataExtractArray[1].equals(password)){
			accepted = true;
		}
		
		return accepted;
	}
	
	private String decode(int code[]){
		
		//function to decode the array into  a readable password string. obsolete and modifiable, will be replaced in future.
		
		String decodedString;
		ArrayList<Integer> decodedInt = new ArrayList<Integer>();
		ArrayList<Character> decodedCharList = new ArrayList<Character>();
		//begin decoding - decode int array to readable charvalue array
		for(int i = 0; i < code.length; i++){
			decodedInt.add(code[i] - decodeKey - decodeFragment[i]);
			System.out.println(i + ": " + decodedInt.get(i));
		}
		//convert charvalue array stored in int array to char array
		
		for(int i = 0; i < decodedInt.size(); i++){
			decodedCharList.add((char) (int)decodedInt.get(i));
		}
		
		Character[] decodedCharacter = new Character[decodedCharList.size()];
		decodedCharList.toArray(decodedCharacter);
		
		char[] decodedChar = new char[decodedCharacter.length];
		
		for(int i = 0; i < decodedCharacter.length; i++){
			decodedChar[i] = (char)decodedCharacter[i];
		}
		
		decodedString = new String(decodedChar);
		
		return decodedString;
	}
	
	private int[] encode(String toCode){
		
		//like decode, obsolete temporary function.
		
		char[] toCodeChars = toCode.toCharArray();
		int[] encoded = new int[toCodeChars.length];
		
		for(int i = 0; i < toCodeChars.length; i++){
			encoded[i] = (int)toCodeChars[i] + decodeKey + decodeFragment[i];
		}
		
		return encoded;
	}
	
	//Obsolete
	public String createDirectoryString(){
		String directoryString = "";
		
		System.out.println(postProcessDefPath);
		
		File tableDirectory = new File(postProcessDefPath + File.separator + "BenchmarkingDatabase" + File.separator + "Tables");
		String[] folderNames = tableDirectory.list();
		int folderAmount = folderNames.length;
		String tempString;
		File pointerFile;
		
		for(int i = 0; i < folderNames.length; i++){
			tempString = new String();
			tempString += " * " + folderNames[i];
			pointerFile = new File(postProcessDefPath + File.separator + "BenchmarkingDatabase" + File.separator + "Tables" + File.separator + folderNames[i]);
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
		
}
