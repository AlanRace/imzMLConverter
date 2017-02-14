/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlconverter.exceptions.ConversionException;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
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
    public static final String CONVERTER_FILENAME = "msconvert.exe";
    //public static final String CONVERTER_x64_LOCATION = "C:\\Program Files\\ProteoWizard";
    public static final String CONVERTER_LOCATION = "C:\\Program Files\\ProteoWizard";
    
    public static final String COMMAND_LINE = " --zlib ";
//    public static final String INDEX_COMMAND = " /index";
    
    public static File[] convert(String filepath) throws IOException {
        return convert(filepath, (new File(filepath)).getParent());
    }
    
    public static File[] convert(String filepath, String outputFilepath) throws IOException {
        final File fileToConvert = new File(filepath);
        File[] mzMLFiles = null;
        
        String extention = "";
            
        if(filepath.lastIndexOf(".") != -1 && filepath.lastIndexOf(".") != 0)
            extention = filepath.substring(filepath.lastIndexOf(".")+1);
            
        if(extention.equalsIgnoreCase("raw") && fileToConvert.isDirectory()) {
            try {
                String tempCommand = getCommand() + " \"" + filepath + "\"" + COMMAND_LINE + " -o \"" + outputFilepath + "\"";
                
                System.out.println(tempCommand);
                Process process = Runtime.getRuntime().exec(tempCommand);
                
                // Wait for the conversion to complete
                process.waitFor();
                
                // Locate the mzML files that were created
                File directory = new File(outputFilepath);
                
                // Get a list of mzML files that include part of the original filename
                mzMLFiles = directory.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        String name = pathname.getName();
                        
                        return name.contains(fileToConvert.getName().replace(".raw", "")) && name.endsWith(".mzML");
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
        File converterFolder = new File(CONVERTER_LOCATION);
        
        File[] subfolders = getSubfolders(converterFolder);
        
        // Check the D drive
        if(subfolders == null) {
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
    
    
    public static void main(String[] args) throws IOException, ConversionException {
        //System.out.println(WatersRAWTomzMLConverter.class.getResource("/DAN.wiff"));
        
        final String[] filePaths = {"F:\\CRUK\\2016_07_08_CRUK_48T_s20.raw"};
                            //"F:\\AstraZeneca\\Lung\\PLD_12_Aug_2015_Grp8_Grp9_htxDHB_100um.raw",
                            //"F:\\AstraZeneca\\Lung\\PLD_13_Aug_2015_Grp6_Grp7_htxDHB_125um.raw",
                            //"F:\\AstraZeneca\\Lung\\PLD_18_Aug_2015_Grp3_4_5_manualDHB_100um.raw",
                            //"F:\\AstraZeneca\\Lung\\PLD_19_Aug_2015_Grp3_4_5_manualDHB_box3_100um.raw"};
        final String[] patternFiles = {"F:\\CRUK\\2016_07_07_cruk_48T_s20.pat"};
        //{"F:\\AstraZeneca\\Lung\\PLD_12_Aug_2015_Grp8_Grp9_htxDHB\\PLD_12_Aug_2015_Grp8_Grp9_htxDHB_100um.pat",
        //                    "F:\\AstraZeneca\\Lung\\PLD_13_Aug_2015_Grp6_Grp7_htxDHB\\PLD_13_Aug_2015_Grp6_Grp7_htxDHB_125um.pat",
         //                   "F:\\AstraZeneca\\Lung\\PLD_18_Aug_2015_Grp3_4_5_manualDHB\\PLD_18_Aug_2015_Grp3_4_5_manualDHB_100um.pat",
         //                   "F:\\AstraZeneca\\Lung\\PLD_19_Aug_2015_Grp3_4_5_manualDHB_box3\\PLD_19_Aug_2015_Grp3_4_5_manualDHB_box3_100um.pat"};
        
        final long startTime = System.currentTimeMillis();
        
//        int i = 1;
        for(int i = 0; i < filePaths.length; i++) {
            final int index = i;
            
            new Thread() {
                @Override
                public void run() {
                    try {
                        try {
                            File[] mzMLFiles = WatersRAWTomzMLConverter.convert(filePaths[index]);
                            
                            System.out.println("Conversion of " + filePaths[index] + " to mzML took " + ((System.currentTimeMillis() - startTime) / 1000.0) + " s");
                        
                            for(File file : mzMLFiles) {
                                System.out.println("Found : " + file.getAbsolutePath());
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(WatersRAWTomzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
                        }
                               
                        String[] inputFiles = {filePaths[index].replace(".raw", ".mzML")};//{mzMLFiles[0].getAbsolutePath()};
                        
                        //String[] inputFiles = {"D:\\AstraZeneca\\John\\19_May_2016_MCTB1604_100mpk_30mins_DHB_MS_100um.mzML"}; //
                        
                        
                        WatersMzMLToImzMLConverter converter = new WatersMzMLToImzMLConverter(filePaths[index], inputFiles, MzMLToImzMLConverter.FileStorage.oneFile);
                        
                        System.out.println("Using .pat file: " + patternFiles[index]);
                        
                        converter.setPatternFile(patternFiles[index]);
                        converter.convert();
                        
                        System.out.println("Conversion of " + filePaths[index] + " to imzML took " + ((System.currentTimeMillis() - startTime) / 1000) + " s");
                    } catch (ConversionException ex) {
                        Logger.getLogger(WatersRAWTomzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (IOException ex) {
//                        Logger.getLogger(WatersRAWTomzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }.start();
        }
        
        System.out.println("Total conversion of " + filePaths.length + " files to imzML took " + ((System.currentTimeMillis() - startTime) / 1000) + " s");
    }
}
