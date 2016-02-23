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
        
        @Parameter(names = {"--combine", "-c"}, description = "Combine all files into a single output file")
        protected boolean combine;   
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
        @Parameter(names = {"--chunk"}, arity = 3, description = "Chunk size. Requires 3 values for spatial, m/z and mobility (-1 = ALL, 0 = ignore dimension)")
        protected List<Long> hdf5Chunk;

        @Parameter(names = {"--compression-level"}, description = "gzip compression level")
        protected Integer compressionLevel;
    }
}
