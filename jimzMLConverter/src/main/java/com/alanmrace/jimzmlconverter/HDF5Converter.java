/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import java.util.logging.Logger;
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;

/**
 *
 * @author amr1
 */
public abstract class HDF5Converter implements Converter {
    private static final Logger logger = Logger.getLogger(HDF5Converter.class.getName());
    
    protected int gzipCompression = 3;
    protected boolean shuffle = false;

    protected long[] chunkSizes;
    
    protected long[] chunkCacheSize;
    protected double w0 = 0.75;
    
    protected int numberOfSpectraPerBlock = 10;
    
    protected String outputFilename;
    
    protected double[] putOnFullmzAxis(double[] fullmzAxis, double[] mzs, double[] intensities) {
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

    public void setChunkCacheSize(long[] chunkCacheSize) {
        this.chunkCacheSize = chunkCacheSize;
    }
    
    public void setw0(double w0) {
        this.w0 = w0;
    }
    
    /**
     * Helper methods for HDF5 dataset creation
     */
    protected class HDF5DataIDs {

        int dacl_id = -1;
        int dcpl_id = -1;
        int dataset_id = -1;
        int filespace_id = -1;
        int file_id = -1;
    }

    protected HDF5DataIDs createDataset(int fileID, String name, long[] dimensions, int type) throws HDF5Exception {
        return createDataset(fileID, name, dimensions, type, dimensions);
    }

    protected HDF5DataIDs createDataset(int fileID, String name, long[] dimensions, int type, long[] chunkSize) throws HDF5Exception {
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

        dataset.dacl_id = H5.H5Pcreate(HDF5Constants.H5P_DATASET_ACCESS);
        
        if(chunkCacheSize != null) {
            H5.H5Pset_chunk_cache(dataset.dacl_id, chunkCacheSize[0], chunkCacheSize[1], w0);
        }
        
        // Create dataset
        dataset.dataset_id = H5.H5Dcreate(fileID, name, type, dataset.filespace_id, HDF5Constants.H5P_DEFAULT, dataset.dcpl_id, dataset.dacl_id);

        return dataset;
    }

    protected void closeDataset(HDF5DataIDs dataset) throws HDF5Exception {
        H5.H5Pclose(dataset.dcpl_id);
        H5.H5Dclose(dataset.dataset_id);
        H5.H5Sclose(dataset.filespace_id);
    }

    protected void addDataset(int fileID, String name, double[] data) throws HDF5Exception {
        long[] datasetDimensions = {data.length};

        HDF5DataIDs dataset = createDataset(fileID, name, datasetDimensions, HDF5Constants.H5T_NATIVE_DOUBLE);

        H5.H5Dwrite(dataset.dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE,
                HDF5Constants.H5S_ALL, dataset.filespace_id, HDF5Constants.H5P_DEFAULT,
                data);

        closeDataset(dataset);
    }

    protected void addDataset(int fileID, String name, int[][] data) throws HDF5Exception {
        long[] datasetDimensions = {data.length, data[0].length};

        HDF5DataIDs dataset = createDataset(fileID, name, datasetDimensions, HDF5Constants.H5T_NATIVE_INT);

        H5.H5Dwrite(dataset.dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE,
                HDF5Constants.H5S_ALL, dataset.filespace_id, HDF5Constants.H5P_DEFAULT,
                data);

        closeDataset(dataset);
    }

    protected void addmzList(int fileID, double[] mzList) throws HDF5Exception {
        addDataset(fileID, "spectralChannels", mzList);
    }

    protected void addOverviewImage(int fileID, double[] overviewImage) throws HDF5Exception {
        addDataset(fileID, "overviewImage", overviewImage);
    }

    protected void addPixelList(int fileID, int[][] pixelList) throws NullPointerException, HDF5Exception {
        addDataset(fileID, "pixelList", pixelList);
    }
}
