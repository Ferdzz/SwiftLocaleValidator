package me.ferdz.swiftlocalevalidator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class SwiftLocaleValidator {

	public static final String DIRECTORY_OPTION_KEY = "directory",
							   LOCALE_OPTION_KEY = "locale";
	
	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		
		Option projectFolderOption = new Option(DIRECTORY_OPTION_KEY, true, "the project directory to parse");
		projectFolderOption.setType(File.class);
		projectFolderOption.setRequired(true);
		projectFolderOption.setArgs(1);
		projectFolderOption.setArgName("path");
		options.addOption(projectFolderOption);
		
		Option localeFileOption = new Option(LOCALE_OPTION_KEY, true, "the locale file to parse with");
		localeFileOption.setType(File.class);
		localeFileOption.setRequired(true);
		localeFileOption.setArgs(1);
		localeFileOption.setArgName("path");
		options.addOption(localeFileOption);

		try {
			CommandLine commandLine = parser.parse(options, args);
			File projectFile = (File) commandLine.getParsedOptionValue(DIRECTORY_OPTION_KEY);
			File localeFile = (File) commandLine.getParsedOptionValue(LOCALE_OPTION_KEY);
			SwiftLocaleValidator validator = new SwiftLocaleValidator();
			validator.validateLocale(projectFile, localeFile);
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("slv", options);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private final HashMap<String, String> localeMap;
	private final HashSet<String> codeStrings;
	
	public SwiftLocaleValidator() {
		this.localeMap = new HashMap<>();
		this.codeStrings = new HashSet<>();
	}
	
	public void validateLocale(File projectFile, File localeFile) throws IOException {
		System.out.println("Parsing the project...\n");
		this.readLocaleFile(localeFile);
		this.readAllFiles(projectFile);
		this.validateStrings();
		System.out.println("\nDone parsing the project!");
	}
	
	private void readLocaleFile(File localeFile) throws IOException {
		System.out.println(" --- Starting parsing locale file --- ");
		
		BufferedReader br = new BufferedReader(new FileReader(localeFile));
	    String line;
	    while ((line = br.readLine()) != null) {
	    	String[] info = StringUtils.substringsBetween(line, "\"", "\"");
	    	if (info == null || info.length < 2) continue;
	    	
	    	if (this.localeMap.put(info[0], info[1]) != null) {
	    		System.err.println("'" + info[0] + "' key was defined multiple times");
	    	}
	    }
	    br.close();
	    
	    System.out.println(" --- Done parsing locale file --- \n");
	}
	
	private void readAllFiles(File projectFile) throws IOException {
		System.out.println(" --- Parsing for NSLocalizedString reference --- ");
		
		Collection<File> files = FileUtils.listFiles(projectFile, new String[] {"swift"}, true);
		for (File file : files) {
			BufferedReader br = new BufferedReader(new FileReader(file));
		    String line;
		    while ((line = br.readLine()) != null) {
		    	String[] info = StringUtils.substringsBetween(line, "NSLocalizedString(\"", "\"");
		    	if (info == null || info.length < 1) continue; 
		    	
		    	for (String string : info) {
		    		this.codeStrings.add(string);
		    	}
		    }
		    br.close();
		}
		
		System.out.println(" --- Done parsing for NSLocalizedString reference --- \n");
	}
	
	private void validateStrings() {
		System.out.println(" --- Comparing code and locale file --- ");
		
		for (String string : this.localeMap.keySet()) {
			if (!this.codeStrings.contains(string)) {
				System.err.println("'" + string + "' was defined in the locale but not used in code! ");
			}
		}
		System.out.println("------");
		for (String string : codeStrings) {
			if (!this.localeMap.containsKey(string)) {
				System.err.println("'" + string + "' was used in the code but never defined in the locale! ");
			}
		}
		
		System.out.println(" --- Done comparing code and locale file --- \n");
	}
}
