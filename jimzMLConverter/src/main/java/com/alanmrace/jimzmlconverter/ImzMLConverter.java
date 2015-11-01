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
import com.alanmrace.jimzmlparser.mzML.FileDescription;
import com.alanmrace.jimzmlparser.mzML.LongCVParam;
import com.alanmrace.jimzmlparser.mzML.ProcessingMethod;
import com.alanmrace.jimzmlparser.mzML.ReferenceableParamGroup;
import com.alanmrace.jimzmlparser.mzML.ReferenceableParamGroupRef;
import com.alanmrace.jimzmlparser.mzML.Scan;
import com.alanmrace.jimzmlparser.mzML.ScanList;
import com.alanmrace.jimzmlparser.mzML.ScanSettings;
import com.alanmrace.jimzmlparser.mzML.ScanSettingsList;
import com.alanmrace.jimzmlparser.mzML.Software;
import com.alanmrace.jimzmlparser.mzML.SourceFile;
import com.alanmrace.jimzmlparser.mzML.SourceFileList;
import com.alanmrace.jimzmlparser.mzML.Spectrum;
import com.alanmrace.jimzmlparser.mzML.SpectrumList;
import com.alanmrace.jimzmlparser.mzML.StringCVParam;
import com.alanmrace.jimzmlparser.obo.OBO;
import com.alanmrace.jimzmlparser.obo.OBOTerm;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Alan
 */
public abstract class ImzMLConverter {

    private static final Logger logger = Logger.getLogger(ImzMLConverter.class.getName());
    
    public static final String version = "2.0.0";

    protected double progress;

    protected static OBO obo;

    protected ImzML baseImzML;

    protected String outputFilename;
    protected String[] inputFilenames;

    protected PixelLocation[] pixelLocations;

    protected CVParam compressionType;
    protected CVParam mzArrayDataType;
    protected CVParam intensityArrayDataType;

    protected boolean removeEmptySpectra;

    protected ReferenceableParamGroup rpgmzArray;
    protected ReferenceableParamGroup rpgintensityArray;

