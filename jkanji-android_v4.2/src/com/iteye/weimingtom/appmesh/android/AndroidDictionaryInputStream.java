package com.iteye.weimingtom.appmesh.android;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;

import com.iteye.weimingtom.appmesh.file.DictionaryInputStream;

public class AndroidDictionaryInputStream extends DictionaryInputStream {
	private AssetManager am;
	
	public AndroidDictionaryInputStream(String filename, AssetManager am) throws IOException {
		this.filename = filename;
		this.am = am;
		reset();
		init();
	}
	
	@Override
    public void reset() throws IOException {
    	if (istr != null && istr.markSupported()) {
	    	istr.reset();
    	} else {
    		if (istr != null) {
    			istr.close();
    		}
    		//TODO: Android mod
    		//Exception in thread "main" java.io.IOException: Resetting to invalid mark
    		InputStream inputStream = null;
    		if (USE_BUFFERD_INPUTSTREAM) {
    			inputStream = new BufferedInputStream(this.am.open(filename), BUFFER_INPUTSTREAM_SIZE);
    		} else {
    			inputStream = this.am.open(filename);
    		}
    		istr = inputStream;
    		if (istr.markSupported()) {
        		istr.mark(0);
    		}
    	}
    }
}
