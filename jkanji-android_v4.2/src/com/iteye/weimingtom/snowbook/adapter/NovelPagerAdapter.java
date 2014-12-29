package com.iteye.weimingtom.snowbook.adapter;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iteye.weimingtom.jkanji.R;

public class NovelPagerAdapter extends PagerAdapter {
    private View mSimplePage;
    private LayoutInflater inflater;
    
    public NovelPagerAdapter(ViewPager parent) {	
    	inflater = LayoutInflater.from(parent.getContext());
    }
	
    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public void startUpdate(View container) {
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //final View page = mSimplePage; //position == 0 ? mSimplePage : mAdvancedPage;
        final View simplePage = inflater.inflate(R.layout.creader_page, container, false);
        final View page = simplePage;
    	((ViewGroup) container).addView(page);
        return page;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        ((ViewGroup) container).removeView((View) object);
    }

    @Override
    public void finishUpdate(View container) {
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }
}
