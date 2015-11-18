/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter.IONTOF;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author amr1
 */
public class IONTOFProperties {
    private final HashMap<String, IONTOFProperty> properties;
    
    public IONTOFProperties() {
        properties = new HashMap<>();
    }
    
    public static IONTOFProperties parseProperties(String filepath) throws IOException {
        IONTOFProperties properties = new IONTOFProperties();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            
            while ((line = br.readLine()) != null) {
               properties.addProperty(IONTOFProperty.parseProperty(line));
            }
        }
        
        return properties;
    }
    
    public void addProperty(IONTOFProperty property) {
        properties.put(property.getName(), property);
    }
    
    public IONTOFProperty getProperty(String name) {
        return properties.get(name);
    }
}
