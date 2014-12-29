package com.iteye.weimingtom.jkanji;

import com.elgubbo.sharetoclipboard.ShareToClipboardActivity;
import com.iteye.weimingtom.jkanji.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class BubbleActivity extends Activity {
	private Button buttonSearch, buttonMemo, buttonSen, buttonCancel;
	private String keyword;
	private String subject;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.bubble);
		
		Intent intent = this.getIntent();
		if (intent != null && intent.getAction() != null && 
			intent.getAction().equals(Intent.ACTION_SEND)) {
			Bundle extras = intent.getExtras();
			if (extras.containsKey(Intent.EXTRA_SUBJECT)) {
				subject = extras.getString(Intent.EXTRA_SUBJECT);
			}
			if (extras.containsKey(Intent.EXTRA_TEXT)) {
				keyword = extras.getString(Intent.EXTRA_TEXT);
			}

		}
		if (subject == null) {
			subject = "";
		}
		if (keyword == null) {
			keyword = "";
		}
		
		buttonSearch = (Button) this.findViewById(R.id.buttonSearch);
		buttonMemo = (Button) this.findViewById(R.id.buttonMemo);
		buttonSen = (Button) this.findViewById(R.id.buttonSen);
		buttonCancel = (Button) this.findViewById(R.id.buttonCancel);
		
		buttonSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(BubbleActivity.this, JKanjiActivity.class);
				intent.putExtra(JKanjiActivity.EXTRA_KEY_SHEARCHTEXT, keyword);
				startActivity(intent);
				finish();
			}
		});
		buttonMemo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(BubbleActivity.this, ShareToClipboardActivity.class);
				intent.setAction(ShareToClipboardActivity.ACTION_SEND_CLIP);
				intent.setType("text/plain");
		        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		        intent.putExtra(Intent.EXTRA_TEXT, keyword);
				startActivity(intent);
				finish();
			}
		});
		buttonSen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(BubbleActivity.this, JkanjiSenActivity.class);
				intent.putExtra(JkanjiSenActivity.INPUT_KEY, keyword);
				startActivity(intent);
				finish();
			}
		});
		buttonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
