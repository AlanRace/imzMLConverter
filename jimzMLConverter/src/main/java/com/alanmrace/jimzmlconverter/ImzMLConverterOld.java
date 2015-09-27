package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlparser.imzML.ImzML;
import com.alanmrace.jimzmlparser.imzML.PixelLocation;
import com.alanmrace.jimzmlconverter.Waters.PatternDefinitionHandler;
import com.alanmrace.jimzmlconverter.exceptions.ImzMLConversionException;
import com.alanmrace.jimzmlparser.exceptions.CVParamAccessionNotFoundException;
import com.alanmrace.jimzmlparser.exceptions.InvalidImzML;
import com.alanmrace.jimzmlparser.exceptions.InvalidMzML;
//import imzMLConverter.gui.imzMLConverterWindow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.alanmrace.jimzmlparser.mzML.*;
import com.alanmrace.jimzmlparser.obo.OBO;
import com.alanmrace.jimzmlparser.parser.ImzMLHandler;
import com.alanmrace.jimzmlparser.parser.MzMLHandler;

import org.xml.sax.SAXException;

public class ImzMLConverterOld {
	
	public static final String version = "2.0.0";
    
	private static Logger logger = Logger.getLogger(ImzMLConverterOld.class.getName());

	public static int rowPerFile = 0;
	public static int oneFile = 1;
	public static int pixelPerFile = 2;
	
	protected double progress; 
	
