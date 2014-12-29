package com.iteye.weimingtom.jkanji;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

public class PackFileReadTask extends AsyncTask<Void, Integer, Void> implements CheckProgressHandler.ICheckProgress {
	private static boolean D = false;
	private String TAG = "PackFileReadTask";
	
	public static final int MAX_FIELD_SIZE = 5;
	
	public static class SessionSaveData {
		public String[][] dict_items;
		public byte[] bytes;
		public int[] dict_size;
		public int pos;
		public int currentProgress = 0;
		public int maxProgress = 100; //FIMXE: first time MUST > currentProgress
	}
	
	//private AssetManager am;
	private WeakReference<AssetManager> am;
	private String filename;
	
	private volatile boolean isRunningState = false;
	//private SessionSaveData data;
	//see http://blog.sina.com.cn/s/blog_4ad7c25401012twi.html
	//Thread pool may cause GC problem.
	private WeakReference<SessionSaveData> data;
	
	private volatile boolean canRetain;
	
	public PackFileReadTask(AssetManager am, String filename, SessionSaveData data) {
		this.am = new WeakReference<AssetManager>(am);
		this.filename = filename;
		this.data = new WeakReference<SessionSaveData>(data);
//		assert this.filename != null;
//		assert this.data != null;
	}
	
	public class ReadPackException extends Exception {
		private static final long serialVersionUID = 1L;
		
		public ReadPackException(String message) {
			super(message);
		}
	}
	
	public synchronized void setIsRunning(boolean isRunning) {
		this.isRunningState = isRunning;
	}
	
	public synchronized void setCanRetain(boolean canRetain) {
		this.canRetain = canRetain;
	}
	
	public synchronized boolean getCanRetain() {
		return this.canRetain;
	}
	
	//switch out (donnot set too much)
	public synchronized boolean getIsRunning() {
		return this.isRunningState;
	}
	
	public int getCurrentProgress() {
		return this.data.get().currentProgress;
	}
	
	public int getMaxProgress() {
		return this.data.get().maxProgress;
	}
	
