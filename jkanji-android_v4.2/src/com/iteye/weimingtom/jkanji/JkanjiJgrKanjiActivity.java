package com.iteye.weimingtom.jkanji;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class JkanjiJgrKanjiActivity extends Activity implements OnScrollListener {
	private final static boolean D = false;
	private final static String TAG = "JkanjiJgrKanjiActivity";
	
	private final static String JGR_FILE_NAME = "jgr_kanji.txt";
	
	private static final String SHARE_PREF_NAME = "pref";
	private static final String SHARE_PREF_LIST_POS_JGR = "listPosJgrKanji";
	private static final String SHARE_SEARCH_VISIBLE_JGR = "searchVisibleJgrKanji";
	
	private ActionBar actionBar;
	private GridView gridView;
	private ScrollView scrollViewLoading;
	private TextView textViewKanjiDetail;
	private LinearLayout linearLayoutOutput;
	private LinearLayout linearLayoutSearch; 
	private EditText editTextKanji;
	private Button buttonChange;
	private Button buttonClear;
	private Button buttonSearch;
	private CheckBox checkBoxJumpSearch;
	
	private ArrayList<String> kanjiData;
	private ArrayList<String> kanjiDetail;
	private Typeface typeface;
	private ArrayAdapter<String> adapter;
	
	private int listPos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.jgr_kanji);

		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("教育汉字");
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
				return R.drawable.config;
			}
			
			@Override
			public void performAction(View view) {
				if (linearLayoutSearch.getVisibility() == LinearLayout.VISIBLE) {
					linearLayoutSearch.setVisibility(LinearLayout.GONE);
					setSearchVisible(false);
				} else {
					linearLayoutSearch.setVisibility(LinearLayout.VISIBLE);
					setSearchVisible(true);
				}
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.shareto;
			}

			@Override
			public void performAction(View view) {
				String str = textViewKanjiDetail.getText().toString();
				
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, str);
                intent.putExtra(Intent.EXTRA_TEXT, str);
                //startActivity(Intent.createChooser(intent, "共享方式"));
                try {
                	startActivity(intent);
                } catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(JkanjiJgrKanjiActivity.this, 
						"共享方式出错", Toast.LENGTH_SHORT)
						.show();
                }
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.search;
			}

			@Override
			public void performAction(View view) {
				String searchStr = "";
				String str = textViewKanjiDetail.getText().toString();
				if (str != null) {
					String[] records = str.split(",");
					if (records != null && records.length > 1 && records[0].length() >= 1) {
						searchStr = records[0].substring(0, 1);
					}
				}
				startActivity(new Intent(JkanjiJgrKanjiActivity.this, 
					JKanjiActivity.class)
					.putExtra(JKanjiActivity.EXTRA_KEY_SHEARCHTEXT, searchStr));
			}
        });
        
        
        scrollViewLoading = (ScrollView) findViewById(R.id.scrollViewLoading);
        gridView = (GridView) findViewById(R.id.gridView1);
        textViewKanjiDetail = (TextView) findViewById(R.id.textViewKanjiDetail);
        linearLayoutOutput = (LinearLayout) findViewById(R.id.linearLayoutOutput);
        linearLayoutSearch = (LinearLayout) findViewById(R.id.linearLayoutSearch);
        editTextKanji = (EditText) findViewById(R.id.editTextKanji);
        buttonChange = (Button) findViewById(R.id.buttonChange);
        buttonClear = (Button) findViewById(R.id.buttonClear);
        buttonSearch = (Button) findViewById(R.id.buttonSearch);
        checkBoxJumpSearch = (CheckBox) findViewById(R.id.checkBoxJumpSearch);
        
        buttonChange.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (JKanjiActivity.getCacheGB2SJ() == null) {
					Toast.makeText(JkanjiJgrKanjiActivity.this, 
						"未加载汉字变换表", Toast.LENGTH_SHORT).show();
				} else {
					String str = editTextKanji.getEditableText().toString();
					int selpos = editTextKanji.getSelectionStart() - 1;
					String right2 = nextKanji(str, selpos);
					if (right2 != null) {
						editTextKanji.getEditableText().replace(selpos,
								editTextKanji.getSelectionEnd(), right2);
					}
				}
			}
        });
        buttonClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				textViewKanjiDetail.setText("");
				editTextKanji.setText("");
			}
        });
        buttonSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String searchStr = editTextKanji.getText().toString();
				if (kanjiData != null && kanjiDetail != null &&
					searchStr != null && searchStr.length() > 0) {
					searchStr = searchStr.substring(0, 1);
					boolean isFound = false;
					for (int i = 0; i < kanjiData.size(); i++) {
						if (searchStr.equals(kanjiData.get(i))) {
							isFound = true;
							gridView.setSelection(i);
							listPos = i;
							setLastListPos(listPos);
							//textViewKanjiDetail.setText(kanjiDetail.get(i));
							break;
						}
					}
					if (!isFound) {
						textViewKanjiDetail.setText("找不到：" + searchStr + "，请尝试变换汉字");
					}
				}
			}
        });
        
        typeface = Typefaces.getFile(JkanjiSettingActivity.getFontFileName(this));	
		textViewKanjiDetail.setTypeface(typeface);
        
        scrollViewLoading.setVisibility(ScrollView.VISIBLE);
        linearLayoutOutput.setVisibility(LinearLayout.INVISIBLE);
        
        kanjiData = new ArrayList<String>();
        kanjiDetail = new ArrayList<String>();

        adapter = new ArrayAdapter<String>(this, R.layout.grid_text_view, kanjiData);
        gridView.setFastScrollEnabled(true);
        gridView.setAdapter(adapter);
        listPos = getLastListPos();
        gridView.setOnScrollListener(this);
        gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				if (!checkBoxJumpSearch.isChecked()) {
					String detail = null;
					if (position < kanjiDetail.size()) {
						detail = kanjiDetail.get(position);
					}
					textViewKanjiDetail.setText(detail);					
				} else {
					startSearchActivity(position);
				}
			}
        });
        gridView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
				startSearchActivity(position);
				return true;
			}
        });
        gridView.postDelayed(new Runnable() {
			@Override
			public void run() {
				loadData(JGR_FILE_NAME);
			    scrollViewLoading.setVisibility(ScrollView.INVISIBLE);
			    linearLayoutOutput.setVisibility(LinearLayout.VISIBLE);
				gridView.setSelection(listPos);
//			    Toast.makeText(JkanjiJgrKanjiActivity.this, 
//			    		"加载完成", Toast.LENGTH_SHORT).show();
			}
        }, 500);
        
        if (getSearchVisible()) {
			linearLayoutSearch.setVisibility(LinearLayout.VISIBLE);
		} else {
			linearLayoutSearch.setVisibility(LinearLayout.GONE);
		}
	}
	
	private void startSearchActivity(int position) {
		String detail = null;
		if (position < kanjiDetail.size()) {
			detail = kanjiDetail.get(position);
		}
		String searchStr = "";
		if (detail != null) {
			String[] records = detail.split(",");
			if (records != null && records.length > 1 && records[0].length() >= 1) {
				searchStr = records[0].substring(0, 1);
			}
		}
		startActivity(new Intent(JkanjiJgrKanjiActivity.this, 
				JKanjiActivity.class)
				.putExtra(JKanjiActivity.EXTRA_KEY_SHEARCHTEXT, searchStr));
	}
	
    private String nextKanji(String str, int sepIndex) {
    	ArrayList<String> cacheGB2SJ = JKanjiActivity.getCacheGB2SJ();
    	if (str == null || str.length() <= 0 || 
			sepIndex < 0 || sepIndex >= str.length() ||
			cacheGB2SJ == null) {
			return null;
		}
		Character right = str.charAt(sepIndex);
		for (String entry : cacheGB2SJ) {
			if (entry.indexOf(right) >= 0 && entry.length() > 1) {
				int index = entry.indexOf(right) + 1;
				if (index > entry.length() - 1) {
					index = 0;
				}
				return Character.toString(entry.charAt(index));
			}
		}
		return null;
    }
	
    private void loadData(String filename) {
    	adapter.clear();
		InputStream istr = null;
		InputStreamReader reader = null;
		BufferedReader rbuf = null;
		try {
			istr = this.getAssets().open(filename);
			reader = new InputStreamReader(istr, "utf8");
			rbuf = new BufferedReader(reader);
			String line;
			while (null != (line = rbuf.readLine())) {
				String[] records = line.split(",");
				if (records != null && 
					records.length > 1 && 
					records[0] != null &&
					records[0].length() >= 1) {
					kanjiData.add(records[0].substring(0, 1));
					kanjiDetail.add(line);
				}
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
		adapter.notifyDataSetChanged();
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		setLastListPos(this.listPos);
	}
    
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		
	}
    
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE){
        	listPos = view.getFirstVisiblePosition();
		}
	}
	
    private void setLastListPos(int listPos) {
    	PrefUtil.putInt(this, SHARE_PREF_NAME,
    			SHARE_PREF_LIST_POS_JGR,
    			listPos);
    }
    
    private int getLastListPos() {
    	return PrefUtil.getInt(this, SHARE_PREF_NAME, 
    			SHARE_PREF_LIST_POS_JGR,
    			0);
    }

    private void setSearchVisible(boolean searchVisible) {
    	PrefUtil.putBoolean(this, SHARE_PREF_NAME,
    			SHARE_SEARCH_VISIBLE_JGR,
    			searchVisible);
    }
    
    private boolean getSearchVisible() {
    	return PrefUtil.getBoolean(this, SHARE_PREF_NAME, 
    			SHARE_SEARCH_VISIBLE_JGR,
    			true);
    }
}
