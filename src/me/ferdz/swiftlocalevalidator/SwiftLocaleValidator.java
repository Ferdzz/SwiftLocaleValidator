package me.ferdz.swiftlocalevalidator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class SwiftLocaleValidator {

    public static final String
            DIRECTORY_OPTION_KEY = "directory",
            LOCALE_OPTION_KEY = "locale",
            SUBSTRING_START_OPTION_KEY = "substring";

    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser();
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

        Option substringFileOption = new Option(SUBSTRING_START_OPTION_KEY, true,
                "the custom substring to check in code if you're not using NSLocalizedString.\n" +
                "For example\n-substring \"\"\" \"\\\".localized\"");
        substringFileOption.setType(String[].class);
        substringFileOption.setOptionalArg(true);
        substringFileOption.setArgs(2);
        substringFileOption.setArgName("substring");
        options.addOption(substringFileOption);

        try {
            CommandLine commandLine = parser.parse(options, args);
            File projectFile = (File) commandLine.getParsedOptionValue(DIRECTORY_OPTION_KEY);
            File localeFile = (File) commandLine.getParsedOptionValue(LOCALE_OPTION_KEY);
            String[] substring = { "NSLocalizedString(\"", "\"" };
            if (commandLine.hasOption(SUBSTRING_START_OPTION_KEY)) {
                substring = commandLine.getOptionValues(SUBSTRING_START_OPTION_KEY);
                if (substring.length != 2) {
                    System.err.println("-substring argument requires exactly 2 parameters");
                    return;
                }
            }
            SwiftLocaleValidator validator = new SwiftLocaleValidator();
            validator.validateLocale(projectFile, localeFile, substring);
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

    public void validateLocale(File projectFile, File localeFile, String[] substring) throws IOException {
        System.out.println("Parsing the project...\n");
        this.readLocaleFile(localeFile);
        this.readAllFiles(projectFile, substring);
        this.validateStrings();
        System.out.println("Done parsing the project!");
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

    private void readAllFiles(File projectFile, String[] substring) throws IOException {
        System.out.println(" --- Parsing code for Localized String references --- ");

        Collection<File> files = FileUtils.listFiles(projectFile, new String[] {"swift"}, true);
        for (File file : files) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] info = StringUtils.substringsBetween(line, substring[0], substring[1]);
                if (info == null || info.length < 1) continue;

                for (String string : info) {
                    this.codeStrings.add(string);
                }
            }
            br.close();
        }

        System.out.println(" --- Done parsing code for Localized String references --- \n");
    }

    private void validateStrings() {
        System.out.println(" --- Comparing code and locale file --- ");

        for (String string : this.localeMap.keySet()) {
            if (!this.codeStrings.contains(string)) {
                System.out.println("'" + string + "' was defined in the locale but not used in code! ");
            }
        }
        System.out.println("------");
        for (String string : codeStrings) {
            if (!this.localeMap.containsKey(string)) {
                System.out.println("'" + string + "' was used in the code but never defined in the locale! ");
            }
        }

        System.out.println(" --- Done comparing code and locale file, tasks finished --- \n");
    }
}
