/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.calculator2mod;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Vector;

import org.javia.arity.SyntaxException;

import com.iteye.weimingtom.jkanji.R;

public class HistoryAdapter extends BaseAdapter {
    private Vector<HistoryEntry> mEntries;
    private LayoutInflater mInflater;
    private Logic mEval;
    
    public HistoryAdapter(Context context, History history, Logic evaluator) {
        mEntries = history.mEntries;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mEval = evaluator;
    }

    @Override
    public int getCount() {
    	if (mEntries != null) {
    		return mEntries.size() - 1;
    	} else {
    		return 0;
    	}
    }

    @Override
    public Object getItem(int position) {
    	if (mEntries != null && position >= 0 && position < mEntries.size()) {
    		return mEntries.elementAt(position);
    	} else {
    		return null;
    	}
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.calc_history_item, parent, false);
        } else {
            view = convertView;
        }
        TextView expr = (TextView) view.findViewById(R.id.historyExpr);
        TextView result = (TextView) view.findViewById(R.id.historyResult);
        HistoryEntry entry = null;
        if (mEntries != null && position >= 0 && position < mEntries.size()) {
        	entry = mEntries.elementAt(position);
        }
        if (entry != null) {
	        String base = entry.getBase();
	        expr.setText(entry.getBase());
	        try {
	            String res = mEval.evaluate(base);
	            result.setText("= " + res);
	        } catch (SyntaxException e) {
	            result.setText("");
	        }
        } else {
	    	expr.setText(null);
	    	result.setText(null);
	    }
        return view;
    }
}

