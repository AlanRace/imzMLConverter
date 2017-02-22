/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlconverter.exceptions.ConversionException;
import com.alanmrace.jimzmlparser.exceptions.ImzMLParseException;
import com.alanmrace.jimzmlparser.imzML.ImzML;
import com.alanmrace.jimzmlparser.parser.ImzMLHandler;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author amr1
 */
public class MainCommand {

    private static String getExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
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
        jc.setCaseSensitiveOptions(false);

        ImzMLConverterCommandArguments.CommandimzML commandimzML = arguments.new CommandimzML();
        jc.addCommand("imzML", commandimzML);

        ImzMLConverterCommandArguments.CommandHDF5 commandHDF5 = arguments.new CommandHDF5();
        jc.addCommand("hdf5", commandHDF5);

        try {
            jc.parse(args);
        } catch (ParameterException pex) {
            arguments.help = true;
            System.out.println(pex.getMessage());
        }

        if (arguments.help || jc.getParsedCommand() == null) {
            System.out.println("imzMLConverter version " + ImzMLConverter.version);
            jc.usage();
            System.exit(0);
        }

        ImzMLConverterCommandArguments.CommonCommands commonCommands;

        if (jc.getParsedCommand().equals("imzML")) {
            commonCommands = commandimzML;
        } else {
            commonCommands = commandHDF5;
        }

        String outputPath = commonCommands.output;

