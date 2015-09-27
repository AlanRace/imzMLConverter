/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlparser.imzML.ImzML;
import com.alanmrace.jimzmlparser.imzML.PixelLocation;
import com.alanmrace.jimzmlparser.mzML.BinaryDataArray;
import com.alanmrace.jimzmlparser.mzML.CVParam;
import com.alanmrace.jimzmlparser.mzML.EmptyCVParam;
import com.alanmrace.jimzmlparser.mzML.MzML;
import com.alanmrace.jimzmlparser.mzML.ReferenceableParamGroup;
import com.alanmrace.jimzmlparser.mzML.StringCVParam;
import com.alanmrace.jimzmlparser.obo.OBO;
import com.alanmrace.jimzmlparser.obo.OBOTerm;

/**
 *
 * @author Alan
 */
public abstract class ImzMLConverter {
    
    protected double progress;
    
    protected OBO obo;

    protected ImzML baseImzML;
    
    protected String outputFilename;
    protected String[] inputFilenames;
    protected FileStorage fileStorage;
    
    protected PixelLocation[] pixelLocations;
    
    protected CVParam compressionType;
    protected CVParam mzArrayDataType;
    protected CVParam intensityArrayDataType;
    
    protected boolean removeEmptySpectra;
    
    protected ReferenceableParamGroup rpgmzArray;
    protected ReferenceableParamGroup rpgintensityArray;
    
    public enum FileStorage {
	rowPerFile,
	oneFile,
	pixelPerFile
    }
    
    public ImzMLConverter(String outputFilename, String[] inputFilenames, FileStorage fileStorage) {
	this.outputFilename = outputFilename;
	this.inputFilenames = inputFilenames;
	this.fileStorage = fileStorage;
	
	// Set up defaults
	compressionType = new EmptyCVParam(getOBOTerm(BinaryDataArray.noCompressionID));
	mzArrayDataType = new EmptyCVParam(getOBOTerm(BinaryDataArray.doublePrecisionID));
	intensityArrayDataType = new EmptyCVParam(getOBOTerm(BinaryDataArray.doublePrecisionID));
    }
    
    public void setOBO(OBO obo) {
	this.obo = obo;
    }
    
    // Set the imzML structure that is used to determine the base
    public void setBaseimzML(ImzML baseImzML) {
	this.baseImzML = baseImzML;
    }
    
    public void setCompressionType(CVParam compressionType) {
	this.compressionType = compressionType;
    }
    
    public void setmzArrayDataType(CVParam mzArrayDataType) {
	this.mzArrayDataType = mzArrayDataType;
    }
    
    public void setIntensityArrayDataType(CVParam intensityArrayDataType) {
	this.intensityArrayDataType = intensityArrayDataType;
    }
    
    public void removeEmptySpectra(boolean removeEmptySpectra) {
	this.removeEmptySpectra = removeEmptySpectra;
    }
    
    public void setPixelLocations(PixelLocation[] pixelLocations) {
	this.pixelLocations = pixelLocations;
    }
    
//    public abstract void convert();
    
    protected abstract void generateBaseImzML();
    
    private OBOTerm getOBOTerm(String cvParamID) {
	if(obo == null)
	    return new OBOTerm(cvParamID);
	
	return obo.getTerm(cvParamID);
    }
    
    protected void generateReferenceableParamArrays() {
	// TODO: MAKE THIS BETTER - ONLY HERE TO PLEASE BIOMAP AND DATACUBE EXPLORER
	rpgmzArray = baseImzML.getReferenceableParamGroupList().getReferenceableParamGroup("mzArray");

	if (rpgmzArray == null) {
	    rpgmzArray = new ReferenceableParamGroup("mzArray");

	    rpgmzArray.addCVParam(new StringCVParam(getOBOTerm(BinaryDataArray.mzArrayID), ""));
	    rpgmzArray.addCVParam(mzArrayDataType);

	    baseImzML.getReferenceableParamGroupList().addReferenceableParamGroup(rpgmzArray);
	} else {
	    rpgmzArray.removeChildOfCVParam(BinaryDataArray.dataTypeID);
	    rpgmzArray.addCVParam(mzArrayDataType);
	}

	rpgintensityArray = baseImzML.getReferenceableParamGroupList().getReferenceableParamGroup("intensityArray");

	// Check to see if Bruker conversion has been used
	if (rpgintensityArray == null) {
	    rpgintensityArray = baseImzML.getReferenceableParamGroupList().getReferenceableParamGroup("intensities");
	}

	// If it's still null then create one
	if (rpgintensityArray == null) {
	    rpgintensityArray = new ReferenceableParamGroup("intensityArray");

	    rpgintensityArray.addCVParam(new StringCVParam(getOBOTerm(BinaryDataArray.intensityArrayID), ""));
	    rpgintensityArray.addCVParam(intensityArrayDataType);

	    baseImzML.getReferenceableParamGroupList().addReferenceableParamGroup(rpgintensityArray);
	} else {
	    rpgintensityArray.removeChildOfCVParam(BinaryDataArray.dataTypeID);
	    rpgintensityArray.addCVParam(intensityArrayDataType);
	}
    }
    
    public void convert() {
	// Check if the baseimzML is null, if so then use the first (i)mzML file as the base
	if(baseImzML == null)
	    generateBaseImzML();
	
	generateReferenceableParamArrays();
    }
}
