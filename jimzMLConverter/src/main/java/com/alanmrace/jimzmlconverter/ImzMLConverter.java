/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlconverter.exceptions.ImzMLConversionException;
import com.alanmrace.jimzmlparser.imzML.ImzML;
import com.alanmrace.jimzmlparser.imzML.PixelLocation;
import com.alanmrace.jimzmlparser.mzML.BinaryDataArray;
import com.alanmrace.jimzmlparser.mzML.BinaryDataArrayList;
import com.alanmrace.jimzmlparser.mzML.CVParam;
import com.alanmrace.jimzmlparser.mzML.ChromatogramList;
import com.alanmrace.jimzmlparser.mzML.DataProcessing;
import com.alanmrace.jimzmlparser.mzML.EmptyCVParam;
import com.alanmrace.jimzmlparser.mzML.MzML;
import com.alanmrace.jimzmlparser.mzML.ProcessingMethod;
import com.alanmrace.jimzmlparser.mzML.ReferenceableParamGroup;
import com.alanmrace.jimzmlparser.mzML.ScanSettings;
import com.alanmrace.jimzmlparser.mzML.ScanSettingsList;
import com.alanmrace.jimzmlparser.mzML.Software;
import com.alanmrace.jimzmlparser.mzML.SpectrumList;
import com.alanmrace.jimzmlparser.mzML.StringCVParam;
import com.alanmrace.jimzmlparser.obo.OBO;
import com.alanmrace.jimzmlparser.obo.OBOTerm;
import java.io.File;

/**
 *
 * @author Alan
 */
public abstract class ImzMLConverter {
    
    public static final String version = "2.0.0";
    
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
    
    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }
    
    public void setOBO(OBO obo) {
	this.obo = obo;
    }
    
    // Set the imzML structure that is used to determine the base
    public void setBaseimzML(ImzML baseImzML) {
	this.baseImzML = baseImzML;
    }
    
    public void setFileStorage(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
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
    
    public void setBaseImzML(ImzML imzML) {
        this.baseImzML = imzML;
    }
    
    protected abstract void generateBaseImzML();
    protected abstract String getConversionDescription();
    
    
    protected final OBOTerm getOBOTerm(String cvParamID) {
	if(obo == null) 
            obo = OBO.getOBO();
	
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
        
        // Add in compression type
        rpgmzArray.removeChildOfCVParam(BinaryDataArray.compressionTypeID);
        rpgmzArray.addCVParam(compressionType);
        rpgintensityArray.removeChildOfCVParam(BinaryDataArray.compressionTypeID);
        rpgintensityArray.addCVParam(compressionType);
    }
    
    protected void generatePixelLocations() {
        if(baseImzML == null)
	    generateBaseImzML();
        
        switch(fileStorage) {
            case pixelPerFile:
                break;
            case oneFile:
                break;
            case rowPerFile:
            default:
//                pixelLocations = new PixelLocation[inputFilenames.length][baseImzML.getRun().getSpectrumList().size()];
                break;
                
        }
    }
    
    public void convert() throws ImzMLConversionException {
	// Check if the baseimzML is null, if so then use the first (i)mzML file as the base
//	if(baseImzML == null)
        generateBaseImzML();
        
        if(pixelLocations == null)
            generatePixelLocations();
	
	generateReferenceableParamArrays();
        
        // Add in the data processing describing the conversion
        Software imzMLConverter = baseImzML.getSoftwareList().getSoftware("imzMLConverter");
		
	if(imzMLConverter == null || !imzMLConverter.getVersion().equals(ImzMLConverter.version)) {
            imzMLConverter = new Software("imzMLConverter", ImzMLConverter.version);
			
            baseImzML.getSoftwareList().addSoftware(imzMLConverter);
	} 
        
        // Add processing description to DataProcessing list
        DataProcessing conversionToImzML = new DataProcessing("conversionToImzML");
	conversionToImzML.addProcessingMethod(new ProcessingMethod(1, imzMLConverter));
	conversionToImzML.getProcessingMethod(0).addCVParam(new StringCVParam(getOBOTerm(ProcessingMethod.fileFormatConversionID), getConversionDescription()));
        conversionToImzML.getProcessingMethod(0).setSoftwareRef(imzMLConverter);
	
        baseImzML.getDataProcessingList().addDataProcessing(conversionToImzML);
	 
	baseImzML.getRun().setSpectrumList(new SpectrumList(0, conversionToImzML));
	baseImzML.getRun().setChromatogramList(new ChromatogramList(0, conversionToImzML));

    }
    
    protected void updateMaximumPixels(int xPixels, int yPixels) {
        // Add in the maximum number of pixels in each dimension
        ScanSettingsList scanSettingsList = baseImzML.getScanSettingsList();
		
	if(scanSettingsList == null) {
            scanSettingsList = new ScanSettingsList(0);		
            baseImzML.setScanSettingsList(scanSettingsList);
	}
		
	// TODO: Should it be the first scanSettings? One for each experiment?
	ScanSettings scanSettings = scanSettingsList.getScanSettings(0);
		
	if(scanSettings == null) {
            scanSettings= new ScanSettings("scanSettings1");
            scanSettingsList.addScanSettings(scanSettings);
	}
        scanSettings.removeChildOfCVParam(ScanSettings.maxCountPixelXID);
	scanSettings.removeChildOfCVParam(ScanSettings.maxCountPixelYID);
		
	scanSettings.addCVParam(new StringCVParam(getOBOTerm(ScanSettings.maxCountPixelXID), ""+xPixels));
	scanSettings.addCVParam(new StringCVParam(getOBOTerm(ScanSettings.maxCountPixelYID), ""+yPixels));
	//scanSettings.addCVParam(new CVParam(obo.getTerm(ScanSettings.maxCountPixelZID), ""+zPixels));
    }
    
    protected BinaryDataArrayList createDefaultBinaryDataArrayList() {
        BinaryDataArrayList binaryDataArrayList = new BinaryDataArrayList(2);
        
        // m/z
        BinaryDataArray mzBinaryDataArray = new BinaryDataArray(0);
        mzBinaryDataArray.addCVParam(new EmptyCVParam(getOBOTerm(BinaryDataArray.mzArrayID)));
        mzBinaryDataArray.addCVParam(new EmptyCVParam(getOBOTerm(BinaryDataArray.doublePrecisionID)));
        mzBinaryDataArray.addCVParam(new EmptyCVParam(getOBOTerm(BinaryDataArray.noCompressionID)));

        binaryDataArrayList.addBinaryDataArray(mzBinaryDataArray);

        // Counts
        BinaryDataArray countsBinaryDataArray = new BinaryDataArray(0);
        countsBinaryDataArray.addCVParam(new EmptyCVParam(getOBOTerm(BinaryDataArray.intensityArrayID)));
        countsBinaryDataArray.addCVParam(new EmptyCVParam(getOBOTerm(BinaryDataArray.doublePrecisionID)));
        countsBinaryDataArray.addCVParam(new EmptyCVParam(getOBOTerm(BinaryDataArray.noCompressionID)));

        binaryDataArrayList.addBinaryDataArray(countsBinaryDataArray);

        return binaryDataArrayList;
    }
    
    public synchronized double getProgress() {
        return progress;
    }
    
    public synchronized void updateProgress(double progress) {
        this.progress = progress;
    }
}
