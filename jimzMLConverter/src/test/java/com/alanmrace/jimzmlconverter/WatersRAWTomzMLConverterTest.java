/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlconverter.MzMLToImzMLConverter.FileStorage;
import java.io.File;
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
public class WatersRAWTomzMLConverterTest {
    
    public static final String TEST_RESOURCE = "/IM_500_IM_S.raw"; // "/141024_HM_02.raw"; // "/2013_12_11_MatrixAndResolutionTest_25um.raw";
    
    public WatersRAWTomzMLConverterTest() {
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
     * Test of convert method, of class WatersRAWTomzMLConverter.
     * @throws java.lang.Exception
     */
    @Test
    @Ignore
    public void testConvert() throws Exception {
        assertNotNull("Test file missing", WatersRAWTomzMLConverterTest.class.getResource(TEST_RESOURCE));
        
        String resourcePath = MzMLToImzMLConverterTest.class.getResource(TEST_RESOURCE).getPath();
            
            if(resourcePath.startsWith("/"))
                resourcePath = resourcePath.substring(1);
        
        System.out.println("convert");
        File[] result = WatersRAWTomzMLConverter.convert(resourcePath);
        
        assertTrue("No mzML files produced", result.length > 0);
        
        String[] inputFiles = new String[]{result[0].getAbsolutePath()};
        
        WatersMzMLToImzMLConverter converter = new WatersMzMLToImzMLConverter(resourcePath, inputFiles, FileStorage.oneFile);
        //converter.setPatternFile(resourcePath.replace(".raw", ".pat"));
        converter.convert();
    }

    /**
     * Test of getCommand method, of class WatersRAWTomzMLConverter.
     */
    @Test
    @Ignore
    public void testGetCommand() {
        System.out.println("getCommand");
        String expResult = "";
        String result = WatersRAWTomzMLConverter.getCommand();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class WatersRAWTomzMLConverter.
     * @throws java.lang.Exception
     */
    @Test
    @Ignore
    public void testMain() throws Exception {
        System.out.println("main");
        String[] args = null;
        WatersRAWTomzMLConverter.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
