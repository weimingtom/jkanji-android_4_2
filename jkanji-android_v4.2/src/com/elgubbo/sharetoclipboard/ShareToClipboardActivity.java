/*
 * Copyright 2012 Alexander Reichert

This file is part of ShareToClipboard.
ShareToClipboard is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
ShareToClipboard is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with Foobar. If not, see http://www.gnu.org/licenses/.
 */
package com.elgubbo.sharetoclipboard;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.elgubbo.sharetoclipboard.db.ShareDataSource;
import com.elgubbo.sharetoclipboard.handlers.IntentHandler;
import com.iteye.weimingtom.jkanji.JkanjiAozoraReaderActivity;
import com.iteye.weimingtom.jkanji.JkanjiMainMenuActivity;
import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.jkanji.Word;
import com.iteye.weimingtom.jkanji.WordEditActivity;
import com.markupartist.android.widget.ActionBar;


/**
 * The Class ShareToClipboardActivity is the Main Activity
 */
public class ShareToClipboardActivity extends Activity {
	public final static String ACTION_SEND_CLIP = "ShareToClipboardActivity.ACTION_SEND_CLIP";
	
	private static final int DIALOG_WARNING_ID = 1;
	
	private final static int CONTEXT_MENU_SHARE = ContextMenu.FIRST + 1;
	private final static int CONTEXT_MENU_DELETE = ContextMenu.FIRST + 2;
	private final static int CONTEXT_MENU_COPY = ContextMenu.FIRST + 3;
	
	/** The datasource. */
//	private ShareDataSource datasource;

	private ListView viewListView;
	private ActionBar actionBar;
	private ShareContentAdapter adapter;
	private ArrayList<ShareContent> values;
	
	
	private boolean firstStarted = true;
	private final static String KEY_FIRST_STARTED = "KEY_FIRST_STARTED";
	
	private AlertDialog.Builder builder1;
	private int currentPos = -1;
	
	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            the saved instance state
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.share_to_clipboard_listlayout);
		
		if (savedInstanceState != null) {
			firstStarted = savedInstanceState.getBoolean(KEY_FIRST_STARTED, true);
		}
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("备忘(可长按)");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.memo;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.shareto;
			}

			@Override
			public void performAction(View view) {
				shareAll();
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.refresh;
			}

			@Override
			public void performAction(View view) {
				updateList();
				Toast.makeText(ShareToClipboardActivity.this, 
					"更新列表完成", 
					Toast.LENGTH_SHORT).show();
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.adding;
			}

			@Override
			public void performAction(View view) {
				startActivity(new Intent(ShareToClipboardActivity.this, 
						ShareToClipboardEditActivity.class));
			}
        });
        
        viewListView = (ListView) this.findViewById(R.id.viewListView);
		builder1 = new AlertDialog.Builder(this);
        
		//FIXME:
		//getListView().setBackgroundResource(R.id.backgroundifempty);
		// create the datasource responsible for maintaining ShareContent
		// objects
//		datasource = new ShareDataSource(this);
//		datasource.open();
		
		Intent intent = getIntent();
		if (intent != null) {
			// This part consumes the Share intent
			// Should be replaced by something like a "intentHandler"
			final IntentHandler ih = new IntentHandler(intent,
					this, (ClipboardManager) getSystemService(CLIPBOARD_SERVICE));
			if (ih != null) {
				ih.handleIntent();
			}
		}
		
		viewListView.setFastScrollEnabled(true);
		viewListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ShareContent item = adapter.getItem(position);
				if (item != null) {
					startActivity(new Intent(ShareToClipboardActivity.this, ShareToClipboardEditActivity.class)
						.putExtra(ShareToClipboardEditActivity.EXTRA_ID, item.getId())
						.putExtra(ShareToClipboardEditActivity.EXTRA_DESCRIPTION, item.getDescription())
						.putExtra(ShareToClipboardEditActivity.EXTRA_CONTENT, item.getContent())
					);
				}
			}
		});
		// A list of all Contents is created
		values = new ArrayList<ShareContent>();
		// Show database elements in a ListView
		adapter = new ShareContentAdapter(this,
				R.layout.share_to_clipboard_list_content, values);
		viewListView.setAdapter(adapter);
		this.registerForContextMenu(viewListView);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateList();
		if (firstStarted) {
			viewListView.setSelection(values.size() - 1);
			firstStarted = false;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (outState != null) {
			outState.putBoolean(KEY_FIRST_STARTED, firstStarted);
		}
	}

	
	
