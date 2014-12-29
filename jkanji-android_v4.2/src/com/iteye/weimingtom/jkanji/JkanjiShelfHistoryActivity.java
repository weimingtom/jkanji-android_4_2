package com.iteye.weimingtom.jkanji;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import fi.harism.curl.BookInfoUtils;
import fi.harism.curl.CurlActivity;
import fi.harism.curl.CurlLandActivity;
import fi.harism.curl.CurlPortActivity;
import fi.harism.curl.ViewPagerActivity;
import fi.harism.curl.ViewPagerLandActivity;
import fi.harism.curl.ViewPagerPortActivity;

public class JkanjiShelfHistoryActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "JkanjiShelfHistoryActivity";
	
	private static final String SHARE_PREF_NAME = "shelf_history_pref";
	private final static String SHARE_SHELF_AOZORA = "shelfAozora";
	//FIXME: 注意修改字面值
	private static final String SHARE_PREF_SORT_TYPE = "shelfSortType";
	private static final String SHARE_SHELF_SHOW_INFO = "shelfShowInfo";
	private static final String SHARE_SHELF_SHOW_CONFIG = "shelfShowConfig";
	private static final String SHARE_SHELF_OPEN_TYPE = "shelfOpenType";
	private static final String SHARE_SHELF_LIST_POS = "shelfListPos";
	
	private final static int CONTEXT_MENU_AOZORA = ContextMenu.FIRST + 1;
	private final static int CONTEXT_MENU_PLAIN = ContextMenu.FIRST + 2;
	private final static int CONTEXT_MENU_DESC = ContextMenu.FIRST + 3;
	private final static int CONTEXT_MENU_SHARE = ContextMenu.FIRST + 4;
	private final static int CONTEXT_MENU_PARSER = ContextMenu.FIRST + 5;
	private final static int CONTEXT_MENU_DELETE = ContextMenu.FIRST + 6;
	
	private static final int DIALOG_WARNING_ID = 1;
	private static final int DIALOG_LIST = 2;
	private static final int DIALOG_TEXT_ENTRY = 3;
	private static final int DIALOG_ADD_LIST = 4;
	
	private static final int REQUEST_RE_PATH = 4;
	private static final int REQUEST_EX = 6;
	
	private final static int SORT_TYPE_CREATE = 0;
	private final static int SORT_TYPE_MODIFY = 1;
	private final static String[] SORT_TYPE_ITEMS = new String[] {
		"创建时间（倒序）",
		"最近阅读时间（倒序）",
	};
	private ArrayAdapter<String> sortAdapter;
	
	private ActionBar actionBar;
	private RecentFileAdapter adapter;
	private ListView listViewShelf;
	private CheckBox checkBoxNote;
	private Spinner spinnerMode;
	private ArrayAdapter<String> spinnerModeAdapter;
	//private JkanjiShelfHistoryDataSource dataSource;

	private Button buttonSettings;
	private AlertDialog.Builder builder1, builder2, builder3, builder4;
	private LinearLayout linearLayoutConfig;
	
	private int currentPos = -1;
	
	private EditText editDescription;
	private AlertDialog descDialog;
	
	private TextView textViewInfo;
	
	private ArrayAdapter<String> addAdapter;
	private final static String[] ADD_TYPE_ITEMS = new String[] {
		"Root Explorer",
		"内置浏览器",
	};
	private final static int ADD_TYPE_RE = 0;
	private final static int ADD_TYPE_EX1 = 1;
	
	private int listPos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.shelf_history);

    	actionBar = (ActionBar) this.findViewById(R.id.actionbar);
		actionBar.setTitle("书架");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.shelf_folder;
				return R.drawable.book;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
        /*
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.book;
			}

			@Override
			public void performAction(View view) {
				startActivity(new Intent(
					JkanjiShelfHistoryActivity.this, JkanjiAozoraReaderActivity.class)
				);
			}
        });
        */        
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.adding;
			}

			@Override
			public void performAction(View view) {
				showDialog(DIALOG_ADD_LIST);
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.sort;
			}

			@Override
			public void performAction(View view) {
				showDialog(DIALOG_LIST);
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.config;
			}

			@Override
			public void performAction(View view) {
				if (getLastShelfShowConfig()) {
					linearLayoutConfig.setVisibility(View.GONE);
					setLastShelfShowConfig(false);
				} else {
					linearLayoutConfig.setVisibility(View.VISIBLE);
					setLastShelfShowConfig(true);
				}
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.config2;
			}

			@Override
			public void performAction(View view) {
				startActivity(new Intent(
					JkanjiShelfHistoryActivity.this, JkanjiAozoraReaderActivity.class)
				);
			}
        });

		listViewShelf = (ListView) this.findViewById(R.id.listViewShelf);
		checkBoxNote = (CheckBox) this.findViewById(R.id.checkBoxNote);
		spinnerMode = (Spinner) this.findViewById(R.id.spinnerMode);
		buttonSettings = (Button) this.findViewById(R.id.buttonSettings);
		textViewInfo = (TextView) this.findViewById(R.id.textViewInfo);
		linearLayoutConfig = (LinearLayout) this.findViewById(R.id.linearLayoutConfig);
		
		builder1 = new AlertDialog.Builder(this);
		builder2 = new AlertDialog.Builder(this);
		builder3 = new AlertDialog.Builder(this);
		builder4 = new AlertDialog.Builder(this);
		sortAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.select_dialog_singlechoice);
		for (String str : SORT_TYPE_ITEMS) {
			sortAdapter.add(str);
		}
		addAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.select_dialog_item);
		for (String str : ADD_TYPE_ITEMS) {
			addAdapter.add(str);
		}
		
		spinnerModeAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_spinner_item);
		spinnerModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerModeAdapter.add("上下文菜单"); // 0
		spinnerModeAdapter.add("青空阅读器"); // 1
		spinnerModeAdapter.add("简单查看器（修改文本编码）"); // 2
		spinnerModeAdapter.add("修改备注"); // 3
		spinnerModeAdapter.add("共享设置"); // 4
		spinnerModeAdapter.add("更换解释器"); // 5
		spinnerModeAdapter.add("删除模式"); // 6
		spinnerMode.setAdapter(spinnerModeAdapter);
		spinnerMode.setSelection(getLastOpenType());
		spinnerMode.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				setLastOpenType(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		
//		dataSource = new JkanjiShelfHistoryDataSource(this);
//		dataSource.open();
		
		adapter = new RecentFileAdapter(this, R.layout.shelf_history_item);
    	listViewShelf.setAdapter(adapter);
    	listViewShelf.setFastScrollEnabled(true);
    	
    	listPos = getLastListPos();
    	listViewShelf.setSelection(listPos);
    	
    	listViewShelf.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int itemPos = spinnerMode.getSelectedItemPosition();
				switch (itemPos) {					
				case 0:
					openContextMenu(view);
					break;
					
				case 1:
					openAozoraItem(position);
					break;
					
				case 2:
					openPlainItem(position);
					break;
					
				case 3:
					currentPos = position;
					showDialog(DIALOG_TEXT_ENTRY);
					break;
					
				case 4:
					shareItem(position);
					break;
					
				case 5:
					switchParserItem(position);
					break;
					
				case 6:
					//deleteItem(position);
					currentPos = position;
					showDialog(DIALOG_WARNING_ID);
					break;
				}
			}
    	});
    	registerForContextMenu(listViewShelf);
    	listViewShelf.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                	if (absListView != null) {
                		listPos = absListView.getFirstVisiblePosition();
                	}
                }
        	}

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
            }
        });
    	
    	checkBoxNote.setChecked(getLastShelfShowInfo());
    	checkBoxNote.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
		    	if (!isChecked) {
		    		textViewInfo.setVisibility(View.GONE);
		    		setLastShelfShowInfo(false);
		    	} else {
		    		textViewInfo.setVisibility(View.VISIBLE);
		    		setLastShelfShowInfo(true);
		        }
			}
    	});
    	
    	buttonSettings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(
					JkanjiShelfHistoryActivity.this, JkanjiAozoraReaderActivity.class)
				);
			}
    	});
    	
    	if (getLastShelfShowInfo()) {
    		this.textViewInfo.setVisibility(View.VISIBLE);
    	} else {
    		this.textViewInfo.setVisibility(View.GONE);
        }
    	
    	if (this.getLastShelfShowConfig()) {
    		this.linearLayoutConfig.setVisibility(View.VISIBLE);
    	} else {
    		this.linearLayoutConfig.setVisibility(View.GONE);
    	}
	}
	
	private void addItemByRE() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		try {
			startActivityForResult(intent, REQUEST_RE_PATH);
		} catch (Throwable e) {
			e.printStackTrace();
			Toast.makeText(JkanjiShelfHistoryActivity.this, 
				"找不到可用的应用程序", Toast.LENGTH_SHORT)
				.show();
		}
	}
	
	private void openPlainItem(int pos) {
		JkanjiShelfHistoryItem item = adapter.getItem(pos);
		String fileName = item.getPlainFileName();
		File file = new File(fileName);
		if (file.canRead() && file.exists()) {
			startActivity(new Intent(
				JkanjiShelfHistoryActivity.this, JkanjiShelfPlainReaderActivity.class)
				.putExtra(JkanjiShelfPlainReaderActivity.EXTRA_ID, item.getId())
				.putExtra(JkanjiShelfPlainReaderActivity.EXTRA_PLAIN_FILE_NAME, fileName)
				.putExtra(JkanjiShelfPlainReaderActivity.EXTRA_PLAIN_ENCODING, item.getPlainEncoding())
				.putExtra(JkanjiShelfPlainReaderActivity.EXTRA_PLAIN_CHAR_POS, item.getPlainCharPos())
				.putExtra(JkanjiShelfPlainReaderActivity.EXTRA_ALWAYS_SAVE, true)
			);
		}
	}
	
	private void openAozoraItem(int pos) {
		JkanjiShelfHistoryItem historyItem = adapter.getItem(pos);
		if (historyItem != null) {
			int parserFormat;
			if (historyItem.getParserType() == JkanjiShelfHistoryItem.PLAIN_FORMAT_AOZORA) {
				parserFormat = BookInfoUtils.PARSER_TYPE_AOZORA;
			} else {
				parserFormat = BookInfoUtils.PARSER_TYPE_PLAIN;
			}
			openAozora(historyItem.getPlainFileName(), 
					historyItem.getPlainCharPos(), 
					historyItem.getPlainEncoding(), 
					historyItem.getId(), 
					parserFormat,
					historyItem.getPlainDesc());
		}
	}
	
	private void openAozora(String filename, int position, String encoding, long id, int parserType, String desc) {
		Intent intent = null;
		int curlType = BookInfoUtils.getLastCurlType(this);
		if (D) {
			Log.e(TAG, "openAozora curlType == " + curlType);
		}
		if (curlType == BookInfoUtils.CURL_TYPE_VIEWPAGER) {
			switch (BookInfoUtils.getLastScreenOri(this)) {
			case BookInfoUtils.SCREEN_ORI_LAND: 
				intent = new Intent(this, ViewPagerLandActivity.class);
				break;
				
			case BookInfoUtils.SCREEN_ORI_PORT:
				intent = new Intent(this, ViewPagerPortActivity.class);
				break;
				
			default:
				intent = new Intent(this, ViewPagerActivity.class);
				break;
			}
		} else {
			switch (BookInfoUtils.getLastScreenOri(this)) {
			case BookInfoUtils.SCREEN_ORI_LAND: 
				intent = new Intent(this, CurlLandActivity.class);
				break;
				
			case BookInfoUtils.SCREEN_ORI_PORT:
				intent = new Intent(this, CurlPortActivity.class);
				break;
				
			default:
				intent = new Intent(this, CurlActivity.class);
				break;
			}
		}
		startActivity(
				intent
				.putExtra(BookInfoUtils.EXTRA_KEY_FILE_NAME, filename)
				.putExtra(BookInfoUtils.EXTRA_KEY_BG_FILE_NAME, BookInfoUtils.getLastBGFileName(this))
				.putExtra(BookInfoUtils.EXTRA_KEY_IS_FULL_SCREEN, BookInfoUtils.getLastIsFullScreen(this))
				.putExtra(BookInfoUtils.EXTRA_KEY_HAS_ACTIONBAR, BookInfoUtils.getLastHasActionbar(this))
				.putExtra(BookInfoUtils.EXTRA_KEY_ENABLE_SEN, BookInfoUtils.getLastEnableSen(this))
				.putExtra(BookInfoUtils.EXTRA_KEY_PAGE, 1)
				.putExtra(BookInfoUtils.EXTRA_KEY_CURL_TYPE, BookInfoUtils.getLastCurlType(this))
				.putExtra(BookInfoUtils.EXTRA_KEY_CODEPAGE, encoding) //FIXME:
				.putExtra(BookInfoUtils.EXTRA_KEY_REVERSE_DIRECTION, BookInfoUtils.getLastReverseDirection(this))
				.putExtra(BookInfoUtils.EXTRA_KEY_PARSER_TYPE, parserType)
				.putExtra(BookInfoUtils.EXTRA_KEY_IS_VERTICAL, BookInfoUtils.getLastIsVertical(this))
				.putExtra(BookInfoUtils.EXTRA_KEY_MASK_BG, BookInfoUtils.getLastMaskBG(this))
				.putExtra(BookInfoUtils.EXTRA_KEY_BASE_PAGE, false)
				.putExtra(BookInfoUtils.EXTRA_KEY_POSITION, position)
				.putExtra(BookInfoUtils.EXTRA_KEY_SCREEN_ORI, BookInfoUtils.getLastScreenOri(this))
				.putExtra(BookInfoUtils.EXTRA_KEY_BLACK_BACK, BookInfoUtils.getLastBlackBack(this))
				.putExtra(BookInfoUtils.EXTRA_KEY_USE_VOLUME_KEY, BookInfoUtils.getLastUseVolumeKey(this))
				.putExtra(BookInfoUtils.EXTRA_KEY_HISTORY_ID, id)
				.putExtra(BookInfoUtils.EXTRA_KEY_DESC, desc)
		);
	}
	
    @Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateItems();
	}
	
	private void updateItems() {
		listPos = getLastListPos();
		
		int sortType = getLastSortType();
		adapter.clear();
		JkanjiShelfHistoryDataSource dataSource = new JkanjiShelfHistoryDataSource(this);
		dataSource.open();
		ArrayList<JkanjiShelfHistoryItem> items = dataSource.getAllItems();
		switch (sortType) {
		case SORT_TYPE_MODIFY:
			Collections.sort(items, new Comparator<JkanjiShelfHistoryItem>() {
				@Override
				public int compare(JkanjiShelfHistoryItem lhs, JkanjiShelfHistoryItem rhs) {
					if (lhs == null) {
						return -1;
					} else if (rhs == null) {
						return 1;
					} else {
						Time time1 = lhs.getPlainTime();
						Time time2 = rhs.getPlainTime();
						if (time1 == null) {
							return -1;
						} else if (time2 == null) {
							return 1;
						} else {
							return Time.compare(time1, time2);
						}
					}
				}
			});
			for (int i = items.size() - 1; i >= 0; i--) {
				adapter.add(items.get(i));
			}
			break;
		
		case SORT_TYPE_CREATE:
		default:
			for (int i = items.size() - 1; i >= 0; i--) {
				adapter.add(items.get(i));
			}
			break;
		}
		adapter.notifyDataSetChanged();
		dataSource.close();
		
		listViewShelf.setSelection(listPos);
		setLastListPos(listPos);
	}

	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
    	super.onCreateContextMenu(menu, v, info);
    	menu.add(0, CONTEXT_MENU_AOZORA, 0, "青空阅读器");
    	menu.add(0, CONTEXT_MENU_PLAIN, 0, "简单查看器（修改文本编码）");
    	menu.add(0, CONTEXT_MENU_DESC, 0, "修改备注");
    	menu.add(0, CONTEXT_MENU_SHARE, 0, "共享设置");
    	menu.add(0, CONTEXT_MENU_PARSER, 0, "切换纯文本/青空解释器");
    	menu.add(0, CONTEXT_MENU_DELETE, 0, "删除记录");
	}
	
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    	if (info != null) {
	    	switch (item.getItemId()) {
	    	case CONTEXT_MENU_DELETE:
	    		//deleteItem((int)info.id);
	    		currentPos = info.position;
	    		showDialog(DIALOG_WARNING_ID);
	    		return true;
	    		
	    	case CONTEXT_MENU_PLAIN:
	    		openPlainItem(info.position/*(int)info.id*/);
	    		return true;
	
	    	case CONTEXT_MENU_AOZORA:
	    		openAozoraItem(info.position/*(int)info.id*/);
	    		return true;
	    		
	    	case CONTEXT_MENU_PARSER:
	    		switchParserItem(info.position/*(int)info.id*/);
	    		return true;
	    		
	    	case CONTEXT_MENU_SHARE:
	    		shareItem(info.position/*(int)info.id*/);
	    		return true;
	    		
	    	case CONTEXT_MENU_DESC:
	    		currentPos = info.position;
	    		showDialog(DIALOG_TEXT_ENTRY);
	    		return true;
	    	}
    	}
    	return super.onContextItemSelected(item);
    }
    

    @Override
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog = null;
	    switch(id) {
	    case DIALOG_WARNING_ID:
    		return builder1
    			.setTitle("警告")
    			.setMessage("是否删除书架记录？")
    			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int id) {
    					deleteItem(currentPos);
    					currentPos = -1;
    				}
    			})
    			.setNegativeButton("取消", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int id) {
    					currentPos = -1;
    				}
    			})
    			.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						currentPos = -1;
					}
    			})
    	       .create();
	        
        case DIALOG_LIST:
    		dialog = builder2
                .setTitle("排序方式")
                .setSingleChoiceItems(sortAdapter, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setLastSortType(which);
                        updateItems();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
                })
                .setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						
					}
                })
                .create();
    		dialog.setOnShowListener(new OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					((AlertDialog) dialog).getListView().setItemChecked(getLastSortType(), true);
				}
    		});
            return dialog;
            
		case DIALOG_TEXT_ENTRY:
			LayoutInflater factory = LayoutInflater.from(this);
            View textEntryView = factory.inflate(R.layout.set_des_dialog, null);
            editDescription = (EditText) textEntryView.findViewById(R.id.editDescription);
            descDialog = builder3
            	.setTitle("备注")
                .setView(textEntryView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	try {
                    		String str = editDescription.getText().toString();
                    		setDesc(currentPos, str);
                    	} catch (Throwable e) {
                    		e.printStackTrace();
                    	}
                    	currentPos = -1;
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	currentPos = -1;
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						currentPos = -1;
					}
                })
                .create();
            descDialog.setOnShowListener(new OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					editDescription.setText("");
					String desc = getDesc(currentPos);
					if (desc != null) {
						editDescription.append(desc);
					}
				}
            });
            return descDialog;
            
        case DIALOG_ADD_LIST:
    		dialog = builder4
                .setTitle("添加方式")
                .setAdapter(addAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	switch (which) {
                    	default:
                    	case ADD_TYPE_RE:
                    		addItemByRE();
                    		break;
                    		
                    	case ADD_TYPE_EX1:
            				startActivityForResult(new Intent(
        						JkanjiShelfHistoryActivity.this, JkanjiShelfBrowserActivity.class), REQUEST_EX
        					);
                    		break;
                    	}
                        dialog.cancel();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
                })
                .setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						
					}
                })
                .create();
            return dialog;
	    }
    	return dialog;
	}

	private void deleteItem(int pos) {
		if (pos >= 0 && pos < adapter.getCount()) {
			JkanjiShelfHistoryItem historyItem = adapter.getItem(pos);
			JkanjiShelfHistoryDataSource dataSource = new JkanjiShelfHistoryDataSource(this);
			dataSource.open();
			dataSource.deleteItem(historyItem);
			dataSource.close();
			adapter.remove(historyItem);
			adapter.notifyDataSetChanged();
		}
    }
    
	private void switchParserItem(int pos) {
		if (pos >= 0 && pos < adapter.getCount()) {
			JkanjiShelfHistoryItem historyItem = adapter.getItem(pos);
			if (historyItem.getParserType() == JkanjiShelfHistoryItem.PLAIN_FORMAT_DEFAULT) {
				historyItem.setParserType(JkanjiShelfHistoryItem.PLAIN_FORMAT_AOZORA);
			} else {
				historyItem.setParserType(JkanjiShelfHistoryItem.PLAIN_FORMAT_DEFAULT);
			}
			JkanjiShelfHistoryDataSource dataSource = new JkanjiShelfHistoryDataSource(this);
			dataSource.open();
			dataSource.createItem(historyItem);
			dataSource.close();
			
			updateItems();
		}
	}
    
	private void shareItem(int pos) {
		if (pos >= 0 && pos < adapter.getCount()) {
			JkanjiShelfHistoryItem historyItem = adapter.getItem(pos);
			Intent intent;
			intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, JkanjiAozoraReaderActivity.getTimeString() + ":书架预设");
			intent.putExtra(Intent.EXTRA_TEXT, historyItem.toShareString());
            try {
            	//startActivity(Intent.createChooser(intent, "共享方式"));
            	startActivity(intent);
            } catch (Throwable e) {
				e.printStackTrace();
				Toast.makeText(JkanjiShelfHistoryActivity.this, 
					"共享方式出错", Toast.LENGTH_SHORT)
					.show();
			}
		}
	}
	
    private void setLastShelfAozora(boolean isEnabled) {
		PrefUtil.putBoolean(this, SHARE_PREF_NAME,
				SHARE_SHELF_AOZORA,
				isEnabled);
    }
    
    private boolean getLastShelfAozora() {
    	return PrefUtil.getBoolean(this, SHARE_PREF_NAME,
				SHARE_SHELF_AOZORA,
    			false);
    }
    
    private void setLastSortType(int type) {
		PrefUtil.putInt(this, SHARE_PREF_NAME,
				SHARE_PREF_SORT_TYPE,
				type);
    }
    
    private int getLastSortType() {
		return PrefUtil.getInt(this, SHARE_PREF_NAME,
    			SHARE_PREF_SORT_TYPE,
    			SORT_TYPE_CREATE);
    }

    private void setLastShelfShowInfo(boolean isEnabled) {
		PrefUtil.putBoolean(this, SHARE_PREF_NAME,
				SHARE_SHELF_SHOW_INFO,
				isEnabled);
    }
    
    private boolean getLastShelfShowInfo() {
    	return PrefUtil.getBoolean(this, SHARE_PREF_NAME,
    			SHARE_SHELF_SHOW_INFO,
    			true);
    }

    private void setLastShelfShowConfig(boolean isEnabled) {
		PrefUtil.putBoolean(this, SHARE_PREF_NAME,
				SHARE_SHELF_SHOW_CONFIG,
				isEnabled);
    }
    
    private boolean getLastShelfShowConfig() {
    	return PrefUtil.getBoolean(this, SHARE_PREF_NAME,
				SHARE_SHELF_SHOW_CONFIG,
    			false);
    }
    
    private void setLastOpenType(int type) {
		PrefUtil.putInt(this, SHARE_PREF_NAME,
    			SHARE_SHELF_OPEN_TYPE,
				type);
    }
    
    private int getLastOpenType() {
		return PrefUtil.getInt(this, SHARE_PREF_NAME,
    			SHARE_SHELF_OPEN_TYPE,
    			0);
    }
    
    private void setLastListPos(int listPos) {
    	PrefUtil.putInt(this, SHARE_PREF_NAME,
				SHARE_SHELF_LIST_POS,
				listPos);
    }
    
    private int getLastListPos() {
    	return PrefUtil.getInt(this, SHARE_PREF_NAME,
    			SHARE_SHELF_LIST_POS,
    			0);
    }
    
    private void setDesc(int pos, String desc) {
		if (pos >= 0 && pos < adapter.getCount()) {
			JkanjiShelfHistoryItem historyItem = adapter.getItem(pos);
			historyItem.setPlainDesc(desc);
			JkanjiShelfHistoryDataSource dataSource = new JkanjiShelfHistoryDataSource(this);
			dataSource.open();
			dataSource.createItem(historyItem);
			dataSource.close();
			adapter.notifyDataSetChanged();
		}
    }
    
    private String getDesc(int pos) {
    	String desc = null;
    	if (pos >= 0 && pos < adapter.getCount()) {
    		JkanjiShelfHistoryItem item = adapter.getItem(pos);
    		desc = item.getPlainDesc();
    	}
    	return desc;
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_RE_PATH:
			if (resultCode == RESULT_OK && 
				data != null &&
				data.getData() != null) {
				String resultPath = data.getData().getPath();
				JkanjiShelfHistoryItem item = new JkanjiShelfHistoryItem();
				item.setId(-1L);
				item.setPlainFileName(resultPath);
				item.setPlainCharPos(0);
				item.setPlainCharLength(0);
				item.setPlainEncoding("shift-jis");
				JkanjiShelfHistoryDataSource dataSrc = new JkanjiShelfHistoryDataSource(this);
				dataSrc.open();
				dataSrc.createItem(item);
				dataSrc.close();
				
				listPos = 0;
				setLastListPos(listPos);
//				updateItems();
			}
			break;
			
		case REQUEST_EX:
			if (resultCode == RESULT_OK) {
				listPos = 0;
				setLastListPos(listPos);
//				updateItems();
			}
			break;
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		setLastListPos(this.listPos);
	}

	@Override
	protected void onStop() {
		super.onStop();
		setLastListPos(this.listPos);
	}
	
	
	private final static class ViewHolder {
		ImageView ivIcon;
		TextView tvFilename;
		TextView tvDesc;
	}
	
	private final static class RecentFileAdapter extends ArrayAdapter<JkanjiShelfHistoryItem> {
		private LayoutInflater inflater;
		private int textViewId;
		
		public RecentFileAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			textViewId = textViewResourceId;
			inflater = LayoutInflater.from(context);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(textViewId, null);
				holder = new ViewHolder();
				holder.ivIcon = (ImageView)convertView.findViewById(R.id.ivIcon);
				holder.tvFilename = (TextView)convertView.findViewById(R.id.tvFilename);
				holder.tvDesc = (TextView)convertView.findViewById(R.id.tvDesc);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			
			JkanjiShelfHistoryItem item = getItem(position);
			if (item != null) {
				File f = new File(item.getPlainFileName());
				holder.tvFilename.setText(f.getName());
				holder.ivIcon.setImageResource(R.drawable.shelf_file);
				holder.tvDesc.setText(item.toHistoryDesc());
			} else {
				holder.tvFilename.setText(null);
				holder.ivIcon.setImageBitmap(null);
				holder.tvDesc.setText(null);				
			}
			return convertView;
		}
	}
}
