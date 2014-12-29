package com.iteye.weimingtom.jkanji;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import net.java.sen.StringTagger;

import fi.harism.curl.BookInfoUtils;
import fi.harism.curl.CurlActivity;
import fi.harism.curl.CurlLandActivity;
import fi.harism.curl.CurlPortActivity;
import fi.harism.curl.ViewPagerLandActivity;
import fi.harism.curl.ViewPagerPortActivity;
import fi.harism.curl.ViewPagerActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class JkanjiAozoraReaderActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "JkanjiAozoraReaderActivity";

	private static final boolean USE_ASYNCTASK = true;
	
	private ActionBar actionBar;
	
	private Button buttonShareReaderSetting;
	
	private EditText editTextFilename;
	private Button buttonBrowseTxt;
	private Button buttonREBrowseTxt;
	private Button buttonOpen;
	private Button buttonOpenSample;
	private Button buttonBrowseEpub;
	private Button buttonSaveSetting;
	private Button buttonFavSet;
	
	private EditText editTextBGFilename;
	private Button buttonBrowseJPG;
	private Button buttonBrowsePNG;
	private Button buttonREBrowseJPG;
	private Button buttonBrowseBGClear;
	
	private RadioButton radioButtonNotFullScreen;
	private RadioButton radioButtonFullScreen;
	
	private RadioButton radioButtonActionbar;
	private RadioButton radioButtonNoActionbar;	
	
	private RadioButton radioButtonCurl;
	private RadioButton radioButtonSimple;
	private RadioButton radioButtonCurlSimple;
	private RadioButton radioButtonViewPager;
	
	private RadioButton radioButtonShiftJIS;
	private RadioButton radioButtonUTF8;
	private RadioButton radioButtonGBK;
	private RadioButton radioButtonUnicode;
	
	private RadioButton radioButtonNotSen;
	private RadioButton radioButtonSen;
	
	private RadioButton radioButtonNotReverseDirection;
	private RadioButton radioButtonReverseDirection;
	
	private RadioButton radioButtonParserAozora;
	private RadioButton radioButtonParserPlain;
	
	private RadioButton radioButtonNotIsVertical;
	private RadioButton radioButtonIsVertical;
	
	private RadioButton radioButtonMaskBG;
	private RadioButton radioButtonNoMaskBG;

	private RadioButton radioButtonBasePage;
	private RadioButton radioButtonBasePosition;

	private RadioButton radioButtonScreenOrientationSys;
	private RadioButton radioButtonScreenOrientationLand;
	private RadioButton radioButtonScreenOrientationPort;
	
	private RadioButton radioButtonNotBlackBack;
	private RadioButton radioButtonBlackBack;
	
	private RadioButton radioButtonNotUseVolumeKey;
	private RadioButton radioButtonUseVolumeKey;
	
	private EditText editTextPage;
	private Button buttonClearPage;

	private EditText editTextPosition;
	private Button buttonClearPosition;
	
	private Button buttonSetPage, buttonSetPos;
	private EditText editPage, editPos;
	private AlertDialog pageDialog, posDialog;
	
	private ScrollView scrollView1;
	private TextView textViewLoading;
	
	private Button buttonTestSen;
	
	private static final int DIALOG_WARNING_ID = 0;
	private static final int DIALOG_LOADING_ID = 1;
	private static final int DIALOG_NOTSAVE_ID = 3;
	private static final int DIALOG_SETPAGE_ID = 4;
	private static final int DIALOG_SETPOS_ID = 5;
	
	private AlertDialog.Builder builder1, builder2, builder3, builder4, builder5;
	
	private static final int REQUEST_PATH = 1;
	private static final int REQUEST_JPG_PATH = 2;
	private static final int REQUEST_PNG_PATH = 3;
	private static final int REQUEST_RE_PATH = 4;
	private static final int REQUEST_RE_JPG_PATH = 5;
	
	//sen
	private final static String[] FILENAMES = {
		"/sen/conf/sen.xml",
		"/sen/conf/sen-processor.xml",
		"/sen/dic/da.sen",
		"/sen/dic/matrix.sen",
		"/sen/dic/posInfo.sen",
		"/sen/dic/token.sen",
	};
	private final static String SEN_HOME = "/sen";
	private final static String TEST_TEXT2 = "なにか日本語を入力して試そう。";
	
	private final static String IS_SAVED = "isSaved";
	private boolean mIsSaved = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.aozora_reader);
		
		if (savedInstanceState != null) {
			mIsSaved = savedInstanceState.getBoolean(IS_SAVED, false);
        } else {
        	mIsSaved = false;
        }
		
		buttonFavSet = (Button) this.findViewById(R.id.buttonFavSet);
		buttonShareReaderSetting = (Button) this.findViewById(R.id.buttonShareReaderSetting);
		editTextFilename = (EditText) this.findViewById(R.id.editTextFilename);
		buttonBrowseTxt = (Button) this.findViewById(R.id.buttonBrowseTxt);
		buttonBrowseEpub = (Button) this.findViewById(R.id.buttonBrowseEpub);
		buttonREBrowseTxt = (Button) this.findViewById(R.id.buttonREBrowseTxt);
		buttonOpen = (Button) this.findViewById(R.id.buttonOpen);
		buttonOpenSample = (Button) this.findViewById(R.id.buttonOpenSample);
		buttonSaveSetting = (Button) this.findViewById(R.id.buttonSaveSetting);
		editTextBGFilename = (EditText) this.findViewById(R.id.editTextBGFilename);
		buttonBrowseJPG = (Button) this.findViewById(R.id.buttonBrowseJPG);
		buttonBrowsePNG = (Button) this.findViewById(R.id.buttonBrowsePNG);
		buttonREBrowseJPG = (Button) this.findViewById(R.id.buttonREBrowseJPG);
		buttonBrowseBGClear = (Button) this.findViewById(R.id.buttonBrowseBGClear);
		radioButtonNotFullScreen = (RadioButton) this.findViewById(R.id.radioButtonNotFullScreen);
		radioButtonFullScreen = (RadioButton) this.findViewById(R.id.radioButtonFullScreen);
		radioButtonActionbar = (RadioButton) this.findViewById(R.id.radioButtonActionbar);
		radioButtonNoActionbar = (RadioButton) this.findViewById(R.id.radioButtonNoActionbar);
		radioButtonCurl = (RadioButton) this.findViewById(R.id.radioButtonCurl);
		radioButtonSimple = (RadioButton) this.findViewById(R.id.radioButtonSimple);
		radioButtonCurlSimple = (RadioButton) this.findViewById(R.id.radioButtonCurlSimple);
		radioButtonViewPager = (RadioButton) this.findViewById(R.id.radioButtonViewPager);
		radioButtonShiftJIS = (RadioButton) this.findViewById(R.id.radioButtonShiftJIS);
		radioButtonUTF8 = (RadioButton) this.findViewById(R.id.radioButtonUTF8);
		radioButtonGBK = (RadioButton) this.findViewById(R.id.radioButtonGBK);
		radioButtonUnicode = (RadioButton) this.findViewById(R.id.radioButtonUnicode);
		radioButtonNotSen = (RadioButton) this.findViewById(R.id.radioButtonNotSen);
		radioButtonSen = (RadioButton) this.findViewById(R.id.radioButtonSen);
		radioButtonNotReverseDirection = (RadioButton) this.findViewById(R.id.radioButtonNotReverseDirection);
		radioButtonReverseDirection = (RadioButton) this.findViewById(R.id.radioButtonReverseDirection);
		radioButtonParserAozora = (RadioButton) this.findViewById(R.id.radioButtonParserAozora);
		radioButtonParserPlain = (RadioButton) this.findViewById(R.id.radioButtonParserPlain);
		radioButtonNotIsVertical = (RadioButton) this.findViewById(R.id.radioButtonNotIsVertical);
		radioButtonIsVertical = (RadioButton) this.findViewById(R.id.radioButtonIsVertical);
		editTextPage = (EditText) this.findViewById(R.id.editTextPage);
		buttonClearPage = (Button) this.findViewById(R.id.buttonClearPage);
		radioButtonMaskBG = (RadioButton) this.findViewById(R.id.radioButtonMaskBG);
		radioButtonNoMaskBG = (RadioButton) this.findViewById(R.id.radioButtonNoMaskBG);
		editTextPosition = (EditText) this.findViewById(R.id.editTextPosition);
		buttonClearPosition = (Button) this.findViewById(R.id.buttonClearPosition);
		radioButtonBasePage = (RadioButton) this.findViewById(R.id.radioButtonBasePage);
		radioButtonBasePosition = (RadioButton) this.findViewById(R.id.radioButtonBasePosition);
		radioButtonScreenOrientationSys = (RadioButton) this.findViewById(R.id.radioButtonScreenOrientationSys);
		radioButtonScreenOrientationLand = (RadioButton) this.findViewById(R.id.radioButtonScreenOrientationLand);
		radioButtonScreenOrientationPort = (RadioButton) this.findViewById(R.id.radioButtonScreenOrientationPort);
		radioButtonNotBlackBack = (RadioButton) this.findViewById(R.id.radioButtonNotBlackBack);
		radioButtonBlackBack = (RadioButton) this.findViewById(R.id.radioButtonBlackBack);
		radioButtonNotUseVolumeKey = (RadioButton) this.findViewById(R.id.radioButtonNotUseVolumeKey);
		radioButtonUseVolumeKey = (RadioButton) this.findViewById(R.id.radioButtonUseVolumeKey);
		
		buttonSetPage = (Button) this.findViewById(R.id.buttonSetPage);
		buttonSetPos = (Button) this.findViewById(R.id.buttonSetPos);
		
		scrollView1 = (ScrollView) this.findViewById(R.id.scrollView1);
		textViewLoading = (TextView) this.findViewById(R.id.textViewLoading);
		
		buttonTestSen = (Button) this.findViewById(R.id.buttonTestSen);
		
		builder1 = new AlertDialog.Builder(this);
		builder2 = new AlertDialog.Builder(this);
		builder3 = new AlertDialog.Builder(this);
		builder4 = new AlertDialog.Builder(this);
		builder5 = new AlertDialog.Builder(this);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("阅读器设置");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				//return R.drawable.book;
				return R.drawable.config2;
			}

			@Override
			public void performAction(View view) {
				if (false) {
					if (mIsSaved) {
						finish();
					} else {
						showDialog(DIALOG_NOTSAVE_ID);
					}
				} else {
					finish();
				}
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
		
        buttonFavSet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				radioButtonFullScreen.setChecked(true);
				radioButtonNoActionbar.setChecked(true);
				radioButtonViewPager.setChecked(true);
				radioButtonNotReverseDirection.setChecked(true);
				radioButtonIsVertical.setChecked(true);
				radioButtonScreenOrientationPort.setChecked(true);
				radioButtonBlackBack.setChecked(true);
				radioButtonNotUseVolumeKey.setChecked(true);
				Toast.makeText(JkanjiAozoraReaderActivity.this, 
						"方案2设置完成，请手动按保存按钮", Toast.LENGTH_SHORT)
						.show();
			}
        });
		buttonShareReaderSetting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String filename = editTextFilename.getText().toString();
				String bgfilename = editTextBGFilename.getText().toString();
				int page = 0;
				try {
					page = Integer.parseInt(editTextPage.getText().toString());
				} catch (Exception e) {
					//e.printStackTrace();
				}
				int position = 0;
				try {
					position = Integer.parseInt(editTextPosition.getText().toString());
				} catch (Exception e) {
					//e.printStackTrace();
				}				
				StringBuffer sb = new StringBuffer();
				sb.append("filename:" + filename + "\n");
				sb.append("bgfilename:" + bgfilename + "\n");
				sb.append("page:" + page + "\n");
				sb.append("isFullScreen:" + radioButtonFullScreen.isChecked() + "\n");
				sb.append("hasActionbar:" + radioButtonActionbar.isChecked() + "\n");
				sb.append("enableSen:" + radioButtonSen.isChecked() + "\n");
				sb.append("curlType:" + getCurlTypeString() + "\n");
				sb.append("codePage:" + getCodePage() + "\n");
				sb.append("reverseDirection:" + radioButtonReverseDirection.isChecked() + "\n");
				sb.append("parserType:" + getParserTypeString() + "\n");
				sb.append("isVertical:" + radioButtonIsVertical.isChecked() + "\n");
				sb.append("maskBG:" + radioButtonMaskBG.isChecked() + "\n");
				sb.append("basePage:" + radioButtonBasePage.isChecked() + "\n");
				sb.append("position:" + position + "\n");
				sb.append("screenOrientation:" + getScreenOrientationString() + "\n");
				sb.append("blackBack:" + radioButtonBlackBack.isChecked() + "\n");
				sb.append("useVolumeKey:" + radioButtonUseVolumeKey.isChecked() + "\n");
				Intent intent;
				intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT, getTimeString() + ":阅读器预设");
				intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
                try {
                	//startActivity(Intent.createChooser(intent, "共享方式"));
                	startActivity(intent);
                } catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(JkanjiAozoraReaderActivity.this, 
						"共享方式出错", Toast.LENGTH_SHORT)
						.show();
				}
			}
		});
		
		buttonBrowseTxt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				browseTxt();
			}
		});
		buttonBrowseEpub.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				browseEpub();
			}
		});
		
		buttonREBrowseTxt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.setType("*/*");
				try {
					startActivityForResult(intent, REQUEST_RE_PATH);
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(JkanjiAozoraReaderActivity.this, 
						"找不到可用的应用程序", Toast.LENGTH_SHORT)
						.show();
				}
			}
		});
		
		buttonOpen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String filename = editTextFilename.getText().toString();
				String bgfilename = editTextBGFilename.getText().toString();
				if (filename != null && filename.length() > 0) {
					int page = 0;
					try {
						page = Integer.parseInt(editTextPage.getText().toString());
					} catch (Exception e) {
						//e.printStackTrace();
					}
					int position = 0;
					try {
						position = Integer.parseInt(editTextPosition.getText().toString());
					} catch (Exception e) {
						//e.printStackTrace();
					}
					Intent intent = null;
					if (getCurlType() == BookInfoUtils.CURL_TYPE_VIEWPAGER) {
						switch (getScreenOrientation()) {
						case BookInfoUtils.SCREEN_ORI_LAND: 
							intent = new Intent(JkanjiAozoraReaderActivity.this, ViewPagerLandActivity.class);
							break;
							
						case BookInfoUtils.SCREEN_ORI_PORT:
							intent = new Intent(JkanjiAozoraReaderActivity.this, ViewPagerPortActivity.class);
							break;
							
						default:
							intent = new Intent(JkanjiAozoraReaderActivity.this, ViewPagerActivity.class);
							break;
						}
					} else {
						switch (getScreenOrientation()) {
						case BookInfoUtils.SCREEN_ORI_LAND: 
							intent = new Intent(JkanjiAozoraReaderActivity.this, CurlLandActivity.class);
							break;
							
						case BookInfoUtils.SCREEN_ORI_PORT:
							intent = new Intent(JkanjiAozoraReaderActivity.this, CurlPortActivity.class);
							break;
							
						default:
							intent = new Intent(JkanjiAozoraReaderActivity.this, CurlActivity.class);
							break;
						}
					}
					startActivity(
							intent
							.putExtra(BookInfoUtils.EXTRA_KEY_FILE_NAME, filename)
							.putExtra(BookInfoUtils.EXTRA_KEY_BG_FILE_NAME, bgfilename)
							.putExtra(BookInfoUtils.EXTRA_KEY_IS_FULL_SCREEN, radioButtonFullScreen.isChecked())
							.putExtra(BookInfoUtils.EXTRA_KEY_HAS_ACTIONBAR, radioButtonActionbar.isChecked())
							.putExtra(BookInfoUtils.EXTRA_KEY_ENABLE_SEN, radioButtonSen.isChecked())
							.putExtra(BookInfoUtils.EXTRA_KEY_PAGE, page)
							.putExtra(BookInfoUtils.EXTRA_KEY_CURL_TYPE, getCurlType())
							.putExtra(BookInfoUtils.EXTRA_KEY_CODEPAGE, getCodePage())
							.putExtra(BookInfoUtils.EXTRA_KEY_REVERSE_DIRECTION, radioButtonReverseDirection.isChecked())
							.putExtra(BookInfoUtils.EXTRA_KEY_PARSER_TYPE, getParserType())
							.putExtra(BookInfoUtils.EXTRA_KEY_IS_VERTICAL, radioButtonIsVertical.isChecked())
							.putExtra(BookInfoUtils.EXTRA_KEY_MASK_BG, radioButtonMaskBG.isChecked())
							.putExtra(BookInfoUtils.EXTRA_KEY_BASE_PAGE, radioButtonBasePage.isChecked())
							.putExtra(BookInfoUtils.EXTRA_KEY_POSITION, position)
							.putExtra(BookInfoUtils.EXTRA_KEY_SCREEN_ORI, getScreenOrientation())
							.putExtra(BookInfoUtils.EXTRA_KEY_BLACK_BACK, radioButtonBlackBack.isChecked())
							.putExtra(BookInfoUtils.EXTRA_KEY_USE_VOLUME_KEY, radioButtonUseVolumeKey.isChecked())
					);
					if (D) {
						Log.d(TAG, "radioButtonFullScreen = " + radioButtonFullScreen.isChecked() + ", " + 
								"radioButtonSen = " + radioButtonSen.isChecked()
								);
					}
					mIsSaved = true;
				} else {
					Toast.makeText(JkanjiAozoraReaderActivity.this, 
							"请先指定文件名", 
							Toast.LENGTH_SHORT).show();
					browseTxt();
				}
			}
		});
		
		buttonOpenSample.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String bgfilename = editTextBGFilename.getText().toString();
				int page = 0;
				try {
					page = Integer.parseInt(editTextPage.getText().toString());
				} catch (Exception e) {
					//e.printStackTrace();
				}
				int position = 0;
				try {
					position = Integer.parseInt(editTextPosition.getText().toString());
				} catch (Exception e) {
					//e.printStackTrace();
				}
				Intent intent = null;
				if (getCurlType() == BookInfoUtils.CURL_TYPE_VIEWPAGER) {
					switch (getScreenOrientation()) {
					case BookInfoUtils.SCREEN_ORI_LAND: 
						intent = new Intent(JkanjiAozoraReaderActivity.this, ViewPagerLandActivity.class);
						break;
						
					case BookInfoUtils.SCREEN_ORI_PORT:
						intent = new Intent(JkanjiAozoraReaderActivity.this, ViewPagerPortActivity.class);
						break;
						
					default:
						intent = new Intent(JkanjiAozoraReaderActivity.this, ViewPagerActivity.class);
						break;
					}
				} else {
					switch (getScreenOrientation()) {
					case BookInfoUtils.SCREEN_ORI_LAND: 
						intent = new Intent(JkanjiAozoraReaderActivity.this, CurlLandActivity.class);
						break;
						
					case BookInfoUtils.SCREEN_ORI_PORT:
						intent = new Intent(JkanjiAozoraReaderActivity.this, CurlPortActivity.class);
						break;
						
					default:
						intent = new Intent(JkanjiAozoraReaderActivity.this, CurlActivity.class);
						break;
					}
				}
				startActivity(
					intent
						.putExtra(BookInfoUtils.EXTRA_KEY_IS_FULL_SCREEN, radioButtonFullScreen.isChecked())
						.putExtra(BookInfoUtils.EXTRA_KEY_HAS_ACTIONBAR, radioButtonActionbar.isChecked())
						.putExtra(BookInfoUtils.EXTRA_KEY_BG_FILE_NAME, bgfilename)
						.putExtra(BookInfoUtils.EXTRA_KEY_ENABLE_SEN, radioButtonSen.isChecked())
						.putExtra(BookInfoUtils.EXTRA_KEY_PAGE, page)
						.putExtra(BookInfoUtils.EXTRA_KEY_CURL_TYPE, getCurlType())
						.putExtra(BookInfoUtils.EXTRA_KEY_CODEPAGE, getCodePage())
						.putExtra(BookInfoUtils.EXTRA_KEY_REVERSE_DIRECTION, radioButtonReverseDirection.isChecked())
						.putExtra(BookInfoUtils.EXTRA_KEY_PARSER_TYPE, getParserType())
						.putExtra(BookInfoUtils.EXTRA_KEY_IS_VERTICAL, radioButtonIsVertical.isChecked())
						.putExtra(BookInfoUtils.EXTRA_KEY_MASK_BG, radioButtonMaskBG.isChecked())
						.putExtra(BookInfoUtils.EXTRA_KEY_BASE_PAGE, radioButtonBasePage.isChecked())
						.putExtra(BookInfoUtils.EXTRA_KEY_POSITION, position)
						.putExtra(BookInfoUtils.EXTRA_KEY_SCREEN_ORI, getScreenOrientation())
						.putExtra(BookInfoUtils.EXTRA_KEY_BLACK_BACK, radioButtonBlackBack.isChecked())
						.putExtra(BookInfoUtils.EXTRA_KEY_USE_VOLUME_KEY, radioButtonUseVolumeKey.isChecked())
						
				);
				if (D) {
					Log.d(TAG, "radioButtonFullScreen = " + radioButtonFullScreen.isChecked() + ", " + 
							"radioButtonSen = " + radioButtonSen.isChecked()
							);
				}
				mIsSaved = true;
			}
		});
		
		buttonSaveSetting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//不保存：文件名，位置，页码，代码页，解释器类型，是否基于页码
				saveSettings();
				Context context = JkanjiAozoraReaderActivity.this;
				Toast.makeText(context, "保存成功，下次在书架中打开文件时生效", Toast.LENGTH_SHORT).show();
				mIsSaved = true;
			}
		});
		
		buttonBrowseJPG.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				browseJPG();
			}
		});
		
		buttonBrowsePNG.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				browsePNG();
			}
		});
		
		buttonREBrowseJPG.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				try {
					startActivityForResult(intent, REQUEST_RE_JPG_PATH);
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(JkanjiAozoraReaderActivity.this, 
						"找不到可用的应用程序", Toast.LENGTH_SHORT)
						.show();
				}
			}
		});
		
		buttonBrowseBGClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editTextBGFilename.setText("");
			}
		});
		
		radioButtonNotFullScreen.setChecked(true);
		radioButtonFullScreen.setChecked(false);
		radioButtonActionbar.setChecked(true);
		radioButtonNoActionbar.setChecked(false);
		radioButtonMaskBG.setChecked(true);
		radioButtonNoMaskBG.setChecked(false);
		radioButtonBasePage.setChecked(true);
		radioButtonBasePosition.setChecked(false);
		radioButtonNotBlackBack.setChecked(true);
		radioButtonBlackBack.setChecked(false);
		radioButtonNotUseVolumeKey.setChecked(false);
		radioButtonUseVolumeKey.setChecked(true);
		
		System.setProperty("sen.home", JkanjiSettingActivity.getDataPackPath(this) + SEN_HOME);
