package com.iteye.weimingtom.jkanji;

import java.io.File;

import jp.co.cyberagent.android.gpuimage.sample.activity.ActivityMain;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.calculator2mod.Calculator;
import com.elgubbo.sharetoclipboard.ShareToClipboardActivity;
import com.markupartist.android.widget.ActionBar;
import com.sonyericsson.zoom.JkanjiAlbumActivity;
import com.sonyericsson.zoom.JkanjiGalleryHistoryActivity;
import com.sonyericsson.zoom.JkanjiGallerySettingActivity;

public class JkanjiBookIndex extends Activity {
	public final static String EXTRA_INDEX_TYPE = "com.iteye.weimingtom.jkanji.JkanjiBookIndex.INDEX_TYPE";
	
	private int indexType = BOOK_INDEX_ETC;
	public final static int BOOK_INDEX_ETC = 0;
	public final static int BOOK_INDEX_DICT = 1;
	public final static int BOOK_INDEX_GAME = 2;
	public final static int BOOK_INDEX_MARKDOWN = 3;
	
	private AlertDialog.Builder builder1;
	private static final int DIALOG_WARNING_ID = 1;
	private final static String NOTE_TITLE = "v3.12注意事项";
	private final static String NOTE_STR = 
			"    由于android 4.x的共享预设在跨进程上可能导致读取错误，v3.12使用新方法保存设置，故将重置所有设置项，包括图库和书架的设置，但不包括图库和书架的历史记录。在此深表歉意。\n\n" + 
			"    有些杀毒软件会报此软件为开机自启动，原因是使用了桌面部件（需移动到手机内存才可用）。\n\n" + 
			"    Android 4.x以后请确保在开发者选项中USB存储保护是关闭的（Android 4.4默认打开此项），否则图库相册和所有读取数据包的功能均不能使用。\n\n" + 
			"";
	
	private String[] __items = DataContext.DIGUST_TITLES;
	
	public static String getIndexTitle(int index) {
		switch (index) {
		default:
		case BOOK_INDEX_ETC:
			return "工具";
			
		case BOOK_INDEX_DICT:
			return "词典";
			
		case BOOK_INDEX_GAME:
			return "游戏";
		
		case BOOK_INDEX_MARKDOWN:
			return "书籍";	
		}
	}
	
	public static String getIndexSubTitle(int index) {
		switch (index) {
		default:
		case BOOK_INDEX_ETC:
			return "常用工具箱";
			
		case BOOK_INDEX_DICT:
			return "基于数据包的扩展搜索功能";
			
		case BOOK_INDEX_GAME:
			return "小游戏";
		
		case BOOK_INDEX_MARKDOWN:
			return "日语学习资料";	
		}
	}

	public static int getIndexIcon(int index) {
		switch (index) {
		default:
		case BOOK_INDEX_ETC:
			return R.drawable.print_all;
			
		case BOOK_INDEX_DICT:
			return R.drawable.search;
			
		case BOOK_INDEX_GAME:
			return R.drawable.game;
		
		case BOOK_INDEX_MARKDOWN:
			return R.drawable.print;	
		}
	}
	
	
	public static JkanjiMainMenuItemAdapter.BookModel[] getIndexModels(int index) {
		switch (index) {
		default:
		case BOOK_INDEX_ETC:
			return modelsEtc;
			
		case BOOK_INDEX_DICT:
			return modelsDict;
			
		case BOOK_INDEX_GAME:
			return modelsGame;
		
		case BOOK_INDEX_MARKDOWN:
			return modelsMarkdown;	
		}		
	}
	
