package com.iteye.weimingtom.snowbook.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iteye.weimingtom.jkanji.R;
import com.nhaarman.listviewanimations.ArrayAdapter;

public class ListViewAnimationsMyListAdapter extends ArrayAdapter<Integer> {
	private final Context mContext;

	public ListViewAnimationsMyListAdapter(final Context context, final ArrayList<Integer> items) {
		super(items);
		mContext = context;
	}

	@Override
	public long getItemId(final int position) {
		return getItem(position).hashCode();
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		TextView tv = (TextView) convertView;
		if (tv == null) {
			tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.lva__list_row, parent, false);
		}
		tv.setText("This is row number " + getItem(position));
		return tv;
	}
}
