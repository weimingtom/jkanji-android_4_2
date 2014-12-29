package com.iteye.weimingtom.jkanji;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.iteye.weimingtom.jkanji.R;

public class JkanjiMainMenuItemAdapter extends BaseAdapter{
	public final static class BookModel {
		public String title;
		public String detail;
		public int icon;
		public Bitmap bitmap;
		
		public BookModel(String title, String detail, int icon) {
			this.title = title;
			this.detail = detail;
			this.icon = icon;
		}
	}
	
	private LayoutInflater mInflater;
	private BookModel[] mModels;
	private boolean mIsGrid;

	public JkanjiMainMenuItemAdapter(Context context, BookModel[] models, boolean isGrid){
		this.mInflater = LayoutInflater.from(context);
		this.mModels = models;
		this.mIsGrid = isGrid;
		if (this.mModels != null) {
			for (int i = 0; i < this.mModels.length; i++) {
				if (mModels[i] != null) {
					mModels[i].bitmap = BitmapFactory.decodeResource(context.getResources(), mModels[i].icon);
				}
			}
		}
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
				convertView = mInflater.inflate(R.layout.main_menu_grid, null);
			}
			RelativeLayout menu_item = (RelativeLayout) convertView.findViewById(R.id.menu_item);
			LinearLayout banner = (LinearLayout) convertView.findViewById(R.id.banner); 
			ImageView imageViewBanner = (ImageView) convertView.findViewById(R.id.imageViewBanner);
			ImageView iv = (ImageView)convertView.findViewById(R.id.sItemIcon);
			TextView sItemTitle = (TextView)convertView.findViewById(R.id.sItemTitle);
			if (mModels != null && position >= 0 && position < mModels.length) {
				BookModel model = mModels[position];
				convertView.setTag(position);
				if (model == null) {
					menu_item.setVisibility(RelativeLayout.GONE);
					imageViewBanner.setImageBitmap(null);
					banner.setVisibility(LinearLayout.VISIBLE);					
				} else if (model.title == null) {
					menu_item.setVisibility(RelativeLayout.GONE);
					//imageViewBanner.setImageResource(model.icon);
					if (model.bitmap != null && !model.bitmap.isRecycled()) {
						imageViewBanner.setImageBitmap(model.bitmap);
						imageViewBanner.setImageBitmap(null);
					} else {
						imageViewBanner.setImageBitmap(null);
					}
					banner.setVisibility(LinearLayout.VISIBLE);
				} else {
					menu_item.setVisibility(RelativeLayout.VISIBLE);
					banner.setVisibility(LinearLayout.GONE);			
					sItemTitle.setText(model.title);
					//iv.setBackgroundResource(model.icon);
					if (model.bitmap != null && !model.bitmap.isRecycled()) {
						iv.setImageBitmap(model.bitmap);
					} else {
						iv.setImageBitmap(null);
					}
				}
			} else {
				menu_item.setVisibility(RelativeLayout.GONE);
				imageViewBanner.setImageBitmap(null);
				banner.setVisibility(LinearLayout.VISIBLE);
			}
		} else {
			if (convertView == null){
				convertView = mInflater.inflate(R.layout.main_menu_item, null);
			}
			BookModel model = mModels[position];
			convertView.setTag(position);
			RelativeLayout menu_item = (RelativeLayout) convertView.findViewById(R.id.menu_item);
			LinearLayout banner = (LinearLayout) convertView.findViewById(R.id.banner); 
			ImageView imageViewBanner = (ImageView) convertView.findViewById(R.id.imageViewBanner);
			ImageView iv = (ImageView)convertView.findViewById(R.id.sItemIcon);
			TextView sItemTitle = (TextView)convertView.findViewById(R.id.sItemTitle);
			TextView sItemInfo = (TextView)convertView.findViewById(R.id.sItemInfo);
			if (model.title == null) {
				menu_item.setVisibility(RelativeLayout.GONE);
				//imageViewBanner.setImageResource(model.icon);
				if (model.bitmap != null && !model.bitmap.isRecycled()) {
					imageViewBanner.setImageBitmap(model.bitmap);
				} else {
					imageViewBanner.setImageBitmap(null);
				}
				banner.setVisibility(LinearLayout.VISIBLE);
			} else {
				menu_item.setVisibility(RelativeLayout.VISIBLE);
				banner.setVisibility(LinearLayout.GONE);			
				sItemTitle.setText(model.title);
				sItemInfo.setText(model.detail);
				//iv.setBackgroundResource(model.icon);
				if (model.bitmap != null && !model.bitmap.isRecycled()) {
					iv.setImageBitmap(model.bitmap);
				} else {
					iv.setImageBitmap(null);
				}
			}
		}
		return convertView;
	}
	
	public void destory() {
		if (this.mModels != null) {
			for (int i = 0; i < this.mModels.length; i++) {
				if (mModels[i] != null && mModels[i].bitmap != null) {
					mModels[i].bitmap.recycle();
					mModels[i].bitmap = null;
				}
			}
		}
	}
}
