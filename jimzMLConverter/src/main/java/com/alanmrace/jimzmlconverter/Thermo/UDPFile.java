/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanmrace.jimzmlconverter.Thermo;

import java.util.Date;

/**
 *
 * @author Alan
 */
public class UDPFile {
    
    // Global
    Date startTime;
    Date endTime;
    int specOrigin;
    String dataPath;
    String operator;
    String lastUser;
    String lastModifier;
    String machine;
    
    // User
    
    // Scan
    int scanScenario;
    int scanDirection;
    int scanForm;
    int massMin;
    int massMax;
    int originX;
    int originY;
    int originZ;
    int oldOriginX;
    int oldOriginY;
    int oldOriginZ;
    
    int resolutionX;
    int resolutionY;
    int resolutionZ;
    
    int voxelSizeX;
    int voxelSizeY;
    int voxelSizeZ;
    
    int imageSizeX;
    int imageSizeY;
    int imageSizeZ;
    
    int maxX;
    int maxY;
    int maxZ;
    int maxN;
    
    int scanEventsPerSpot;
    int scanEventsPerPixel;
    
    int pattern;
    
    // Spectra
    
    // Instrument
    int laserPower;
    String staticAttenuator;
    int variableAttenuator;
    int attenuatorCalibration1;
    int attenuatorCalibration2;
    int attenuatorCalibration3;
    int targetVoltage;
    int apartureVoltageI;
    int apartureVoltageII;
    int capillaryTemperature;
    String tuneFilename;
    String auxiliary1;
    String auxiliary2;
    String auxiliary3;
    String commentary1;
    String commentary2;
    
    // Experiment
    String userName;
    Date userDate;
    String userFunction;
    int lowMass;
    int highMass;
    String sampleID;
    int sampleNo;
    Date sampleInLabDate;
    String sampleOrigin;
    String sampleSpecies;
    String sampleTissueType;
    String sampleStoring;
    String sampleMatrix;
    Date sampleMatrixAppDate;
    String sampleAddInfos;
    String mcpVersion;
    String mcpTitle;
    String osVersion;
    String osTitle;
    String pcName;
    String pcip;
    
    public int getMaxX() {
        return maxX;
    }
    
    public int getMaxY() {
        return maxY;
    }
    
    public int getResolutionX() {
        return resolutionX;
    }
    
    public int getResolutionY() {
        return resolutionY;
    }
    
    public String getDataPath() {
        return dataPath;
    }
}
