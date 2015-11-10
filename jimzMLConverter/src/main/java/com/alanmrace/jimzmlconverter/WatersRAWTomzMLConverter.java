/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlconverter.exceptions.ImzMLConversionException;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author amr1
 */
public class WatersRAWTomzMLConverter {
    public static final String CONVERTER_FILENAME = "msconvert.exe";
    //public static final String CONVERTER_x64_LOCATION = "C:\\Program Files\\ProteoWizard";
    public static final String CONVERTER_LOCATION = "C:\\Program Files\\ProteoWizard";
    
    public static final String COMMAND_LINE = " --zlib ";
//    public static final String INDEX_COMMAND = " /index";
    
    public static File[] convert(String filepath) throws IOException {
        final File fileToConvert = new File(filepath);
        File[] mzMLFiles = null;
        
        String extention = "";
            
        if(filepath.lastIndexOf(".") != -1 && filepath.lastIndexOf(".") != 0)
            extention = filepath.substring(filepath.lastIndexOf(".")+1);
            
        if(extention.equalsIgnoreCase("raw") && fileToConvert.isDirectory()) {
            try {
                String tempCommand = getCommand() + " " + filepath + COMMAND_LINE + " -o " + fileToConvert.getParent();
                
                System.out.println(tempCommand);
                Process process = Runtime.getRuntime().exec(tempCommand);
                
                // Wait for the conversion to complete
                process.waitFor();
                
                // Locate the mzML files that were created
                File directory = new File(fileToConvert.getParent());
                
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
    
    public static String getCommand() {
        File converterFolder = new File(CONVERTER_LOCATION);
        
        File[] subfolders = converterFolder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if(name.contains(name))
                    return true;
                
                return false;
            }
        });
        
        // Use the latest version of ProteoWizard available
        if(subfolders.length > 0)
            converterFolder = subfolders[subfolders.length-1];
        
        
        return converterFolder.getAbsolutePath() + "\\" + CONVERTER_FILENAME;
    }
    
    
    public static void main(String[] args) throws IOException, ImzMLConversionException {
        //System.out.println(WatersRAWTomzMLConverter.class.getResource("/DAN.wiff"));
        
        final String[] filePaths = {"F:\\AstraZeneca\\MALDIData\\05_Sept_2014_AZ13708229_Day_1_2_Recovery.raw",
                            "F:\\AstraZeneca\\MALDIData\\18_Sept_2014_AZ13647935_Day_1_2_7.raw",
                            "F:\\AstraZeneca\\MALDIData\\22_Sept_2014_AZ13708229_Day_1_2_7_Recovery.raw",
                            "F:\\AstraZeneca\\MALDIData\\26_June_2014_PMB_AZ11983219_D22_AZ13719017_D3.raw",
                            "F:\\AstraZeneca\\MALDIData\\29_Aug_2014_PMB_AZ13719017_Day_1_2_7.raw"};
        final String[] patternFiles = {"F:\\AstraZeneca\\MALDIData\\PatternFiles\\05_Sept_2014_AZ13708229_Day_1_2_Recovery\\05_Sept_2014_AZ13708229_Day_1_2_Recovery.pat",
                            "F:\\AstraZeneca\\MALDIData\\PatternFiles\\18_Sept_2014_AZ13647935_Day_1_2_7\\18_Sept_2014_AZ13647935_Day_1_2_7.pat",
                            "F:\\AstraZeneca\\MALDIData\\PatternFiles\\22_Sept_2014_AZ13708229_Day_1_2_7_Recovery\\22_Sept_2014_AZ13708229_Day_1_2_7_Recovery.pat",
                            "F:\\AstraZeneca\\MALDIData\\PatternFiles\\26_June_2014_PMB_AZ11983219_D22_AZ13719017_D3\\26_June_2014_PMB_AZ11983219_D22_AZ13719017_D3.pat",
                            "F:\\AstraZeneca\\MALDIData\\PatternFiles\\29_Aug_2014_PMB_AZ13719017_Day_1_2_7\\29_Aug_2014_PMB_AZ13719017_Day_1_2_7.pat"};
        
        final long startTime = System.currentTimeMillis();
        
        int i = 1;
//        for(int i = 2; i < filePaths.length; i++) {
            final int index = i;
            
            new Thread() {
                @Override
                public void run() {
                    try {
//                        File[] mzMLFiles = WatersRAWTomzMLConverter.convert(filePaths[index]);
                        
//                        System.out.println("Conversion of " + filePaths[index] + " to mzML took " + ((System.currentTimeMillis() - startTime) / 1000) + " s");
                        
//                        for(File file : mzMLFiles) {
//                            System.out.println("Found : " + file.getAbsolutePath());
//                        }
                        
                        String[] inputFiles = {filePaths[index].replace(".raw", ".mzML")};//{mzMLFiles[0].getAbsolutePath()};
                        
                        WatersMzMLToImzMLConverter converter = new WatersMzMLToImzMLConverter(filePaths[index], inputFiles, MzMLToImzMLConverter.FileStorage.oneFile);
                        
                        System.out.println("Using .pat file: " + patternFiles[index]);
                        
                        converter.setPatternFile(patternFiles[index]);
                        converter.convert();
                        
                        System.out.println("Conversion of " + filePaths[index] + " to imzML took " + ((System.currentTimeMillis() - startTime) / 1000) + " s");
                    } catch (ImzMLConversionException ex) {
                        Logger.getLogger(WatersRAWTomzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }.start();
 //       }
        
        System.out.println("Total conversion of " + filePaths.length + " files to imzML took " + ((System.currentTimeMillis() - startTime) / 1000) + " s");
    }
}
