/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import static com.alanmrace.jimzmlconverter.ImzMLConverter.getOBOTerm;
import com.alanmrace.jimzmlconverter.exceptions.ConversionException;
import com.alanmrace.jimzmlparser.exceptions.FatalParseException;
import com.alanmrace.jimzmlparser.exceptions.FatalParseIssue;
import com.alanmrace.jimzmlparser.exceptions.FatalRuntimeParseException;
import com.alanmrace.jimzmlparser.exceptions.MzMLParseException;
import com.alanmrace.jimzmlparser.imzml.ImzML;
import com.alanmrace.jimzmlparser.imzml.PixelLocation;
import com.alanmrace.jimzmlparser.mzml.BinaryDataArray;
import com.alanmrace.jimzmlparser.mzml.CVParam;
import com.alanmrace.jimzmlparser.mzml.EmptyCVParam;
import com.alanmrace.jimzmlparser.mzml.MzML;
import com.alanmrace.jimzmlparser.mzml.ReferenceableParamGroup;
import com.alanmrace.jimzmlparser.mzml.ReferenceableParamGroupRef;
import com.alanmrace.jimzmlparser.mzml.Scan;
import com.alanmrace.jimzmlparser.mzml.ScanSettings;
import com.alanmrace.jimzmlparser.mzml.Spectrum;
import com.alanmrace.jimzmlparser.mzml.SpectrumList;
import com.alanmrace.jimzmlparser.parser.MzMLHeaderHandler;
import com.alanmrace.jimzmlparser.writer.ImzMLWriter;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alan
 */
public class MzMLToImzMLConverter extends ImzMLConverter {

    FileStorage fileStorage;

    CVParam lineScanDirection;

    protected int x, y;

    String coordsFilename;

    public enum FileStorage {

        rowPerFile,
        oneFile,
        pixelPerFile
    }

    public enum Direction {
        LeftToRight,
        RightToLeft,
        TopToBottom,
        BottomToTop
    }

    Direction primaryDirection, secondaryDirection;

    public MzMLToImzMLConverter(String outputFilename, String[] inputFilenames, FileStorage fileStorage) {
        super(outputFilename, inputFilenames);

        this.fileStorage = fileStorage;

        //lineScanDirection = new EmptyCVParam(getOBOTerm(ScanSettings.LINE_SCAN_DIRECTION_LEFT_RIGHT_ID));

    }

    public void setDirections(Direction primaryDirection, Direction secondaryDirection) {
        this.primaryDirection = primaryDirection;
        this.secondaryDirection = secondaryDirection;

        // TODO: Set lineScanDirection
    }

    public void setCoordsFile(String filename) {
        this.coordsFilename = filename;
    }

