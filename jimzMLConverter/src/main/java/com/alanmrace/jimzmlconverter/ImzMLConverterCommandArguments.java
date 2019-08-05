/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.alanmrace.jimzmlparser.data.DataTypeTransform.DataType;
import com.alanmrace.jimzmlparser.mzml.BinaryDataArray.CompressionType;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.util.List;


/**
 *
 * @author Alan
 */
public class ImzMLConverterCommandArguments {
    
    @Parameter(names = "--debug", description = "Debug mode")
    protected boolean debug = false;
    
    @Parameter(names = {"--help", "-h"}, help = true)
    protected boolean help;

    public enum SourceType {
        LESA,
        DESI
    }

    public class CommonCommands {
        @Parameter(description = "The list of files to convert", required = true)
        protected List<String> files;
        
        @Parameter(names = {"--output", "-o"}, description = "Output filepath")
        protected String output;
        
        @Parameter(names = {"--combine", "-c"}, arity = 2, description = "Combine all files into a single output file with dimensions x, y")
        protected List<Integer> combine;   
        
        @Parameter(names = {"--split", "-s"}, arity = 1, description = "Split x different scans out into separate output files")
        protected Integer split = 1;   
                
        @Parameter(names = {"--include-global-mz-list"}, arity = 1, description = "Calculate and include a global m/z list within the output format")
        protected Boolean includeGlobalmzList = false;
        
        @Parameter(names = {"--centroid"}, description = "Perform peak picking (centroid the data)")
        protected Boolean centroid = false;
    }
    
    @Parameters(commandDescription = "Convert to imzML")
    public class CommandimzML extends CommonCommands {  
        @Parameter(names = {"--pixel-location-file", "-p"}, description = "Pixel location file. (*.pat) for Waters data. (*.properties.txt) for ION-TOF data. Not compatible with --image-dimensions option.")
        protected List<String> pixelLocationFile;
        
        @Parameter(names = {"--image-dimensions"}, arity = 2, description = "Create rectangular image with dimensions x, y in number of pixels. Not compatible with --pixel-location-file.")
        protected List<Integer> imageDimensions;
        
        @Parameter(names = {"--ignore-scans"}, arity = 1, description = "Skip the first x scans when converting. Only compatible with --image-dimensions option.")
        protected Integer ignoreScans = 0;
        
        @Parameter(names = {"--sum-scans-with-ppm"}, description = "Sum all scans within a single file with the specified ppm tolerance")
        protected Double sumScansWithPPM = -Double.MAX_VALUE;

        @Parameter(names ={"--source-type"}, description = "Source type, which dictates how a folder full of files is handled")
        protected SourceType sourceType = null;

        @Parameter(names = {"--lesa-step-size"}, description = "LESA step size (um)")
        protected Integer lesaStepSize = 200;
        
        @Parameter(names = {"--ms-level-filter"}, arity = 2, description = "Apply a filter to the MS level when converting to mzML files. Requires min and max level")
        protected List<Integer> msLevelFilter;
        
        @Parameter(names = {"--compression"}, description = "Compression type", converter = CompressionConverter.class)
        protected CompressionType compression = CompressionType.NONE;
        
        @Parameter(names = {"--compression-mz-array"}, description = "Compression type for m/z array", converter = CompressionConverter.class)
        protected CompressionType mzArrayCompression = null;
        
        @Parameter(names = {"--compression-intensity-array"}, description = "Compression type for intensity array", converter = CompressionConverter.class)
        protected CompressionType intensityArrayCompression = null;
        
        @Parameter(names = {"--mz-array-type"}, description = "Data type used to store m/z array", converter = DataTypeConverter.class)
        protected DataType mzArrayType = DataType.DOUBLE;
        
        @Parameter(names = {"--intensity-array-type"}, description = "Data type used to store intensity array", converter = DataTypeConverter.class)
        protected DataType intensityArrayType = DataType.DOUBLE;
    }
    
    @Parameters(separators = "=", commandDescription = "Convert to HDF5")
    public class CommandHDF5 extends CommonCommands {         
        @Parameter(names = {"--chunk-size"}, arity = 3, description = "Chunk size. Requires 3 values for spatial, m/z and mobility (-1 = ALL, 0 = ignore dimension)")
        protected List<Long> hdf5Chunk;
        
        @Parameter(names = {"--chunk-cache-size"}, arity = 2, description = "Chunk cache size. Requires 2 values")
        protected List<Long> hdf5ChunkCache;
        
        @Parameter(names = "--w0", description = "Chunk cache w0.")
        protected Double w0;

        @Parameter(names = {"--compression-level"}, description = "gzip compression level")
        protected Integer compressionLevel;
    }
}
