/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlconverter.IONTOF.IONTOFProperties;
import com.alanmrace.jimzmlconverter.IONTOF.IONTOFProperty;
import com.alanmrace.jimzmlparser.exceptions.ImzMLWriteException;
import com.alanmrace.jimzmlparser.imzML.ImzML;
import com.alanmrace.jimzmlparser.mzML.BinaryDataArray;
import com.alanmrace.jimzmlparser.mzML.BinaryDataArrayList;
import com.alanmrace.jimzmlparser.mzML.DataProcessing;
import com.alanmrace.jimzmlparser.mzML.DataProcessingList;
import com.alanmrace.jimzmlparser.mzML.DoubleCVParam;
import com.alanmrace.jimzmlparser.mzML.EmptyCVParam;
import com.alanmrace.jimzmlparser.mzML.FileContent;
import com.alanmrace.jimzmlparser.mzML.FileDescription;
import com.alanmrace.jimzmlparser.mzML.InstrumentConfiguration;
import com.alanmrace.jimzmlparser.mzML.InstrumentConfigurationList;
import com.alanmrace.jimzmlparser.mzML.IntegerCVParam;
import com.alanmrace.jimzmlparser.mzML.LongCVParam;
import com.alanmrace.jimzmlparser.mzML.ReferenceableParamGroup;
import com.alanmrace.jimzmlparser.mzML.ReferenceableParamGroupList;
import com.alanmrace.jimzmlparser.mzML.ReferenceableParamGroupRef;
import com.alanmrace.jimzmlparser.mzML.Run;
import com.alanmrace.jimzmlparser.mzML.Scan;
import com.alanmrace.jimzmlparser.mzML.ScanList;
import com.alanmrace.jimzmlparser.mzML.ScanSettings;
import com.alanmrace.jimzmlparser.mzML.ScanSettingsList;
import com.alanmrace.jimzmlparser.mzML.SoftwareList;
import com.alanmrace.jimzmlparser.mzML.SourceFile;
import com.alanmrace.jimzmlparser.mzML.SourceFileList;
import com.alanmrace.jimzmlparser.mzML.Spectrum;
import com.alanmrace.jimzmlparser.mzML.SpectrumList;
import com.alanmrace.jimzmlparser.mzML.StringCVParam;
import com.alanmrace.jimzmlparser.obo.OBO;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author amr1
 */
public class GRDToImzMLConverter extends ImzMLConverter {

    private static final Logger logger = Logger.getLogger(GRDToImzMLConverter.class.getName());

    private IONTOFProperties properties;

    private int numPixelsX;
    private int numPixelsY;

    private double k0;
    private double sf;

    private ReferenceableParamGroupRef intensityArrayRef;
    private ReferenceableParamGroupRef mzArrayRef;
    private SpectrumList spectrumList;

    public GRDToImzMLConverter(String outputFilename, String[] inputFilenames) {
        super(outputFilename, inputFilenames);
    }

    public void setPropertiesFile(String propertiesFile) throws IOException {
        properties = IONTOFProperties.parseProperties(propertiesFile);

        IONTOFProperty xDimension = properties.getProperty("Registration.Raster.StageRaster.TotalResolution.X");
        IONTOFProperty yDimension;

        if (xDimension == null) {
            xDimension = properties.getProperty("Registration.Raster.Resolution");
            yDimension = xDimension;
        } else {
            yDimension = properties.getProperty("Registration.Raster.StageRaster.TotalResolution.Y");
        }

        numPixelsX = xDimension.getIntegerValue();
        numPixelsY = yDimension.getIntegerValue();

        logger.log(Level.FINE, "Image size: {0}, {1}", new Object[]{numPixelsX, numPixelsY});

        k0 = properties.getProperty("Context.MassScale.K0").getDoubleValue();
        sf = properties.getProperty("Context.MassScale.SF").getDoubleValue();

        logger.log(Level.FINE, "Calibration parameters, k0 = {0}, sf = {1}", new Object[]{k0, sf});
    }