	private static JkanjiMainMenuItemAdapter.BookModel[] modelsEtc = {
		new JkanjiMainMenuItemAdapter.BookModel("小春音", "游戏配音素材，发音功能需要数据包", R.drawable.media), // 0
		new JkanjiMainMenuItemAdapter.BookModel("sen日语发音标注", "sen形态素分析引擎，需要数据包", R.drawable.view), // 1
		new JkanjiMainMenuItemAdapter.BookModel("双列csv查看器", "查看逗号分隔的双列对照utf8文本", R.drawable.view), // 2
		new JkanjiMainMenuItemAdapter.BookModel("ssa字幕播放器", "ssa与mp3同步播放器", R.drawable.media), // 3
		new JkanjiMainMenuItemAdapter.BookModel("网页书签", "网页书签", R.drawable.bookmark), // 4
		new JkanjiMainMenuItemAdapter.BookModel("历史与收藏夹", "搜索器的历史与收藏夹", R.drawable.view), // 5
		new JkanjiMainMenuItemAdapter.BookModel("阅读器设置", "阅读纯文本或青空文库格式的文本，使用sen分词引擎和数据包，支持假名标注和分词", R.drawable.config2), // 6
		new JkanjiMainMenuItemAdapter.BookModel("旧版主菜单（2.x）", "已废弃", R.drawable.icon_actionbar_v1), // 7
		new JkanjiMainMenuItemAdapter.BookModel("旧版帮助（2.x）", "已废弃", R.drawable.help), // 8
		new JkanjiMainMenuItemAdapter.BookModel("本地HTML测试器", "测试工具", R.drawable.view), // 9
		new JkanjiMainMenuItemAdapter.BookModel("备忘录", "内置文本收集器", R.drawable.memo), // 10
		new JkanjiMainMenuItemAdapter.BookModel("书架", "简单查看器与青空文库阅读器的历史记录", R.drawable.book), // 11
		new JkanjiMainMenuItemAdapter.BookModel("快速教程", "快速教程", R.drawable.yuno), // 12
		new JkanjiMainMenuItemAdapter.BookModel("图库", "图片翻译工具", R.drawable.gallery), // 13
		new JkanjiMainMenuItemAdapter.BookModel("图库设置", "图库和相册设置", R.drawable.config2), // 14
		new JkanjiMainMenuItemAdapter.BookModel("计算器", "系统自带计算器", R.drawable.calculator), // 15
		new JkanjiMainMenuItemAdapter.BookModel("GPUImage", "GPUImage GLES2图片滤镜", R.drawable.gpuimage), // 16
		new JkanjiMainMenuItemAdapter.BookModel("相册", "系统相册缩略图", R.drawable.album), // 17
		new JkanjiMainMenuItemAdapter.BookModel(NOTE_TITLE, NOTE_STR, R.drawable.islandwind), // 18
	};
	
	private void onItemClickEtc(int position) {
		Intent intent;
		switch (position) {
		case 0:
			intent = new Intent(JkanjiBookIndex.this, JkanjiListReaderActivity.class);
			startActivity(intent);
			break;
			
		case 1:
			intent = new Intent(JkanjiBookIndex.this, JkanjiSenActivity.class);
			startActivity(intent);
			break;	
			
		case 2:
			intent = new Intent(JkanjiBookIndex.this, JkanjiTextListActivity.class);
			startActivity(intent);					
			break;
			
		case 3:
			intent = new Intent(JkanjiBookIndex.this, MediaPlayerActivity.class);
			startActivity(intent);						
			break;
			
		case 4:
			if (false) {
				startActivity(new Intent(JkanjiBookIndex.this, DictPreference.class));
			} else {
				startActivity(new Intent(JkanjiBookIndex.this, JkanjiWebsiteActivity.class));
			}
			break;
			
		case 5:
			startActivity(new Intent(JkanjiBookIndex.this, JkanjiHistoryActivity.class));
			break;
			
		case 6:
			startActivity(new Intent(JkanjiBookIndex.this, JkanjiAozoraReaderActivity.class));
			break;
			
		case 7:
			startActivity(new Intent(JkanjiBookIndex.this, JkanjiLauncher.class));
			break;
			
		case 8:
			startActivity(new Intent(JkanjiBookIndex.this, AboutActivity.class));
			break;
			
		case 9:
			startActivity(new Intent(JkanjiBookIndex.this, HtmltestActivity.class));
			break;
			
		case 10:
			intent = new Intent(JkanjiBookIndex.this, ShareToClipboardActivity.class);
			startActivity(intent);
			break;
			
		case 11:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiShelfHistoryActivity.class));
			break;
			
