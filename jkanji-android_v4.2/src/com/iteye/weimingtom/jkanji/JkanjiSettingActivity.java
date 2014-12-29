package com.iteye.weimingtom.jkanji;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;

import fi.harism.curl.BookInfoUtils;

public class JkanjiSettingActivity extends Activity {
	public final static int HL_TYPE_CHAR = 0;
	public final static int HL_TYPE_STRING = 1;
	public final static int HL_TYPE_NONE = 2;
	
	private static final String SHARE_PREF_NAME = "pref";
	private static final String SHARE_PREF_FONT_FILENAME = "fontfilename";
	private static final String SHARE_PREF_EPWING_FILENAME = "epwingfilename";
	private static final String SHARE_PREF_USE_OPERA = "useOpera";
	private static final String SHARE_PREF_SHOW_TALK_ICON = "showTalkIcon";
	private static final String SHARE_PREF_SHOW_SEN_DLG = "showSenDlg";
	private static final String SHARE_PREF_USE_HIRAGANA_EXTRA = "useHiraganaExtra";
	private static final String SHARE_PREF_SEARCH_MINI = "searchMini";
	private static final String SHARE_PREF_BG_FILENAME = "bgfilename";
	private static final String SHARE_PREF_DICT_SERVICE = "dictService";
	private static final String SHARE_PREF_SEN_SERVICE = "senService";
	private static final String SHARE_PREF_AOZORA_SERVICE = "aozoraService";
	private static final String SHARE_PREF_USE_TTS = "useTTS";
	private static final String SHARE_PREF_SHOW_BANNER = "showBanner";
	private static final String SHARE_PREF_USE_GRID = "useGrid";
	private static final String SHARE_PREF_USE_KEY_PAGER = "useKeyPager";
	private static final String SHARE_PREF_HL_TYPE = "hlType";
	private static final String SHARE_PREF_USE_TOOL_BUTTONS = "useToolButtons";
	private static final String SHARE_PREF_USE_RADICAL_INPUT = "useRadicalInput";
	private static final String SHARE_PREF_DATA_PACK_PATH = "dataPackPath";
	private static final String SHARE_PREF_SHOW_SPLASH_SCREEN = "showSplashScreen";
	private static final String SHARE_PREF_JUMP_OLD_VERSION = "jumpOldVersion";
	
	private static final String SHARE_PREF_RB_SIZE = "aozoraRbSize";
	private static final String SHARE_PREF_RT_SIZE = "aozoraRtSize";
	private static final String SHARE_PREF_SPACE_SIZE = "aozoraSpaceSize";
	
	private static final int REQUEST_TTF_PATH = 1;
	private static final int REQUEST_EPWING_PATH = 2;
	private static final int REQUEST_RE_TTF_PATH = 3;
	private static final int REQUEST_RE_EPWING_PATH = 4;
	private static final int REQUEST_BG_JPG_PATH = 5;
	private static final int REQUEST_BG_PNG_PATH = 6;
	private static final int REQUEST_RE_BG_PATH = 7;
	private static final int REQUEST_GAIJI_MAP_PATH = 8;
	private static final int REQUEST_RE_GAIJI_MAP_PATH = 9;
	
	private static final int DIALOG_TEXT_ENTRY = 1;
	private AlertDialog.Builder builder;
	private AlertDialog fontSizeDialog;
	private EditText editTextRbSize, editTextRtSize, editTextSpaceSize;
	private Button buttonAozoraFontSize, buttonAozoraDefaultFontSize;
	
	private Button buttonLoadGaijiDB, buttonClearGaijiDB, buttonLoadREGaijiDB;
	
	private ActionBar actionBar;
	private ImageView imageViewTop;
	
	private EditText editTextFontFilePath;
	private Button buttonOpenFontFilePath;
	private Button buttonOpenREFontFilePath;
	private Button buttonClearFontFilePath;
	
	private EditText editTextEpwingPath;
	private Button buttonOpenEpwingPath;
	private Button buttonOpenREEpwingPath;
	private Button buttonClearEpwingPath;
	
	private CheckBox checkBoxUseOpera;
	private CheckBox checkBoxShowTalkIcon;
	private CheckBox checkBoxShowSenDlg;
	private CheckBox checkBoxUseHiraganaExtra;
	private CheckBox checkBoxSearchMini;
	private CheckBox checkBoxEnableDictService;
	private CheckBox checkBoxEnableSenService;
	private CheckBox checkBoxEnableAozoraService;
	private CheckBox checkBoxUseTTS;
	private CheckBox checkBoxShowBanner;
	private CheckBox checkBoxUseGrid;
	private CheckBox checkBoxUseKeyPager;
	private CheckBox checkBoxUseToolButtons;
//	private CheckBox checkBoxUseRadicalInput;
	private CheckBox checkBoxShowSplashScreen;
	private CheckBox checkBoxJumpOldVersion;
	
