/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlconverter.exceptions.ConversionException;
import com.alanmrace.jimzmlparser.exceptions.FatalParseException;
import com.alanmrace.jimzmlparser.imzml.ImzML;
import com.alanmrace.jimzmlparser.imzml.PixelLocation;
import com.alanmrace.jimzmlparser.parser.ImzMLHandler;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.io.File;
import java.io.IOException;
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

    public static final Logger logger = Logger.getLogger(ImzMLConverter.class.getName());

    private static String getExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }

        return extension;
    }

    ImzMLConverterCommandArguments.CommonCommands commonCommands;
    ImzMLConverterCommandArguments.CommandimzML commandimzML;
    ImzMLConverterCommandArguments.CommandHDF5 commandHDF5;
    String outputPath;
    
    PixelLocation[] pixelLocations;

    public MainCommand(ImzMLConverterCommandArguments.CommonCommands commonCommands,
            ImzMLConverterCommandArguments.CommandimzML commandimzML,
            ImzMLConverterCommandArguments.CommandHDF5 commandHDF5) {
        this.commonCommands = commonCommands;
        this.commandimzML = commandimzML;
        this.commandHDF5 = commandHDF5;
    }

    private String[] generatemzMLFiles(String fileName, ImzMLConverterCommandArguments.CommandimzML commandimzML) throws ConversionException {
        File[] mzMLFiles = null;
        String[] inputFilenames = null;
        File currentFile = new File(fileName);
        String extension = getExtension(fileName);

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
        } else if (extension.equalsIgnoreCase("raw") && currentFile.isDirectory()) {
            logger.log(Level.INFO, "Detected Waters RAW file");

            try {
                if (outputPath == null) {
                    mzMLFiles = WatersRAWTomzMLConverter.convert(fileName, commonCommands.centroid);
                } else {
                    mzMLFiles = WatersRAWTomzMLConverter.convert(fileName, outputPath, commonCommands.centroid);
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }

            if (outputPath == null || outputPath.isEmpty()) {
                outputPath = fileName.replace(".raw", "");
            }

        } else if (extension.equalsIgnoreCase("raw")) {
            logger.log(Level.INFO, "Detected Thermo RAW file");

            try {
                if (outputPath == null) {
                    mzMLFiles = ThermoRAWTomzMLConverter.convert(fileName, commonCommands.centroid);
                } else {
                    mzMLFiles = ThermoRAWTomzMLConverter.convert(fileName, outputPath, commonCommands.centroid);
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }

            if (outputPath == null || outputPath.isEmpty()) {
                outputPath = fileName.replace(".raw", "");
            }
        }
        
        // TODO: Remove duplicate code (appears again above)
        if (mzMLFiles != null) {
            inputFilenames = new String[mzMLFiles.length];

            for (int i = 0; i < mzMLFiles.length; i++) {
                inputFilenames[i] = mzMLFiles[i].getAbsolutePath();
                mzMLFiles[i].deleteOnExit();
            }
        }

        if (inputFilenames.length < 1) {
            throw new ConversionException("No mzML files found to continue conversion, do they exist in the raw data directory?");
        }

        return inputFilenames;
    }

    public static void main(String[] args) {
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
            
            // Check that exclusive arguments aren't both supplied
            if(commandimzML.imageDimensions != null && commandimzML.pixelLocationFile != null) {
                System.out.println("imzMLConverter version " + ImzMLConverter.version);
                System.out.println("Cannot supply both arguments --pixel-location-file and --image-dimensions");
                System.exit(0);
            }
        } else {
            commonCommands = commandHDF5;
        }

        MainCommand command = new MainCommand(commonCommands, commandimzML, commandHDF5);
        command.convert(jc.getParsedCommand());
    }

    public void convert(String parsedCommand) {
        outputPath = commonCommands.output;

        try {
            if (commonCommands.combine == null) {
                for (int fileIndex = 0; fileIndex < commonCommands.files.size(); fileIndex++) {
                    String fileName = commonCommands.files.get(fileIndex);

                    logger.log(Level.INFO, MessageFormat.format("Converting file {0}", fileName));

                    File currentFile = new File(fileName);

                    String extension = getExtension(fileName);

                    Converter converter = null;

                    String[] inputFilenames;
                    
                    if (extension.equals("grd"))
                        inputFilenames = new String[] {fileName};
                    else
                        inputFilenames = generatemzMLFiles(fileName, commandimzML);

                    // TODO: CHECK MzML FILES EXIST
                    // UPDATE OUTPUT PATH
                    
                    for(int splitIndex = 0; splitIndex < commonCommands.split; splitIndex++) {
                        if (extension.equals("wiff")) {
                            converter = new MzMLToImzMLConverter(outputPath, inputFilenames, MzMLToImzMLConverter.FileStorage.rowPerFile);
                        } else if (extension.equalsIgnoreCase("raw") && currentFile.isDirectory()) {
                            if (commandimzML.pixelLocationFile == null || commandimzML.pixelLocationFile.size() <= fileIndex
                                    || commandimzML.pixelLocationFile.get(fileIndex) == null || !(commandimzML.pixelLocationFile.get(fileIndex).contains(".pat") || commandimzML.pixelLocationFile.get(fileIndex).contains(".txt"))) {
                                logger.log(Level.SEVERE, "No .pat or .txt file supplied for the {0}th file {1}", new Object[]{fileIndex, fileName});
                            } else {
                                converter = new WatersMzMLToImzMLConverter(outputPath, inputFilenames, MzMLToImzMLConverter.FileStorage.oneFile);

                                if (commandimzML.pixelLocationFile.get(fileIndex).contains(".pat")) {
                                    ((WatersMzMLToImzMLConverter) converter).setPatternFile(commandimzML.pixelLocationFile.get(fileIndex));
                                } else {
                                    ((MzMLToImzMLConverter) converter).setCoordsFile(commandimzML.pixelLocationFile.get(fileIndex));
                                }
                            }
                        } else if (extension.equalsIgnoreCase("raw")) {
                            if (commandimzML.pixelLocationFile == null || commandimzML.pixelLocationFile.size() <= fileIndex
                                    || commandimzML.pixelLocationFile.get(fileIndex) == null || !(commandimzML.pixelLocationFile.get(fileIndex).toLowerCase().contains(".udp") || commandimzML.pixelLocationFile.get(fileIndex).contains(".txt"))) {
                                logger.log(Level.SEVERE, "No .udp or .txt file supplied for the {0}th file {1} so assuming non-imaging data", new Object[]{fileIndex, fileName});
                            }

                            converter = new ThermoMzMLToImzMLConverter(outputPath, inputFilenames, MzMLToImzMLConverter.FileStorage.oneFile);

                            if (commandimzML.pixelLocationFile != null) {
                                if (commandimzML.pixelLocationFile.get(fileIndex).contains(".UDP")) {
                                    ((ThermoMzMLToImzMLConverter) converter).setUDPFile(commandimzML.pixelLocationFile.get(fileIndex));
                                } else {
                                    ((MzMLToImzMLConverter) converter).setCoordsFile(commandimzML.pixelLocationFile.get(fileIndex));
                                }
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
                                        outputPath = fileName.replace(".grd", ".imzML");
                                    }

                                    converter = new GRDToImzMLConverter(outputPath, inputFilenames);
                                    ((GRDToImzMLConverter) converter).setPropertiesFile(commandimzML.pixelLocationFile.get(fileIndex));
                                } catch (IOException ex) {
                                    Logger.getLogger(MainCommand.class.getName()).log(Level.SEVERE, null, ex);

                                    // Failed so just exit
                                    converter = null;
                                }
                            }
                        } else if (extension.equals("mzML")) {
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

                            if (parsedCommand.equals("hdf5")) {
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
                                } catch (FatalParseException ex) {
                                    Logger.getLogger(MainCommand.class.getName()).log(Level.SEVERE, null, ex);

                                    converter = null;
                                }
                            }
                        }

                        if (parsedCommand.equals("hdf5")) {
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

                        if (parsedCommand.equals("imzML")) {
                            if (converter instanceof ImzMLConverter) {
                                ((ImzMLConverter) converter).setIncludeGlobalmzList(commonCommands.includeGlobalmzList);

                                if (commandimzML.mzArrayType != null) {
                                    ((ImzMLConverter) converter).setmzArrayDataType(commandimzML.mzArrayType);
                                }

                                if (commandimzML.intensityArrayType != null) {
                                    ((ImzMLConverter) converter).setIntensityArrayDataType(commandimzML.intensityArrayType);
                                }

                                if (commandimzML.compression != null) {
                                    ((ImzMLConverter) converter).setCompressionType(commandimzML.compression);
                                }
                                
                                if(commandimzML.imageDimensions != null) {                                    
                                    int numPixels = (commandimzML.imageDimensions.get(0) * commandimzML.imageDimensions.get(1)) + commandimzML.ignoreScans;
                                    PixelLocation[] locations = new PixelLocation[numPixels];
                                    
                                    int pixelIndex = 0;
                                    
                                    for(pixelIndex = 0; pixelIndex < commandimzML.ignoreScans; pixelIndex++)
                                        locations[pixelIndex] = new PixelLocation(-1, -1, -1);
                                    
                                    for(int y = 0; y < commandimzML.imageDimensions.get(1); y++) {
                                        for(int x = 0; x < commandimzML.imageDimensions.get(0); x++) {
                                            locations[pixelIndex++] = new PixelLocation(x + 1, y + 1, 1);
                                        }
                                    }
                                        
                                    pixelLocations = locations;
                                    ((ImzMLConverter) converter).setPixelLocations(locations);    
                                }
                                
                                // If splitting up the data then need to redefine the pixel locations and output path
                                if(commonCommands.split > 1) {
                                    if(splitIndex == 0 && commandimzML.imageDimensions == null)
                                        pixelLocations = ((ImzMLConverter) converter).getPixelLocations();
                                    
                                    PixelLocation[] locations = new PixelLocation[pixelLocations.length];
                                    
                                    for(int pixelIndex = 0; pixelIndex < pixelLocations.length; pixelIndex++) {
                                        if(pixelIndex % commonCommands.split == splitIndex) {
                                            locations[pixelIndex] = new PixelLocation((pixelLocations[pixelIndex].getX()-1) / commonCommands.split + 1, 
                                                pixelLocations[pixelIndex].getY(), pixelLocations[pixelIndex].getZ());
                                        } else {
                                            locations[pixelIndex] = new PixelLocation(-1, -1, -1);
                                        }
                                    }
                                    
                                    ((ImzMLConverter) converter).setPixelLocations(locations);
                                    ((ImzMLConverter) converter).setOutputFilename(outputPath + "_" + splitIndex);
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
                    }
                }
            } else {
                if (parsedCommand.equals("imzML")) {
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
            Logger.getLogger(MainCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
