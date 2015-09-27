package com.alanmrace.jimzmlconverter.Waters;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PatternDefinitionHandler extends DefaultHandler {
	PatternDefinition patternDefinition;
	
	boolean processingLine = false;
	boolean processingValidArea = false;
	boolean processingPlateSize = false;
	boolean processingLaserSize = false;
	
	boolean processingDouble = false;
	protected StringBuffer stringBuffer;
	
	Line currentLine;
	Region currentRegion;
	
	public PatternDefinitionHandler() {
		patternDefinition = new PatternDefinition();
		
		stringBuffer = new StringBuffer();
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		// Most common attribute at the start to reduce the number of comparisons needed
		if(qName.equals("X1")) {
			stringBuffer.setLength(0);
			processingDouble = true;
			
		} else if(qName.equals("X2")) {
			stringBuffer.setLength(0);
			processingDouble = true;
			
		} else if(qName.equals("X")) {
			stringBuffer.setLength(0);
			processingDouble = true;
			
		} else if(qName.equals("Y")) {
			stringBuffer.setLength(0);
			processingDouble = true;
			
		} else if(qName.equals("Line")) {
			currentLine = new Line(attributes.getValue("Units"));
			
			currentRegion.addLine(currentLine);
			
			processingLine = true;
		} else if(qName.equals("Y1")) {
			stringBuffer.setLength(0);
			processingDouble = true;
			
		} else if(qName.equals("Y2")) {
			stringBuffer.setLength(0);
			processingDouble = true;
			
		} else if(qName.equals("Region")) {
			currentRegion = new Region(attributes.getValue("Name"), attributes.getValue("Type"));
			
			patternDefinition.addRegion(currentRegion);
		} else if(qName.equals("ValidArea")) {
			processingValidArea = true;
			
		} else if(qName.equals("Length")) {
			stringBuffer.setLength(0);
			processingDouble = true;
			
		} else if(qName.equals("Width")) {
			stringBuffer.setLength(0);
			processingDouble = true;
			
		} else if(qName.equals("PlateSize")) {
			processingPlateSize = true;
			
		} else if(qName.equals("LaserSize")) {
			processingLaserSize = true;
			
		} else if(qName.equals("Metafile")) {
			stringBuffer.setLength(0);
			
		}
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		stringBuffer.append(ch, start, length);
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(processingDouble) {
			float value = Float.parseFloat(stringBuffer.toString());
			
			//System.out.println(stringBuffer.toString() + " -> " + value);
			
			if(qName.equals("X1")) {
				if(processingLine) {
					currentLine.setX1(value);
				} else if(processingValidArea) {
					patternDefinition.setValidAreaX1(value);
				}
			} else if(qName.equals("X2")) {
				if(processingLine) {
					currentLine.setX2(value);
				} else if(processingValidArea) {
					patternDefinition.setValidAreaX2(value);
				}
			} else if(qName.equals("Y")) {
				if(processingLine) {
					currentLine.setY(value);
				} else if(processingLaserSize) {
					patternDefinition.setLaserSizeY(value);
				}
			} else if(qName.equals("Y1")) {
				patternDefinition.setValidAreaY1(value);
			} else if(qName.equals("Y2")) {
				patternDefinition.setValidAreaY2(value);
			} else if(qName.equals("X")) {
				patternDefinition.setLaserSizeX(value);
			} else if(qName.equals("Width")) {
				patternDefinition.setPlateSizeWidth(value);
			} else if(qName.equals("Length")) {
				patternDefinition.setPlateSizeLength(value);
			}
			
			processingDouble = false;
		} else if(qName.equals("Metafile")) {
			
		}
		
		if(qName.equals("Line"))
			processingLine = false;
		if(qName.equals("ValidArea"))
			processingValidArea = false;
		if(qName.equals("PlateSize"))
			processingPlateSize = false;
		if(qName.equals("LaserSize"))
			processingLaserSize = false;
	}
	
	public PatternDefinition getPatternDefinition() {
		return patternDefinition;
	}
}
