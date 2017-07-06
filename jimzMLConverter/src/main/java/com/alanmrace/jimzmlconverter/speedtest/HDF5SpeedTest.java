/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter.speedtest;

import com.alanmrace.jimzmlconverter.ImzMLToHDF5Converter;
import com.alanmrace.jimzmlconverter.exceptions.ConversionException;
import com.alanmrace.jimzmlparser.exceptions.FatalParseException;
import com.alanmrace.jimzmlparser.exceptions.ImzMLParseException;
import com.alanmrace.jimzmlparser.imzml.ImzML;
import com.alanmrace.jimzmlparser.parser.ImzMLHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author amr1
 */
public class HDF5SpeedTest {
    
    public static final String TEST_RESOURCE = "/IM_500_IM_S.raw.imzML";
    
    public static void main(String[] args) {
        try {
            System.out.println(HDF5SpeedTest.class.getResource(TEST_RESOURCE));
            String resourcePath = "D:\\GitProjects\\jimzMLConverter\\jimzMLConverter\\target\\test-classes\\IM_500_IM_S.raw.imzML"; // HDF5SpeedTest.class.getResource(TEST_RESOURCE).getPath();
            
            if (resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring(1);
            }
            
            ImzML imzML = ImzMLHandler.parseimzML(resourcePath);
            
            ImzMLToHDF5Converter instance = new ImzMLToHDF5Converter(imzML, resourcePath.replace(".imzML", ".hd5"));
            System.out.println("About to convert");
            instance.convert();
            System.out.println("Converted");
        } catch (FatalParseException | ConversionException ex) {
            Logger.getLogger(HDF5SpeedTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
