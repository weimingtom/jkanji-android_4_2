package com.iteye.weimingtom.appmesh.android;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.widget.BaseAdapter;

import com.iteye.weimingtom.appmesh.dictionary.Dictionary;
import com.iteye.weimingtom.appmesh.dictionary.Node;
import com.iteye.weimingtom.appmesh.dictionary.OnAddNodeListener;
import com.iteye.weimingtom.appmesh.file.DictionaryInput;
import com.iteye.weimingtom.snowbook.activity.BaseActivity;

public class DictionarySearchTask extends AsyncTask<Void, Node, Void> implements OnAddNodeListener {
	private static final boolean D = false;
	private static final String TAG = "DictionarySearchTask";
	
	private WeakReference<Activity> context;
	private String filename;
	private String prefix;
	private WeakReference<List<Node>> words;
	private WeakReference<BaseAdapter> adapter;
	
	private Dictionary dictionary = new Dictionary();
	private volatile boolean isStop = false;
	
	private static final int NODES_MAX = 50;
	private List<Node> nodes = new ArrayList<Node>();
	
	public DictionarySearchTask(Activity context, BaseAdapter adapter, String filename, String prefix, List<Node> words) {
		this.context = new WeakReference<Activity>(context);
		this.adapter = new WeakReference<BaseAdapter>(adapter);
		this.filename = filename;
		this.prefix = prefix;
		this.words = new WeakReference<List<Node>>(words);
	}
	
	public void setStop(boolean value) {
		this.isStop = value;
		dictionary.setStop(value);
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.words.get().clear();
		this.adapter.get().notifyDataSetChanged();
		this.nodes.clear();
		
//	    ((BaseActivity)this.context.get()).setSupportProgressBarIndeterminate(true);
//        ((BaseActivity)this.context.get()).setSupportProgressBarIndeterminateVisibility(true);
    }
	
	protected Void doInBackground(Void... arg0) {
		AssetManager am = this.context.get().getAssets();
		if (am != null) {
			try {
				DictionaryInput input = new AndroidDictionaryInputStream(this.filename, am);
				dictionary.setOnAddNodeListener(this);
				dictionary.findPrefix(this.prefix, input);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		if (!isStop) {
			for (Node node : nodes) {
				if (node != null) {
					this.words.get().add(node);
				}
			}
			nodes.clear();
			this.adapter.get().notifyDataSetChanged();
		}
//		((BaseActivity)this.context.get()).setSupportProgressBarIndeterminateVisibility(false);
//		Toast.makeText(this.context.get(), 
//			"search finish", 
//			Toast.LENGTH_SHORT)
//			.show();
	}

	@Override
	public void onAddNode(Node node) {
		if (!isStop) {
			nodes.add(node);
			if (nodes.size() == NODES_MAX) {
				this.publishProgress(nodes.toArray(new Node[nodes.size()]));
				nodes.clear();
			}
		}
	}

	@Override
	protected void onProgressUpdate(Node... values) {
		super.onProgressUpdate(values);
		if (!isStop) {
			for (Node node : values) {
				if (node != null) {
					this.words.get().add(node);
				}
			}
			this.adapter.get().notifyDataSetChanged();
		}
	}
}
