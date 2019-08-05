package com.alanmrace.jimzmlconverter.Waters;

import com.alanmrace.jimzmlconverter.exceptions.ConversionException;
import com.alanmrace.jimzmlparser.imzml.PixelLocation;

import java.util.ArrayList;
import java.util.Iterator;

import com.alanmrace.jimzmlparser.mzml.CVParam;
import com.alanmrace.jimzmlparser.mzml.Scan;
import com.alanmrace.jimzmlparser.mzml.Spectrum;
import com.alanmrace.jimzmlparser.mzml.SpectrumList;

public class PatternDefinition implements Iterable<Region> {

    // Laser size
    private String laserSizeUnits;
    private double laserSizeX;
    private double laserSizeY;

    // Plate size
    private String plateSizeUnits;
    private double plateSizeWidth;
    private double plateSizeLength;

    // Valid area
    private double validAreaX1;
    private double validAreaX2;
    private double validAreaY1;
    private double validAreaY2;

    // Regions
    private ArrayList<Region> regions;

    private double meanThreshold = 1.03;
    private double meanThresholdIncrements = 0.01;

    private int maxRepeats = 50;
    private int currentRepeat = 0;

    private int errorThreshold = Integer.MAX_VALUE;

    public PatternDefinition() {
        regions = new ArrayList<Region>();
    }

    public void addRegion(Region region) {
        regions.add(region);
    }

    public void setLaserSizeUnits(String units) {
        this.laserSizeUnits = units;
    }

    public String getLaserSizeUnits() {
        return laserSizeUnits;
    }

    public void setLaserSizeX(double x) {
        this.laserSizeX = x;
    }

    public double getLaserSizeX() {
        return laserSizeX;
    }

    public void setLaserSizeY(double y) {
        this.laserSizeY = y;
    }

    public double getLaserSizeY() {
        return laserSizeY;
    }

    public void setPlateSizeUnits(String units) {
        this.plateSizeUnits = units;
    }

    public String getPlateSizeUnits() {
        return plateSizeUnits;
    }

    public void setPlateSizeWidth(double width) {
        this.plateSizeWidth = width;
    }

    public double getPlateSizeWidth() {
        return plateSizeWidth;
    }

    public void setPlateSizeLength(double length) {
        this.plateSizeLength = length;
    }

    public double getPlateSizeLength() {
        return plateSizeLength;
    }

    public void setValidAreaX1(double x1) {
        this.validAreaX1 = x1;
    }

    public double getValidAreaX1() {
        return validAreaX1;
    }

    public void setValidAreaX2(double x2) {
        this.validAreaX2 = x2;
    }

    public double getValidAreaX2() {
        return validAreaX2;
    }

    public void setValidAreaY1(double y1) {
        this.validAreaY1 = y1;
    }

    public double getValidAreaY1() {
        return validAreaY1;
    }

    public void setValidAreaY2(double y2) {
        this.validAreaY2 = y2;
    }

    public double getValidAreaY2() {
        return validAreaY2;
    }

