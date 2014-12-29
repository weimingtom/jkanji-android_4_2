package com.example.android.bitmapfun.provider;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.util.Log;

public class FileImageWorkerAdapter {
    private static final boolean D = false;
    private static final String TAG = "FileImageWorkerAdapter";
    
    public static final String JUMP_ROOT = "转至根目录";
	public static final String JUMP_UP = "向上";
	public static final String ROOT_PATH = "/";
	
	private String pathname;
    private ArrayList<String> arrayList;
    
	public FileImageWorkerAdapter(String pathname, final boolean hasFolder, final boolean isSortFilenameNum, final String[] fileNames) {
    	this.pathname = pathname;
    	this.arrayList = new ArrayList<String>();
    	if (hasFolder) {
    		arrayList.add(JUMP_ROOT);
    		arrayList.add(JUMP_UP);
    	}
    	if (fileNames != null) {
    		for (int i = 0; i < fileNames.length; i++) {
    			arrayList.add(pathname + File.separator + fileNames[i]);
    		}
    	} else {
	    	File dir = new File(pathname);
	    	if (dir.isDirectory()) {
	    		final String pathname2 = this.pathname;
	        	File[] files = dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String filename) {
						if (filename != null) {
							if (D) {
								Log.d(TAG, "accept : " + filename);
							}
							File file = new File(pathname2, filename);
							if (hasFolder && file.isDirectory()) {
								return true;
							} else if (file.isFile()) {
								String filename2 = filename.toLowerCase();
								if (filename2 != null && 
									(filename2.endsWith(".png") || 
									filename2.endsWith(".jpg") || 
									filename2.endsWith(".jpeg") ||
									filename2.endsWith(".gif"))) {
									return true;
								}
							}
						}
						return false;
					}
	        	});
	    		ArrayList<String> dir1 = new ArrayList<String>();
	    		ArrayList<String> dir2 = new ArrayList<String>();
	        	if (files != null) {
	        		for (File file : files) {
						if (file.isDirectory()) {
							dir1.add(file.getPath());
						} else {
							dir2.add(file.getPath());
						}
	        		}
	        	}
	    		Collections.sort(dir1, new Comparator<String>() {
	    			@Override
	    			public int compare(String arg0, String arg1) {
	    				if (arg0 == null) {
	    					return -1;
	    				}
	    				if (arg1 == null) {
	    					return 1;
	    				}
						if (isSortFilenameNum) {
							return FileNameCompare.compareParts(arg0, arg1);
						} else {
							return arg0.compareToIgnoreCase(arg1);
						}
	    			}
	    		});
	    		Collections.sort(dir2, new Comparator<String>() {
	    			@Override
	    			public int compare(String arg0, String arg1) {
	    				if (arg0 == null) {
	    					return -1;
	    				}
	    				if (arg1 == null) {
	    					return 1;
	    				}
						if (isSortFilenameNum) {
							return FileNameCompare.compareParts(arg0, arg1);
						} else {
							return arg0.compareToIgnoreCase(arg1);
						}
	    			}
	    		});
	    		arrayList.addAll(dir1);
	    		arrayList.addAll(dir2);
	    	}
    	}
    }
	
	public String getParentPath() {
		String parent = new File(pathname).getParent();
		return parent != null ? parent : ""; 
	} 
	
    public Object getItem(int num) {
		return arrayList.get(num);
    }

    public int getSize() {
    	return arrayList.size();
    }
    
}
