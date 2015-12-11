/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import static com.alanmrace.jimzmlconverter.MzMLToImzMLConverterTest.TEST_RESOURCE;
import com.alanmrace.jimzmlparser.imzML.ImzML;
import com.alanmrace.jimzmlparser.parser.ImzMLHandler;
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
public class ImzMLToHDF5ConverterTest {

    public static final String TEST_RESOURCE = "/IM_500_IM_S.raw.imzML"; // "/2012_5_2_medium(120502,20h18m).wiff"; 

    public ImzMLToHDF5ConverterTest() {
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
     * Test of convert method, of class ImzMLToHDF5Converter.
     */
    @Test
    public void testConvert() throws Exception {
        System.out.println("convert");
        assertNotNull("Test file missing", ImzMLToHDF5ConverterTest.class.getResource(TEST_RESOURCE));

        String resourcePath = MzMLToImzMLConverterTest.class.getResource(TEST_RESOURCE).getPath();

        if (resourcePath.startsWith("/")) {
            resourcePath = resourcePath.substring(1);
        }
        
        ImzML imzML = ImzMLHandler.parseimzML(resourcePath);

        ImzMLToHDF5Converter instance = new ImzMLToHDF5Converter(imzML, resourcePath.replace(".imzML", ".hd5"));
        System.out.println("About to convert");
        instance.convert();
        System.out.println("Converted");
    }

}