    public void setFileStorage(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    public void setLineScanDirection(CVParam lineScanDirection) {
        this.lineScanDirection = lineScanDirection;
    }

    public void setPixelPerFile(int x, int y) {
        fileStorage = FileStorage.pixelPerFile;

        this.x = x;
        this.y = y;
    }

    @Override
    protected void generatePixelLocations() {
        if (baseImzML == null) {
            generateBaseImzML();
        }

        if (coordsFilename != null) {
            try {
//                if(coordsFilename.endsWith("csv")) {
//                    pixelLocations = getPixelLocationFromCSVFile(coordsFilename);
//                } else {
                    pixelLocations = getPixelLocationFromTextFile(coordsFilename, getNumberSpectraPerPixel(baseImzML.getRun().getSpectrumList()));
//                }
            } catch (ConversionException ex) {
                Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            int spectraPerPixel = getNumberSpectraPerPixel(baseImzML.getRun().getSpectrumList());

            switch (fileStorage) {
                case pixelPerFile:
                    if (x == 0 || y == 0) {
                        // Try and make the image square
                        x = y = (int) Math.ceil(Math.sqrt(inputFilenames.length));
                    }


                    pixelLocations = new PixelLocation[y * x * spectraPerPixel];

                    for (int i = 0; i < y; i++) {
                        for (int j = 0; j < x; j++) {
                            for (int k = 0; k < spectraPerPixel; k++) {
                                pixelLocations[i * x * spectraPerPixel + j * spectraPerPixel + k] = new PixelLocation(j + 1, i + 1, 1);
                            }
                        }
                    }

                    break;
                case oneFile:
                    int numSpectra = baseImzML.getRun().getSpectrumList().size();
                    
                    pixelLocations = new PixelLocation[numSpectra * spectraPerPixel];
                    
                    for(int i = 0; i < numSpectra; i++) {
                        for (int k = 0; k < spectraPerPixel; k++) {
                            pixelLocations[i * spectraPerPixel + k] = new PixelLocation(i + 1, 1, 1);
                        }
                    }
                    
                    break;
                case rowPerFile:
                    int spectraInFile = baseImzML.getRun().getSpectrumList().size();
                    int numFiles = this.inputFilenames.length;
                    int specrumIndex = 0;

                    pixelLocations = new PixelLocation[spectraInFile * numFiles * spectraPerPixel];

                    if((primaryDirection == Direction.LeftToRight || primaryDirection == Direction.RightToLeft) && (secondaryDirection == Direction.TopToBottom || secondaryDirection == Direction.BottomToTop)) {
                        int startY, endY, startX, endX, yDirection, xDirection;

                        if(secondaryDirection == Direction.TopToBottom) {
                            startY = 0;
                            endY = inputFilenames.length;
                            yDirection = 1;
                        } else {
                            startY = inputFilenames.length - 1;
                            endY = -1;
                            yDirection = -1;
                        }

                        try {
                            for (int y = startY; y != endY; y += yDirection) {
                                MzML mzML = MzMLHeaderHandler.parsemzMLHeader(inputFilenames[y]);

                                if(primaryDirection == Direction.LeftToRight) {
                                    startX = 0;
                                    endX = mzML.getRun().getSpectrumList().size();
                                    xDirection = 1;
                                } else {
                                    startX = mzML.getRun().getSpectrumList().size() - 1;
                                    endX = -1;
                                    xDirection = -1;
                                }

                                for (int x = startX; x != endX; x += xDirection) {
                                    for (int k = 0; k < spectraPerPixel; k++) {
                                        pixelLocations[specrumIndex++] = new PixelLocation(x + 1, y + 1, 1);
                                        //(y * spectraInFile * spectraPerPixel) + (x * spectraPerPixel) + k
                                    }
                                }
                            }
                        } catch (MzMLParseException e) {
                            throw new FatalRuntimeParseException(new FatalParseIssue(e.getLocalizedMessage()));
                        }
                    } else {
                        throw new UnsupportedOperationException("Unsupported set of directions: " + primaryDirection + ", " + secondaryDirection);
                    }

                    break;
                default:
                    //                pixelLocations = new PixelLocation[inputFilenames.length][baseImzML.getRun().getSpectrumList().size()];
                    break;

            }
        }
    }

    //public static PixelLocation[] getPixelLocationFromCSVFile(String spectrumLocationFile) {
    //    throw new UnsupportedOperationException("Not supported yet.");
    //}
    
    public static PixelLocation[] getPixelLocationFromTextFile(String spectrumLocationFile, int numSpectraPerPixel) throws ConversionException {
        int xPixels = 0;
        int yPixels = 0;
        int zPixels = 0;

        ArrayList<PixelLocation> location = new ArrayList<>();

        BufferedReader in;

        try {
            in = new BufferedReader(new FileReader(spectrumLocationFile));
        } catch (FileNotFoundException e) {
            throw new ConversionException("Could not find file " + spectrumLocationFile);
        }

        String line;

        try {
            while ((line = in.readLine()) != null) {
                String[] coords;

                if (line.contains(",")) {
                    coords = line.split(",");
                } else {
                    coords = line.split("\\s+");
                }

                int curX = Integer.parseInt(coords[0].trim());
                int curY = Integer.parseInt(coords[1].trim());
                int curZ = 1;

                if (coords.length == 3) {
                    curZ = Integer.parseInt(coords[2].trim());
                }

                if (curX > xPixels) {
                    xPixels = curX;
                }
                if (curY > yPixels) {
                    yPixels = curY;
                }
                if (curZ > zPixels) {
                    zPixels = curZ;
                }

                location.add(new PixelLocation(curX, curY, curZ));
            }

            in.close();
        } catch (IOException e) {
            throw new ConversionException("Error reading from " + spectrumLocationFile + ". " + e.getLocalizedMessage());
        }

        PixelLocation[] spectrumLocation = new PixelLocation[location.size() * numSpectraPerPixel];

        for (int i = 0; i < location.size(); i++) {
            for (int j = 0; j < numSpectraPerPixel; j++) {
                spectrumLocation[i * numSpectraPerPixel + j] = location.get(i);
            }
        }

        return spectrumLocation;
    }

    public static int getNumberSpectraPerPixel(SpectrumList spectrumList) {
        // Check the number of spectra that have the same scan start time. This is
        double originalScanStartTime = -1;
        int numSpectraPerPixel = 0;

        for (Spectrum spectrum : spectrumList) {
            CVParam scanStartTimeParam = spectrum.getScanList().getScan(0).getCVParam(Scan.SCAN_START_TIME_ID);

            // If there is no scan start time recorded, we cannot determine if there were multiple scans acquried at
            // the same pixel location
            if(scanStartTimeParam == null) {
                numSpectraPerPixel = 1;
                break;
            }

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
        
        //if(numSpectraPerPixel > 1000)
        //    numSpectraPerPixel = 1;

        Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.INFO, "Detected {0} spectra per pixel", numSpectraPerPixel);
        
        return numSpectraPerPixel;
    }

    @Override
    protected void generateBaseImzML() {
        try {
            MzML mzML = MzMLHeaderHandler.parsemzMLHeader(inputFilenames[0], false);

            Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.INFO, "Finished parsing mzML Header");

            baseImzML = new ImzML(mzML);
        } catch (FatalParseException ex) {
            Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected String getConversionDescription() {
        return "Conversion from mzML to imzML";
    }

    protected void superconvert() throws ConversionException {
        super.convert();
    }
    
    @Override
    public void convert() throws ConversionException {
        super.convert();

        int x = 1;
        int y = 1;

        int maxX = x;
        int maxY = y;

        int currentPixelLocation = 0;

        // Add all necessary spectra to the base imzML
        int currentmzMLFile = 0;

        for (String mzMLFilename : inputFilenames) {
            try {
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

                /*if (fileStorage == FileStorage.rowPerFile) {
                    int xDirection = -1;
                    int startValue = numSpectra;
                    int endValue = -1;

                    if (ScanSettings.LINE_SCAN_DIRECTION_LEFT_RIGHT_ID.equals(lineScanDirection.getTerm().getID())) {
                        xDirection = 1;
                        startValue = 1;
                        endValue = numSpectra;
                    }

                    this.pixelLocations = new PixelLocation[numSpectra];
                    currentPixelLocation = 0;

                    for (int index = startValue; index * xDirection <= endValue; index += xDirection) {
                        pixelLocations[index - 1] = new PixelLocation(x, y, 1);
                        x++;
                    }

                    x = 1;
                    y++;
                }*/

                for (int i = 0; i < numSpectra; i++) {
                    if (currentPixelLocation >= pixelLocations.length) {
                        Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.SEVERE, "Current pixel location index exceeds the number of pixel locations specified");

                        break;
                    }

                    Spectrum spectrum = currentmzML.getRun().getSpectrumList().getSpectrum(i);

                    int spectrumX = pixelLocations[currentPixelLocation].getX();
                    int spectrumY = pixelLocations[currentPixelLocation].getY();

                    currentPixelLocation++;

                    if (spectrumX == -1 || spectrumY == -1) {
                        continue;
                    }
                    
                    // TODO: NEEDS TO BE REMOVED
//                    try {
//                        spectrum.getmzArray();
//                    } catch (IOException ex) {
//                        Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                    
                    for(BinaryDataArray binaryDataArray : spectrum.getBinaryDataArrayList()) {
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
                    
                    baseImzML.addSpectrum(spectrum);
                    
                    setCoordinatesOfSpectrum(spectrum, spectrumX, spectrumY);

                    if (spectrumX > maxX) {
                        maxX = spectrumX;
                    }
                    if (spectrumY > maxY) {
                        maxY = spectrumY;
                    }
                    
                    if (fileStorage == FileStorage.pixelPerFile) {
                        break;
                    }
                }

                Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.FINEST, "About to close mzML in convert()");
                // TODO: Figure out a way around this?
                //currentmzML.close();
            } catch (FatalParseException ex) {
                Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);

                throw new ConversionException("MzMLParseException: " + ex, ex);
            }
        }

        
//        for(Spectrum spectrum : baseImzML.getSpectrumList()) {
//            System.out.println(spectrum);
//            System.out.println(spectrum.getBinaryDataArrayList().size() + " BDA");
//        }
        
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

//        // Open the .ibd data stream
//        DataOutputStream binaryDataStream;
//
//        try {
//            binaryDataStream = new DataOutputStream(new FileOutputStream(outputFilename + ".ibd"));
//
//            // Default to 'Processed' data
//            baseImzML.getFileDescription().getFileContent().removeChildOfCVParam(FileContent.binaryTypeID);
//            baseImzML.getFileDescription().getFileContent().addCVParam(new EmptyCVParam(getOBOTerm(FileContent.binaryTypeProcessedID)));
//
//            
//            String uuid = UUID.randomUUID().toString().replace("-", "");
//            System.out.println(uuid);
//
//            // Add UUID to the imzML file
//            baseImzML.getFileDescription().getFileContent().removeChildOfCVParam(FileContent.ibdIdentificationID);
//            baseImzML.getFileDescription().getFileContent().addCVParam(new StringCVParam(getOBOTerm(FileContent.uuidIdntificationID), uuid));
//
//            try {
//                binaryDataStream.write(hexStringToByteArray(uuid));
//            } catch (IOException e2) {
//                try {
//                    binaryDataStream.close();
//
//                    throw new ConversionException("Error writing UUID " + e2.getLocalizedMessage(), e2);
//                } catch (IOException e) {
//                    throw new ConversionException("Error closing .ibd file after failing writing UUID " + e.getLocalizedMessage(), e);
//                }
//            }
//
//            long offset = binaryDataStream.size();
//            int currentmzMLFile = 0;
//
//            for (String mzMLFilename : inputFilenames) {
//                try {
//                    MzML currentmzML = MzMLHeaderHandler.parsemzMLHeader(mzMLFilename);
//
//                    // TODO: Add all referenceParamGoups - TEMPORARY FIX
//                    for (ReferenceableParamGroup rpg : currentmzML.getReferenceableParamGroupList()) {
//                        baseImzML.getReferenceableParamGroupList().addReferenceableParamGroup(rpg);
//                    }
//
//                    String filenameID = "mzML" + currentmzMLFile++;
//                    addSourceFileToImzML(baseImzML, mzMLFilename, filenameID, currentmzML.getFileDescription());
//
//                    SpectrumList spectrumList = currentmzML.getRun().getSpectrumList();
//                    int numSpectra = spectrumList.size();
//
//                    if (fileStorage == FileStorage.rowPerFile) {
//                        int xDirection = -1;
//                        int startValue = numSpectra;
//                        int endValue = -1;
//
//                        if (lineScanDirection.getTerm().getID().equals(ScanSettings.lineScanDirectionLeftRightID)) {
//                            xDirection = 1;
//                            startValue = 1;
//                            endValue = numSpectra;
//                        }
//
//                        this.pixelLocations = new PixelLocation[numSpectra];
//                        currentPixelLocation = 0;
//
//                        for (int index = startValue; index * xDirection <= endValue; index += xDirection) {
//                            pixelLocations[index - 1] = new PixelLocation(x, y, 1);
//                            x++;
//                        }
//
//                        x = 1;
//                        y++;
//                    }
//
//                    for (int i = 0; i < numSpectra; i++) {
//                        if(currentPixelLocation >= pixelLocations.length) {
//                            Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.SEVERE, "Current pixel location index exceeds the number of pixel locations specified");
//                            
//                            break;
//                        }
//                        
//                        Spectrum spectrum = currentmzML.getRun().getSpectrumList().getSpectrum(i);
//                        int spectrumX = pixelLocations[currentPixelLocation].getX();
//                        int spectrumY = pixelLocations[currentPixelLocation].getY();
//                        
//                        currentPixelLocation++;
//                        
//                        if(spectrumX == -1 || spectrumY == -1)
//                            continue;
//
//                        // TODO: REMOVE THIS FOR SPEED INCREASE WHEN WORKAROUND IS IMPLEMENTED
//                        double[] mzs = spectrum.getmzArray();
//
//                        // TODO: REMOVE EMPTY SPECTRA FROM imzML OPTION
//                        //if(mzs == null || mzs.length == 0) 
//                        //    continue;
//                        
//                        offset = copySpectrumToImzML(baseImzML, spectrum, binaryDataStream, offset);
//                        setCoordinatesOfSpectrum(spectrum, spectrumX, spectrumY);
//
//                        if (spectrumX > maxX) {
//                            maxX = spectrumX;
//                        }
//                        if (spectrumY > maxY) {
//                            maxY = spectrumY;
//                        }
//                    }
//
//                    Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.FINEST, "About to close mzML in convert()");
//                    currentmzML.close();
//                } catch (MzMLParseException ex) {
//                    Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
//                    
//                    throw new ConversionException("MzMLParseException: " + ex, ex);
//                } catch (ArrayIndexOutOfBoundsException ex) {
//                    Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//
//            outputFullmzList(binaryDataStream, offset);
//            
//            binaryDataStream.close();
//        } catch (IOException ex) {
//            Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
//            
//            throw new ConversionException("Error closing " + outputFilename + ".ibd", ex);
//        }
//
//        if (removeEmptySpectra) {
//            removeEmptySpectraFromImzML(baseImzML);
//        }
//
//        setImzMLImageDimensions(baseImzML, maxX, maxY);
//
//        String sha1Hash = calculateSHA1(outputFilename + ".ibd");
//        baseImzML.getFileDescription().getFileContent().removeChildOfCVParam(FileContent.ibdChecksumID);
//
//        System.out.println("SHA-1 Hash: " + sha1Hash);
//        baseImzML.getFileDescription().getFileContent().addCVParam(new StringCVParam(getOBOTerm(FileContent.sha1ChecksumID), sha1Hash));
//
//        // Output the imzML portion of the data
//        ImzMLHeaderWriter imzMLWriter = new ImzMLHeaderWriter();
//        
//        try {
//            imzMLWriter.write(baseImzML, outputFilename);
//        } catch (IOException ex) {
//            Logger.getLogger(ImzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
////        try {
////            baseImzML.write(outputFilename + ".imzML");
////        } catch (ImzMLWriteException ex) {
////            Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
////        }
//
//        baseImzML.close();
    }

    /*    public static void main(String args[]) throws IOException, ImzMLConversionException {
     String wiffFile = "D:\\Test\\Data7_1_2011-acc0.1_cyc10.wiff";
     wiffFile = "D:\\Rory\\SampleData\\2012_5_2_medium(120502,20h18m).wiff";

     long startTime = System.currentTimeMillis();
     File[] mzMLFiles = WiffTomzMLConverter.convert(wiffFile);
     String[] mzMLFilepaths = new String[mzMLFiles.length];

     for (int i = 0; i < mzMLFiles.length; i++) {
     mzMLFilepaths[i] = mzMLFiles[i].getAbsolutePath();
     }

     long end1Time = System.currentTimeMillis();

     MzMLToImzMLConverter converter = new MzMLToImzMLConverter(wiffFile, mzMLFilepaths, FileStorage.rowPerFile);
     converter.setFileStorage(FileStorage.rowPerFile);

     converter.convert();

     long end2Time = System.currentTimeMillis();

     System.out.println("Conversion to mzML took: " + (end1Time - startTime) + " ms");
     System.out.println("Conversion to imzML took: " + (end2Time - end1Time) + " ms");
     }*/
}
