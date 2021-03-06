/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlconverter.exceptions.ConversionException;
import com.alanmrace.jimzmlparser.data.DataTypeTransform.DataType;
import com.alanmrace.jimzmlparser.imzml.ImzML;
import com.alanmrace.jimzmlparser.imzml.PixelLocation;
import com.alanmrace.jimzmlparser.mzml.BinaryDataArray;
import com.alanmrace.jimzmlparser.mzml.BinaryDataArray.CompressionType;
import com.alanmrace.jimzmlparser.mzml.BinaryDataArrayList;
import com.alanmrace.jimzmlparser.mzml.BooleanCVParam;
import com.alanmrace.jimzmlparser.mzml.CV;
import com.alanmrace.jimzmlparser.mzml.CVParam;
import com.alanmrace.jimzmlparser.mzml.ChromatogramList;
import com.alanmrace.jimzmlparser.mzml.DataProcessing;
import com.alanmrace.jimzmlparser.mzml.EmptyCVParam;
import com.alanmrace.jimzmlparser.mzml.FileDescription;
import com.alanmrace.jimzmlparser.mzml.LongCVParam;
import com.alanmrace.jimzmlparser.mzml.ProcessingMethod;
import com.alanmrace.jimzmlparser.mzml.ReferenceableParamGroup;
import com.alanmrace.jimzmlparser.mzml.ReferenceableParamGroupList;
import com.alanmrace.jimzmlparser.mzml.Scan;
import com.alanmrace.jimzmlparser.mzml.ScanList;
import com.alanmrace.jimzmlparser.mzml.ScanSettings;
import com.alanmrace.jimzmlparser.mzml.ScanSettingsList;
import com.alanmrace.jimzmlparser.mzml.Software;
import com.alanmrace.jimzmlparser.mzml.SourceFile;
import com.alanmrace.jimzmlparser.mzml.SourceFileList;
import com.alanmrace.jimzmlparser.mzml.Spectrum;
import com.alanmrace.jimzmlparser.mzml.SpectrumList;
import com.alanmrace.jimzmlparser.mzml.StringCVParam;
import com.alanmrace.jimzmlparser.obo.OBO;
import com.alanmrace.jimzmlparser.obo.OBOTerm;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alan
 */
public abstract class ImzMLConverter implements Converter {

    private static final Logger logger = Logger.getLogger(ImzMLConverter.class.getName());

    public static final String version = "2.0.0";
    public static final String IMZMLCONVERTER_ID = "IMS:1000502";

    protected double progress;

    protected static OBO obo;

    protected ImzML baseImzML;

    protected String outputFilename;
    protected String[] inputFilenames;

    protected PixelLocation[] pixelLocations;

    protected CVParam mzArrayCompressionType;
    protected CVParam intenstiyArrayCompressionType;
    protected CVParam mzArrayDataType;
    protected CVParam intensityArrayDataType;

    protected boolean removeEmptySpectra;

    protected Software imzMLConverter;

    protected ReferenceableParamGroup rpgmzArray;
    protected ReferenceableParamGroup rpgintensityArray;

    protected boolean includeGlobalmzList;
    protected HashSet<Double> fullmzList;

