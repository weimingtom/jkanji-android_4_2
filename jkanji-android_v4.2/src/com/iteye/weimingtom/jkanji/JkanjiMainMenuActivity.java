package com.iteye.weimingtom.jkanji;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import com.elgubbo.sharetoclipboard.ShareToClipboardActivity;
import com.markupartist.android.widget.ActionBar;
import com.sonyericsson.zoom.JkanjiGalleryHistoryActivity;

public class JkanjiMainMenuActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "JkanjiMainMenuActivity";
	
	private ListView viewBookList;
	private JkanjiMainMenuItemAdapter adapter;
	private ActionBar actionBar;
	private ImageView imageViewTop;
	private boolean isShowBanner;
	private GridView gridViewBookIndex;
	private boolean useGrid;
	
	
	private JkanjiMainMenuItemAdapter.BookModel[] models = {
		new JkanjiMainMenuItemAdapter.BookModel(null, null, R.drawable.banner_furukawayui), // 0
		new JkanjiMainMenuItemAdapter.BookModel("常用词搜索", "内置常用词词库搜索器（可用web搜索器扩展查词），建议打开缓存开关以避免重复加载。如果内存不足自动退出，请改用搜索会话（会话模式）查词。", R.drawable.search), // 1
		new JkanjiMainMenuItemAdapter.BookModel("搜索会话", "难以名状的搜索器，与搜索器的功能类似，不需要预加载，但不支持关键词中简体汉字到日文汉字的转换。适用于搜索器因内存不足而无法运行的情况下。", R.drawable.nyaruko), // 2
		new JkanjiMainMenuItemAdapter.BookModel("sqlite搜索器", "SQLite数据库（特定表结构）搜索引擎，需要数据包，用于搜索EDICT、WadokuJT和MuiltDic等词典和WWWJDIC的日英例句数据库", R.drawable.search_sqlite), // 3
		new JkanjiMainMenuItemAdapter.BookModel(JkanjiBookIndex.getIndexTitle(JkanjiBookIndex.BOOK_INDEX_DICT), JkanjiBookIndex.getIndexSubTitle(JkanjiBookIndex.BOOK_INDEX_DICT), JkanjiBookIndex.getIndexIcon(JkanjiBookIndex.BOOK_INDEX_DICT)), // 4
		new JkanjiMainMenuItemAdapter.BookModel(JkanjiBookIndex.getIndexTitle(JkanjiBookIndex.BOOK_INDEX_GAME), JkanjiBookIndex.getIndexSubTitle(JkanjiBookIndex.BOOK_INDEX_GAME), JkanjiBookIndex.getIndexIcon(JkanjiBookIndex.BOOK_INDEX_GAME)), // 5
		new JkanjiMainMenuItemAdapter.BookModel(JkanjiBookIndex.getIndexTitle(JkanjiBookIndex.BOOK_INDEX_MARKDOWN), JkanjiBookIndex.getIndexSubTitle(JkanjiBookIndex.BOOK_INDEX_MARKDOWN), JkanjiBookIndex.getIndexIcon(JkanjiBookIndex.BOOK_INDEX_MARKDOWN)), // 6
		new JkanjiMainMenuItemAdapter.BookModel(JkanjiBookIndex.getIndexTitle(JkanjiBookIndex.BOOK_INDEX_ETC), JkanjiBookIndex.getIndexSubTitle(JkanjiBookIndex.BOOK_INDEX_ETC), JkanjiBookIndex.getIndexIcon(JkanjiBookIndex.BOOK_INDEX_ETC)), // 7
		new JkanjiMainMenuItemAdapter.BookModel("全局设置", "本地文件路径指定和全局设置", R.drawable.config), // 8
		new JkanjiMainMenuItemAdapter.BookModel("帮助", "帮助页面与历史版本", R.drawable.help), // 9
		new JkanjiMainMenuItemAdapter.BookModel("扩展应用程序", "单独开发的分支版本（缩小程序大小）与扩展程序（添加写入存储卡和网络权限）", R.drawable.yunohidamari), // 10
		new JkanjiMainMenuItemAdapter.BookModel(null, null, R.drawable.banner_001),
	};
	private JkanjiMainMenuItemAdapter.BookModel[] models2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.main_menu);
		
		viewBookList = (ListView) findViewById(R.id.viewBookList);
		gridViewBookIndex = (GridView) findViewById(R.id.gridViewBookIndex);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		imageViewTop = (ImageView) findViewById(R.id.imageViewTop);
		actionBar.setTitle("日语简易词典");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.icon_actionbar;
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
				startActivity(new Intent(JkanjiMainMenuActivity.this, 
						ShareToClipboardActivity.class));
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.gallery;
			}

			@Override
			public void performAction(View view) {
				startActivity(new Intent(JkanjiMainMenuActivity.this, 
						JkanjiGalleryHistoryActivity.class));
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.book;
			}

			@Override
			public void performAction(View view) {
				startActivity(new Intent(JkanjiMainMenuActivity.this, 
						//JkanjiAozoraReaderActivity.class));
						JkanjiShelfHistoryActivity.class));
			}
        });
        
        isShowBanner = JkanjiSettingActivity.getShowBanner(JkanjiMainMenuActivity.this);
        useGrid = JkanjiSettingActivity.getUseGrid(this);
        if (useGrid) {
        	isShowBanner = false;
        }
        if (isShowBanner) {
        	adapter = new JkanjiMainMenuItemAdapter(this, models, useGrid);
        } else {
        	if (models[models.length - 1].title == null) {
        		models2 = new JkanjiMainMenuItemAdapter.BookModel[models.length - 2];
	        	for (int i = 0; i < models.length - 2; i++) {
	        		models2[i] = models[i + 1];
	        	}
        	} else {
	        	models2 = new JkanjiMainMenuItemAdapter.BookModel[models.length - 1];
	        	for (int i = 0; i < models.length - 1; i++) {
	        		models2[i] = models[i + 1];
	        	}
        	}
        	adapter = new JkanjiMainMenuItemAdapter(this, models2, useGrid);
        }
        
        viewBookList.setAdapter(adapter);
		viewBookList.setOnItemClickListener(mOnItemClickListener);
		gridViewBookIndex.setAdapter(adapter);
		gridViewBookIndex.setOnItemClickListener(mOnItemClickListener);
		
        if (useGrid) {
        	viewBookList.setVisibility(ListView.INVISIBLE);
            gridViewBookIndex.setVisibility(GridView.VISIBLE);	
        } else {
        	viewBookList.setVisibility(ListView.VISIBLE);
            gridViewBookIndex.setVisibility(GridView.INVISIBLE);
        }
	}
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			int pos = position;
			if (isShowBanner) {
				pos = position;
			} else {
				pos = position + 1;
			}
			switch(pos) {
			case 1:
				startActivity(new Intent(JkanjiMainMenuActivity.this, 
						JKanjiActivity.class));
				break;
			
			case 2:
				startActivity(new Intent(JkanjiMainMenuActivity.this, 
						JkanjiTalkActivity.class));	
				break;
			
			case 3:
				startActivity(new Intent(JkanjiMainMenuActivity.this, 
						SQLiteReaderActivity.class));						
				break;

			case 4:
				startActivity(new Intent(JkanjiMainMenuActivity.this, JkanjiBookIndex.class)
						.putExtra(JkanjiBookIndex.EXTRA_INDEX_TYPE, JkanjiBookIndex.BOOK_INDEX_DICT));
				break;
				
			case 5:
				startActivity(new Intent(JkanjiMainMenuActivity.this, JkanjiBookIndex.class)
						.putExtra(JkanjiBookIndex.EXTRA_INDEX_TYPE, JkanjiBookIndex.BOOK_INDEX_GAME));
				break;
				
			case 6:
				startActivity(new Intent(JkanjiMainMenuActivity.this, JkanjiBookIndex.class)
						.putExtra(JkanjiBookIndex.EXTRA_INDEX_TYPE, JkanjiBookIndex.BOOK_INDEX_MARKDOWN));
				break;
				
			case 7:
				startActivity(new Intent(JkanjiMainMenuActivity.this, JkanjiBookIndex.class)
						.putExtra(JkanjiBookIndex.EXTRA_INDEX_TYPE, JkanjiBookIndex.BOOK_INDEX_ETC));
				break;
			
			case 8:
				startActivity(new Intent(JkanjiMainMenuActivity.this, 
						JkanjiSettingActivity.class));	
				break;
				
			case 9:
				break;
				
			case 10:
				startActivity(new Intent(JkanjiMainMenuActivity.this, 
						JkanjiBranchActivity.class));
				break;
			}
		}
	};
	
    @Override
	protected void onResume() {
		super.onResume();
		File file = new File(JkanjiSettingActivity.getBGFileName(this));
		if (file.canRead() && file.exists() && file.isFile()) {
			imageViewTop.setImageURI(Uri.fromFile(file));			
		} else {
			imageViewTop.setImageURI(null);
		}
	}
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		if (D) {
			Log.e(TAG, "onDestroy");
		}
		if (adapter != null) {
			adapter.destory();
		}
	}
}
