package com.iteye.weimingtom.snowbook.fragment;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import com.elgubbo.sharetoclipboard.ShareToClipboardActivity;
import com.iteye.weimingtom.jkanji.JKanjiActivity;
import com.iteye.weimingtom.jkanji.JkanjiBookIndex;
import com.iteye.weimingtom.jkanji.JkanjiBranchActivity;
import com.iteye.weimingtom.jkanji.JkanjiMainMenuActivity;
import com.iteye.weimingtom.jkanji.JkanjiSettingActivity;
import com.iteye.weimingtom.jkanji.JkanjiShelfHistoryActivity;
import com.iteye.weimingtom.jkanji.JkanjiTalkActivity;
import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.jkanji.SQLiteReaderActivity;
import com.iteye.weimingtom.jkanji.WebViewActivity;
import com.iteye.weimingtom.snowbook.activity.BaseActivity;
import com.iteye.weimingtom.snowbook.adapter.MainMenuItemAdapter;
import com.markupartist.android.widget.ActionBar;
import com.sonyericsson.zoom.JkanjiGalleryHistoryActivity;

/**
 * @see com.iteye.weimingtom.jkanji.JkanjiMainMenuActivity
 * @author weimingtom
 *
 */
public class OldVersionFragment extends Fragment {
	private final static boolean D = false;
	private final static String TAG = "OldVersionFragment";
	
	private ListView viewBookList;
	private MainMenuItemAdapter adapter;
	private ActionBar actionBar;
	private ImageView imageViewTop;
	private boolean isShowBanner;
	private GridView gridViewBookIndex;
	private boolean useGrid;
	
