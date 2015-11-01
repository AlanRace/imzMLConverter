/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

/**
 *
 * @author Alan
 */
public class ImzMLToImzMLConverter extends ImzMLConverter {

    public ImzMLToImzMLConverter(String outputFilename, String[] inputFilenames) {
	super(outputFilename, inputFilenames);
    }

    @Override
    protected void generateBaseImzML() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected String getConversionDescription() {
        return "Conversion of imzML to imzML";
    }

    @Override
    protected void generatePixelLocations() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
