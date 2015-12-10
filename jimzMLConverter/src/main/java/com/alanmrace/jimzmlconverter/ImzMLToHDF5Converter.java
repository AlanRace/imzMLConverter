/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlconverter.exceptions.ConversionException;
import com.alanmrace.jimzmlparser.imzML.ImzML;
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

    private ImzML imzML;
    private String outputFilename;

    private int gzipCompression = 9;
    
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
        try {
            double[] fullmzList = imzML.getFullmzList();
            
            int file_id = H5.H5Fcreate(outputFilename, HDF5Constants.H5F_ACC_TRUNC,
                    HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
            
            addmzList(file_id, fullmzList);
            
            H5.H5Fclose(file_id);
        } catch (HDF5Exception ex) {
            Logger.getLogger(ImzMLToHDF5Converter.class.getName()).log(Level.SEVERE, null, ex);
            
            ex.printStackTrace();
            
            throw new ConversionException(ex.getLocalizedMessage());
        }
    }

    private void addmzList(int fileID, double[] mzList) throws NullPointerException, HDF5Exception {
        int numberOfDimensions = 1;
        long[] datasetDimensions = {mzList.length};

        int filespace_id = H5.H5Screate_simple(numberOfDimensions, datasetDimensions, null);

        int dcpl_id = H5.H5Pcreate(HDF5Constants.H5P_DATASET_CREATE);
        if (dcpl_id >= 0) {
            H5.H5Pset_deflate(dcpl_id, gzipCompression);

            // Set the chunk size.
            H5.H5Pset_chunk(dcpl_id, numberOfDimensions, datasetDimensions);
        }

        // Create dataset
        int dataset_id = H5.H5Dcreate(fileID, "spectralChannels", HDF5Constants.H5T_NATIVE_DOUBLE, filespace_id, HDF5Constants.H5P_DEFAULT, dcpl_id, HDF5Constants.H5P_DEFAULT);

        H5.H5Dwrite(dataset_id, HDF5Constants.H5T_NATIVE_DOUBLE,
                HDF5Constants.H5S_ALL, filespace_id, HDF5Constants.H5P_DEFAULT,
                mzList);

        H5.H5Pclose(dcpl_id);
        H5.H5Dclose(dataset_id);
        H5.H5Sclose(filespace_id);
    }
}
