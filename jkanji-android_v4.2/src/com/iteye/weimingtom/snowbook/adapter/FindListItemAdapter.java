package com.iteye.weimingtom.snowbook.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iteye.weimingtom.appmesh.dictionary.Node;
import com.iteye.weimingtom.appmesh.dictionary.Record;
import com.iteye.weimingtom.jkanji.R;

public class FindListItemAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<Node> models;
	private Context mContext;

	public FindListItemAdapter(Context context, List<Node> models){
		this.inflater = LayoutInflater.from(context);
		this.models = models;
		this.mContext = context;
	}

	public int getCount() {
		if (models == null) {
			return 0;
		}
		return models.size();
	}

	public Object getItem(int position) {
		if (position >= getCount()){
			return null;
		}
		return models.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null){
			convertView = inflater.inflate(R.layout.snowbook_word_item_list, null);
			holder = new ViewHolder();
			holder.textViewRomaji = (TextView)convertView.findViewById(R.id.textViewRomaji);
			holder.textViewSound = (TextView)convertView.findViewById(R.id.textViewSound);
			holder.hiddenLayout = (LinearLayout)convertView.findViewById(R.id.hiddenLayout);
			holder.imagenArrow = (ImageButton)convertView.findViewById(R.id.imagenArrow);
			
            holder.textViewKanji = (TextView) convertView.findViewById(R.id.textViewKanji);
            holder.textViewAccent = (TextView) convertView.findViewById(R.id.textViewAccent);
            holder.textViewPos = (TextView) convertView.findViewById(R.id.textViewPos);
            holder.textViewMean = (TextView) convertView.findViewById(R.id.textViewMean);
            
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Node node = models.get(position);
		if (node != null) {
			Record record = node.getValue();
	        if (record != null) {
	        	holder.textViewRomaji.setText(replaceNum(record.getRomaji()));
	        	holder.textViewSound.setText(record.getSound());
	        } else {
	        	holder.textViewRomaji.setText("");
	        	holder.textViewSound.setText("");
	        }
	        if (record != null) {
	        	holder.textViewKanji.setText(replaceText(record.getKanji()));
	            holder.textViewAccent.setText(replaceText(record.getAccent()));
	            holder.textViewPos.setText(replaceText(record.getPos()));
	            holder.textViewMean.setText(record.getMean());
	        } else {
	        	holder.textViewKanji.setText("");
	            holder.textViewAccent.setText("");
	            holder.textViewPos.setText("");
	            holder.textViewMean.setText("");
	        }
			if (node.isExpanded()) {
				holder.hiddenLayout.setVisibility(View.VISIBLE);
			} else {
				holder.hiddenLayout.setVisibility(View.GONE);
			}
			convertView.forceLayout();
			holder.imagenArrow.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					
				}
			});
		}
		return convertView;
	}
	
    private String replaceNum(String str) {
    	if (str == null) {
    		return null;
    	}
    	return str.replaceAll("\\d", "");
    }
    
    private String replaceText(String str) {
    	if (str == null) {
    		return null;
    	}
    	return str.replace("|", "、");
    }
	
    private static final class ViewHolder {
    	ImageButton imagenArrow;
    	TextView textViewRomaji;
    	TextView textViewSound;
    	LinearLayout hiddenLayout;
    	
    	TextView textViewKanji;
    	TextView textViewAccent;
    	TextView textViewPos;
    	TextView textViewMean;
    }
}
