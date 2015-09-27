package com.alanmrace.jimzmlconverter.Waters;

public class Line {
	
	private String units;
	
	private float x1;
	private float x2;
	private float y;
	
	public Line(String units) {
		this.units = units;
	}
	
	public String getUnits() {
		return units;
	}
	
	public void setX1(float x1) {
		this.x1 = x1;
	}
	
	public float getX1() {
		return x1;
	}
	
	public void setX2(float x2) {
		this.x2 = x2;
	}
	
	public float getX2() {
		return x2;
	}
	
	public void setY(float y) {
		this.y = y;
	}
	
	public float getY() {
		return y;
	}
	
	public float getLength() {
		if(x1 > x2)
			return x1 - x2;
		else
			return x2 - x1;
	}
}