//		radioButtonSen.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				tryEnableSen();
//			}
//		});

		buttonSetPage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_SETPAGE_ID);
			}
		});
		buttonClearPage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editTextPage.setText("");
			}
		});
		editTextFilename.setText(BookInfoUtils.getLastFileName(this));
		editTextBGFilename.setText(BookInfoUtils.getLastBGFileName(this));
		
		if (BookInfoUtils.getLastIsFullScreen(this)) {
			radioButtonFullScreen.setChecked(true);
		} else {
			radioButtonNotFullScreen.setChecked(true);
		}
		if (BookInfoUtils.getLastHasActionbar(this)) {
			radioButtonActionbar.setChecked(true);
		} else {
			radioButtonNoActionbar.setChecked(true);
		}
		
		int curlType = BookInfoUtils.getLastCurlType(this);
		switch (curlType) {
		case BookInfoUtils.CURL_TYPE_CURL:
			radioButtonCurl.setChecked(true);
			break;
		
		case BookInfoUtils.CURL_TYPE_SIMPLE:
			radioButtonSimple.setChecked(true);
			break;
			
		case BookInfoUtils.CURL_TYPE_VIEWPAGER:
			radioButtonViewPager.setChecked(true);
			break;
			
		default:
			radioButtonCurlSimple.setChecked(true);
			break;
		}
		
		int parserType = BookInfoUtils.getLastParserType(this);
		switch (parserType) {
		case BookInfoUtils.PARSER_TYPE_AOZORA:
			radioButtonParserAozora.setChecked(true);
			break;
			
		case BookInfoUtils.PARSER_TYPE_PLAIN:
			radioButtonParserPlain.setChecked(true);
			break;
			
		default:
			radioButtonParserAozora.setChecked(true);
			break;
		}

		if (BookInfoUtils.getLastEnableSen(this)) {
			radioButtonSen.setChecked(true);
			if (false) {
				tryEnableSen();
			} else {
				if (!checkFilesExist(FILENAMES)) {
					radioButtonNotSen.setChecked(true);
					radioButtonSen.setChecked(false);
					radioButtonSen.setEnabled(false);
				}
			}
		} else {
			radioButtonNotSen.setChecked(true);
		}
		
		String codePage = BookInfoUtils.getLastCodePage(this);
		if (codePage != null && codePage.length() > 0) {
			if ("utf8".equals(codePage)) {
				radioButtonUTF8.setChecked(true);
			} else if ("gbk".equals(codePage)) {
				radioButtonGBK.setChecked(true);
			} else if ("unicode".equals(codePage)) {
				radioButtonUnicode.setChecked(true);
			} else {
				radioButtonShiftJIS.setChecked(true);
			}
		} else {
			radioButtonShiftJIS.setChecked(true);
		}
		
		if (BookInfoUtils.getLastReverseDirection(this)) {
			radioButtonReverseDirection.setChecked(true);
		} else {
			radioButtonNotReverseDirection.setChecked(true);
		}
		
		if (BookInfoUtils.getLastIsVertical(this)) {
			radioButtonIsVertical.setChecked(true);
		} else {
			radioButtonNotIsVertical.setChecked(true);
		}
		
		if (BookInfoUtils.getLastMaskBG(this)) {
			radioButtonMaskBG.setChecked(true);
		} else {
			radioButtonNoMaskBG.setChecked(true);
		}

		buttonSetPos.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_SETPOS_ID);
			}
		});
		buttonClearPosition.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editTextPosition.setText("");
			}
		});
		
		if (BookInfoUtils.getLastBasePage(this)) {
			radioButtonBasePage.setChecked(true);
		} else {
			radioButtonBasePosition.setChecked(true);
		}
		if (radioButtonBasePage.isChecked()) {
			buttonSetPage.setEnabled(true);
			buttonClearPage.setEnabled(true);
			buttonSetPos.setEnabled(false);
			buttonClearPosition.setEnabled(false);
		} else {
			buttonSetPage.setEnabled(false);
			buttonClearPage.setEnabled(false);
			buttonSetPos.setEnabled(true);
			buttonClearPosition.setEnabled(true);
		}
		radioButtonBasePage.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					buttonSetPage.setEnabled(true);
					buttonClearPage.setEnabled(true);
					buttonSetPos.setEnabled(false);
					buttonClearPosition.setEnabled(false);
				} else {
					buttonSetPage.setEnabled(false);
					buttonClearPage.setEnabled(false);
					buttonSetPos.setEnabled(true);
					buttonClearPosition.setEnabled(true);
				}				
			}
		});
		
		int screenOri = BookInfoUtils.getLastScreenOri(this);
		switch (screenOri) {
		case BookInfoUtils.SCREEN_ORI_LAND:
			radioButtonScreenOrientationLand.setChecked(true);
			break;
		
		case BookInfoUtils.SCREEN_ORI_PORT:
			radioButtonScreenOrientationPort.setChecked(true);
			break;
			
		default:
			radioButtonScreenOrientationSys.setChecked(true);
			break;
		}
		if (BookInfoUtils.getLastBlackBack(this)) {
			radioButtonBlackBack.setChecked(true);
		} else {
			radioButtonNotBlackBack.setChecked(true);
		}
		if (BookInfoUtils.getLastUseVolumeKey(this)) {
			radioButtonUseVolumeKey.setChecked(true);
		} else {
			radioButtonNotUseVolumeKey.setChecked(true);
		}
		
		buttonTestSen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tryEnableSen();
			}
		});
	}
	
	private void tryEnableSen() {
		if (!checkFilesExist(FILENAMES)) {
			showDialog(DIALOG_WARNING_ID);
			radioButtonNotSen.setChecked(true);
			radioButtonSen.setChecked(false);
//			radioButtonSen.setVisibility(RadioButton.INVISIBLE);
			radioButtonSen.setEnabled(false);
		} else {
			if (JkanjiSettingActivity.getShowSenDlg(this)) {
				showDialog(DIALOG_LOADING_ID);
			} else {
				onAnalyze();
			}
		}
	}
	
	private void browseTxt() {
		Intent intent = new Intent(
				JkanjiAozoraReaderActivity.this, DirBrowser.class);
		intent.putExtra(DirBrowser.EXTRA_KEY_SUFFIX, ".txt");
		startActivityForResult(intent, REQUEST_PATH);
	}
	private void browseEpub() {
		Intent intent = new Intent(
				JkanjiAozoraReaderActivity.this, DirBrowser.class);
		intent.putExtra(DirBrowser.EXTRA_KEY_SUFFIX, ".epub");
		startActivityForResult(intent, REQUEST_PATH);
	}
	
	private void browseJPG() {
		Intent intent = new Intent(
				JkanjiAozoraReaderActivity.this, DirBrowser.class);
		intent.putExtra(DirBrowser.EXTRA_KEY_SUFFIX, ".jpg");
		startActivityForResult(intent, REQUEST_JPG_PATH);
	}
	
	private void browsePNG() {
		Intent intent = new Intent(
				JkanjiAozoraReaderActivity.this, DirBrowser.class);
		intent.putExtra(DirBrowser.EXTRA_KEY_SUFFIX, ".png");
		startActivityForResult(intent, REQUEST_PNG_PATH);
	}
	
    @Override
	protected void onPause() {
		super.onPause();
		saveSettings();
	}

	@Override
	protected void onStop() {
    	super.onStop();
    	saveSettings();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (D) {
			Log.d(TAG, "onResume");
		}
		editTextPage.setText(Integer.toString(BookInfoUtils.getLastPage(this) + 1));
		editTextPosition.setText(Integer.toString(BookInfoUtils.getLastPosition(this)));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_PATH:
			if (data != null) {
				String resultPath = data.getStringExtra(DirBrowser.EXTRA_KEY_RESULT_PATH);
				this.editTextFilename.setText(resultPath);
			}
			break;
			
		case REQUEST_JPG_PATH:
		case REQUEST_PNG_PATH:
			if (data != null) {
				String resultPath = data.getStringExtra(DirBrowser.EXTRA_KEY_RESULT_PATH);
				this.editTextBGFilename.setText(resultPath);
			}
			break;
			
		case REQUEST_RE_PATH:
			if (resultCode == RESULT_OK && 
				data != null &&
				data.getData() != null) {
				String resultPath = data.getData().getPath();
				this.editTextFilename.setText(resultPath);
			}
			break;
			
		case REQUEST_RE_JPG_PATH:
			if (resultCode == RESULT_OK && 
				data != null &&
				data.getData() != null) {
				String resultPath = data.getData().getPath();
				this.editTextBGFilename.setText(resultPath);
			}			
			break;
		}
	}
    
	private boolean checkFilesExist(String[] filenames) {
		boolean result = true;
		for (int i = 0; i < filenames.length; i++) {
			File file = new File(JkanjiSettingActivity.getDataPackPath(this) + filenames[i]);
			if (!file.canRead() || !file.exists()) {
				result = false;
				//System.out.println("==================" + filenames[i]);
				break;
			}
		}
		return result;
	}
    
	@Override
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog = null;
	    switch(id) {
	    case DIALOG_WARNING_ID:
	    	if (builder1 != null) {
	    		return builder1
	    			.setTitle("警告")
	    			.setMessage("\nsen需要的一些文件貌似不存在，请在/sdcard/jkanji/sen或全局设置中指定的目录下安装数据包。")
	    			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
	    				public void onClick(DialogInterface dialog, int id) {
	    					
	    				}
	    			})
	    	       .create();
	    	}
	        break;
	        
	    case DIALOG_LOADING_ID:
	    	if (builder2 != null) {
	    		return builder2
	    			.setTitle("sen初始化")
	    			.setMessage("\nsen将进行初始化，第一次初始化可能需要1至2分钟，是否开始？")
	    			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
	    				public void onClick(DialogInterface dialog, int id) {
	    					//isFirstStarted = false;
	    					onAnalyze();
	    				}
	    			})
	    			.setNegativeButton("取消", new DialogInterface.OnClickListener() {
	    				public void onClick(DialogInterface dialog, int id) {
//	    					radioButtonNotSen.setChecked(true);
//	    					radioButtonSen.setChecked(false);
	    				}
	    			})
	    			.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
	    					radioButtonNotSen.setChecked(true);
	    					radioButtonSen.setChecked(false);							
						}
	    			})
	    	       .create();
	    	}
	    	break;
            
        case DIALOG_NOTSAVE_ID:
	    	if (builder3 != null) {
	    		return builder3
	    			.setTitle("警告")
	    			.setMessage("\n设置尚未按保存按钮或预览（预览将保存设置），是否不保存退出？")
	    			.setPositiveButton("退出不保存", new DialogInterface.OnClickListener() {
	    				public void onClick(DialogInterface dialog, int id) {
	    					finish();
	    				}
	    			})
	    			.setNegativeButton("取消", new DialogInterface.OnClickListener() {
	    				public void onClick(DialogInterface dialog, int id) {
	    					
	    				}
	    			})
	    	       .create();
	    	}
	    	break;
	    	
		case DIALOG_SETPAGE_ID:
			LayoutInflater factory1 = LayoutInflater.from(this);
            View textEntryView1 = factory1.inflate(R.layout.set_page_dialog, null);
            editPage = (EditText) textEntryView1.findViewById(R.id.editPage);
            pageDialog = builder4
            	.setTitle("指定页数")
                .setView(textEntryView1)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	editTextPage.setText(editPage.getText().toString());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	
                    }
                })
                .create();
            pageDialog.setOnShowListener(new OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					editPage.setEnabled(true);
					editPage.setText("");
		            editPage.append(editTextPage.getText().toString());
				}
            });
            return pageDialog;
            
		case DIALOG_SETPOS_ID:
			LayoutInflater factory2 = LayoutInflater.from(this);
            View textEntryView2 = factory2.inflate(R.layout.set_pos_dialog, null);
            editPos = (EditText) textEntryView2.findViewById(R.id.editPos);
            posDialog = builder5
            	.setTitle("指定位置")
                .setView(textEntryView2)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	editTextPosition.setText(editPos.getText().toString());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	
                    }
                })
                .create();
            posDialog.setOnShowListener(new OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					editPos.setEnabled(true);
					editPos.setText("");
		            editPos.append(editTextPosition.getText().toString());
				}
            });
            return posDialog;
	    }
	    return dialog;
	}
	
	private void onAnalyze() {
		if (!BookInfoUtils.USE_CONTENT_PROVIDER) {
			boolean result = analyzeTest(); // initialize on first time.
			if (result == false) {
				Toast.makeText(this, 
						"Error: data files in " + 
						JkanjiSettingActivity.getDataPackPath(this) + SEN_HOME +
						" does not exist, or something goes wrong.",
						Toast.LENGTH_SHORT).show();
				radioButtonNotSen.setChecked(true);
				radioButtonSen.setChecked(false);
				radioButtonSen.setVisibility(RadioButton.INVISIBLE);
			} else {
				radioButtonNotSen.setChecked(false);
				radioButtonSen.setChecked(true);
			}
		} else {
			if (USE_ASYNCTASK) {
				new LoadDataTask().execute();
			} else {
				ArrayList<BookDrawTextUtil.ExtraRubyInfo> result = BookInfoUtils.showResults(this, TEST_TEXT2, 0);
				if (result != null && result.size() > 0) {
					radioButtonNotSen.setChecked(false);
					radioButtonSen.setChecked(true);				
				} else {
					radioButtonNotSen.setChecked(true);
					radioButtonSen.setChecked(false);
					radioButtonSen.setVisibility(RadioButton.INVISIBLE);				
				}
			}
		}
	}
	
	private boolean analyzeTest() {
		StringTagger tagger;
		boolean result = true;
		try {
			tagger = StringTagger.getInstance();
			tagger.analyze(TEST_TEXT2);
		} catch (Throwable e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	
	private int getCurlType() {
		if (this.radioButtonCurl.isChecked()) {
			return BookInfoUtils.CURL_TYPE_CURL;
		} else if (this.radioButtonSimple.isChecked()) {
			return BookInfoUtils.CURL_TYPE_SIMPLE;
		} else if (this.radioButtonViewPager.isChecked()) {
			return BookInfoUtils.CURL_TYPE_VIEWPAGER;
		} else {
			return BookInfoUtils.CURL_TYPE_CURLSIMPLE;
		}
	}

	private String getCurlTypeString() {
		if (this.radioButtonCurl.isChecked()) {
			return "curl";
		} else if (this.radioButtonSimple.isChecked()) {
			return "simple";
		} else if (this.radioButtonViewPager.isChecked()) {
			return "viewpager";
		} else {
			return "cuilAndSimple";
		}
	}
	
	private int getCurlTypeInt(String str) {
		if (str != null && str.length() > 0) {
			if ("curl".equals(str)) {
				return 0;
			} else if ("simple".equals(str)) {
				return 1;
			} else {
				return 2;
			}
		} else {
			return 0;
		}
	}
	
	private int getParserType() {
		if (this.radioButtonParserAozora.isChecked()) {
			return BookInfoUtils.PARSER_TYPE_AOZORA;
		} else if (this.radioButtonParserPlain.isChecked()) {
			return BookInfoUtils.PARSER_TYPE_PLAIN;
		} else {
			return BookInfoUtils.PARSER_TYPE_AOZORA;
		}
	}
	
	private String getParserTypeString() {
		if (this.radioButtonParserAozora.isChecked()) {
			return "aozora";
		} else if (this.radioButtonParserPlain.isChecked()) {
			return "plain";
		} else {
			return "aozora";
		}
	}
	
	private String getCodePage() {
		if (this.radioButtonShiftJIS.isChecked()) {
			return "shift-jis";
		} else if (this.radioButtonUTF8.isChecked()) {
			return "utf8";
		} else if (this.radioButtonUnicode.isChecked()) {
			return "unicode";
		} else {
			return "gbk";
		}
	}

	private int getScreenOrientation() {
		if (this.radioButtonScreenOrientationLand.isChecked()) {
			return BookInfoUtils.SCREEN_ORI_LAND;
		} else if (this.radioButtonScreenOrientationPort.isChecked()) {
			return BookInfoUtils.SCREEN_ORI_PORT;
		} else {
			return BookInfoUtils.SCREEN_ORI_UNDEFINED;
		}
	}
	
	private String getScreenOrientationString() {
		if (this.radioButtonScreenOrientationLand.isChecked()) {
			return "landscape";
		} else if (this.radioButtonScreenOrientationPort.isChecked()) {
			return "portrait";
		} else {
			return "undefined";
		}
	}
	
	private class LoadDataTask extends AsyncTask<Void, Void, Boolean> {
		private boolean loadResult = false;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			scrollView1.setVisibility(View.INVISIBLE);
			textViewLoading.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				ArrayList<BookDrawTextUtil.ExtraRubyInfo> result = BookInfoUtils.showResults(JkanjiAozoraReaderActivity.this, TEST_TEXT2, 0);
				if (result != null && result.size() > 0) {
					loadResult = true;
				} else {
					loadResult = false;
				}
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			scrollView1.setVisibility(View.VISIBLE);
			textViewLoading.setVisibility(View.INVISIBLE);
			if (result == true && !JkanjiAozoraReaderActivity.this.isFinishing()) {
				if (loadResult) {
					radioButtonNotSen.setChecked(false);
					radioButtonSen.setChecked(true);	
					startSenForgroundService();
				} else {
					radioButtonNotSen.setChecked(true);
					radioButtonSen.setChecked(false);
					radioButtonSen.setVisibility(RadioButton.INVISIBLE);				
				}
			} else if (result == false) {
				finish();
			}
		}
    }
	
    public static String getTimeString() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale
			.getDefault()).format(new Date(System.currentTimeMillis()));
    }
    
	public void startSenForgroundService() {
		if (JkanjiSettingActivity.getSenService(this)) {
			startService(
					new Intent(this, JkanjiSenService.class)
						.setAction(JkanjiSenService.ACTION_FOREGROUND));
		}
	}


	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			savedInstanceState.putBoolean(IS_SAVED, mIsSaved);
		}
	}


