package com.iteye.weimingtom.jkanji;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * CANNOT BE private final static class
 * @author Administrator
 *
 */
public class ImageFragmentAdapter extends FragmentPagerAdapter {	
	private int pageCount;
    
	public ImageFragmentAdapter(FragmentManager fm, int count) {
        super(fm);
        this.pageCount = count;
    }

    @Override
    public Fragment getItem(int position) {
    	ImageFragment result = new ImageFragment();
    	result.page = position;
        return result;
    }

    @Override
    public int getCount() {
        return this.pageCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
    	return "";
    }
}