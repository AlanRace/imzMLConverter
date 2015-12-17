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
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;

/**
 *
 * @author amr1
 */
public class ImzMLToHDF5Converter implements Converter {

    private static final Logger logger = Logger.getLogger(ImzMLToHDF5Converter.class.getName());

    private ImzML imzML;
    private String outputFilename;

    private int gzipCompression = 9;
    private boolean shuffle = false;

    long[] chunkSizes;
    
    int numberOfSpectraPerBlock = 20;
    
    /**
     *
     * @param imzML
     * @param outputFilename
     */
    public ImzMLToHDF5Converter(ImzML imzML, String outputFilename) {
        this.imzML = imzML;
        this.outputFilename = outputFilename;
    }

    private double[] putOnFullmzAxis(double[] fullmzAxis, double[] mzs, double[] intensities) {
        double[] zeroFilled = new double[fullmzAxis.length];

        if (mzs != null && intensities != null) {
            int j = 0;

            for (int i = 0; i < mzs.length; i++) {
                while (j < zeroFilled.length && mzs[i] != fullmzAxis[j]) {
                    j++;
                }

                if (j < zeroFilled.length && mzs[i] == fullmzAxis[j]) {
                    zeroFilled[j] += intensities[i];
                }
            }
        }

        return zeroFilled;
    }
    
    public void setCompressionLevel(int compressionLevel) {
        this.gzipCompression = compressionLevel;
    }
    
    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }
    
    public void setChunkSizes(long[] chunkSizes) {
        this.chunkSizes = chunkSizes;
    }

    /**
     *
     * @throws ConversionException
     */
    @Override
    public void convert() throws ConversionException {
        try {
            double[] fullmzList = imzML.getFullmzList();

            if (fullmzList != null) {
                logger.log(Level.FINE, "Full m/z list found with size {0}", fullmzList.length);
            } else {
                logger.log(Level.FINE, "No m/z list found");
            }

            int file_id = H5.H5Fcreate(outputFilename, HDF5Constants.H5F_ACC_TRUNC,
                    HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);

            if (fullmzList != null) {
                addmzList(file_id, fullmzList);
            }

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

            if(chunkSizes == null) {
                chunkSizes = new long[storedDimensionality];
                
                // TODO: Optimise these and allow use setting
                chunkSizes[0] = 50;
                chunkSizes[1] = 50;
                
                if(storedDimensionality > 2)
                    chunkSizes[2] = 50;
            }
            
            logger.log(Level.FINE, "Starting conversion to HDF5");
            
            HDF5DataIDs dataset = createDataset(file_id, "data", dimensionSizes, HDF5Constants.H5T_NATIVE_DOUBLE, chunkSizes);

            long[] start = {0, 0, 0};
            long[] stride = {1, 1, 1};
            long[] count = {1, 1, 1};
            long[] block;
            
            if(storedDimensionality > 2)
                block = new long[]{numberOfSpectraPerBlock, dimensionSizes[1], dimensionSizes[2]};
            else
                block = new long[]{numberOfSpectraPerBlock, dimensionSizes[1]};

            int memspace = H5.H5Screate_simple(dimensionSizes.length, block, null);

            boolean addedmzList = false;
            int pixelIndex = 0;
            int mobilityIndex = 0;
            int spectrumIndex = 0;

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

                    H5.H5Sselect_hyperslab(dataset.filespace_id, HDF5Constants.H5S_SELECT_SET,
                            start, stride, count, block);

                    H5.H5Dwrite(dataset.dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE,
                            memspace, dataset.filespace_id, HDF5Constants.H5P_DEFAULT,
                            mobilogram);

                    start[0] += block[0];

                    spectrumIndex = 0;
                }
            }

            closeDataset(dataset);
            
            logger.log(Level.FINE, "Finished conversion to HDF5");

            H5.H5Fclose(file_id);
        } catch (HDF5Exception ex) {
            Logger.getLogger(ImzMLToHDF5Converter.class.getName()).log(Level.SEVERE, null, ex);

            //ex.printStackTrace();
            throw new ConversionException(ex.getLocalizedMessage(), ex);
        } catch (IOException ex) {
            Logger.getLogger(ImzMLToHDF5Converter.class.getName()).log(Level.SEVERE, null, ex);

            throw new ConversionException(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Helper methods for HDF5 dataset creation
     */
    private class HDF5DataIDs {

        int dcpl_id = -1;
        int dataset_id = -1;
        int filespace_id = -1;
        int file_id = -1;
    }

    private HDF5DataIDs createDataset(int fileID, String name, long[] dimensions, int type) throws HDF5Exception {
        return createDataset(fileID, name, dimensions, type, dimensions);
    }

    private HDF5DataIDs createDataset(int fileID, String name, long[] dimensions, int type, long[] chunkSize) throws HDF5Exception {
        HDF5DataIDs dataset = new HDF5DataIDs();
        dataset.file_id = fileID;

        // Create the filespace with desired dimensions (as both the initial and maximum size)
        dataset.filespace_id = H5.H5Screate_simple(dimensions.length, dimensions, null);

        dataset.dcpl_id = H5.H5Pcreate(HDF5Constants.H5P_DATASET_CREATE);

        if (dataset.dcpl_id >= 0) {
            if (shuffle) {
                H5.H5Pset_shuffle(dataset.dcpl_id);
            }

            H5.H5Pset_deflate(dataset.dcpl_id, gzipCompression);

            // Set the chunk size.
            H5.H5Pset_chunk(dataset.dcpl_id, dimensions.length, chunkSize);
        }

        // Create dataset
        dataset.dataset_id = H5.H5Dcreate(fileID, name, type, dataset.filespace_id, HDF5Constants.H5P_DEFAULT, dataset.dcpl_id, HDF5Constants.H5P_DEFAULT);

        return dataset;
    }

    private void closeDataset(HDF5DataIDs dataset) throws HDF5Exception {
        H5.H5Pclose(dataset.dcpl_id);
        H5.H5Dclose(dataset.dataset_id);
        H5.H5Sclose(dataset.filespace_id);
    }

    private void addDataset(int fileID, String name, double[] data) throws HDF5Exception {
        long[] datasetDimensions = {data.length};

        HDF5DataIDs dataset = createDataset(fileID, name, datasetDimensions, HDF5Constants.H5T_NATIVE_DOUBLE);

        H5.H5Dwrite(dataset.dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE,
                HDF5Constants.H5S_ALL, dataset.filespace_id, HDF5Constants.H5P_DEFAULT,
                data);

        closeDataset(dataset);
    }

    private void addDataset(int fileID, String name, int[][] data) throws HDF5Exception {
        long[] datasetDimensions = {data.length, data[0].length};

        HDF5DataIDs dataset = createDataset(fileID, name, datasetDimensions, HDF5Constants.H5T_NATIVE_INT);

        H5.H5Dwrite(dataset.dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE,
                HDF5Constants.H5S_ALL, dataset.filespace_id, HDF5Constants.H5P_DEFAULT,
                data);

        closeDataset(dataset);
    }

    private void addmzList(int fileID, double[] mzList) throws HDF5Exception {
        addDataset(fileID, "spectralChannels", mzList);
    }

    private void addOverviewImage(int fileID, double[] overviewImage) throws HDF5Exception {
        addDataset(fileID, "overviewImage", overviewImage);
    }

    private void addPixelList(int fileID, int[][] pixelList) throws NullPointerException, HDF5Exception {
        addDataset(fileID, "pixelList", pixelList);
    }
}
