/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlconverter.Waters.PatternDefinitionHandler;
import com.alanmrace.jimzmlconverter.exceptions.ImzMLConversionException;
import com.alanmrace.jimzmlparser.imzML.PixelLocation;
import com.alanmrace.jimzmlparser.mzML.CVParam;
import com.alanmrace.jimzmlparser.mzML.Scan;
import com.alanmrace.jimzmlparser.mzML.Spectrum;
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
 * @author amr1
 */
public class WatersMzMLToImzMLConverter extends MzMLToImzMLConverter {
    
    private static final Logger logger = Logger.getLogger(WatersMzMLToImzMLConverter.class.getName());
    
    String patternFile;
    
    public WatersMzMLToImzMLConverter(String outputFilename, String[] inputFilenames, FileStorage fileStorage) {
        super(outputFilename, inputFilenames, fileStorage);
    }
    
    public void setPatternFile(String patternFile) {
        this.patternFile = patternFile;
    }
    
    @Override
    protected void generatePixelLocations() {
        if(patternFile != null) {
            try {
                this.pixelLocations = WatersMzMLToImzMLConverter.getPixelLocationFromWatersFile(patternFile, baseImzML.getRun().getSpectrumList());
            } catch (ImzMLConversionException ex) {
                logger.log(Level.SEVERE, null, ex); 
            }
        } else {
            super.generatePixelLocations();
        }
    }
    
    public static PixelLocation[] getPixelLocationFromWatersFile(String patternFile, SpectrumList oldSpectrumList) throws ImzMLConversionException {
        int xPixels = 0;
        int yPixels = 0;
        int zPixels = 0;

        // Convert mzML header information -> imzML
        PatternDefinitionHandler handler = new PatternDefinitionHandler();
        File patternF = new File(patternFile);

        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse(patternF, handler);

        } catch (SAXException | IOException | ParserConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        // Check the number of spectra that have the same scan start time. This is
        double originalScanStartTime = -1;
        int numSpectraPerPixel = 0;

        for (Spectrum spectrum : oldSpectrumList) {
            CVParam scanStartTimeParam = spectrum.getScanList().getScan(0).getCVParam(Scan.scanStartTimeID);
            double scanStartTime = scanStartTimeParam.getValueAsDouble();

            if (originalScanStartTime == -1) {
                originalScanStartTime = scanStartTime;
            }

            if (originalScanStartTime == scanStartTime) {
                numSpectraPerPixel++;
            } else {
                break;
            }
        }
        
        logger.log(Level.INFO, MessageFormat.format("Found {0} spectra per pixel", numSpectraPerPixel));

        PixelLocation[] spectrumLocation = handler.getPatternDefinition().convertToPixelLocations(oldSpectrumList, numSpectraPerPixel);

        for (int i = 0; i < spectrumLocation.length; i++) {
            if (spectrumLocation[i].getX() > xPixels) {
                xPixels = spectrumLocation[i].getX();
            }
            if (spectrumLocation[i].getY() > yPixels) {
                yPixels = spectrumLocation[i].getY();
            }
            if (spectrumLocation[i].getZ() > zPixels) {
                zPixels = spectrumLocation[i].getZ();
            }
        }

        return spectrumLocation;
    }

    
}
