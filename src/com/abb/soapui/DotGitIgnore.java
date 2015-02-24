package com.abb.soapui;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class DotGitIgnore {

	public static final String defaultGitIgnoreFilename = ".gitignore";
	
	private List<String> lines = new ArrayList<String>();
	private File gitignoreFile;
	private boolean initialized = false;
	
	public DotGitIgnore() {
		this(new File(defaultGitIgnoreFilename));
	}
	
	public DotGitIgnore(File gitignoreFile) {
		try {
			FileInputStream input =new FileInputStream(gitignoreFile);
			initialize(input);
		} 
		catch (FileNotFoundException e) {
			System.out.println("Cannot find .gitignore file: " + gitignoreFile.getAbsolutePath());
		}
		this.gitignoreFile = gitignoreFile;
	}
	
	public DotGitIgnore(InputStream input) {
		initialize(input);
	}
	
	private void initialize(InputStream input) {
		BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(input)));
		String line = null;
		try {
			while((line = r.readLine()) != null) {
				line = line.trim();
				if(!lines.contains(line)) {
					lines.add(line);
				}
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
		initialized = true;
	}
	
	public void processConfiguration(Configuration config) throws IOException {
		addLine("*" + config.getLocalSuffix());
		addLine(config.getPropertiesFile().getName());
		addLine(this.getGitignoreFilename());
	}
	
	public void addLine(String line) throws IOException {
		
		if(line ==  null)
			return;
		
		if(!line.startsWith("/"))
			line = "/" + line.trim();
		
		if(lines.contains(line))
			return;
		
		PrintWriter pw = new PrintWriter(new FileWriter(gitignoreFile, true));
		pw.println();
		pw.println(line);
		pw.flush();
		pw.close();
		
		lines.add(line.trim());
	}
	
	public boolean isInitialized() {
		return initialized;
	}

	public String getGitignoreFilename() {
		if(gitignoreFile == null)
			return defaultGitIgnoreFilename;
		return gitignoreFile.getName();
	}
}
