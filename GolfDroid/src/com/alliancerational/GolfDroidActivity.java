package com.alliancerational;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.content.Context;
import android.graphics.Point;
import android.graphics.DashPathEffect;
import android.graphics.Bitmap;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.RadioButton;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.Display;
import android.widget.TextView;
import android.view.Menu;

import org.osmdroid.api.IGeoPoint;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.*;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.api.IMapView;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.util.ArrayList;
import java.util.List;
import com.alliancerational.android.FileIO.LocalFileReader;

public class GolfDroidActivity extends Activity implements LocationListener{
	private MapController mapController;
	private MapView mapView;
	private boolean map_photo_mode = true;
	private GeoPoint green_center = new GeoPoint(0,0);
	private GeoPoint green_front = new GeoPoint(0,0);
	private GeoPoint green_rear = new GeoPoint(0,0);
	private GeoPoint tee_point = new GeoPoint(0,0);
	private GeoPoint user_location = new GeoPoint(51.776193,-0.195815);
	private GeoPoint tapped_location = null;
	ArrayList<Hole> hole_list = new ArrayList<Hole>();
	ArrayList<Hazard> hazard_list = new ArrayList<Hazard>();
	Paint white_text_paint = new Paint();
	Paint red_text_paint = new Paint();
	Paint green_text_paint = new Paint();
	Paint dashed_line_paint = new Paint();
	Paint blue_line_paint = new Paint();
	Paint yellow_boundary_paint = new Paint();
	Bitmap red_flag_bmp;
	Bitmap tee_bmp;
	Bitmap golfer_bmp;
	Bitmap cross_bmp;
	
	TextView second_layout_label;

	private int scrollLimitNorthE6;
	private int scrollLimitEastE6;
	private int scrollLimitSouthE6;
	private int scrollLimitWestE6;
	private int lastVaildX;
	private int lastValidY;
	
	LocationManager mlocManager;
	private String provider;
	int location_segment_number = 250;
	
	private String club_id;
	private String validation_result = "no";
	
	Matrix transform_matrix = new Matrix();
	AffineTransform tap_transformer = null;
	
	int bearing_degrees = 60;
	int zoom_level = 0;
	AnimatedPanel second_layout;
	int hole_number = 0;
	RotatingLinearLayout rll;
	
