/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import static com.alanmrace.jimzmlconverter.MzMLToImzMLConverter.getNumberSpectraPerPixel;
import com.alanmrace.jimzmlconverter.Waters.PatternDefinitionHandler;
import com.alanmrace.jimzmlconverter.exceptions.ConversionException;
import com.alanmrace.jimzmlparser.imzML.PixelLocation;
import com.alanmrace.jimzmlparser.mzML.SpectrumList;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author Alan
 */
public class ThermoMzMLToImzMLConverter extends MzMLToImzMLConverter {
    
    private static final Logger logger = Logger.getLogger(WatersMzMLToImzMLConverter.class.getName());
    
    String udpFile;
    
    public ThermoMzMLToImzMLConverter(String outputFilename, String[] inputFilenames, FileStorage fileStorage) {
        super(outputFilename, inputFilenames, fileStorage);
    }
    
    public void setUDPFile(String udpFile) {
        this.udpFile = udpFile;
    }
    
    @Override
    protected void generatePixelLocations() {
        if(coordsFilename != null) {
            super.generatePixelLocations();
        } else {
            if(udpFile != null) {
                try {
                    this.pixelLocations = ThermoMzMLToImzMLConverter.getPixelLocationFromUDPFile(udpFile, baseImzML.getRun().getSpectrumList());

                    logger.log(Level.INFO, "Generated pixel locations from .pat file");
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
        
    public static PixelLocation[] getPixelLocationFromUDPFile(String udpFile, SpectrumList oldSpectrumList) throws ConversionException {
        int xPixels = 0;
        int yPixels = 0;
        int zPixels = 0;

        // Convert mzML header information -> imzML
        PatternDefinitionHandler handler = new PatternDefinitionHandler();
        File patternF = new File(udpFile);

        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse(patternF, handler);

        } catch (SAXException | IOException | ParserConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        int numSpectraPerPixel = getNumberSpectraPerPixel(oldSpectrumList);
        
        logger.log(Level.INFO, MessageFormat.format("Found {0} spectra per pixel", numSpectraPerPixel));

        PixelLocation[] spectraLocations = handler.getPatternDefinition().convertToPixelLocations(oldSpectrumList, numSpectraPerPixel);

        for (PixelLocation spectrumLocation : spectraLocations) {
            if (spectrumLocation.getX() > xPixels) {
                xPixels = spectrumLocation.getX();
            }
            if (spectrumLocation.getY() > yPixels) {
                yPixels = spectrumLocation.getY();
            }
            if (spectrumLocation.getZ() > zPixels) {
                zPixels = spectrumLocation.getZ();
            }
        }

        return spectraLocations;
    }
}
