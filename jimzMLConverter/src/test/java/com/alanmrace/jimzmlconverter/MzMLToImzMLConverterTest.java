/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlconverter.exceptions.ImzMLConversionException;
import com.alanmrace.jimzmlparser.imzML.ImzML;
import com.alanmrace.jimzmlparser.mzML.BinaryDataArray;
import com.alanmrace.jimzmlparser.mzML.EmptyCVParam;
import com.alanmrace.jimzmlparser.mzML.Spectrum;
import com.alanmrace.jimzmlparser.obo.OBOTerm;
import com.alanmrace.jimzmlparser.parser.ImzMLHandler;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author amr1
 */
public class MzMLToImzMLConverterTest {
    
    public static final String TEST_RESOURCE = "/MatrixTests_N2.wiff"; // "/2012_5_2_medium(120502,20h18m).wiff"; 
    
    protected static File[] mzMLFiles;
    protected static String outputPath;
    
    protected MzMLToImzMLConverter converter;
    
    public MzMLToImzMLConverterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        System.out.println("Setting up MzMLToImzMLConverterTest");
        assertNotNull("Test file missing", MzMLToImzMLConverterTest.class.getResource(TEST_RESOURCE));
        
        try {
            String resourcePath = MzMLToImzMLConverterTest.class.getResource(TEST_RESOURCE).getPath();
            
            if(resourcePath.startsWith("/"))
                resourcePath = resourcePath.substring(1);
            
            mzMLFiles = WiffTomzMLConverter.convert(resourcePath);
            
            // Create the output path
            outputPath = resourcePath.replace(".wiff", "");
            
            System.out.println("Converted " + mzMLFiles.length + " files");
            assertEquals("Number of mzML files produced ", 4, mzMLFiles.length);
        } catch (IOException ex) {
            fail("IOException: " + ex);
        }
    }
    
    @AfterClass
    public static void tearDownClass() {
        System.out.println("Removing mzML files");
        
        for(File file : mzMLFiles)
            file.delete();
    }
    
    @Before
    public void setUp() {
        String[] inputFilenames = new String[mzMLFiles.length];
        
        for(int i = 0; i < mzMLFiles.length; i++)
            inputFilenames[i] = mzMLFiles[i].getAbsolutePath();
        
        converter = new MzMLToImzMLConverter(outputPath, inputFilenames, MzMLToImzMLConverter.FileStorage.rowPerFile);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of generateBaseImzML method, of class MzMLToImzMLConverter.
     */
    @org.junit.Test
    public void testGenerateBaseImzML() {
        System.out.println("generateBaseImzML");
        converter.generateBaseImzML();
    }


    /**
     * Test of convert method, of class MzMLToImzMLConverter.
     * @throws com.alanmrace.jimzmlconverter.exceptions.ImzMLConversionException
     */
    @org.junit.Test
    public void testConvert() throws ImzMLConversionException {
        String ibdFilepath = outputPath + ".ibd";
        
        System.out.println("convert");
        converter.convert();
        testImzMLOutput();
        
        File ibdFile = new File(ibdFilepath);
        long convertedSize = ibdFile.length();
        System.out.println("Converted binary file size: " + convertedSize);
        
        // Test the conversion can produce compressed code
        System.out.println("convert zlib");
        converter.setCompressionType(new EmptyCVParam(new OBOTerm(BinaryDataArray.zlibCompressionID)));
        converter.convert();
        testImzMLOutput();
        
        long convertedzlibLength = ibdFile.length();
        System.out.println("Converted zlib compressed binary file size: " + convertedzlibLength);
        
        assertTrue("zlib compressed size < binary size", convertedzlibLength < convertedSize);
        
        // Test that the conversion can alter the data type used to store values
        // Single precision for m/z array
        converter.setmzArrayDataType(new EmptyCVParam(new OBOTerm(BinaryDataArray.singlePrecisionID)));
        converter.convert();
        testImzMLOutput();
    }
    
    protected void testImzMLOutput() {
        ImzML imzML = ImzMLHandler.parseimzML(outputPath + ".imzML");
        assertNotNull(imzML);
            
        Spectrum spectrum = imzML.getSpectrum(1, 1);
        assertNotNull(spectrum);
         
        try {    
            double[] mzs = spectrum.getmzArray();
            assertNotNull(mzs);
            
            assertEquals(50, mzs[0], 1);
            assertEquals(2000, mzs[mzs.length-1], 1);
        } catch (IOException ex) {
            Logger.getLogger(MzMLToImzMLConverterTest.class.getName()).log(Level.SEVERE, null, ex);
            
            fail("IOException: " + ex);
        }
    }
    
}