    public ImzMLConverter(String outputFilename, String[] inputFilenames) {
        this.outputFilename = outputFilename;
        this.inputFilenames = inputFilenames;

        // Set up defaults
        mzArrayCompressionType = new EmptyCVParam(getOBOTerm(BinaryDataArray.NO_COMPRESSION_ID));
        intenstiyArrayCompressionType = new EmptyCVParam(getOBOTerm(BinaryDataArray.NO_COMPRESSION_ID));
        mzArrayDataType = new EmptyCVParam(getOBOTerm(BinaryDataArray.DOUBLE_PRECISION_ID));
        intensityArrayDataType = new EmptyCVParam(getOBOTerm(BinaryDataArray.DOUBLE_PRECISION_ID));

        fullmzList = new HashSet<>();
    }

    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }

    public void setIncludeGlobalmzList(boolean includeGlobalmzList) {
        this.includeGlobalmzList = includeGlobalmzList;
    }
    
    public void setOBO(OBO obo) {
        ImzMLConverter.obo = obo;
    }

    // Set the imzML structure that is used to determine the base
    public void setBaseimzML(ImzML baseImzML) {
        this.baseImzML = baseImzML;
    }

    public void setCompressionType(CompressionType compressionType) {
        OBOTerm term = CompressionType.toOBOTerm(compressionType);
//        System.out.println("Setting compression type (" + compressionType + ") to " + term);
        if(term != null) {
            this.mzArrayCompressionType = new EmptyCVParam(term);
            this.intenstiyArrayCompressionType = new EmptyCVParam(term);
        }
    }
    
    public void setmzArrayCompressionType(CompressionType compressionType) {
        OBOTerm term = CompressionType.toOBOTerm(compressionType);
        
        if(term != null) {
            this.mzArrayCompressionType = new EmptyCVParam(term);
        }
    }
    
    public void setintensityArrayCompressionType(CompressionType compressionType) {
        OBOTerm term = CompressionType.toOBOTerm(compressionType);
        
        if(term != null) {
            this.intenstiyArrayCompressionType = new EmptyCVParam(term);
        }
    }
    
    public void setCompressionType(CVParam compressionType) {
        this.mzArrayCompressionType = compressionType;
        this.intenstiyArrayCompressionType = compressionType;
    }
    
    public void setmzArrayCompressionType(CVParam compressionType) {
        this.mzArrayCompressionType = compressionType;
    }
    
    public void setIntensityArrayCompressionType(CVParam compressionType) {
        this.intenstiyArrayCompressionType = compressionType;
    }

    public void setmzArrayDataType(DataType dataType) {
        OBOTerm term = DataType.toOBOTerm(dataType);
        
        if(term != null)
            this.mzArrayDataType = new EmptyCVParam(term);
    }
    
    public void setmzArrayDataType(CVParam mzArrayDataType) {
        this.mzArrayDataType = mzArrayDataType;
    }

    public void setIntensityArrayDataType(DataType dataType) {
        OBOTerm term = DataType.toOBOTerm(dataType);
        
        if(term != null)
            this.intensityArrayDataType = new EmptyCVParam(term);
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
    
    public PixelLocation[] getPixelLocations() {
        if (pixelLocations == null) {
            generatePixelLocations();
        }
        
        return pixelLocations;
    }

//    public abstract void convert();
    public void setBaseImzML(ImzML imzML) {
        this.baseImzML = imzML;
    }

    protected abstract void generateBaseImzML();

    protected abstract String getConversionDescription();

    public void setDirections(MzMLToImzMLConverter.Direction primaryDirection, MzMLToImzMLConverter.Direction secondaryDirection) {}

    protected static final OBOTerm getOBOTerm(String cvParamID) {
        if (obo == null) {
            obo = OBO.getOBO();
        }

        return obo.getTerm(cvParamID);
    }

    protected void generateReferenceableParamArrays() {
        // TODO: MAKE THIS BETTER - ONLY HERE TO PLEASE BIOMAP AND DATACUBE EXPLORER
        ReferenceableParamGroupList rpgList = baseImzML.getReferenceableParamGroupList(); 
        
        if(rpgList == null) {
            rpgList = new ReferenceableParamGroupList(2);
            baseImzML.setReferenceableParamGroupList(rpgList);
        }
        
        rpgmzArray = baseImzML.getReferenceableParamGroupList().getReferenceableParamGroup("mzArray");            

        if (rpgmzArray == null) {
            rpgmzArray = new ReferenceableParamGroup("mzArray");

            rpgmzArray.addCVParam(new EmptyCVParam(getOBOTerm(BinaryDataArray.MZ_ARRAY_ID), getOBOTerm(BinaryDataArray.MZ_ARRAY_UNITS_ID)));
            rpgmzArray.addCVParam(mzArrayDataType);

            baseImzML.getReferenceableParamGroupList().addReferenceableParamGroup(rpgmzArray);
        } else {
            rpgmzArray.removeChildrenOfCVParam(BinaryDataArray.BINARY_DATA_TYPE_ID, false);
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

            rpgintensityArray.addCVParam(new EmptyCVParam(getOBOTerm(BinaryDataArray.INTENSITY_ARRAY_ID), getOBOTerm(BinaryDataArray.INTENSITY_ARRAY_UNITS_NUMBER_OF_COUNTS_ID)));
            rpgintensityArray.addCVParam(intensityArrayDataType);

            baseImzML.getReferenceableParamGroupList().addReferenceableParamGroup(rpgintensityArray);
        } else {
            rpgintensityArray.removeChildrenOfCVParam(BinaryDataArray.BINARY_DATA_TYPE_ID, false);
            rpgintensityArray.addCVParam(intensityArrayDataType);
        }

        // Add in compression type
        rpgmzArray.removeChildrenOfCVParam(BinaryDataArray.COMPRESSION_TYPE_ID, false);
        rpgmzArray.addCVParam(mzArrayCompressionType);
        rpgintensityArray.removeChildrenOfCVParam(BinaryDataArray.COMPRESSION_TYPE_ID, false);
        rpgintensityArray.addCVParam(intenstiyArrayCompressionType);
        
        // 
        rpgmzArray.removeCVParam(BinaryDataArray.EXTERNAL_DATA_ID);
        rpgmzArray.addCVParam(new BooleanCVParam(getOBOTerm(BinaryDataArray.EXTERNAL_DATA_ID), true));
        rpgintensityArray.removeCVParam(BinaryDataArray.EXTERNAL_DATA_ID);
        rpgintensityArray.addCVParam(new BooleanCVParam(getOBOTerm(BinaryDataArray.EXTERNAL_DATA_ID), true));
    }

    protected abstract void generatePixelLocations();
//    protected abstract int getNumberSpectraPerPixel(SpectrumList spectrumList);

    public static void addSourceFileToImzML(ImzML imzML, String filename, String filenameID, FileDescription currentFileDescription) {
        // Add the sourceFile to the sourceFileList
        File file = new File(filename);
        
        // If the file does not have a parent directory then 
        File parentFolder = file.getParentFile();
        String parentDirectory;
        
        if(parentFolder == null)
            parentDirectory = "";
        else
            parentDirectory = parentFolder.toURI().toString();
        

        SourceFile sourceFile = new SourceFile(filenameID, parentDirectory, file.getName());

        if (imzML.getFileDescription().getSourceFileList() == null) {
            imzML.getFileDescription().setSourceFileList(new SourceFileList(1));
        }

        imzML.getFileDescription().getSourceFileList().addSourceFile(sourceFile);

        try {
            sourceFile.addCVParam(new StringCVParam(getOBOTerm(SourceFile.SHA1_FILE_CHECKSUM_ID), calculateSHA1(filename)));
        } catch (ConversionException ex) {
            Logger.getLogger(ImzMLConverter.class.getName()).log(Level.SEVERE, "Failed to generate SHA1 for " + filename, ex);
        }

        // Add the native spectrum format
        if (currentFileDescription.getSourceFileList() != null) {
            sourceFile.addCVParam(currentFileDescription.getSourceFileList().getSourceFile(0).getCVParamOrChild(SourceFile.NATIVE_SPECTRUM_IDENTIFIER_FORMAT_ID));
            // TODO: Checksum					
        }
        sourceFile.addCVParam(new StringCVParam(getOBOTerm(SourceFile.MZML_FILE_FORMAT_ID), ""));
    }

    public static void removeEmptySpectraFromImzML(ImzML imzML) {
        // Remove empty spectra
        System.out.println("Checking spectra to remove...");

        ArrayList<Spectrum> spectraToRemove = new ArrayList<>();

        for (Spectrum spectrum : imzML.getRun().getSpectrumList()) {
            if (spectrum.getBinaryDataArrayList().getBinaryDataArray(0) != null
                    && spectrum.getBinaryDataArrayList().getBinaryDataArray(0).getEncodedLength() == 0) {
                spectraToRemove.add(spectrum);
            }
        }

        logger.log(Level.FINE, "Number of spectra before removing: {0}", imzML.getRun().getSpectrumList().size());

        for (Spectrum spectrum : spectraToRemove) {
            logger.log(Level.FINEST, "Removing empty spectrum: {0}", spectrum);

            imzML.getRun().getSpectrumList().removeSpectrum(spectrum);
        }

        logger.log(Level.FINE, "Number of spectra after removing: {0}", imzML.getRun().getSpectrumList().size());
    }

    @Override
    public void convert() throws ConversionException {
        // Check if the baseimzML is null, if so then use the first (i)mzML file as the base
//	if(baseImzML == null)
        
        // Checks
        if(inputFilenames == null || inputFilenames.length < 1) {
            throw new ConversionException("Not enough input files to convert, at least one must be supplied");
        }
        
        logger.log(Level.INFO, "Generating base imzML");
        
        generateBaseImzML();
        
        logger.log(Level.INFO, "Generated base imzML");

        baseImzML.getCVList().clear();
        OBO obo = OBO.getOBO();
        List<OBO> fullOBOList = obo.getFullImportHeirarchy();
        
        for(OBO currentOBO : fullOBOList) {
            baseImzML.getCVList().addCV(new CV(currentOBO));
        }
        
        // Add in IMS cv if it doesn't already exist
//        CV imsCV = baseImzML.getCVList().getCV("IMS");

//        if (imsCV == null) {
//            baseImzML.getCVList().addCV(new CV(CV.IMS_URI, "IMS", "IMS"));
//        }
        
        if (pixelLocations == null) {
            generatePixelLocations();
        }

        baseImzML.getReferenceableParamGroupList().clear();
        generateReferenceableParamArrays();

        // Add in the data processing describing the conversion
        imzMLConverter = baseImzML.getSoftwareList().getSoftware("imzMLConverter");

        if (imzMLConverter == null || !imzMLConverter.getVersion().equals(ImzMLConverter.version)) {
            imzMLConverter = new Software("imzMLConverter", ImzMLConverter.version);
            imzMLConverter.addCVParam(new EmptyCVParam(obo.getTerm(IMZMLCONVERTER_ID)));

            baseImzML.getSoftwareList().addSoftware(imzMLConverter);
        }

        // Add processing description to DataProcessing list
        DataProcessing conversionToImzML = new DataProcessing("conversionToImzML");
        conversionToImzML.addProcessingMethod(new ProcessingMethod(imzMLConverter));
        conversionToImzML.getProcessingMethod(0).addCVParam(new StringCVParam(getOBOTerm(ProcessingMethod.FILE_FORMAT_CONVERSION_ID), getConversionDescription()));
        conversionToImzML.getProcessingMethod(0).setSoftwareRef(imzMLConverter);

        baseImzML.getDataProcessingList().addDataProcessing(conversionToImzML);

        baseImzML.getRun().setSpectrumList(new SpectrumList(0, conversionToImzML));
        baseImzML.getRun().setChromatogramList(new ChromatogramList(0, conversionToImzML));

    }

    public static void setImzMLImageDimensions(ImzML imzML, int xPixels, int yPixels) {
        // Add in the maximum number of pixels in each dimension
        ScanSettingsList scanSettingsList = imzML.getScanSettingsList();

        if (scanSettingsList == null) {
            scanSettingsList = new ScanSettingsList(0);
            imzML.setScanSettingsList(scanSettingsList);
        }

        // TODO: Should it be the first scanSettings? One for each experiment?
        ScanSettings scanSettings = scanSettingsList.getScanSettings(0);

        if (scanSettings == null) {
            scanSettings = new ScanSettings("scanSettings1");
            scanSettingsList.addScanSettings(scanSettings);
        }
        scanSettings.removeCVParam(ScanSettings.MAX_COUNT_PIXEL_X_ID);
        scanSettings.removeCVParam(ScanSettings.MAX_COUNT_PIXEL_Y_ID);

        scanSettings.addCVParam(new StringCVParam(getOBOTerm(ScanSettings.MAX_COUNT_PIXEL_X_ID), "" + xPixels));
        scanSettings.addCVParam(new StringCVParam(getOBOTerm(ScanSettings.MAX_COUNT_PIXEL_Y_ID), "" + yPixels));
        //scanSettings.addCVParam(new CVParam(obo.getTerm(ScanSettings.maxCountPixelZID), ""+zPixels));
    }

    protected long copySpectrumToImzML(ImzML imzML, Spectrum spectrum, DataOutputStream binaryDataStream, long offset) throws IOException {
//        long prevOffset = offset;
//
//        imzML.getRun().getSpectrumList().addSpectrum(spectrum);
//
//        // Copy over data to .ibd stream if any
//        BinaryDataArrayList binaryDataArrayList = spectrum.getBinaryDataArrayList();
//
//        if (binaryDataArrayList == null) {
//            binaryDataArrayList = createDefaultBinaryDataArrayList();
//            spectrum.setBinaryDataArrayList(binaryDataArrayList);
//        }
//
//        for (BinaryDataArray binaryDataArray : binaryDataArrayList) {
//            byte[] dataToWrite = binaryDataArray.getDataAsByte();
//            CVParam dataArrayType = binaryDataArray.getDataArrayType();
//
//            CVParam dataType;
//
//            switch (dataArrayType.getTerm().getID()) {
//                case BinaryDataArray.mzArrayID:
//                    // Get the data as a double to populate the full m/z list
//                    if (includeGlobalmzList && fullmzList != null) {
//                        double[] mzList = binaryDataArray.getDataAsDouble();
//                        
//                        if(mzList != null) {
//                            Double[] dmzList = new Double[mzList.length];
//
//                            for (int i = 0; i < mzList.length; i++) {
//                                dmzList[i] = mzList[i];
//                            }
//
//                            fullmzList.addAll(Arrays.asList(dmzList));
//                        }
//                    }
//
//                    dataToWrite = BinaryDataArray.convertDataType(dataToWrite, binaryDataArray.getDataType(), this.mzArrayDataType);
//
//                    binaryDataArray.addReferenceableParamGroupRef(new ReferenceableParamGroupRef(rpgmzArray));
//                    binaryDataArray.removeCVParam(BinaryDataArray.mzArrayID);
//
//                    dataType = mzArrayDataType;
//                    break;
//                case BinaryDataArray.intensityArrayID:
//                    dataToWrite = BinaryDataArray.convertDataType(dataToWrite, binaryDataArray.getDataType(), this.intensityArrayDataType);
//
//                    binaryDataArray.addReferenceableParamGroupRef(new ReferenceableParamGroupRef(rpgintensityArray));
//                    binaryDataArray.removeCVParam(BinaryDataArray.intensityArrayID);
//
//                    dataType = intensityArrayDataType;
//                    break;
//                default:
//                    throw new UnsupportedOperationException("Unsupported dataArrayType: " + dataArrayType);
//            }
//
//            // Compress if necessary
//            dataToWrite = BinaryDataArray.compress(dataToWrite, this.compressionType);
//
//            // Write out data 
//            if (dataToWrite != null) {
//                binaryDataStream.write(dataToWrite);
//                offset += dataToWrite.length;
//            }
//
//            // Make sure that any previous settings are removed
//            binaryDataArray.removeChildOfCVParam(BinaryDataArray.compressionTypeID);
//            binaryDataArray.removeChildOfCVParam(BinaryDataArray.dataTypeID);
//
//            // Add binary data values to cvParams
//            binaryDataArray.removeCVParam(BinaryDataArray.externalEncodedLengthID);
//            binaryDataArray.addCVParam(new LongCVParam(getOBOTerm(BinaryDataArray.externalEncodedLengthID), (offset - prevOffset)));
//
//            binaryDataArray.removeCVParam(BinaryDataArray.externalDataID);
//            binaryDataArray.addCVParam(new StringCVParam(getOBOTerm(BinaryDataArray.externalDataID), "true"));
//
//            binaryDataArray.removeCVParam(BinaryDataArray.externalOffsetID);
//            binaryDataArray.addCVParam(new LongCVParam(getOBOTerm(BinaryDataArray.externalOffsetID), prevOffset));
//
//            binaryDataArray.removeCVParam(BinaryDataArray.externalArrayLengthID);
//            binaryDataArray.addCVParam(new LongCVParam(getOBOTerm(BinaryDataArray.externalArrayLengthID), dataToWrite.length / BinaryDataArray.getDataTypeInBytes(dataType)));
//
//            prevOffset = offset;
//        }
//
        return offset;
    }

    protected long outputFullmzList(DataOutputStream binaryDataStream, long offset) throws IOException {    
        if(this.includeGlobalmzList) {
            List<Double> sortedmzList = new ArrayList(fullmzList);
            Collections.sort(sortedmzList);

            imzMLConverter.addCVParam(new LongCVParam(obo.getTerm(BinaryDataArray.EXTERNAL_OFFSET_ID), offset));
            imzMLConverter.addCVParam(new LongCVParam(obo.getTerm(BinaryDataArray.EXTERNAL_ARRAY_LENGTH_ID), sortedmzList.size()));
            imzMLConverter.addCVParam(new LongCVParam(obo.getTerm(BinaryDataArray.EXTERNAL_ENCODED_LENGTH_ID), sortedmzList.size() * Double.BYTES));

            for (Double mz : sortedmzList) {
                binaryDataStream.writeDouble(mz);
            }
            
            offset += sortedmzList.size() * Double.BYTES;
        }
        
        return offset;
    }

    protected static void setCoordinatesOfSpectrum(Spectrum spectrum, int x, int y) {
        // TODO: Add position values to cvParams
        ScanList scanList = spectrum.getScanList();

        if (scanList == null) {
            scanList = new ScanList(0);
            spectrum.setScanList(scanList);
        }

        if (scanList.size() == 0) {
            Scan scan = new Scan();

            scanList.addScan(scan);
        }

        Scan scan = scanList.getScan(0);
        scan.removeCVParam(Scan.POSITION_X_ID);
        scan.addCVParam(new StringCVParam(getOBOTerm(Scan.POSITION_X_ID), "" + x));
        scan.removeCVParam(Scan.POSITION_Y_ID);
        scan.addCVParam(new StringCVParam(getOBOTerm(Scan.POSITION_Y_ID), "" + y));
    }

    protected static BinaryDataArrayList createDefaultBinaryDataArrayList() {
        BinaryDataArrayList binaryDataArrayList = new BinaryDataArrayList(2);

        // m/z
        BinaryDataArray mzBinaryDataArray = new BinaryDataArray(0);
        mzBinaryDataArray.addCVParam(new EmptyCVParam(getOBOTerm(BinaryDataArray.MZ_ARRAY_ID)));
        mzBinaryDataArray.addCVParam(new EmptyCVParam(getOBOTerm(BinaryDataArray.DOUBLE_PRECISION_ID)));
        mzBinaryDataArray.addCVParam(new EmptyCVParam(getOBOTerm(BinaryDataArray.NO_COMPRESSION_ID)));

        binaryDataArrayList.addBinaryDataArray(mzBinaryDataArray);

        // Counts
        BinaryDataArray countsBinaryDataArray = new BinaryDataArray(0);
        countsBinaryDataArray.addCVParam(new EmptyCVParam(getOBOTerm(BinaryDataArray.INTENSITY_ARRAY_ID)));
        countsBinaryDataArray.addCVParam(new EmptyCVParam(getOBOTerm(BinaryDataArray.DOUBLE_PRECISION_ID)));
        countsBinaryDataArray.addCVParam(new EmptyCVParam(getOBOTerm(BinaryDataArray.NO_COMPRESSION_ID)));

        binaryDataArrayList.addBinaryDataArray(countsBinaryDataArray);

        return binaryDataArrayList;
    }

    public synchronized double getProgress() {
        return progress;
    }

    public synchronized void updateProgress(double progress) {
        this.progress = progress;
    }

    public static String calculateSHA1(String filename) throws ConversionException {
        // Open the .ibd data stream
        DataInputStream dataStream = null;
        byte[] hash;

        try {
            dataStream = new DataInputStream(new FileInputStream(filename));
        } catch (FileNotFoundException e2) {
            throw new ConversionException("Could not open file " + filename, e2);
        }

        try {
            byte[] buffer = new byte[1024 * 1024];
            int bytesRead = 0;

            MessageDigest md = MessageDigest.getInstance("SHA-1");

            do {
                bytesRead = dataStream.read(buffer);

                if (bytesRead > 0) {
                    md.update(buffer, 0, bytesRead);
                }
            } while (bytesRead > 0);

            dataStream.close();

            hash = md.digest();
        } catch (NoSuchAlgorithmException e) {
            try {
                dataStream.close();
            } catch (IOException e1) {
                throw new ConversionException("Failed to close ibd file after trying to generate SHA-1 hash", e1);
            }

            throw new ConversionException("Generation of SHA-1 hash failed. No SHA-1 algorithm. " + e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new ConversionException("Failed generating SHA-1 hash. Failed to read data from " + filename + e.getMessage(), e);
        }

        try {
            dataStream.close();
        } catch (IOException e) {
            throw new ConversionException("Failed to close ibd file after generating SHA-1 hash", e);
        }

        return byteArrayToHexString(hash);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    public static String byteArrayToHexString(byte[] byteArray) {
        StringBuilder sb = new StringBuilder(2 * byteArray.length);

        byte[] Hexhars = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f'
        };

        for (int i = 0; i < byteArray.length; i++) {

            int v = byteArray[i] & 0xff;

            sb.append((char) Hexhars[v >> 4]);
            sb.append((char) Hexhars[v & 0xf]);
        }

        return sb.toString();
    }

    
}
