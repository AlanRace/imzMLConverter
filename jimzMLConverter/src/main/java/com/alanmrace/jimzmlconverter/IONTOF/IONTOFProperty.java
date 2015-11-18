/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter.IONTOF;

/**
 *
 * @author amr1
 */
public class IONTOFProperty {
    private final String name;
    private final int integerValue;
    private final double doubleValue;
    private final String details;
    
    public IONTOFProperty(String name, int integerValue, double doubleValue, String details) {
        this.name = name;
        this.integerValue = integerValue;
        this.doubleValue = doubleValue;
        this.details = details;
    }
    
    public static IONTOFProperty parseProperty(String line) {
        String[] parts = line.split("\t");
        
        String name = null;
        int integerValue = 0;
        double doubleValue = 0;
        String details = null;
        
        if(parts.length == 4) {
            name = parts[0].trim();
            integerValue = Integer.parseInt(parts[1]);
            doubleValue = Double.parseDouble(parts[2]);
            details = parts[3].trim();
        }
        
        return new IONTOFProperty(name, integerValue, doubleValue, details);
    }
    
    public String getName() {
        return name;
    }
    
    public int getIntegerValue() {
        return integerValue;
    }
    
    public double getDoubleValue() {
        return doubleValue;
    }
    
    public String getDetails() {
        return details;
    }
}
