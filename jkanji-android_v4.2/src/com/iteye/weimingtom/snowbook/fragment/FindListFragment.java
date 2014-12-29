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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.iteye.weimingtom.appmesh.android.DictionarySearchTask;
import com.iteye.weimingtom.appmesh.dictionary.Node;
import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.snowbook.adapter.FindListItemAdapter;

public class FindListFragment extends Fragment {
	private static final boolean D = false;
	private static final String TAG = "FindListFragment";
	
	private ListView listView1;
	private FindListItemAdapter listViewAdapter;
	//private List<FindListItemModel> modelList;
    private EditText editTextSearch;
    private Button buttonSearch;
    
    private Activity mContext;
    //private Dictionary dictionary = new Dictionary();
    private String filename = "appmesh/n5.bin";
    //private DictionaryInput input = null; 
    private List<Node> words;
    private DictionarySearchTask searchTask = null;
	private static final int KILL_TASK_TIMEOUT = 1000;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
    	return inflater.inflate(R.layout.snowbook_find_mylist, null);        
    }
	
    @Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mContext = this.getActivity();
		
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
		
		//modelList = new ArrayList<FindListItemModel>();
		words = new LinkedList<Node>();
		listView1 = (ListView) view.findViewById(R.id.activity_mylist_listview);
		
		listViewAdapter = new FindListItemAdapter(this.getActivity(), words);
		listView1.setAdapter(listViewAdapter);
		listView1.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position >= 0 && position < words.size()) {
					words.get(position).toggleExpanded();
				}
				listViewAdapter.notifyDataSetChanged();
//				Toast.makeText(getActivity(), 
//					"click", Toast.LENGTH_SHORT).show();
			}
		});
		loadData(true);
		
	    searchTask = new DictionarySearchTask(mContext, listViewAdapter, filename, "", words);
	    searchTask.execute();
	}
    
    private void loadData(final boolean hideLoading) {
		//modelList.add(model); 
		//listViewAdapter.notifyDataSetChanged();
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
	
	public void onQueryTextSubmit(String query) {
		killTask();
	    searchTask = new DictionarySearchTask(mContext, listViewAdapter, filename, query, words);
        searchTask.execute();
	}
	
	public void onQueryTextChange(String newText) {
		killTask();
	    searchTask = new DictionarySearchTask(mContext, listViewAdapter, filename, newText, words);
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
