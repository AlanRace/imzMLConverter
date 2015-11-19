/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlconverter.exceptions.ImzMLConversionException;
import com.alanmrace.jimzmlparser.exceptions.ImzMLParseException;
import com.alanmrace.jimzmlparser.exceptions.ImzMLWriteException;
import com.alanmrace.jimzmlparser.imzML.ImzML;
import com.alanmrace.jimzmlparser.imzML.PixelLocation;
import com.alanmrace.jimzmlparser.mzML.FileContent;
import com.alanmrace.jimzmlparser.mzML.ReferenceableParamGroup;
import com.alanmrace.jimzmlparser.mzML.Spectrum;
import com.alanmrace.jimzmlparser.mzML.SpectrumList;
import com.alanmrace.jimzmlparser.mzML.StringCVParam;
import com.alanmrace.jimzmlparser.parser.ImzMLHandler;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alan
 */
public class ImzMLToImzMLConverter extends ImzMLConverter {

    private List<PixelLocation> imageSizes;
    private List<PixelLocation> imageStartCoordinate;
    
    //private List<ImzML> imzMLs;
    
    public ImzMLToImzMLConverter(String outputFilename, String[] inputFilenames) {
	super(outputFilename, inputFilenames);
        
        imageSizes = new ArrayList<>();
        imageStartCoordinate = new ArrayList<>();
    }

