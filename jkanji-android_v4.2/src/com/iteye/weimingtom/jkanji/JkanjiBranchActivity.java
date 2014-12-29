package com.iteye.weimingtom.jkanji;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class JkanjiBranchActivity extends Activity {
	private ActionBar actionBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.branch_version);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("扩展应用程序");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.yunohidamari;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
	}
}