	public void loadAllBytes() throws ReadPackException {
		SessionSaveData data = this.data.get();
		if (data.currentProgress == 0 && data.currentProgress < data.maxProgress) {
			InputStream inputstream = null;
			try {
				if (D) {
					Log.e(TAG, "open " + filename);
				}
				inputstream = am.get().open(filename, AssetManager.ACCESS_STREAMING);
				data.bytes = new byte[inputstream.available()];
				inputstream.read(data.bytes);
				data.pos = 0;
				data.dict_items = null;
				if (D) {
					Log.e(TAG, "data.dict_items = null");
				}
				data.dict_size = null;
				data.currentProgress = 0;
				data.maxProgress = 100;
			} catch (IOException e) {
				e.printStackTrace();
				throw new ReadPackException("loadAllBytes read file error");
			} finally {
				if (inputstream != null) {
					try {
						inputstream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					inputstream = null;
				}
			}
			readMagic();
			readItemCount();
		}
	}
	
	public void readMagic() throws ReadPackException {
		SessionSaveData data = this.data.get();
		if (data.pos + 4 > data.bytes.length) {
			throw new ReadPackException("readMagic overflow");
		}
		if (data.bytes[data.pos++] != 'P' ||
			data.bytes[data.pos++] != 'A' ||
			data.bytes[data.pos++] != 'C' ||
			data.bytes[data.pos++] != 'K') {
			throw new ReadPackException("readMagic not PACK magic");
		}
	}
	
	public int readInt32() throws ReadPackException {
		SessionSaveData data = this.data.get();
		if (data.pos + 4 > data.bytes.length) {
			throw new ReadPackException("readInt32 overflow");
		}
		int result = 0;
		if (D) {
			Log.e(TAG, (data.pos) + " : " + (data.bytes[data.pos] & 0xff));
			Log.e(TAG, (data.pos + 1) + " : " + (data.bytes[data.pos + 1] & 0xff));
			Log.e(TAG, (data.pos + 2) + " : " + (data.bytes[data.pos + 2] & 0xff));
			Log.e(TAG, (data.pos + 3) + " : " + (data.bytes[data.pos + 3] & 0xff));
		}
		result |= (data.bytes[data.pos++] & 0xff) << 24;
		result |= (data.bytes[data.pos++] & 0xff) << 16;
		result |= (data.bytes[data.pos++] & 0xff) << 8;
		result |= (data.bytes[data.pos++] & 0xff) << 0;
		return result;
	}
	
	public byte[] readBytes(int length) throws ReadPackException  {
		SessionSaveData data = this.data.get();
		if (data.pos + length > data.bytes.length) {
			throw new ReadPackException("readInt32 overflow");
		}
		byte[] bytes = new byte[length];
		if (D) {
			Log.e(TAG, "data.pos = " + data.pos + ", length = " + length);
		}
		System.arraycopy(data.bytes, data.pos, bytes, 0, length);
		data.pos += length;
		return bytes;
	}
	
	public void readItemCount() throws ReadPackException {
		SessionSaveData data = this.data.get();
		data.currentProgress = 0;
		data.dict_size = new int[readInt32()];
		int maxProgress = 0;
		for (int i = 0; i < data.dict_size.length; i++) {
			int n = readInt32();
			data.dict_size[i] = n;
			maxProgress += n;
		}
		data.maxProgress = maxProgress;
		if (D) {
			Log.e(TAG, "data.maxProgress = " + data.maxProgress);
		}
		data.dict_items = new String[data.maxProgress][];
	}
	
	public boolean readItem() throws ReadPackException {
		SessionSaveData data = this.data.get();
		if (data.currentProgress >= data.maxProgress) {
			return false;
		}
		int currentProgress = readInt32();
		assert data.currentProgress == currentProgress;
		data.dict_items[currentProgress] = new String[MAX_FIELD_SIZE];
		int length = readInt32();
		byte[] bytes = readBytes(length);
		int p = 0;
		for (int i = 0; i < MAX_FIELD_SIZE; i++) {
			int strlen = 0;
			strlen |= (bytes[p++] & 0xff) << 24;
			strlen |= (bytes[p++] & 0xff) << 16;
			strlen |= (bytes[p++] & 0xff) << 8;
			strlen |= (bytes[p++] & 0xff) << 0;
			String str = null;
			if (strlen > 0) {
				try {
					str = new String(bytes, p, strlen, "UTF8");
					p += strlen;
					if (D) {
						Log.e(TAG, str);
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					throw new ReadPackException("readItem read string error");
				}
			}
			if (D) {
				if (data == null) {
					Log.e(TAG, "data == null");				
				}
				if (data.dict_items == null) {
					Log.e(TAG, "data.dict_items == null currentProgress = " + currentProgress);
				}
				if (data.dict_items[currentProgress] == null) {
					Log.e(TAG, "data.dict_items[currentProgress] == null currentProgress = " + currentProgress);
				}
			}
			data.dict_items[currentProgress][i] = str;
		}
		data.currentProgress++;
		return true;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		SessionSaveData data = this.data.get();
		if (!getIsRunning()) { //don't reenter
			long lasttime = System.currentTimeMillis();
			setIsRunning(true);
			setCanRetain(false);
			try {
				//!
				if (!DataContext.DONNOT_LOAD_PAC) {
					loadAllBytes();
					while (getIsRunning()) {
						if (D) {
							Log.e(TAG, data.currentProgress + " / " + data.maxProgress);
						}
						if (!readItem()) {
							break;
						}
					}
				}
			} catch (ReadPackException e) {
				System.err.println("data.pos = " + data.pos + ", data.currentProgress = " + data.currentProgress + ", data.bytes.length = " + data.bytes.length);
				e.printStackTrace();
			}
			if (D) {
				Log.e(TAG, "doInBackground time: " + (System.currentTimeMillis() - lasttime));
			}
			setCanRetain(true);
			setIsRunning(false);
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void param) {
		if (D) {
			Log.e(TAG, "onPostExecute");
		}
    }
}