	private EditText editTextBGFilePath;
	private Button buttonOpenBGJPGFilePath;
	private Button buttonOpenBGPNGFilePath;
	private Button buttonOpenREBGFilePath;
	private Button buttonClearBGFilePath;
	private Button buttonKillDictService;
	private Button buttonKillSenService;
	private Button buttonKillAozoraService;
	private RadioButton radioButtonHLChar, radioButtonHLString, radioButtonHLNone;
	
	private ScrollView scrollView1;
	private TextView textViewLoading;
	
	private Button buttonDeleteWebviewCache;
	
	private EditText editTextDataPackPath;
	private Button buttonDataPackPath1, buttonDataPackPath2, buttonDataPackPath3, buttonDataPackPath4, buttonDataPackPath5;
	
	
	private static final int DIALOG_TEXT_ENTRY2 = 2;
	private AlertDialog.Builder builder2;
	private AlertDialog dataPackPathDialog;
	private EditText editTextDataPackInput;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.setting);
		
		editTextFontFilePath = (EditText) this.findViewById(R.id.editTextFontFilePath);
		buttonOpenFontFilePath = (Button) this.findViewById(R.id.buttonOpenFontFilePath);
		buttonOpenREFontFilePath = (Button) this.findViewById(R.id.buttonOpenREFontFilePath);
		buttonClearFontFilePath = (Button) this.findViewById(R.id.buttonClearFontFilePath);
		
		editTextEpwingPath = (EditText) this.findViewById(R.id.editTextEpwingPath);
		buttonOpenEpwingPath = (Button) this.findViewById(R.id.buttonOpenEpwingPath);
		buttonOpenREEpwingPath = (Button) this.findViewById(R.id.buttonOpenREEpwingPath);
		buttonClearEpwingPath = (Button) this.findViewById(R.id.buttonClearEpwingPath);
		
		checkBoxUseOpera = (CheckBox) this.findViewById(R.id.checkBoxUseOpera);
		checkBoxShowTalkIcon = (CheckBox) this.findViewById(R.id.checkBoxShowTalkIcon);
		checkBoxShowSenDlg = (CheckBox) this.findViewById(R.id.checkBoxShowSenDlg);
		checkBoxUseHiraganaExtra = (CheckBox) this.findViewById(R.id.checkBoxUseHiraganaExtra);
		checkBoxSearchMini = (CheckBox) this.findViewById(R.id.checkBoxSearchMini);
		checkBoxEnableDictService = (CheckBox) this.findViewById(R.id.checkBoxEnableDictService);
		checkBoxEnableSenService = (CheckBox) this.findViewById(R.id.checkBoxEnableSenService);
		checkBoxEnableAozoraService = (CheckBox) this.findViewById(R.id.checkBoxEnableAozoraService);
		checkBoxUseTTS = (CheckBox) this.findViewById(R.id.checkBoxUseTTS);
		checkBoxShowBanner = (CheckBox) this.findViewById(R.id.checkBoxShowBanner);
		checkBoxUseGrid = (CheckBox) this.findViewById(R.id.checkBoxUseGrid);
		checkBoxUseKeyPager = (CheckBox) this.findViewById(R.id.checkBoxUseKeyPager);
		checkBoxUseToolButtons = (CheckBox) this.findViewById(R.id.checkBoxUseToolButtons);