	private MainMenuItemAdapter.BookModel[] models = {
		new MainMenuItemAdapter.BookModel(null, null, R.drawable.banner_furukawayui), // 0
		new MainMenuItemAdapter.BookModel("常用词搜索", "内置常用词词库搜索器（可用web搜索器扩展查词），建议打开缓存开关以避免重复加载。如果内存不足自动退出，请改用搜索会话（会话模式）查词。", R.drawable.search), // 1
		new MainMenuItemAdapter.BookModel("搜索会话", "难以名状的搜索器，与搜索器的功能类似，不需要预加载，但不支持关键词中简体汉字到日文汉字的转换。适用于搜索器因内存不足而无法运行的情况下。", R.drawable.nyaruko), // 2
		new MainMenuItemAdapter.BookModel("sqlite搜索器", "SQLite数据库（特定表结构）搜索引擎，需要数据包，用于搜索EDICT、WadokuJT和MuiltDic等词典和WWWJDIC的日英例句数据库", R.drawable.search_sqlite), // 3
		new MainMenuItemAdapter.BookModel(JkanjiBookIndex.getIndexTitle(JkanjiBookIndex.BOOK_INDEX_DICT), JkanjiBookIndex.getIndexSubTitle(JkanjiBookIndex.BOOK_INDEX_DICT), JkanjiBookIndex.getIndexIcon(JkanjiBookIndex.BOOK_INDEX_DICT)), // 4
		new MainMenuItemAdapter.BookModel(JkanjiBookIndex.getIndexTitle(JkanjiBookIndex.BOOK_INDEX_GAME), JkanjiBookIndex.getIndexSubTitle(JkanjiBookIndex.BOOK_INDEX_GAME), JkanjiBookIndex.getIndexIcon(JkanjiBookIndex.BOOK_INDEX_GAME)), // 5
		new MainMenuItemAdapter.BookModel(JkanjiBookIndex.getIndexTitle(JkanjiBookIndex.BOOK_INDEX_MARKDOWN), JkanjiBookIndex.getIndexSubTitle(JkanjiBookIndex.BOOK_INDEX_MARKDOWN), JkanjiBookIndex.getIndexIcon(JkanjiBookIndex.BOOK_INDEX_MARKDOWN)), // 6
		new MainMenuItemAdapter.BookModel(JkanjiBookIndex.getIndexTitle(JkanjiBookIndex.BOOK_INDEX_ETC), JkanjiBookIndex.getIndexSubTitle(JkanjiBookIndex.BOOK_INDEX_ETC), JkanjiBookIndex.getIndexIcon(JkanjiBookIndex.BOOK_INDEX_ETC)), // 7
		new MainMenuItemAdapter.BookModel("全局设置", "本地文件路径指定和全局设置", R.drawable.config), // 8
		new MainMenuItemAdapter.BookModel("帮助", "帮助页面与历史版本", R.drawable.help), // 9
		new MainMenuItemAdapter.BookModel("扩展应用程序", "单独开发的分支版本（缩小程序大小）与扩展程序（添加写入存储卡和网络权限）", R.drawable.yunohidamari), // 10
		new MainMenuItemAdapter.BookModel(null, null, R.drawable.banner_001),
	};
	private MainMenuItemAdapter.BookModel[] models2;
	
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
    	return inflater.inflate(R.layout.snowbook_main_menu_old, null);
    }
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		viewBookList = (ListView) view.findViewById(R.id.viewBookList);
		gridViewBookIndex = (GridView) view.findViewById(R.id.gridViewBookIndex);
		
		imageViewTop = (ImageView) view.findViewById(R.id.imageViewTop);
        
		actionBar = (ActionBar) view.findViewById(R.id.actionbar);
		imageViewTop = (ImageView) view.findViewById(R.id.imageViewTop);
		actionBar.setTitle("日语简易词典");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.icon_actionbar;
			}

			@Override
			public void performAction(View view) {
				//finish();
				((BaseActivity)getActivity()).toggle();
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
				startActivity(new Intent(getActivity(), 
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
				startActivity(new Intent(getActivity(), 
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
				startActivity(new Intent(getActivity(), 
						//JkanjiAozoraReaderActivity.class));
						JkanjiShelfHistoryActivity.class));
			}
        });
		
        isShowBanner = JkanjiSettingActivity.getShowBanner(this.getActivity());
        useGrid = JkanjiSettingActivity.getUseGrid(this.getActivity());
        if (useGrid) {
        	isShowBanner = false;
        }
        if (isShowBanner) {
        	adapter = new MainMenuItemAdapter(this.getActivity(), models, 
        		useGrid, ((BaseActivity)getActivity()).getBitmapDrawableLruCache());
        } else {
        	if (models[models.length - 1].title == null) {
        		models2 = new MainMenuItemAdapter.BookModel[models.length - 2];
	        	for (int i = 0; i < models.length - 2; i++) {
	        		models2[i] = models[i + 1];
	        	}
        	} else {
	        	models2 = new MainMenuItemAdapter.BookModel[models.length - 1];
	        	for (int i = 0; i < models.length - 1; i++) {
	        		models2[i] = models[i + 1];
	        	}
        	}
        	adapter = new MainMenuItemAdapter(this.getActivity(), models2, 
        		useGrid, ((BaseActivity)getActivity()).getBitmapDrawableLruCache());
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
				startActivity(new Intent(getActivity(), 
						JKanjiActivity.class));
				break;
			
			case 2:
				startActivity(new Intent(getActivity(), 
						JkanjiTalkActivity.class));	
				break;
			
			case 3:
				startActivity(new Intent(getActivity(), 
						SQLiteReaderActivity.class));						
				break;

			case 4:
				startActivity(new Intent(getActivity(), JkanjiBookIndex.class)
						.putExtra(JkanjiBookIndex.EXTRA_INDEX_TYPE, JkanjiBookIndex.BOOK_INDEX_DICT));
				break;
				
			case 5:
				startActivity(new Intent(getActivity(), JkanjiBookIndex.class)
						.putExtra(JkanjiBookIndex.EXTRA_INDEX_TYPE, JkanjiBookIndex.BOOK_INDEX_GAME));
				break;
				
			case 6:
				startActivity(new Intent(getActivity(), JkanjiBookIndex.class)
						.putExtra(JkanjiBookIndex.EXTRA_INDEX_TYPE, JkanjiBookIndex.BOOK_INDEX_MARKDOWN));
				break;
				
			case 7:
				startActivity(new Intent(getActivity(), JkanjiBookIndex.class)
						.putExtra(JkanjiBookIndex.EXTRA_INDEX_TYPE, JkanjiBookIndex.BOOK_INDEX_ETC));
				break;
			
			case 8:
				startActivity(new Intent(getActivity(), 
						JkanjiSettingActivity.class));	
				break;
				
			case 9:
				Intent intent = new Intent(getActivity(), WebViewActivity.class);
				intent.putExtra(WebViewActivity.FILENAME_KEY, "help/index.html");
				startActivity(intent);
				break;
				
			case 10:
				startActivity(new Intent(getActivity(), 
						JkanjiBranchActivity.class));
				break;
			}
		}
	};
	
    @Override
	public void onResume() {
		super.onResume();
		File file = new File(JkanjiSettingActivity.getBGFileName(getActivity()));
		if (file.canRead() && file.exists() && file.isFile()) {
			imageViewTop.setImageURI(Uri.fromFile(file));			
		} else {
			imageViewTop.setImageURI(null);
		}
	}
    
    @Override
	public void onDestroy() {
		super.onDestroy();
		if (D) {
			Log.e(TAG, "onDestroy");
		}
	}
}
