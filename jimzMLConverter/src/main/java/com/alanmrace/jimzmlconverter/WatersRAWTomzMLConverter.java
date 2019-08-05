/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlconverter.exceptions.ConversionException;

import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author amr1
 */
public class WatersRAWTomzMLConverter {
    
    private static final Logger logger = Logger.getLogger(WatersRAWTomzMLConverter.class.getName());
    
    public static final String CONVERTER_NAME = "ProteoWizard";
    public static final String WINE_CONVERTER = "wine msconvert";
    public static final String CONVERTER_FILENAME = "msconvert.exe";
    //public static final String CONVERTER_x64_LOCATION = "C:\\Program Files\\ProteoWizard";
    public static final String CONVERTER_LOCATION = "C:\\Program Files\\ProteoWizard";
    
    public static final String COMMAND_LINE = " --zlib --simAsSpectra ";
//    public static final String INDEX_COMMAND = " /index";


    
    public static File[] convert(String filepath) throws IOException {
        return convert(filepath, false, "");
    }
    
    public static File[] convert(String filepath, boolean centroid, String msconvertFilter) throws IOException {
        return convert(filepath, (new File(filepath)).getAbsoluteFile().getParent(), centroid, msconvertFilter);
    }
    
    public static File[] convert(String filepath, String outputFilepath, boolean centroid, String msconvertFilter) throws IOException {
        final File fileToConvert = new File(filepath);
        File[] mzMLFiles = null;
        
        String extention = "";
        
        File outputFile = new File(outputFilepath);
        if(!outputFile.isDirectory())
            outputFilepath = outputFile.getParentFile().getAbsolutePath();
            
        if(filepath.lastIndexOf(".") != -1 && filepath.lastIndexOf(".") != 0)
            extention = filepath.substring(filepath.lastIndexOf(".")+1);
            
        if(extention.equalsIgnoreCase("raw")) { // && fileToConvert.isDirectory()) {
            try {
                String tempCommand = getCommand();
                boolean isLinux = tempCommand.contains("wine");

                if(isLinux)
                    tempCommand += " " + filepath;
                else
                    tempCommand += " \"" + filepath + "\"";

                tempCommand += COMMAND_LINE;
                
                if(centroid)
                    tempCommand +=  " --filter \"peakPicking true 1-\"";
                
                if(!msconvertFilter.isEmpty())
                    tempCommand += " --filter \"" + msconvertFilter + "\"";
                
                tempCommand += " -o ";

                if(isLinux)
                    tempCommand += outputFilepath;
                else
                    tempCommand += "\"" + outputFilepath + "\"";
                
                System.out.println(tempCommand);
                Process process = Runtime.getRuntime().exec(tempCommand);
                
                // Wait for the conversion to complete
                process.waitFor();

                BufferedReader br=new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;

                while((line = br.readLine()) != null) {
                    System.out.println(line);
                }

                
                // Locate the mzML files that were created
                File directory = new File(outputFilepath);
                
                // Get a list of mzML files that include part of the original filename
                mzMLFiles = directory.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        String name = pathname.getName();
                        
                        return name.contains(fileToConvert.getName().replace(".raw", "").replace("RAW", "")) && name.endsWith(".mzML");
                    }
                });
                
                if(mzMLFiles != null) {
                    // Order the files by their modified date
                    Arrays.sort(mzMLFiles, new Comparator<File>(){
                        @Override
                        public int compare(File f1, File f2)
                        {
                            return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                        } 
                    });
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(WiffTomzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return mzMLFiles;
    }
    
    private static File[] getSubfolders(File converterFolder) {
        File[] subfolders = converterFolder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.contains(CONVERTER_NAME) && dir.isDirectory();
            }
        });
        
        return subfolders;
    }
    
    private static class VersionDetails {
        public int major;
        public int minor;
        public int patch;
        
        public VersionDetails(int major, int minor, int patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }
        
        @Override
        public String toString() {
            return "" + major + "." + minor + "." + patch;
        }
    }
    
    private static VersionDetails parseVersionDetails(String path) {
        String[] pathSplit = path.split(CONVERTER_NAME);
        String[] numbers = pathSplit[pathSplit.length-1].trim().split(Pattern.quote("."));
        
        VersionDetails details = new VersionDetails(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1]), Integer.parseInt(numbers[2]));
        
        return details;
    }
    
    public static String getCommand() {
        try {
            Process process = Runtime.getRuntime().exec(WINE_CONVERTER);

            // Wait for the conversion to complete
            process.waitFor();

            Logger.getLogger(WiffTomzMLConverter.class.getName()).log(Level.INFO, "Found wine, so running using wine");

            return WINE_CONVERTER;
        } catch (InterruptedException e) {
        } catch (IOException e) {
        }

        System.out.println("Checking for latest version of ProteoWizard in " + Paths.get(System.getProperty("user.home"), "AppData", "Local", "Apps").toString());

        File converterFolder = new File(Paths.get(System.getProperty("user.home"), "AppData", "Local", "Apps").toString());

        File[] subfolders = getSubfolders(converterFolder);

        if(subfolders == null || subfolders.length == 0) {
            converterFolder = new File(CONVERTER_LOCATION);

            subfolders = getSubfolders(converterFolder);
        }

        // Check the D drive
        if(subfolders == null || subfolders.length == 0) {
            converterFolder = new File(CONVERTER_LOCATION.replaceFirst("C", "D"));
            subfolders = getSubfolders(converterFolder);
        }
        
        // Use the latest version of ProteoWizard available
        if(subfolders != null && subfolders.length > 0) {
            VersionDetails details = parseVersionDetails(subfolders[0].getName());
            int newestID = 0;
            
            for(int i = 1; i < subfolders.length; i++) {
                VersionDetails currentDetails = parseVersionDetails(subfolders[i].getName());
                                
                if(currentDetails.major > details.major || 
                        (currentDetails.major >= details.major && currentDetails.minor > details.minor) ||
                        (currentDetails.major >= details.major && currentDetails.minor >= details.minor && currentDetails.patch >= details.patch)) {
                    details = currentDetails;
                    newestID = i;
                }
            }
            
            converterFolder = subfolders[newestID];
        }
        
        return "\"" + converterFolder.getAbsolutePath() + "\\" + CONVERTER_FILENAME + "\"";
    }
}
