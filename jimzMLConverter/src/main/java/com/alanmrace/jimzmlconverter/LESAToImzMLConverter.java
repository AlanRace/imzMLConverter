/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import static com.alanmrace.jimzmlconverter.ImzMLConverter.addSourceFileToImzML;
import static com.alanmrace.jimzmlconverter.ImzMLConverter.setCoordinatesOfSpectrum;
import com.alanmrace.jimzmlconverter.exceptions.ConversionException;
import com.alanmrace.jimzmlparser.exceptions.FatalParseException;
import com.alanmrace.jimzmlparser.imzml.PixelLocation;
import com.alanmrace.jimzmlparser.mzml.BinaryDataArray;
import com.alanmrace.jimzmlparser.mzml.DataProcessing;
import com.alanmrace.jimzmlparser.mzml.MzML;
import com.alanmrace.jimzmlparser.mzml.ReferenceableParamGroup;
import com.alanmrace.jimzmlparser.mzml.ReferenceableParamGroupRef;
import com.alanmrace.jimzmlparser.mzml.Spectrum;
import com.alanmrace.jimzmlparser.mzml.SpectrumList;
import com.alanmrace.jimzmlparser.parser.MzMLHeaderHandler;
import com.alanmrace.jimzmlparser.writer.ImzMLWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alan
 */
public class LESAToImzMLConverter extends MzMLToImzMLConverter {
    
    private static final Logger logger = Logger.getLogger(LESAToImzMLConverter.class.getName());
    
    String csvFileLocation;
    double sumScansPPMTolerance;
    int lesaStepSize;
    
    public LESAToImzMLConverter(String outputFilename, String[] inputFilenames, FileStorage fileStorage) {
        super(outputFilename, inputFilenames, fileStorage);
    }
    
    public void setCSVFile(String csvFileLocation) {
        this.csvFileLocation = csvFileLocation;
    }
    
    public void setSumScansPPMTolerance(double sumScansPPMTolerance) {
        this.sumScansPPMTolerance = sumScansPPMTolerance;
    }
    
    public void setLESAStepSize(int lesaStepSize) {
        this.lesaStepSize = lesaStepSize;
    }
    