	public static void convertimzMLToimzML(String outputFilename, ImzML imzML, OBO obo, 
			String[] mzMLFilenames, int fileOrganisation, int xPixels, int yPixels, String spectrumLocationFile,
			boolean compress, boolean removeEmptySpectra, CVParam mzArrayDataType, CVParam intensityArrayDataType) throws ImzMLConversionException, CVParamAccessionNotFoundException {
		int x = 1;
		int y = 1;
		
		long offset = 0;
		
		ArrayList<ImzML> imzMLList = new ArrayList<ImzML>();
		int[][] xDimensions = new int[xPixels][yPixels];
		int[][] yDimensions = new int[xPixels][yPixels];
		
		// TODO: Change the data type in ReferenceableParamGroup
		ReferenceableParamGroup rpgmzArray = imzML.getReferenceableParamGroupList().getReferenceableParamGroup("mzArray");
		
		// Generally called 'intensityArray'
		ReferenceableParamGroup rpgintensityArray = imzML.getReferenceableParamGroupList().getReferenceableParamGroup("intensityArray");
		
		rpgmzArray.removeChildOfCVParam(BinaryDataArray.dataTypeID);
		rpgmzArray.addCVParam(mzArrayDataType);
		
		// If can't find it then maybe Bruker exporter was used, which calls it 'intensities'
		if(rpgintensityArray == null)
			rpgintensityArray = imzML.getReferenceableParamGroupList().getReferenceableParamGroup("intensities");
		
		rpgintensityArray.removeChildOfCVParam(BinaryDataArray.dataTypeID);
		rpgintensityArray.addCVParam(intensityArrayDataType);		
		
		int mzArrayDataTypeInBytes = BinaryDataArray.getDataTypeInBytes(mzArrayDataType);
		int intensityArrayDataTypeInBytes = BinaryDataArray.getDataTypeInBytes(intensityArrayDataType);
		
		// Reset the imzML spectrumList & chromatogramList
		imzML.getRun().setSpectrumList(new SpectrumList(0, imzML.getRun().getSpectrumList().getDefaultDataProcessingRef()));
		imzML.getRun().setChromatogramList(new ChromatogramList(0, imzML.getRun().getSpectrumList().getDefaultDataProcessingRef()));	
		
		// Remove any old compression values if any
		rpgmzArray.removeChildOfCVParam(BinaryDataArray.compressionTypeID);
		rpgintensityArray.removeChildOfCVParam(BinaryDataArray.compressionTypeID);
		
		if(compress) {
			rpgmzArray.addCVParam(new StringCVParam(obo.getTerm(BinaryDataArray.zlibCompressionID), ""));
			rpgintensityArray.addCVParam(new StringCVParam(obo.getTerm(BinaryDataArray.zlibCompressionID), ""));
		} else {
			rpgmzArray.addCVParam(new StringCVParam(obo.getTerm(BinaryDataArray.noCompressionID), ""));
			rpgintensityArray.addCVParam(new StringCVParam(obo.getTerm(BinaryDataArray.noCompressionID), ""));
		}
		
		for(String mzMLFilename : mzMLFilenames) {
			File ibdFile = new File(mzMLFilename.substring(0, mzMLFilename.lastIndexOf('.')) + ".ibd");
			
			// Convert mzML header information -> imzML
	        ImzMLHandler handler = new ImzMLHandler(obo, ibdFile);
	        		
	        SAXParserFactory spf = SAXParserFactory.newInstance();
	        try {
	        	//get a new instance of parser
	        	SAXParser sp = spf.newSAXParser();
	        	
	        	File mzMLFile = new File(mzMLFilename);
	        			
	        	//parse the file and also register this class for call backs
	        	sp.parse(mzMLFile, handler);
	        			
	        } catch(SAXException se) {
	        	throw new ImzMLConversionException("Error parsing " + mzMLFilename + ". " + se.getLocalizedMessage());
	        } catch(ParserConfigurationException pce) {
	        	throw new ImzMLConversionException("Error parsing " + mzMLFilename + ". " + pce.getLocalizedMessage());
	        } catch (IOException ie) {
	        	throw new ImzMLConversionException("Error parsing " + mzMLFilename + ". " + ie.getLocalizedMessage());
	        } catch (InvalidImzML ii) {
	    		throw new ImzMLConversionException("Invalid imzML file " + mzMLFilename + ". " + ii.getLocalizedMessage());
	    	}

	        ImzML currentimzML = handler.getimzML();
	        
	        imzMLList.add(currentimzML);
	        
	        xDimensions[x-1][y-1] = currentimzML.getWidth();
	        yDimensions[x-1][y-1] = currentimzML.getHeight();
	        
	        x++;
	        if(x > xPixels) {
	        	x = 1;
	        	y++;
	        }
		}
		
		x = 1;
		y = 1;
		
		int maxX = 0;
		int maxY = 0;
		
		// Need to determine the largest dimensions
		for(int i = 0; i < yPixels; i++) {
			int curRow = 0;
			
			for(int j = 0; j < xPixels; j++)
				curRow += xDimensions[j][i];
			
			if(curRow > maxX)
				maxX = curRow;
		}
		
		for(int i = 0; i < xPixels; i++) {
			int curColumn = 0;
			
			for(int j = 0; j < yPixels; j++)
				curColumn += yDimensions[i][j];
			
			if(curColumn > maxY)
				maxY = curColumn;
		}
		
		
		
		// Open the .ibd data stream
		DataOutputStream binaryDataStream = null;
		try {
			binaryDataStream = new DataOutputStream(new FileOutputStream(outputFilename + ".ibd"));
		} catch (FileNotFoundException e2) {
			throw new ImzMLConversionException("Could not find the file " + outputFilename + ".ibd");
		}
			
		String uuid = UUID.randomUUID().toString().replace("-", "");
		System.out.println(uuid);
		
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
		offset = binaryDataStream.size();
		
		long prevOffset = offset;
		
		System.out.println("MaxX = " + maxX + ", MaxY = " + maxY);
		for(int curFileY = 0; curFileY < yPixels*xPixels; curFileY += xPixels) {
			int maxRows = 0;
			
			// Determine the maximum number of yPixels for this row of images
			for(int curFileX = curFileY; curFileX < (curFileY+xPixels) && curFileX < imzMLList.size(); curFileX++)			
			{
				System.out.println("MaxX = " + maxX + ", MaxY = " + maxY);
				System.out.println("curFileY: " + curFileY + ", curFileX: " + curFileX + " maxRows: " + maxRows);
				ImzML currentimzML = imzMLList.get(curFileX);
				
				if(currentimzML.getHeight() > maxRows)
					maxRows = currentimzML.getHeight();
			}
			
			System.out.println("curFileY: " + curFileY + ", maxRows: " + maxRows);
			
			for(int currentRow = 1; currentRow <= maxRows; currentRow++) {
				for(int curFileX = curFileY; curFileX < (curFileY+xPixels) && curFileX < imzMLList.size(); curFileX++) {				
					
					ImzML currentimzML = imzMLList.get(curFileX);
					System.out.println("(" +x+","+y+") curFileX: " + curFileX + "(" +  ")" + " curFileY: " + curFileY + "(" + currentRow + ") " + imzMLList.size());
					System.out.println("" + currentimzML.getWidth());
					for(int currentColumn = 1; currentColumn <= currentimzML.getWidth(); currentColumn++) {					
						Spectrum spectrum = currentimzML.getSpectrum(currentColumn, currentRow);
						
						// Make an empty spectrum
			    		if(spectrum != null) {						
							imzML.getRun().getSpectrumList().addSpectrum(spectrum);
				    		
				    		// Copy over data to .ibd stream if any
				    		BinaryDataArrayList binaryDataArrayList = spectrum.getBinaryDataArrayList();
				    		
				    		for(int j = 0; j < binaryDataArrayList.size(); j++) {
				    			BinaryDataArray binaryDataArray = binaryDataArrayList.getBinaryDataArray(j);
				    			Binary binary = binaryDataArray.getBinary();
				    			
				    			if(binaryDataArray.getCVParam(BinaryDataArray.mzArrayID) != null) {
				    				if(binary != null)
					    				offset = binary.copyDataToDataStream(binaryDataStream, offset, compress, mzArrayDataType);
				    							    				
				    				CVParam externalArrayLength = binaryDataArray.getCVParam(BinaryDataArray.externalArrayLengthID);
				    				
//				    				System.out.println("Test");
//				    				System.out.println(externalArrayLength);
//				    				System.out.println(offset);
//				    				System.out.println(prevOffset);
//				    				System.out.println(mzArrayDataTypeInBytes);
				    				
					    			externalArrayLength.setValueAsString("" + ((offset - prevOffset) / mzArrayDataTypeInBytes));
				    			} else if(binaryDataArray.getCVParam(BinaryDataArray.intensityArrayID) != null) {
				    				if(binary != null)
					    				offset = binary.copyDataToDataStream(binaryDataStream, offset, compress, intensityArrayDataType);
				    				
					    			CVParam externalArrayLength = binaryDataArray.getCVParam(BinaryDataArray.externalArrayLengthID);
					    			externalArrayLength.setValueAsString("" + ((offset - prevOffset) / intensityArrayDataTypeInBytes));
				    			} else if(binary != null)
				    				offset = binary.copyDataToDataStream(binaryDataStream, offset, compress);	    			
	
				    			// Add binary data values to cvParams			
				    			CVParam externalEncodedLength = binaryDataArray.getCVParam(BinaryDataArray.externalEncodedLengthID);
				    			externalEncodedLength.setValueAsString("" + (offset - prevOffset));
				    			CVParam externalOffset = binaryDataArray.getCVParam(BinaryDataArray.externalOffsetID);
				    			externalOffset.setValueAsString("" + prevOffset);
				    			
				    			prevOffset = offset;
				    		
				    		}
				    		
				    		// TODO: Add position values to cvParams
				    		ScanList scanList = spectrum.getScanList();
				    		
				    		if(scanList == null) {
				    			scanList = new ScanList(0);
				    			spectrum.setScanList(scanList);
				    		}
				    		
				    		if(scanList.size() == 0) {
				    			Scan scan = new Scan();
				    			
				    			scanList.addScan(scan);
	
					    		scan.addCVParam(new StringCVParam(obo.getTerm(Scan.positionXID), ""+x));
					    		scan.addCVParam(new StringCVParam(obo.getTerm(Scan.positionYID), ""+y));
				    		} else {
				    			Scan scan = scanList.getScan(0);
				    			
				    			CVParam positionX = scan.getCVParam(Scan.positionXID);
				    			positionX.setValueAsString(""+x);
				    			CVParam positionY = scan.getCVParam(Scan.positionYID);
				    			positionY.setValueAsString(""+y);
				    		}
				    		
				    		if(x > maxX)
				    			maxX = x;
				    		if(y > maxY)
				    			maxY = y;
				    	}	
				    		
			    		x++;
					}
				}
				
				if(progressBar != null) {
					progressBar.setSelection(((y-1) * 100) / maxY);
//					System.out.println((((y-1) * 100) / maxY) + "%");
					progressBar.update();
					progressBar.redraw();
					
					while(progressBar.getDisplay().readAndDispatch());
				}
				
				x = 1;
				y++;
			}
		}
		
		try {
			binaryDataStream.close();
		} catch (IOException e) {
			throw new ImzMLConversionException("Could not close file " + outputFilename + ".ibd");
		}
		
		// Add in x and y pixel counts variables
				ScanSettingsList scanSettingsList = imzML.getScanSettingsList();
								
				if(scanSettingsList == null) {
					scanSettingsList = new ScanSettingsList(0);		
					imzML.setScanSettingsList(scanSettingsList);
				}
						
				// TODO: Should it be the first scanSettings? One for each experiment?
				ScanSettings scanSettings = scanSettingsList.getScanSettings(0);
				
				if(scanSettings == null) {
					scanSettings = new ScanSettings("scanSettings1");
					scanSettingsList.addScanSettings(scanSettings);
					
					scanSettings.addCVParam(new StringCVParam(obo.getTerm(ScanSettings.maxCountPixelXID), ""+maxX));
					scanSettings.addCVParam(new StringCVParam(obo.getTerm(ScanSettings.maxCountPixelYID), ""+maxY));
				} else {			
					CVParam maxCountPixelX = scanSettings.getCVParam(ScanSettings.maxCountPixelXID);
					maxCountPixelX.setValueAsString(""+maxX);
					CVParam maxCountPixelY = scanSettings.getCVParam(ScanSettings.maxCountPixelYID);
					maxCountPixelY.setValueAsString(""+maxY);
				}
		
		// Remove empty spectra
		if(removeEmptySpectra) {
			System.out.println("Checking spectra to remove...");
			
			ArrayList<Spectrum> spectraToRemove = new ArrayList<Spectrum>();
			
			for(Spectrum spectrum : imzML.getRun().getSpectrumList()) {
				if(spectrum.getBinaryDataArrayList().getBinaryDataArray(0) != null &&
						spectrum.getBinaryDataArrayList().getBinaryDataArray(0).getEncodedLength() == 0) {
					spectraToRemove.add(spectrum);
				}
			}
			
			System.out.println("Number of spectra before removing: " + imzML.getRun().getSpectrumList().size());
			
			for(Spectrum spectrum : spectraToRemove) {
				System.out.println("Removing empty spectrum: " + spectrum);
				imzML.getRun().getSpectrumList().removeSpectrum(spectrum);
			}
			
			System.out.println("Number of spectra after removing: " + imzML.getRun().getSpectrumList().size());
		}
		
		
		try {
			String encoding = "ISO-8859-1";
			
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outputFilename + ".imzML"), encoding);
			BufferedWriter output = new BufferedWriter(out);
			