	String satellite_tile_source = "";
	String drawing_tile_source = "";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		second_layout_label = (TextView) findViewById(R.id.second_screen_label);
		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
        alertbox.setMessage("Before reading the holes file.");
        alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                stepOne();
            }
        });
        alertbox.show();
	}
	
	private void stepOne(){
		Document hole_file = LocalFileReader.readFile("/sdcard/holes/MillGreenHoles.xml", this);
		GolfClub golf_club = new GolfClub(hole_file);
		getHole(golf_club.getHoleList());
	}
	
	private void getHole(final ArrayList<Hole> holes_list){
		final RadioButton[] rb = new RadioButton[holes_list.size()];
		final RadioGroup rg = new RadioGroup(this);
		rg.setOrientation(RadioGroup.VERTICAL);
		for(int i=0; i<holes_list.size(); i++){
			rb[i]  = new RadioButton(this);
			rg.addView(rb[i]);
			rb[i].setText(holes_list.get(i).getName());
		}
		final AlertDialog.Builder hole_dialog = new AlertDialog.Builder(this);
		hole_dialog.setTitle("Hole");
		hole_dialog.setMessage("Please select the hole");
		hole_dialog.setView(rg);
		hole_dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				int rbid = rg.getCheckedRadioButtonId();
				View rb = rg.findViewById(rbid);
				int idx = rg.indexOfChild(rb);

				System.out.println("Selected hole is: "+idx);
				setHole(holes_list.get(idx));
				rll.setBearing(bearing_degrees);
				hole_number = idx;
			}
		});
		hole_dialog.show();
	}
	
	private void setHole(Hole hole){
		green_center = hole.getGreenCenter();
		green_front = hole.getGreenFront();
		green_rear = hole.getGreenRear();
//		bearing_degrees = hole.getOrientation();
		this.satellite_tile_source = hole.getSatelliteTileSource();
		this.drawing_tile_source = hole.getDrawingTileSource();
		System.out.println("satellite_tile_source is: "+satellite_tile_source);
		System.out.println("drawing_tile_source is: "+drawing_tile_source);
		mapView.setTileSource(new OnlineTileSourceBase(satellite_tile_source, ResourceProxy.string.unknown, 0, 19, 256, ".png", "http://mt3.google.com/vt/v=w2.97") {
			@Override
			public String getTileURLString(final MapTile aTile) {
				return getBaseUrl() + "&x=" + aTile.getX() + "&y=" + aTile.getY() + "&z=" + aTile.getZoomLevel();
			}
		});
		mapController.animateTo(green_center);
		mapView.invalidate();
	}
	
	private void stepThree(){
		second_layout = (AnimatedPanel)findViewById(R.id.second_layout);
		second_layout.setLayoutAnimExit(second_layout, second_layout.getContext());
		
		DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        System.out.println("DPI is: "+dm.heightPixels+", "+dm.widthPixels);
		inputUserID();
		
		white_text_paint.setColor(Color.WHITE);
		white_text_paint.setStyle(Paint.Style.STROKE);
		white_text_paint.setTextSize(24);

		red_text_paint.setColor(Color.RED);
		red_text_paint.setStyle(Paint.Style.STROKE);
		red_text_paint.setTextSize(24);
		
		green_text_paint.setColor(Color.GREEN);
		green_text_paint.setStyle(Paint.Style.STROKE);
		green_text_paint.setTextSize(24);

		dashed_line_paint.setColor(Color.RED);
		DashPathEffect dashPath = new DashPathEffect(new float[]{10,10}, 1);
//		dashed_line_paint.setPathEffect(dashPath);
		dashed_line_paint.setStrokeWidth(3);
		
		blue_line_paint.setColor(Color.BLUE);
		blue_line_paint.setStrokeWidth(2);
		blue_line_paint.setTextSize(24);
		
		yellow_boundary_paint.setColor(Color.WHITE);
		yellow_boundary_paint.setStyle(Paint.Style.STROKE);
		yellow_boundary_paint.setPathEffect(dashPath);
		yellow_boundary_paint.setStrokeWidth(2);
		
		mapView = (MapView) findViewById(R.id.mapview);
		
		rll = (RotatingLinearLayout)findViewById(R.id.rotating_layout);
		rll.setBearing(bearing_degrees);
		System.out.println("Check Point 01");

		mapView.setTileSource(new OnlineTileSourceBase("MillGreen2", ResourceProxy.string.unknown, 0, 19, 256, ".png", "http://mt3.google.com/vt/v=w2.97") {
			@Override
			public String getTileURLString(final MapTile aTile) {
				return getBaseUrl() + "&x=" + aTile.getX() + "&y=" + aTile.getY() + "&z=" + aTile.getZoomLevel();
			}
		});
		red_flag_bmp = BitmapFactory.decodeResource(getResources(), R.drawable.redflag);
		tee_bmp = BitmapFactory.decodeResource(getResources(), R.drawable.golftee);
		golfer_bmp = BitmapFactory.decodeResource(getResources(), R.drawable.golfer);
		cross_bmp = BitmapFactory.decodeResource(getResources(), R.drawable.cross);

		MapOverlay map_overlay = new MapOverlay(this);
		mapView.getOverlays().add(map_overlay);

		mapController = mapView.getController();
		mapController.setZoom(18);
		zoom_level = 18;
		GeoPoint point2 = new GeoPoint(51776898, -196173);
		mapController.setCenter(point2);  
		mapView.setUseDataConnection(false);
		initMainControls();
		
		mapView.invalidate();
/*		mapView.setMapListener(new MapListener() {

			@Override
			public boolean onZoom(ZoomEvent arg0) {
				return false;
			}

			@Override
			public boolean onScroll(ScrollEvent arg0) {

				try{
					if (mapView.getBoundingBox().getLatNorthE6() >= scrollLimitNorthE6 ||
							mapView.getBoundingBox().getLonEastE6() >= scrollLimitEastE6 ||
							mapView.getBoundingBox().getLatSouthE6() <= scrollLimitSouthE6 ||
							mapView.getBoundingBox().getLonWestE6() <= scrollLimitWestE6) {
						mapView.scrollTo(lastVaildX, lastValidY);
					} else {
						lastVaildX = arg0.getX();
						lastValidY = arg0.getY();
					}
				}
				catch(Exception ex){
					System.out.println("Map listener issue.");
				}

				return false;
			}
		});*/
		mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = mlocManager.getBestProvider(criteria, false);

        LocationListener mlocListener = new GeoUpdateHandler();
        
        mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
        mlocManager.addGpsStatusListener(gpsListener);
        if (!mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){  
            System.out.println("GPS disabled");
      }

	}
	
	
	
	private void inputUserID(){
		
		LayoutInflater factory = LayoutInflater.from(this);
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final View textEntryView = factory.inflate(R.layout.input_form, null);

		alert.setTitle("User ID");
		alert.setMessage("Please enter your details");

		alert.setView(textEntryView);
		final EditText input1 = (EditText) textEntryView .findViewById(R.id.player1_id);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String user_id = input1.getText().toString();
//				validateUserID(""+club_id, user_id);
//				checkValidity();
			}
		});

		alert.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				System.exit(0);
			}
		});
		alert.show();
	}
	
	private void checkValidity(){
		System.out.println("checkValidity called:"+validation_result+"|");
		if(!validation_result.contains("yes")){
			AlertDialog.Builder confirmation = new AlertDialog.Builder(this);
			confirmation.setTitle("Invalid ID");
			confirmation.setMessage("Please check your ID and Re Enter.");
			confirmation.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					inputUserID();
				  }
				});

				confirmation.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				    System.exit(0);
				  }
				});
				confirmation.show();
		}
	}
	
	public void onLocationChanged(Location location) {}
	public void onProviderDisabled(String provider) {}
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	public void onProviderEnabled(String provider) {}

	/*	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    if (event.getAction() == MotionEvent.ACTION_DOWN) {
	    	System.out.println("Action Down: "+event.getX());
	        // manage down press
	    }
	    else if (event.getAction() == MotionEvent.ACTION_MOVE) {
	    	System.out.println("Action Move: "+event.getX());
	        // manage move
	    }
	    else {
	        // manage any other MotionEvent
	    }
	    System.out.println("onTouchEvent called");
	    return super.onTouchEvent(event);
	}*/
	
