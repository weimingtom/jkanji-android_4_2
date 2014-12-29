package org.nick.wwwjdic.krad;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnShowListener;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class KradChart extends Activity implements OnClickListener, OnItemClickListener {
    private static final boolean D = false;
    private static final String TAG = "KradChart";
    
    private static final int DIALOG_LIST = 2;
    
    public static final String KEY_CHAR_SELECT = "KEY_CHAR_SELECT";
    
    private static final int NUM_SUMMARY_CHARS = 5;

    private static final List<String> NUM_STROKES = Arrays.asList(new String[] {
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
            "13", "14", "17" });
	private static final String[] STROKES = {
		"一｜丶ノ乙亅",
		"二亠人亻个儿入ハ并冂冖冫几凵刀刂力勹匕匚十卜卩厂厶又マ九ユ乃",
		"辶口囗土士夂夕大女子宀寸小尚尢尸屮山川巛工已巾干幺广廴廾弋弓ヨ彑彡彳忄扌氵犭艹邦阡也亡及久",
		"耂心戈戸手支攵文斗斤方无日曰月木欠止歹殳比毛氏气水火灬爪父爻爿片牛犬礻王元井勿尤五屯巴毋",
		"玄瓦甘生用田疋疒癶白皮皿目矛矢石示禸禾穴立衤世巨冊母罒牙",
		"瓜竹米糸缶羊羽而耒耳聿肉自至臼舌舟艮色虍虫血行衣西",
		"臣見角言谷豆豕豸貝赤走足身車辛辰酉釆里舛麦",
		"金長門隶隹雨青非奄岡免斉",
		"面革韭音頁風飛食首香品",
		"馬骨高髟鬥鬯鬲鬼竜韋",
		"魚鳥鹵鹿麻亀滴黄黒",
		"黍黹無歯",
		"黽鼎鼓鼠",
		"鼻齊",
		"",
		"",
		"龠",
	};
    private static final List<String> REPLACED_CHARS = Arrays.asList(new String[] { 
        "亻", "个", 
        "辶", "尚", "艹", "邦", "阡", 
        "耂"
    });

    private List<String> radicals = new ArrayList<String>();

    private static final String STATE_KEY = "org.nick.wwwjdic.kradChartState";
    
    static class State implements Serializable {
        private static final long serialVersionUID = -6074503793592867534L;

        Set<String> selectedRadicals = new HashSet<String>();
        Set<String> enabledRadicals = new HashSet<String>();
        Set<String> matchingKanjis = new HashSet<String>();
    }

    private State state = new State();

    private ActionBar actionBar;
    private TextView matchedKanjiText;
    private TextView totalMatchesText;
    private Button showAllButton;
    private Button clearButton;

    private GridView radicalChartGrid;
    private KradAdapter adapter;

    private static KradDb kradDb;
    private AlertDialog.Builder builder;
    private ArrayAdapter<String> selectAdapter;
    
    private TextView textViewLoading;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.krad_chart);

        actionBar = (ActionBar) findViewById(R.id.actionbar);
        matchedKanjiText = (TextView) findViewById(R.id.matched_kanji);
        totalMatchesText = (TextView) findViewById(R.id.total_matches);
        textViewLoading = (TextView) findViewById(R.id.textViewLoading);
        builder = new AlertDialog.Builder(this);
        displayTotalMatches();

		selectAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.select_dialog_item);
        
        showAllButton = (Button) findViewById(R.id.show_all_button);
        showAllButton.setOnClickListener(this);
        clearButton = (Button) findViewById(R.id.clear_button);
        clearButton.setOnClickListener(this);
        toggleButtons();

        radicalChartGrid = (GridView) findViewById(R.id.kradChartGrid);
        radicalChartGrid.setOnItemClickListener(this);

        actionBar.setTitle("wwwjdic部首输入");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.write_input;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
        
