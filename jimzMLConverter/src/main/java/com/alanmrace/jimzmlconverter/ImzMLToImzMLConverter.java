/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlconverter.exceptions.ConversionException;
import com.alanmrace.jimzmlparser.exceptions.FatalParseException;
import com.alanmrace.jimzmlparser.exceptions.ImzMLWriteException;
import com.alanmrace.jimzmlparser.imzml.ImzML;
import com.alanmrace.jimzmlparser.imzml.PixelLocation;
import com.alanmrace.jimzmlparser.mzml.FileContent;
import com.alanmrace.jimzmlparser.mzml.ReferenceableParamGroup;
import com.alanmrace.jimzmlparser.mzml.SourceFile;
import com.alanmrace.jimzmlparser.mzml.Spectrum;
import com.alanmrace.jimzmlparser.mzml.SpectrumList;
import com.alanmrace.jimzmlparser.mzml.StringCVParam;
import com.alanmrace.jimzmlparser.parser.ImzMLHandler;
import com.alanmrace.jimzmlparser.writer.ImzMLHeaderWriter;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
        } catch (FatalParseException ex) {
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
        
        if(imageStartCoordinate.isEmpty())
            setImageGrid(1, inputFilenames.length);
    }
    
    public List<PixelLocation> getImageSizes() {
        if(imageSizes.isEmpty()) {
            for(int i = 0; i < inputFilenames.length; i++) {
                try {
                    ImzML imzML = ImzMLHandler.parseimzML(inputFilenames[i], false);
                    
                    imageSizes.add(i, new PixelLocation(imzML.getWidth(), imzML.getHeight(), imzML.getDepth()));
                } catch (FatalParseException ex) {
                    Logger.getLogger(ImzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        }
        
        return imageSizes;
    }
    
    
    public void setImageGrid(int imagesInX, int imagesInY) {
        // Make sure that image sizes have been generated
        getImageSizes();
        
        int startY = 1;
        int nextStartY = 1;
        
        int startX = 1;
        
        int image = 0;
        
        for(int y = 0; y < imagesInY; y++) {
            if(image >= imageSizes.size())
                break;
            
            for(int x = 0; x < imagesInX; x++) {
                if(image >= imageSizes.size())
                    break;
                
                PixelLocation imageSize = imageSizes.get(image);
                
                imageStartCoordinate.add(image, new PixelLocation(startX, startY, 1));
                
                // Make sure that we record the largest height in the row
                if((startY + imageSize.getY()) > nextStartY)
                    nextStartY = (startY + imageSize.getY());
                
                startX += imageSize.getX();
                
                image++;
            }
            
            startX = 1;            
            startY = nextStartY;
        }
    }
    
    
    @Override
    public void convert() throws ConversionException {
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

                    throw new ConversionException("Error writing UUID " + e2.getLocalizedMessage(), e2);
                } catch (IOException e) {
                    throw new ConversionException("Error closing .ibd file after failing writing UUID " + e.getLocalizedMessage(), e);
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

                    String filenameID = "imzML" + currentmzMLFile++;
                    
                    // TODO: Add all referenceParamGoups - TEMPORARY FIX
                    for (ReferenceableParamGroup rpg : currentimzML.getReferenceableParamGroupList()) {
                        rpg.setID(filenameID + "_" + rpg.getID());
                        baseImzML.getReferenceableParamGroupList().addReferenceableParamGroup(rpg);
                    }
                    
                    if(fileIndex > 0) {
                        // Add in all sourceFiles
                        for(SourceFile sourceFile : currentimzML.getFileDescription().getSourceFileList()) {
                            sourceFile.setID(filenameID + "_" + sourceFile.getID());
                            baseImzML.getFileDescription().getSourceFileList().addSourceFile(sourceFile);
                        }
                    }
                    
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
                } catch (FatalParseException ex) {
                    Logger.getLogger(ImzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
                    
                    throw new ConversionException("ImzMLParseException: " + ex, ex);
                }
            }

            outputFullmzList(binaryDataStream, offset);
            
            binaryDataStream.close();
        } catch (IOException ex) {
            Logger.getLogger(ImzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
            
            throw new ConversionException("Error closing " + outputFilename + ".ibd", ex);
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
        ImzMLHeaderWriter imzMLWriter = new ImzMLHeaderWriter();
        
        try {
            imzMLWriter.write(baseImzML, outputFilename);
        } catch (IOException ex) {
            Logger.getLogger(ImzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //try {
        //    baseImzML.write(outputFilename + ".imzML");
        //} catch (ImzMLWriteException ex) {
        //    Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        //}

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
            //    "F:\\AstraZeneca\\Lung\\PLD_12_Aug_2015_Grp8_Grp9_htxDHB_100um.raw.imzML",
            //    "F:\\AstraZeneca\\Lung\\PLD_13_Aug_2015_Grp6_Grp7_htxDHB_125um.raw.imzML",
            //    "F:\\AstraZeneca\\Lung\\PLD_18_Aug_2015_Grp3_4_5_manualDHB_100um.raw.imzML",
            //    "F:\\AstraZeneca\\Lung\\PLD_19_Aug_2015_Grp3_4_5_manualDHB_box3_100um.raw.imzML"
//"F:\\Projects\\CRACK-IT\\2015_12_09_lung_R1_saline_S1(151209,15h20m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_09_lung_R1_saline_S4(151209,08h51m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_09_lung_R1_saline_S7(151209,18h37m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_09_lung_R1_saline_S10(151209,15h50m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_09_lung_R1_saline_S13(151209,09h22m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_09_lung_R1_saline_S16(151209,19h06m).wiff.imzML",
//
//"F:\\Projects\\CRACK-IT\\2015_12_16_lung_R6_saline_S19(151216,16h15m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_16_lung_R6_saline_S22(151216,12h15m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_16_lung_R6_saline_S25(151216,09h05m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_16_lung_R6_saline_S28(151216,15h55m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_16_lung_R6_saline_S31(151216,12h32m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_16_lung_R6_saline_S34(151216,09h24m).wiff.imzML",
//                
//"F:\\Projects\\CRACK-IT\\2015_12_17_lung_R7_saline_S1(151217,11h32m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_17_lung_R7_salinei_S4(151217,09h32m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_17_lung_R7_saline_S7(151217,14h35m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_17_lung_R7_saline_S10(151217,15h00m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_17_lung_R7_saline_S13(151217,11h57m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_17_lung_R7_salinei_S16(151217,09h54m).wiff.imzML",


"F:\\Projects\\CRACK-IT\\2015_12_09_lung_R2_Ami_S19(151209,16h15m).wiff.imzML",
"F:\\Projects\\CRACK-IT\\2015_12_09_lung_R2_Ami_S22(151209,09h45m).wiff.imzML",
"F:\\Projects\\CRACK-IT\\2015_12_09_lung_R2_Ami_S25(151209,19h30m).wiff.imzML",
"F:\\Projects\\CRACK-IT\\2015_12_09_lung_R2_Ami_S28(151209,16h43m).wiff.imzML",
"F:\\Projects\\CRACK-IT\\2015_12_09_lung_R2_Ami_S31(151209,10h09m).wiff.imzML",
"F:\\Projects\\CRACK-IT\\2015_12_09_lung_R2_Ami_S34(151209,19h56m).wiff.imzML",


"F:\\Projects\\CRACK-IT\\2015_12_10_lung_R3_Ami_S19(151210,14h38m).wiff.imzML",
"F:\\Projects\\CRACK-IT\\2015_12_10_lung_R3_Ami_S22(151210,08h10m).wiff.imzML",
"F:\\Projects\\CRACK-IT\\2015_12_10_lung_R3_Ami_S28(151210,14h54m).wiff.imzML",
"F:\\Projects\\CRACK-IT\\2015_12_10_lung_R3_Ami_S31(151210,11h18m).wiff.imzML",
"F:\\Projects\\CRACK-IT\\2015_12_10_lung_R3_Ami_S31(151210,08h27m).wiff.imzML",
"F:\\Projects\\CRACK-IT\\2015_12_10_lung_R3_Ami_S34(151210,11h03m).wiff.imzML",


"F:\\Projects\\CRACK-IT\\2015_12_17_lung_R8_Ami_S19(151217,12h21m).wiff.imzML",
"F:\\Projects\\CRACK-IT\\2015_12_17_lung_R8_Ami_S22(151217,10h17m).wiff.imzML",
"F:\\Projects\\CRACK-IT\\2015_12_17_lung_R8_Ami_S25(151217,15h24m).wiff.imzML",
"F:\\Projects\\CRACK-IT\\2015_12_17_lung_R8_Ami_S28(151217,12h38m).wiff.imzML",
"F:\\Projects\\CRACK-IT\\2015_12_17_lung_R8_Ami_S31(151217,10h32m).wiff.imzML",
"F:\\Projects\\CRACK-IT\\2015_12_17_lung_R8_Ami_S34(151217,15h41m).wiff.imzML",

//"F:\\Projects\\CRACK-IT\\2015_12_10_lung_R4_LPVA_S1(151210,14h04m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_10_lung_R4_LPVA_S4(151210,07h36m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_10_lung_R4_LPVA_S7(151210,10h27m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_10_lung_R4_LPVA_S10(151210,10h39m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_10_lung_R4_LPVA_S13(151210,14h15m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_10_lung_R4_LPVA_S16(151210,07h46m).wiff.imzML",
//
//
//"F:\\Projects\\CRACK-IT\\2015_12_16_lung_R5_LPVA_S1(151216,15h30m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_16_lung_R5_LPVA_S4(151216,11h47m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_16_lung_R5_LPVA_S7(151216,08h41m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_16_lung_R5_LPVA_S10(151216,15h44m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_16_lung_R5_LPVA_S13(151216,12h03m).wiff.imzML",
//"F:\\Projects\\CRACK-IT\\2015_12_16_lung_R5_LPVA_S16(151216,08h53m).wiff.imzML"
            };
            
            String outputFile = "F:\\Projects\\CRACK-IT\\CRACKIT_Lung_Ami_6x3.imzML";
            
            
            ImzMLToImzMLConverter converter = new ImzMLToImzMLConverter(outputFile, inputFiles);
            
            converter.setImageGrid(6, 3);
            
            converter.convert();
        } catch (ConversionException ex) {
            Logger.getLogger(ImzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
