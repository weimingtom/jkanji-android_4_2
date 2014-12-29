package com.elgubbo.sharetoclipboard.listeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.elgubbo.sharetoclipboard.ShareContent;
import com.elgubbo.sharetoclipboard.ShareContentAdapter;

/*
 * This handles the clicks on each "share" button in the list
 */
public class OnItemShareClickListener implements OnClickListener{

	private int mPosition;
	private ShareContentAdapter shareContAdap;
	private Context cont;

	public OnItemShareClickListener(int position, ShareContentAdapter sca, Context cont){
		this.mPosition = position;
		this.shareContAdap = sca;
		this.cont = cont;
	}

	@Override
	public void onClick(View v) {
		ShareContent sc = shareContAdap.getItem(mPosition);
		Intent i = new Intent(Intent.ACTION_SEND);
		i.putExtra(Intent.EXTRA_SUBJECT, sc.getDescription());
		i.putExtra(Intent.EXTRA_TEXT, sc.getContent());
		i.setType("text/*");
		try {
			cont.startActivity(i);
		} catch (Throwable e) {
			e.printStackTrace();
			Toast.makeText(cont, "找不到适当的程序", Toast.LENGTH_SHORT).show();
		}
	}
}
