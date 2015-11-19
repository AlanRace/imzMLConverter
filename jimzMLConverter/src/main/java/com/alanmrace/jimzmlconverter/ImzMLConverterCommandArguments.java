/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter;

import com.beust.jcommander.Parameter;
import java.util.List;


/**
 *
 * @author Alan
 */
public class ImzMLConverterCommandArguments {
    @Parameter(description = "The list of files to convert", required = true)
    protected List<String> files;

    
    @Parameter(names = "--debug", description = "Debug mode")
    protected boolean debug = false;
    
    @Parameter(names = {"--help", "-h"}, help = true)
    protected boolean help;

    @Parameter(names = {"--combine", "-c"}, description = "Combine all files into a single imzML file")
    protected boolean combine;
    
    @Parameter(names = {"--output", "-o"}, description = "Output filepath")
    protected String output;
    
    @Parameter(names = {"--pixel-location-file", "-p"}, description = "Pixel location file. (*.pat) for Waters data. (*.properties.txt) for ION-TOF data.")
    protected List<String> pixelLocationFile;
}