/*	@Override
    public boolean dispatchTouchEvent(MotionEvent event) {
		System.out.println("Dispatch Touch Events called: "+event.getX()+", "+event.getY());
		float[] coords = new float[] {
                event.getX(), event.getY()
        };
		
	        
	        adjustCoords(coords, bearing_degrees);
	        MotionEvent evt = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event
	                .getAction(), coords[0], coords[1], event.getPressure(), event.getSize(), event
	                .getMetaState(), event.getXPrecision(), event.getYPrecision(), event.getDeviceId(),
	                event.getEdgeFlags());
	        System.out.println("Altered Event: "+evt.getX()+", "+evt.getY());
	        evt.setLocation(coords[0], coords[1]);
		        return super.dispatchTouchEvent(evt);
    }*/

	class MapOverlay extends Overlay{
		MapOverlay(Context ctx){
			super(ctx);
		}
		protected void draw(Canvas c, MapView mapView, boolean shadow) {

			Point green_center_point = new Point();
			mapView.getProjection().toPixels(green_center, green_center_point);
			c.save();
			c.rotate(bearing_degrees, green_center_point.x, green_center_point.y);
			c.drawText(""+green_center.distanceTo(user_location), green_center_point.x+100, green_center_point.y, white_text_paint);
			c.drawLine(green_center_point.x, green_center_point.y , (green_center_point.x)+100, green_center_point.y, white_text_paint);
			c.restore();

			Point green_front_point = new Point();
			mapView.getProjection().toPixels(green_front, green_front_point);

			c.save();
			c.rotate(bearing_degrees, green_front_point.x, green_front_point.y);
			c.drawLine(green_front_point.x, green_front_point.y , (green_front_point.x)+90, green_front_point.y, green_text_paint);
			c.drawText(""+green_front.distanceTo(user_location), green_front_point.x+100, green_front_point.y, green_text_paint);
			c.restore();

			Point green_rear_point = new Point();
			mapView.getProjection().toPixels(green_rear, green_rear_point);
			c.save();
			c.rotate(bearing_degrees, green_rear_point.x, green_rear_point.y);
			c.drawLine(green_rear_point.x, green_rear_point.y , (green_rear_point.x)+90, green_rear_point.y, green_text_paint);
			c.drawText(""+green_rear.distanceTo(user_location), green_rear_point.x+100, green_rear_point.y, green_text_paint);
			c.restore();

			for(int i = 0; i < hole_list.get(hole_number).hazard_list.size(); i++){
				mapView.getProjection().toPixels(hole_list.get(hole_number).hazard_list.get(i).getHazardFront(), green_rear_point);
				c.save();
				c.rotate(bearing_degrees, green_rear_point.x, green_rear_point.y);
				c.drawLine(green_rear_point.x, green_rear_point.y , (green_rear_point.x)+90, green_rear_point.y, dashed_line_paint);
				c.drawText(""+hole_list.get(hole_number).hazard_list.get(i).getHazardFront().distanceTo(user_location), green_rear_point.x+100, green_rear_point.y, red_text_paint);
				c.restore();

				
				mapView.getProjection().toPixels(hole_list.get(hole_number).hazard_list.get(i).getHazardRear(), green_rear_point);
				c.save();
				c.rotate(bearing_degrees, green_rear_point.x, green_rear_point.y);
				c.drawLine(green_rear_point.x, green_rear_point.y , (green_rear_point.x)+90, green_rear_point.y, dashed_line_paint);
				c.drawText(""+hole_list.get(hole_number).hazard_list.get(i).getHazardRear().distanceTo(user_location), green_rear_point.x+100, green_rear_point.y, red_text_paint);
				c.restore();
			}

			mapView.getProjection().toPixels(tee_point, green_rear_point);
			c.save();
			c.rotate(bearing_degrees, green_center_point.x, green_center_point.y);
			c.drawBitmap(red_flag_bmp, green_center_point.x-25, green_center_point.y-205, null);
			c.restore();
			
			c.save();
			c.rotate(bearing_degrees, green_rear_point.x, green_rear_point.y);
			c.drawBitmap(tee_bmp, green_rear_point.x-45, green_rear_point.y-105, null);
			c.restore();

//			c.drawText("x", green_rear_point.x, green_rear_point.y, red_text_paint);

			Point user_point = new Point();
			mapView.getProjection().toPixels(user_location, user_point);
			float angle_to_green = ((float)user_location.bearingTo(green_center))-90;
//			System.out.println("Angle: "+user_location.bearingTo(green_center));
			final RectF oval = new RectF();
			oval.set(user_point.x - mapView.getProjection().metersToEquatorPixels(50), user_point.y - mapView.getProjection().metersToEquatorPixels(50), user_point.x + mapView.getProjection().metersToEquatorPixels(50), user_point.y + mapView.getProjection().metersToEquatorPixels(50));
			c.drawArc(oval, angle_to_green-45, 90, false, yellow_boundary_paint);
			oval.set(user_point.x - mapView.getProjection().metersToEquatorPixels(100), user_point.y - mapView.getProjection().metersToEquatorPixels(100), user_point.x + mapView.getProjection().metersToEquatorPixels(100), user_point.y + mapView.getProjection().metersToEquatorPixels(100));
			c.drawArc(oval, angle_to_green-45, 90, false, yellow_boundary_paint);
			oval.set(user_point.x - mapView.getProjection().metersToEquatorPixels(150), user_point.y - mapView.getProjection().metersToEquatorPixels(150), user_point.x + mapView.getProjection().metersToEquatorPixels(150), user_point.y + mapView.getProjection().metersToEquatorPixels(150));
			c.drawArc(oval, angle_to_green-45, 90, false, yellow_boundary_paint);
			
//			c.drawCircle(green_rear_point.x, green_rear_point.y, mapView.getProjection().metersToEquatorPixels(50), yellow_boundary_paint);
//			c.drawCircle(green_rear_point.x, green_rear_point.y, mapView.getProjection().metersToEquatorPixels(100), yellow_boundary_paint);
//			c.drawCircle(green_rear_point.x, green_rear_point.y, mapView.getProjection().metersToEquatorPixels(150), yellow_boundary_paint);

			c.save();
			c.rotate(bearing_degrees, user_point.x, user_point.y);
			c.drawBitmap(golfer_bmp, user_point.x-63, user_point.y-99, null);
			c.restore();
			
			if(tapped_location!=null){
				
				mapView.getProjection().toPixels(tapped_location, green_rear_point);
				
				float[] tmp = new float[2];
				tmp[0] = green_rear_point.x;
				tmp[1] = green_rear_point.y;
Matrix m = new Matrix();
//				c.drawLine(user_point.x, user_point.y, green_rear_point.x, green_rear_point.y, blue_line_paint);
//				c.drawLine( green_rear_point.x, green_rear_point.y, green_center_point.x, green_center_point.y, blue_line_paint);
				
				c.save();

				c.rotate(bearing_degrees, green_rear_point.x, green_rear_point.y);
				c.getMatrix(m);
				c.drawBitmap(cross_bmp, green_rear_point.x-30, green_rear_point.y+25, null);
				c.drawText(""+tapped_location.distanceTo(user_location), green_rear_point.x+60, green_rear_point.y+70, blue_line_paint);
				c.restore();
//				m.mapPoints(tmp);
//				m.setTranslate((int)(-200*Math.cos(bearing_degrees)), 0);
//				m.mapPoints(tmp);
				c.drawLine(user_point.x, user_point.y, tmp[0], tmp[1]+75, blue_line_paint);
				c.drawLine( tmp[0], tmp[1]+75, green_center_point.x, green_center_point.y, blue_line_paint);
			}
		}
		
		@Override
        public boolean onDoubleTap(MotionEvent e, MapView mapView) {
			
			System.out.println("Double tap: "+e.getX()+", "+e.getY());
			IGeoPoint point = mapView.getProjection().fromPixels(e.getX(), e.getY());
			tapped_location = new GeoPoint(point.getLatitudeE6(), point.getLongitudeE6());
			mapView.invalidate();
            return true;
        }
	}

	private void initMainControls(){
		final Button map_button = (Button)findViewById(R.id.map_button);
		map_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				map_photo_mode = !map_photo_mode;
				if(map_photo_mode){
					mapView.setTileSource(new OnlineTileSourceBase(satellite_tile_source, ResourceProxy.string.unknown, 0, 19, 256, ".png", "http://mt3.google.com/vt/v=w2.97") {
						@Override
						public String getTileURLString(final MapTile aTile) {
							return getBaseUrl() + "&x=" + aTile.getX() + "&y=" + aTile.getY() + "&z=" + aTile.getZoomLevel();
						}
					});
					map_button.setText("Plan");
				}
				else{
					mapView.setTileSource(new OnlineTileSourceBase(drawing_tile_source, ResourceProxy.string.unknown, 0, 19, 256, ".png", "http://mt3.google.com/vt/v=w2.97") {
						@Override
						public String getTileURLString(final MapTile aTile) {
							return getBaseUrl() + "&x=" + aTile.getX() + "&y=" + aTile.getY() + "&z=" + aTile.getZoomLevel();
						}
					});
					map_button.setText("Sat");
				}
				mapView.invalidate();
				mapView.refreshDrawableState();
			}
		});
		final Button exit_button = (Button)findViewById(R.id.exit_button);
		exit_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				System.exit(0);
			}
		});
		final Button zoom_in_button = (Button)findViewById(R.id.zoom_in);
		zoom_in_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(zoom_level<mapView.getMaxZoomLevel()){
					mapController.setZoom(++zoom_level);
					mapView.invalidate();
				}
				}
		});
		final Button zoom_out_button = (Button)findViewById(R.id.zoom_out);
		zoom_out_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(zoom_level>mapView.getMinZoomLevel()){
					mapController.setZoom(--zoom_level);
					mapView.invalidate();
				}
			}
		});
		final Button score_sheet_button = (Button)findViewById(R.id.score_sheet_button);
		score_sheet_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				second_layout_label.setText("This is the Score Sheet screen");
				second_layout.setLayoutAnimEntrance(second_layout, second_layout.getContext());
			}
		});
		final Button back_button = (Button)findViewById(R.id.back_button);
		back_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				second_layout.setLayoutAnimExit(second_layout, second_layout.getContext());
			}
		});
		
		final Button other_button = (Button)findViewById(R.id.another_button);
		other_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				second_layout_label.setText("Some other screen");
				second_layout.setLayoutAnimEntrance(second_layout, second_layout.getContext());
			}
		});
		
		final Button hole_button = (Button)findViewById(R.id.hole);
		hole_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				getHole();
			}
		});
	}

	

