package com.iteye.weimingtom.jkanji;

import java.io.IOException;
import java.io.InputStream;

import com.elgubbo.sharetoclipboard.ShareToClipboardActivity;
import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebViewActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "WebViewActivity";
	
	public final static String FILENAME_KEY = "com.iteye.weimingtom.jkanji.WebViewActivity.filename";
	private final static boolean USE_ASSETS_URL = true;
	
	private ActionBar actionBar;
	private WebView wv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.webview);

		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("网页查看器");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
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
				return R.drawable.memo;
			}

			@Override
			public void performAction(View view) {
				startActivity(new Intent(WebViewActivity.this, 
						ShareToClipboardActivity.class));
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.search_sqlite;
			}

			@Override
			public void performAction(View view) {
				startActivity(new Intent(WebViewActivity.this, 
					SQLiteReaderActivity.class));
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.search;
			}

			@Override
			public void performAction(View view) {
				startActivity(new Intent(WebViewActivity.this, 
						JKanjiActivity.class));
			}
        });
        
        wv = (WebView) findViewById(R.id.webView1);
        wv.setNetworkAvailable(false);
        wv.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        //wv.getSettings().setDisplayZoomControls(true);
        wv.getSettings().setUseWideViewPort(true);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setSupportZoom(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setLightTouchEnabled(true);
        
		//System.out.println(this.getIntent().getStringExtra(JKanjiActivity.EXTRA_KEY));
		String filename = this.getIntent().getStringExtra(FILENAME_KEY);
		if (filename != null) {
			for (int i = 0; i < DataContext.DIGUST_FILE_NAMES.length; i++) {
				if (filename.equals(DataContext.DIGUST_FILE_NAMES[i])) {
					this.setTitle(DataContext.DIGUST_TITLES[i]);
					actionBar.setTitle(DataContext.DIGUST_TITLES[i]);
				}
			}
			
			wv.setWebViewClient(new WebViewClient() {
	            @Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
	                if (url != null && url.startsWith("http")) {
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
	                	return true;
	                } else {
	                	view.loadUrl(url);
	                	return true;
	                }
	            }
				
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					if (D) {
						Log.d(TAG, "onPageStarted");
					}
					wv.setVisibility(WebView.INVISIBLE);
				}
				
				@Override
			    public void onPageFinished(WebView view, String url){
			    	super.onPageFinished(view, url);
					if (D) {
						Log.d(TAG, "onPageFinished");
					}
					wv.setVisibility(WebView.VISIBLE);
				}
			});
			
			if (USE_ASSETS_URL) {
				wv.loadUrl("file:///android_asset/" + filename);
			} else {
				InputStream ins = null;
				try {
					ins = this.getAssets().open(filename);
					byte[] buffer = new byte[ins.available()];
					ins.read(buffer);
					String str = new String(buffer, "UTF-8");
					wv.getSettings().setDefaultTextEncodingName("utf-8");
					wv.loadData(str, "text/html", "utf-8");
					
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (ins != null) {
						try {
							ins.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						ins = null;
					}
				}
			}
		}
	}
}
