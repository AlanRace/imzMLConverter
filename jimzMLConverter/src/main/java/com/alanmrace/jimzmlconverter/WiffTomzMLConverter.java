/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlconverter.MzMLToImzMLConverter.FileStorage;
import com.alanmrace.jimzmlconverter.exceptions.ConversionException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author amr1
 */
public class WiffTomzMLConverter {
    public static final String CONVERTER_FILENAME = "AB_SCIEX_MS_Converter.exe";
    public static final String CONVERTER_x64_LOCATION = "C:\\Program Files (x86)\\AB SCIEX\\MS Data Converter";
    public static final String CONVERTER_x86_LOCATION = "C:\\Program Files\\AB SCIEX\\MS Data Converter";
    
    public static final String COMMAND_LINE = " -profile MZML ";
    public static final String INDEX_COMMAND = " /index";
    
    /**
     *
     * @param filepath Location of the wiff file to convert
     * @return File[] containing the list of mzML files created in the conversion
     * @throws IOException
     */
    public static File[] convert(String filepath) throws IOException {
        final File fileToConvert = new File(filepath);
        File[] mzMLFiles = null;
        
        String extention = "";
            
        if(filepath.lastIndexOf(".") != -1 && filepath.lastIndexOf(".") != 0)
            extention = filepath.substring(filepath.lastIndexOf(".")+1);
            
        if(extention.equalsIgnoreCase("wiff")) {
            try {
                String tempCommand = getCommand() + "\"" + filepath + "\"" + COMMAND_LINE + "\"" +  filepath.substring(0, filepath.lastIndexOf(".")) + ".mzML\"" + INDEX_COMMAND;
                
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
                        
                        return name.contains(fileToConvert.getName().replace(".wiff", "")) && name.endsWith(".mzML");
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
        String command = "\"";
        
        if(System.getProperty("os.arch").contains("64"))
            command += CONVERTER_x64_LOCATION;
        else
            command += CONVERTER_x86_LOCATION;
        
        command += "\\" + CONVERTER_FILENAME + "\" WIFF ";
        
        return command;
    }
    
    public static void main(String args[]) throws IOException, ConversionException {
        String folder = "D:\\SLAM";
        
        File folderLocation = new File(folder);
        ArrayList<File> files = new ArrayList<File>(Arrays.asList(folderLocation.listFiles()));
        
        
                    
        for(File file : files) {
            if(file.getAbsolutePath().endsWith(".wiff")) {
            
                File[] mzMLFiles = convert(file.getAbsolutePath());
                String[] filenames = new String[mzMLFiles.length];
                
                for(int i = 0; i < mzMLFiles.length; i++)
                    filenames[i] = mzMLFiles[i].getAbsolutePath();
            
                MzMLToImzMLConverter converter = new MzMLToImzMLConverter(file.getAbsolutePath(), filenames, FileStorage.rowPerFile);
                
                converter.convert();
            }
        }
    }
}
