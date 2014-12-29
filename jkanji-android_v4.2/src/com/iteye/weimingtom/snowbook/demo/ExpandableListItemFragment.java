package com.iteye.weimingtom.snowbook.demo;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.snowbook.adapter.ListViewAnimationsMyExpandableListItemAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;

public class ExpandableListItemFragment extends Fragment {
	private ListView mListView;
    private ListViewAnimationsMyExpandableListItemAdapter mExpandableListItemAdapter;
    private boolean mLimited = false;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
    	final View layout = inflater.inflate(R.layout.lva__mylist, container, false);        
		mListView = (ListView) layout.findViewById(R.id.activity_mylist_listview);
		mListView.setDivider(null);
		
        mExpandableListItemAdapter = new ListViewAnimationsMyExpandableListItemAdapter(this.getActivity(), getItems());
        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(mExpandableListItemAdapter);
        alphaInAnimationAdapter.setAbsListView(getListView());
        alphaInAnimationAdapter.setInitialDelayMillis(500);
        getListView().setAdapter(alphaInAnimationAdapter);
        
        mExpandableListItemAdapter.setLimit(mLimited ? 2 : 0);

    	return layout;
    }

	public ListView getListView() {
		return mListView;
	}

	public static ArrayList<Integer> getItems() {
		ArrayList<Integer> items = new ArrayList<Integer>();
		for (int i = 0; i < 1000; i++) {
			items.add(i);
		}
		return items;
	}
}
