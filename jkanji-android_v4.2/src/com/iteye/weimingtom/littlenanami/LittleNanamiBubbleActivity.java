package com.iteye.weimingtom.littlenanami;

import com.iteye.weimingtom.jkanji.JKanjiActivity;
import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.jkanji.SQLiteReaderActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class LittleNanamiBubbleActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "LittleNanamiBubbleActivity";
	
	public final static String EXTRA_BUBBLE_SCENARIO = "com.iteye.weimingtom.littlenanami.LittleNanamiBubbleActivity.EXTRA_BUBBLE_SCENARIO";
	public final static String EXTRA_BUBBLE_WIDGETID = "com.iteye.weimingtom.littlenanami.LittleNanamiBubbleActivity.EXTRA_BUBBLE_WIDGETID";
	public final static String EXTRA_BUBBLE_LEFT = "com.iteye.weimingtom.littlenanami.LittleNanamiBubbleActivity.EXTRA_BUBBLE_LEFT";
	public final static String EXTRA_BUBBLE_TOP = "com.iteye.weimingtom.littlenanami.LittleNanamiBubbleActivity.EXTRA_BUBBLE_TOP";
	public final static String EXTRA_BUBBLE_RIGHT = "com.iteye.weimingtom.littlenanami.LittleNanamiBubbleActivity.EXTRA_BUBBLE_RIGHT";
	public final static String EXTRA_BUBBLE_BOTTOM = "com.iteye.weimingtom.littlenanami.LittleNanamiBubbleActivity.EXTRA_BUBBLE_BOTTOM";
	
	private RelativeLayout relativeLayoutBubble;
	private Button buttonSearch, buttonExitBubble;
	private ImageView imageView1;
	
	private int widgetId;
	private int scenario;
	private int widgetLeft, widgetRight, widgetTop, widgetBottom;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.widget_nanami_bubble_layout);
		imageView1 = (ImageView) this.findViewById(R.id.imageView1);
		relativeLayoutBubble = (RelativeLayout) this.findViewById(R.id.relativeLayoutBubble);
		buttonExitBubble = (Button) this.findViewById(R.id.buttonExitBubble);
		buttonSearch = (Button) this.findViewById(R.id.buttonSearch);
		
		imageView1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				nextImage();
			}
		});
		relativeLayoutBubble.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		buttonSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LittleNanamiBubbleActivity.this, JKanjiActivity.class);
				//intent.putExtra(JKanjiActivity.EXTRA_KEY_SHEARCHTEXT, keyword);
				startActivity(intent);
				finish();
			}
		});
		buttonSearch.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Intent intent = new Intent(LittleNanamiBubbleActivity.this, SQLiteReaderActivity.class);
				startActivity(intent);
				return true;
			}
		});
		buttonExitBubble.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		Intent intent = this.getIntent();
		if (intent != null) {
			String action = intent.getAction();
	        if (action != null && Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
	            Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
	            shortcutIntent.setClass(this, this.getClass());
	            Intent intentShortCutResult = new Intent();
	            intentShortCutResult.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
	            intentShortCutResult.putExtra(Intent.EXTRA_SHORTCUT_NAME, this.getText(R.string.shortcut_name));
	            Parcelable shortIcon = Intent.ShortcutIconResource.fromContext(
	                    this, R.drawable.ic_launcher);
	            intentShortCutResult.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortIcon);
	            setResult(RESULT_OK, intentShortCutResult);
	            finish();
	            return;
	        }
			widgetId = intent.getIntExtra(EXTRA_BUBBLE_WIDGETID, -1);
			scenario = intent.getIntExtra(EXTRA_BUBBLE_SCENARIO, CommonSettings.DEFAULT_SCENARIO);
			widgetLeft = intent.getIntExtra(EXTRA_BUBBLE_LEFT, 0);
			widgetRight = intent.getIntExtra(EXTRA_BUBBLE_RIGHT, 0);
			widgetTop = intent.getIntExtra(EXTRA_BUBBLE_TOP, 0);
			widgetBottom = intent.getIntExtra(EXTRA_BUBBLE_BOTTOM, 0);
			imageView1.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					if (widgetId >= 0) {
						int[] loc = new int[2];
						relativeLayoutBubble.getLocationInWindow(loc);
						if (D) {
							Log.e(TAG, "loc[0] == " + loc[0] + ", loc[1] == " + loc[1]);
						}
						RelativeLayout.LayoutParams lp;
						lp = new RelativeLayout.LayoutParams(widgetRight - widgetLeft, widgetBottom - widgetTop);
						lp.setMargins(widgetLeft - loc[0], widgetTop - loc[1], 0, 0);
						imageView1.setLayoutParams(lp);
						//new RelativeLayout.MarginLayoutParams
						//new RelativeLayout.LayoutParams(
						//(widgetRight - widgetLeft, widgetBottom - widgetTop)
					}
				}
			});
		} else {
			widgetId = -1;
			scenario = CommonSettings.DEFAULT_SCENARIO;
		}
		setCurrentImage();
	}

	private void setCurrentImage() {
		if (scenario >= 0 && scenario < CommonSettings.SCENARIO_TABLE.length) {
			imageView1.setImageResource(CommonSettings.SCENARIO_TABLE[scenario]);
			Animation ani = AnimationUtils.loadAnimation(this, R.anim.bubble_fade);
			imageView1.startAnimation(ani);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		this.finish();
	}
	
	private void nextImage() {
		scenario++;
		if (scenario >= CommonSettings.SCENARIO_TABLE.length) {
			scenario = 0;
		}
		setCurrentImage();
		if (widgetId >= 0) {
			this.startService(new Intent(this, LittleNanamiService.class)
				.setAction(LittleNanamiService.ACTION_CHANGE)
				.putExtra(LittleNanamiService.EXTRA_WIDGET_ID, widgetId)
				.putExtra(LittleNanamiService.EXTRA_SCENARID, scenario)
				.putExtra(LittleNanamiService.EXTRA_VISIBLE, false)
			);
		} else {
			//finish();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (widgetId >= 0) {
			this.startService(new Intent(this, LittleNanamiService.class)
				.setAction(LittleNanamiService.ACTION_CHANGE)
				.putExtra(LittleNanamiService.EXTRA_WIDGET_ID, widgetId)
				.putExtra(LittleNanamiService.EXTRA_SCENARID, scenario)
				.putExtra(LittleNanamiService.EXTRA_VISIBLE, true)
			);
		}
	}
}
