package com.alanmrace.jimzmlconverter.exceptions;

/**
 *
 * @author Alan Race
 */
public class ProteoWizardNotInstalledException extends Exception {
    
    public ProteoWizardNotInstalledException() {
        super("ProteoWizard is not installed and so mzML files cannot be generated. http://proteowizard.sourceforge.net/");
    }
}
