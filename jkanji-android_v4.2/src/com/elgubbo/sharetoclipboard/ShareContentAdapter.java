package com.elgubbo.sharetoclipboard;

import java.util.ArrayList;

import com.elgubbo.sharetoclipboard.db.ShareDataSource;
import com.elgubbo.sharetoclipboard.listeners.OnItemClickCopyListener;
import com.elgubbo.sharetoclipboard.listeners.OnItemDeleteClickListener;
import com.elgubbo.sharetoclipboard.listeners.OnItemShareClickListener;

import com.iteye.weimingtom.jkanji.R;

import android.content.Context;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ShareContentAdapter extends ArrayAdapter<ShareContent> {
	private ArrayList<ShareContent> contents;
	private Context cont;
//	private ShareDataSource datasource;
	View.OnTouchListener gestureListener;

	public ShareContentAdapter(Context context, int textViewResourceId,
			ArrayList<ShareContent> contents) {
		super(context, textViewResourceId, contents);
		this.contents = contents;
		this.cont = context;
//		this.datasource = ds;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder holder;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) cont.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.share_to_clipboard_list_content, null);
			holder = new ViewHolder();
			holder.contentText = (TextView) v.findViewById(R.id.actualcontent);
			holder.descriptionText = (TextView) v.findViewById(R.id.description);
			holder.textViewTime = (TextView) v.findViewById(R.id.textViewTime);
			holder.copybtn = (ImageView) v.findViewById(R.id.copybtn);
			holder.delbtn = (ImageView) v.findViewById(R.id.deletebtn);
			holder.sharebtn = (ImageView) v.findViewById(R.id.sharebtn);
			v.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) v.getTag();
		}

		ShareContent content = null;
		if (contents != null && position >= 0 && position < contents.size()) {
			content = contents.get(position);
		}
		if (content != null) {
			// Set the OnClickListeners for the Buttons
			holder.delbtn.setOnClickListener(new OnItemDeleteClickListener(
					position, this, cont));
			holder.sharebtn.setOnClickListener(new OnItemShareClickListener(
					position, this, cont));
			holder.copybtn.setOnClickListener(new OnItemClickCopyListener(this,
					position, cont));
			holder.descriptionText.setText(content.getDescription());
			holder.contentText.setText(content.getContent());
			Time time = content.getTime();
			if (time != null) {
				holder.textViewTime.setText(time.format("%Y-%m-%d %H:%M:%S"));
			} else {
				holder.textViewTime.setText("---");
			}
		} else {
			holder.delbtn.setOnClickListener(null);
			holder.sharebtn.setOnClickListener(null);
			holder.copybtn.setOnClickListener(null);
			holder.descriptionText.setText(null);
			holder.contentText.setText(null);
			holder.textViewTime.setText(null);			
		}
		return v;
	}

	private final static class ViewHolder {
		TextView descriptionText;
		TextView contentText;
		TextView textViewTime;
		ImageView delbtn;
		ImageView sharebtn;
		ImageView copybtn;
	}
}