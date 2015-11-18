/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter.IONTOF;

import java.io.IOException;
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
public class IONTOFPropertiesTest {
    
    private final static String PROPERTIES_RESOURCE = "/PTFE4.raw.properties.txt";
    
    public IONTOFPropertiesTest() {
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
     * Test of parseProperties method, of class IONTOFProperties.
     */
    @Test
    public void testParseProperties() {
        try {
            System.out.println("parseProperties");
            
            IONTOFProperties result = IONTOFProperties.parseProperties(IONTOFPropertiesTest.class.getResource(PROPERTIES_RESOURCE).getPath());
            
            assertEquals("Context.MassScale.K0 value", -2129.247002858151, result.getProperty("Context.MassScale.K0").getDoubleValue(), 1e-6);
        } catch (IOException ex) {
            Logger.getLogger(IONTOFPropertiesTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
