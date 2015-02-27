package com.abb.soapui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SoapUIProjectFile {

	private Configuration config;
	private File projectFile;
	private StringBuilder builder = new StringBuilder();
	
	public SoapUIProjectFile(Configuration config, File projectFile) throws IOException {
		this.config = config;
		this.projectFile = projectFile;
	}
	
	/**
	 * Iterate over all files contained in the root directory for soapui project files pulled from git.
	 * Each qualifying file will have a new "local" file produced based on its content.
	 * 
	 * @throws IOException
	 */
	public static void processFiles() throws IOException {
		processFiles(null);
	}
	
	public static void processFiles(String propertiesPathname) throws IOException {
		
		// Validate the root directory specified
		if(propertiesPathname == null) {
			propertiesPathname = Configuration.defaultPropertiesFileName;
		}
		File propertiesFile = new File(propertiesPathname);
		if(!propertiesFile.isFile()) {
			System.out.println("Cannot find properties file: " + propertiesPathname);
			return;
		}
		
		// Get the Configuration instance
		final Configuration config;
		config = new Configuration(propertiesFile);
		if(!config.isInitialized()) {
			System.out.println("Cannot initialize configuration from properties file. Cancelling process!");
			return;
		}
		
		// Get the .gitignore file wrapper class instance
		final DotGitIgnore dotGitIgnore;
		dotGitIgnore = new DotGitIgnore(new File(config.getRootDirectory() + DotGitIgnore.defaultGitIgnoreFilename));
		if(!dotGitIgnore.isInitialized()) {
			System.out.println("Cannot initialize .gitignore file. Cancelling process!");
			return;
		}
		dotGitIgnore.processConfiguration(config);

		File dir = new File(config.getRootDirectory());
		
		if(!dir.isDirectory()) {
			System.out.println("Root directory cannot be found: " + config.getRootDirectory());
			return;
		}
		
		if(!(new File(config.getTargetDirectory())).isDirectory() && !isEmpty(config.getTargetDirectory())) {
			System.out.println("Target directory cannot be found: " + config.getTargetDirectory());
			return;
		}
		
		if(dir.isDirectory()) {
			File[] filesToProcess = dir.listFiles(new FileFilter(){
				@Override public boolean accept(File f) {
					if(f.getName().endsWith(".xml")) {
						if(f.getName().matches(".*?" + config.getLocalSuffix() + "\\d*\\.xml") == false) {
							try {
								if(isSoapUIProjectFile(f)) {
									return true;
								}
							} 
							catch (IOException e) {
								e.printStackTrace();
								return false;
							}
						}
					}
					return false;
				}
			});
			
			for(File f : filesToProcess) {
				
				SoapUIProjectFile proj = new SoapUIProjectFile(config, f);
				
				proj.readFile();
				
				File local = proj.getFileToWrite(config.getLocalSuffix());
				
				proj.writeFile(local);
			}
		}
		
	}
	
	/**
	 * Determine if the provided file is a soapui project file by checking the first 10 lines 
	 * for the soapui project element.
	 * 
	 * @param f
	 * @return
	 * @throws IOException
	 */
	private static boolean isSoapUIProjectFile(File f) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(f));
		String line = null;
		int lineCounter = 0;
		try {
			while((line = r.readLine()) != null) {
				if(lineCounter == 10)
					break;
				
				if(line.contains("<con:soapui-project")) {
					return true;
				}
				lineCounter ++;
			}
			return false;
		} 
		finally {
			if(r != null) {
				r.close();
			}
		}
	}				
	
	public void readFile() throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(projectFile));
		String line = null;
		try {
			while((line = r.readLine()) != null) {
				
				try {
					String[] findList = config.getFindList().toArray(new String[config.getFindList().size()]);
					for (int i = 0; i < findList.length; i++) {
						String find = findList[i];
						if(find.length() == 0)
							continue;
						String replacement = config.getReplaceList().get(i);
						if(replacement == null)
							continue;
						line = line.replaceAll(find, replacement);
					}
					
					String[] findRegexList = config.getRegexFindList().toArray(new String[config.getRegexFindList().size()]);
					for (int i = 0; i < findRegexList.length; i++) {
						String find = findRegexList[i];
						if(find.length() == 0)
							continue;
						if(config.getRegexReplaceList().size() < (i+1))
							continue;
						String replacement = config.getRegexReplaceList().get(i);
						if(replacement == null)
							continue;
						line = line.replaceAll(find, replacement);
					}
					
					builder.append(line).append("\r\n");
				} 
				catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		} 
		finally {
			if(r != null) {
				r.close();
			}
		}
	}
	
	/**
	 * If overwriting local files, return a file whose name is the same as the soapui project file with 
	 * the localSuffix appended to the end.
	 * Else, do not overwrite any existing "local" files, but keep adjusting the name by incrementing a numeric
	 * suffix at the end of the filename and checking until no matching file is found. This will be the
	 * name of the new "local" file.
	 */
	private File getFileToWrite(String localSuffix) {
		File local = null;
		String pathname = null;
		
		if(!isEmpty(config.getTargetDirectory())) {
			String targetDir = config.getTargetDirectory();
			if(!targetDir.endsWith(File.separator))
				targetDir += File.separator;			
			String[] parts = projectFile.getName().split("\\.xml");			
			pathname = targetDir + parts[0] + localSuffix + ".xml";
		}
		else {
			pathname = projectFile.getAbsolutePath();
			String[] parts = pathname.split("\\.xml");
			pathname = parts[0] + localSuffix + ".xml";
		}
		
		local = new File(pathname);
		
		if(config.isOverwriteExisting()) {
			return local;
		}
		else {
			while(true) {
				if(local.isFile()) {
					Integer index = null;
					String sIndex = localSuffix.substring(config.getLocalSuffix().length());
					if(sIndex != null && sIndex.length() > 0) {
						index = Integer.valueOf(sIndex);
						index++;
					}
					else {
						index = 2;
					}
					localSuffix = config.getLocalSuffix() + String.valueOf(index);
					return getFileToWrite(localSuffix);
				}
				else {
					return local;
				}
			}
		}
	}
	
	/**
	 * Create a new file and write the contents of the StringBuilder to it.
	 * The new file is the local version of the soapui project file and the StringBuilder contains
	 * its contents with all the necessary text replacements having been made.
	 * 
	 * @param f
	 */
	public void writeFile(File f) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(f);
			writer.write(builder.toString());
			writer.flush();
		} 
		catch (FileNotFoundException e) {
			System.out.println("No such file: " + f.getAbsolutePath());
		}
		catch (Exception e) {
			System.out.println("IOException for file: " + f.getAbsolutePath());
			e.printStackTrace(System.out);
		}
		finally {
			if(writer != null) {
				try {
					writer.close();
				} 
				catch (IOException e) {
					System.out.println("Cannot close file: " + f.getAbsolutePath());
					e.printStackTrace(System.out);
				}
			}
		}
	}
	
	public static boolean isEmpty(String s) {
		if(s == null)
			return true;
		if(String.valueOf(s).trim().length() == 0)
			return true;
		return false;
	}
	
	public static void main(String[] args) throws Exception {
		String propertiesFilePathName = null;
		
		if(args.length > 0) {
			propertiesFilePathName = args[0].trim();
		}
		
//		SoapUIProjectFile.processFiles(propertiesFilePathName);
		SoapUIProjectFile.processFiles("C:\\whennemuth\\documentation\\abb\\soapui\\SoapUILocalizer.properties");
	}
}