//        kradDb = null;
//        if (kradDb == null) {
//        		Toast.makeText(this, "未加载数据，无法检索汉字", Toast.LENGTH_SHORT).show();
//        }
        
        for (String numStrokesStr : NUM_STROKES) {
            String labelStr = new String(numStrokesStr);
            radicals.add(labelStr);
            int index = Integer.parseInt(numStrokesStr) - 1;
            if (index >= 0 && index < STROKES.length && STROKES[index].length() > 0) {
                String[] radicalArr = new String[STROKES[index].length()];
                for (int i = 0; i < radicalArr.length; i++) {
                	radicalArr[i] = STROKES[index].substring(i, i + 1);
                }
                radicals.addAll(Arrays.asList(radicalArr));
            }
        }
        
        adapter = new KradAdapter(KradChart.this,
                R.layout.krad_item, radicals);
        radicalChartGrid.setAdapter(adapter);
        
        clearSelection();
        
        if (kradDb == null) {
        	new LoadDataTask(this).execute();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        state = (State) savedInstanceState.getSerializable(STATE_KEY);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_KEY, state);
    }

    private void displayTotalMatches() {
        String totalMatchesTemplate = "%d个匹配项";
        totalMatchesText.setText(String.format(totalMatchesTemplate,
                state.matchingKanjis.size()));
    }

    private void enableAllRadicals() {
        for (String radical : radicals) {
            if (!isStrokeNumLabel(radical)) {
                state.enabledRadicals.add(radical);
            }
        }
    }

    public class KradAdapter extends ArrayAdapter<String> {
        public KradAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            TextView result = (TextView) super.getView(position, convertView, viewGroup);
            result.setTextColor(Color.BLACK);
            result.setBackgroundColor(Color.WHITE);
            
            String modelStr = getItem(position);
            if (isStrokeNumLabel(modelStr)) {
                result.setBackgroundColor(Color.GRAY);
                result.setTextColor(Color.WHITE);
            } else {
                String radical = modelStr;
                String displayStr = radical;
                result.setText(displayStr);
                if (REPLACED_CHARS.contains(displayStr)) {
                    result.setTextColor(Color.BLUE);
                }
            }
            if (isSelected(modelStr)) {
                result.setBackgroundColor(Color.GREEN);
            }
            if (isDisabled(modelStr)) {
                result.setBackgroundColor(Color.DKGRAY);
            }
            return result;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            String text = getItem(position);
            return !isStrokeNumLabel(text) && !isDisabled(text);
        }
    }

    private static boolean isStrokeNumLabel(String str) {
        return NUM_STROKES.contains(str);
    }

    private boolean isSelected(String radical) {
        return state.selectedRadicals.contains(radical);
    }

    private boolean isDisabled(String radical) {
        return !isStrokeNumLabel(radical) && 
        	!state.enabledRadicals.contains(radical);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
    	if (kradDb == null) {
            Toast.makeText(this, "未加载数据，无法检索汉字", Toast.LENGTH_SHORT).show();
            return;
    	}
    	
        String radical = radicals.get(position);
        if (state.selectedRadicals.contains(radical)) {
            state.selectedRadicals.remove(radical);
        } else {
            state.selectedRadicals.add(radical);
        }

        if (state.selectedRadicals.isEmpty()) {
            enableAllRadicals();
            state.matchingKanjis.clear();
            matchedKanjiText.setText("无匹配");
        } else {
            state.matchingKanjis = kradDb.getKanjisForRadicals(state.selectedRadicals);
            addClickableKanji(matchedKanjiText);
            if (D) {
            	Log.d(TAG, "matching kanjis: " + state.matchingKanjis);
            }
            state.enabledRadicals = kradDb.getRadicalsForKanjis(state.matchingKanjis);
            if (D) {
            	Log.d(TAG, "enabled radicals: " + state.enabledRadicals);
            }
        }

        toggleButtons();

        displayTotalMatches();

        adapter.notifyDataSetChanged();
    }

    private void addClickableKanji(TextView textView) {
        if (state.matchingKanjis.isEmpty()) {
            return;
        }

        String[] matchingChars = state.matchingKanjis
                .toArray(new String[state.matchingKanjis.size()]);
        Arrays.sort(matchingChars);

        String[] charsToDisplay = new String[NUM_SUMMARY_CHARS];
        if (matchingChars.length < charsToDisplay.length) {
            charsToDisplay = new String[matchingChars.length];
        }
        System.arraycopy(matchingChars, 0, charsToDisplay, 0,
                charsToDisplay.length);
        String text = TextUtils.join(" ", charsToDisplay);
        String ellipsis = "...";
        if (matchingChars.length > charsToDisplay.length) {
            text += " " + ellipsis;
        }
        SpannableString str = new SpannableString(text);

        for (String c : charsToDisplay) {
            int idx = text.indexOf(c);
            if (idx != -1) {
                int end = idx + 1;
                if (end > str.length() - 1) {
                    end = str.length();
                }
                str.setSpan(new IntentSpan(this, text.substring(idx, idx + 1)), idx, idx + 1,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }

        int idx = text.indexOf(ellipsis);
        if (idx != -1) {
            str.setSpan(new IntentSpan(this, null), idx,
                    idx + ellipsis.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        }

        textView.setText(str);
        textView.setLinkTextColor(Color.BLACK);
        MovementMethod m = textView.getMovementMethod();
        if ((m == null) || !(m instanceof LinkMovementMethod)) {
            if (textView.getLinksClickable()) {
                textView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    private void toggleButtons() {
        boolean matchesFound = !state.matchingKanjis.isEmpty();
        showAllButton.setEnabled(matchesFound);
        //clearButton.setEnabled(matchesFound);
        clearButton.setEnabled(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.show_all_button:
            showCandidates();
            break;
            
        case R.id.clear_button:
            clearSelection();
            break;
        
        default:
            // do nothing
        	break;
        }
    }

    private void clearSelection() {
        state.selectedRadicals.clear();
        state.matchingKanjis.clear();
        enableAllRadicals();
        matchedKanjiText.setText("无匹配");
        displayTotalMatches();
        toggleButtons();
        adapter.notifyDataSetChanged();
    }

    private void showCandidates() {
    	showDialog(DIALOG_LIST);
    }
    
    private final class IntentSpan extends ClickableSpan {
        private Context context;
        private String selectChar;

        public IntentSpan(Context context, String selectChar) {
            this.context = context;
            this.selectChar = selectChar;
        }

        @Override
        public void onClick(View widget) {
        	if (selectChar != null) {
	        	Intent data = new Intent();
	        	data.putExtra(KEY_CHAR_SELECT, this.selectChar);
	        	setResult(RESULT_OK, data);
	        	finish();
	        } else {
	        	showDialog(DIALOG_LIST);
	        }
        }
    }
    
    @Override
	protected Dialog onCreateDialog(int id) {
    	Dialog dialog = null;
	    switch(id) {
        case DIALOG_LIST:
    		dialog = builder
                .setTitle("候选字")
                .setAdapter(selectAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	String str = selectAdapter.getItem(which);
        	        	Intent data = new Intent();
        	        	data.putExtra(KEY_CHAR_SELECT, str);
        	        	setResult(RESULT_OK, data);
                    	finish();
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
					selectAdapter.clear();
					String[] matchingChars = state.matchingKanjis
			                .toArray(new String[state.matchingKanjis.size()]);
			        if (matchingChars != null) {
			        	Arrays.sort(matchingChars);
			        	for (String str : matchingChars) {
			        		if (str != null) {
			        			selectAdapter.add(str);
			        		}
			        	}
			        }
				}
    		});
            return dialog;
	    }
	    return dialog;
    }
    
	private final static class LoadDataTask extends AsyncTask<Void, Void, Boolean> {
		private boolean loadResult = false;
		private WeakReference<KradChart> act;
		private KradDb kradDbTemp;
		
		public LoadDataTask(KradChart activity) {
			act = new WeakReference<KradChart>(activity);
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			act.get().radicalChartGrid.setVisibility(View.INVISIBLE);
			act.get().textViewLoading.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				InputStream istr = act.get().getAssets().open("radkfile-u-jis208.txt");
				InputStreamReader reader = new InputStreamReader(istr, "UTF-8");
				BufferedReader buf = new BufferedReader(reader);
				
				kradDbTemp = new KradDb();
				String line = null;
				while ((line = buf.readLine()) != null) {
					kradDbTemp.readLine(line);
				}
				
				buf.close();
				reader.close();
				istr.close();
				
				loadResult = true;
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			act.get().radicalChartGrid.setVisibility(View.VISIBLE);
			act.get().textViewLoading.setVisibility(View.INVISIBLE);
			if (result == true && !act.get().isFinishing()) {
				if (loadResult) {
					kradDb = this.kradDbTemp;
				} else {
					
				}
			} else if (result == false) {
				act.get().finish();
			}
			this.kradDbTemp = null;
		}
    }
}
