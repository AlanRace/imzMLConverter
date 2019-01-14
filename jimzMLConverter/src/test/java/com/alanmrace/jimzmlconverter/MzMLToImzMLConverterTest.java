/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlconverter.exceptions.ConversionException;
import com.alanmrace.jimzmlparser.exceptions.FatalParseException;
import com.alanmrace.jimzmlparser.imzml.ImzML;
import com.alanmrace.jimzmlparser.mzml.BinaryDataArray;
import com.alanmrace.jimzmlparser.mzml.EmptyCVParam;
import com.alanmrace.jimzmlparser.mzml.Spectrum;
import com.alanmrace.jimzmlparser.obo.OBO;
import com.alanmrace.jimzmlparser.parser.ImzMLHandler;
import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author amr1
 */

public class MzMLToImzMLConverterTest {
    
    private static final Logger logger = Logger.getLogger(MzMLToImzMLConverterTest.class.getName());
    
    public static final String TEST_RESOURCE = "/MatrixTests_N2.wiff"; // "/2012_5_2_medium(120502,20h18m).wiff"; 
    
    protected static File[] mzMLFiles;
    protected static String outputPath;
    
    protected MzMLToImzMLConverter converter;
    
    public MzMLToImzMLConverterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        org.junit.Assume.assumeTrue(isWindows());
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
            //assertEquals("Number of mzML files produced ", 4, mzMLFiles.length);
            
