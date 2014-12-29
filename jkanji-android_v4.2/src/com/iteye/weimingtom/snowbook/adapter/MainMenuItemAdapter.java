package com.iteye.weimingtom.snowbook.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.snowbook.util.BitmapDrawableLruCache;
import com.iteye.weimingtom.snowbook.widget.RecyclingImageView;

public class MainMenuItemAdapter extends BaseAdapter{
	public final static class BookModel {
		public String title;
		public String detail;
		public int icon;

		public BookModel(String title, String detail, int icon) {
			this.title = title;
			this.detail = detail;
			this.icon = icon;
		}
	}
	
	private Context mContext;
	private LayoutInflater mInflater;
	private BookModel[] mModels;
	private boolean mIsGrid;
	private BitmapDrawableLruCache mMemoryCache;
	
	public MainMenuItemAdapter(Context context, BookModel[] models, 
		boolean isGrid, BitmapDrawableLruCache memoryCache){
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
		this.mModels = models;
		this.mIsGrid = isGrid;
		this.mMemoryCache = memoryCache;
	}

	@Override
	public int getCount() {
		if (mModels == null) {
			return 0;
		}
		return mModels.length;
	}

	@Override
	public Object getItem(int position) {
		if (mModels == null) {
			return null;
		}
		if (position < 0 || position >= mModels.length){
			return null;
		}
		return mModels[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (mIsGrid) {
			if (convertView == null){
				convertView = mInflater.inflate(R.layout.snowbook_main_menu_grid, null);
			}
			RelativeLayout menu_item = (RelativeLayout) convertView.findViewById(R.id.menu_item);
			LinearLayout banner = (LinearLayout) convertView.findViewById(R.id.banner); 
			RecyclingImageView imageViewBanner = (RecyclingImageView) convertView.findViewById(R.id.imageViewBanner);
			RecyclingImageView iv = (RecyclingImageView)convertView.findViewById(R.id.sItemIcon);
			TextView sItemTitle = (TextView)convertView.findViewById(R.id.sItemTitle);
			if (mModels != null && position >= 0 && position < mModels.length) {
				BookModel model = mModels[position];
				convertView.setTag(position);
				if (model == null) {
					menu_item.setVisibility(RelativeLayout.GONE);
					imageViewBanner.setImageDrawable(null);
					banner.setVisibility(LinearLayout.VISIBLE);					
				} else if (model.title == null) {
					menu_item.setVisibility(RelativeLayout.GONE);
					//imageViewBanner.setImageResource(model.icon);
					BitmapDrawable drawable = this.mMemoryCache.getDrawable(
							mContext.getResources(),
							model.icon);
					imageViewBanner.setImageDrawable(drawable);
					banner.setVisibility(LinearLayout.VISIBLE);
				} else {
					menu_item.setVisibility(RelativeLayout.VISIBLE);
					banner.setVisibility(LinearLayout.GONE);			
					sItemTitle.setText(model.title);
					//iv.setBackgroundResource(model.icon);
					BitmapDrawable drawable = this.mMemoryCache.getDrawable(
							mContext.getResources(),
							model.icon);
					iv.setImageDrawable(drawable);
				}
			} else {
				menu_item.setVisibility(RelativeLayout.GONE);
				imageViewBanner.setImageBitmap(null);
				banner.setVisibility(LinearLayout.VISIBLE);
			}
		} else {
			if (convertView == null){
				convertView = mInflater.inflate(R.layout.snowbook_main_menu_item, null);
			}
			BookModel model = mModels[position];
			convertView.setTag(position);
			RelativeLayout menu_item = (RelativeLayout) convertView.findViewById(R.id.menu_item);
			LinearLayout banner = (LinearLayout) convertView.findViewById(R.id.banner); 
			RecyclingImageView imageViewBanner = (RecyclingImageView) convertView.findViewById(R.id.imageViewBanner);
			RecyclingImageView iv = (RecyclingImageView)convertView.findViewById(R.id.sItemIcon);
			TextView sItemTitle = (TextView)convertView.findViewById(R.id.sItemTitle);
			TextView sItemInfo = (TextView)convertView.findViewById(R.id.sItemInfo);
			if (model.title == null) {
				menu_item.setVisibility(RelativeLayout.GONE);
				//imageViewBanner.setImageResource(model.icon);
				BitmapDrawable drawable = this.mMemoryCache.getDrawable(
						mContext.getResources(),
						model.icon);
				imageViewBanner.setImageDrawable(drawable);
				banner.setVisibility(LinearLayout.VISIBLE);
			} else {
				menu_item.setVisibility(RelativeLayout.VISIBLE);
				banner.setVisibility(LinearLayout.GONE);			
				sItemTitle.setText(model.title);
				sItemInfo.setText(model.detail);
				//iv.setBackgroundResource(model.icon);
				BitmapDrawable drawable = this.mMemoryCache.getDrawable(
						mContext.getResources(),
						model.icon);
				iv.setImageDrawable(drawable);
			}
		}
		return convertView;
	}
}
