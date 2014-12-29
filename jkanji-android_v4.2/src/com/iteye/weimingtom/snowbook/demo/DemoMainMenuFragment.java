package com.iteye.weimingtom.snowbook.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.snowbook.activity.MainActivity;
import com.iteye.weimingtom.snowbook.fragment.FindListFragment;

public class DemoMainMenuFragment extends ListFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.snowbook_list, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		String[] colors = getResources().getStringArray(R.array.snowbook_color_names);
		ArrayAdapter<String> colorAdapter = new ArrayAdapter<String>(getActivity(), 
				android.R.layout.simple_list_item_1, android.R.id.text1, colors);
		setListAdapter(colorAdapter);
	}

	/**
	 * @see R.array.color_names
	 */
	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		Fragment newContent = null;
		switch (position) {
		case 0:
			newContent = new TabPageFragment();
			break;
			
		case 1:
			newContent = new GridFragment();
			break;
			
		case 2:
			newContent = new ColorFragment(R.color.snowbook_red);
			break;
			
		case 3:
			newContent = new ColorFragment(R.color.snowbook_green);
			break;
			
		case 4:
			newContent = new ColorFragment(R.color.snowbook_blue);
			break;
			
		case 5:
			newContent = new ColorFragment(android.R.color.white);
			break;
			
		case 6:
			newContent = new ColorFragment(android.R.color.black);
			break;
			
		case 7:
			newContent = new ExpandableListItemFragment();
			break;
			
		case 8:
			//newContent = new ExpandListFragment();
			newContent = new FindListFragment();
			break;
		}
		if (newContent != null)
			switchFragment(newContent);
	}

	// the meat of switching the above fragment
	private void switchFragment(Fragment fragment) {
		if (getActivity() == null)
			return;
		
		if (getActivity() instanceof MainActivity) {
			MainActivity fca = (MainActivity) getActivity();
			fca.switchContent(fragment);
		}
	}
}
