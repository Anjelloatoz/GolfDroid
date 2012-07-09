package com.alliancerational;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;

public class ModalActivity extends Activity implements
RadioGroup.OnCheckedChangeListener {
	ArrayList<String> options;
	static int RESULTCODE = 2;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Creating a new RelativeLayout
		RelativeLayout relativeLayout = new RelativeLayout(this);
		RelativeLayout.LayoutParams relative_layout_params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		
		TextView text_view = new TextView(this);
        text_view.setText("Test");
        
		if(getIntent().getStringArrayListExtra("options") !=null)
		{
			options = getIntent().getStringArrayListExtra("options");
//			System.out.println("Size of options: "+options.size());
		}
		

        final RadioButton[] rb = new RadioButton[options.size()];
		final RadioGroup rg = new RadioGroup(this);
		rg.setOrientation(RadioGroup.VERTICAL);

		for(int i=0; i<options.size(); i++){
			rb[i]  = new RadioButton(this);
			rg.addView(rb[i]);
			rb[i].setText(options.get(i));
		}
		rg.setOnCheckedChangeListener(this);
        
/*     // Defining the layout parameters of the TextView
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);

        // Setting the parameters on the TextView
        text_view.setLayoutParams(lp);
*/
     // Adding the TextView to the RelativeLayout as a child
        relativeLayout.addView(rg);

        // Setting the RelativeLayout as our content view
        setContentView(relativeLayout, relative_layout_params);
	}
	
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		System.out.println("Radio Group check changed.");
		int rbid = group.getCheckedRadioButtonId();
		View rb = group.findViewById(rbid);
		int idx = group.indexOfChild(rb);

		System.out.println("Selected hole is: "+idx);
		Bundle b = new Bundle();
		  b.putString("Value", "Successfully returned");
		  Intent intent = new Intent();
		  intent.putExtras(b);
		  intent.putExtra("selection", idx);
		  setResult(RESULTCODE, intent);
		  finish();
	}
}
