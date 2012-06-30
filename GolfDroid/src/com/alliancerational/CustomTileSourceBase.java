package com.alliancerational;

import org.osmdroid.ResourceProxy.string;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;

public class CustomTileSourceBase extends BitmapTileSourceBase {

public CustomTileSourceBase(String aName, string aResourceId, int aZoomMinLevel,
 int aZoomMaxLevel, int aTileSizePixels, String aImageFilenameEnding){
 super(aName, aResourceId, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
 aImageFilenameEnding);
 // TODO Auto-generated constructor stub
 }

}