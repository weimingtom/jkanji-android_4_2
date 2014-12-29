package com.iteye.weimingtom.snowbook.demo;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.snowbook.adapter.NovelPagerAdapter;
import com.viewpagerindicator.TabPageIndicator;

public class TabPageFragment extends Fragment {
	private static final boolean D = false;
	private static final String TAG = "TabPageFragment";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) {
			Log.e(TAG, "TabPageFragment onCreate");
		}
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (D) {
			Log.e(TAG, "TabPageFragment onCreateView");
		}
		
    	final View layout = inflater.inflate(R.layout.creader_main_tabs, container, false);
        
        ViewPager pager = (ViewPager)layout.findViewById(R.id.pager);
        PagerAdapter adapter = new NovelPagerAdapter(pager);
        pager.setAdapter(adapter);
        TabPageIndicator indicator = (TabPageIndicator)layout.findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        
    	return layout;
    }
}
