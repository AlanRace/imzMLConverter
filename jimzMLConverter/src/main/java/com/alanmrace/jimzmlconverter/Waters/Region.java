package com.alanmrace.jimzmlconverter.Waters;

import java.util.ArrayList;
import java.util.Iterator;

public class Region implements Iterable<Line> {

	private String name;
	private String type;
	
	private ArrayList<Line> lines;
	
	public Region(String name, String type) {
		this.name = name;
		this.type = type;
		
		lines = new ArrayList<Line>();
	}
	
	public void addLine(Line line) {
		lines.add(line);
	}
	
	public int numLines() {
		return lines.size();
	}
	
	public Line getLine(int index) {
		return lines.get(index);
	}

	@Override
	public Iterator<Line> iterator() {
		return lines.iterator();
	}
}