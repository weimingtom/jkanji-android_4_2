package com.iteye.weimingtom.snowbook.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.snowbook.pojo.ListItemModel;
import com.iteye.weimingtom.snowbook.util.BitmapDrawableLruCache;
import com.iteye.weimingtom.snowbook.widget.RecyclingImageView;

public class ListItemAdapter extends BaseAdapter {
	private Context mContext;
	private LayoutInflater inflater;
	private List<ListItemModel> models;
	private BitmapDrawableLruCache mMemoryCache;

	public ListItemAdapter(Context context, List<ListItemModel> models,
			BitmapDrawableLruCache memoryCache){
		this.mContext = context;
		this.inflater = LayoutInflater.from(context);
		this.models = models;
		this.mMemoryCache = memoryCache;
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

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null){
			convertView = inflater.inflate(R.layout.snowbook_item_list, null);
			holder = new ViewHolder();
			holder.textViewTitle = (TextView)convertView.findViewById(R.id.textViewTitle);
			holder.textViewDetail = (TextView)convertView.findViewById(R.id.textViewDetail);
			holder.imageViewIcon = (RecyclingImageView)convertView.findViewById(R.id.imageViewIcon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		ListItemModel model = models.get(position);
		if (model != null) {
			holder.textViewTitle.setText(model.getTitle());
			holder.textViewDetail.setText(model.getDetail());
			BitmapDrawable drawable = this.mMemoryCache.getDrawable(
					mContext.getResources(),
					mContext.getAssets(),
					model.getFilename());
			holder.imageViewIcon.setImageDrawable(drawable);
		} else {
			holder.imageViewIcon.setImageDrawable(null);
		}
		return convertView;
	}
	
    private static final class ViewHolder {
    	TextView textViewTitle;
    	TextView textViewDetail;
    	RecyclingImageView imageViewIcon;
    }
}
