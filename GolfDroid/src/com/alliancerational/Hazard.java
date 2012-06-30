package com.alliancerational;

import org.osmdroid.util.GeoPoint;

public class Hazard {
	private GeoPoint front;
	private GeoPoint rear;
	
	Hazard(GeoPoint front, GeoPoint green_rear){
		this.front = front;
		this.rear = green_rear;
	}
	
	public GeoPoint getHazardFront(){
		return this.front;
	}
	
	public GeoPoint getHazardRear(){
		return this.rear;
	}
}