//	@Override
//	public void onBackPressed() {
//		if (mIsSaved) {
//			super.onBackPressed();
//		} else {
//			showDialog(DIALOG_NOTSAVE_ID);
//		}
//	}
	
	private void saveSettings() {
		String bgfilename = editTextBGFilename.getText().toString();
		Context context = JkanjiAozoraReaderActivity.this;
		BookInfoUtils.setLastIsFullScreen(context, radioButtonFullScreen.isChecked());
		BookInfoUtils.setLastHasActionbar(context, radioButtonActionbar.isChecked());
		BookInfoUtils.setLastBGFileName(context, bgfilename);
		BookInfoUtils.setLastEnableSen(context, radioButtonSen.isChecked());
		BookInfoUtils.setLastCurlType(context, getCurlType());
		BookInfoUtils.setLastReverseDirection(context, radioButtonReverseDirection.isChecked());
		BookInfoUtils.setLastIsVertical(context, radioButtonIsVertical.isChecked());
		BookInfoUtils.setLastMaskBG(context, radioButtonMaskBG.isChecked());
		BookInfoUtils.setLastScreenOri(context, getScreenOrientation());
		BookInfoUtils.setLastBlackBack(context, radioButtonBlackBack.isChecked());
		BookInfoUtils.setLastUseVolumeKey(context, radioButtonUseVolumeKey.isChecked());
	}
}