    public ImzMLConverter(String outputFilename, String[] inputFilenames) {
	this.outputFilename = outputFilename;
	this.inputFilenames = inputFilenames;

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

    protected static final OBOTerm getOBOTerm(String cvParamID) {
	if (obo == null) {
	    obo = OBO.getOBO();
	}

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

    protected abstract void generatePixelLocations();

    public static void addSourceFileToImzML(ImzML imzML, String filename, String filenameID, FileDescription currentFileDescription) {
	// Add the sourceFile to the sourceFileList
	File file = new File(filename);

	SourceFile sourceFile = new SourceFile(filenameID, file.getParentFile().toURI().toString(), file.getName());

	if (imzML.getFileDescription().getSourceFileList() == null) {
	    imzML.getFileDescription().setSourceFileList(new SourceFileList(1));
	}

	imzML.getFileDescription().getSourceFileList().addSourceFile(sourceFile);

	try {
	    sourceFile.addCVParam(new StringCVParam(getOBOTerm(SourceFile.sha1FileChecksumType), calculateSHA1(filename)));
	} catch (ImzMLConversionException ex) {
	    Logger.getLogger(ImzMLConverter.class.getName()).log(Level.SEVERE, "Failed to generate SHA1 for " + filename, ex);
	}

	// Add the native spectrum format
	if (currentFileDescription.getSourceFileList() != null) {
	    sourceFile.addCVParam(currentFileDescription.getSourceFileList().getSourceFile(0).getCVParamOrChild(SourceFile.nativeSpectrumIdentifierFormat));
	    // TODO: Checksum					
	}
	sourceFile.addCVParam(new StringCVParam(getOBOTerm(SourceFile.mzMLFileFormat), ""));
    }

    public static void removeEmptySpectraFromImzML(ImzML imzML) {
	// Remove empty spectra
	System.out.println("Checking spectra to remove...");

	ArrayList<Spectrum> spectraToRemove = new ArrayList<Spectrum>();

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

    public void convert() throws ImzMLConversionException {
	// Check if the baseimzML is null, if so then use the first (i)mzML file as the base
//	if(baseImzML == null)
	generateBaseImzML();

	if (pixelLocations == null) {
	    generatePixelLocations();
	}

	generateReferenceableParamArrays();

	// Add in the data processing describing the conversion
	Software imzMLConverter = baseImzML.getSoftwareList().getSoftware("imzMLConverter");

	if (imzMLConverter == null || !imzMLConverter.getVersion().equals(ImzMLConverter.version)) {
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
	scanSettings.removeChildOfCVParam(ScanSettings.maxCountPixelXID);
	scanSettings.removeChildOfCVParam(ScanSettings.maxCountPixelYID);

	scanSettings.addCVParam(new StringCVParam(getOBOTerm(ScanSettings.maxCountPixelXID), "" + xPixels));
	scanSettings.addCVParam(new StringCVParam(getOBOTerm(ScanSettings.maxCountPixelYID), "" + yPixels));
	//scanSettings.addCVParam(new CVParam(obo.getTerm(ScanSettings.maxCountPixelZID), ""+zPixels));
    }

    protected long copySpectrumToImzML(ImzML imzML, Spectrum spectrum, DataOutputStream binaryDataStream, long offset) throws IOException {
	long prevOffset = offset;
	
	imzML.getRun().getSpectrumList().addSpectrum(spectrum);

	// Copy over data to .ibd stream if any
	BinaryDataArrayList binaryDataArrayList = spectrum.getBinaryDataArrayList();

	if (binaryDataArrayList == null) {
	    binaryDataArrayList = createDefaultBinaryDataArrayList();
	    spectrum.setBinaryDataArrayList(binaryDataArrayList);
	}

	for (BinaryDataArray binaryDataArray : binaryDataArrayList) {
	    byte[] dataToWrite = binaryDataArray.getDataAsByte();
	    CVParam dataArrayType = binaryDataArray.getDataArrayType();

	    switch (dataArrayType.getTerm().getID()) {
		case BinaryDataArray.mzArrayID:
		    dataToWrite = BinaryDataArray.convertDataType(dataToWrite, binaryDataArray.getDataType(), this.mzArrayDataType);

		    binaryDataArray.addReferenceableParamGroupRef(new ReferenceableParamGroupRef(rpgmzArray));
		    binaryDataArray.removeCVParam(BinaryDataArray.mzArrayID);
		    break;
		case BinaryDataArray.intensityArrayID:
		    dataToWrite = BinaryDataArray.convertDataType(dataToWrite, binaryDataArray.getDataType(), this.intensityArrayDataType);

		    binaryDataArray.addReferenceableParamGroupRef(new ReferenceableParamGroupRef(rpgintensityArray));
		    binaryDataArray.removeCVParam(BinaryDataArray.intensityArrayID);
		    break;
		default:
		    throw new UnsupportedOperationException("Unsupported dataArrayType: " + dataArrayType);
	    }

	    // Compress if necessary
	    dataToWrite = BinaryDataArray.compress(dataToWrite, this.compressionType);

	    // Write out data 
	    if (dataToWrite != null) {
		binaryDataStream.write(dataToWrite);
		offset += dataToWrite.length;
	    }

	    // Make sure that any previous settings are removed
	    binaryDataArray.removeChildOfCVParam(BinaryDataArray.compressionTypeID);
	    binaryDataArray.removeChildOfCVParam(BinaryDataArray.dataTypeID);

	    // Add binary data values to cvParams
	    binaryDataArray.addCVParam(new LongCVParam(getOBOTerm(BinaryDataArray.externalEncodedLengthID), (offset - prevOffset)));
	    binaryDataArray.addCVParam(new StringCVParam(getOBOTerm(BinaryDataArray.externalDataID), "true"));
	    binaryDataArray.addCVParam(new LongCVParam(getOBOTerm(BinaryDataArray.externalOffsetID), prevOffset));
	    
	    prevOffset = offset;
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
	scan.addCVParam(new StringCVParam(getOBOTerm(Scan.positionXID), "" + x));
	scan.addCVParam(new StringCVParam(getOBOTerm(Scan.positionYID), "" + y));
    }

    protected static BinaryDataArrayList createDefaultBinaryDataArrayList() {
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

    public static String calculateSHA1(String filename) throws ImzMLConversionException {
	// Open the .ibd data stream
	DataInputStream dataStream = null;
	byte[] hash;

	try {
	    dataStream = new DataInputStream(new FileInputStream(filename));
	} catch (FileNotFoundException e2) {
	    throw new ImzMLConversionException("Could not open file " + filename);
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
		throw new ImzMLConversionException("Failed to close ibd file after trying to generate SHA-1 hash");
	    }

	    throw new ImzMLConversionException("Generation of SHA-1 hash failed. No SHA-1 algorithm. " + e.getLocalizedMessage());
	} catch (IOException e) {
	    throw new ImzMLConversionException("Failed generating SHA-1 hash. Failed to read data from " + filename + e.getMessage());
	}

	try {
	    dataStream.close();
	} catch (IOException e) {
	    throw new ImzMLConversionException("Failed to close ibd file after generating SHA-1 hash");
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
    
    private static String getExtension(String fileName) {
	String extension = "";

		int i = fileName.lastIndexOf('.');
		if (i > 0) {
		    extension = fileName.substring(i+1);
		}
		
		return extension;
    }
    
    public static void main(String[] args) {
	Logger logger = Logger.getLogger(ImzMLConverter.class.getName());  
	FileHandler fh;  

	try {  
	    // This block configure the logger with handler and formatter  
	    fh = new FileHandler("imzMLConverter.log");  
	    logger.addHandler(fh);
	    SimpleFormatter formatter = new SimpleFormatter();  
	    fh.setFormatter(formatter);  
	} catch (IOException | SecurityException ex) {
	    Logger.getLogger(ImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
	} 
	
	ImzMLConverterCommandArguments arguments = new ImzMLConverterCommandArguments();
	
	JCommander jc = new JCommander(arguments);
	jc.setProgramName("jimzMLConverter");
	
	try {
	    jc.parse(args);
	} catch(ParameterException pex) {
	    arguments.help = true;
	    System.out.println(pex.getMessage());
	}
    
	if(arguments.help) {
	    System.out.println("imzMLConverter version " + ImzMLConverter.version);
	    jc.usage();
	    System.exit(0);
	}
	
	String outputPath = arguments.output;
	
	
	if(!arguments.combine) {
	    for(String fileName : arguments.files) {
                logger.log(Level.INFO, "Converting file {0}", fileName);
                
		File[] mzMLFiles = null;
		String[] inputFilenames = null;
		
		String extension = getExtension(fileName);
		
		if(extension.equals("wiff")) {
                    logger.log(Level.INFO, "Detected WIFF file");
                    
		    try {
			mzMLFiles = WiffTomzMLConverter.convert(fileName);			
		    } catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		    }
		    
		    if(outputPath == null || outputPath.isEmpty())
			outputPath = fileName.replace(".wiff", "");
		}
		
		if(mzMLFiles != null) {
		    inputFilenames = new String[mzMLFiles.length];
		    
		    for(int i = 0; i < mzMLFiles.length; i++) {
			    inputFilenames[i] = mzMLFiles[i].getAbsolutePath();
		    }
		}
		
		if(inputFilenames != null) {
		    ImzMLConverter converter = new MzMLToImzMLConverter(outputPath, inputFilenames, MzMLToImzMLConverter.FileStorage.rowPerFile);

		    try {
			converter.convert();
                        
                        logger.log(Level.INFO, MessageFormat.format("Converted {0}", outputPath));
		    } catch (ImzMLConversionException ex) {
			logger.log(Level.SEVERE, "Failed to convert " + fileName, ex);
		    }
		}
                
                // Cleanup
                if(mzMLFiles != null) {
                    for(File mzMLFile : mzMLFiles) {
                        try {
                            Files.delete(mzMLFile.toPath());
                            logger.log(Level.INFO, MessageFormat.format("Cleaned up {0}", mzMLFile));
                        } catch (IOException ex) {
                            logger.log(Level.SEVERE, MessageFormat.format("Failed to clean up {0}", mzMLFile), ex);
                        }
                    }
                }
	    }
	}
    }
}
