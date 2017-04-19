package main;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class LabeledObjectOutputStream extends ObjectOutputStream{
	
	String username = "notdefined";
	String pipeType = "notdefined";

	public LabeledObjectOutputStream(OutputStream out) throws IOException {
		super(out);
		// TODO Auto-generated constructor stub
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPipeType() {
		return pipeType;
	}

	public void setPipeType(String pipeType) {
		this.pipeType = pipeType;
	}


}
