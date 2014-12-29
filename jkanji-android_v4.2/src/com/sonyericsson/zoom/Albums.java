package com.sonyericsson.zoom;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;

public class Albums {
	private final static String[] PROJECTION = { 
		MediaStore.Images.Thumbnails._ID,
		MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
		MediaStore.Images.Media.DISPLAY_NAME, 
		MediaStore.Images.Media.DATA
	};
	
	private ArrayList<String> mAlbumNames;
	private ArrayList<Integer> mAlbumIcons;
	private ArrayList<Integer> mImagesCount;
	private ArrayList<AlbumStruct> mAlbumStructs;
	private ArrayList<String> mAlbumFirstFileNames;
	
	public void updateData(Context context) {
		mAlbumNames = new ArrayList<String>();
		mAlbumIcons = new ArrayList<Integer>();
		mImagesCount = new ArrayList<Integer>();
		mAlbumStructs = new ArrayList<AlbumStruct>();
		mAlbumFirstFileNames = new ArrayList<String>();
		
		try {
			boolean isSortFilenameNum = JkanjiGallerySettingActivity.getSortFilenameNum(context);
			ContentResolver _contentResolver = context.getContentResolver();
			Cursor cursor = _contentResolver.query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					PROJECTION, null, null, null);
			
			if (cursor != null) {			
				int lenght = cursor.getCount();
				if (cursor.moveToFirst()) {
					for (int i = 0; i < lenght; i++) {
						cursor.moveToPosition(i);
						
						int imageId = cursor.getInt(cursor.getColumnIndexOrThrow(
							Images.Media._ID));
						String albumName = cursor.getString(cursor.getColumnIndexOrThrow(
							Images.Media.BUCKET_DISPLAY_NAME));
						String imageName = cursor.getString(cursor.getColumnIndex(
							Images.Media.DISPLAY_NAME));
						
						if (containsAlbumName(albumName)) {
							int position = indexOfAlbum(albumName);
							if (position >= 0 && position < mAlbumStructs.size()) {
								mAlbumStructs.get(position).addImageId(imageId);
								mAlbumStructs.get(position).addImageName(imageName);
							}
						} else {		
							String path = cursor.getString(cursor.getColumnIndexOrThrow(
								Images.Media.DATA));
							String absolutePath = new File(path).getParent();
							
							AlbumStruct tempAlbumStruct = new AlbumStruct(albumName, absolutePath);
							tempAlbumStruct.addImageId(imageId);
							tempAlbumStruct.addImageName(imageName);
							mAlbumStructs.add(tempAlbumStruct);
							addAlbumName(albumName); // thumb name
							//addAlbumIcon(imageId); // thumb bitmap
						}
					}
					int count = size();
					for (int i = 0; i < count; i++) {
						if (i >= 0 && i < mAlbumStructs.size()) {
							AlbumStruct albumStruct = mAlbumStructs.get(i);
							addImagesCount(albumStruct.size());
							albumStruct.calcFirstName(isSortFilenameNum);
							addFirstName(albumStruct.mFirstName);
							addAlbumIcon(albumStruct.mFirstId);
						}
					}
				}
				cursor.close();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private boolean containsAlbumName(String albumName) {
		return mAlbumNames.contains(albumName);
	}

	private int indexOfAlbum(String albumName) {
		return mAlbumNames.indexOf(albumName);
	}

	private void addAlbumName(String albumName) {
		mAlbumNames.add(albumName);
	}

	private void addAlbumIcon(int iconId) {
		mAlbumIcons.add(iconId);
	}
	
	private void addImagesCount(int count) {
		mImagesCount.add(count);
	}
	
	public String getAlbumName(int position) {
		return mAlbumNames.get(position);
	}

	public int getAlbumIcon(int position) {
		return mAlbumIcons.get(position);
	}

	public int getImagesCount(int numberAlbum) {
		return mImagesCount.get(numberAlbum);
	}
	
	public String getAbsoluteAlbumPath(int position) {
		return mAlbumStructs.get(position).mAbsoluteAlbumPath;
	}

	private void addFirstName(String name) {
		mAlbumFirstFileNames.add(name);
	}
	
	//thumb file name
	public String getAlbumFirstFileName(int position) {
		if (false) {
			String path = mAlbumStructs.get(position).mAbsoluteAlbumPath;
			ArrayList<String> filenames = mAlbumStructs.get(position).mImageNames;
			if (filenames.size() > 0) {
				String filename = filenames.get(0);
				if (path != null && filename != null) {
					return new File(path, filename).getAbsolutePath();
				}
			}
			return null;
		} else {
			String path = mAlbumStructs.get(position).mAbsoluteAlbumPath;
			String filename = mAlbumFirstFileNames.get(position);
			if (path != null && filename != null) {
				return new File(path, filename).getAbsolutePath();
			}
			return null;
		}
	}
	
	public int size() {
		if (mAlbumNames != null) {
			return mAlbumNames.size();
		} else {
			return 0;
		}
	}
	
	private final static class AlbumStruct {
		private String mAlbumName;
		private String mAbsoluteAlbumPath;

		private ArrayList<Integer> mImageIds;
		private ArrayList<String> mImageNames;
		
		private String mFirstName;
		private int mFirstId;

		public AlbumStruct(String albumName, String path) {
			this.mAlbumName = albumName;
			this.mAbsoluteAlbumPath = path;
			
			this.mImageIds = new ArrayList<Integer>();
			this.mImageNames = new ArrayList<String>();
		}

		public void addImageId(int imageId) {
			mImageIds.add(imageId);
		}

		public void addImageName(String imageName) {
			mImageNames.add(imageName);
		}

		public int size() {
			return mImageIds.size();
		}
		
		public void calcFirstName(final boolean isSortFilenameNum) {
			String[] files = new String[mImageNames.size()];
			for (int i = 0; i < mImageNames.size(); i++) {
				files[i] = mImageNames.get(i);
			}
    		Arrays.sort(files, new Comparator<String>() {
				@Override
				public int compare(String lhs, String rhs) {
					if (lhs == null) {
						return -1;
					} else if (rhs == null) {
						return 1;
					} else {
						if (isSortFilenameNum) {
							return FileNameCompare.compareParts(lhs, rhs);
						} else {
							return lhs.compareToIgnoreCase(rhs);
						}
					}
				}
    		});
    		mFirstName = files[0];
			for (int i = 0; i < mImageNames.size(); i++) {
				if (mImageNames.get(i).equals(mFirstName)) {
					mFirstId = mImageIds.get(i);
				}
			}
		}
	}
}