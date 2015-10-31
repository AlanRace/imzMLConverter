/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import static com.alanmrace.jimzmlconverter.ImzMLConverterOld.calculateSHA1;
import static com.alanmrace.jimzmlconverter.ImzMLConverterOld.hexStringToByteArray;
import com.alanmrace.jimzmlconverter.exceptions.ImzMLConversionException;
import com.alanmrace.jimzmlparser.exceptions.ImzMLWriteException;
import com.alanmrace.jimzmlparser.mzML.BinaryDataArray;
import com.alanmrace.jimzmlparser.mzML.BinaryDataArrayList;
import com.alanmrace.jimzmlparser.mzML.CVParam;
import com.alanmrace.jimzmlparser.mzML.FileContent;
import com.alanmrace.jimzmlparser.mzML.FileDescription;
import com.alanmrace.jimzmlparser.mzML.LongCVParam;
import com.alanmrace.jimzmlparser.mzML.MzML;
import com.alanmrace.jimzmlparser.mzML.ReferenceableParamGroup;
import com.alanmrace.jimzmlparser.mzML.ReferenceableParamGroupRef;
import com.alanmrace.jimzmlparser.mzML.Scan;
import com.alanmrace.jimzmlparser.mzML.ScanList;
import com.alanmrace.jimzmlparser.mzML.ScanSettings;
import com.alanmrace.jimzmlparser.mzML.SourceFile;
import com.alanmrace.jimzmlparser.mzML.SourceFileList;
import com.alanmrace.jimzmlparser.mzML.Spectrum;
import com.alanmrace.jimzmlparser.mzML.StringCVParam;
import com.alanmrace.jimzmlparser.parser.ImzMLHandler;
import com.alanmrace.jimzmlparser.parser.MzMLHeaderHandler;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alan
 */
public class MzMLToImzMLConverter extends ImzMLConverter {

    FileStorage fileStorage;
    
    public enum FileStorage {
	rowPerFile,
	oneFile,
	pixelPerFile
    }
    
    public MzMLToImzMLConverter(String outputFilename, String[] inputFilenames, FileStorage fileStorage) {
	super(outputFilename, inputFilenames);
        
        this.fileStorage = fileStorage;
    }

    
    public void setFileStorage(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }
    
    @Override
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
    
    @Override
    protected void generateBaseImzML() {
	baseImzML = ImzMLHandler.parseimzML(inputFilenames[0]);
    }

    @Override
    protected String getConversionDescription() {
        return "Conversion from mzML to imzML";
    }
    
