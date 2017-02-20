/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlparser.imzML.ImzML;
import com.alanmrace.jimzmlparser.imzML.PixelLocation;
import com.alanmrace.jimzmlparser.mzML.BinaryDataArrayList;
import com.alanmrace.jimzmlparser.mzML.CVParam;
import com.alanmrace.jimzmlparser.mzML.FileDescription;
import com.alanmrace.jimzmlparser.mzML.Spectrum;
import com.alanmrace.jimzmlparser.obo.OBO;
import com.alanmrace.jimzmlparser.obo.OBOTerm;
import java.io.DataOutputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Alan
 */
public class ImzMLConverterTest {
    
    public ImzMLConverterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of setOutputFilename method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testSetOutputFilename() {
	System.out.println("setOutputFilename");
	String outputFilename = "";
	ImzMLConverter instance = null;
	instance.setOutputFilename(outputFilename);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of setOBO method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testSetOBO() {
	System.out.println("setOBO");
	OBO obo = null;
	ImzMLConverter instance = null;
	instance.setOBO(obo);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of setBaseimzML method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testSetBaseimzML() {
	System.out.println("setBaseimzML");
	ImzML baseImzML = null;
	ImzMLConverter instance = null;
	instance.setBaseimzML(baseImzML);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of setCompressionType method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testSetCompressionType() {
	System.out.println("setCompressionType");
	CVParam compressionType = null;
	ImzMLConverter instance = null;
	instance.setCompressionType(compressionType);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of setmzArrayDataType method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testSetmzArrayDataType() {
	System.out.println("setmzArrayDataType");
	CVParam mzArrayDataType = null;
	ImzMLConverter instance = null;
	instance.setmzArrayDataType(mzArrayDataType);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of setIntensityArrayDataType method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testSetIntensityArrayDataType() {
	System.out.println("setIntensityArrayDataType");
	CVParam intensityArrayDataType = null;
	ImzMLConverter instance = null;
	instance.setIntensityArrayDataType(intensityArrayDataType);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of removeEmptySpectra method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testRemoveEmptySpectra() {
	System.out.println("removeEmptySpectra");
	boolean removeEmptySpectra = false;
	ImzMLConverter instance = null;
	instance.removeEmptySpectra(removeEmptySpectra);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of setPixelLocations method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testSetPixelLocations() {
	System.out.println("setPixelLocations");
	PixelLocation[] pixelLocations = null;
	ImzMLConverter instance = null;
	instance.setPixelLocations(pixelLocations);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of setBaseImzML method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testSetBaseImzML() {
	System.out.println("setBaseImzML");
	ImzML imzML = null;
	ImzMLConverter instance = null;
	instance.setBaseImzML(imzML);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of generateBaseImzML method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testGenerateBaseImzML() {
	System.out.println("generateBaseImzML");
	ImzMLConverter instance = null;
	instance.generateBaseImzML();
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of getConversionDescription method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testGetConversionDescription() {
	System.out.println("getConversionDescription");
	ImzMLConverter instance = null;
	String expResult = "";
	String result = instance.getConversionDescription();
	assertEquals(expResult, result);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of getOBOTerm method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testGetOBOTerm() {
	System.out.println("getOBOTerm");
	String cvParamID = "";
	OBOTerm expResult = null;
	OBOTerm result = ImzMLConverter.getOBOTerm(cvParamID);
	assertEquals(expResult, result);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of generateReferenceableParamArrays method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testGenerateReferenceableParamArrays() {
	System.out.println("generateReferenceableParamArrays");
	ImzMLConverter instance = null;
	instance.generateReferenceableParamArrays();
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of generatePixelLocations method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testGeneratePixelLocations() {
	System.out.println("generatePixelLocations");
	ImzMLConverter instance = null;
	instance.generatePixelLocations();
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of addSourceFileToImzML method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testAddSourceFileToImzML() {
	System.out.println("addSourceFileToImzML");
	ImzML imzML = null;
	String filename = "";
	String filenameID = "";
	FileDescription currentFileDescription = null;
	ImzMLConverter.addSourceFileToImzML(imzML, filename, filenameID, currentFileDescription);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of removeEmptySpectraFromImzML method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testRemoveEmptySpectraFromImzML() {
	System.out.println("removeEmptySpectraFromImzML");
	ImzML imzML = null;
	ImzMLConverter.removeEmptySpectraFromImzML(imzML);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of convert method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testConvert() throws Exception {
	System.out.println("convert");
	ImzMLConverter instance = null;
	instance.convert();
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of setImzMLImageDimensions method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testSetImzMLImageDimensions() {
	System.out.println("setImzMLImageDimensions");
	ImzML imzML = null;
	int xPixels = 0;
	int yPixels = 0;
	ImzMLConverter.setImzMLImageDimensions(imzML, xPixels, yPixels);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of copySpectrumToImzML method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testCopySpectrumToImzML() throws Exception {
	System.out.println("copySpectrumToImzML");
	ImzML imzML = null;
	Spectrum spectrum = null;
	DataOutputStream binaryDataStream = null;
	long offset = 0L;
	ImzMLConverter instance = null;
	long expResult = 0L;
	long result = instance.copySpectrumToImzML(imzML, spectrum, binaryDataStream, offset);
	assertEquals(expResult, result);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of setCoordinatesOfSpectrum method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testSetCoordinatesOfSpectrum() {
	System.out.println("setCoordinatesOfSpectrum");
	Spectrum spectrum = null;
	int x = 0;
	int y = 0;
	ImzMLConverter.setCoordinatesOfSpectrum(spectrum, x, y);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of createDefaultBinaryDataArrayList method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testCreateDefaultBinaryDataArrayList() {
	System.out.println("createDefaultBinaryDataArrayList");
	BinaryDataArrayList expResult = null;
	BinaryDataArrayList result = ImzMLConverter.createDefaultBinaryDataArrayList();
	assertEquals(expResult, result);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of getProgress method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testGetProgress() {
	System.out.println("getProgress");
	ImzMLConverter instance = null;
	double expResult = 0.0;
	double result = instance.getProgress();
	assertEquals(expResult, result, 0.0);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of updateProgress method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testUpdateProgress() {
	System.out.println("updateProgress");
	double progress = 0.0;
	ImzMLConverter instance = null;
	instance.updateProgress(progress);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of calculateSHA1 method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testCalculateSHA1() throws Exception {
	System.out.println("calculateSHA1");
	String filename = "";
	String expResult = "";
	String result = ImzMLConverter.calculateSHA1(filename);
	assertEquals(expResult, result);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of hexStringToByteArray method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testHexStringToByteArray() {
	System.out.println("hexStringToByteArray");
	String s = "";
	byte[] expResult = null;
	byte[] result = ImzMLConverter.hexStringToByteArray(s);
	assertArrayEquals(expResult, result);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of byteArrayToHexString method, of class ImzMLConverter.
     */
    @Ignore
    @Test
    public void testByteArrayToHexString() {
	System.out.println("byteArrayToHexString");
	byte[] byteArray = null;
	String expResult = "";
	String result = ImzMLConverter.byteArrayToHexString(byteArray);
	assertEquals(expResult, result);
	// TODO review the generated test code and remove the default call to fail.
	fail("The test case is a prototype.");
    }

    /**
     * Test of generateBaseImzML method, of class MzMLToImzMLConverter.
     */
    @org.junit.Test
    @Ignore
    public void testMain() {
        System.out.println("main");
	//String[] args = {MzMLToImzMLConverterTest.class.getResource(MzMLToImzMLConverterTest.TEST_RESOURCE).getPath()};
	String[] args = {"imzML", "C:\\Users\\Alan\\Documents\\Work\\CHCA_MaleRatBrain_N2_Raster(110112,14h27m).wiff"};
        //String[] args = {"D:\\Rory\\SampleData\\2012_5_2_medium(120502,20h18m).wiff"};
	
        MainCommand.main(args);
    }
    
    public class ImzMLConverterImpl extends ImzMLConverter {

	public ImzMLConverterImpl() {
	    super("", null);
	}

	public void generateBaseImzML() {
	}

	public String getConversionDescription() {
	    return "";
	}

	public void generatePixelLocations() {
	}
    }
    
}
