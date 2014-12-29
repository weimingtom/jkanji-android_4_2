package fi.harism.curl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnShowListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.elgubbo.sharetoclipboard.ShareToClipboardActivity;
import com.iteye.weimingtom.jkanji.AozoraParser;
import com.iteye.weimingtom.jkanji.BookDrawTextUtil;
import com.iteye.weimingtom.jkanji.DictWebListActivity;
import com.iteye.weimingtom.jkanji.JKanjiActivity;
import com.iteye.weimingtom.jkanji.JkanjiBookIndex;
import com.iteye.weimingtom.jkanji.JkanjiSettingActivity;
import com.iteye.weimingtom.jkanji.JkanjiShelfHistoryDataSource;
import com.iteye.weimingtom.jkanji.JkanjiShelfHistoryItem;
import com.iteye.weimingtom.jkanji.JkanjiShelfPlainReaderActivity;
import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.jkanji.SQLiteReaderActivity;
import com.iteye.weimingtom.jkanji.SimplePageView;
import com.iteye.weimingtom.jkanji.Typefaces;
import com.iteye.weimingtom.jkanji.AozoraParser.RubyInfo;

public class BookLoader {
	private final static boolean D = false;
	private final static String TAG = "BookLoader";
	
	private Activity activity;
	
	public String fileName;
	public boolean isFullscreen;
	public boolean isEnableSen;
	public boolean hasActionbar;
	public int page;
	public int curlType;
	public String codePage;
	public String bgFileName;
	public boolean reverseDirection;
	public int parserType;
	public int dialogCheckPosition;
	public boolean isVertical;
	public boolean maskBG;
	public int position;
	public boolean basePage;
	public int screenOri;
	public boolean blackBack;
	public boolean useVolumeKey;
	
	//FIXME:
	public long curHistoryId = -1L; //see CurlActivity.onRetainNonConfigurationInstance
	public int curLength = 0; //see CurlActivity.onRetainNonConfigurationInstance
	
	public BookDrawTextUtil textUtil;
	public String textReader;
	
	
	public AozoraParser parser;
	public ArrayList<RubyInfo> rubyInfoList;
		
	public int totalPage;
	public int curPage; //see CurlActivity.onRetainNonConfigurationInstance
	public int curPosition; //see CurlActivity.onRetainNonConfigurationInstance
	public String curDesc; //see CurlActivity.onRetainNonConfigurationInstance
	
	public ArrayAdapter<String> singleChoiceAdapter;
	public boolean isListSearching = false;
	
	public Bitmap bgBitmap;
	public Matrix bgMatrix;
	public Paint bgPaint;
	public Paint bgRectPaint;
	
	public boolean isFastTap = false;
	
	public Typeface typeface;
	private AlertDialog dialog1, dialog2;
	private AlertDialog.Builder builder1, builder2;
	
	private AlertDialog pageDialog;
	private TextView textViewInfo;
	private EditText editPage;
	
	public boolean isProgress = true;
	
	private JkanjiShelfHistoryDataSource dataSrc;
	
