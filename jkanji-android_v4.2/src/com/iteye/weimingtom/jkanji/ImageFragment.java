package com.iteye.weimingtom.jkanji;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.iteye.weimingtom.jkanji.R;

/**
 * CANNOT BE private final static class
 * @author Administrator
 *
 */
public class ImageFragment extends Fragment {
	private static final String KEY_PAGE = "Image.page";

    public int page;
    private ImageView image;
    
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
    	image = new ImageView(getActivity());
        image.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        layout.addView(image);
        
        //Log.e("BitmapFragment", "onCreateView " + this.page);
        switch (page) {
        case 0:
        	image.setImageResource(R.drawable.tutorial1);
        	break;
        	
        case 1:
        	image.setImageResource(R.drawable.tutorial2);
        	break;
        	
        case 2:
        	image.setImageResource(R.drawable.tutorial3);
        	break;
        	
        case 3:
        	image.setImageResource(R.drawable.tutorial4);
        	break;
        	
        case 4:
        default:
        	image.setImageResource(R.drawable.tutorial5);
        	break;
        }
        return layout;
    }

	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PAGE, this.page);
    }
}