//	@Override
//	protected void onPause() {
//		super.onPause();
//		if (isFinishing() && datasource != null) {
//			datasource.close();
//			datasource = null;
//		}
//	}
//
//	@Override
//	protected void onDestroy() {
//		super.onDestroy();
//		if (datasource != null) {
//			datasource.close();
//			datasource = null;
//		}
//	}
	
	private void updateList() {
		ShareDataSource datasource = new ShareDataSource(this);
		datasource.open();
		ArrayList<ShareContent> newValues = datasource.getAllContents();
		datasource.close();
		if (newValues != null) {
			values.clear();
			values.addAll(newValues);
		}
		adapter.notifyDataSetChanged();
	}
	
	private void shareAll() {
		String subjectString = 
				"[" + JkanjiAozoraReaderActivity.getTimeString() + "]" + 
				"备忘录";
		StringBuffer shareString = new StringBuffer();
		ShareDataSource datasource = new ShareDataSource(this);
		datasource.open();
		ArrayList<ShareContent> newValues = datasource.getAllContents();
		datasource.close();
		if (newValues != null) {
			for (ShareContent content : newValues) {
				String desc = content.getDescription();
				if (desc != null) {
					shareString.append(desc);
					shareString.append("\n");					
				}
				String cont = content.getContent();
				if (cont != null) {
					shareString.append(cont);
					shareString.append("\n");
				}
				if (content.getTime() != null) {
					String time = content.getTime().format("%Y-%m-%d %H:%M:%S");
					shareString.append(time);
					shareString.append("\n");					
				}
				shareString.append("\n");
			}
		}
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subjectString);
        intent.putExtra(Intent.EXTRA_TEXT, shareString.toString());
		try {
			startActivity(intent);
		} catch (Throwable e) {
			e.printStackTrace();
			Toast.makeText(this, 
				"共享方式出错", Toast.LENGTH_SHORT)
				.show();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
    	menu.add(0, CONTEXT_MENU_SHARE, 0, "共享");
    	menu.add(0, CONTEXT_MENU_DELETE, 0, "删除");
    	menu.add(0, CONTEXT_MENU_COPY, 0, "复制到剪贴板");
	}
	
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    	if (info != null) {
    	switch (item.getItemId()) {
	    	case CONTEXT_MENU_SHARE:
	    		shareItem(info.position);
	    		return true;
	    	
	    	case CONTEXT_MENU_DELETE:
//	    		deleteItem(info.position);
	    		currentPos = info.position;
	    		showDialog(DIALOG_WARNING_ID);
	    		return true;
	
	    	case CONTEXT_MENU_COPY:
	    		copyItem(info.position);
	    		return true;
	    	}
    	}
    	return super.onContextItemSelected(item);
    }
    
    private void shareItem(int pos) {
		ShareContent sc = adapter.getItem(pos);
		Intent i = new Intent(Intent.ACTION_SEND);
		i.putExtra(Intent.EXTRA_SUBJECT, sc.getDescription());
		i.putExtra(Intent.EXTRA_TEXT, sc.getContent());
		i.setType("text/*");
		try {
			startActivity(i);
		} catch (Throwable e) {
			e.printStackTrace();
			Toast.makeText(this, "找不到适当的程序", Toast.LENGTH_SHORT).show();
		}
    }
    
    private void deleteItem(int pos) {
    	if (adapter != null && pos >= 0 && pos < adapter.getCount()) {
	    	ShareDataSource datasource = new ShareDataSource(this);
			datasource.open();
			ShareContent item = adapter.getItem(pos);
			datasource.deleteContent(item);
			datasource.close();
			adapter.remove(item);
			adapter.notifyDataSetChanged();
    	}
	}
    
    private void copyItem(int pos) {
		if (adapter != null && pos >= 0 && pos < adapter.getCount()) {
	    	ClipboardManager cm = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
			cm.setText(adapter.getItem(pos).getContent());
			Toast.makeText(this, "复制至剪贴板", Toast.LENGTH_SHORT).show();
		}
	}
    
    @Override
 	protected Dialog onCreateDialog(int id) {
 	    Dialog dialog = null;
 	    switch(id) {
 	    case DIALOG_WARNING_ID:
     		return builder1
     			.setTitle("警告")
     			.setMessage("是否删除这条备忘录？")
     			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
     				public void onClick(DialogInterface dialog, int id) {
     					deleteItem(currentPos);
     					currentPos = -1;
     				}
     			})
     			.setNegativeButton("取消", new DialogInterface.OnClickListener() {
     				public void onClick(DialogInterface dialog, int id) {
     					currentPos = -1;
     				}
     			})
     			.setOnCancelListener(new OnCancelListener() {
 					@Override
 					public void onCancel(DialogInterface dialog) {
 						currentPos = -1;
 					}
     			})
     	       .create();
 	    }
 	    return dialog;
    }
}