	public void onCreate(Activity activity) {
		this.activity = activity;
		System.setProperty("sen.home", JkanjiSettingActivity.getDataPackPath(activity) + BookInfoUtils.SEN_HOME);
		
		Intent intent = activity.getIntent();
		if (intent != null) {
			isFullscreen = intent.getBooleanExtra(BookInfoUtils.EXTRA_KEY_IS_FULL_SCREEN, false);
			BookInfoUtils.setLastIsFullScreen(activity, isFullscreen);
			
			fileName = intent.getStringExtra(BookInfoUtils.EXTRA_KEY_FILE_NAME);
			BookInfoUtils.setLastFileName(activity, fileName);
			
			isEnableSen = intent.getBooleanExtra(BookInfoUtils.EXTRA_KEY_ENABLE_SEN, false);
			BookInfoUtils.setLastEnableSen(activity, isEnableSen);
			
			hasActionbar = intent.getBooleanExtra(BookInfoUtils.EXTRA_KEY_HAS_ACTIONBAR, false);
			BookInfoUtils.setLastHasActionbar(activity, hasActionbar);
			
			page = intent.getIntExtra(BookInfoUtils.EXTRA_KEY_PAGE, 0);
			
			curlType = intent.getIntExtra(BookInfoUtils.EXTRA_KEY_CURL_TYPE, BookInfoUtils.CURL_TYPE_CURL);
			BookInfoUtils.setLastCurlType(activity, curlType);
			
			codePage = intent.getStringExtra(BookInfoUtils.EXTRA_KEY_CODEPAGE);
			bgFileName = intent.getStringExtra(BookInfoUtils.EXTRA_KEY_BG_FILE_NAME);

			reverseDirection = intent.getBooleanExtra(BookInfoUtils.EXTRA_KEY_REVERSE_DIRECTION, false);
			BookInfoUtils.setLastReverseDirection(activity, reverseDirection);
			
			parserType = intent.getIntExtra(BookInfoUtils.EXTRA_KEY_PARSER_TYPE, BookInfoUtils.PARSER_TYPE_AOZORA);
			BookInfoUtils.setLastParserType(activity, parserType);

			isVertical = intent.getBooleanExtra(BookInfoUtils.EXTRA_KEY_IS_VERTICAL, false);
			BookInfoUtils.setLastIsVertical(activity, isVertical);
			
			maskBG = intent.getBooleanExtra(BookInfoUtils.EXTRA_KEY_MASK_BG, true);
			BookInfoUtils.setLastMaskBG(activity, maskBG);
			
			position = intent.getIntExtra(BookInfoUtils.EXTRA_KEY_POSITION, 0);
			BookInfoUtils.setLastPosition(activity, position);
			
			basePage = intent.getBooleanExtra(BookInfoUtils.EXTRA_KEY_BASE_PAGE, true);
			BookInfoUtils.setLastBasePage(activity, basePage);
			
			screenOri = intent.getIntExtra(BookInfoUtils.EXTRA_KEY_SCREEN_ORI, BookInfoUtils.SCREEN_ORI_UNDEFINED);
			BookInfoUtils.setLastScreenOri(activity, screenOri);
			
			blackBack = intent.getBooleanExtra(BookInfoUtils.EXTRA_KEY_BLACK_BACK, false);
			BookInfoUtils.setLastBlackBack(activity, blackBack);

			useVolumeKey = intent.getBooleanExtra(BookInfoUtils.EXTRA_KEY_USE_VOLUME_KEY, true);
			BookInfoUtils.setLastUseVolumeKey(activity, useVolumeKey);
			
			curDesc = intent.getStringExtra(BookInfoUtils.EXTRA_KEY_DESC);
			
			curHistoryId = intent.getLongExtra(BookInfoUtils.EXTRA_KEY_HISTORY_ID, -1L);
		}
		if (codePage == null) {
			codePage = "shift-jis";
		}
		BookInfoUtils.setLastCodePage(activity, codePage);
		if (bgFileName == null) {
			bgFileName = "";
		}
		BookInfoUtils.setLastBGFileName(activity, bgFileName);
		hideTitle();
		
		
		bgMatrix = new Matrix();
		bgPaint = new Paint();
		bgPaint.setAntiAlias(true);
		bgPaint.setFilterBitmap(true);
		bgRectPaint = new Paint();
		if (blackBack) {
			bgRectPaint.setColor(Color.BLACK);
		} else {
			bgRectPaint.setColor(Color.WHITE);
		}
		bgRectPaint.setAlpha(125);
		
		singleChoiceAdapter = new ArrayAdapter<String>(activity, 
        		android.R.layout.select_dialog_singlechoice);
		
		builder1 = new AlertDialog.Builder(activity);
		builder2 = new AlertDialog.Builder(activity);
		typeface = Typefaces.getFile(JkanjiSettingActivity.getFontFileName(activity));
	
		dataSrc = new JkanjiShelfHistoryDataSource(activity);
		dataSrc.open();
	}
	