    @Override
    public void convert() throws ImzMLConversionException {
        super.convert();
        
        // TODO: Remove these magic numbers
        int xPixels = baseImzML.getRun().getSpectrumList().size();
        int yPixels = inputFilenames.length;
        String lineScanDirection = ScanSettings.lineScanDirectionLeftRightID;
        
        int x = 1;
        int y = 1;
        
        int maxX = x;
        int maxY = y;
        
        // Open the .ibd data stream
	DataOutputStream binaryDataStream = null;
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
        
        int currentSpectrumIndex = 0;
	int currentmzMLFile = 0;

        for (String mzMLFilename : inputFilenames) {
//            if (currentSpectrumIndex >= pixelLocations.length) {
//                break;
//            }

            int maxSpectra = 0;
            int currentFileSpectrumIndex = 0;


            try {
                MzML currentmzML = MzMLHeaderHandler.parsemzMLHeader(mzMLFilename);
                //System.out.println(currentmzML.getRun().getSpectrumList().size());
                //System.out.println(currentmzML.getRun().getSpectrumList().getSpectrum(0).getBinaryDataArrayList().getBinaryDataArray(0));
                
    //            System.out.println(currentmzML.getRun().getSpectrumList().getSpectrum(0).getmzArray()[0]);
                //            // Sort out the order of the spectra as they appear in the mzML file
                if (fileStorage == FileStorage.rowPerFile) {
                    maxSpectra = currentmzML.getRun().getSpectrumList().size();
    //                if (lineScanDirection.equals(ScanSettings.lineScanDirectionLeftRightID)
    //                        || lineScanDirection.equals(ScanSettings.lineScanDirectionRightLeftID)) {
    //                    maxSpectra = xPixels;
    //                } else {
    //                    maxSpectra = yPixels;
    //                }
                } else if (fileStorage == FileStorage.oneFile) {
                    maxSpectra = pixelLocations.length;
                } else if (fileStorage == FileStorage.pixelPerFile) {
                    maxSpectra = 1;
                }

                // TODO: Add all referenceParamGoups - TEMPORARY FIX
                for (ReferenceableParamGroup rpg : currentmzML.getReferenceableParamGroupList()) {
                    baseImzML.getReferenceableParamGroupList().addReferenceableParamGroup(rpg);
                }

                String filenameID = "mzML" + currentmzMLFile++;;
                addSourceFileToImzML(baseImzML, mzMLFilename, filenameID, currentmzML.getFileDescription());

                

                

                long prevOffset = offset;

                for (int i = 0; i < maxSpectra; i++) {
//                    if (currentSpectrumIndex >= pixelLocations.length) {
//                        break;
//                    }

                    Spectrum spectrum = currentmzML.getRun().getSpectrumList().getSpectrum(currentFileSpectrumIndex++);
//                    PixelLocation location = pixelLocations[currentSpectrumIndex++];
                    
                    // TODO: REMOVE THIS FOR SPEED INCREASE WHEN WORKAROUND IS IMPLEMENTED
                    spectrum.getmzArray();

                    // If there isn't binaryDataList then don't copy over the spectrum (for DatacubeExplorer)
 //                   if (spectrum != null && location.getX() != -1 && location.getY() != -1 && spectrum.getBinaryDataArrayList().size() > 0) {
                        baseImzML.getRun().getSpectrumList().addSpectrum(spectrum);

                        // Copy over data to .ibd stream if any
                        BinaryDataArrayList binaryDataArrayList = spectrum.getBinaryDataArrayList();

                        if (binaryDataArrayList == null) {
                            binaryDataArrayList = createDefaultBinaryDataArrayList();
                            spectrum.setBinaryDataArrayList(binaryDataArrayList);
                        }

                        for(BinaryDataArray binaryDataArray : binaryDataArrayList) {
                            byte[] dataToWrite = binaryDataArray.getDataAsByte();
                            CVParam dataArrayType = binaryDataArray.getDataArrayType();
                            
                            switch(dataArrayType.getTerm().getID()) {
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
                            if(dataToWrite != null) {
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
//                        scan.addCVParam(new StringCVParam(getOBOTerm(Scan.positionXID), "" + location.getX()));
//                        scan.addCVParam(new StringCVParam(getOBOTerm(Scan.positionYID), "" + location.getY()));
//                        scan.addCVParam(new StringCVParam(getOBOTerm(Scan.positionZID), "" + location.getZ()));
                        
                        if(x > maxX)
                            maxX = x;
                        

                        x++;
                    }
                
                    if(y > maxY)
                        maxY = y;
                    
//                    if (x > xPixels) {                        
                        x = 1;
                        y++;
//                    }

//						System.out.println((((((y-1) * xPixels) + x) * 100) / (yPixels*xPixels)) + "%");
//                    }

//                    if (progressBar != null) {
//                        progressBar.setSelection(((((y - 1) * xPixels) + x) * 100) / (yPixels * xPixels));
//                        progressBar.update();
//                        progressBar.redraw();
//
//                        while (progressBar.getDisplay().readAndDispatch());
//                    }

//                }

                currentmzML = null;
//                handler.deleteTemporaryFile();
            } catch (FileNotFoundException fnfe) {
                throw new ImzMLConversionException("Could not find the file " + mzMLFilename);
            }
        }

            binaryDataStream.close();
        } catch (IOException e) {
            throw new ImzMLConversionException("Error closing " + outputFilename + ".ibd");
        }

        // Remove empty spectra
        if (removeEmptySpectra) {
            System.out.println("Checking spectra to remove...");

            ArrayList<Spectrum> spectraToRemove = new ArrayList<Spectrum>();

            for (Spectrum spectrum : baseImzML.getRun().getSpectrumList()) {
                if (spectrum.getBinaryDataArrayList().getBinaryDataArray(0) != null
                        && spectrum.getBinaryDataArrayList().getBinaryDataArray(0).getEncodedLength() == 0) {
                    spectraToRemove.add(spectrum);
                }
            }

            System.out.println("Number of spectra before removing: " + baseImzML.getRun().getSpectrumList().size());

            for (Spectrum spectrum : spectraToRemove) {
                System.out.println("Removing empty spectrum: " + spectrum);
                baseImzML.getRun().getSpectrumList().removeSpectrum(spectrum);
            }

            System.out.println("Number of spectra after removing: " + baseImzML.getRun().getSpectrumList().size());
        }

        this.updateMaximumPixels(maxX, maxY);
        
        String sha1Hash = calculateSHA1(outputFilename + ".ibd");
        baseImzML.getFileDescription().getFileContent().removeChildOfCVParam(FileContent.ibdChecksumID);

        System.out.println("SHA-1 Hash: " + sha1Hash);
        baseImzML.getFileDescription().getFileContent().addCVParam(new StringCVParam(getOBOTerm(FileContent.sha1ChecksumID), sha1Hash));

        try {
            baseImzML.write(outputFilename + ".imzML");
            
//        if (progressBar != null) {
//            progressBar.setSelection(100);
//            progressBar.update();
//            progressBar.redraw();
//
//            while (progressBar.getDisplay().readAndDispatch());
//        }
        } catch (ImzMLWriteException ex) {
            Logger.getLogger(MzMLToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    public static void main(String args[]) throws IOException, ImzMLConversionException {
        String wiffFile = "D:\\Test\\Data7_1_2011-acc0.1_cyc10.wiff";
        wiffFile = "D:\\Rory\\SampleData\\2012_5_2_medium(120502,20h18m).wiff";

        long startTime = System.currentTimeMillis();
        File[] mzMLFiles = WiffTomzMLConverter.convert(wiffFile);
        String[] mzMLFilepaths = new String[mzMLFiles.length];
        
        for(int i = 0; i < mzMLFiles.length; i++)
            mzMLFilepaths[i] = mzMLFiles[i].getAbsolutePath();
        
        long end1Time = System.currentTimeMillis();
        
        MzMLToImzMLConverter converter = new MzMLToImzMLConverter(wiffFile, mzMLFilepaths, FileStorage.rowPerFile);
        converter.setFileStorage(FileStorage.rowPerFile);
        
        converter.convert();
        
        long end2Time = System.currentTimeMillis();
        
        System.out.println("Conversion to mzML took: " + (end1Time - startTime) + " ms");
        System.out.println("Conversion to imzML took: " + (end2Time - end1Time) + " ms");
    }
}