            // Check to see if the correct number of files have been converted to continue (i.e. if MS Data Converter (SCIEX) is installed)
            org.junit.Assume.assumeTrue(mzMLFiles.length == 4);
        } catch (IOException ex) {
            fail("IOException: " + ex);
        }
    }
    
    @AfterClass
    public static void tearDownClass() {
        org.junit.Assume.assumeTrue(isWindows());
        System.out.println("Removing mzML files");

        if(mzMLFiles != null)
            for(File file : mzMLFiles)
                file.delete();
    }
     
    String[] inputFilenames;

    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    @Before
    public void setUp() {
        logger.setLevel(Level.ALL);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
        
       inputFilenames = new String[mzMLFiles.length];
        
        for(int i = 0; i < mzMLFiles.length; i++)
            inputFilenames[i] = mzMLFiles[i].getAbsolutePath();
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
        //converter.generateBaseImzML();
    }


    /**
     * Test of convert method, of class MzMLToImzMLConverter.
     * @throws com.alanmrace.jimzmlconverter.exceptions.ConversionException
     */
    @org.junit.Test
    public void testConvert() throws ConversionException {        
        System.out.println("testConvert");
        setUp();
        
        String imzMLFile = outputPath;
        converter = new MzMLToImzMLConverter(imzMLFile, inputFilenames, MzMLToImzMLConverter.FileStorage.rowPerFile);
        converter.convert();
        testImzMLOutput(imzMLFile);
    }
    
    @Ignore
    @org.junit.Test
    public void testConvertPixelPerFile() throws ConversionException {
        String[] inputFilenames = new String[] {"D:\\SmithAndNephew\\SampleMean\\2016_04_28_SmithNephew_10.mzML", "D:\\SmithAndNephew\\SampleMean\\2016_04_28_SmithNephew_11.mzML"};
        
        MzMLToImzMLConverter converter = new MzMLToImzMLConverter("testttttt.imzML", inputFilenames, MzMLToImzMLConverter.FileStorage.pixelPerFile);
        converter.convert();
    }
    
    @org.junit.Test
    public void testzlibConvert() throws ConversionException {
        String ibdFilepath = outputPath + "_testzlibConvert.ibd";
        
        System.out.println("testzlibConvert");
        setUp();
        
        String imzMLFile = outputPath + "_testzlibConvert";
        converter = new MzMLToImzMLConverter(imzMLFile, inputFilenames, MzMLToImzMLConverter.FileStorage.rowPerFile);
        
        converter.setmzArrayDataType(new EmptyCVParam(OBO.getOBO().getTerm(BinaryDataArray.DOUBLE_PRECISION_ID)));
        converter.setIntensityArrayDataType(new EmptyCVParam(OBO.getOBO().getTerm(BinaryDataArray.DOUBLE_PRECISION_ID)));
        converter.setCompressionType(new EmptyCVParam(OBO.getOBO().getTerm(BinaryDataArray.NO_COMPRESSION_ID)));
        converter.convert();
        testImzMLOutput(imzMLFile);
        
        File ibdFile = new File(ibdFilepath);
        long convertedSize = ibdFile.length();
        System.out.println("Converted binary file size: " + convertedSize);
        
        // Test the conversion can produce compressed code
        System.out.println("convert zlib");
   //     converter.setCompressionType(new EmptyCVParam(new OBOTerm(BinaryDataArray.zlibCompressionID)));
        setUp();
        
        imzMLFile = outputPath + "_testzlibConvert_2";
        converter = new MzMLToImzMLConverter(imzMLFile, inputFilenames, MzMLToImzMLConverter.FileStorage.rowPerFile);
        
        converter.setmzArrayDataType(new EmptyCVParam(OBO.getOBO().getTerm(BinaryDataArray.DOUBLE_PRECISION_ID)));
        converter.setIntensityArrayDataType(new EmptyCVParam(OBO.getOBO().getTerm(BinaryDataArray.DOUBLE_PRECISION_ID)));
        converter.setCompressionType(new EmptyCVParam(OBO.getOBO().getTerm(BinaryDataArray.ZLIB_COMPRESSION_ID)));
        
        converter.convert();
        testImzMLOutput(imzMLFile);
        
        ibdFilepath = outputPath + "_testzlibConvert_2.ibd";
        ibdFile = new File(ibdFilepath);
        long convertedzlibLength = ibdFile.length();
        System.out.println("Converted zlib compressed binary file size: " + convertedzlibLength);
        
        assertTrue("zlib compressed size < binary size", convertedzlibLength < convertedSize);
    }
    
    @org.junit.Test
    public void testSinglePrecisionConvert() throws ConversionException {
        // Test that the conversion can alter the data type used to store values
        // Single precision for m/z array
        System.out.println("convert single precision");
        setUp();
        
        String imzMLFile = outputPath + "_testSinglePrecisionConvert";
        converter = new MzMLToImzMLConverter(imzMLFile, inputFilenames, MzMLToImzMLConverter.FileStorage.rowPerFile);
        
        converter.setmzArrayDataType(new EmptyCVParam(OBO.getOBO().getTerm(BinaryDataArray.SINGLE_PRECISION_ID)));
        converter.setIntensityArrayDataType(new EmptyCVParam(OBO.getOBO().getTerm(BinaryDataArray.SINGLE_PRECISION_ID)));
        converter.convert();
        testImzMLOutput(imzMLFile);
    }
    
    @org.junit.Test
    public void testSigned64bitIntConvert() throws ConversionException {
        // Test that the conversion can alter the data type used to store values
        // Single precision for m/z array
        System.out.println("convert signed 64 bit integer");
        setUp();
        
        String imzMLFile = outputPath + "_testSigned64bitIntConvert";
        converter = new MzMLToImzMLConverter(imzMLFile, inputFilenames, MzMLToImzMLConverter.FileStorage.rowPerFile);
        
        converter.setmzArrayDataType(new EmptyCVParam(OBO.getOBO().getTerm(BinaryDataArray.SIGNED_64BIT_INTEGER_ID)));
        converter.setIntensityArrayDataType(new EmptyCVParam(OBO.getOBO().getTerm(BinaryDataArray.SIGNED_64BIT_INTEGER_ID)));
        converter.convert();
        testImzMLOutput(imzMLFile);
    }
    
    @org.junit.Test
    public void testSigned32bitIntConvert() throws ConversionException {
        // Test that the conversion can alter the data type used to store values
        // Single precision for m/z array
        System.out.println("convert signed 32 bit integer");
        setUp();
        
        String imzMLFile = outputPath + "_testSigned32bitIntConvert";
        converter = new MzMLToImzMLConverter(imzMLFile, inputFilenames, MzMLToImzMLConverter.FileStorage.rowPerFile);
        
        converter.setmzArrayDataType(new EmptyCVParam(OBO.getOBO().getTerm(BinaryDataArray.SIGNED_32BIT_INTEGER_ID)));
        converter.setIntensityArrayDataType(new EmptyCVParam(OBO.getOBO().getTerm(BinaryDataArray.SIGNED_32BIT_INTEGER_ID)));
        converter.convert();
        testImzMLOutput(imzMLFile);
    }
    
    @org.junit.Test
    public void testSigned16bitIntConvert() throws ConversionException {
        // Test that the conversion can alter the data type used to store values
        // Single precision for m/z array
        System.out.println("convert signed 16 bit integer");
        setUp();
        
        String imzMLFile = outputPath + "_testSigned16bitIntConvert";
        converter = new MzMLToImzMLConverter(imzMLFile, inputFilenames, MzMLToImzMLConverter.FileStorage.rowPerFile);
        
        converter.setmzArrayDataType(new EmptyCVParam(OBO.getOBO().getTerm(BinaryDataArray.SIGNED_16BIT_INTEGER_ID)));
        converter.setIntensityArrayDataType(new EmptyCVParam(OBO.getOBO().getTerm(BinaryDataArray.SIGNED_16BIT_INTEGER_ID)));
        converter.convert();
        testImzMLOutput(imzMLFile);
    }
    
    @org.junit.Ignore
    @org.junit.Test
    public void testSigned8bitIntConvert() throws ConversionException {
        // Test that the conversion can alter the data type used to store values
        // Single precision for m/z array
        System.out.println("convert signed 8 bit integer");
        
        setUp();
        
        String imzMLFile = outputPath + "_testSigned8bitIntConvert";
        converter = new MzMLToImzMLConverter(imzMLFile, inputFilenames, MzMLToImzMLConverter.FileStorage.rowPerFile);
        
        converter.setIntensityArrayDataType(new EmptyCVParam(OBO.getOBO().getTerm(BinaryDataArray.SIGNED_8BIT_INTEGER_ID)));
        converter.convert();
        testImzMLOutput(imzMLFile);
    }
    
    protected void testImzMLOutput(String imzMLFile) throws ConversionException {
        try {
            ImzML imzML = ImzMLHandler.parseimzML(imzMLFile + ".imzML");
            assertNotNull(imzML);
            
            Spectrum spectrum = imzML.getSpectrum(1, 1);
            assertNotNull(spectrum);
            
            try {
                double[] mzs = spectrum.getmzArray();
                assertNotNull(mzs);
                
                System.out.println("m/z range: " + mzs[0] + " - " + mzs[mzs.length-1]);
                
                assertEquals(50, mzs[0], 1);
                assertEquals(2000, mzs[mzs.length-1], 1);
                
                double[] intensities = spectrum.getIntensityArray();
                
                System.out.println("Intensity: " + intensities[0]);
                assertEquals(2, intensities[0], 0.1);
            } catch (IOException ex) {
                Logger.getLogger(MzMLToImzMLConverterTest.class.getName()).log(Level.SEVERE, null, ex);
                
                fail("IOException: " + ex);
            }
	    
	    imzML.close();
        } catch (FatalParseException ex) {
            Logger.getLogger(MzMLToImzMLConverterTest.class.getName()).log(Level.SEVERE, null, ex);
            
            throw new ConversionException("FatalParseException: " + ex.getLocalizedMessage(), ex);
        } 
    }
 
    public static void main(String args[]) {
        MzMLToImzMLConverterTest.setUpClass();
        MzMLToImzMLConverterTest test = new MzMLToImzMLConverterTest();
        test.setUp();
        
        try {
            test.testConvert();
        } catch (ConversionException ex) {
            Logger.getLogger(MzMLToImzMLConverterTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