		case 12:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiTutorialActivity.class));
			break;
			
		case 13:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiGalleryHistoryActivity.class));
			break;
			
		case 14:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiGallerySettingActivity.class)
			);
			break;
			
		case 15:
			startActivity(new Intent(JkanjiBookIndex.this, 
					Calculator.class));
			break;
			
		case 16:
			startActivity(new Intent(JkanjiBookIndex.this, 
					ActivityMain.class));
			break;
			
		case 17:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiAlbumActivity.class));
			break;
			
		case 18:
			showDialog(DIALOG_WARNING_ID);
			break;
		}
	}
	
	private static JkanjiMainMenuItemAdapter.BookModel[] modelsDict = {
		new JkanjiMainMenuItemAdapter.BookModel("sqlite搜索器", "SQLite数据库（特定表结构）搜索引擎，需要数据包，用于搜索EDICT、WadokuJT和MuiltDic等词典和WWWJDIC的日英例句数据库", R.drawable.search_sqlite), // 0
		new JkanjiMainMenuItemAdapter.BookModel("epwing搜索器", "epwing格式字典，需要在全局设置中指定路径，目前仅测试支持広辞苑5和DreyeJC中日日中辞書", R.drawable.search), // 1
		new JkanjiMainMenuItemAdapter.BookModel("浩叡日中词典", "浩叡日中词典sqlite数据库搜索器", R.drawable.search), // 2
		new JkanjiMainMenuItemAdapter.BookModel("日文词库", "内置日文词库列表，支持发音（需安装N2 TTS），在启用和加载完搜索器缓存的情况下可加快滑动速度", R.drawable.view), // 3
		new JkanjiMainMenuItemAdapter.BookModel("英文词库", "内置英文词库列表（测试版，未校对），在启用和加载完搜索器缓存的情况下可加快滑动速度", R.drawable.view), // 4
		new JkanjiMainMenuItemAdapter.BookModel("日中中日辞書", "日中中日辞書（ピンイン付け版）", R.drawable.search), // 5
		new JkanjiMainMenuItemAdapter.BookModel("书籍搜索", "内置书籍搜索", R.drawable.search), // 6
	};
	
	private void onItemClickDict(int position) {
		Intent intent;
		switch (position) {
		case 0:
			intent = new Intent(JkanjiBookIndex.this, SQLiteReaderActivity.class);
			startActivity(intent);						
			break;
			
		case 1:
			startActivity(new Intent(JkanjiBookIndex.this, JkanjiEb4jActivity.class));
			break;
			
		case 2:
			intent = new Intent(JkanjiBookIndex.this, JkanjiHorryActivity.class);
			startActivity(intent);
			break;
			
		case 3:
			startActivity(new Intent(JkanjiBookIndex.this, JkanjiViewerActivity.class)
					.putExtra(JkanjiViewerActivity.EXTRA_DATA_TYPE, JkanjiViewerActivity.DATA_TYPE_JPWORDS)
			);
			break;
	
		case 4:
			startActivity(new Intent(JkanjiBookIndex.this, JkanjiViewerActivity.class)
					.putExtra(JkanjiViewerActivity.EXTRA_DATA_TYPE, JkanjiViewerActivity.DATA_TYPE_ENWORDS)
			);
			break;
			
		case 5:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiCJJCPinyinActivity.class));
			break;
			
		case 6:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiBookSearchActivity.class));
			break;
		}
	}
	
	private static JkanjiMainMenuItemAdapter.BookModel[] modelsGame = {
		new JkanjiMainMenuItemAdapter.BookModel("小游戏（游魂quiz改）", "看汉字猜发音", R.drawable.game), // 0
		new JkanjiMainMenuItemAdapter.BookModel("神経衰弱", "学习五十音的小游戏", R.drawable.cardgame), // 1
		new JkanjiMainMenuItemAdapter.BookModel("小游戏（未命名）", "工事中", R.drawable.game), // 2
		new JkanjiMainMenuItemAdapter.BookModel("kikya", "kikya弹幕游戏移植版", R.drawable.game), // 3
		new JkanjiMainMenuItemAdapter.BookModel("黑白棋争胜", "黑白棋争胜andengine版", R.drawable.game), // 4
		new JkanjiMainMenuItemAdapter.BookModel("五子棋争胜", "五子棋争胜andengine版", R.drawable.game), // 5
	};
	
	private void onItemClickGame(int position) {
		startActivity(new Intent(JkanjiBookIndex.this, 
				AboutGamesActivity.class));
		
		/*
		Intent intent;
		switch (position) {
		case 0:
			startActivity(new Intent(JkanjiBookIndex.this, JkanjiGameActivity.class));
			break;
			
		case 1:
			startActivity(new Intent(JkanjiBookIndex.this, JkanjiCardGameActivity.class));
			break;

		case 2:
			intent = new Intent(JkanjiBookIndex.this, WebViewGameActivity.class);
			intent.putExtra(WebViewGameActivity.FILENAME_KEY, "www/game/index.html");
			startActivity(intent);
			break;
			
		case 3:
			startActivity(new Intent(JkanjiBookIndex.this, 
					MainFrame.class));
			break;
			
		case 4:
			startActivity(new Intent(JkanjiBookIndex.this,
					ReversiActivity.class));
			break;
		
		case 5:
			startActivity(new Intent(JkanjiBookIndex.this,
					GomokuActivity.class));
			break;
		}
		*/
	}
	
	private static JkanjiMainMenuItemAdapter.BookModel[] modelsMarkdown = {
		new JkanjiMainMenuItemAdapter.BookModel("日语口语型", "日语口语缩略用法笔记", R.drawable.print), // 0
		new JkanjiMainMenuItemAdapter.BookModel("常用漢字表", "学习日语的音读与训读", R.drawable.print), // 1
		new JkanjiMainMenuItemAdapter.BookModel("助词语法", "日语助词语法（测试版）", R.drawable.print), // 2
		new JkanjiMainMenuItemAdapter.BookModel("综合文例语法摘录", "综合文例日语语法摘录284条（测试版）", R.drawable.print), // 3
		new JkanjiMainMenuItemAdapter.BookModel("教育汉字", "日语汉字音读与训读", R.drawable.print), // 4
		new JkanjiMainMenuItemAdapter.BookModel("歌词收集", "自译和收集的日文歌词", R.drawable.print), // 5
		new JkanjiMainMenuItemAdapter.BookModel("英语名言", "英语谚语与名人名言", R.drawable.print), // 6
		new JkanjiMainMenuItemAdapter.BookModel("流行日语", "10小时速成最简单的流行日语", R.drawable.print), // 7
		new JkanjiMainMenuItemAdapter.BookModel("日语口语", "星星之火日语口语900句", R.drawable.print), // 8
		new JkanjiMainMenuItemAdapter.BookModel("句型840个", "日语最该掌握的句型840个", R.drawable.print), // 9
		new JkanjiMainMenuItemAdapter.BookModel("动词活用", "日语动词活用规则", R.drawable.print), // 10
		new JkanjiMainMenuItemAdapter.BookModel("常用惯用句", "日语常用惯用句668", R.drawable.print), // 11
		new JkanjiMainMenuItemAdapter.BookModel("常用谚语", "日语常用谚语350", R.drawable.print), // 12
		new JkanjiMainMenuItemAdapter.BookModel("常用汉字", "常用汉字2011", R.drawable.print), // 13
		new JkanjiMainMenuItemAdapter.BookModel("惯用句型大全", "日语经典惯用句型大全1137", R.drawable.print), // 14
	};
	
	private void onItemClickMarkdown(int position) {
		Intent intent;
		switch (position) {
		case 0:
			intent = new Intent(JkanjiBookIndex.this, WebViewActivity.class);
			intent.putExtra(WebViewActivity.FILENAME_KEY, "digust_1.html");
			startActivity(intent);
			break;

		case 1:
			intent = new Intent(JkanjiBookIndex.this, WebViewActivity.class);
			intent.putExtra(WebViewActivity.FILENAME_KEY, "digust_2.html");
			startActivity(intent);
			break;

		case 2:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiMarkdownIndexActivity.class)
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_PATH, "markdown/book001/")
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_TITLE, "助词语法")
			);	
			break;

		case 3:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiMarkdownIndexActivity.class)
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_PATH, "markdown/book002/")
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_TITLE, "综合文例")
			);
			break;
			
		case 4:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiJgrKanjiActivity.class));					
			break;

		case 5:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiMarkdownIndexActivity.class)
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_PATH, "markdown/book003/")
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_TITLE, "歌词收集")
			);	
			break;
			
		case 6:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiMarkdownIndexActivity.class)
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_PATH, "markdown/book004/")
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_TITLE, "英语名言")
			);	
			break;

		case 7:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiMarkdownIndexActivity.class)
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_PATH, "markdown/book005/")
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_TITLE, "流行日语")
			);	
			break;
			
		case 8:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiMarkdownIndexActivity.class)
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_PATH, "markdown/book006/")
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_TITLE, "日语口语")
			);	
			break;
			
		case 9:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiMarkdownIndexActivity.class)
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_PATH, "markdown/book007/")
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_TITLE, "句型840个")
			);	
			break;
			
		case 10:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiMarkdownIndexActivity.class)
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_PATH, "markdown/book008/")
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_TITLE, "动词活用")
			);
			break;
			
		case 11:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiMarkdownIndexActivity.class)
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_PATH, "markdown/book009/")
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_TITLE, "日语常用惯用句668")
			);
			break;
			
		case 12:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiMarkdownIndexActivity.class)
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_PATH, "markdown/book010/")
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_TITLE, "日语常用谚语350")
			);
			break;
			
		case 13:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiMarkdownIndexActivity.class)
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_PATH, "markdown/book011/")
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_TITLE, "日语常用汉字2011")
			);
			break;
			
		case 14:
			startActivity(new Intent(JkanjiBookIndex.this, 
					JkanjiMarkdownIndexActivity.class)
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_PATH, "markdown/book012/")
					.putExtra(JkanjiMarkdownIndexActivity.EXTRA_TITLE, "惯用句型大全")
			);
			break;
		}
	}
	
	
	private JkanjiMainMenuItemAdapter adapter;
	private ActionBar actionBar;
	private ListView listViewBookIndex;
	private GridView gridViewBookIndex;
	private ImageView imageViewTop;
	private boolean useGrid;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.book_index);

        Intent intent = this.getIntent();
        if (intent != null) {
        	indexType = intent.getIntExtra(EXTRA_INDEX_TYPE, BOOK_INDEX_ETC);
        }
        
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		imageViewTop = (ImageView) findViewById(R.id.imageViewTop);
		actionBar.setTitle(getIndexTitle(indexType));
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				//return R.drawable.print_all;
				return getIndexIcon(indexType);
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
				//return R.drawable.icon_actionbar;
				return R.drawable.book;
			}

			@Override
			public void performAction(View view) {
				startActivity(new Intent(JkanjiBookIndex.this, 
						JkanjiAozoraReaderActivity.class));
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				return R.drawable.game;
			}

			@Override
			public void performAction(View view) {
				startActivity(new Intent(JkanjiBookIndex.this, 
						JkanjiGameActivity.class));
			}
        });
        */
        
        listViewBookIndex = (ListView) this.findViewById(R.id.listViewBookIndex);
        gridViewBookIndex = (GridView) this.findViewById(R.id.gridViewBookIndex);
        
        useGrid = JkanjiSettingActivity.getUseGrid(this);
        
        adapter = new JkanjiMainMenuItemAdapter(this, getIndexModels(indexType), useGrid);
        listViewBookIndex.setAdapter(adapter);
        listViewBookIndex.setOnItemClickListener(mOnitemclickListener);
        gridViewBookIndex.setAdapter(adapter);
        gridViewBookIndex.setOnItemClickListener(mOnitemclickListener);
        
        builder1 = new AlertDialog.Builder(this);
		
        if (useGrid) {
            listViewBookIndex.setVisibility(ListView.INVISIBLE);
            gridViewBookIndex.setVisibility(GridView.VISIBLE);	
        } else {
            listViewBookIndex.setVisibility(ListView.VISIBLE);
            gridViewBookIndex.setVisibility(GridView.INVISIBLE);
        }
    }
    
    private OnItemClickListener mOnitemclickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			switch (indexType) {
			default:
			case BOOK_INDEX_ETC:
				onItemClickEtc(position);
				break;
				
			case BOOK_INDEX_DICT:
				onItemClickDict(position);
				break;
				
			case BOOK_INDEX_GAME:
				onItemClickGame(position);
				break;
				
			case BOOK_INDEX_MARKDOWN:
				onItemClickMarkdown(position);
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
		if (adapter != null) {
			adapter.destory();
		}
	}
	
	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
 	    Dialog dialog = null;
 	    switch(id) {
 	    case DIALOG_WARNING_ID:
     		return builder1
     			.setTitle(NOTE_TITLE)
     			.setMessage(NOTE_STR)
     			.setIcon(R.drawable.islandwind)
     			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
     				public void onClick(DialogInterface dialog, int id) {
     					
     				}
     			})
     	       .create();
 	    }
 	    return dialog;
	}
}
