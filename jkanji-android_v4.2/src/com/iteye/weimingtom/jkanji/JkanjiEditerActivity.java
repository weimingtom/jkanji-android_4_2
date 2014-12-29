package com.iteye.weimingtom.jkanji;

import java.util.ArrayList;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class JkanjiEditerActivity extends Activity {
	private static final boolean D = false;
	private static final String TAG = "JkanjiEditerActivity";
	
	private ActionBar actionBar;
	private ListView listViewResultList;
	private ArrayList<Word> data;
	private String keyword;
	private Typeface typeface;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.result_list);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("搜索结果列表");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				return R.drawable.jump;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
		
		listViewResultList = (ListView) this.findViewById(R.id.listViewResultList);
		listViewResultList.setAdapter(new EfficientAdapter(this));
		listViewResultList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startActivity(new Intent(JkanjiEditerActivity.this, WordEditActivity.class)
					.putExtra(WordEditActivity.EXTRA_DATA, data.get(position)));
			}
		});
		listViewResultList.setFastScrollEnabled(true);
		
		Intent intent = this.getIntent();
		data = intent.getParcelableArrayListExtra(JKanjiActivity.EXTRA_KEY);
		keyword = intent.getStringExtra(JKanjiActivity.EXTRA_KEYWORD);
		if (!DataContext.DONNOT_LOAD_TYPEFACE) {
			//typeface = Typeface.createFromAsset(getAssets(), "mplus-1m-regular.ttf");
			//typeface = Typefaces.get(this, "mplus-1m-regular.ttf");
			typeface = Typefaces.getFile(JkanjiSettingActivity.getFontFileName(this));	
		}
		if (data != null) {
			actionBar.setTitle("搜索结果列表" + "(" + data.size() + ")");
		}
	}
	
    private class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private Context mContext;

        public EfficientAdapter(Context context) {
        	mContext = context;
            mInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return data != null ? data.size() : 0;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item, null);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.title.setTypeface(typeface);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            String title = "";
            String text = "";
            Word word = null;
            if (data != null && position >= 0 && position < data.size()) {
            	word = data.get(position);
            }
            if (word != null) {
				String reading = (word.reading != null ? ("【" + word.reading + "】") : "");
				String reading2 = (word.reading != null ? word.reading : "");
				String kanji = ((word.kanji != null && word.kanji.length() > 0) ? word.kanji : reading2);
				title = kanji + reading + word.getAccent(); 
				text = ((word.catalog != null && word.catalog.length() > 0) ? "【" + word.catalog + "】\n" : "") +
					((word.mean != null) ? CharTrans.formatMean(word.mean, false) : "");
			}
            holder.title.setText(getColorString(title, keyword));
            holder.text.setText(getColorString(text, keyword));
            
            return convertView;
        }

        private CharSequence getColorString(String str, String keyword) {
        	if (str != null) {
        		SpannableString spannable = new SpannableString(str);
        		if (keyword != null && keyword.length() > 0) {
    	    		switch (JkanjiSettingActivity.getHLType(mContext)) {
    	    		default:
    	        	case JkanjiSettingActivity.HL_TYPE_CHAR: {
		        			for (int i = 0; i < str.length(); i++) {
		        				if (keyword.indexOf(str.charAt(i)) >= 0) {
		        					ForegroundColorSpan span = new ForegroundColorSpan(Color.RED);
		        					spannable.setSpan(span, i, i+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        				}
		        			}
	    	        	}
    	        		break;
    	        		
    	    		case JkanjiSettingActivity.HL_TYPE_STRING:
    	    			int start = 0;
    	    			while (start < str.length()) {
    	    				int index = str.indexOf(keyword, start);
    	    				if (index == -1) {
    	    					break;
    	    				} else {
    	    					ForegroundColorSpan span = new ForegroundColorSpan(Color.RED);
    	    					spannable.setSpan(span, index, index + keyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    	    					start = index + keyword.length();
    	    				}
    	    			}
    	    			break;
    	    			
    	    		case JkanjiSettingActivity.HL_TYPE_NONE:
    	    			break;
    	    		}
        		}
        		return spannable;
        	} else {
        		return null;
        	}
        }
        
        private class ViewHolder {
        	TextView title;
            TextView text;
        }
    }
}
