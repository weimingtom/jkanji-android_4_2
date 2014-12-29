package com.iteye.weimingtom.snowbook.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.snowbook.pojo.BookInfo;

public class BookGridAdapter extends BaseAdapter {
	private Context context;
	private GridViewHolder gridholder;
	private List<BookInfo> dataList;

	public BookGridAdapter(Context context, List<BookInfo> results) {
		this.context = context;
		this.dataList = results;
	}

	@Override
	public int getCount() {
		if (dataList != null) {
			return dataList.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = View.inflate(this.context, R.layout.creader_bookgridviewitem, null);
			gridholder = new GridViewHolder();
			gridholder.tfBookName = (TextView) convertView.findViewById(R.id.bookgrid_name);
			gridholder.ivCoverImage = (ImageView) convertView.findViewById(R.id.bookgrid_pic);
			gridholder.ivCoverImageBack = (ImageView) convertView.findViewById(R.id.bookgrid_pic_backgroud);
			convertView.setTag(gridholder);
		} else {
			gridholder = (GridViewHolder) convertView.getTag();
		}
		int bookType = dataList.get(position).getBookType();
		String bookName = dataList.get(position).getBookName();
		switch (bookType) {
		case BookInfo.BOOK_TYPE_SPACE:
			gridholder.tfBookName.setText("");
			gridholder.ivCoverImageBack.setVisibility(View.INVISIBLE);
			gridholder.ivCoverImage.setVisibility(View.INVISIBLE);
			break;
			
		case BookInfo.BOOK_TYPE_DEFAULT:
		default:
			gridholder.tfBookName.setText(bookName);
			gridholder.ivCoverImageBack.setVisibility(View.VISIBLE);
			gridholder.ivCoverImage.setVisibility(View.VISIBLE);
			Bitmap bm = null;
			try {
				bm = BitmapFactory.decodeFile(dataList.get(position).getCoverImage());
			} catch (Throwable e) {
				e.printStackTrace();
				bm = null;
			}
			if (bm != null) {
				//gridholder.ivCoverImageBack.setImageResource(R.drawable.jkanji_book_bg);
				try{
					gridholder.ivCoverImage.setImageBitmap(bm);
				} catch(Throwable e){
					e.printStackTrace();
					//gridholder.ivCoverImage.setImageResource(R.drawable.jkanji_book_cover);
				}
			} else {
				//gridholder.ivCoverImageBack.setImageResource(R.drawable.jkanji_book_bg);
				//gridholder.ivCoverImage.setImageResource(R.drawable.jkanji_book_cover);
			}
			break;
		}
		return convertView;
	}

	private final static class GridViewHolder {
		private TextView tfBookName;
		private ImageView ivCoverImage;
		private ImageView ivCoverImageBack;
	}
}
