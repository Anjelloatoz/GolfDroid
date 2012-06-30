package com.alliancerational;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;

public class Hole {
	private GeoPoint green_front;
	private GeoPoint green_center;
	private GeoPoint green_rear;
	private String name = "";
	private int orientation = 0;
	private String satellite_tile_source = "";
	private String drawing_tile_source = "";
	ArrayList<Hazard> hazard_list = new ArrayList<Hazard>();
	
	Hole(GeoPoint green_front, GeoPoint green_center, GeoPoint green_rear, String name, int orientation, String satellite_tile_source, String drawing_tile_source){
		this.green_front = green_front;
		this.green_center = green_center;
		this.green_rear = green_rear;
		this.name = name;
		this.orientation = orientation;
		this.satellite_tile_source = satellite_tile_source;
		this.drawing_tile_source = drawing_tile_source;
	}
	
	public GeoPoint getGreenFront(){
		return this.green_front;
	}
	
	public GeoPoint getGreenCenter(){
		return this.green_center;
	}
	
	public GeoPoint getGreenRear(){
		return this.green_rear;
	}
	
	public String getName(){
		return this.name;
	}
	
	public int getOrientation(){
		return this.orientation;
	}
	
	public String getSatelliteTileSource(){
		return this.satellite_tile_source;
	}
	
	public String getDrawingTileSource(){
		return this.drawing_tile_source;
	}
}
