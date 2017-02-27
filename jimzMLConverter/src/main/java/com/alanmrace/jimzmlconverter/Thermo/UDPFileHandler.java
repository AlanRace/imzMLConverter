/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter.Thermo;

import com.alanmrace.jimzmlconverter.Waters.PatternDefinition;
import com.alanmrace.jimzmlconverter.Waters.PatternDefinitionHandler;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Alan
 */
public class UDPFileHandler extends DefaultHandler {
    private static final Logger logger = Logger.getLogger(PatternDefinitionHandler.class.getName());

    UDPFile udpFile;

    boolean processingLine = false;
    boolean processingValidArea = false;
    boolean processingPlateSize = false;
    boolean processingLaserSize = false;

    boolean processingDate = false;
    boolean processingInt = false;
    protected StringBuffer stringBuffer;

    public UDPFileHandler() {
        udpFile = new UDPFile();

        stringBuffer = new StringBuffer(100);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        stringBuffer.setLength(0);
        
        switch(qName) {
            case "StartTime":
            case "EndTime":
            case "UserDate":
            case "SampleInLabDate":
            case "SampleMatrixAppDate":
                processingDate = true;
                break;
            case "SpecOrigin":
            case "ScanScenario":
            case "ScanDirection":
            case "ScanForm":
            case "MassMin":
            case "MassMax":
            case "OriginX":
            case "OriginY":
            case "OriginZ":
            case "OldOriginX":
            case "OldOriginY":
            case "OldOriginZ":
            case "ResolutionY":
            case "ResolutionZ":
            case "ResolutionX":
            case "VoxelSizeX":
            case "VoxelSizeY":
            case "VoxelSizeZ":
            case "ImageSizeX":
            case "ImageSizeY":
            case "ImageSizeZ":
            case "MaxX":
            case "MaxY":
            case "MaxZ":
            case "MaxN":
            case "ScanEventsPerSpot":
            case "ScanEventsPerPixel":
            case "Pattern":
            case "LASER_power":
            case "Variable_Attenuator":
            case "Attenuator_Calibration1":
            case "Attenuator_Calibration2":
            case "Attenuator_Calibration3":
            case "Target_voltage":
            case "Capillary_voltage":
            case "Aparture_Voltage_I":
            case "Aparture_Voltage_II":
            case "LowMass":
            case "HighMass":
            case "SampleNo":
                processingInt = true;
                break;
        }
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        stringBuffer.append(ch, start, length);
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (processingDate) {
            Date date = null;
            
            DateFormat format = new SimpleDateFormat("dd.MM.yy HH:mm:ss");

            try {
                date = format.parse(stringBuffer.toString());
            } catch (ParseException ex) {
                // Try the English date representation
                format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                
                try {
                    date = format.parse(stringBuffer.toString());
                } catch (ParseException ex1) {
                    // Try just the year date representation
                    format = new SimpleDateFormat("dd.MM.yy");
                    
                    try {
                        date = format.parse(stringBuffer.toString());
                    } catch (ParseException ex2) {
                        Logger.getLogger(UDPFileHandler.class.getName()).log(Level.SEVERE, null, ex2);
                    }
                }
            }
            
            switch (qName) {
                case "StartTime":
                    udpFile.startTime = date;
                    break;
                case "EndTime":
                    udpFile.endTime = date;
                    break;
                case "UserDate":
                    udpFile.userDate = date;
                    break;
                case "SampleInLabDate":
                    udpFile.sampleInLabDate = date;
                    break;
                case "SampleMatrixAppDate":
                    udpFile.sampleMatrixAppDate = date;
                    break;
                default:
                    break;
            }
            
            processingDate = false;
        } else if(processingInt) {
            int value = Integer.parseInt(stringBuffer.toString());
            
            switch (qName) {
                case "SpecOrigin":
                    udpFile.specOrigin = value;
                    break;
                case "ScanScenario":
                    udpFile.scanScenario = value;
                    break;
                case "ScanDirection":
                    udpFile.scanDirection = value;
                    break;
                case "ScanForm":
                    udpFile.scanForm = value;
                    break;
                case "MassMin":
                    udpFile.massMin = value;
                    break;
                case "MassMax":
                    udpFile.massMax = value;
                    break;
                case "OriginX":
                    udpFile.originX = value;
                    break;
                case "OriginY":
                    udpFile.originY = value;
                    break;
                case "OriginZ":
                    udpFile.oldOriginZ = value;
                    break;
                case "OldOriginX":
                    udpFile.oldOriginX = value;
                    break;
                case "OldOriginY":
                    udpFile.oldOriginY = value;
                    break;
                case "OldOriginZ":
                    udpFile.oldOriginZ = value;
                    break;
                case "ResolutionY":
                    udpFile.resolutionY = value;
                    break;
                case "ResolutionZ":
                    udpFile.resolutionZ = value;
                    break;
                case "ResolutionX":
                    udpFile.resolutionX = value;
                    break;
                case "VoxelSizeX":
                    udpFile.voxelSizeX = value;
                    break;
                case "VoxelSizeY":
                    udpFile.voxelSizeY = value;
                    break;
                case "VoxelSizeZ":
                    udpFile.voxelSizeZ = value;
                    break;
                case "ImageSizeX":
                    udpFile.imageSizeX = value;
                    break;
                case "ImageSizeY":
                    udpFile.imageSizeY = value;
                    break;
                case "ImageSizeZ":
                    udpFile.imageSizeZ = value;
                    break;
                case "MaxX":
                    udpFile.maxX = value;
                    break;
                case "MaxY":
                    udpFile.maxY = value;
                    break;
                case "MaxZ":
                    udpFile.maxZ = value;
                    break;
                case "MaxN":
                    udpFile.maxN = value;
                    break;
                case "ScanEventsPerSpot":
                    udpFile.scanEventsPerSpot = value;
                    break;
                case "ScanEventsPerPixel":
                    udpFile.scanEventsPerPixel = value;
                    break;
                case "Pattern":
                    udpFile.pattern = value;
                    break;
                case "LASER_power":
                    udpFile.laserPower = value;
                    break;
                case "Variable_Attenuator":
                    udpFile.variableAttenuator = value;
                    break;
                case "Attenuator_Calibration1":
                    udpFile.attenuatorCalibration1 = value;
                    break;
                case "Attenuator_Calibration2":
                    udpFile.attenuatorCalibration2 = value;
                    break;
                case "Attenuator_Calibration3":
                    udpFile.attenuatorCalibration3 = value;
                    break;
                case "Target_voltage":
                    udpFile.targetVoltage = value;
                    break;
                case "Capillary_voltage":
                    udpFile.capillaryTemperature = value;
                    break;
                case "Aparture_Voltage_I":
                    udpFile.apartureVoltageI = value;
                    break;
                case "Aparture_Voltage_II":
                    udpFile.apartureVoltageII = value;
                    break;
                case "LowMass":
                    udpFile.lowMass = value;
                    break;
                case "HighMass":
                    udpFile.highMass = value;
                    break;
                case "SampleNo":
                    udpFile.sampleNo = value;
                    break;
                default:
                    break;
            }
            
            processingInt = false;
        } else {
            String value = stringBuffer.toString().trim();
            
            switch (qName) {
                case "DataPath":
                    udpFile.dataPath = value;
                    break;
                case "Operator":
                    udpFile.operator = value;
                    break;
                case "LastUser":
                    udpFile.lastUser = value;
                    break;
                case "LastModifier":
                    udpFile.lastModifier = value;
                    break;
                case "Machine":
                    udpFile.machine = value;
                    break;
                case "Static_Attenuator":
                    udpFile.staticAttenuator = value;
                    break;
                case "Tune_filename":
                    udpFile.tuneFilename = value;
                    break;
                case "Auxiliary_1":
                    udpFile.auxiliary1 = value;
                    break;
                case "Auxiliary_2":
                    udpFile.auxiliary2 = value;
                    break;
                case "Auxiliary_3":
                    udpFile.auxiliary3 = value;
                    break;
                case "Commentary_1":
                    udpFile.commentary1 = value;
                    break;
                case "Commentary_2":
                    udpFile.commentary2 = value;
                    break;
                case "UserName":
                    udpFile.userName = value;
                    break;
                case "UserFunction":
                    udpFile.userFunction = value;
                    break;
                case "SampleID":
                    udpFile.sampleID = value;
                    break;
                case "SampleOrigin":
                    udpFile.sampleOrigin = value;
                    break;
                case "SampleSpecies":
                    udpFile.sampleSpecies = value;
                    break;
                case "SampleTissueType":
                    udpFile.sampleTissueType = value;
                    break;
                case "SampleStoring":
                    udpFile.sampleStoring = value;
                    break;
                case "SampleMatrix":
                    udpFile.sampleMatrix = value;
                    break;
                case "SampleAddInfos":
                    udpFile.sampleAddInfos = value;
                    break;
                case "MCPVersion":
                    udpFile.mcpVersion = value;
                    break;
                case "MCPTitle":
                    udpFile.mcpTitle = value;
                    break;
                case "OSVersion":
                    udpFile.osVersion = value;
                    break;
                case "OSTitle":
                    udpFile.osTitle = value;
                    break;
                case "PCName":
                    udpFile.pcName = value;
                    break;
                case "PCIP":
                    udpFile.pcip = value;
                    break;
            }
        }
    }
    
    public UDPFile getUDPFile() {
        return udpFile;
    }

    public static UDPFile parseUDPFile(String udpFile) {
        UDPFileHandler handler = new UDPFileHandler();
        File patternF = new File(udpFile);

        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse(patternF, handler);

        } catch (SAXException | IOException | ParserConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        return handler.udpFile;
    }
}
