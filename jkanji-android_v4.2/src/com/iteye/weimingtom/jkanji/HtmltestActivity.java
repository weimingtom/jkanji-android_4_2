package com.iteye.weimingtom.jkanji;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

public class HtmltestActivity extends Activity {
    private final static int PATH_MENU_ID = 1;
	private final static int FRESH_MENU_ID = 2;
	
    private static final int DIALOG_TEXT_ENTRY = 1;
    
    private static final String SHARE_PREF_NAME = "pref";
	private static final String SHARE_PREF_HTML_TEST_URL = "htmlTestURL";
    
	private static final String DEFAULT_PATH = "file:///mnt/sdcard/index.html";
	
	private ActionBar actionBar;
	private WebView webView1;
	private AlertDialog.Builder builder;
	private AlertDialog pathDialog;
	private EditText editPath;
	private Button buttonDefault;
	private Button buttonPaste;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.html_test);
        
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("本地HTML测试器");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				return R.drawable.print;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				return R.drawable.config;
			}

			@Override
			public void performAction(View view) {
				HtmltestActivity.this.openOptionsMenu();
			}
        });
        
        webView1 = (WebView) this.findViewById(R.id.webView1);
        webView1.setNetworkAvailable(false);
        webView1.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView1.getSettings().setUseWideViewPort(true);
        webView1.getSettings().setJavaScriptEnabled(true);
        webView1.getSettings().setSupportZoom(true);
        webView1.getSettings().setBuiltInZoomControls(true);
        webView1.getSettings().setLightTouchEnabled(true);
        webView1.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        builder = new AlertDialog.Builder(this);
        reload();
    }

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_TEXT_ENTRY:
			LayoutInflater factory = LayoutInflater.from(this);
            View textEntryView = factory.inflate(R.layout.html_test_dialog, null);
            pathDialog = builder
            	.setTitle("指定URL")
                .setView(textEntryView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	setLastHtmlTestURL(editPath.getText().toString());
                    	reload();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	
                    }
                })
                .create();
            editPath = (EditText) textEntryView.findViewById(R.id.editPath);
            editPath.setText("");
            editPath.append(getLastHtmlTestURL());
            buttonDefault = (Button) textEntryView.findViewById(R.id.buttonDefault);
            buttonDefault.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					editPath.setText("");
					editPath.append(DEFAULT_PATH);
				}
            });
            buttonPaste = (Button) textEntryView.findViewById(R.id.buttonPaste);
            buttonPaste.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					CharSequence text = cm.getText();
					if (text != null) {
						editPath.setText("");
						editPath.append(text);
					}
				}
            });
            return pathDialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case DIALOG_TEXT_ENTRY:
			if (editPath != null) {
				editPath.setText("");
				editPath.append(getLastHtmlTestURL());
			}
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, PATH_MENU_ID, 0, "指定URL");
		menu.add(0, FRESH_MENU_ID, 0, "刷新");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case PATH_MENU_ID:
			this.showDialog(PATH_MENU_ID);
			break;
			
		case FRESH_MENU_ID:
			reload();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
    private void reload() {
    	webView1.clearView();
    	webView1.clearSslPreferences();
    	webView1.clearFormData();
    	webView1.clearHistory();
		webView1.loadUrl(getLastHtmlTestURL());
    }
	
    
    
    @Override
	public void onBackPressed() {
    	if (webView1.canGoBack()) {
    		webView1.goBack();
    	} else {
    		super.onBackPressed();
    	}
	}

	private void setLastHtmlTestURL(String str) {
		PrefUtil.putString(this, SHARE_PREF_NAME, 
				SHARE_PREF_HTML_TEST_URL, 
				str);
    }
    
    private String getLastHtmlTestURL() {
		return PrefUtil.getString(this, SHARE_PREF_NAME, 
				SHARE_PREF_HTML_TEST_URL, 
				DEFAULT_PATH);
    }
}
