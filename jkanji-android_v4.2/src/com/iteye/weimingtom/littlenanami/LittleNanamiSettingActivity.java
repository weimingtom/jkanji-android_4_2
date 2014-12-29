package com.iteye.weimingtom.littlenanami;

import com.iteye.weimingtom.jkanji.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class LittleNanamiSettingActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "LittleCatSettingActivity";
	
	private ImageButton button1;
	private ImageButton button2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.widget_nanami_settings);
		if (D) {
			Log.i(TAG, "Settings: start settings Activity");
		}
		button1 = (ImageButton) findViewById(R.id.imageButton1);
		button2 = (ImageButton) findViewById(R.id.imageButton2);
		button1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeScenario(0);
			}
		});
		button2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeScenario(1);
			}
		});
	}
	
	public void changeScenario(int scenario) {
		if (D) {
			Log.i(TAG, "Settings: Change Scenario -> " + scenario);
		}
		this.startService(new Intent(this, LittleNanamiService.class)
			.setAction(LittleNanamiService.ACTION_CHANGE)
			.putExtra(LittleNanamiService.EXTRA_SCENARID, scenario)
		);
		this.finish();
	}
}
