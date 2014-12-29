package com.iteye.weimingtom.snowbook.fragment;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.iteye.weimingtom.appmesh.android.DictionarySearchTask;
import com.iteye.weimingtom.appmesh.dictionary.Node;
import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.snowbook.adapter.ExpandListAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;

public class ExpandListFragment extends Fragment {
	private static final int KILL_TASK_TIMEOUT = 1000;
	private static final boolean RESUME_SEARCH = false;
	
	private ListView mListView;
    private ExpandListAdapter mExpandListAdapter;
    private boolean mLimited = false;
    private EditText editTextSearch;
    private Button buttonSearch;
    
    private Activity mContext;
    //private Dictionary dictionary = new Dictionary();
    private String filename = "appmesh/n5.bin";
    //private DictionaryInput input = null; 
    private List<Node> words = new LinkedList<Node>();
    private DictionarySearchTask searchTask = null;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
    	return inflater.inflate(R.layout.snowbook_expand_mylist, null);        
    }

    @Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		editTextSearch = (EditText) view.findViewById(R.id.editTextSearch);
		buttonSearch = (Button) view.findViewById(R.id.buttonSearch);
		buttonSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hideIme(editTextSearch);
				onQueryTextSubmit(editTextSearch.getText().toString());
			}
		});
		editTextSearch.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					hideIme(editTextSearch);
				}
			}
		});
		editTextSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				onQueryTextChange(s.toString());
			}

			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		
		mListView = (ListView) view.findViewById(R.id.activity_mylist_listview);
		mListView.setDivider(null);
		
		mContext = this.getActivity();
        mExpandListAdapter = new ExpandListAdapter(this.getActivity(), words);
        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(mExpandListAdapter);
        alphaInAnimationAdapter.setAbsListView(getListView());
        alphaInAnimationAdapter.setInitialDelayMillis(500);
        getListView().setAdapter(alphaInAnimationAdapter);
        
        mExpandListAdapter.setLimit(mLimited ? 2 : 0);
        
		killTask();
	    searchTask = new DictionarySearchTask(mContext, mExpandListAdapter, filename, "", words);
	    searchTask.execute();
    }
    
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (RESUME_SEARCH) {
			killTask();
		    searchTask = new DictionarySearchTask(mContext, mExpandListAdapter, filename, "", words);
	        searchTask.execute();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		killTask();
	}
	
	public void onPause() {
		super.onPause();
		hideIme(editTextSearch);
	}
	
	public void killTask() {
		if (searchTask != null) {
			searchTask.setStop(true);
			try {
				searchTask.get(KILL_TASK_TIMEOUT, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
			searchTask = null;
		}
	}

	public ListView getListView() {
		return mListView;
	}
	
	public void onQueryTextSubmit(String query) {
		killTask();
	    searchTask = new DictionarySearchTask(mContext, mExpandListAdapter, filename, query, words);
        searchTask.execute();
	}
	
	public void onQueryTextChange(String newText) {
		killTask();
	    searchTask = new DictionarySearchTask(mContext, mExpandListAdapter, filename, newText, words);
        searchTask.execute();
	}
	
    private void hideIme(final EditText editText) {
    	InputMethodManager imm = (InputMethodManager)
    			this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    	if (imm != null) {
    		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }
    
    public void onSlidingMenuOpen() {
    	hideIme(editTextSearch);
    }
}
