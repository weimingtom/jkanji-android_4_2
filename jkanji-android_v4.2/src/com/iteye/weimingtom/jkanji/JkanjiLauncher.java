package com.iteye.weimingtom.jkanji;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

public class JkanjiLauncher extends Activity implements LauncherView.OnSelectIconListener{
	public final static boolean D = false;
	public final static String TAG = "JkanjiLauncher";
	
	private ActionBar actionBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.launcher);
		
		LauncherView launcher = (LauncherView) this.findViewById(R.id.launcher);
		launcher.setOnSelectIconListener(this);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("旧版主菜单（2.x）");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				return R.drawable.icon_actionbar_v1;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		System.gc();
	}

	@Override
	protected void onStop() {
		super.onStop();
		System.gc();
		if (D) {
			Log.e(TAG, "onStop");
		}
	}

	@Override
	public void onSelectIcon(int index) {
		switch (index) {
		case 0:
			startActivity(new Intent(this, JKanjiActivity.class));
			break;
		
		case 1:
			startActivity(new Intent(this, JkanjiViewerActivity.class));
			break;
			
		case 2:
			startActivity(new Intent(this, JkanjiBookIndex.class));
			break;
			
		case 3:
			startActivity(new Intent(this, AboutActivity.class));
			break;
		}
	}
}