    @Override
    public void convert() throws ConversionException {
        if(fileStorage != FileStorage.pixelPerFile)
            super.convert();
        else {
            super.superconvert();
            
            int x = 1;
            int y = 1;

            int maxX = x;
            int maxY = y;

            int currentPixelLocation = 0;

            // Add all necessary spectra to the base imzML
            int currentmzMLFile = 0;

            for (String mzMLFilename : inputFilenames) {
                try {
                    if(currentPixelLocation >= pixelLocations.length) {
                        Logger.getLogger(LESAToImzMLConverter.class.getName()).log(Level.SEVERE, "Too many mzML files found ({0}) for the number of pixels specified ({1})", new Object[]{inputFilenames.length, pixelLocations.length});
                        
                        break;
                    }
                    
                    MzML currentmzML = MzMLHeaderHandler.parsemzMLHeader(mzMLFilename, true);

                    // TODO: Add all referenceParamGoups - TEMPORARY FIX
                    for (ReferenceableParamGroup rpg : currentmzML.getReferenceableParamGroupList()) {
                        if(!baseImzML.getReferenceableParamGroupList().contains(rpg)) {
                            // Check if the ID is the same as one already in the list
                            if(baseImzML.getReferenceableParamGroupList().containsID(rpg.getID())) {
                                rpg.setID(rpg.getID() + "_");
                            }

                            baseImzML.getReferenceableParamGroupList().addReferenceableParamGroup(rpg);
                        }
                    }

                    String filenameID = "mzML" + currentmzMLFile++;
                    addSourceFileToImzML(baseImzML, mzMLFilename, filenameID, currentmzML.getFileDescription());

                    SpectrumList spectrumList = currentmzML.getRun().getSpectrumList();
                    int numSpectra = spectrumList.size();
                    
                    if(numSpectra > 0) {
                        Spectrum baseSpectrum = currentmzML.getRun().getSpectrumList().getSpectrum(0);

                        int spectrumX = pixelLocations[currentPixelLocation].getX();
                        int spectrumY = pixelLocations[currentPixelLocation].getY();

                        currentPixelLocation++;

                        if (spectrumX == -1 || spectrumY == -1) {
                            continue;
                        }

                        for(BinaryDataArray binaryDataArray : baseSpectrum.getBinaryDataArrayList()) {
                            binaryDataArray.removeChildrenOfCVParam(BinaryDataArray.COMPRESSION_TYPE_ID, false);
                            binaryDataArray.removeChildrenOfCVParam(BinaryDataArray.BINARY_DATA_TYPE_ID, false);
                            binaryDataArray.removeCVParam(BinaryDataArray.EXTERNAL_DATA_ID);

                            if(binaryDataArray.ismzArray()) {
                                binaryDataArray.addReferenceableParamGroupRef(new ReferenceableParamGroupRef(rpgmzArray));
                                binaryDataArray.removeCVParam(BinaryDataArray.MZ_ARRAY_ID);
                            } else if(binaryDataArray.isIntensityArray()) {
                                binaryDataArray.addReferenceableParamGroupRef(new ReferenceableParamGroupRef(rpgintensityArray));
                                binaryDataArray.removeCVParam(BinaryDataArray.INTENSITY_ARRAY_ID);
                            }
                        }

                        // TODO: Generate a new chromatogram?                    

                        baseImzML.addSpectrum(baseSpectrum);

                        setCoordinatesOfSpectrum(baseSpectrum, spectrumX, spectrumY);

                        if (spectrumX > maxX) {
                            maxX = spectrumX;
                        }
                        if (spectrumY > maxY) {
                            maxY = spectrumY;
                        }
                        
                        if(sumScansPPMTolerance >= 0) {
                            double[] mzs = baseSpectrum.getmzArray();
                            double[] intensities = baseSpectrum.getIntensityArray();

                            //Map<Double, Double> spectrumMap = new HashMap<>();
                            ArrayList<Double> mzArray = new ArrayList<>();
                            ArrayList<Double> intensityArray = new ArrayList<>();
                            
                            for(int i = 0; i < mzs.length; i++) {
                                mzArray.add(mzs[i]); //, intensities[i]);
                                intensityArray.add(intensities[i]);
                            }
                                
                            for (int i = 1; i < numSpectra; i++) {
                                Spectrum currentSpectrum = currentmzML.getRun().getSpectrumList().getSpectrum(i);
                                mzs = currentSpectrum.getmzArray();
                                intensities = currentSpectrum.getIntensityArray();
                                
                                int mzArrayIndex = 0;
                                
                                for(int dataIndex = 0; dataIndex < mzs.length; dataIndex++) {
                                    double previousDifferance = Double.MAX_VALUE;
                                    
                                    double difference = mzs[dataIndex] - mzArray.get(mzArrayIndex);
                                    double ppmDifference = difference / mzs[dataIndex] * 1e6;
                                    
                                    while(difference > 0 && Math.abs(ppmDifference) > sumScansPPMTolerance && mzArrayIndex < mzArray.size() - 2) {
                                        mzArrayIndex++;
                                        previousDifferance = difference;
                                        difference = mzs[dataIndex] - mzArray.get(mzArrayIndex); 
                                        ppmDifference = difference / mzs[dataIndex] * 1e6;
                                    }
                                    
                                    do {
                                        if(Math.abs(ppmDifference) > sumScansPPMTolerance)
                                            break;
                                        if(mzArrayIndex >= mzArray.size() - 1)
                                            break;
                                        
                                        mzArrayIndex++;
                                        previousDifferance = difference;
                                        difference = mzs[dataIndex] - mzArray.get(mzArrayIndex); 
                                        ppmDifference = difference / mzs[dataIndex] * 1e6;
                                    } while(difference >= 0);
                                    
                                    if(Math.abs(previousDifferance) < Math.abs(difference)) {
                                        mzArrayIndex--;
                                        difference = previousDifferance;
                                        ppmDifference = difference / mzs[dataIndex] * 1e6;
                                    }
                                    
                                    if(Math.abs(ppmDifference) < sumScansPPMTolerance) {
                                        //System.out.println("(2) Matched " + mzs[dataIndex] + " with " + mzArray.get(mzArrayIndex) + " (" + ppmDifference + ") at " + mzArrayIndex);
                                        //System.out.println(previousDifferance + ", " + difference);
                                        intensityArray.set(mzArrayIndex, intensityArray.get(mzArrayIndex) + intensities[dataIndex]);
                                    } else {
                                        //if(mzs[dataIndex] < mzArray.get(mzArrayIndex))
                                        //    mzArrayIndex--;
                                        if(mzs[dataIndex] > mzArray.get(mzArrayIndex))
                                            mzArrayIndex++;
                                        
                                        mzArray.add(mzArrayIndex, mzs[dataIndex]);
                                        intensityArray.add(mzArrayIndex, intensities[dataIndex]);
                                        //System.out.println("(2) Added " + mzs[dataIndex] + " at " + mzArrayIndex);
                                    }
                                }
                                //System.out.println("Processing spectrum..");
                            }
                            
                            double[] finalmzArray = new double[mzArray.size()];
                            double[] finalIntensityArray = new double[intensityArray.size()];
                            
                            for(int mzIndex = 0; mzIndex < mzArray.size(); mzIndex++) {
                                finalmzArray[mzIndex] = mzArray.get(mzIndex);
                                finalIntensityArray[mzIndex] = intensityArray.get(mzIndex) / numSpectra;
                            }
                            
                            baseSpectrum.updateSpectralData(finalmzArray, finalIntensityArray, DataProcessing.create());
                            
                            Logger.getLogger(LESAToImzMLConverter.class.getName()).log(Level.FINEST, "About to close mzML in convert()");
                        }
                    }
                } catch (FatalParseException ex) {
                    Logger.getLogger(LESAToImzMLConverter.class.getName()).log(Level.SEVERE, "Failed on file " + mzMLFilename);
                    Logger.getLogger(LESAToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);

                    throw new ConversionException("MzMLParseException: " + ex, ex);
                } catch (ArrayIndexOutOfBoundsException | IOException ex) {
                    Logger.getLogger(LESAToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }


        // If the chromatogram list is empty, then remove it to comply with the (i)mzML standard
            if(baseImzML.getRun().getChromatogramList().size() == 0)
                baseImzML.getRun().setChromatogramList(null);

            ImzMLWriter writer = new ImzMLWriter();
            try {
                writer.write(baseImzML, outputFilename + ".imzML");
            } catch (IOException ex) {
                Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
            }

            baseImzML.close();
        }
    }
    
    @Override
    protected void generatePixelLocations() {        
        if(coordsFilename != null) {
            super.generatePixelLocations();
        } else {
            if(csvFileLocation != null) {
                try {
                    this.pixelLocations = LESAToImzMLConverter.getPixelLocationFromCSVFile(csvFileLocation, lesaStepSize);
                    
                    logger.log(Level.INFO, "Generated pixel locations from .csv file");
                } catch (ConversionException ex) {
                    Logger.getLogger(LESAToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
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
    
    public static PixelLocation[] getPixelLocationFromCSVFile(String csvFileLocation) throws ConversionException {
        return getPixelLocationFromCSVFile(csvFileLocation, 200);
    }
    
    public static PixelLocation[] getPixelLocationFromCSVFile(String csvFileLocation, int lesaStepSize) throws ConversionException {
        int minStepSize = lesaStepSize;
        
        String line = "";
        String cvsSplitBy = ",";
        
        List<PixelLocation> locations = new ArrayList<>();
        
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFileLocation))) {

            // Skip header
            line = br.readLine();
            
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] lesaLocation = line.split(cvsSplitBy);

                PixelLocation newLocation = new PixelLocation(Integer.parseInt(lesaLocation[1]), Integer.parseInt(lesaLocation[2]), 1);
                
                locations.add(newLocation);

                if(newLocation.getX() > maxX)
                    maxX = newLocation.getX();
                if(newLocation.getY() > maxY)
                    maxY = newLocation.getY();
                
                if(newLocation.getX() < minX)
                    minX = newLocation.getX();
                if(newLocation.getY() < minY)
                    minY = newLocation.getY();
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage());
            
            throw new ConversionException(e.getLocalizedMessage(), e);
        }
        
//        int numSpectraPerPixel = getNumberSpectraPerPixel(oldSpectrumList);
        
//        logger.log(Level.INFO, MessageFormat.format("Found {0} spectra per pixel", numSpectraPerPixel));

        

        System.out.println("LESA coordinate limits found x: " + minX + " - " + maxX + ", y: " + minY + " - " + maxY);
        
        
        PixelLocation[] spectraLocations = new PixelLocation[locations.size()];
        int currentIndex = 0;
        
        for(PixelLocation location : locations) {
            spectraLocations[currentIndex++] = new PixelLocation(((location.getX() - minX) / minStepSize) + 1, ((location.getY() - minY) / minStepSize) + 1, 1);
        }

//        for(int y = 0; y < maxY; y++) {
//            for(int x = 0; x < maxX; x++) {
//                int index = y * maxX*numSpectraPerPixel + x * numSpectraPerPixel;
//                
//                for(int n = 0; n < numSpectraPerPixel; n++) {
//                    spectraLocations[index + n] = new PixelLocation(x+1, y+1, 1);
//                }
//            }
//        }
        
        return spectraLocations;
    }
}