    @Override
    protected void generateBaseImzML() {
        baseImzML = new ImzML("1.0");

        FileDescription fileDescription = new FileDescription();
        baseImzML.setFileDescription(fileDescription);

        SourceFileList sourceFileList = new SourceFileList(this.inputFilenames.length);
        fileDescription.setSourceFileList(sourceFileList);
        
        // Add source files to the base imzML
        for(int i = 0; i < inputFilenames.length; i++) {
            ImzMLConverter.addSourceFileToImzML(baseImzML, inputFilenames[i], "grd" + i, fileDescription);
            
//            String fileName = inputFilenames[i];
//            File file = new File(fileName);
//            
//            // If the file does not have a parent directory then 
//            File parentFolder = file.getParentFile();
//            String parentDirectory;
//
//            if(parentFolder == null)
//                parentDirectory = "";
//            else
//                parentDirectory = parentFolder.toURI().toString();
//
//
//            SourceFile sourceFile = new SourceFile("grd" + i, parentDirectory, file.getName());
//            
//            sourceFileList.addSourceFile(sourceFile);
//            
//            sourceFile.addCVParam(new EmptyCVParam(obo.getTerm("MS:1000824")));
        }
        
        FileContent fileContent = new FileContent();
        fileDescription.setFileContent(fileContent);

        ReferenceableParamGroupList rpgl = new ReferenceableParamGroupList(2);
        baseImzML.setReferenceableParamGroupList(rpgl);

        ReferenceableParamGroup mzArrayGroup = new ReferenceableParamGroup();
        rpgl.addReferenceableParamGroup(mzArrayGroup);
        // m/z array
        mzArrayGroup.addCVParam(new EmptyCVParam(obo.getTerm("MS:1000514")));
        // 64-bit float
        mzArrayGroup.addCVParam(new EmptyCVParam(obo.getTerm("MS:1000523")));
        // no compression
        mzArrayGroup.addCVParam(new EmptyCVParam(obo.getTerm("MS:1000576")));
        mzArrayRef = new ReferenceableParamGroupRef(mzArrayGroup);

        ReferenceableParamGroup intensityArrayGroup = new ReferenceableParamGroup();
        rpgl.addReferenceableParamGroup(intensityArrayGroup);
        // intensity array
        intensityArrayGroup.addCVParam(new EmptyCVParam(obo.getTerm("MS:1000515")));
        // 64-bit float
        intensityArrayGroup.addCVParam(new EmptyCVParam(obo.getTerm("MS:1000523")));
        // no compression
        intensityArrayGroup.addCVParam(new EmptyCVParam(obo.getTerm("MS:1000576")));
        intensityArrayRef = new ReferenceableParamGroupRef(intensityArrayGroup);

        SoftwareList softwareList = new SoftwareList(0);
        baseImzML.setSoftwareList(softwareList);

        ScanSettingsList scanSettingsList = new ScanSettingsList(1);
        baseImzML.setScanSettingsList(scanSettingsList);

        ScanSettings scanSettings = new ScanSettings("TOF-SIMS");
        scanSettingsList.addScanSettings(scanSettings);

        // Max count of pixels x
        scanSettings.addCVParam(new IntegerCVParam(obo.getTerm("IMS:1000042"), numPixelsX));
        // Max count of pixels y
        scanSettings.addCVParam(new IntegerCVParam(obo.getTerm("IMS:1000043"), numPixelsY));

        InstrumentConfigurationList instrumentConfigurationList = new InstrumentConfigurationList(0);
        baseImzML.setInstrumentConfigurationList(instrumentConfigurationList);

        InstrumentConfiguration instrumentConfiguration = new InstrumentConfiguration("TOF-SIMS");
        instrumentConfigurationList.addInstrumentConfiguration(instrumentConfiguration);

        DataProcessingList dataProcessingList = new DataProcessingList(0);
        baseImzML.setDataProcessingList(dataProcessingList);

        DataProcessing dataProcessing = new DataProcessing("conversionToimzML");
        dataProcessingList.addDataProcessing(dataProcessing);

        Run run = new Run(inputFilenames[0], instrumentConfiguration);
        baseImzML.setRun(run);

        spectrumList = new SpectrumList(0, dataProcessing);
        run.setSpectrumList(spectrumList);
    }

