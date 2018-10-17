/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import static com.alanmrace.jimzmlconverter.MzMLToImzMLConverter.getNumberSpectraPerPixel;
import com.alanmrace.jimzmlconverter.Thermo.UDPFile;
import com.alanmrace.jimzmlconverter.Thermo.UDPFileHandler;
import com.alanmrace.jimzmlconverter.exceptions.ConversionException;
import com.alanmrace.jimzmlparser.imzml.PixelLocation;
import com.alanmrace.jimzmlparser.mzml.SpectrumList;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alan
 */
public class ThermoMzMLToImzMLConverter extends MzMLToImzMLConverter {
    
    private static final Logger logger = Logger.getLogger(ThermoMzMLToImzMLConverter.class.getName());
    
    String udpFileLocation;
    
    public ThermoMzMLToImzMLConverter(String outputFilename, String[] inputFilenames, FileStorage fileStorage) {
        super(outputFilename, inputFilenames, fileStorage);
    }
    
    public void setUDPFile(String udpFileLocation) {
        this.udpFileLocation = udpFileLocation;
    }
    
    @Override
    protected void generatePixelLocations() {
        if(coordsFilename != null) {
            super.generatePixelLocations();
        } else {
            if(udpFileLocation != null) {
                if (baseImzML == null) {
                    generateBaseImzML();
                }
                
                try {
                    this.pixelLocations = ThermoMzMLToImzMLConverter.getPixelLocationFromUDPFile(udpFileLocation, baseImzML.getRun().getSpectrumList());

                    logger.log(Level.INFO, "Generated pixel locations from .udp file");
                } catch (ConversionException ex) {
                    logger.log(Level.SEVERE, null, ex); 
                }
            } 

            // If a pattern file has not been supplied then it is likely that the data is not imaging
            if(pixelLocations == null) {
                int numSpectraPerPixel = getNumberSpectraPerPixel(baseImzML.getRun().getSpectrumList());
                int numSpectra = baseImzML.getRun().getSpectrumList().size();

                pixelLocations = new PixelLocation[numSpectra];

                for(int i = 0; i < numSpectra; i++) {
                    pixelLocations[i] = new PixelLocation(i / numSpectraPerPixel + 1, 1, 1);
                }
            }
        }
    }
        
    public static PixelLocation[] getPixelLocationFromUDPFile(String udpFileLocation, SpectrumList oldSpectrumList) throws ConversionException {
        UDPFile udpFile = UDPFileHandler.parseUDPFile(udpFileLocation);

        int numSpectraPerPixel = getNumberSpectraPerPixel(oldSpectrumList);
        
        logger.log(Level.INFO, MessageFormat.format("Found {0} spectra per pixel", numSpectraPerPixel));

        int maxX = udpFile.getMaxX();
        int maxY = udpFile.getMaxY();
        
        PixelLocation[] spectraLocations = new PixelLocation[maxX*maxY*numSpectraPerPixel];

        for(int y = 0; y < maxY; y++) {
            for(int x = 0; x < maxX; x++) {
                int index = y * maxX*numSpectraPerPixel + x * numSpectraPerPixel;
                
                for(int n = 0; n < numSpectraPerPixel; n++) {
                    spectraLocations[index + n] = new PixelLocation(x+1, y+1, 1);
                }
            }
        }
        
        return spectraLocations;
    }
}
