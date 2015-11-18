/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter.IONTOF;

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
public class IONTOFPropertyTest {
    
    public IONTOFPropertyTest() {
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
     * Test of parseProperty method, of class IONTOFProperty.
     */
    @Test
    public void testParseProperty() {
        System.out.println("parseProperty");
        String line = "Analysis.SputterTime	1311	1310.7199668884277	1310.72 s";
        IONTOFProperty expResult = new IONTOFProperty("Analysis.SputterTime", 1311, 1310.7199668884277, "1310.72 s");
        
        IONTOFProperty result = IONTOFProperty.parseProperty(line);
        
        assertNotNull(result);
        
        assertEquals("Property name", expResult.getName(), result.getName());
        assertEquals("Property integer value", expResult.getIntegerValue(), result.getIntegerValue());
        assertEquals("Property double value", expResult.getDoubleValue(), result.getDoubleValue(), 1e-6);
        assertEquals("Property details", expResult.getDetails(), result.getDetails());
    }
    
}