//	private void readHoleFile(){
//		InputStream input_stream = null;
//		Document document = null;
//		DocumentBuilderFactory doc_build_factory = DocumentBuilderFactory.newInstance();
//		DocumentBuilder doc_builder = null;
//		try{
//			doc_builder = doc_build_factory.newDocumentBuilder();
//			input_stream = new FileInputStream("/sdcard/holes/MillGreenHoles.xml");
//			document = doc_builder.parse(input_stream);
//			stepTwo(document);
//		}
//		catch(Exception ex1){
//			System.out.printn("Exception caught in the readHoleFile line 93: "+ex1);
//			AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
//	        alertbox.setMessage("Error occured reading the XML file: "+ex1);
//	        alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
//	            public void onClick(DialogInterface arg0, int arg1) {
//	            }
//	        });
//	        alertbox.show();
//		}
//	}
//	
	private void stepTwo(Document document){
		document.getDocumentElement().normalize();
		Node club_node = document.getFirstChild();
		Element club_element = (Element)club_node;
		club_id = getTagValue("Club_ID", club_element);

		NodeList golf_hole_list = document.getElementsByTagName("Hole");
		//		System.out.println("Number of holes: "+golf_hole_list.getLength());

		for(int i = 0; i < golf_hole_list.getLength(); i++){
			Node node = golf_hole_list.item(i);
			if(node.getNodeType()== Node.ELEMENT_NODE){
				Element element = (Element)node;
				Hole hole = new Hole(new GeoPoint(Double.parseDouble(getTagValue("GreenFront_Lat", element)), Double.parseDouble(getTagValue("GreenFront_Lon", element))), new GeoPoint(Double.parseDouble(getTagValue("GreenCentre_Lat", element)), Double.parseDouble(getTagValue("GreenCentre_Lon", element))), new GeoPoint(Double.parseDouble(getTagValue("GreenBack_Lat", element)), Double.parseDouble(getTagValue("GreenBack_Lon", element))), getTagValue("aerial_image", element), Integer.parseInt(getTagValue("Orientation", element)), getTagValue("aerial_image", element), getTagValue("layout_filename", element));
				tee_point = new GeoPoint(Double.parseDouble(getTagValue("Tee_Lat", element)), Double.parseDouble(getTagValue("Tee_Lon", element)));
				scrollLimitEastE6 = D2mD(Double.parseDouble(getTagValue("BottomRight_Lon", element)));
				scrollLimitNorthE6 = D2mD(Double.parseDouble(getTagValue("TopLeft_Lat", element)));
				scrollLimitWestE6 = D2mD(Double.parseDouble(getTagValue("TopLeft_Lon", element)));
				scrollLimitSouthE6 = D2mD(Double.parseDouble(getTagValue("BottomRight_Lat", element)));
				this.hole_list.add(hole);
				NodeList hazards = element.getElementsByTagName("Hazard");
				for(int j = 0; j < hazards.getLength(); j++){
					Node hazard_node = hazards.item(j);
					if(hazard_node.getNodeType()== Node.ELEMENT_NODE){
						Element hazard_element = (Element)hazard_node;
						hole.hazard_list.add(new Hazard(new GeoPoint(Double.parseDouble(getTagValue("Front_Lat", hazard_element)), Double.parseDouble(getTagValue("Front_Lon", hazard_element))), new GeoPoint(Double.parseDouble(getTagValue("Back_Lat", hazard_element)), Double.parseDouble(getTagValue("Back_Lon", hazard_element)))));
					}
				}
			}
		}
		System.out.println("Number of hazards: "+hazard_list.size());

		getHole();
		stepThree();
	}
	
	private void validateUserID(String id, String user_id){
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost("http://www.alliancerational.com/golf/uservalidate.php");
		String result = "";

		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("course_id", id));
			nameValuePairs.add(new BasicNameValuePair("user_id", user_id));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			try{
				HttpResponse response = client.execute(post);
				InputStreamReader in = new InputStreamReader(response.getEntity().getContent());
				StringBuffer xml = new StringBuffer();
				int c =0;
				while( (c = in.read()) != -1){
					xml.append((char)c);
				}
				result = xml.toString();
			}
			catch(Exception ex1){
				System.out.println("Connection Exception: "+ex1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.validation_result = result;
	}
	
	GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) {
			if( event == GpsStatus.GPS_EVENT_FIRST_FIX){
				System.out.println("GPS first fix");
				location_segment_number = 250;
			}
		}
	};
	
	public class GeoUpdateHandler implements LocationListener {
		public void onLocationChanged(Location location) {

			System.out.println("XXX GeoUpdateHandler called: "+location.getLatitude()+", "+location.getLongitude());
			 user_location = new GeoPoint(location.getLatitude(), location.getLongitude());
			 mapView.invalidate();
		}

		public void onProviderDisabled(String provider) {
			System.out.println("Provider disabled");
		}

		public void onProviderEnabled(String provider) {
			System.out.println("Provider enabled");
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	private static String getTagValue(String sTag, Element eElement) {
//		System.out.println("Sent Tag: "+sTag);
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = (Node) nlList.item(0);
		String value = "0";
		try{
			value = nValue.getNodeValue();
		}
		catch(Exception ex1){
			System.out.println("Node value is null");
		}
		return value;
	}

	private double mD2D(int microDegrees) {
		return microDegrees / 1E6;
	}

	private int D2mD(double Degrees) {
		return (int)(Degrees * 1E6);
	}
}