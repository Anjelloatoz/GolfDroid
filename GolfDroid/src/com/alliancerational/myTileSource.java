package com.alliancerational;

import org.osmdroid.ResourceProxy.string;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;

public class myTileSource extends BitmapTileSourceBase {

	public myTileSource(String aName, string aResourceId, int aZoomMinLevel,
			int aZoomMaxLevel, int aTileSizePixels, String aImageFilenameEnding) {
		super(aName, aResourceId, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
				aImageFilenameEnding);
	}
}