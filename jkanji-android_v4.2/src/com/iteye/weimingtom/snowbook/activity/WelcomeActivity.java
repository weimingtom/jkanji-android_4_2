package com.iteye.weimingtom.snowbook.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.fontawesome.example.TextAwesome;
import com.iteye.weimingtom.jkanji.JkanjiMainMenuActivity;
import com.iteye.weimingtom.jkanji.JkanjiSettingActivity;
import com.iteye.weimingtom.jkanji.R;

public class WelcomeActivity extends Activity {
	private Button btnNewVersion;
	private Button btnOldVersion;
	private Button btnCheckShow;
	private TextAwesome textAwesomeCheck;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!JkanjiSettingActivity.getShowSplashScreen(this)) {
			this.setContentView(R.layout.snowbook_welcome_empty);
			//this.setVisible(false);
			if (!JkanjiSettingActivity.getJumpOldVersion(this)) {
				startActivity(new Intent(WelcomeActivity.this, 
						MainActivity.class));
			} else {
				startActivity(new Intent(WelcomeActivity.this, 
						JkanjiMainMenuActivity.class));
			}
			this.finish();
			return;
		}
		
		this.setContentView(R.layout.snowbook_welcome);
		
		btnNewVersion = (Button) this.findViewById(R.id.btnNewVersion);
		btnOldVersion = (Button) this.findViewById(R.id.btnOldVersion);
		btnCheckShow = (Button) this.findViewById(R.id.btnCheckShow);
		textAwesomeCheck = (TextAwesome) this.findViewById(R.id.textAwesomeCheck);
		
		btnNewVersion.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!JkanjiSettingActivity.getShowSplashScreen(WelcomeActivity.this)) {
					JkanjiSettingActivity.setJumpOldVersion(
						WelcomeActivity.this, false);
				}
				startActivity(new Intent(WelcomeActivity.this, 
						MainActivity.class));
				finish();
			}
		});
		btnOldVersion.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!JkanjiSettingActivity.getShowSplashScreen(WelcomeActivity.this)) {
					JkanjiSettingActivity.setJumpOldVersion(
						WelcomeActivity.this, true);
				}
				startActivity(new Intent(WelcomeActivity.this, 
						JkanjiMainMenuActivity.class));
				finish();
			}
		});
		btnCheckShow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (textAwesomeCheck.getText().toString().equals(getString(R.string.fa_square))) {
					textAwesomeCheck.setText(R.string.fa_check_square);
					JkanjiSettingActivity.setShowSplashScreen(
						WelcomeActivity.this, false);
				} else {
					textAwesomeCheck.setText(R.string.fa_square);
					JkanjiSettingActivity.setShowSplashScreen(
							WelcomeActivity.this, true);
				}
			}
		});
	}
}
