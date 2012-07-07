package com.alliancerational.android.FileIO;

import java.io.FileInputStream;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import android.content.Context;
import org.w3c.dom.Document;
import android.app.AlertDialog;
import android.content.DialogInterface;

public abstract class LocalFileReader {
	public static Document readFile(String file_name, Context context){
		InputStream input_stream = null;
		Document document = null;
		DocumentBuilderFactory doc_build_factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder doc_builder = null;
		try{
			doc_builder = doc_build_factory.newDocumentBuilder();
			input_stream = new FileInputStream(file_name);
			document = doc_builder.parse(input_stream);
			return document;
		}
		catch(Exception ex1){
			System.out.println("Exception at com.alliancerational.android.FileIO.LocalFileReader: "+ex1);
			if(context!=null){
				AlertDialog.Builder alertbox = new AlertDialog.Builder(context);
				alertbox.setMessage("Error occured reading:\n"+file_name+"\n:"+ex1);
				alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
					}
				});
				alertbox.show();
			}
			return null;
		}
	}
}