//		checkBoxUseRadicalInput = (CheckBox) this.findViewById(R.id.checkBoxUseRadicalInput);
		checkBoxShowSplashScreen = (CheckBox) this.findViewById(R.id.checkBoxShowSplashScreen);
		checkBoxJumpOldVersion = (CheckBox) this.findViewById(R.id.checkBoxJumpOldVersion);
		
		editTextBGFilePath = (EditText) this.findViewById(R.id.editTextBGFilePath);
		buttonOpenBGJPGFilePath = (Button) this.findViewById(R.id.buttonOpenBGJPGFilePath);
		buttonOpenBGPNGFilePath = (Button) this.findViewById(R.id.buttonOpenBGPNGFilePath);
		buttonOpenREBGFilePath = (Button) this.findViewById(R.id.buttonOpenREBGFilePath);
		buttonClearBGFilePath = (Button) this.findViewById(R.id.buttonClearBGFilePath);
		buttonKillDictService = (Button) this.findViewById(R.id.buttonKillDictService);
		buttonKillSenService = (Button) this.findViewById(R.id.buttonKillSenService);
		buttonKillAozoraService = (Button) this.findViewById(R.id.buttonKillAozoraService);
		
		buttonAozoraFontSize = (Button) this.findViewById(R.id.buttonAozoraFontSize);
		buttonAozoraDefaultFontSize = (Button) this.findViewById(R.id.buttonAozoraDefaultFontSize);
		
		buttonLoadGaijiDB = (Button) this.findViewById(R.id.buttonLoadGaijiDB);
		buttonClearGaijiDB = (Button) this.findViewById(R.id.buttonClearGaijiDB);
		buttonLoadREGaijiDB = (Button) this.findViewById(R.id.buttonLoadREGaijiDB);
		
		scrollView1 = (ScrollView) this.findViewById(R.id.scrollView1);
		textViewLoading = (TextView) this.findViewById(R.id.textViewLoading);
		
		radioButtonHLChar = (RadioButton) this.findViewById(R.id.radioButtonHLChar);
		radioButtonHLString = (RadioButton) this.findViewById(R.id.radioButtonHLString);
		radioButtonHLNone = (RadioButton) this.findViewById(R.id.radioButtonHLNone);
		
		buttonDeleteWebviewCache = (Button) this.findViewById(R.id.buttonDeleteWebviewCache);
		
		editTextDataPackPath = (EditText) this.findViewById(R.id.editTextDataPackPath);
		buttonDataPackPath1 = (Button) this.findViewById(R.id.buttonDataPackPath1);
		buttonDataPackPath2 = (Button) this.findViewById(R.id.buttonDataPackPath2);
		buttonDataPackPath3 = (Button) this.findViewById(R.id.buttonDataPackPath3);
		buttonDataPackPath4 = (Button) this.findViewById(R.id.buttonDataPackPath4);
		buttonDataPackPath5 = (Button) this.findViewById(R.id.buttonDataPackPath5);
		
		
		
		
		
		builder = new AlertDialog.Builder(this);
		builder2 = new AlertDialog.Builder(this);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		imageViewTop = (ImageView) findViewById(R.id.imageViewTop);
		actionBar.setTitle("全局设置");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				return R.drawable.config;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
		
		editTextFontFilePath.setText(getFontFileName(this));
		editTextEpwingPath.setText(getEpwingFileName(this));
		checkBoxUseOpera.setChecked(getUseOpera(this));
		checkBoxShowTalkIcon.setChecked(getShowTalkIcon(this));
		checkBoxShowSenDlg.setChecked(getShowSenDlg(this));
		checkBoxUseHiraganaExtra.setChecked(getUseHiraganaExtra(this));
		checkBoxSearchMini.setChecked(getSearchMini(this));
		editTextBGFilePath.setText(getBGFileName(this));
		checkBoxEnableDictService.setChecked(getDictService(this));
		checkBoxEnableSenService.setChecked(getSenService(this));
		checkBoxEnableAozoraService.setChecked(getAozoraService(this));
		checkBoxUseTTS.setChecked(getUseTTS(this));
		checkBoxShowBanner.setChecked(getShowBanner(this));
		checkBoxUseGrid.setChecked(getUseGrid(this));
		checkBoxUseKeyPager.setChecked(getUseKeyPager(this));
		checkBoxUseToolButtons.setChecked(getUseToolButtons(this));
