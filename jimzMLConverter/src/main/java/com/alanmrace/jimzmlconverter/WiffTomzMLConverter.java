/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author amr1
 */
public class WiffTomzMLConverter {
    public static final String CONVERTER_FILENAME = "AB_SCIEX_MS_Converter.exe";
    public static final String CONVERTER_x64_LOCATION = "C:\\Program Files (x86)\\AB SCIEX\\MS Data Converter";
    public static final String CONVERTER_x86_LOCATION = "C:\\Program Files\\AB SCIEX\\MS Data Converter";
    
    public static final String COMMAND_LINE = " -profile MZML ";
    
    public static void main(String args[]) throws IOException {
        String folder = "D:\\Ryan";
        
        File folderLocation = new File(folder);
        ArrayList<File> files = new ArrayList<File>(Arrays.asList(folderLocation.listFiles()));
        
        String command;
        
        if(System.getProperty("os.arch").contains("64"))
            command = CONVERTER_x64_LOCATION;
        else
            command = CONVERTER_x86_LOCATION;
        
        command += "\\" + CONVERTER_FILENAME + " WIFF ";
                    
        for(File file : files) {
            String fileName = file.getName();
            String extention = "";
            
            if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
                extention = fileName.substring(fileName.lastIndexOf(".")+1);
            
            if(extention.equalsIgnoreCase("wiff")) {
                String tempCommand = command + file.getAbsolutePath() + COMMAND_LINE + file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(".")) + ".mzML";
                
                System.out.println(tempCommand);
                Process process = Runtime.getRuntime().exec(tempCommand);
            }
        }
    }
}
