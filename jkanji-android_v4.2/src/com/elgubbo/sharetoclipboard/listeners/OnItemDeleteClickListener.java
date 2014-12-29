package com.elgubbo.sharetoclipboard.listeners;

import com.elgubbo.sharetoclipboard.ShareContentAdapter;
import com.elgubbo.sharetoclipboard.db.ShareDataSource;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

/*
 * This handles the clicks on each "delete" button in the list
 */
public class OnItemDeleteClickListener implements OnClickListener{
	private final static boolean D = false;
	private final static String TAG = "OnItemDeleteClickListener";
	
	private int mPosition;
//	private ShareDataSource datasource;
	private ShareContentAdapter shareContAdap;
	private Context context;
    
	public OnItemDeleteClickListener(int position, ShareContentAdapter shareContentAdapter, Context cont){
            this.mPosition = position;
//            this.datasource = ds;
            this.shareContAdap = shareContentAdapter;
            this.context = cont;
    }
    
	@Override
	public void onClick(View v) {
		if (D) {
			Log.d(TAG, "OnItemDeleteClickListener");
		}
		ShareDataSource datasource = new ShareDataSource(this.context);
		datasource.open();
		datasource.deleteContent((shareContAdap.getItem(mPosition)));
		datasource.close();
		shareContAdap.remove(shareContAdap.getItem(mPosition));
		shareContAdap.notifyDataSetChanged();
	}
}
