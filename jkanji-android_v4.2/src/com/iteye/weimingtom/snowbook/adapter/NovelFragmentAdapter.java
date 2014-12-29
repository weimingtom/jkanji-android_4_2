package com.iteye.weimingtom.snowbook.adapter;

import com.iteye.weimingtom.snowbook.demo.GridFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class NovelFragmentAdapter extends FragmentPagerAdapter {
	public NovelFragmentAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		Fragment fragment = new GridFragment();
		return fragment;
	}

    @Override
    public CharSequence getPageTitle(int position) {
        return "标题" + position;
    }
	
	@Override
	public int getCount() {
		return 10;
	}
}
