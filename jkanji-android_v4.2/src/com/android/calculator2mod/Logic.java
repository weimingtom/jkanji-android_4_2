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

import com.android.calculator2mod.CalculatorDisplay.Scroll;
import com.iteye.weimingtom.jkanji.R;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.EditText;
import android.content.Context;
import android.content.res.Resources;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import org.javia.arity.Symbols;
import org.javia.arity.SyntaxException;

public class Logic {
    private static final String INFINITY = "Infinity";
    private static final String NAN = "NaN";
    public static final char MINUS = '\u2212';
    private static final String INFINITY_UNICODE = "\u221e";
    public static final String MARKER_EVALUATE_ON_RESUME = "?";
    private final String mErrorString;
    public final static int DELETE_MODE_BACKSPACE = 0;
    public final static int DELETE_MODE_CLEAR = 1;

    public interface Listener {
        void onDeleteModeChange();
    }
    
	private CalculatorDisplay mDisplay;
    private Symbols mSymbols = new Symbols();
    private History mHistory;
    private String  mResult = "";
    private boolean mIsError = false;
    private int mLineLength = 0;
    private int mDeleteMode = DELETE_MODE_BACKSPACE;
    private Listener mListener;
    private Context mContext;
    private Set<Entry<String, String>> mTranslationsSet;

