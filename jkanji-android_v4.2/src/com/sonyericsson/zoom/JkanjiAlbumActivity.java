package com.sonyericsson.zoom;

import java.io.File;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @see com.sen.imageviewer
 * @author Administrator
 *
 */
public class JkanjiAlbumActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "JkanjiAlbumActivity";
	
	private final static int CONTEXT_MENU_ADD_GALLERY = ContextMenu.FIRST + 1;
	private final static int CONTEXT_MENU_GRID = ContextMenu.FIRST + 2;
	private final static int CONTEXT_MENU_PLAIN2 = ContextMenu.FIRST + 3;
	private final static int CONTEXT_MENU_PLAIN_SCROLL2 = ContextMenu.FIRST + 4;
	private final static int CONTEXT_MENU_PLAIN_PAGER2 = ContextMenu.FIRST + 5;
	
	private ActionBar actionBar;
	private TextView textViewLoading;
	private GridView mAlbumGrid;
	private AlbumAdapter mAdapter;
	private Albums mAlbums;
	private Bitmap[] mThumbnails;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.album_main);
		actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setTitle("相册");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.album;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
        /*
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.refresh;
			}

			@Override
			public void performAction(View view) {
				try {
					sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
						Uri.fromFile(Environment.getExternalStorageDirectory())
					));
					Toast.makeText(JkanjiAlbumActivity.this, 
						"开始请求更新系统媒体库，请先退出此界面等待一段时间", 
						Toast.LENGTH_SHORT).show();
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(JkanjiAlbumActivity.this, 
							"请求更新系统媒体库失败", 
							Toast.LENGTH_SHORT).show();
				}
			}
        });
        */
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.config2;
			}

			@Override
			public void performAction(View view) {
				startActivity(new Intent(
					JkanjiAlbumActivity.this, JkanjiGallerySettingActivity.class)
				);
			}
        });
        
		mAlbumGrid = (GridView) findViewById(R.id.albumGrid);
		textViewLoading = (TextView) findViewById(R.id.textViewLoading);
		
		
		mAdapter = new AlbumAdapter(this);
		mAlbumGrid.setAdapter(mAdapter);
		mAlbumGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				openContextMenu(v);
			}
		});
		registerForContextMenu(mAlbumGrid);
		
		new LoadDataTask().execute();
	}
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
    	super.onCreateContextMenu(menu, v, info);
    	menu.add(0, CONTEXT_MENU_ADD_GALLERY, 0, "添加至图库历史");
    	menu.add(0, CONTEXT_MENU_GRID, 0, "目录表格预览");
    	menu.add(0, CONTEXT_MENU_PLAIN2, 0, "图库查看器");
    	menu.add(0, CONTEXT_MENU_PLAIN_SCROLL2, 0, "卷轴查看器");
    	menu.add(0, CONTEXT_MENU_PLAIN_PAGER2, 0, "平移查看器");
    }
	
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    	switch (item.getItemId()) {
    	case CONTEXT_MENU_ADD_GALLERY:
    		addGallery(info.position);
    		break;
    	
    	case CONTEXT_MENU_GRID:
    		openGrid(info.position);
    		return true;
    		
    	case CONTEXT_MENU_PLAIN2:
    		openPlainItem(info.position, false);
    		return true;

    	case CONTEXT_MENU_PLAIN_SCROLL2:
    		openPlainItemScroll(info.position, false);
    		return true;
    		
    	case CONTEXT_MENU_PLAIN_PAGER2:
    		openPlainItemPager(info.position, false);
    		return true;
    	}
    	return super.onContextItemSelected(item);
    }
	
    private void addGallery(int pos) {
		String fileName = this.mAlbums.getAlbumFirstFileName(pos);
		if (fileName == null) {
			return;
		}
		File file = new File(fileName);
		if (file.exists()) {
			JkanjiGalleryHistoryDataSource dataSrc = new JkanjiGalleryHistoryDataSource(this);
	    	dataSrc.open();
	    	JkanjiGalleryHistoryItem item = new JkanjiGalleryHistoryItem();
	    	item.setId(-1L);
	    	item.setPlainZoom(1.0f);
	    	item.setPlainPanX(0.5f);
	    	item.setPlainPanY(0.5f);
	    	item.setPlainPage(-1);
	    	item.setPlainTotalPage(0);
	    	item.setPlainFileName(file.getName());
	    	item.setPlainPathName(file.getParent());
	    	item.setPlainEnableMulti(true);
	    	dataSrc.createItem(item);
	    	dataSrc.close();
			Toast.makeText(this, 
				"添加至图库：" + fileName, 
				Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, 
				"目录或文件不存在：" + fileName, 
				Toast.LENGTH_SHORT).show();
		}    	
    }
    
	private void openGrid(int pos) {
		String fileName = this.mAlbums.getAlbumFirstFileName(pos);
		if (fileName == null) {
			return;
		}
		File file = new File(fileName);
		if (file.exists()) {
			startActivity(new Intent(
				this, ImageGrid2Activity.class)
				.putExtra(ImageGrid2Activity.EXTRA_FILENAME, file.getAbsolutePath())
				.putExtra(ImageGrid2Activity.EXTRA_NORECORD, true)
			);
		} else {
			Toast.makeText(this, 
				"目录或文件不存在：" + fileName, 
				Toast.LENGTH_SHORT).show();
		}
	}
    
	private void openPlainItem(int pos, boolean isRecord) {
		String fileName = this.mAlbums.getAlbumFirstFileName(pos);
		if (fileName == null) {
			return;
		}
		File file = new File(fileName);
		if (file.exists()) {
			Class<?> galleryClass = JkanjiGalleryActivity.class;
			int screenOri;
			if (isRecord) {
				screenOri = JkanjiGallerySettingActivity.getScreenOri(this);
			} else {
				screenOri = JkanjiGallerySettingActivity.getScreenOri2(this);
			}
			if (JkanjiGallerySettingActivity.getAutoCalcOri(this)) {
				screenOri = JkanjiGallerySettingActivity.calcOri(this, file, screenOri);
			}
			switch (screenOri) {
	    	case JkanjiGallerySettingActivity.SCREEN_ORI_PORT:
	    		galleryClass = JkanjiGalleryPortActivity.class;
	    		break;
	    		
	    	case JkanjiGallerySettingActivity.SCREEN_ORI_LAND:
	    		galleryClass = JkanjiGalleryLandActivity.class;
	    		break;
	    	}
			startActivity(new Intent(
				this, galleryClass)
				.putExtra(JkanjiGalleryActivity.EXTRA_KEY_PATH, file.getParent())
				.putExtra(JkanjiGalleryActivity.EXTRA_KEY_FILENAME, file.getName())
				.putExtra(JkanjiGalleryActivity.EXTRA_KEY_FILEID, -1)
				.putExtra(JkanjiGalleryActivity.EXTRA_KEY_ISRECORD, isRecord)
			);
		} else {
			Toast.makeText(this, 
				"目录不存在：" + fileName, 
				Toast.LENGTH_SHORT).show();
		}
	}
	
	private void openPlainItemScroll(int pos, boolean isRecord) {
		String fileName = this.mAlbums.getAlbumFirstFileName(pos);
		if (fileName == null) {
			return;
		}
		File file = new File(fileName);
		if (file.exists()) {
			Class<?> galleryClass = JkanjiScrollGalleryActivity.class;
			int screenOri;
			if (isRecord) {
				screenOri = JkanjiGallerySettingActivity.getScreenOri(this);
			} else {
				screenOri = JkanjiGallerySettingActivity.getScreenOri2(this);
			}
			if (JkanjiGallerySettingActivity.getAutoCalcOri(this)) {
				screenOri = JkanjiGallerySettingActivity.calcOri(this, file, screenOri);
			}
			switch (screenOri) {
	    	case JkanjiGallerySettingActivity.SCREEN_ORI_PORT:
	    		galleryClass = JkanjiScrollGalleryPortActivity.class;
	    		break;
	    		
	    	case JkanjiGallerySettingActivity.SCREEN_ORI_LAND:
	    		galleryClass = JkanjiScrollGalleryLandActivity.class;
	    		break;
	    	}
			startActivity(new Intent(
				this, galleryClass)
				.putExtra(JkanjiScrollGalleryActivity.EXTRA_KEY_PATH, file.getParent())
				.putExtra(JkanjiScrollGalleryActivity.EXTRA_KEY_FILENAME, file.getName())
				.putExtra(JkanjiScrollGalleryActivity.EXTRA_KEY_FILEID, -1)
				.putExtra(JkanjiScrollGalleryActivity.EXTRA_KEY_ISRECORD, isRecord)
			);
		} else {
			Toast.makeText(this, 
				"目录不存在：" + fileName, 
				Toast.LENGTH_SHORT).show();
		}
	}
	
	private void openPlainItemPager(int pos, boolean isRecord) {
		String fileName = this.mAlbums.getAlbumFirstFileName(pos);
		if (fileName == null) {
			return;
		}
		File file = new File(fileName);
		if (file.exists()) {
			Class<?> galleryClass = JkanjiPagerGalleryActivity.class;
			int screenOri;
			if (isRecord) {
				screenOri = JkanjiGallerySettingActivity.getScreenOri(this);
			} else {
				screenOri = JkanjiGallerySettingActivity.getScreenOri2(this);
			}
			if (JkanjiGallerySettingActivity.getAutoCalcOri(this)) {
				screenOri = JkanjiGallerySettingActivity.calcOri(this, file, screenOri);
			}
			switch (screenOri) {
	    	case JkanjiGallerySettingActivity.SCREEN_ORI_PORT:
	    		galleryClass = JkanjiPagerGalleryPortActivity.class;
	    		break;
	    		
	    	case JkanjiGallerySettingActivity.SCREEN_ORI_LAND:
	    		galleryClass = JkanjiPagerGalleryLandActivity.class;
	    		break;
	    	}
			startActivity(new Intent(
				this, galleryClass)
				.putExtra(JkanjiPagerGalleryActivity.EXTRA_KEY_PATH, file.getParent())
				.putExtra(JkanjiPagerGalleryActivity.EXTRA_KEY_FILENAME, file.getName())
				.putExtra(JkanjiPagerGalleryActivity.EXTRA_KEY_FILEID, -1)
				.putExtra(JkanjiPagerGalleryActivity.EXTRA_KEY_ISRECORD, isRecord)
			);
		} else {
			Toast.makeText(this, 
				"目录不存在：" + fileName, 
				Toast.LENGTH_SHORT).show();
		}
	}
	
	private final class AlbumAdapter extends BaseAdapter {
		private Context _context;
		private LayoutInflater _inflater;

		public AlbumAdapter(Context context) {
			this._context = context;
			this._inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			if (mAlbums != null) {
				return mAlbums.size();
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
			AlbumViewHolder holder;
			if (convertView == null) {
				holder = new AlbumViewHolder();
				convertView = _inflater.inflate(R.layout.album_info, null);
				holder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
				holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
				holder.tvSize = (TextView) convertView.findViewById(R.id.tvSize);
				convertView.setTag(holder);
			} else {
				holder = (AlbumViewHolder) convertView.getTag();
			}
			
			if (mAlbums != null) {
				if (mThumbnails != null) {
					Bitmap bitmap = mThumbnails[position];
					if (bitmap != null && !bitmap.isRecycled()) {	
						holder.ivIcon.setImageBitmap(bitmap);
					} else {
						holder.ivIcon.setImageBitmap(null);
					}
				} else {
					holder.ivIcon.setImageBitmap(null);
				}
				String albumName = mAlbums.getAlbumName(position);
				String albumSize = "(" + mAlbums.getImagesCount(position) + ")";
				holder.tvName.setText(albumName);
				holder.tvSize.setText(albumSize);
			}
			return convertView;
		}
	}
	
	private final static class AlbumViewHolder {
		public ImageView ivIcon;
		public TextView tvName;
		public TextView tvSize;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mThumbnails != null) {
			for (int i = 0; i < mThumbnails.length; i++) {
				if (mThumbnails[i] != null && !mThumbnails[i].isRecycled()) {
					mThumbnails[i].recycle();
					mThumbnails[i] = null;
				}
			}
		}
	}

	private class LoadDataTask extends AsyncTask<Void, Void, Boolean> {
		private boolean loadResult = false;
		
		private Albums albums;
		private Bitmap[] thumbnails;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mAlbumGrid.setVisibility(View.INVISIBLE);
			textViewLoading.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				albums = new Albums();
				albums.updateData(JkanjiAlbumActivity.this);
				thumbnails = new Bitmap[albums.size()];
				loadResult = true;				
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			mAlbumGrid.setVisibility(View.VISIBLE);
			textViewLoading.setVisibility(View.INVISIBLE);
			if (result == true && !JkanjiAlbumActivity.this.isFinishing()) {
				if (loadResult) {
					mAlbums = albums;
					mThumbnails = thumbnails;
					mAdapter.notifyDataSetChanged();
					new LoadThumbTask().execute();
				} else {
					
				}
			} else if (result == false) {
				finish();
			}
		}
    }
	
	private class LoadThumbTask extends AsyncTask<Void, Void, Boolean> {
		private final static float PERCENT = 0.8f;
		
		private boolean loadResult = false;
		private BitmapFactory.Options options;
		private byte[] tempStorage = new byte[16 * 1024];
		private long totalsize = 0;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			options = new BitmapFactory.Options();
			options.inDither = true;
			options.inSampleSize = 2;
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			options.inPurgeable = true;  
			options.inInputShareable = true;
			options.inTempStorage = tempStorage;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				for (int position = 0; position < mAlbums.size(); position++) {
					if (totalsize > Runtime.getRuntime().maxMemory() * PERCENT) {
						break;
					}
					int albumId = mAlbums.getAlbumIcon(position);
					Bitmap bitmap = Thumbnails.getThumbnail(
						JkanjiAlbumActivity.this.getContentResolver(), 
						albumId,
						Thumbnails.MINI_KIND, //MediaStore.Images.Thumbnails.MICRO_KIND, 
						options);
					mThumbnails[position] = bitmap;
					if (bitmap != null) {
						totalsize += bitmap.getRowBytes() * bitmap.getHeight();
					}
					if (D) {
						Log.e(TAG, "position == " + position + ", totalsize == " + totalsize + 
							", max == " + Runtime.getRuntime().maxMemory() * PERCENT);
					}
					this.publishProgress();
				}
				loadResult = true;				
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			mAdapter.notifyDataSetChanged();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (totalsize > Runtime.getRuntime().maxMemory() * PERCENT) {
				Toast.makeText(JkanjiAlbumActivity.this, 
					"内存不足，没有加载全部相册缩略图", 
					Toast.LENGTH_SHORT).show();
			}
			if (result == true && !JkanjiAlbumActivity.this.isFinishing()) {
				if (loadResult) {
					mAdapter.notifyDataSetChanged();
				} else {
					
				}
			} else if (result == false) {
				finish();
			}
		}
    }
}