/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

/**
 *
 * @author amr1
 */
public class WatersRAWToImzMLConverter implements Converter {

    @Override
    public void convert() {

//        try {
//            String[] mzMLFiles = WatersRAWTomzMLConverter.convert(fileName);
//        } catch (IOException ex) {
//            logger.log(Level.SEVERE, null, ex);
//        }
//
//        if (outputPath == null || outputPath.isEmpty()) {
//            outputPath = fileName.replace(".raw", "");
//        }
//
//        // TODO: Remove duplicate code (appears again above)
//        if (mzMLFiles != null) {
//            inputFilenames = new String[mzMLFiles.length];
//
//            for (int i = 0; i < mzMLFiles.length; i++) {
//                inputFilenames[i] = mzMLFiles[i].getAbsolutePath();
//            }
//        }
//
//        if (inputFilenames.length < 1) {
//            throw new ConversionException("No mzML files found to continue conversion, do they exist in the raw data directory?");
//        }
//
//        converter = new WatersMzMLToImzMLConverter(outputPath, inputFilenames, MzMLToImzMLConverter.FileStorage.oneFile);
//        ((WatersMzMLToImzMLConverter) converter).setPatternFile(commandimzML.pixelLocationFile.get(fileIndex));
    }

}
