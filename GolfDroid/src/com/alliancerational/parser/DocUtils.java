package com.alliancerational.parser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class DocUtils {
	public static String getTagValueString(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = (Node)nlList.item(0);
		String value = "0";
		try{
			value = nValue.getNodeValue();
		}
		catch(Exception ex1){
			System.out.println("Exception at: com.alliancerational.parser.DocUtils(13): "+ex1);
		}
		return value;
	}
	public static int getTagValueInteger(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = (Node)nlList.item(0);
		int value = 0;
		try{
			value = Integer.parseInt(nValue.getNodeValue());
		}
		catch(Exception ex1){
			System.out.println("Exception at: com.alliancerational.parser.DocUtils(25): "+ex1);
		}
		return value;
	}
	public static double getDouble(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = (Node)nlList.item(0);
		double value = 0;
		try{
			value = Double.parseDouble(nValue.getNodeValue());
		}
		catch(Exception ex1){
			System.out.println("Exception at: com.alliancerational.parser.DocUtils(37): "+ex1);
		}
		return value;
	}
}
