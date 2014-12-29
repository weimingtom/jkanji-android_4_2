package fi.harism.curl;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class BitmapFragmentAdapter extends FragmentPagerAdapter {	
	private int pageCount = 1;
    
	public BitmapFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

	public void setPageCount(int count) {
		this.pageCount = count;
	}
	
    @Override
    public Fragment getItem(int position) {
    	BitmapFragment result = new BitmapFragment();
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