        try {
            if (commonCommands.combine == null) {
                for (int fileIndex = 0; fileIndex < commonCommands.files.size(); fileIndex++) {
                    String fileName = commonCommands.files.get(fileIndex);

                    logger.log(Level.INFO, MessageFormat.format("Converting file {0}", fileName));

                    File currentFile = new File(fileName);
                    File[] mzMLFiles = null;
                    String[] inputFilenames = null;

                    String extension = getExtension(fileName);

                    Converter converter = null;

                    if (extension.equals("wiff")) {
                        logger.log(Level.INFO, "Detected WIFF file");

                        try {
                            mzMLFiles = WiffTomzMLConverter.convert(fileName);
                        } catch (IOException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }

                        if (outputPath == null || outputPath.isEmpty()) {
                            outputPath = fileName.replace(".wiff", "");
                        }

                        // TODO: Remove duplicate code (appears again below)
                        if (mzMLFiles != null) {
                            inputFilenames = new String[mzMLFiles.length];

                            for (int i = 0; i < mzMLFiles.length; i++) {
                                inputFilenames[i] = mzMLFiles[i].getAbsolutePath();
                            }
                        }

                        converter = new MzMLToImzMLConverter(outputPath, inputFilenames, MzMLToImzMLConverter.FileStorage.rowPerFile);
                    } else if (extension.equalsIgnoreCase("raw") && currentFile.isDirectory()) {
                        logger.log(Level.INFO, "Detected Waters RAW file");

                        if (commandimzML.pixelLocationFile == null || commandimzML.pixelLocationFile.size() <= fileIndex
                                || commandimzML.pixelLocationFile.get(fileIndex) == null || !(commandimzML.pixelLocationFile.get(fileIndex).contains(".pat") || commandimzML.pixelLocationFile.get(fileIndex).contains(".txt"))) {
                            logger.log(Level.SEVERE, "No .pat or .txt file supplied for the {0}th file {1}", new Object[]{fileIndex, fileName});
                        } else {
                            try {
                                if(outputPath == null)
                                    mzMLFiles = WatersRAWTomzMLConverter.convert(fileName);
                                else
                                    mzMLFiles = WatersRAWTomzMLConverter.convert(fileName, outputPath);
                            } catch (IOException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            }

                            if (outputPath == null || outputPath.isEmpty()) {
                                outputPath = fileName.replace(".raw", "");
                            }

                            // TODO: Remove duplicate code (appears again above)
                            if (mzMLFiles != null) {
                                inputFilenames = new String[mzMLFiles.length];

                                for (int i = 0; i < mzMLFiles.length; i++) {
                                    inputFilenames[i] = mzMLFiles[i].getAbsolutePath();
                                }
                            }

                            if (inputFilenames.length < 1) {
                                throw new ConversionException("No mzML files found to continue conversion, do they exist in the raw data directory?");
                            }

                            converter = new WatersMzMLToImzMLConverter(outputPath, inputFilenames, MzMLToImzMLConverter.FileStorage.oneFile);
                            
                            if(commandimzML.pixelLocationFile.get(fileIndex).contains(".pat"))
                                ((WatersMzMLToImzMLConverter) converter).setPatternFile(commandimzML.pixelLocationFile.get(fileIndex));
                            else
                                ((MzMLToImzMLConverter) converter).setCoordsFile(commandimzML.pixelLocationFile.get(fileIndex));
                        }
                    } else if(extension.equalsIgnoreCase("raw")) {
                        logger.log(Level.INFO, "Detected Thermo RAW file");
                        
                        if (commandimzML.pixelLocationFile == null || commandimzML.pixelLocationFile.size() <= fileIndex
                                || commandimzML.pixelLocationFile.get(fileIndex) == null || !(commandimzML.pixelLocationFile.get(fileIndex).contains(".udp") || commandimzML.pixelLocationFile.get(fileIndex).contains(".txt"))) {
                            logger.log(Level.SEVERE, "No .udp or .txt file supplied for the {0}th file {1}", new Object[]{fileIndex, fileName});
                        } else {
                            try {
                                if(outputPath == null)
                                    mzMLFiles = ThermoRAWTomzMLConverter.convert(fileName);
                                else
                                    mzMLFiles = ThermoRAWTomzMLConverter.convert(fileName, outputPath);
                            } catch (IOException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            }

                            if (outputPath == null || outputPath.isEmpty()) {
                                outputPath = fileName.replace(".raw", "");
                            }

                            // TODO: Remove duplicate code (appears again above)
                            if (mzMLFiles != null) {
                                inputFilenames = new String[mzMLFiles.length];

                                for (int i = 0; i < mzMLFiles.length; i++) {
                                    inputFilenames[i] = mzMLFiles[i].getAbsolutePath();
                                }
                            }

                            if (inputFilenames.length < 1) {
                                throw new ConversionException("No mzML files found to continue conversion, do they exist in the raw data directory?");
                            }

                            converter = new ThermoMzMLToImzMLConverter(outputPath, inputFilenames, MzMLToImzMLConverter.FileStorage.oneFile);
                            
                            if(commandimzML.pixelLocationFile.get(fileIndex).contains(".pat"))
                                ((ThermoMzMLToImzMLConverter) converter).setUDPFile(commandimzML.pixelLocationFile.get(fileIndex));
                            else
                                ((MzMLToImzMLConverter) converter).setCoordsFile(commandimzML.pixelLocationFile.get(fileIndex));
                        }
                    } else if (extension.equals("grd")) {
                        logger.log(Level.INFO, "Detected ION-TOF GRD file");

                        if (commandimzML.pixelLocationFile == null || commandimzML.pixelLocationFile.size() <= fileIndex
                                || commandimzML.pixelLocationFile.get(fileIndex) == null || !commandimzML.pixelLocationFile.get(fileIndex).contains(".properties.txt")) {
                            logger.log(Level.SEVERE, "No .properties.txt file supplied for the {0}th file {1}", new Object[]{fileIndex, fileName});
                        } else {
                            try {
                                inputFilenames = new String[]{fileName};

                                if (outputPath == null || outputPath.isEmpty()) {
                                    outputPath = fileName.replace(".grd", "");
                                }

                                converter = new GRDToImzMLConverter(outputPath, inputFilenames);
                                ((GRDToImzMLConverter) converter).setPropertiesFile(commandimzML.pixelLocationFile.get(fileIndex));
                            } catch (IOException ex) {
                                Logger.getLogger(MainCommand.class.getName()).log(Level.SEVERE, null, ex);

                                // Failed so just exit
                                converter = null;
                            }
                        }
                    } else if(extension.equals("mzML")) {
                        logger.log(Level.INFO, "Detected mzML file");
                        
                        if (commandimzML.pixelLocationFile == null || commandimzML.pixelLocationFile.size() <= fileIndex
                                || commandimzML.pixelLocationFile.get(fileIndex) == null || !commandimzML.pixelLocationFile.get(fileIndex).contains(".txt")) {
                            logger.log(Level.SEVERE, "No coordinates .txt file supplied for the {0}th file {1}", new Object[]{fileIndex, fileName});
                        } else {
                            inputFilenames = new String[]{fileName};

                            if (outputPath == null || outputPath.isEmpty()) {
                                outputPath = fileName.replace(".mzML", "");
                            }

                            converter = new MzMLToImzMLConverter(outputPath, inputFilenames, MzMLToImzMLConverter.FileStorage.oneFile);
                            ((MzMLToImzMLConverter) converter).setCoordsFile(commandimzML.pixelLocationFile.get(fileIndex));
                        }
                    } else if (extension.equals("imzML")) {

                        if (jc.getParsedCommand().equals("hdf5")) {
                            try {
                                inputFilenames = new String[]{fileName};

                                if (outputPath == null || outputPath.isEmpty()) {
                                    outputPath = fileName.replace(".imzML", ".h5");
                                }

                                logger.log(Level.INFO, "Parsing {0}", fileName);
                                ImzML imzML = ImzMLHandler.parseimzML(fileName);
                                logger.log(Level.INFO, "Parsed {0}", fileName);
                                converter = new ImzMLToHDF5Converter(imzML, outputPath);
                                logger.log(Level.INFO, "Set up converter");
                            } catch (ImzMLParseException ex) {
                                Logger.getLogger(MainCommand.class.getName()).log(Level.SEVERE, null, ex);

                                converter = null;
                            }
                        }
                    }

                    if (jc.getParsedCommand().equals("hdf5")) {
                        HDF5Converter hdf5Converter = (HDF5Converter) converter;

                        if (hdf5Converter != null) {
                            if (commandHDF5.compressionLevel != null) {
                                hdf5Converter.setCompressionLevel(commandHDF5.compressionLevel);
                            }

                            if (commandHDF5.hdf5Chunk != null) {
                                long[] chunk = new long[commandHDF5.hdf5Chunk.size()];
                                for (int i = 0; i < chunk.length; i++) {
                                    chunk[i] = commandHDF5.hdf5Chunk.get(i);
                                }

                                hdf5Converter.setChunkSizes(chunk);
                            }

                            if (commandHDF5.hdf5ChunkCache != null) {
                                long[] chunkCache = new long[commandHDF5.hdf5ChunkCache.size()];
                                for (int i = 0; i < chunkCache.length; i++) {
                                    chunkCache[i] = commandHDF5.hdf5ChunkCache.get(i);
                                }

                                hdf5Converter.setChunkCacheSize(chunkCache);
                            }
                        }
                    }

                    // Perform the conversion
                    if (inputFilenames != null && converter != null) {
                        try {
                            converter.convert();

                            logger.log(Level.INFO, MessageFormat.format("Converted {0} to {1}{2}", fileName, outputPath, ".imzML"));
                        } catch (ConversionException ex) {
                            logger.log(Level.SEVERE, "Failed to convert " + fileName, ex);
                        }
                    }

                    // Cleanup
                    if (mzMLFiles != null) {
                        for (File mzMLFile : mzMLFiles) {
                            try {
                                Files.delete(mzMLFile.toPath());
                                logger.log(Level.FINER, MessageFormat.format("Cleaned up {0}", mzMLFile));
                            } catch (IOException ex) {
                                logger.log(Level.WARNING, MessageFormat.format("Failed to clean up {0}", mzMLFile), ex);
                            }
                        }
                    }
                }
            } else {
                if (jc.getParsedCommand().equals("imzML")) {
                    if (outputPath == null || outputPath.isEmpty()) {
                        outputPath = commonCommands.files.get(0).replace(".imzML", ".combined");
                    }

                    String[] files = new String[commonCommands.files.size()];
                    commonCommands.files.toArray(files);

                    ImzMLToImzMLConverter converter = new ImzMLToImzMLConverter(outputPath, files);

                    converter.setImageGrid(commonCommands.combine.get(0), commonCommands.combine.get(1));

                    try {
                        converter.convert();

                        logger.log(Level.INFO, MessageFormat.format("Converted {0} files to {1}", commonCommands.files.size(), outputPath));
                    } catch (ConversionException ex) {
                        Logger.getLogger(MainCommand.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (ConversionException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
}