//		checkBoxUseRadicalInput.setChecked(getUseRadicalInput(this));
		checkBoxShowSplashScreen.setChecked(getShowSplashScreen(this));
		checkBoxJumpOldVersion.setChecked(getJumpOldVersion(this));
		editTextDataPackPath.setText(getDataPackPath(this));
		
		
		switch (getHLType(this)) {
		default:
		case HL_TYPE_CHAR:
			radioButtonHLChar.setChecked(true);
			break;
			
		case HL_TYPE_STRING:
			radioButtonHLString.setChecked(true);
			break;
		
		case HL_TYPE_NONE:
			radioButtonHLNone.setChecked(true);
			break;
		}
		
		buttonOpenFontFilePath.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						JkanjiSettingActivity.this, DirBrowser.class);
				intent.putExtra(DirBrowser.EXTRA_KEY_SUFFIX, ".ttf");
				startActivityForResult(intent, REQUEST_TTF_PATH);
			}
		});
		
		buttonOpenREFontFilePath.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.setType("*/*");
				try {
					startActivityForResult(intent, REQUEST_RE_TTF_PATH);
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(JkanjiSettingActivity.this, 
						"找不到可用的应用程序", Toast.LENGTH_SHORT)
						.show();
				}
			}
		});
		
		buttonClearFontFilePath.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editTextFontFilePath.setText("");
				setFontFileName(JkanjiSettingActivity.this, "");
			}
		});
		
		
		buttonOpenEpwingPath.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						JkanjiSettingActivity.this, DirBrowser.class);
				intent.putExtra(DirBrowser.EXTRA_KEY_SUFFIX, "Catalogs");
				startActivityForResult(intent, REQUEST_EPWING_PATH);
			}
		});
		
		buttonOpenREEpwingPath.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.setType("*/*");
				try {
					startActivityForResult(intent, REQUEST_RE_EPWING_PATH);
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(JkanjiSettingActivity.this, 
						"找不到可用的应用程序", Toast.LENGTH_SHORT)
						.show();
				}
			}
		});
		
		buttonClearEpwingPath.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editTextEpwingPath.setText("");
				setEpwingFileName(JkanjiSettingActivity.this, "");
			}
		});
		
		checkBoxUseOpera.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setUseOpera(JkanjiSettingActivity.this, isChecked);
			}
		});

		checkBoxShowTalkIcon.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setShowTalkIcon(JkanjiSettingActivity.this, isChecked);
			}
		});

		checkBoxShowSenDlg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setShowSenDlg(JkanjiSettingActivity.this, isChecked);
			}
		});
		checkBoxUseHiraganaExtra.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setUseHiraganaExtra(JkanjiSettingActivity.this, isChecked);
			}
		});
		checkBoxSearchMini.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setSearchMini(JkanjiSettingActivity.this, isChecked);
			}
		});
		checkBoxEnableDictService.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setDictService(JkanjiSettingActivity.this, isChecked);
			}
		});
		checkBoxEnableSenService.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setSenService(JkanjiSettingActivity.this, isChecked);
			}
		});
		checkBoxEnableAozoraService.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setAozoraService(JkanjiSettingActivity.this, isChecked);
			}
		});
		
		checkBoxUseTTS.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setUseTTS(JkanjiSettingActivity.this, isChecked);
			}
		});
		
		checkBoxShowBanner.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setShowBanner(JkanjiSettingActivity.this, isChecked);
			}
		});
		
		checkBoxUseGrid.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setUseGrid(JkanjiSettingActivity.this, isChecked);
			}
		});

		buttonOpenBGJPGFilePath.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						JkanjiSettingActivity.this, DirBrowser.class);
				intent.putExtra(DirBrowser.EXTRA_KEY_SUFFIX, ".jpg");
				startActivityForResult(intent, REQUEST_BG_JPG_PATH);
			}
		});

		buttonOpenBGPNGFilePath.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						JkanjiSettingActivity.this, DirBrowser.class);
				intent.putExtra(DirBrowser.EXTRA_KEY_SUFFIX, ".png");
				startActivityForResult(intent, REQUEST_BG_PNG_PATH);
			}
		});
		
		buttonOpenREBGFilePath.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				try {
					startActivityForResult(intent, REQUEST_RE_BG_PATH);
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(JkanjiSettingActivity.this, 
						"找不到可用的应用程序", Toast.LENGTH_SHORT)
						.show();
				}
			}
		});
		
		buttonClearBGFilePath.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editTextBGFilePath.setText("");
				setBGFileName(JkanjiSettingActivity.this, "");
				File file = new File(JkanjiSettingActivity.getBGFileName(JkanjiSettingActivity.this));
				if (file.canRead() && file.exists() && file.isFile()) {
					imageViewTop.setImageURI(Uri.fromFile(file));
				} else {
					imageViewTop.setImageURI(null);
				}
			}
		});

		
		buttonKillDictService.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				JkanjiSettingActivity.this.startService(
						new Intent(JkanjiSettingActivity.this, JkanjiDictService.class)
							.setAction(JkanjiDictService.ACTION_STOP));
			}
		});
		buttonKillSenService.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				JkanjiSettingActivity.this.startService(
						new Intent(JkanjiSettingActivity.this, JkanjiSenService.class)
							.setAction(JkanjiSenService.ACTION_STOP));
			}
		});
		buttonKillAozoraService.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				JkanjiSettingActivity.this.startService(
						new Intent(JkanjiSettingActivity.this, JkanjiAozoraService.class)
							.setAction(JkanjiAozoraService.ACTION_STOP));
			}
		});
		
		buttonAozoraFontSize.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_TEXT_ENTRY);
			}
		});
		buttonAozoraDefaultFontSize.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setRbSize(JkanjiSettingActivity.this, BookInfoUtils.DEFAULT_RB_SIZE);
        		setRtSize(JkanjiSettingActivity.this, BookInfoUtils.DEFAULT_RT_SIZE);
        		setSpaceSize(JkanjiSettingActivity.this, BookInfoUtils.DEFAULT_SPACE_SIZE);
        		Toast.makeText(JkanjiSettingActivity.this, "还原默认字体大小", Toast.LENGTH_SHORT).show();
			}
		});
		
		buttonLoadGaijiDB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						JkanjiSettingActivity.this, DirBrowser.class);
				intent.putExtra(DirBrowser.EXTRA_KEY_SUFFIX, ".map");
				startActivityForResult(intent, REQUEST_GAIJI_MAP_PATH);
			}
		});
		buttonLoadREGaijiDB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.setType("*/*");
				try {
					startActivityForResult(intent, REQUEST_RE_GAIJI_MAP_PATH);
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(JkanjiSettingActivity.this, 
						"找不到可用的应用程序", Toast.LENGTH_SHORT)
						.show();
				}
			}
		});
		buttonClearGaijiDB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		    	GaijiDataSource dataSrc = new GaijiDataSource(JkanjiSettingActivity.this);
		    	dataSrc.open();
		    	dataSrc.deleteAllItem();
		    	dataSrc.close();
		    	Toast.makeText(JkanjiSettingActivity.this, 
						"清空外字数据库完成", Toast.LENGTH_SHORT).show();
			}
		});
		
		checkBoxUseKeyPager.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setUseKeyPager(JkanjiSettingActivity.this, isChecked);
			}
		});

		checkBoxUseToolButtons.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setUseToolButtons(JkanjiSettingActivity.this, isChecked);
			}
		});
		
