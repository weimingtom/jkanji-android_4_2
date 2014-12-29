package com.iteye.weimingtom.jkanji;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class AboutGamesActivity extends Activity {
	private ActionBar actionBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.about_games);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("jkanji-games");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.help;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
		
		Button download1 = (Button)this.findViewById(R.id.download1);
		download1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openURL("http://wmtwp.sturgeon.mopaas.com/");
			}
		});
		Button download2 = (Button)this.findViewById(R.id.download2);
		download2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openURL("http://wmtwordpress.jd-app.com/");
			}
		});
		Button back = (Button)this.findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void openURL(String url) {
		Intent intent;
		intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		try {
			startActivity(intent);
		} catch (Throwable e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(),
				"找不到可用的应用程序", Toast.LENGTH_SHORT)
				.show();
		}
	}
}
