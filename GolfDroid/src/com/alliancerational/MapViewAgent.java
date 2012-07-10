package com.alliancerational;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import org.osmdroid.views.MapController;
import org.osmdroid.views.overlay.Overlay;

public class MapViewAgent {
	int map_type = 0;
	MapView mapView;
	MapController mapController;
	boolean data_connection = true;

	MapViewAgent(View mapview, boolean data_connection){
		mapView = (MapView)mapview;
		this.data_connection = data_connection;
		mapController = mapView.getController();
		MapOverlay map_overlay = new MapOverlay(mapView.getContext());
		mapView.getOverlays().add(map_overlay);
	}

	public void setTiles(String tilesource){
		mapView.setTileSource(new OnlineTileSourceBase(tilesource, ResourceProxy.string.unknown, 0, 19, 256, ".png", "http://mt3.google.com/vt/v=w2.97") {
			@Override
			public String getTileURLString(final MapTile aTile) {
				return getBaseUrl() + "&x=" + aTile.getX() + "&y=" + aTile.getY() + "&z=" + aTile.getZoomLevel();
			}
		});
		mapView.invalidate();
	}
	
	public void setCenter(int lat, int lon){
		GeoPoint center_point = new GeoPoint(lat, lon);
		mapController.setCenter(center_point);
	}
	
	public void setZoom(int zoom){
		mapController.setZoom(zoom);
	}

	class MapOverlay extends Overlay{
		MapOverlay(Context ctx){
			super(ctx);
		}
		protected void draw(Canvas c, MapView mapView, boolean shadow) {

		}

		@Override
		public boolean onDoubleTap(MotionEvent e, MapView mapView) {
			return true;
		}
	}
}