    @Override
    protected void generateBaseImzML() {
        try {
            baseImzML = ImzMLHandler.parseimzML(inputFilenames[0], false);
        } catch (ImzMLParseException ex) {
            Logger.getLogger(ImzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected String getConversionDescription() {
        return "Conversion of imzML to imzML";
    }
    
    @Override
    protected void generatePixelLocations() {
	if (baseImzML == null) {
            generateBaseImzML();
        }
        
        setImageGrid(1, inputFilenames.length);
    }
    
    public List<PixelLocation> getImageSizes() {
        if(imageSizes.isEmpty()) {
            for(int i = 0; i < inputFilenames.length; i++) {
                try {
                    ImzML imzML = ImzMLHandler.parseimzML(inputFilenames[i], false);
                    
                    imageSizes.add(i, new PixelLocation(imzML.getWidth(), imzML.getHeight(), imzML.getDepth()));
                } catch (ImzMLParseException ex) {
                    Logger.getLogger(ImzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        }
        
        return imageSizes;
    }
    
    
    public void setImageGrid(int imagesInX, int imagesInY) {
        // Make sure that image sizes have been generated
        getImageSizes();
        
        int sizeInY = 1;
        
        for(int image = 0; image < imageSizes.size(); image++) {
            PixelLocation imageSize = imageSizes.get(image);
            
            imageStartCoordinate.add(image, new PixelLocation(1, sizeInY, 1));
            
            sizeInY += imageSize.getY();
        }
    }
    
    
    @Override
    public void convert() throws ImzMLConversionException {
        super.convert();

        int x = 1;
        int y = 1;

        int maxX = x;
        int maxY = y;

        int currentPixelLocation = 0;

        // Open the .ibd data stream
        DataOutputStream binaryDataStream;

        try {
            binaryDataStream = new DataOutputStream(new FileOutputStream(outputFilename + ".ibd"));

            String uuid = UUID.randomUUID().toString().replace("-", "");
            System.out.println(uuid);

            // Add UUID to the imzML file
            baseImzML.getFileDescription().getFileContent().removeChildOfCVParam(FileContent.ibdIdentificationID);
            baseImzML.getFileDescription().getFileContent().addCVParam(new StringCVParam(getOBOTerm(FileContent.uuidIdntificationID), uuid));

            try {
                binaryDataStream.write(hexStringToByteArray(uuid));
            } catch (IOException e2) {
                try {
                    binaryDataStream.close();

                    throw new ImzMLConversionException("Error writing UUID " + e2.getLocalizedMessage());
                } catch (IOException e) {
                    throw new ImzMLConversionException("Error closing .ibd file after failing writing UUID " + e.getLocalizedMessage());
                }
            }

            long offset = binaryDataStream.size();
            int currentmzMLFile = 0;

            for (int fileIndex = 0; fileIndex < inputFilenames.length; fileIndex++) {
                String imzMLFilename = inputFilenames[fileIndex];
                
                try {
                    ImzML currentimzML = ImzMLHandler.parseimzML(imzMLFilename);
                    
                    PixelLocation startCoordinate = this.imageStartCoordinate.get(fileIndex);
                    System.out.println(startCoordinate);

                    // TODO: Add all referenceParamGoups - TEMPORARY FIX
                    for (ReferenceableParamGroup rpg : currentimzML.getReferenceableParamGroupList()) {
                        baseImzML.getReferenceableParamGroupList().addReferenceableParamGroup(rpg);
                    }

                    String filenameID = "imzML" + currentmzMLFile++;
                    addSourceFileToImzML(baseImzML, imzMLFilename, filenameID, currentimzML.getFileDescription());

                    SpectrumList spectrumList = currentimzML.getRun().getSpectrumList();
                    int numSpectra = spectrumList.size();
                    
                    for (int i = 0; i < numSpectra; i++) {
                        Spectrum spectrum = currentimzML.getRun().getSpectrumList().getSpectrum(i);
                        PixelLocation spectrumLocation = spectrum.getPixelLocation();
                        
                        int spectrumX = spectrumLocation.getX() + startCoordinate.getX()-1;
                        int spectrumY = spectrumLocation.getY() + startCoordinate.getY()-1;
                        
                        currentPixelLocation++;

                        // TODO: REMOVE THIS FOR SPEED INCREASE WHEN WORKAROUND IS IMPLEMENTED
                        spectrum.getmzArray();

                        offset = copySpectrumToImzML(baseImzML, spectrum, binaryDataStream, offset);
                        setCoordinatesOfSpectrum(spectrum, spectrumX, spectrumY);

                        if (spectrumX > maxX) {
                            maxX = spectrumX;
                        }
                        if (spectrumY > maxY) {
                            maxY = spectrumY;
                        }
                    }

                    Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.FINEST, "About to close mzML in convert()");
                    currentimzML.close();
                } catch (ImzMLParseException ex) {
                    Logger.getLogger(ImzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
                    
                    throw new ImzMLConversionException("ImzMLParseException: " + ex);
                }
            }

            outputFullmzList(binaryDataStream, offset);
            
            binaryDataStream.close();
        } catch (IOException ex) {
            Logger.getLogger(ImzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
            
            throw new ImzMLConversionException("Error closing " + outputFilename + ".ibd");
        }

        if (removeEmptySpectra) {
            removeEmptySpectraFromImzML(baseImzML);
        }

        setImzMLImageDimensions(baseImzML, maxX, maxY);

        String sha1Hash = calculateSHA1(outputFilename + ".ibd");
        baseImzML.getFileDescription().getFileContent().removeChildOfCVParam(FileContent.ibdChecksumID);

        System.out.println("SHA-1 Hash: " + sha1Hash);
        baseImzML.getFileDescription().getFileContent().addCVParam(new StringCVParam(getOBOTerm(FileContent.sha1ChecksumID), sha1Hash));

        // Output the imzML portion of the data
        try {
            baseImzML.write(outputFilename + ".imzML");
        } catch (ImzMLWriteException ex) {
            Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

        baseImzML.close();
    }
    
    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            // todo
            String[] inputFiles = {
            //"F:\\AstraZeneca\\MALDIData\\05_Sept_2014_AZ13708229_Day_1_2_Recovery.raw_Processed.imzML",
            //    "F:\\AstraZeneca\\MALDIData\\18_Sept_2014_AZ13647935_Day_1_2_7.raw_Processed.imzML",
            //    "F:\\AstraZeneca\\MALDIData\\22_Sept_2014_AZ13708229_Day_1_2_7_Recovery.raw_Processed.imzML",
            //    "F:\\AstraZeneca\\MALDIData\\26_June_2014_PMB_AZ11983219_D22_AZ13719017_D3.raw_Processed.imzML",
            //    "F:\\AstraZeneca\\MALDIData\\29_Aug_2014_PMB_AZ13719017_Day_1_2_7.raw_Processed.imzML"
                "F:\\AstraZeneca\\Lung\\PLD_12_Aug_2015_Grp8_Grp9_htxDHB_100um.raw.imzML",
                "F:\\AstraZeneca\\Lung\\PLD_13_Aug_2015_Grp6_Grp7_htxDHB_125um.raw.imzML",
                "F:\\AstraZeneca\\Lung\\PLD_18_Aug_2015_Grp3_4_5_manualDHB_100um.raw.imzML",
                "F:\\AstraZeneca\\Lung\\PLD_19_Aug_2015_Grp3_4_5_manualDHB_box3_100um.raw.imzML"
            };
            
            String outputFile = "F:\\AstraZeneca\\Lung\\AZ_Lung_All.imzML";
            
            
            ImzMLToImzMLConverter converter = new ImzMLToImzMLConverter(outputFile, inputFiles);
            
            converter.convert();
        } catch (ImzMLConversionException ex) {
            Logger.getLogger(ImzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