    @Override
    protected String getConversionDescription() {
        return "ION-TOF GRD to imzML";
    }

    @Override
    protected void generatePixelLocations() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void convert() {
        generateBaseImzML();

        HashMap<Long, Short>[][] pixelData = new HashMap[numPixelsY][numPixelsX];

        // First pass though
        // Generate TIC (max # m/z values per spectrum)
        // Generate full m/z list
        Set<Long> fullmzList = new HashSet<>();

        int[][] totalIonCountImage = new int[numPixelsY][numPixelsX];
        long totalEvents = 0;

        FileInputStream dis = null;

        try {
            dis = new FileInputStream(new File(inputFilenames[0]));

            int numEventsToProcessAtOnce = 1024;

            ByteBuffer byteBuffer = ByteBuffer.allocate(20 * numEventsToProcessAtOnce);
            byte[] buffer = new byte[20 * numEventsToProcessAtOnce];

            int scanNumber;
            int shotNumber;
            int x = 0;
            int y = 0;

            int read = 0;

            while (dis.available() > 0) {
                read = dis.read(buffer);

                if (read <= 0) {
                    break;
                }

                byteBuffer.rewind();
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN).put(buffer);
                byteBuffer.rewind();

                //System.out.println(read);
                for (int eventNum = 0; eventNum < read / 20; eventNum++) {
                    scanNumber = byteBuffer.getInt();
                    shotNumber = byteBuffer.getInt();

                    x = byteBuffer.getInt();
                    y = byteBuffer.getInt();
                    long tof = byteBuffer.getInt() & 0xFFFFFFFFL;

                    logger.log(Level.FINE, "TOF Event [1st] {0}, {1}, {2}, {3}, {4}", new Object[]{scanNumber, shotNumber, x, y, tof});

                    totalIonCountImage[y][x]++;

                    fullmzList.add(tof);
                    
                    totalEvents++;
                }
            }

            dis.close();
        } catch (IOException ex) {
            Logger.getLogger(GRDToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

        logger.log(Level.FINE, "Found # m/z: {0}", fullmzList.size());

        // Calculate the offset in bytes for each pixel
        long[][] pixelOffset = new long[numPixelsY][numPixelsX];

        for (int y = 0; y < numPixelsY; y++) {
            for (int x = 0; x < numPixelsX; x++) {
                if (y == 0 && x == 0) {
                    continue;
                }

                if (x == 0) {
                    pixelOffset[y][x] = pixelOffset[y - 1][numPixelsX - 1] + totalIonCountImage[y - 1][numPixelsX - 1];
                } else {
                    pixelOffset[y][x] = pixelOffset[y][x - 1] + totalIonCountImage[y][x - 1];
                }
            }
        }

        // Second pass through
        // Generate total spectrum 
        // Combine data belonging to individual spectra
        HashMap<Long, Integer> totalSpectrum = new HashMap<>(fullmzList.size());

        int[][] pixelEventsProcessed = new int[numPixelsY][numPixelsX];

        RandomAccessFile raf;
        DataOutputStream dos;

        try {
            dis = new FileInputStream(new File(inputFilenames[0]));
            raf = new RandomAccessFile(new File(outputFilename + ".ibdtmp"), "rw");
            dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(raf.getFD())));

            raf.setLength(totalEvents * 4);
            
            int numEventsToProcessAtOnce = 1024;

            ByteBuffer byteBuffer = ByteBuffer.allocate(20 * numEventsToProcessAtOnce);
            byte[] buffer = new byte[20 * numEventsToProcessAtOnce];

            int scanNumber;
            int shotNumber;
            int x = 0;
            int y = 0;

            int read = 0;

            while (dis.available() > 0) {
                read = dis.read(buffer);

                if (read <= 0) {
                    break;
                }

                byteBuffer.rewind();
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN).put(buffer);
                byteBuffer.rewind();

                //System.out.println(read);
                for (int eventNum = 0; eventNum < read / 20; eventNum++) {
                    scanNumber = byteBuffer.getInt();
                    shotNumber = byteBuffer.getInt();

                    x = byteBuffer.getInt();
                    y = byteBuffer.getInt();
                    int tof = byteBuffer.getInt();
                    long tofL = tof & 0xFFFFFFFFL;

                    logger.log(Level.FINE, "TOF Event [2nd] {0}, {1}, {2}, {3}, {4} ({5})", new Object[]{scanNumber, shotNumber, x, y, tof, tofL});
                    
                    if (totalSpectrum.containsKey(tofL)) {
                        totalSpectrum.put(tofL, totalSpectrum.get(tofL) + 1);
                    } else {
                        totalSpectrum.put(tofL, 1);
                    }

                    long outputLocation = (pixelOffset[y][x] + pixelEventsProcessed[y][x]) * 4;
                    
                    logger.log(Level.FINE, "Writing {0} to {1}", new Object[] {tof, outputLocation});

                    raf.seek(outputLocation);
                    raf.writeInt(tof);
                    //dos.writeInt(tof);
                    
                    pixelEventsProcessed[y][x]++;
                }
            }