//		checkBoxUseRadicalInput.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				setUseRadicalInput(JkanjiSettingActivity.this, isChecked);
//			}
//		});
		
		checkBoxShowSplashScreen.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setShowSplashScreen(JkanjiSettingActivity.this, isChecked);
			}
		});
		
		checkBoxJumpOldVersion.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setJumpOldVersion(JkanjiSettingActivity.this, isChecked);
			}
		});
		
		radioButtonHLChar.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setHLType(JkanjiSettingActivity.this, HL_TYPE_CHAR);
				}
			}
		});
		radioButtonHLString.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setHLType(JkanjiSettingActivity.this, HL_TYPE_STRING);
				}
			}
		});
		radioButtonHLNone.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setHLType(JkanjiSettingActivity.this, HL_TYPE_NONE);
				}
			}
		});
		
		buttonDeleteWebviewCache.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/**
				 * @see http://androiddada.iteye.com/blog/1280946
				 */
		    	try {
					deleteDatabase("webview.db");    
					deleteDatabase("webviewCache.db");
					Toast.makeText(JkanjiSettingActivity.this, 
						"删除网页缓存成功", Toast.LENGTH_SHORT).show();
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(JkanjiSettingActivity.this, 
						"删除网页缓存失败", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		
		
		
		
		buttonDataPackPath1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setDataPackPath(JkanjiSettingActivity.this, "/mnt/sdcard/jkanji");
				editTextDataPackPath.setText(getDataPackPath(JkanjiSettingActivity.this));
			}
		});
		buttonDataPackPath2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setDataPackPath(JkanjiSettingActivity.this, "/mnt/sdcard2/jkanji");
				editTextDataPackPath.setText(getDataPackPath(JkanjiSettingActivity.this));
			}
		});
		buttonDataPackPath3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setDataPackPath(JkanjiSettingActivity.this, "/storage/sdcard0/jkanji");
				editTextDataPackPath.setText(getDataPackPath(JkanjiSettingActivity.this));
			}
		});
		buttonDataPackPath4.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setDataPackPath(JkanjiSettingActivity.this, "/storage/sdcard1/jkanji");
				editTextDataPackPath.setText(getDataPackPath(JkanjiSettingActivity.this));
			}
		});
		buttonDataPackPath5.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_TEXT_ENTRY2);
			}
		});
	}

	public static void setFontFileName(Context context, String filename) {
    	PrefUtil.putString(context, SHARE_PREF_NAME, 
    			SHARE_PREF_FONT_FILENAME, 
    			filename);
    }
    
    public static String getFontFileName(Context context) {
		return PrefUtil.getString(context, SHARE_PREF_NAME, 
				SHARE_PREF_FONT_FILENAME, 
				"");
    }
    
    public static void setEpwingFileName(Context context, String filename) {
    	PrefUtil.putString(context, SHARE_PREF_NAME, 
    			SHARE_PREF_EPWING_FILENAME, 
    			filename);
    }
    
    public static String getEpwingFileName(Context context) {
		return PrefUtil.getString(context, SHARE_PREF_NAME, 
				SHARE_PREF_EPWING_FILENAME, 
				"");
    }

    public static void setUseOpera(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME, 
    			SHARE_PREF_USE_OPERA, 
    			enable);
    }
    
    public static boolean getUseOpera(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME, 
				SHARE_PREF_USE_OPERA, 
				false);
    }

    public static void setShowTalkIcon(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME, 
    			SHARE_PREF_SHOW_TALK_ICON, 
    			enable);
    }
    
    public static boolean getShowTalkIcon(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME, 
				SHARE_PREF_SHOW_TALK_ICON, 
				true);
    }

    public static void setShowSenDlg(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME, 
    			SHARE_PREF_SHOW_SEN_DLG, 
    			enable);
    }
    
    public static boolean getShowSenDlg(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME, 
				SHARE_PREF_SHOW_SEN_DLG, 
				true);
    }

    public static void setUseHiraganaExtra(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME, 
    			SHARE_PREF_USE_HIRAGANA_EXTRA, 
    			enable);
    }
    
    public static boolean getUseHiraganaExtra(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME, 
				SHARE_PREF_USE_HIRAGANA_EXTRA, 
				true);
    }
    
    public static void setSearchMini(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME, 
    			SHARE_PREF_SEARCH_MINI, 
    			enable);
    }
    
    public static boolean getSearchMini(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME, 
				SHARE_PREF_SEARCH_MINI, 
				false);
    }

    public static void setBGFileName(Context context, String filename) {
    	PrefUtil.putString(context, SHARE_PREF_NAME, 
    			SHARE_PREF_BG_FILENAME, 
    			filename);
    }
    
    public static String getBGFileName(Context context) {
		return PrefUtil.getString(context, SHARE_PREF_NAME, 
				SHARE_PREF_BG_FILENAME, 
				"");
    }
    
    public static void setDictService(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME, 
    			SHARE_PREF_DICT_SERVICE, 
    			enable);
    }
    
    public static boolean getDictService(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME, 
				SHARE_PREF_DICT_SERVICE, 
				false);
    }
    
    public static void setSenService(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME, 
    			SHARE_PREF_SEN_SERVICE, 
    			enable);
    }
    
    public static boolean getSenService(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME, 
				SHARE_PREF_SEN_SERVICE, 
				false);
    }    
    
    public static void setAozoraService(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME, 
    			SHARE_PREF_AOZORA_SERVICE, 
    			enable);
    }
    
    public static boolean getAozoraService(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME, 
				SHARE_PREF_AOZORA_SERVICE, 
				false);
    }
    
    public static void setUseTTS(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME, 
    			SHARE_PREF_USE_TTS, 
    			enable);
    }
    
    public static boolean getUseTTS(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME, 
				SHARE_PREF_USE_TTS, 
				true);
    }

    public static void setShowBanner(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME, 
    			SHARE_PREF_SHOW_BANNER, 
    			enable);
    }
    
    public static boolean getShowBanner(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME, 
				SHARE_PREF_SHOW_BANNER, 
				true);
    }
    
    public static void setUseGrid(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME, 
    			SHARE_PREF_USE_GRID, 
    			enable);
    }
    
    public static boolean getUseGrid(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME, 
				SHARE_PREF_USE_GRID, 
				false);
    }

    public static void setRbSize(Context context, int value) {
    	PrefUtil.putInt(context, SHARE_PREF_NAME, 
    			SHARE_PREF_RB_SIZE, 
    			value);
    }
    
    public static int getRbSize(Context context) {
		return PrefUtil.getInt(context, SHARE_PREF_NAME, 
				SHARE_PREF_RB_SIZE, 
				BookInfoUtils.DEFAULT_RB_SIZE);
    }
    
    public static void setRtSize(Context context, int value) {
    	PrefUtil.putInt(context, SHARE_PREF_NAME, 
    			SHARE_PREF_RT_SIZE, 
    			value);
    }
    
    public static int getRtSize(Context context) {
		return PrefUtil.getInt(context, SHARE_PREF_NAME, 
				SHARE_PREF_RT_SIZE, 
				BookInfoUtils.DEFAULT_RT_SIZE);
    }
    
    public static void setSpaceSize(Context context, int value) {
    	PrefUtil.putInt(context, SHARE_PREF_NAME, 
    			SHARE_PREF_SPACE_SIZE, 
    			value);
    }
    
    public static int getSpaceSize(Context context) {
		return PrefUtil.getInt(context, SHARE_PREF_NAME, 
				SHARE_PREF_SPACE_SIZE, 
				BookInfoUtils.DEFAULT_SPACE_SIZE);
    }
    
    public static void setUseKeyPager(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME, 
				SHARE_PREF_USE_KEY_PAGER, 
    			enable);
    }
    
    public static boolean getUseKeyPager(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME, 
	    		SHARE_PREF_USE_KEY_PAGER, 
	        	true);
    }

    public static void setHLType(Context context, int value) {
    	PrefUtil.putInt(context, SHARE_PREF_NAME, 
    			SHARE_PREF_HL_TYPE, 
    			value);
    }
    
    public static int getHLType(Context context) {
		return PrefUtil.getInt(context, SHARE_PREF_NAME, 
				SHARE_PREF_HL_TYPE, 
				HL_TYPE_CHAR);
    }

    public static void setUseToolButtons(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME, 
    			SHARE_PREF_USE_TOOL_BUTTONS, 
    			enable);
    }
    
    public static boolean getUseToolButtons(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME, 
				SHARE_PREF_USE_TOOL_BUTTONS, 
        		true);
    }

    private static void setUseRadicalInput(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME, 
    			SHARE_PREF_USE_RADICAL_INPUT, 
    			enable);
    }
    
    private static boolean getUseRadicalInput(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME, 
    			SHARE_PREF_USE_RADICAL_INPUT, 
        		false);
    }

    public static void setShowSplashScreen(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME, 
    			SHARE_PREF_SHOW_SPLASH_SCREEN, 
    			enable);
    }
    
    public static boolean getShowSplashScreen(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME, 
				SHARE_PREF_SHOW_SPLASH_SCREEN, 
        		true);
    }
    
    public static void setJumpOldVersion(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME, 
    			SHARE_PREF_JUMP_OLD_VERSION, 
    			enable);
    }
    
    public static boolean getJumpOldVersion(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME, 
				SHARE_PREF_JUMP_OLD_VERSION, 
        		true);
    }
    
	public static void setDataPackPath(Context context, String filename) {
    	PrefUtil.putString(context, SHARE_PREF_NAME, 
    			SHARE_PREF_DATA_PACK_PATH, 
    			filename);
    }
    
    public static String getDataPackPath(Context context) {
    	return PrefUtil.getString(context, SHARE_PREF_NAME, 
    			SHARE_PREF_DATA_PACK_PATH, 
    			"/mnt/sdcard/jkanji");
    }
        
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_TTF_PATH:
			if (data != null) {
				String resultPath = data.getStringExtra(DirBrowser.EXTRA_KEY_RESULT_PATH);
				this.editTextFontFilePath.setText(resultPath);
				setFontFileName(this, resultPath);
			}
			break;
			
		case REQUEST_EPWING_PATH:
			if (data != null) {
				String resultPath = data.getStringExtra(DirBrowser.EXTRA_KEY_RESULT_PATH);
				this.editTextEpwingPath.setText(resultPath);
				setEpwingFileName(this, resultPath);
			}
			break; 
			
		case REQUEST_RE_TTF_PATH:
			if (resultCode == RESULT_OK && 
				data != null &&
				data.getData() != null) {
				String resultPath = data.getData().getPath();
				this.editTextFontFilePath.setText(resultPath);
				setFontFileName(this, resultPath);
			}
			break;
			
		case REQUEST_RE_EPWING_PATH:
			if (resultCode == RESULT_OK && 
				data != null &&
				data.getData() != null) {
				String resultPath = data.getData().getPath();
				this.editTextEpwingPath.setText(resultPath);
				setEpwingFileName(this, resultPath);
			}
			break;
			
		case REQUEST_BG_JPG_PATH:	
		case REQUEST_BG_PNG_PATH:
			if (data != null) {
				String resultPath = data.getStringExtra(DirBrowser.EXTRA_KEY_RESULT_PATH);
				this.editTextBGFilePath.setText(resultPath);
				setBGFileName(this, resultPath);
			}
			break;
		
		case REQUEST_RE_BG_PATH:
			if (resultCode == RESULT_OK && 
				data != null &&
				data.getData() != null) {
				String resultPath = data.getData().getPath();
				this.editTextBGFilePath.setText(resultPath);
				setBGFileName(this, resultPath);
			}
			break;

		case REQUEST_GAIJI_MAP_PATH:	
			if (data != null) {
				String resultPath = data.getStringExtra(DirBrowser.EXTRA_KEY_RESULT_PATH);
				new LoadGaijiTask().execute(resultPath);
			}
			break;
			
		case REQUEST_RE_GAIJI_MAP_PATH:
			if (resultCode == RESULT_OK && 
				data != null &&
				data.getData() != null) {
				String resultPath = data.getData().getPath();
				new LoadGaijiTask().execute(resultPath);
			}
			break;
		}
	}
	
    @Override
	protected void onResume() {
		super.onResume();
		File file = new File(JkanjiSettingActivity.getBGFileName(JkanjiSettingActivity.this));
		if (file.canRead() && file.exists() && file.isFile()) {
			imageViewTop.setImageURI(Uri.fromFile(file));
		} else {
			imageViewTop.setImageURI(null);
		}
	}
    
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_TEXT_ENTRY:
		{
			LayoutInflater factory = LayoutInflater.from(this);
            View textEntryView = factory.inflate(R.layout.set_font_size_dialog, null);
            editTextRbSize = (EditText) textEntryView.findViewById(R.id.editTextRbSize);
            editTextRtSize = (EditText) textEntryView.findViewById(R.id.editTextRtSize);
            editTextSpaceSize = (EditText) textEntryView.findViewById(R.id.editTextSpaceSize);
            fontSizeDialog = builder
            	.setTitle("设置阅读器字体")
                .setView(textEntryView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	try {
                    		String str;
                    		str = editTextRbSize.getText().toString();
                    		setRbSize(JkanjiSettingActivity.this, Integer.parseInt(str));
                    		str = editTextRtSize.getText().toString();
                    		setRtSize(JkanjiSettingActivity.this, Integer.parseInt(str));
                    		str = editTextSpaceSize.getText().toString();
                    		setSpaceSize(JkanjiSettingActivity.this, Integer.parseInt(str));
                    	} catch (Throwable e) {
                    		e.printStackTrace();
                    	}
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	
                    }
                })
                .create();
            fontSizeDialog.setOnShowListener(new OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					editTextRbSize.setText("");
					editTextRbSize.append(Integer.toString(getRbSize(JkanjiSettingActivity.this)));
					editTextRtSize.setText("");
					editTextRtSize.append(Integer.toString(getRtSize(JkanjiSettingActivity.this)));
					editTextSpaceSize.setText("");
					editTextSpaceSize.append(Integer.toString(getSpaceSize(JkanjiSettingActivity.this)));
				}
            });
            return fontSizeDialog;
		}            
            
		case DIALOG_TEXT_ENTRY2:
		{
			LayoutInflater factory = LayoutInflater.from(this);
            View textEntryView = factory.inflate(R.layout.set_data_pack_path_dialog, null);
            editTextDataPackInput = (EditText) textEntryView.findViewById(R.id.editTextDataPackInput);
            dataPackPathDialog = builder2
            	.setTitle("设置数据包目录")
                .setView(textEntryView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	String str;
                    	str = editTextDataPackInput.getText().toString();
                    	setDataPackPath(JkanjiSettingActivity.this, str);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	
                    }
                })
                .create();
            dataPackPathDialog.setOnShowListener(new OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					editTextDataPackInput.setText("");
					editTextDataPackInput.append(getDataPackPath(JkanjiSettingActivity.this));
				}
            });
            return dataPackPathDialog;
		}
		}
		return super.onCreateDialog(id);
	}
	
	private class LoadGaijiTask extends AsyncTask<String, Void, Boolean> {
		private boolean loadResult = false;
		private int loadNum = 0;
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			textViewLoading.setVisibility(View.VISIBLE);
			scrollView1.setVisibility(View.INVISIBLE);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
		    	GaijiDataSource dataSrc = new GaijiDataSource(JkanjiSettingActivity.this);
		    	dataSrc.open();
		    	dataSrc.deleteAllItem();
		    	loadNum = GaijiMapParser.parse(params[0], dataSrc);
		    	dataSrc.close();				
				loadResult = true;
			} catch (Throwable e) {
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			textViewLoading.setVisibility(View.INVISIBLE);
			scrollView1.setVisibility(View.VISIBLE);
			if (result == true && !JkanjiSettingActivity.this.isFinishing()) {
				if (loadResult) {
					Toast.makeText(JkanjiSettingActivity.this, 
						"加载外字map文件成功:" + loadNum, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(JkanjiSettingActivity.this, 
						"加载外字map文件失败", Toast.LENGTH_SHORT).show();
				}
			} else if (result == false) {
				finish();
			}
		}
    }
}
