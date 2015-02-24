package com.abb.soapui;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;

public class Configuration {

	public static String defaultPropertiesFileName = "SoapUILocalizer.properties";
	private String rootDirectory;
	private LinkedHashSet<String> findList = new LinkedHashSet<String>();
	private LinkedHashSet<String> regexFindList = new LinkedHashSet<String>();
	private List<String> replaceList = new ArrayList<String>();
	private List<String> regexReplaceList = new ArrayList<String>();
	private String localSuffix;
	private Boolean overwriteExisting;
	private File propertiesFile;
	Properties props = new Properties();
	private boolean initialized = false;
	
	public Configuration() {
		this(new File(defaultPropertiesFileName));
	}
		
	public Configuration(File propertiesFile) {		
		FileInputStream input = null;
		try {
			input = new FileInputStream(propertiesFile);
			this.propertiesFile = propertiesFile;
		} 
		catch (FileNotFoundException e) {
			System.out.println("Cannot find configuration file: " + propertiesFile.getAbsolutePath());
		}
		initialize(input);
	}

	public Configuration(InputStream input) {
		initialize(input);
	}
	
	private void initialize(InputStream input) {
		try {
			props.load(input);
		} 
		catch (IOException e) {
			e.printStackTrace(System.out);
		}
		initialized = true;
	}
	
	public String getRootDirectory() {
		if(rootDirectory == null) {
			rootDirectory = props.getProperty("RootDirectory");
			if(rootDirectory != null) {
				if(!rootDirectory.endsWith(File.separator)) {
					rootDirectory += File.separator;
				}
			}
		}
		return rootDirectory;
	}
	public LinkedHashSet<String> getFindList() {
		if(findList.isEmpty()) {
			for(int i=1; i<=100; i++) {
				String prop = props.getProperty("find" + String.valueOf(i));
				if(prop != null) {
					prop = prop.trim();
					if(prop.length() > 0 && !findList.contains(prop))
						findList.add(prop.trim());
				}
			}
		}
		return findList;
	}
	public LinkedHashSet<String> getRegexFindList() {
		if(regexFindList.isEmpty()) {
			for(int i=1; i<=100; i++) {
				String prop = props.getProperty("findRegex" + String.valueOf(i));
				if(prop != null) {
					prop = prop.trim();
					if(prop.length() > 0 && !regexFindList.contains(prop))
						regexFindList.add(prop.trim());
				}
			}
		}
		return regexFindList;
	}
	public List<String> getReplaceList() {
		if(replaceList.isEmpty()) {
			for(int i=1; i<=100; i++) {
				String prop = props.getProperty("replace" + String.valueOf(i));
				if(prop != null) {
					prop = prop.trim();
					if(prop.length() > 0 && !replaceList.contains(prop))
						replaceList.add(prop.trim());
				}
			}
		}
		return replaceList;
	}
	public List<String> getRegexReplaceList() {
		if(regexReplaceList.isEmpty()) {
			for(int i=1; i<=100; i++) {
				String prop = props.getProperty("replaceRegex" + String.valueOf(i));
				if(prop != null) {
					prop = prop.trim();
					if(prop.length() > 0 && !regexReplaceList.contains(prop))
						regexReplaceList.add(prop.trim());
				}
			}
		}
		return regexReplaceList;
	}
	public String getLocalSuffix() {
		if(localSuffix == null) {
			localSuffix = props.getProperty("localSuffix");
			if(localSuffix != null)
				localSuffix = localSuffix.trim();
		}
		return localSuffix;
	}
	public boolean isOverwriteExisting() {
		if(overwriteExisting == null) {
			String prop = props.getProperty("overwriteExisting");
			if(prop != null)
				overwriteExisting = prop.trim().matches("(?i)(on)|(yes)|(true)");
		}
		return overwriteExisting;
	}
	
	public boolean isInitialized() {
		return initialized;
	}

	public File getPropertiesFile() {
		return propertiesFile;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Configuration [")
				.append("\r\n  getRootDirectory()=").append(getRootDirectory())
				.append("\r\n  getLocalSuffix()=").append(getLocalSuffix())
				.append("\r\n  isOverwriteExisting()=").append(isOverwriteExisting())
				.append("\r\n  getFindList()=").append(Arrays.toString(getFindList().toArray()))
				.append("\r\n  getRegexFindList()=").append(Arrays.toString(getRegexFindList().toArray()))
				.append("\r\n  getReplaceList()=").append(Arrays.toString(getReplaceList().toArray()))
				.append("\r\n  getRegexReplaceList()=").append(Arrays.toString(getRegexReplaceList().toArray()))
				.append("\r\n]");
		return builder.toString();
	}

	public static void main(String[] args) throws IOException {
		
		String s = ""
				+ "RootDirectory = . \r\n"
				+ "localSuffix = _localhost \r\n"
				+ "overwriteExisting = true \r\n"
				+ ""
				+ "find1 = apples \r\n"
				+ "find2 = oranges \r\n"
				+ "find3 = pears \r\n"
				+ ""
				+ "replace1 = bannanas \r\n"
				+ "replace2 = grapes \r\n"
				+ "replace3 = mangos \r\n"
				+ ""
				+ "findRegex1 = \\d+ \r\n"
				+ "findRegex2 = \\D+ \r\n"
				+ "findRegex3 = (\\d{2})(\\D{2}) \r\n"
				+ ""
				+ "regexReplace1 = hello! \r\n"
				+ "regexReplace2 = goodbye! \r\n"
				+ "regexReplace3 = $2$1";
		
		ByteArrayInputStream input = new ByteArrayInputStream(s.getBytes());
		Configuration config = new Configuration(input);
		System.out.println(config);
	}
}