            dos.close();
            raf.close();
            dis.close();

        } catch (IOException ex) {
            Logger.getLogger(GRDToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        } 

        logger.log(Level.FINE, "Written temp");

        // Third pass
        // Go through the new temporary file to organise into spectra 
        try {
            dis = new FileInputStream(new File(outputFilename + ".ibdtmp"));
            dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFilename + ".ibd")));

            long outputOffset = 0;
            
            for (int y = 0; y < numPixelsY; y++) {
                for (int x = 0; x < numPixelsX; x++) {
                    logger.log(Level.FINE, "Pixel ({0}, {1}) has {2} events", new Object[]{x, y, pixelEventsProcessed[y][x]});
                    
                    ByteBuffer byteBuffer = ByteBuffer.allocate(pixelEventsProcessed[y][x] * 4);
                    byte[] buffer = new byte[pixelEventsProcessed[y][x] * 4];

                    dis.read(buffer);

                    byteBuffer.put(buffer);
                    byteBuffer.rewind();

                    HashMap<Long, Integer> spectrumData = new HashMap<Long, Integer>();

                    for (int val = 0; val < pixelEventsProcessed[y][x]; val++) {
                        long tof = byteBuffer.getInt() & 0xFFFFFFFFL;

                        logger.log(Level.FINE, "TOF Event [3rd] {0}, {1}, {2}", new Object[]{x, y, tof});
                        
                        if (spectrumData.containsKey(tof)) {
                            spectrumData.put(tof, spectrumData.get(tof) + 1);
                        } else {
                            spectrumData.put(tof, 1);
                        }
                    }

                    outputOffset = outputSpectrum(spectrumData, x, y, k0, sf,
                            numPixelsX, numPixelsY, obo, mzArrayRef, intensityArrayRef,
                            dos, outputOffset, spectrumList);
                }
            }

            dis.close();
            dos.close();
        } catch (IOException ex) {
            Logger.getLogger(GRDToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        } 

        logger.log(Level.FINE, "Outputting {0} spectra", spectrumList.size());

        try {
            baseImzML.write(outputFilename + ".imzML");
        } catch (ImzMLWriteException ex) {
            Logger.getLogger(GRDToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    public static long outputSpectrum(HashMap<Long, Integer> spectrumData, int x, int y, double k0, double sf,
            int numxPixels, int numyPixels,
            OBO obo, ReferenceableParamGroupRef mzArrayRef, ReferenceableParamGroupRef intensityArrayRef,
            DataOutputStream dos, long outputOffset, SpectrumList spectrumList) throws IOException {
        if (spectrumData != null) {
            SortedSet<Long> keys = new TreeSet<Long>(spectrumData.keySet());

            double[] mzs = new double[keys.size()];
            double[] counts = new double[keys.size()];

            double tic = 0;

            int i = 0;

            for (Long tof : keys) {
                double time = ((tof - k0) / sf);
                double mz = time * time;
                int intensity = spectrumData.get(tof);

                mzs[i] = mz;
                counts[i] = intensity;

                tic += intensity;

                i++;
            }

            int index = (y * numxPixels + x);

//            if (index == 1) {
//                logger.log(Level.INFO, "m/z {0}", mzs[0]);
//                logger.log(Level.INFO, "counts {0}", counts[0]);
//            }

            Spectrum spectrum = new Spectrum("index=" + index, 0, index);
            // MS1 spectrum
            spectrum.addCVParam(new EmptyCVParam(obo.getTerm("MS:1000579")));
            // Total Ion Current
            spectrum.addCVParam(new DoubleCVParam(obo.getTerm("MS:1000285"), tic));

            ScanList scanList = new ScanList(1);
            spectrum.setScanList(scanList);

            Scan scan = new Scan();
            scanList.addScan(scan);

            // X Position
            scan.addCVParam(new IntegerCVParam(obo.getTerm("IMS:1000050"), (x + 1)));
            // Y Position
            scan.addCVParam(new IntegerCVParam(obo.getTerm("IMS:1000051"), (y + 1)));

            BinaryDataArrayList bdal = new BinaryDataArrayList(2);
            spectrum.setBinaryDataArrayList(bdal);

            BinaryDataArray mzArray = new BinaryDataArray(mzs.length * 8);
            BinaryDataArray intensityArray = new BinaryDataArray(counts.length * 8);

            bdal.addBinaryDataArray(mzArray);
            bdal.addBinaryDataArray(intensityArray);

            mzArray.addReferenceableParamGroupRef(mzArrayRef);
            // External array length
            mzArray.addCVParam(new LongCVParam(obo.getTerm("IMS:1000103"), mzs.length));
            // External encoded length
            mzArray.addCVParam(new LongCVParam(obo.getTerm("IMS:1000104"), (mzs.length * 8)));
            // External data
            mzArray.addCVParam(new StringCVParam(obo.getTerm("IMS:1000101"), "true"));
            // External offset
            mzArray.addCVParam(new LongCVParam(obo.getTerm("IMS:1000102"), outputOffset));
            
            // Increment the offset by the encoded length
            outputOffset += (mzs.length * 8);

            ByteBuffer buffer = ByteBuffer.allocate(8 * mzs.length);
            for (double mz : mzs) {
                buffer.putDouble(mz);
            }

            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.rewind();

            if (index == 13250) {
                System.out.print("(" + x + ", " + y + ") ");
            }

            for (double mz : mzs) {
                dos.writeDouble(buffer.getDouble());

                if (index == 13250) {
                    System.out.print(mz + ", ");
                }
            }

            if (index == 13250) {
                System.out.println();
            }

            intensityArray.addReferenceableParamGroupRef(intensityArrayRef);
            // External array length
            intensityArray.addCVParam(new LongCVParam(obo.getTerm("IMS:1000103"), counts.length));
            // External encoded length
            intensityArray.addCVParam(new LongCVParam(obo.getTerm("IMS:1000104"), (counts.length * 8)));
            // External data
            intensityArray.addCVParam(new StringCVParam(obo.getTerm("IMS:1000101"), "true"));
            // External offset
            intensityArray.addCVParam(new LongCVParam(obo.getTerm("IMS:1000102"), outputOffset));
            
            // Increment the offset by the encoded length
            outputOffset += counts.length * 8;

            buffer.rewind();
            buffer.order(ByteOrder.BIG_ENDIAN);
            for (double count : counts) {
                buffer.putDouble(count);
            }

            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.rewind();

            for (double count : counts) {
                dos.writeDouble(buffer.getDouble());
            }

            spectrumList.addSpectrum(spectrum);
        }
        
        return outputOffset;
    }

    public static void main(String[] args) {
        try {
            GRDToImzMLConverter converter = new GRDToImzMLConverter("D:\\Rory\\2014_03_17_lipid_standard\\2014_03_17_PC16-0_16-0_20keVAr500_+ve_1.itm.grd", new String[]{"D:\\Rory\\2014_03_17_lipid_standard\\2014_03_17_PC16-0_16-0_20keVAr500_+ve_1.itm.grd"});
            converter.setPropertiesFile("D:\\Rory\\2014_03_17_lipid_standard\\2014_03_17_PC16-0_16-0_20keVAr500_+ve_1.properties.txt");
            converter.convert();
        } catch (IOException ex) {
            Logger.getLogger(GRDToImzMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
