/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlconverter.exceptions.ConversionException;
import com.alanmrace.jimzmlparser.imzML.ImzML;
import com.alanmrace.jimzmlparser.mzML.Spectrum;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;
import hdf.hdf5lib.exceptions.HDF5Exception;

/**
 *
 * @author amr1
 */
public class ImzMLToHDF5Converter extends HDF5Converter {

    private static final Logger logger = Logger.getLogger(ImzMLToHDF5Converter.class.getName());

    private ImzML imzML;

    /**
     *
     * @param imzML
     * @param outputFilename
     */
    public ImzMLToHDF5Converter(ImzML imzML, String outputFilename) {
        this.imzML = imzML;
        this.outputFilename = outputFilename;
    }

    /**
     *
     * @throws ConversionException
     */
    @Override
    public void convert() throws ConversionException {
        logger.setLevel(Level.ALL);

        long startTime = System.currentTimeMillis();
        long timeSpentWriting = 0;

        try {
            double[] fullmzList = imzML.getFullmzList();

            if (fullmzList != null) {
                logger.log(Level.FINE, "Full m/z list found with size {0}", fullmzList.length);
            } else {
                logger.log(Level.FINE, "No m/z list found");

                throw new ConversionException("No full m/z list found. Files must be converted with imzMLConverter 2.0 or later for this feature.");
            }

            long file_id = H5.H5Fcreate(outputFilename, HDF5Constants.H5F_ACC_TRUNC,
                    HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);

            // Store the m/z list in the container
            addmzList(file_id, fullmzList);

            // Determine the number of dimensions in the imzML file (should be in range 2 - 5)
            int dimensionality = imzML.getDimensionality();
            int spatialDimensionality = imzML.getSpatialDimensionality();

            // Stored dimensionality collapses all spatial dimensions
            int storedDimensionality = dimensionality - spatialDimensionality + 1;

            // TODO: Consider collapsing m/z dimensions as well
            logger.log(Level.FINE, "Determined dimensionality of data to be {0}", dimensionality);
            logger.log(Level.FINE, "Spatial dimenaionality of data is {0}", spatialDimensionality);

            // Create the necessary 
            long[] dimensionSizes = new long[storedDimensionality];

            int numPixels = imzML.getWidth() * imzML.getHeight() * imzML.getDepth();

            dimensionSizes[0] = numPixels;
            dimensionSizes[1] = fullmzList.length;

            if (storedDimensionality > 2) {
                dimensionSizes[2] = imzML.getNumberOfSpectraPerPixel();

                logger.log(Level.FINE, "Dimenion sizes [2] {0}", dimensionSizes[2]);
            }

            if (chunkSizes == null) {
                chunkSizes = new long[storedDimensionality];

                // TODO: Optimise these and allow use setting
                chunkSizes[0] = numberOfSpectraPerBlock;
                chunkSizes[1] = 5000;

                if (storedDimensionality > 2) {
                    chunkSizes[2] = 50;
                }
            }

            logger.log(Level.FINE, "Starting conversion to HDF5");

            HDF5DataIDs dataset = createDataset(file_id, "data", dimensionSizes, HDF5Constants.H5T_NATIVE_DOUBLE, chunkSizes);

            long[] start = {0, 0, 0};
            long[] stride = {1, 1, 1};
            long[] count = {1, 1, 1};
            long[] block;

            if (storedDimensionality > 2) {
                block = new long[]{numberOfSpectraPerBlock, dimensionSizes[1], dimensionSizes[2]};
            } else {
                block = new long[]{numberOfSpectraPerBlock, dimensionSizes[1]};
            }

//            block = new long[] {1, dimensionSizes[1], 1};
            
            long memspace = H5.H5Screate_simple(dimensionSizes.length, block, null);

//            boolean addedmzList = false;
            int pixelIndex = 0;
            int mobilityIndex = 0;
            int spectrumIndex = 0;

//            for (Spectrum spectrum_ : imzML.getRun().getSpectrumList()) {
//                double[] mzs = spectrum_.getmzArray();
//                double[] intensities = spectrum_.getIntensityArray();
//
//                intensities = putOnFullmzAxis(fullmzList, mzs, intensities);
//
//                logger.log(Level.FINE, "About to select hyperslab for spectrumIndex {0}", spectrumIndex);
//
//                startTime = System.currentTimeMillis();
//
//                H5.H5Sselect_hyperslab(dataset.filespace_id, HDF5Constants.H5S_SELECT_SET,
//                        start, stride, count, block);
//
//                H5.H5Dwrite(dataset.dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE,
//                        memspace, dataset.filespace_id, HDF5Constants.H5P_DEFAULT,
//                        intensities);
//
//                timeSpentWriting += (System.currentTimeMillis() - startTime);
//
//                logger.log(Level.FINE, "Written hyperslab for spectrumIndex {0}", spectrumIndex);
//
//                if (dimensionSizes.length > 2) {
//                    start[2]++;
//
//                    if (start[2] >= dimensionSizes[2]) {
//                        start[2] = 0;
//                        start[0]++;
//                    }
//                } else {
//                    start[0]++;
//                }
//
////                    start[0] += block[0];
////                    spectrumIndex = 0;
//            }

            if(dimensionSizes.length > 2) {
                double[][][] mobilogram = new double[(int) block[0]][(int) dimensionSizes[1]][(int) dimensionSizes[2]];

                for (Spectrum spectrum_ : imzML.getRun().getSpectrumList()) {
                    double[] mzs = spectrum_.getmzArray();
                    double[] intensities = spectrum_.getIntensityArray();

                    intensities = putOnFullmzAxis(fullmzList, mzs, intensities);

                    // Update the mobilogram with new data
                    for (int i = 0; i < intensities.length; i++) {
                        mobilogram[spectrumIndex][i][mobilityIndex] = intensities[i];
                    }

    //                // Add the pixel coordinates to the pixelLocations list to write out later
    //                pixelLocations[pixelIndex][0] = Integer.parseInt(spectrum_.getScanList().getScan(0).getCVParam(Scan.positionXID).getValue());
    //                pixelLocations[pixelIndex][1] = Integer.parseInt(spectrum_.getScanList().getScan(0).getCVParam(Scan.positionYID).getValue());
    //                double tic = 0;
    //
    //                CVParam cvParam = spectrum_.getCVParam(Spectrum.totalIonCurrentID);
    //
    //                if (cvParam != null) {
    //                    tic = Double.parseDouble(cvParam.getValue());
    //                } else {
    //                    CVParam<Double> doubleCVParam = spectrum_.getDoubleCVParam(Spectrum.totalIonCurrentID);
    //
    //                    if (doubleCVParam != null) {
    //                        tic = doubleCVParam.getValue();
    //                    }
    //                }
    //
    //                ticImage[pixelIndex] += tic;
                    mobilityIndex++;

                    if (mobilityIndex >= dimensionSizes[2]) {
                        mobilityIndex = 0;
                        pixelIndex++;
                        spectrumIndex++;

    //	    			start[2] = 0;
                    }

                    if (spectrumIndex >= block[0]) {
                        logger.log(Level.FINE, "About to select hyperslab for spectrumIndex {0}", spectrumIndex);

                        startTime = System.currentTimeMillis();

                        H5.H5Sselect_hyperslab(dataset.filespace_id, HDF5Constants.H5S_SELECT_SET,
                                start, stride, count, block);

                        H5.H5Dwrite(dataset.dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE,
                                memspace, dataset.filespace_id, HDF5Constants.H5P_DEFAULT,
                                mobilogram);

                        timeSpentWriting += (System.currentTimeMillis() - startTime);

                        logger.log(Level.FINE, "Written hyperslab for spectrumIndex {0}", spectrumIndex);

                        start[0] += block[0];

                        spectrumIndex = 0;
                    }
                } 
            } else {
                double[][] spectrum = new double[(int) block[0]][(int) dimensionSizes[1]];

                for (Spectrum spectrum_ : imzML.getRun().getSpectrumList()) {
                    double[] mzs = spectrum_.getmzArray();
                    double[] intensities = spectrum_.getIntensityArray();

                    intensities = putOnFullmzAxis(fullmzList, mzs, intensities);

                    // Update the mobilogram with new data
                    System.arraycopy(intensities, 0, spectrum[spectrumIndex], 0, intensities.length);
                    
                    spectrumIndex++;
                    
                    if (spectrumIndex >= block[0]) {
                        logger.log(Level.FINE, "About to select hyperslab for spectrumIndex {0}", spectrumIndex);

                        startTime = System.currentTimeMillis();

                        H5.H5Sselect_hyperslab(dataset.filespace_id, HDF5Constants.H5S_SELECT_SET,
                                start, stride, count, block);

                        H5.H5Dwrite(dataset.dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE,
                                memspace, dataset.filespace_id, HDF5Constants.H5P_DEFAULT,
                                spectrum);

                        timeSpentWriting += (System.currentTimeMillis() - startTime);

                        logger.log(Level.FINE, "Written hyperslab for spectrumIndex {0}", spectrumIndex);

                        start[0] += block[0];

                        spectrumIndex = 0;
                    }
                }
            }
            closeDataset(dataset);

            logger.log(Level.FINE, "Finished conversion to HDF5");

            H5.H5Fclose(file_id);
        } catch (HDF5Exception | IOException ex) {
            Logger.getLogger(ImzMLToHDF5Converter.class.getName()).log(Level.SEVERE, null, ex);

            //ex.printStackTrace();
            throw new ConversionException(ex.getLocalizedMessage(), ex);
        }

        logger.log(Level.INFO, "Time spent writing: {0}s", ((timeSpentWriting) / 1000.0));
    }

//    public static void main(String args[]) {
//        args = new String[] {"hdf5", "D:\\GitProjects\\jimzMLConverter\\jimzMLConverter\\target\\test-classes\\IM_500_IM_S.raw.imzML"};
//        
//        MainCommand.main(args);
//    }
}
