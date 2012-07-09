package com.alliancerational;

import org.osmdroid.util.GeoPoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.alliancerational.parser.DocUtils;

import java.util.ArrayList;

public class GolfClub {
	private String club_id;
	private ArrayList<Hole> hole_list = new ArrayList<Hole>();
	Hole selected_hole;
	GolfClub(Document document){
		document.getDocumentElement().normalize();
		Node club_node = document.getFirstChild();
		Element club_element = (Element)club_node;
		club_id = DocUtils.getTagValueString("Club_ID", club_element);
		NodeList golf_hole_list = document.getElementsByTagName("Hole");
		for(int i = 0; i < golf_hole_list.getLength(); i++){
			Node node = golf_hole_list.item(i);
			if(node.getNodeType()== Node.ELEMENT_NODE){
				Element hole_element = (Element)node;
				Hole hole = new Hole(new GeoPoint(DocUtils.getDouble("GreenFront_Lat", hole_element),DocUtils.getDouble("GreenFront_Lon", hole_element)), new GeoPoint(DocUtils.getDouble("GreenCentre_Lat", hole_element),DocUtils.getDouble("GreenCentre_Lon", hole_element)), new GeoPoint(DocUtils.getDouble("GreenBack_Lat", hole_element),DocUtils.getDouble("GreenBack_Lon", hole_element)), DocUtils.getTagValueString("aerial_image", hole_element), DocUtils.getTagValueInteger("Orientation", hole_element), DocUtils.getTagValueString("aerial_image", hole_element), DocUtils.getTagValueString("layout_filename", hole_element));
				hole.setTee_point(new GeoPoint(DocUtils.getDouble("Tee_Lat", hole_element),DocUtils.getDouble("Tee_Lon", hole_element)));

/*				scrollLimitEastE6 = D2mD(Double.parseDouble(getTagValue("BottomRight_Lon", hole_element)));
				scrollLimitNorthE6 = D2mD(Double.parseDouble(getTagValue("TopLeft_Lat", hole_element)));
				scrollLimitWestE6 = D2mD(Double.parseDouble(getTagValue("TopLeft_Lon", hole_element)));
				scrollLimitSouthE6 = D2mD(Double.parseDouble(getTagValue("BottomRight_Lat", hole_element)));*/
				this.hole_list.add(hole);
				NodeList hazards = hole_element.getElementsByTagName("Hazard");
				for(int j = 0; j < hazards.getLength(); j++){
					Node hazard_node = hazards.item(j);
					if(hazard_node.getNodeType()== Node.ELEMENT_NODE){
						Element hazard_element = (Element)hazard_node;
						hole.hazard_list.add(new Hazard(new GeoPoint(DocUtils.getDouble("Front_Lat", hazard_element), DocUtils.getDouble("Front_Lon", hazard_element)), new GeoPoint(DocUtils.getDouble("Back_Lat", hazard_element), DocUtils.getDouble("Back_Lon", hazard_element))));
					}
				}
			}
		}
	}

	public ArrayList<Hole> getHoleList(){
		return this.hole_list;
	}
	
	public void setSelectedHole(int number){
		this.selected_hole = this.hole_list.get(number);
	}
	
	public Hole getSelectedHole(){
		return selected_hole;
	}
}
