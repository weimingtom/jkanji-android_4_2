package com.iteye.weimingtom.jkanji;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.util.EncodingUtils;

import com.elgubbo.sharetoclipboard.ShareToClipboardActivity;
import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;
import com.petebevin.markdown.MarkdownProcessor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class JkanjiMarkdownActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "JkanjiMarkdownActivity";
	
	public final static String FILENAME_KEY = "com.iteye.weimingtom.jkanji.JkanjiMarkdownActivity.filename";
	public final static String TITLE_KEY = "com.iteye.weimingtom.jkanji.JkanjiMarkdownActivity.title";
	
	private WebView webView1;
	private ActionBar actionBar;
	
	private String filename;
	
	private final static String HEAD_STR = 
			"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
			"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" + 
			"<head>" +
			"<title>001.txt</title>" +
			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />" +
			"</head>" +
			"<body>" +
			"";
	private final static String FOOT_STR = 
			"</body>" +
			"</html>" +
			"";	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.webview);
        
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("Markdown网页查看器");
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
				startActivity(new Intent(JkanjiMarkdownActivity.this, 
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
				startActivity(new Intent(JkanjiMarkdownActivity.this, 
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
				startActivity(new Intent(JkanjiMarkdownActivity.this, 
						JKanjiActivity.class));
			}
        });
        
        webView1 = (WebView) findViewById(R.id.webView1);
        webView1.setVisibility(WebView.INVISIBLE);
        webView1.setNetworkAvailable(false);
        webView1.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView1.getSettings().setUseWideViewPort(true);
        webView1.getSettings().setJavaScriptEnabled(true);
        webView1.getSettings().setSupportZoom(true);
        webView1.getSettings().setBuiltInZoomControls(true);
        webView1.getSettings().setLightTouchEnabled(true);
    	webView1.setWebViewClient(new WebViewClient() {
            @Override
    		public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            
    		@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				if (D) {
					Log.d(TAG, "onPageStarted");
				}
				webView1.setVisibility(WebView.INVISIBLE);
			}
			
			@Override
		    public void onPageFinished(WebView view, String url){
		    	super.onPageFinished(view, url);
				if (D) {
					Log.d(TAG, "onPageFinished");
				}
				webView1.setVisibility(WebView.VISIBLE);
			}
		});

		Intent intent = this.getIntent();
		if (intent != null) {
			String title = intent.getStringExtra(TITLE_KEY);
			if (title != null) {
				actionBar.setTitle(title);
			}
	    	filename = intent.getStringExtra(FILENAME_KEY);
		}
		
		webView1.postDelayed(new Runnable() {
			@Override
			public void run() {
				beginLoad();
			}
    	}, 100);
    }
    
    private void beginLoad() {
		//System.out.println(this.getIntent().getStringExtra(JKanjiActivity.EXTRA_KEY));

        //String filename = "markdown/book001/001.txt";
        if (filename != null) {

			String strHead;
			String strFoot;
			
			if (true) {
				strHead = getStringFromAsset2("markdown/markdown_head.txt"); 
				strFoot = getStringFromAsset2("markdown/markdown_foot.txt"); 
			} else {
				strHead = HEAD_STR;
				strFoot = FOOT_STR;
			}
			String str = getStringFromAsset2(filename);
//			if (D) {
//				Log.d(TAG, str);
//			}
	        String output = new MarkdownProcessor().markdown(str);
	        StringBuffer sb = new StringBuffer();
	        sb.append(strHead);
	        sb.append(output);
	        sb.append(strFoot);
			if (D) {
				Log.d(TAG, sb.toString());
			}
	        webView1.getSettings().setDefaultTextEncodingName("utf-8");
	        //webView1.loadData(sb.toString(), "text/html", "utf-8");
	        webView1.loadDataWithBaseURL(null, sb.toString(), "text/html", "utf-8", null);
	        //webView1.loadData(getStringFromAsset3("digust_1.html"), "text/html", "utf-8");
        }
    }
    
    private String getStringFromAsset2(String filename) {
    	StringBuffer sb = new StringBuffer();
		InputStream istr = null;
		InputStreamReader reader = null;
		BufferedReader rbuf = null;
		try {
			istr = this.getAssets().open(filename);
			reader = new InputStreamReader(istr, "UTF-8");
			rbuf = new BufferedReader(reader);
			String line;
			while (null != (line = rbuf.readLine())) {
				sb.append(line);
				sb.append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (rbuf != null) {
				try {
					rbuf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (istr != null) {
				try {
					istr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
    }
    
    /**
     * @see http://code.google.com/p/android/issues/detail?id=3552
     * @param filename
     * @return
     */
    private String getStringFromAsset3(String filename) {
        final int MAX_HTML_SIZE = 1024 * 100;
    	byte[] buff = new byte[MAX_HTML_SIZE];
        int len;
        String rawText;
        InputStream is = null;
        try {
        	is = this.getAssets().open(filename);
            len = is.read(buff, 0, MAX_HTML_SIZE);
            rawText = EncodingUtils.getString(buff, 0, len, "utf-8");
        } catch (Exception e) {
            rawText = "";
        } finally {
        	if (is != null) {
        		try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
    	return rawText;
    }
}
