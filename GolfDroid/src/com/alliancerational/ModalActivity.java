package com.alliancerational;

import android.app.Activity;
import android.os.Bundle;

public class ModalActivity extends Activity  {
	String retrievedData;
	static int RESULTCODE = 2;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hole_layout);
		System.out.println("MEssage from the modal activity.");
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null) {
			retrievedData = bundle.getString("name");
		}
	}
}
