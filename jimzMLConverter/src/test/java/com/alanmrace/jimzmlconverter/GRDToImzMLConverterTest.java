/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlparser.imzML.ImzML;
import com.alanmrace.jimzmlparser.mzML.ReferenceableParamGroupRef;
import com.alanmrace.jimzmlparser.mzML.Spectrum;
import com.alanmrace.jimzmlparser.mzML.SpectrumList;
import com.alanmrace.jimzmlparser.obo.OBO;
import com.alanmrace.jimzmlparser.parser.ImzMLHandler;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author amr1
 */
public class GRDToImzMLConverterTest {
    
    private static final Logger logger = Logger.getLogger(GRDToImzMLConverterTest.class.getName());
    
    private static final String TEST_GRD_RESOURCE = "/test.grd";
    private static final String TEST_PROPERTIES_RESOURCE = "/test.properties.txt";
    
    //private GRDToImzMLConverter instance;
    
    private final String grdPath = GRDToImzMLConverterTest.class.getResource(TEST_GRD_RESOURCE).getPath();
    private final String propertiesFile = GRDToImzMLConverterTest.class.getResource(TEST_PROPERTIES_RESOURCE).getPath();
    
    public GRDToImzMLConverterTest() {
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
     * Test of setPropertiesFile method, of class GRDToImzMLConverter.
     */
    @Test
    public void testSetPropertiesFile() throws Exception {
        System.out.println("setPropertiesFile");
        
        
        GRDToImzMLConverter instance = new GRDToImzMLConverter(grdPath, new String[]{grdPath});
        
        instance.setPropertiesFile(propertiesFile);
        
        // TODO: Check properties
    }

    /**
     * Test of generateBaseImzML method, of class GRDToImzMLConverter.
     */
    @Test
    public void testGenerateBaseImzML() {
        try {
            System.out.println("generateBaseImzML");
            GRDToImzMLConverter instance = new GRDToImzMLConverter(grdPath, new String[]{grdPath});
            
            instance.setPropertiesFile(propertiesFile);
            instance.generateBaseImzML();
            
            // TODO: Check base imzML
        } catch (IOException ex) {
            Logger.getLogger(GRDToImzMLConverterTest.class.getName()).log(Level.SEVERE, null, ex);
            
            fail("IOException " + ex);
        }
    }

    /**
     * Test of getConversionDescription method, of class GRDToImzMLConverter.
     */
    @Test
    @Ignore
    public void testGetConversionDescription() {
        System.out.println("getConversionDescription");
        GRDToImzMLConverter instance = null;
        String expResult = "";
        String result = instance.getConversionDescription();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of generatePixelLocations method, of class GRDToImzMLConverter.
     */
    @Test
    @Ignore
    public void testGeneratePixelLocations() {
        System.out.println("generatePixelLocations");
        GRDToImzMLConverter instance = null;
        instance.generatePixelLocations();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of convert method, of class GRDToImzMLConverter.
     */
    @Test
    public void testConvert() {
        System.out.println("convert");
        
        try {
            GRDToImzMLConverter instance = new GRDToImzMLConverter(grdPath, new String[]{grdPath});
            
            instance.setPropertiesFile(propertiesFile);
            instance.convert();
            
            ImzML imzML = ImzMLHandler.parseimzML(grdPath + ".imzML");
            Spectrum firstSpectrum = imzML.getSpectrum(1, 1);
            double[] mzs = firstSpectrum.getmzArray();
            double[] intensities = firstSpectrum.getIntensityArray();
            
            logger.log(Level.FINE, "m/z array length {0}", mzs.length);
            logger.log(Level.FINE, "intensities array length {0}", intensities.length);
            
            assertEquals("Intensity of first pixel", 3, intensities[0], 0.1);
        } catch (IOException ex) {
            Logger.getLogger(GRDToImzMLConverterTest.class.getName()).log(Level.SEVERE, null, ex);
            
            fail("IOException " + ex);
        }
    }

    /**
     * Test of outputSpectrum method, of class GRDToImzMLConverter.
     */
    @Test
    @Ignore
    public void testOutputSpectrum() throws Exception {
        System.out.println("outputSpectrum");
        HashMap<Long, Integer> spectrumData = null;
        int x = 0;
        int y = 0;
        double k0 = 0.0;
        double sf = 0.0;
        int numxPixels = 0;
        int numyPixels = 0;
        OBO obo = null;
        ReferenceableParamGroupRef mzArrayRef = null;
        ReferenceableParamGroupRef intensityArrayRef = null;
        DataOutputStream dos = null;
        SpectrumList spectrumList = null;
        GRDToImzMLConverter.outputSpectrum(spectrumData, x, y, k0, sf, numxPixels, numyPixels, obo, mzArrayRef, intensityArrayRef, dos, spectrumList);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class GRDToImzMLConverter.
     */
    @Test
    @Ignore
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        GRDToImzMLConverter.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