    public Logic(Context context, History history, CalculatorDisplay display) {
        mContext = context;
        mErrorString = mContext.getResources().getString(R.string.error);
        mHistory = history;
        mDisplay = display;
        mDisplay.setLogic(this);
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public void setDeleteMode(int mode) {
        if (mDeleteMode != mode) {
            mDeleteMode = mode;
            mListener.onDeleteModeChange();
        }
    }

    public int getDeleteMode() {
        return mDeleteMode;
    }

    public void setLineLength(int nDigits) {
        mLineLength = nDigits;
    }

    public boolean eatHorizontalMove(boolean toLeft) {
        EditText editText = mDisplay.getEditText();
        int cursorPos = editText.getSelectionStart();
        return toLeft ? cursorPos == 0 : cursorPos >= editText.length();
    }

    private String getText() {
        return mDisplay.getText().toString();
    }

    public void insert(String delta) {
        mDisplay.insert(delta);
        setDeleteMode(DELETE_MODE_BACKSPACE);
    }

    public void onTextChanged() {
        setDeleteMode(DELETE_MODE_BACKSPACE);
    }

    public void resumeWithHistory() {
        clearWithHistory(false);
    }

    private void clearWithHistory(boolean scroll) {
        String text = mHistory.getText();
        if (MARKER_EVALUATE_ON_RESUME.equals(text)) {
            if (!mHistory.moveToPrevious()) {
                text = "";
            }
            text = mHistory.getText();
            evaluateAndShowResult(text, CalculatorDisplay.Scroll.NONE);
        } else {
            mResult = "";
            mDisplay.setText(
                    text, scroll ? CalculatorDisplay.Scroll.UP : CalculatorDisplay.Scroll.NONE);
            mIsError = false;
        }
    }

    private void clear(boolean scroll) {
    	//FIXME:
//        mHistory.enter("");
        mDisplay.setText("", scroll ? CalculatorDisplay.Scroll.UP : CalculatorDisplay.Scroll.NONE);
        cleared();
    }

    public void cleared() {
        mResult = "";
        mIsError = false;
        updateHistory();
        setDeleteMode(DELETE_MODE_BACKSPACE);
    }

    public boolean acceptInsert(String delta) {
        String text = getText();
        return !mIsError &&
            (!mResult.equals(text) ||
             isOperator(delta) ||
             mDisplay.getSelectionStart() != text.length());
    }

    public void onDelete() {
        if (getText().equals(mResult) || mIsError) {
            clear(false);
        } else {
            mDisplay.dispatchKeyEvent(new KeyEvent(0, KeyEvent.KEYCODE_DEL));
            mResult = "";
        }
    }

    public void onClear() {
        clear(mDeleteMode == DELETE_MODE_CLEAR);
    }

    public void onEnter() {
        if (mDeleteMode == DELETE_MODE_CLEAR) {
            clearWithHistory(false); // clear after an Enter on result
        } else {
            evaluateAndShowResult(getText(), CalculatorDisplay.Scroll.UP);
        }
    }

    public void evaluateAndShowResult(String text, Scroll scroll) {
        try {
            String result = evaluate(text);
            if (!text.equals(result)) {
                mHistory.enter(text);
                mResult = result;
                mDisplay.setText(mResult, scroll);
                setDeleteMode(DELETE_MODE_CLEAR);
            }
        } catch (SyntaxException e) {
            mIsError = true;
            mResult = mErrorString;
            mDisplay.setText(mResult, scroll);
            setDeleteMode(DELETE_MODE_CLEAR);
        }
    }

    public void onUp() {
        String text = getText();
        if (!text.equals(mResult)) {
            mHistory.update(text);
        }
        if (mHistory.moveToPrevious()) {
            mDisplay.setText(mHistory.getText(), CalculatorDisplay.Scroll.DOWN);
        }
    }

    public void onDown() {
        String text = getText();
        if (!text.equals(mResult)) {
            mHistory.update(text);
        }
        if (mHistory.moveToNext()) {
            mDisplay.setText(mHistory.getText(), CalculatorDisplay.Scroll.UP);
        }
    }

    public void updateHistory() {
        String text = getText();
        if (!TextUtils.isEmpty(text) && 
        	!TextUtils.equals(text, mErrorString) && 
        	text.equals(mResult)) {
            mHistory.update(MARKER_EVALUATE_ON_RESUME);
        } else {
            mHistory.update(getText());
        }
    }

    public String evaluate(String input) throws SyntaxException {
        if (input.trim().equals("")) {
            return "";
        }
        int size = input.length();
        while (size > 0 && isOperator(input.charAt(size - 1))) {
            input = input.substring(0, size - 1);
            --size;
        }
        input = replaceTranslations(input);
        double value = mSymbols.eval(input);
        String result = "";
        for (int precision = mLineLength; precision > 6; precision--) {
            result = tryFormattingWithPrecision(value, precision);
            if (result.length() <= mLineLength) {
                break;
            }
        }
        return result.replace('-', MINUS).replace(INFINITY, INFINITY_UNICODE);
    }

    private void addTranslation(HashMap<String, String> map, int t, int m) {
        Resources res = mContext.getResources();
        String translated = res.getString(t);
        String math = res.getString(m);
        if (!TextUtils.equals(translated, math)) {
            map.put(translated, math);
        }
    }

    private String replaceTranslations(String input) {
        if (mTranslationsSet == null) {
            HashMap<String, String> map = new HashMap<String, String>();
            addTranslation(map, R.string.sin, R.string.sin_mathematical_value);
            addTranslation(map, R.string.cos, R.string.cos_mathematical_value);
            addTranslation(map, R.string.tan, R.string.tan_mathematical_value);
            addTranslation(map, R.string.e, R.string.e_mathematical_value);
            addTranslation(map, R.string.ln, R.string.ln_mathematical_value);
            addTranslation(map, R.string.lg, R.string.lg_mathematical_value);
            mTranslationsSet = map.entrySet();
        }
        for (Entry<String, String> entry : mTranslationsSet) {
            input = input.replace(entry.getKey(), entry.getValue());
        }
        return input;
    }

    private String tryFormattingWithPrecision(double value, int precision) {
        String result = String.format(Locale.US, "%" + mLineLength + "." + precision + "g", value);
        if (result.equals(NAN)) {
            mIsError = true;
            return mErrorString;
        }
        String mantissa = result;
        String exponent = null;
        int e = result.indexOf('e');
        if (e != -1) {
            mantissa = result.substring(0, e);
            exponent = result.substring(e + 1);
            if (exponent.startsWith("+")) {
                exponent = exponent.substring(1);
            }
            exponent = String.valueOf(Integer.parseInt(exponent));
        } else {
            mantissa = result;
        }
        int period = mantissa.indexOf('.');
        if (period == -1) {
            period = mantissa.indexOf(',');
        }
        if (period != -1) {
            // Strip trailing 0's
            while (mantissa.length() > 0 && mantissa.endsWith("0")) {
                mantissa = mantissa.substring(0, mantissa.length() - 1);
            }
            if (mantissa.length() == period + 1) {
                mantissa = mantissa.substring(0, mantissa.length() - 1);
            }
        }
        if (exponent != null) {
            result = mantissa + 'e' + exponent;
        } else {
            result = mantissa;
        }
        return result;
    }

    public static boolean isOperator(String text) {
        return text.length() == 1 && isOperator(text.charAt(0));
    }

    public static boolean isOperator(char c) {
        return "+\u2212\u00d7\u00f7/*".indexOf(c) != -1;
    }
}
