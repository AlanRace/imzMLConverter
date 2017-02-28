/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

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
  
    public class CommonCommands {
        @Parameter(description = "The list of files to convert", required = true)
        protected List<String> files;
        
        @Parameter(names = {"--output", "-o"}, description = "Output filepath")
        protected String output;
        
        @Parameter(names = {"--combine", "-c"}, arity = 2, description = "Combine all files into a single output file with dimensions x, y")
        protected List<Integer> combine;   
        
        @Parameter(names = {"--split", "-s"}, description = "Split different scans out into separate output files")
        protected Boolean split = false;   
        
        @Parameter(names = {"--include-global-mz-list"}, arity = 1, description = "Calculate and include a global m/z list within the output format")
        protected Boolean includeGlobalmzList = true;
    }
    
    @Parameters(commandDescription = "Convert to imzML")
    public class CommandimzML extends CommonCommands {  
        @Parameter(names = {"--pixel-location-file", "-p"}, description = "Pixel location file. (*.pat) for Waters data. (*.properties.txt) for ION-TOF data.")
        protected List<String> pixelLocationFile;
        
        @Parameter(names = {"--compression"}, description = "Compression type")
        protected String compression;
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