    public PixelLocation[] convertToPixelLocations(SpectrumList spectrumList, int numSpectraPerPixel) throws ConversionException {
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        double[] chromatogram = new double[spectrumList.size() / numSpectraPerPixel];
        double[] chromatogramDiff = new double[chromatogram.length - 1];
        double meanTimeDelay = 0;
        double maxTimeDelay = 0;

        int spectrumIndex = 0;

        for (int i = 0; i < spectrumList.size(); i += numSpectraPerPixel) {
            Spectrum spectrum = spectrumList.getSpectrum(i);

            CVParam scanStartTime = spectrum.getScanList().getScan(0).getCVParam(Scan.SCAN_START_TIME_ID);
            //System.out.println(scanStartTime);

            chromatogram[spectrumIndex++] = scanStartTime.getValueAsDouble();
        }

        for (int i = 0; i < chromatogramDiff.length; i++) {
            chromatogramDiff[i] = (chromatogram[i + 1] - chromatogram[i]);
            meanTimeDelay = meanTimeDelay + chromatogramDiff[i];

            if (chromatogramDiff[i] > maxTimeDelay) {
                maxTimeDelay = chromatogramDiff[i];
            }
        }

        meanTimeDelay /= chromatogramDiff.length;

        int numLines = 0;

        for (Region region : regions) {
            for (Line line : region) {
                numLines++;

                if (line.getX1() < minX) {
                    minX = line.getX1();
                }
                if (line.getX1() > maxX) {
                    maxX = line.getX1();
                }

                if (line.getX2() < minX) {
                    minX = line.getX2();
                }
                if (line.getX2() > maxX) {
                    maxX = line.getX2();
                }

                if (line.getY() < minY) {
                    minY = line.getY();
                }
                if (line.getY() > maxY) {
                    maxY = line.getY();
                }

//				System.out.println("" + line.getX1() + ", " + line.getX2() + ", " + line.getY());
//				System.out.println("X: " + minX + " -> " + maxX);
//				System.out.println("Y: " + minY + " -> " + maxY);
            }
        }

        //int[] numPixelsOnLines = new int[numLines];
        int numErrors = errorThreshold;
        ArrayList<PixelLocation> pixelLocations = new ArrayList<>();

        while (numErrors >= errorThreshold && currentRepeat < maxRepeats) {
            numErrors = 0;
            int currentLineNum = 0;

            ArrayList<Integer> numPixelsOnLines = new ArrayList<>(numLines);

            numPixelsOnLines.add(currentLineNum, 1);

            System.out.println("Number of Lines in pattern file: " + numLines);
            System.out.println("Mean time delay: " + meanTimeDelay);
            System.out.println("Max time delay: " + maxTimeDelay);
            System.out.println("Mean threshold: " + meanThreshold);

            for (int i = 0; i < chromatogramDiff.length; i++) {
                if(currentLineNum == 0)
                    System.out.println("Line " + currentLineNum +  ": chromatogramDiff[i] = " + chromatogramDiff[i]);

                if (chromatogramDiff[i] > meanTimeDelay * meanThreshold) // Was 1.0120
                {
                    //System.out.println("Line " + currentLineNum +  ": chromatogramDiff[i] = " + chromatogramDiff[i]);
                    currentLineNum++;
                }

                int curValue = 0;

                if (numPixelsOnLines.size() > currentLineNum) {
                    curValue = numPixelsOnLines.get(currentLineNum);
                } else {
                    numPixelsOnLines.add(currentLineNum, 0);
                }

                numPixelsOnLines.set(currentLineNum, curValue + 1);
            }

    //		System.out.println("-----");
            //		for(int lineCount : numPixelsOnLines)
            //			System.out.println(lineCount);
            //		System.out.println("-----");
            System.out.println("X: " + minX + " -> " + maxX);
            System.out.println("Y: " + minY + " -> " + maxY);

            pixelLocations = new ArrayList<>();

            currentLineNum = 0;

            // try {
            for (Region region : regions) {
                for (int lineNum = 0; lineNum < region.numLines(); lineNum++) {
                    Line line = region.getLine(lineNum);

                    int yCoord = (int) Math.round(((line.getY() - minY) / laserSizeY) + 1);

                    int xCoordOffset = (int) Math.round(((line.getX1() - minX) / laserSizeX) + 1);

                    if (currentLineNum >= numPixelsOnLines.size()) {
                        System.out.println("NUMBER OF LINES DETERMINED EXCEEDS NUMBER OF LINES RECORDED");
                        numErrors++;

                        break;
                    }

                    // Do Waters use ceil rather than round here?
                    int numPixelsOnCurrentLine = numPixelsOnLines.get(currentLineNum); // ((int) Math.round(line.getLength() / laserSizeX)) + 1;
                    int expectedNumberOfPixels = ((int) Math.round(line.getLength() / laserSizeX)) + 1;

                    int numPixelsLeeway = 2;

                    // Compare with the chromatogram to see if we have a believable number of pixels
                    if (!((numPixelsOnCurrentLine - numPixelsLeeway) <= expectedNumberOfPixels && expectedNumberOfPixels <= (numPixelsOnCurrentLine + numPixelsLeeway))) {
                        if (expectedNumberOfPixels > numPixelsOnCurrentLine) {
                            System.out.println("[" + currentLineNum + "] " + "Less on line: " + currentLineNum + ". Found " + numPixelsOnCurrentLine + ". Expected " + expectedNumberOfPixels);

                            currentLineNum++;

                            if (numPixelsOnLines.size() <= currentLineNum) {
                                System.out.println("[" + currentLineNum + "] " + "Needed to merge but couldn't. Setting number of pixels on line to " + (numPixelsOnCurrentLine));
                            } else {
                                System.out.println("[" + currentLineNum + "] " + "Merging lines. Changing number of pixels on line from " + numPixelsOnLines.get(currentLineNum) + " to " + (numPixelsOnCurrentLine + numPixelsOnLines.get(currentLineNum)));
                                numPixelsOnCurrentLine = numPixelsOnCurrentLine + numPixelsOnLines.get(currentLineNum);
                            }
                        } else {
                            System.out.println("[" + currentLineNum + "] " + "More on line: " + currentLineNum + ". Found " + numPixelsOnCurrentLine + ". Expected " + expectedNumberOfPixels);

                            if (expectedNumberOfPixels == 1) {
                                currentLineNum -= 1;
                                numPixelsOnLines.set(currentLineNum, 0);
                                numPixelsOnLines.set(currentLineNum + 1, numPixelsOnLines.get(currentLineNum + 1) + 1);//[currentLineNum + 1]++;

                                // Could try increasing num pixels on next line by 1, incrementing the current line num and then continue to next line - skipping the addition to pixelLocations
                            } else {
                                if (lineNum + 1 < region.numLines()) {
                                    int expectedNumberOfPixelsOnNextLine = ((int) Math.round(region.getLine(lineNum + 1).getLength() / laserSizeX)) + 1;

                                    int remainder = numPixelsOnLines.get(currentLineNum) - expectedNumberOfPixels - expectedNumberOfPixelsOnNextLine;

                                    System.out.println("[" + currentLineNum + "] " + "Expected on next line: " + expectedNumberOfPixelsOnNextLine + ". Remainder: " + remainder);

                                    currentLineNum -= 1;

                                    if (currentLineNum < 0) {
                                        currentLineNum = 0;
                                    }

                                    // Depending on the remainder, adjust the number of pixels that exist on the current line
                                    switch (remainder) {
                                        case -2:
                                            numPixelsOnLines.set(currentLineNum, expectedNumberOfPixels - 1);
                                            numPixelsOnLines.set(currentLineNum + 1, expectedNumberOfPixelsOnNextLine - 1);
                                            break;
                                        case -1:
                                            numPixelsOnLines.set(currentLineNum, expectedNumberOfPixels - 1);
                                            numPixelsOnLines.set(currentLineNum + 1, expectedNumberOfPixelsOnNextLine);
                                            break;
                                        case 0:
                                            numPixelsOnLines.set(currentLineNum, expectedNumberOfPixels);
                                            numPixelsOnLines.set(currentLineNum + 1, expectedNumberOfPixelsOnNextLine);
                                            break;
                                        case 1:
                                            numPixelsOnLines.set(currentLineNum, expectedNumberOfPixels + 1);
                                            numPixelsOnLines.set(currentLineNum + 1, expectedNumberOfPixelsOnNextLine);
                                            break;
                                        case 2:
                                            numPixelsOnLines.set(currentLineNum, expectedNumberOfPixels + 1);
                                            numPixelsOnLines.set(currentLineNum + 1, expectedNumberOfPixelsOnNextLine + 1);
                                            break;
                                        default:
                                            System.out.println("[" + currentLineNum + "] " + "Remainder not a common one: " + remainder);
                                            numPixelsOnLines.set(currentLineNum, expectedNumberOfPixels);

                                            System.out.println("[" + currentLineNum + "] " + "Changing next line from: " + numPixelsOnLines.get(currentLineNum + 1) + " to: " + (expectedNumberOfPixelsOnNextLine + remainder) + " (expected next line was: " + expectedNumberOfPixelsOnNextLine + ")");
//                                            numPixelsOnLines.set(currentLineNum + 1, expectedNumberOfPixelsOnNextLine + remainder);
                                    }
                                }
                            }

                            numPixelsOnCurrentLine = numPixelsOnLines.get(currentLineNum);

                        }

                        numErrors++;
                    }

                    for (int i = 0; i < numPixelsOnCurrentLine; i++) {
                        for (int z = 1; z <= numSpectraPerPixel; z++) {
                            pixelLocations.add(new PixelLocation((xCoordOffset + i), yCoord, z));
                        }
                    }

            //				for(float pos = line.getX1(); pos <= line.getX2(); pos += laserSizeX)
                    //					pixelLocations.add(new PixelLocation((xCoordOffset++), yCoord, 1));
                    //				System.out.println("" + line.getY());
                    //System.out.println("" + (xCoordOffset) + ", " + yCoord + "(" + numPixelsOnLine + ")");
                    currentLineNum++;
                }
            }
            //} catch (ArrayIndexOutOfBoundsException aiob) {
            //   throw new ImzMLConversionException("Invalid Waters pattern file for this dataset");
            //}

            //Collections.reverse(pixelLocations);
            
            meanThreshold += this.meanThresholdIncrements;
        }

        return pixelLocations.toArray(new PixelLocation[0]);
    }

    @Override
    public Iterator<Region> iterator() {
        return regions.iterator();
    }
}