    private void hideTitle() {
    	if (isFullscreen) {
    		this.activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	}
    	this.activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
    
    public void checkDialogPos(int pos) {
		if (D) {
			Log.d(TAG, "checkDialogPos : " + pos);
		}
		this.dialogCheckPosition = pos;
    }
    
    public void onPositionLine(float x, float y) {
		if (this.isEnableSen) {
			this.textUtil.setPage(this.curPage);
			int positionLine = this.textUtil.getPositionLine(x, y);
			int lineFirstPos = this.textUtil.getLineFirstPos(positionLine);
			int lineLastPos = this.textUtil.getLineLastPos(positionLine);
			int pageFirstPos = this.textUtil.getPageFirstPos();
			int pageLastPos = this.textUtil.getPageLastPos();
			int indexChar = this.textUtil.getTextIndexOnPosXY(lineFirstPos, lineLastPos, x - BookInfoUtils.getMargin(activity), y - BookInfoUtils.getMargin(activity));
			
			if (D) {
				Log.d(TAG, "onPositionLine : " + positionLine + " f:" + lineFirstPos + ", e:" + lineLastPos);
				Log.d(TAG, "indexChar : " + indexChar);
			}
			if (lineFirstPos >= 0 && lineLastPos >= 0) {
				BookDrawTextUtil.DrawTextInfo drawtextinfo = this.textUtil.getDrawText();
				if (drawtextinfo != null) {
					if (BookInfoUtils.USE_CONTENT_PROVIDER) {
						singleChoiceAdapter.clear();
						int[] posIndex = new int[1];
						posIndex[0] = -1;
						ArrayList<String> basicStrings = BookInfoUtils.getBasicStrings(activity, 
								drawtextinfo.str, drawtextinfo.startPos, 
								pageFirstPos, pageLastPos, 
								lineFirstPos, lineLastPos,
								indexChar, posIndex);
						Iterator<String> iter = basicStrings.iterator();
						while (iter.hasNext()) {
							String bs = iter.next();
							singleChoiceAdapter.add(bs);
						}
						if (posIndex[0] >= 0) {
							checkDialogPos(posIndex[0]);
						} else {
							checkDialogPos(0);
						}
						activity.showDialog(BookInfoUtils.DIALOG_SINGLE_CHOICE);
					}
				}
			}
		} else {
			boolean hasJapanLocal = false;
			Locale[] locales = BreakIterator.getAvailableLocales();
			if (locales != null) {
				for (Locale locale : locales) {
					if (D) {
						Log.d(TAG, "locale: " + locale.toString());
					}
					if (locale != null && locale.equals(Locale.JAPAN)) {
						hasJapanLocal = true;
						break;
					}
				}
			}
			if (D) {
				Log.d(TAG, "hasJapanLocal " + hasJapanLocal);
			}
			if (hasJapanLocal) {
				this.textUtil.setPage(this.curPage);
				int positionLine = this.textUtil.getPositionLine(x, y);
				int lineFirstPos = this.textUtil.getLineFirstPos(positionLine);
				int lineLastPos = this.textUtil.getLineLastPos(positionLine);
				//int pageFirstPos = this.textUtil.getPageFirstPos();
				//int pageLastPos = this.textUtil.getPageLastPos();
				int indexChar = this.textUtil.getTextIndexOnPosXY(lineFirstPos, lineLastPos, x - BookInfoUtils.getMargin(activity), y - BookInfoUtils.getMargin(activity));
				
				if (D) {
					Log.d(TAG, "onPositionLine : " + positionLine + " f:" + lineFirstPos + ", e:" + lineLastPos);
					Log.d(TAG, "indexChar : " + indexChar);
				}
				
				if (lineFirstPos >= 0 && lineLastPos >= 0) {
					BookDrawTextUtil.DrawTextInfo drawtextinfo = this.textUtil.getDrawText();
					if (drawtextinfo != null && drawtextinfo.str != null) {
						singleChoiceAdapter.clear();
				        BreakIterator boundary = BreakIterator.getWordInstance(Locale.JAPAN);
				        boundary.setText(drawtextinfo.str);
				        int start = boundary.first();
				        int posIndex = -1;
				        boolean posFound = false;
				        for (int end = boundary.next(); 
				        	end != BreakIterator.DONE; 
				        	start = end, end = boundary.next()) {
				        	String bs = drawtextinfo.str.substring(start, end);
				        	if (end > lineFirstPos - drawtextinfo.startPos && 
				        		start < lineLastPos - drawtextinfo.startPos) {
				        		singleChoiceAdapter.add(bs);
				        		if (posFound == false && 
				        			indexChar >= 0 &&
				        			drawtextinfo.startPos + start >= indexChar) {
				        			posIndex = singleChoiceAdapter.getCount() - 1;
				        			posFound = true;
				        		}
				        	}
				        }
				        if (D) {
				        	Log.d(TAG, "posIndex : " + posIndex);
				        }
				        //FIXME: some wrongs happen when first opening the dialog
				        //I don't know why and how to fix this bug.
				        if (posIndex >= 0) {
							checkDialogPos(posIndex);
						} else {
							checkDialogPos(0);
						}
				        activity.showDialog(BookInfoUtils.DIALOG_SINGLE_CHOICE_ICU4C);
					}
				}
			} else {
				//Toast.makeText(this, "尚未启用Sen分词", Toast.LENGTH_SHORT).show();
				Toast.makeText(activity, "Sen尚未启用，icu4c日文分词无法启用", Toast.LENGTH_SHORT).show();
			}
		}
    }
    
    public void drawBG(Canvas canvas, float w, float h) {
    	float bgScale;
    	if (bgBitmap != null) {
			if (w < h) {
		    	if (w / h > (float)bgBitmap.getWidth() / bgBitmap.getHeight()) {
		    		bgScale = w / (float)bgBitmap.getWidth();
		    	} else {
		    		bgScale = h / (float)bgBitmap.getHeight();
		    	}
			} else {
		    	if (w / h < (float)bgBitmap.getWidth() / bgBitmap.getHeight()) {
		    		bgScale = h / (float)bgBitmap.getHeight();
		    	} else {
		    		bgScale = w / (float)bgBitmap.getWidth();
		    	}    			
			}
	    	bgMatrix.setScale(bgScale, bgScale);
	    	canvas.drawBitmap(bgBitmap, bgMatrix, bgPaint);
	    	if (this.maskBG) {
	    		canvas.drawRect(0, 0, w, h, bgRectPaint);
	    	}
    	}
    }
    
    public int positionToPage(int pos) {
    	if (this.textUtil != null) {
    		return this.textUtil.positionToPage(pos);
    	} else {
    		return 1;
    	}
    }
    
	public void loadData() {
		if (D) {
			Log.d(TAG, "fileName " + (this.fileName != null ? this.fileName : ""));
		}
		if (this.fileName != null) {
			FileInputStream inputStream = null;
			try {
				if (this.fileName.toLowerCase().endsWith(".epub")) {
					if (this.parserType == BookInfoUtils.PARSER_TYPE_PLAIN) {
						parser = new AozoraParser();
						String inputstr = openEpub(this.fileName);
						parser.openText(inputstr);
						this.textReader = parser.getLoadedText();
						rubyInfoList = parser.getRubyList();
					} else {
						parser = new AozoraParser();
						String inputstr = openEpub(this.fileName);
						parser.openText(inputstr);
						parser.parseRuby();
						this.textReader = parser.getOutputString();
						rubyInfoList = parser.getRubyList();
					}
				} else {
					if (this.parserType == BookInfoUtils.PARSER_TYPE_PLAIN) {
						parser = new AozoraParser();
						inputStream = new FileInputStream(this.fileName);
						parser.openInputStream(inputStream, "\n", this.codePage);
						this.textReader = parser.getLoadedText();
						rubyInfoList = parser.getRubyList();
					} else {
						parser = new AozoraParser();
						inputStream = new FileInputStream(this.fileName);
						parser.openInputStream(inputStream, "\n", this.codePage);
						parser.parseRuby();
						this.textReader = parser.getOutputString();
						rubyInfoList = parser.getRubyList();
					}					
				}
			} catch (IOException e) {
				e.printStackTrace();
				this.textReader = "";
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			if (BookInfoUtils.USE_ASSETS) {
				if (!BookInfoUtils.USE_PARSER) {
					this.textReader = BookInfoUtils.loadAssetsText(activity, this.codePage);
				} else {
					if (this.parserType == BookInfoUtils.PARSER_TYPE_PLAIN) {
						parser = new AozoraParser();
						try {
							parser.openInputStream(activity.getAssets().open(BookInfoUtils.TXT_ASSET_NAME), "\n", this.codePage);
							this.textReader = parser.getLoadedText();
							rubyInfoList = parser.getRubyList();
						} catch (IOException e) {
							e.printStackTrace();
							this.textReader = "";
						}
					} else {
						parser = new AozoraParser();
						try {
							parser.openInputStream(activity.getAssets().open(BookInfoUtils.TXT_ASSET_NAME), "\n", this.codePage);
							parser.parseRuby();
							this.textReader = parser.getOutputString();
							rubyInfoList = parser.getRubyList();
						} catch (IOException e) {
							e.printStackTrace();
							this.textReader = "";
						}
					}
				}
			} else {
				this.textReader = BookInfoUtils.TEST_TEXT;
			}
		}
		if (this.textReader != null) {
			this.curLength = this.textReader.length();
		} else {
			this.curLength = 0;
		}
		this.textUtil = new BookDrawTextUtil();
	}
	
	public void loadBG() {
		if (this.bgFileName != null) {
			bgBitmap = BitmapFactory.decodeFile(this.bgFileName);
		}
	}
	
	public void setCurPage(int curPage) {
		this.curPage = curPage;
		BookInfoUtils.setLastPage(activity, this.curPage);
		if (this.textUtil != null) {
			this.textUtil.setPage(this.curPage);
			if (this.textUtil.getPageFirstPos() >= 0) {
				this.curPosition = this.textUtil.getPageFirstPos();
				BookInfoUtils.setLastPosition(activity, this.curPosition);
				if (D) {
					Log.e(TAG, "BookLoader.setCurPage setLastPosition = " + this.curPosition);
				}
				saveHistory();
			}
			if (D) {
				Log.d(TAG, "lastNonConfigurationPosition, curPosition == " + this.curPosition);
			}
		}
	}
	
    public Dialog onCreateDialog(int id) {
    	switch(id) {
    	case BookInfoUtils.DIALOG_SINGLE_CHOICE:
    		dialog1 = makeDialog("基本型分词（仅供参考）"); 
    		return dialog1;
    	
    	case BookInfoUtils.DIALOG_SINGLE_CHOICE_ICU4C:
    		dialog2 = makeDialog("ICU4C分词（仅供参考）");
    		return dialog2;
    		
		case BookInfoUtils.DIALOG_TEXT_ENTRY:
			LayoutInflater factory = LayoutInflater.from(activity);
            View textEntryView = factory.inflate(R.layout.set_page_dialog, null);
            textViewInfo = (TextView) textEntryView.findViewById(R.id.textViewInfo);
            editPage = (EditText) textEntryView.findViewById(R.id.editPage);
            pageDialog = builder2
            	.setTitle("指定页数")
                .setView(textEntryView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	try {
                    		String str = editPage.getText().toString();
                    		int pageId = Integer.parseInt(str) - 1;
                    		if (activity instanceof CurlActivity) {
                    			final SimplePageView simplePage = (SimplePageView) activity.findViewById(R.id.simplePage); 
                    			if (simplePage != null) {
                    				simplePage.setPage(pageId);
                    				setCurPage(pageId);
                    			}
                    			final CurlView mCurlView = (CurlView) activity.findViewById(R.id.curl);
                    			if (mCurlView != null) {
                    				mCurlView.setCurrentIndex(pageId);
                    				setCurPage(pageId);
                    			}   			
                    		} else if (activity instanceof ViewPagerActivity) {
                    			final BitmapViewPager mPager = (BitmapViewPager) activity.findViewById(R.id.pager);
                    			if (mPager != null) {
                    				mPager.setPage(pageId);
                    				setCurPage(pageId);
                    			}
                    		}
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
            pageDialog.setOnShowListener(new OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					textViewInfo.setText("指定页数(1-" + totalPage + ")：");
					editPage.setEnabled(true);
					editPage.setText("");
		            editPage.append(Integer.toString(curPage + 1));
				}
            });
            return pageDialog;
    	}
    	return null;
    }
    
    private AlertDialog makeDialog(String title) {
		AlertDialog dialog = builder1
			.setTitle(title)
			.setSingleChoiceItems(this.singleChoiceAdapter, 0, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	
                }
            })
            .setPositiveButton("搜索器", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	int pos = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                	if (pos >= 0 && pos < BookLoader.this.singleChoiceAdapter.getCount()) {
                		String text = BookLoader.this.singleChoiceAdapter.getItem(pos);
						activity.startActivity(
							new Intent(activity, JKanjiActivity.class)
								.putExtra(JKanjiActivity.EXTRA_KEY_SHEARCHTEXT, text)
						);
						BookLoader.this.isListSearching = true;
                	} else {
                		Toast.makeText(activity, 
                				"没有任何选择文本", 
                				Toast.LENGTH_SHORT).show();
                	}
                }
            })
            .setNeutralButton("在线搜索", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	int pos = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                	if (pos >= 0 && pos < BookLoader.this.singleChoiceAdapter.getCount()) {
                		String text = BookLoader.this.singleChoiceAdapter.getItem(pos);
                		activity.startActivity(
                			new Intent(activity, DictWebListActivity.class)
                			.putExtra(DictWebListActivity.EXTRA_KEY, text)
                		);
                		BookLoader.this.isListSearching = true;
                	} else {
                		Toast.makeText(activity, 
                				"没有任何选择文本", 
                				Toast.LENGTH_SHORT).show();
                	}
                }
            })
            .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	BookLoader.this.isListSearching = false;
                }
            })
            .setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					BookLoader.this.isListSearching = false;
				}
            })
            .setCancelable(false)
            .create();
		dialog.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				ListView lv = ((AlertDialog)dialog).getListView();
				if (lv != null) {
					lv.setFastScrollEnabled(true);
					lv.setItemChecked(BookLoader.this.dialogCheckPosition, true);
					lv.setSelection(BookLoader.this.dialogCheckPosition);
					lv.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
							BookLoader.this.dialogCheckPosition = pos;
						}
					});
				}
			}
		});
		return dialog;
    }
    
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, BookInfoUtils.SWITCH_BASICSTRING_ID, Menu.NONE, "分词").setIcon(android.R.drawable.ic_menu_view);
		menu.add(Menu.NONE, BookInfoUtils.SWITCH_SEARCH_ID, Menu.NONE, "查词").setIcon(android.R.drawable.ic_menu_search);
		menu.add(Menu.NONE, BookInfoUtils.SWITCH_SHARE_ID, Menu.NONE, "共享").setIcon(android.R.drawable.ic_menu_share);
		menu.add(Menu.NONE, BookInfoUtils.SWITCH_SHELF_ID, Menu.NONE, "添至书架").setIcon(android.R.drawable.ic_menu_add);
		menu.add(Menu.NONE, BookInfoUtils.SWITCH_SQLITE_ID, Menu.NONE, "sqlite搜索").setIcon(android.R.drawable.ic_menu_search);
		menu.add(Menu.NONE, BookInfoUtils.SWITCH_MEMO_ID, Menu.NONE, "备忘录").setIcon(android.R.drawable.ic_menu_edit);
		menu.add(Menu.NONE, BookInfoUtils.SWITCH_PAGE_ID, Menu.NONE, "跳转至").setIcon(android.R.drawable.ic_menu_info_details);
		menu.add(Menu.NONE, BookInfoUtils.SWITCH_EXIT_ID, Menu.NONE, "退出").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}
	
	public void onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case BookInfoUtils.SWITCH_BASICSTRING_ID:
			if (this.isEnableSen) {
				this.textUtil.setPage(this.curPage);
				int pageFirstPos = this.textUtil.getPageFirstPos();
				int pageLastPos = this.textUtil.getPageLastPos();
				if (pageFirstPos >= 0 && pageLastPos >= 0) {
					BookDrawTextUtil.DrawTextInfo drawtextinfo = this.textUtil.getDrawText();
					if (drawtextinfo != null && drawtextinfo.str != null) {
						if (BookInfoUtils.USE_CONTENT_PROVIDER) {
							this.singleChoiceAdapter.clear();
							ArrayList<String> basicStrings = BookInfoUtils.getBasicStrings(activity, 
									drawtextinfo.str, drawtextinfo.startPos, 
									pageFirstPos, pageLastPos, -1, -1,
									0, null);
							Iterator<String> iter = basicStrings.iterator();
							while (iter.hasNext()) {
								String bs = iter.next();
								this.singleChoiceAdapter.add(bs);
							}
							activity.showDialog(BookInfoUtils.DIALOG_SINGLE_CHOICE);
						}
					}
				}
			} else {
				boolean hasJapanLocal = false;
				Locale[] locales = BreakIterator.getAvailableLocales();
				if (locales != null) {
					for (Locale locale : locales) {
						if (D) {
							Log.d(TAG, "locale: " + locale.toString());
						}
						if (locale != null && locale.equals(Locale.JAPAN)) {
							hasJapanLocal = true;
							break;
						}
					}
				}
				if (D) {
					Log.d(TAG, "hasJapanLocal " + hasJapanLocal);
				}
				if (hasJapanLocal) {
					this.textUtil.setPage(this.curPage);
					int pageFirstPos = this.textUtil.getPageFirstPos();
					int pageLastPos = this.textUtil.getPageLastPos();
					if (pageFirstPos >= 0 && pageLastPos >= 0) {
						BookDrawTextUtil.DrawTextInfo drawtextinfo = this.textUtil.getDrawText();
						if (drawtextinfo != null && drawtextinfo.str != null) {
							this.singleChoiceAdapter.clear(); 
							//drawtextinfo.str, 
							//drawtextinfo.startPos, 
							//pageFirstPos, 
							//pageLastPos
							//FIXME:
					        BreakIterator boundary = BreakIterator.getWordInstance(Locale.JAPAN);
					        boundary.setText(drawtextinfo.str);
					        int start = boundary.first();
					        for (int end = boundary.next(); 
					        	end != BreakIterator.DONE; 
					        	start = end, end = boundary.next()) {
					        	String bs = drawtextinfo.str.substring(start, end);
					        	if (end > pageFirstPos - drawtextinfo.startPos && 
					        		start < pageLastPos - drawtextinfo.startPos) {
					        		this.singleChoiceAdapter.add(bs);
					        	}
					        }
							activity.showDialog(BookInfoUtils.DIALOG_SINGLE_CHOICE_ICU4C);
						}
					}
				} else {
					//Toast.makeText(this, "尚未启用Sen分词", Toast.LENGTH_SHORT).show();
					Toast.makeText(activity, "Sen尚未启用，icu4c日文分词无法启用", Toast.LENGTH_SHORT).show();
				}
			}
			break;
			
		case BookInfoUtils.SWITCH_SEARCH_ID:
			if (!this.isProgress) {
				activity.startActivity(new Intent(activity, JKanjiActivity.class));
			}
			break;
			
		case BookInfoUtils.SWITCH_SHARE_ID:
			if (!this.isProgress) {
				this.textUtil.setPage(this.curPage);
				int pageFirstPos = this.textUtil.getPageFirstPos();
				int pageLastPos = this.textUtil.getPageLastPos();
				if (pageFirstPos >= 0 && pageLastPos >= 0) {
					BookDrawTextUtil.DrawTextInfo drawtextinfo = this.textUtil.getDrawText();
					if (drawtextinfo != null && drawtextinfo.str != null) {
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_SEND);
						intent.setType("text/plain");
		                intent.putExtra(Intent.EXTRA_SUBJECT, "阅读器[" + 
		                		(this.fileName != null ? this.fileName : "") + 
		                		"][" + 
		                		this.curPage + 
		                		"]");
		                intent.putExtra(Intent.EXTRA_TEXT, drawtextinfo.str);
						try {
							//startActivity(Intent.createChooser(intent, "共享方式"));
							activity.startActivity(intent);
						} catch (Throwable e) {
							e.printStackTrace();
							Toast.makeText(activity, 
								"共享方式出错", Toast.LENGTH_SHORT)
								.show();
						}
					}
				}
			}
			break;
			
		case BookInfoUtils.SWITCH_SHELF_ID:
			saveNewHistory();
			break;
			
		case BookInfoUtils.SWITCH_SQLITE_ID:
			activity.startActivity(new Intent(activity, SQLiteReaderActivity.class));
        	break;
			
		case BookInfoUtils.SWITCH_MEMO_ID:
			Intent intent = new Intent(activity, ShareToClipboardActivity.class);
			activity.startActivity(intent);
			break;
			
		case BookInfoUtils.SWITCH_PAGE_ID:
			activity.showDialog(BookInfoUtils.DIALOG_TEXT_ENTRY);
			break;
			
		case BookInfoUtils.SWITCH_EXIT_ID:
			//FIXME:if (this.curPage == 0)
			if (this.curPage < 0) { // "setCurPage(lastNonConfigurationPage)" is not executed
				this.setCurPage(this.page - 1);
				if (D) {
					Log.d(TAG, "setCurrentIndex page - 1:" + (this.page - 1));
				}
			} else {
				this.setCurPage(this.curPage);
				if (D) {
					Log.d(TAG, "setCurrentIndex curPage:" + this.curPage);
				}
			}
			BookInfoUtils.stopForgroundService(activity);
			activity.finish();
			break;
		}
	}
	
    public void onDestroy() {
    	if (this.bgBitmap != null) {
			this.bgBitmap.recycle();
			this.bgBitmap = null;
		}
    	if (this.dataSrc != null) {
    		dataSrc.close();
    		dataSrc = null;
    	}
    }
    
	public void onResume() {
		if (this.isListSearching) {
			activity.showDialog(BookInfoUtils.DIALOG_SINGLE_CHOICE);
		}
	}
	
	public void onBackPressed() {
		activity.openOptionsMenu();
	}
	
	private String openEpub(String filename) {
        StringBuffer output = new StringBuffer();
        try {
            InputStream is = new FileInputStream(filename);  
            Book book = new EpubReader().readEpub(is);
            for (Resource res : book.getContents()) {
            	StringBuffer sb = new StringBuffer();
            	InputStreamReader ir = null;
            	BufferedReader r = null;
            	try {
                	InputStream istr = res.getInputStream();
                	String textEncoding = res.getInputEncoding();
                	if (textEncoding != null) {
	                	ir = new InputStreamReader(istr, textEncoding);
	            		r = new BufferedReader(ir);
		                String line;
		                while ((line = r.readLine()) != null) {
		                    sb.append(line);
		                }
		                String inputHTML = sb.toString();
		                //Log.e("EpubtestActivity", Html.fromHtml(inputHTML).toString());
		                Spanned spanned = Html.fromHtml(inputHTML);
		                if (spanned != null) {
		                	String str = spanned.toString();
		                	output.append(str);
		                }
                	}
	            } catch (IOException e) {
	            	e.printStackTrace();
				} finally {
					if (r != null) {
						r.close();
					}
					if (ir != null) {
						ir.close();
					}
				}
            }
        } catch (IOException e) {  
            e.printStackTrace();  
        }
        return output.toString();
	}
	
	private void saveHistory() {
		if (dataSrc != null && fileName != null && fileName.length() > 0 && curHistoryId >= 0) {
			JkanjiShelfHistoryItem item = new JkanjiShelfHistoryItem();
			item.setId(curHistoryId);
			item.setPlainFileName(fileName);
			item.setPlainCharPos(curPosition);
			item.setPlainCharLength(curLength);
			item.setPlainEncoding(codePage);
			if (parserType == BookInfoUtils.PARSER_TYPE_AOZORA) {
				item.setParserType(JkanjiShelfHistoryItem.PLAIN_FORMAT_AOZORA);
			} else {
				item.setParserType(JkanjiShelfHistoryItem.PLAIN_FORMAT_DEFAULT);
			}
			item.setPlainDesc(curDesc);
			curHistoryId = dataSrc.createItem(item);
		}
	}
	
	private void saveNewHistory() {
		if (dataSrc != null && fileName != null && fileName.length() > 0) {
			JkanjiShelfHistoryItem item = new JkanjiShelfHistoryItem();
			item.setId(-1L);
			item.setPlainFileName(fileName);
			item.setPlainCharPos(curPosition);
			item.setPlainCharLength(curLength);
			item.setPlainEncoding(codePage);
			if (parserType == BookInfoUtils.PARSER_TYPE_AOZORA) {
				item.setParserType(JkanjiShelfHistoryItem.PLAIN_FORMAT_AOZORA);
			} else {
				item.setParserType(JkanjiShelfHistoryItem.PLAIN_FORMAT_DEFAULT);
			}
			item.setPlainDesc(curDesc);
			long resultId = dataSrc.createItem(item);
			if (resultId >= 0) {
				Toast.makeText(activity, "添至书架成功", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(activity, "添至书架失败", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(activity, "添至书架失败", Toast.LENGTH_SHORT).show();
		}
	}
}
