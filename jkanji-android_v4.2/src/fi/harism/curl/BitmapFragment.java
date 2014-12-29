package fi.harism.curl;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

/**
 * 如果修改ViewPagerActivity的基类，请解除下面代码行的注释
				//FIXME:
				//updatePage((ViewPagerActivity) getActivity(), image, width, height, page);
 * 
 * 必须有无参数的构造函数，否则会在转屏重启后报错
 * @author Administrator
 *
 */
public class BitmapFragment extends Fragment {
	private final static boolean D = false;
	private final static String TAG = "BitmapFragment";
	
	private static final String KEY_PAGE = "fi.harism.curl.BitmapFragment.page";

    public int page;
    private BitmapFragmentView image;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_PAGE)) {
            this.page = savedInstanceState.getInt(KEY_PAGE);
        }

        //Log.e("BitmapFragment", "onCreate " + this.page);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	image = new BitmapFragmentView(getActivity());
        image.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        image.setSizeChangedObserver(new BitmapFragmentView.SizeChangedObserver() {
			@Override
			public void onSizeChanged(int width, int height) {
				//FIXME:
				//updatePage((ViewPagerActivity) getActivity(), image, width, height, page);
			}
        });
        image.setClickable(false);
        
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        layout.addView(image);
        
        //Log.e("BitmapFragment", "onCreateView " + this.page);
        
        return layout;
    }

	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PAGE, this.page);
    }
	
	private void updatePage(ViewPagerActivity activity, BitmapFragmentView view, int width, int height, int index) {
		if (activity != null) {
			if (D) {
				Log.d(TAG, "updatePage " + page + ", " + width + "," + height);
			}
			activity.updatePager(view, width, height, index);
		}				
	}
}