			output.write("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>\n");
			imzML.outputXML(output, 0);
			
			output.flush();
			output.close();
		} catch (IOException e1) {
			throw new ImzMLConversionException("Error writing imzML file " + outputFilename + ".imzML. " + e1.getLocalizedMessage());
		}
	}
	
	/**
	 * 
	 * @param imzML
	 * @param xPixels
	 * @param yPixels
	 * @return
	 */
	public static PixelLocation[] getPixelLocationFromimzML(ImzML imzML, int xPixels, int yPixels) {
		// Check the number of spectra that have the same scan start time. This is
		double originalScanStartTime = -1;
		int numSpectraPerPixel = 0;

		SpectrumList oldSpectrumList = imzML.getRun().getSpectrumList();

		for(Spectrum spectrum : oldSpectrumList) {
			CVParam scanStartTimeParam = spectrum.getScanList().getScan(0).getCVParam(Scan.scanStartTimeID);
			double scanStartTime = scanStartTimeParam.getValueAsDouble();

			if(originalScanStartTime == -1) {
				originalScanStartTime = scanStartTime;
			}

			if(originalScanStartTime == scanStartTime)
				numSpectraPerPixel++;
			else
				break;
		}

		System.out.println("Found " + numSpectraPerPixel + " spectra that have the same scan time. >1 assumes ion mobility.");
		
		PixelLocation[] spectrumLocation = new PixelLocation[xPixels*yPixels*numSpectraPerPixel];
		
		String lineScanDirection = imzML.getScanSettingsList().getScanSettings(0).getLineScanDirection().getTerm().getID();
		String scanDirection = imzML.getScanSettingsList().getScanSettings(0).getScanDirection().getTerm().getID();
		String scanPattern = imzML.getScanSettingsList().getScanSettings(0).getScanPattern().getTerm().getID();
		
		if(lineScanDirection.equals(ScanSettings.lineScanDirectionLeftRightID) || 
				lineScanDirection.equals(ScanSettings.lineScanDirectionRightLeftID)) {
		
			// Assume that scanDirection is top down
			int yStart = 0;
			int yEnd = yPixels;
			int yIterate = 1;
			
			if(scanDirection.equals(ScanSettings.scanDirectionBottomUpID)) {
				yStart = yPixels - 1;
				yEnd = -1;
				yIterate = -1;
			}
			
			int spectrumNumber = 0;
			
			for(int yTemp = yStart; yTemp != yEnd; yTemp+=yIterate) {
				int yPixelLocation = yTemp + 1;					
				
				// Assume that lineScanDirection is left right
				int xStart = 0;
				int xEnd = xPixels;
				int xIterate = 1;
				
				// If the lineScanDirection is right left then change
				if(lineScanDirection.equals(ScanSettings.lineScanDirectionRightLeftID)) {
					xStart = xPixels - 1;
					xEnd = -1;
					xIterate = -1;
				}
				
				for(int xTemp = xStart; xTemp != xEnd; xTemp+=xIterate) {
					int xPixelLocation = xStart + 1;
					
					if(scanPattern.equals(ScanSettings.scanPatternFlybackID)) {
						xPixelLocation = xTemp + 1;
					} else if(scanPattern.equals(ScanSettings.scanPatternMeanderingID)) {
						if ((yTemp % 2) == 0) {
							xPixelLocation = xTemp + 1;
						} else {
							xPixelLocation = (xPixels - 1 - xTemp) + 1;
						}
					}
					
					for(int i = 0; i < numSpectraPerPixel; i++) {
						spectrumLocation[spectrumNumber++] = new PixelLocation(xPixelLocation, yPixelLocation, 1);
					}
				}
			}
		} else {
			// If the lineScanDirection is either top down or bottom up
			
			// Assume that scanDirection is left right
			int xStart = 0;
			int xEnd = xPixels;
			int xIterate = 1;
			
			if(scanDirection.equals(ScanSettings.scanDirectionRightLeftID)) {
				xStart = xPixels - 1;
				xEnd = -1;
				xIterate = -1;
			}
			
			int spectrumNumber = 0;
			
			for(int xTemp = xStart; xTemp != xEnd; xTemp+=xIterate) {
				int xPixelLocation = xTemp + 1;					
				
				// Assume that lineScanDirection is left right
				int yStart = 0;
				int yEnd = yPixels;
				int yIterate = 1;
				
				// If the lineScanDirection is top down then change
				if(lineScanDirection.equals(ScanSettings.lineScanDirectionBottomUpID)) {
					yStart = yPixels - 1;
					yEnd = -1;
					yIterate = -1;
				}
				
				for(int yTemp = yStart; yTemp != yEnd; yTemp+=yIterate) {
					int yPixelLocation = yStart + 1;
					
					if(scanPattern.equals(ScanSettings.scanPatternFlybackID)) {
						yPixelLocation = yTemp + 1;
					} else if(scanPattern.equals(ScanSettings.scanPatternMeanderingID)) {
						if ((xTemp % 2) == 0) {
							yPixelLocation = yTemp + 1;
						} else {
							yPixelLocation = (yPixels - 1 - yTemp) + 1;
						}
					}
					
					for(int i = 0; i < numSpectraPerPixel; i++) {
						spectrumLocation[spectrumNumber++] = new PixelLocation(xPixelLocation, yPixelLocation, 1);
					}
				}
			}
		}
		
		return spectrumLocation;
	}
	
	public static PixelLocation[] getPixelLocationFromTextFile(String spectrumLocationFile) throws ImzMLConversionException {		
		int xPixels = 0;
		int yPixels = 0;
		int zPixels = 0;

		ArrayList<PixelLocation> location = new ArrayList<PixelLocation>();

		BufferedReader in;
		
		try {
			in = new BufferedReader(new FileReader(spectrumLocationFile));
		} catch (FileNotFoundException e) {
			throw new ImzMLConversionException("Could not find file " + spectrumLocationFile);
		}

		String line;

		try {
			while((line= in.readLine()) != null) {
				String[] coords = null;
	
				if(line.contains(","))
					coords = line.split(",");
				else
					coords = line.split("\\s+");					
	
				int curX = Integer.parseInt(coords[0]);
				int curY = Integer.parseInt(coords[1]);
				int curZ = 1;
	
				if(coords.length == 3)
					curZ = Integer.parseInt(coords[2]);
	
				if(curX > xPixels)
					xPixels = curX;
				if(curY > yPixels)
					yPixels = curY;
				if(curZ > zPixels)
					zPixels = curZ;
	
				location.add(new PixelLocation(curX, curY, curZ));
			}
	
			in.close();
		} catch (IOException e) {
			throw new ImzMLConversionException("Error reading from " + spectrumLocationFile + ". " + e.getLocalizedMessage());
		}

		PixelLocation[] spectrumLocation = new PixelLocation[location.size()];

		for(int i = 0; i < spectrumLocation.length; i++)
			spectrumLocation[i] = location.get(i);

		return spectrumLocation;
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

		} catch(SAXException se) {
			se.printStackTrace();
		} catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}

		// Check the number of spectra that have the same scan start time. This is
		double originalScanStartTime = -1;
		int numSpectraPerPixel = 0;
		
		for(Spectrum spectrum : oldSpectrumList) {
			CVParam scanStartTimeParam = spectrum.getScanList().getScan(0).getCVParam(Scan.scanStartTimeID);
			double scanStartTime = scanStartTimeParam.getValueAsDouble();
			
			if(originalScanStartTime == -1) {
				originalScanStartTime = scanStartTime;
			}
			
			if(originalScanStartTime == scanStartTime)
				numSpectraPerPixel++;
			else
				break;
		}
		
		System.out.println("Found " + numSpectraPerPixel + " spectra per pixel");
		
		PixelLocation[] spectrumLocation = handler.getPatternDefinition().convertToPixelLocations(oldSpectrumList, numSpectraPerPixel);

		for(int i = 0; i < spectrumLocation.length; i++) {
			if(spectrumLocation[i].getX() > xPixels)
				xPixels = spectrumLocation[i].getX();
			if(spectrumLocation[i].getY() > yPixels)
				yPixels = spectrumLocation[i].getY();
			if(spectrumLocation[i].getZ() > zPixels)
				zPixels = spectrumLocation[i].getZ();
		}

		return spectrumLocation;
	}
	
	public static void convertmzMLToimzML(String outputFilename, ImzML imzML, 
			OBO obo, String[] mzMLFilenames, int fileOrganisation, int xPixels, int yPixels,
			PixelLocation[] spectrumLocation,
			boolean compress, boolean removeEmptySpectra, CVParam mzArrayDataType, CVParam intensityArrayDataType) throws ImzMLConversionException, CVParamAccessionNotFoundException {
		int x = 1;
		int y = 1;
		
		int zPixels = 1;
		
		long offset = 0;
		
		// TODO: MAKE THIS BETTER - ONLY HERE TO PLEASE BIOMAP AND DATACUBE EXPLORER
		ReferenceableParamGroup rpgmzArray = imzML.getReferenceableParamGroupList().getReferenceableParamGroup("mzArray");
		
		if(rpgmzArray == null) {
			rpgmzArray = new ReferenceableParamGroup("mzArray");
		
			rpgmzArray.addCVParam(new StringCVParam(obo.getTerm(BinaryDataArray.mzArrayID), ""));	
			rpgmzArray.addCVParam(mzArrayDataType);
			
			imzML.getReferenceableParamGroupList().addReferenceableParamGroup(rpgmzArray);
		} else {
			rpgmzArray.removeChildOfCVParam(BinaryDataArray.dataTypeID);
			rpgmzArray.addCVParam(mzArrayDataType);
		}
				
		ReferenceableParamGroup rpgintensityArray = imzML.getReferenceableParamGroupList().getReferenceableParamGroup("intensityArray");
		
		// Check to see if Bruker conversion has been used
		if(rpgintensityArray == null)
			rpgintensityArray = imzML.getReferenceableParamGroupList().getReferenceableParamGroup("intensities");
		
		// If it's still null then create one
		if(rpgintensityArray == null) {
			rpgintensityArray = new ReferenceableParamGroup("intensityArray");
		
			rpgintensityArray.addCVParam(new StringCVParam(obo.getTerm(BinaryDataArray.intensityArrayID), ""));
			rpgintensityArray.addCVParam(intensityArrayDataType);
			
			imzML.getReferenceableParamGroupList().addReferenceableParamGroup(rpgintensityArray);
		} else {
			rpgintensityArray.removeChildOfCVParam(BinaryDataArray.dataTypeID);
			rpgintensityArray.addCVParam(intensityArrayDataType);
		}
		
		int mzArrayDataTypeInBytes = BinaryDataArray.getDataTypeInBytes(mzArrayDataType);
		int intensityArrayDataTypeInBytes = BinaryDataArray.getDataTypeInBytes(intensityArrayDataType);
		
		// Add in the data processing describing the conversion
		Software imzMLConverter = imzML.getSoftwareList().getSoftware("imzMLConverter");
		
		if(imzMLConverter == null) {
			imzMLConverter = new Software("imzMLConverter", ImzMLConverterOld.version);
			
			imzML.getSoftwareList().addSoftware(imzMLConverter);
		} else
			imzMLConverter.setVersion(ImzMLConverterOld.version);
		
		DataProcessing conversionToImzML = imzML.getDataProcessingList().getDataProcessing("conversionToImzML");
		
		if(conversionToImzML == null) {
			conversionToImzML = new DataProcessing("conversionToImzML");
			conversionToImzML.addProcessingMethod(new ProcessingMethod(1, imzMLConverter));
			conversionToImzML.getProcessingMethod(0).addCVParam(new StringCVParam(obo.getTerm(ProcessingMethod.fileFormatConversionID), "Conversion from mzML to imzML"));
			
			imzML.getDataProcessingList().addDataProcessing(conversionToImzML);
		} else 
			conversionToImzML.getProcessingMethod(0).setSoftwareRef(imzMLConverter);
		
		// Reset the imzML sourceFileList & spectrumList & chromatogramList
		SpectrumList oldSpectrumList = imzML.getRun().getSpectrumList();
		
		
		//imzML.getFileDescription().setSourceFileList(new SourceFileList(0));
		imzML.getRun().setSpectrumList(new SpectrumList(0, imzML.getRun().getSpectrumList().getDefaultDataProcessingRef()));
		imzML.getRun().setChromatogramList(new ChromatogramList(0, imzML.getRun().getSpectrumList().getDefaultDataProcessingRef()));
		
		// Remove any old compression values if any
		rpgmzArray.removeChildOfCVParam(BinaryDataArray.compressionTypeID);
		rpgintensityArray.removeChildOfCVParam(BinaryDataArray.compressionTypeID);
		
		if(compress) {
			rpgmzArray.addCVParam(new EmptyCVParam(obo.getTerm(BinaryDataArray.zlibCompressionID)));
			rpgintensityArray.addCVParam(new EmptyCVParam(obo.getTerm(BinaryDataArray.zlibCompressionID)));
		} else {
			rpgmzArray.addCVParam(new EmptyCVParam(obo.getTerm(BinaryDataArray.noCompressionID)));
			rpgintensityArray.addCVParam(new EmptyCVParam(obo.getTerm(BinaryDataArray.noCompressionID)));
		}
		
		// TODO: sourceFileList
		
		// Add in x and y pixel counts variables
		ScanSettingsList scanSettingsList = imzML.getScanSettingsList();
		
		if(scanSettingsList == null) {
			scanSettingsList = new ScanSettingsList(0);		
			imzML.setScanSettingsList(scanSettingsList);
		}
		
		// TODO: Should it be the first scanSettings? One for each experiment?
		ScanSettings scanSettings = scanSettingsList.getScanSettings(0);
		
		if(scanSettings == null) {
			scanSettings= new ScanSettings("scanSettings1");
			scanSettingsList.addScanSettings(scanSettings);
		}
				
		// Open the .ibd data stream
		DataOutputStream binaryDataStream = null;
		try {
			binaryDataStream = new DataOutputStream(new FileOutputStream(outputFilename + ".ibd"));
		} catch (FileNotFoundException e2) {
			throw new ImzMLConversionException("Could not open file " + outputFilename + ".ibd");
		}
		
		String uuid = UUID.randomUUID().toString().replace("-", "");
		System.out.println(uuid);
		
		// Add UUID to the imzML file
		imzML.getFileDescription().getFileContent().removeChildOfCVParam(FileContent.ibdIdentificationID);
		imzML.getFileDescription().getFileContent().addCVParam(new StringCVParam(obo.getTerm(FileContent.uuidIdntificationID), uuid));
		
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
		offset = binaryDataStream.size();
		
		// Remove old values of pixel counts if they exist
		scanSettings.removeChildOfCVParam(ScanSettings.maxCountPixelXID);
		scanSettings.removeChildOfCVParam(ScanSettings.maxCountPixelYID);
		
		scanSettings.addCVParam(new StringCVParam(obo.getTerm(ScanSettings.maxCountPixelXID), ""+xPixels));
		scanSettings.addCVParam(new StringCVParam(obo.getTerm(ScanSettings.maxCountPixelYID), ""+yPixels));
		//scanSettings.addCVParam(new CVParam(obo.getTerm(ScanSettings.maxCountPixelZID), ""+zPixels));
		
		String lineScanDirection = imzML.getScanSettingsList().getScanSettings(0).getLineScanDirection().getTerm().getID();
		
		int currentSpectrumIndex = 0;
		int currentmzMLFile = 0;
		
		for(String mzMLFilename : mzMLFilenames) {
			if(currentSpectrumIndex >= spectrumLocation.length)
				break;
			
//			int[] order = new int[0];
			int maxSpectra = 0;
			int currentFileSpectrumIndex = 0;
    		
			// Sort out the order of the spectra as they appear in the mzML file
    		if(fileOrganisation == rowPerFile) {
//    			currentSpectrumIndex = (y-1) * xPixels;
    			
    			if(lineScanDirection.equals(ScanSettings.lineScanDirectionLeftRightID) || 
    					lineScanDirection.equals(ScanSettings.lineScanDirectionRightLeftID)) {
    				maxSpectra = xPixels;
    			} else {
    				maxSpectra = yPixels;
    			}
    			
//    			order = new int[xPixels];
//    			
//    			for(int i = 0; i < xPixels; i++) {
//    				order[i] = i;
//    			} 
    		} else if(fileOrganisation == oneFile) {
//    			maxSpectra = xPixels * yPixels;
    			maxSpectra = spectrumLocation.length;
//				order = new int[xPixels * yPixels];
//				int curNum = 0;
//
//				for (int yLoc = 0; yLoc < yPixels; yLoc++) {
//					for (int xLoc = 0; xLoc < xPixels; xLoc++) {
//
//						if ((yLoc % 2) == 0) {
//							order[curNum] = (yLoc * xPixels) + xLoc + 1;
//						} else {
//							order[curNum] = (yLoc * xPixels) + (xPixels - 1 - xLoc) + 1;
//						}
//
//						curNum++;
//					}
//				}
    		} else if(fileOrganisation == pixelPerFile) {
    			maxSpectra = 1;
//    			order = new int[1];
//    			
//    			order[0] = 0;
    		}

	        try {
	    		File temporaryBinaryFile = new File(mzMLFilename + ".tmp");
		    	MzMLHandler handler = new MzMLHandler(obo, temporaryBinaryFile);
		    		
		    	SAXParserFactory spf = SAXParserFactory.newInstance();
		    	try {
		    		
		    		//get a new instance of parser
		    		SAXParser sp = spf.newSAXParser();
		    		
		    		logger.info("Parsing " + mzMLFilename);
		    		
		    		File mzMLFile = new File(mzMLFilename);
		    		
		    		//parse the file and also register this class for call backs
		    		sp.parse(mzMLFile, handler);
		    		
		    	} catch(SAXException se) {
		    		throw new ImzMLConversionException("Error parsing " + mzMLFilename + ". " + se.getLocalizedMessage());
		    	} catch(ParserConfigurationException pce) {
		    		throw new ImzMLConversionException("Error parsing " + mzMLFilename + ". " + pce.getLocalizedMessage());
		    	} catch (IOException ie) {
		    		throw new ImzMLConversionException("Error parsing " + mzMLFilename + ". " + ie.getLocalizedMessage());
		    	} catch (InvalidMzML im) {
		    		throw new ImzMLConversionException("Invalid mzML file " + mzMLFilename + ". " + im.getLocalizedMessage());
		    	}
		    	
		    	MzML currentmzML = handler.getmzML();
		    	
		    	// TODO: Add all referenceParamGoups - TEMPORARY FIX
		    	for(ReferenceableParamGroup rpg : currentmzML.getReferenceableParamGroupList())
		    		imzML.getReferenceableParamGroupList().addReferenceableParamGroup(rpg);
		    	
		    	// Add the sourceFile to the sourceFileList
				File file = new File(mzMLFilename);
				String filenameID = "mzML" + currentmzMLFile++;
				SourceFile sourceFile = new SourceFile(filenameID, file.getParentFile().toURI().toString(), file.getName());
				
				if(imzML.getFileDescription().getSourceFileList() == null)
					imzML.getFileDescription().setSourceFileList(new SourceFileList(1));
					
				imzML.getFileDescription().getSourceFileList().addSourceFile(sourceFile);
				
				sourceFile.addCVParam(new StringCVParam(obo.getTerm(SourceFile.sha1FileChecksumType), ImzMLConverterOld.calculateSHA1(mzMLFilename)));
				
				// Add the native spectrum format
				FileDescription currentFileDescription = currentmzML.getFileDescription();
				
				if(currentFileDescription.getSourceFileList() != null) {
					sourceFile.addCVParam(currentFileDescription.getSourceFileList().getSourceFile(0).getCVParamOrChild(SourceFile.nativeSpectrumIdentifierFormat));
					// TODO: Checksum					
				}
				sourceFile.addCVParam(new StringCVParam(obo.getTerm(SourceFile.mzMLFileFormat), ""));
		    	
		    	long prevOffset = offset;
		    	
		    	for(int i = 0; i < maxSpectra; i++) {
		    		if(currentSpectrumIndex >= spectrumLocation.length)
						break;
		    		
		    		Spectrum spectrum = currentmzML.getRun().getSpectrumList().getSpectrum(currentFileSpectrumIndex++);
		    		PixelLocation location = spectrumLocation[currentSpectrumIndex++];
		    		
		    		// If there isn't binaryDataList then don't copy over the spectrum (for DatacubeExplorer)
		    		if(spectrum != null && location.getX() != -1 && location.getY() != -1 && spectrum.getBinaryDataArrayList().size() > 0) {
			    		imzML.getRun().getSpectrumList().addSpectrum(spectrum);
			    		
			    		// Copy over data to .ibd stream if any
			    		BinaryDataArrayList binaryDataArrayList = spectrum.getBinaryDataArrayList();
			    		
			    		if(binaryDataArrayList == null) {
			    			binaryDataArrayList = new BinaryDataArrayList(2);
			    			spectrum.setBinaryDataArrayList(binaryDataArrayList);
			    			
			    			// m/z
			    			BinaryDataArray mzBinaryDataArray = new BinaryDataArray(0);
			    			mzBinaryDataArray.addCVParam(new EmptyCVParam(obo.getTerm(BinaryDataArray.mzArrayID)));
			    			mzBinaryDataArray.addCVParam(new EmptyCVParam(obo.getTerm(BinaryDataArray.doublePrecisionID)));
			    			mzBinaryDataArray.addCVParam(new EmptyCVParam(obo.getTerm(BinaryDataArray.noCompressionID)));
			    			
			    			binaryDataArrayList.addBinaryDataArray(mzBinaryDataArray);
			    			
			    			// Counts
			    			BinaryDataArray countsBinaryDataArray = new BinaryDataArray(0);
			    			countsBinaryDataArray.addCVParam(new EmptyCVParam(obo.getTerm(BinaryDataArray.intensityArrayID)));
			    			countsBinaryDataArray.addCVParam(new EmptyCVParam(obo.getTerm(BinaryDataArray.doublePrecisionID)));
			    			countsBinaryDataArray.addCVParam(new EmptyCVParam(obo.getTerm(BinaryDataArray.noCompressionID)));		    			
			    			
			    			binaryDataArrayList.addBinaryDataArray(countsBinaryDataArray);
			    		}
			    		
			    		for(int j = 0; j < binaryDataArrayList.size(); j++) {
			    			BinaryDataArray binaryDataArray = binaryDataArrayList.getBinaryDataArray(j);
			    			Binary binary = binaryDataArray.getBinary();
			    					    
//			    			System.out.println("Binary: " + binary);
			    			
			    			// Add the m/z  
			    			if(binaryDataArray.getCVParam(BinaryDataArray.mzArrayID) != null) {
			    				if(binary != null)
				    				offset = binary.copyDataToDataStream(binaryDataStream, offset, compress, mzArrayDataType);
	
			    				binaryDataArray.addReferenceableParamGroupRef(new ReferenceableParamGroupRef(rpgmzArray));
			    				binaryDataArray.removeCVParam(BinaryDataArray.mzArrayID);
			    				
			    				binaryDataArray.addCVParam(new StringCVParam(obo.getTerm(BinaryDataArray.externalArrayLengthID), "" + ((offset - prevOffset) / mzArrayDataTypeInBytes)));
			    			} else if(binaryDataArray.getCVParam(BinaryDataArray.intensityArrayID) != null) {
			    				if(binary != null)
				    				offset = binary.copyDataToDataStream(binaryDataStream, offset, compress, intensityArrayDataType);
			    				
			    				binaryDataArray.addReferenceableParamGroupRef(new ReferenceableParamGroupRef(rpgintensityArray));
			    				binaryDataArray.removeCVParam(BinaryDataArray.intensityArrayID);
			    				
				    			binaryDataArray.addCVParam(new StringCVParam(obo.getTerm(BinaryDataArray.externalArrayLengthID), "" + ((offset - prevOffset) / intensityArrayDataTypeInBytes)));
			    			} else if(binary != null)
			    				offset = binary.copyDataToDataStream(binaryDataStream, offset, compress);	    			
	
		    				binaryDataArray.removeChildOfCVParam(BinaryDataArray.compressionTypeID);
		    				binaryDataArray.removeChildOfCVParam(BinaryDataArray.dataTypeID);
			    			
			    			// Add binary data values to cvParams
			    			binaryDataArray.addCVParam(new StringCVParam(obo.getTerm(BinaryDataArray.externalEncodedLengthID), "" + (offset - prevOffset)));
			    			binaryDataArray.addCVParam(new StringCVParam(obo.getTerm(BinaryDataArray.externalDataID), "true"));
			    			binaryDataArray.addCVParam(new StringCVParam(obo.getTerm(BinaryDataArray.externalOffsetID), "" + prevOffset));
			    			
			    			prevOffset = offset;
			    		}
			    		
			    		// TODO: Add position values to cvParams
			    		ScanList scanList = spectrum.getScanList();
			    		
			    		if(scanList == null) {
			    			scanList = new ScanList(0);
			    			spectrum.setScanList(scanList);
			    		}
			    		
			    		if(scanList.size() == 0) {
			    			Scan scan = new Scan();
			    			
			    			scanList.addScan(scan);
			    		} 
			    		
			    		Scan scan = scanList.getScan(0);
			    		scan.addCVParam(new StringCVParam(obo.getTerm(Scan.positionXID), ""+location.getX()));
			    		scan.addCVParam(new StringCVParam(obo.getTerm(Scan.positionYID), ""+location.getY()));
			    		scan.addCVParam(new StringCVParam(obo.getTerm(Scan.positionZID), ""+location.getZ()));
		    		}
		    		
		    		x++;
		    		if(x > xPixels) {
		    			x = 1;
		    			y++;		    			

//						System.out.println((((((y-1) * xPixels) + x) * 100) / (yPixels*xPixels)) + "%");
		    		}
		    		
		    		if(progressBar != null) {
						progressBar.setSelection(((((y-1) * xPixels) + x) * 100) / (yPixels*xPixels));
						progressBar.update();
						progressBar.redraw();
						
						while(progressBar.getDisplay().readAndDispatch());						
					}
		    			
		    	}
		    	
		    	currentmzML = null;
		    	handler.deleteTemporaryFile();
	        } catch (FileNotFoundException fnfe) {
	        	throw new ImzMLConversionException("Could not find the file " + mzMLFilename);
	        }
		}
		
		try {
			binaryDataStream.close();
		} catch (IOException e) {
			throw new ImzMLConversionException("Error closing " + outputFilename + ".ibd");
		}
		
		// Remove empty spectra
		if(removeEmptySpectra) {
			System.out.println("Checking spectra to remove...");
					
			ArrayList<Spectrum> spectraToRemove = new ArrayList<Spectrum>();
					
			for(Spectrum spectrum : imzML.getRun().getSpectrumList()) {
				if(spectrum.getBinaryDataArrayList().getBinaryDataArray(0) != null &&
						spectrum.getBinaryDataArrayList().getBinaryDataArray(0).getEncodedLength() == 0) {
					spectraToRemove.add(spectrum);
				}
			}
					
			System.out.println("Number of spectra before removing: " + imzML.getRun().getSpectrumList().size());
			
			for(Spectrum spectrum : spectraToRemove) {
				System.out.println("Removing empty spectrum: " + spectrum);
				imzML.getRun().getSpectrumList().removeSpectrum(spectrum);
			}
					
			System.out.println("Number of spectra after removing: " + imzML.getRun().getSpectrumList().size());
		}
		
		
		String sha1Hash = calculateSHA1(outputFilename + ".ibd");
		scanSettings.removeChildOfCVParam(FileContent.ibdChecksumID);


		System.out.println("SHA-1 Hash: " + sha1Hash);
		imzML.getFileDescription().getFileContent().addCVParam(new StringCVParam(obo.getTerm(FileContent.sha1ChecksumID), sha1Hash));

		imzML.write(outputFilename + ".imzML");
		
		if(progressBar != null) {
			progressBar.setSelection(100);
			progressBar.update();
			progressBar.redraw();
			
			while(progressBar.getDisplay().readAndDispatch());						
		}
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
			byte[] buffer = new byte[1024*1024];
			int bytesRead = 0;

			MessageDigest md = MessageDigest.getInstance("SHA-1");

			do {
				bytesRead = dataStream.read(buffer);

				if(bytesRead > 0)
					md.update(buffer, 0, bytesRead);
			} while(bytesRead > 0);

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
			throw new ImzMLConversionException("Failed generating SHA-1 hash. Failed to read data from "  + filename + e.getMessage());
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
					+ Character.digit(s.charAt(i+1), 16));
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

			sb.append((char)Hexhars[v >> 4]);
			sb.append((char)Hexhars[v & 0xf]);
			}
		
		return sb.toString();
	}
}