package com.iteye.weimingtom.jkanji;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.nick.wwwjdic.krad.KradChart;

import spark.tomoe.DictionaryItem;
import spark.tomoe.HiraganaDictionary;
import spark.tomoe.HiraganaExtraDictionary;

import com.iteye.weimingtom.jkanji.HandInputView.OnChoiseChangedHandler;
import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class JkanjiHandActivity extends Activity {
	private static final boolean D = false;
	private static final String TAG = "JkanjiHandActivity";
	
	public static final String EXTRA_KEY_INIT_STRING = "com.iteye.weimingtom.jkanji.JkanjiHandActivity.initString";
	public static final String EXTRA_KEY_RESULT_STRING = "com.iteye.weimingtom.jkanji.JkanjiHandActivity.resultString";
	
	private static final int REQUEST_KRAD = 5;
	
	private EditText output;
	private Button ok;
	private Button finish;
	private HandInputView handinput;
	private Spinner choise;
	private ArrayAdapter<String> adapter;
	private ArrayAdapter<String> adapterSamples;
	private CheckBox checkBoxChoise;
	private DictionaryItem[] ditems, ditems2;
	private ActionBar actionBar;
	private LinearLayout linearLayoutEdit, linearLayoutConfig;
	private RadioButton radioButtonHand, radioButtonSel;
	private GridView gridViewSel;
	private ArrayAdapter<String> adapterSel;	
	private Button buttonSel1, buttonSel2, buttonSel3, buttonSel4, buttonClean;
	private Button buttonTrans, buttonKrad;
	
	private TextView textViewMessage;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.handinput);
        //this.setTitle("假名手写输入（测试版）");
        
        handinput = (HandInputView) this.findViewById(R.id.handinput);
        output = (EditText) this.findViewById(R.id.output);
        choise = (Spinner) this.findViewById(R.id.choise);
        linearLayoutEdit = (LinearLayout) this.findViewById(R.id.linearLayoutEdit);
        linearLayoutConfig = (LinearLayout) this.findViewById(R.id.linearLayoutConfig);
        radioButtonHand = (RadioButton) this.findViewById(R.id.radioButtonHand);
        radioButtonSel = (RadioButton) this.findViewById(R.id.radioButtonSel);
        gridViewSel = (GridView) this.findViewById(R.id.gridViewSel);
        buttonSel1 = (Button) this.findViewById(R.id.buttonSel1);
        buttonSel2 = (Button) this.findViewById(R.id.buttonSel2);
        buttonSel3 = (Button) this.findViewById(R.id.buttonSel3);
        buttonSel4 = (Button) this.findViewById(R.id.buttonSel4);
        buttonClean = (Button) this.findViewById(R.id.buttonClean);
        buttonTrans = (Button) this.findViewById(R.id.buttonTrans);
        buttonKrad = (Button) this.findViewById(R.id.buttonKrad);
        
        textViewMessage = (TextView) this.findViewById(R.id.textViewMessage);
        
        linearLayoutEdit.setVisibility(View.VISIBLE);
        linearLayoutConfig.setVisibility(View.GONE);
        
        adapterSel = new ArrayAdapter<String>(this, R.layout.grid_text_view);
        gridViewSel.setAdapter(adapterSel);
        
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("输入");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				return R.drawable.write_input;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.del;
			}

			@Override
			public void performAction(View view) {
				int st = output.getSelectionStart() - 1;
				int ed = output.getSelectionEnd();
				if (st >= 0 && ed >= 0) {
					output.getEditableText().delete(st, ed);				
				}
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.delete_normal;
			}

			@Override
			public void performAction(View view) {
				output.setText("");
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.config;
			}

			@Override
			public void performAction(View view) {
				if (linearLayoutConfig.getVisibility() == View.INVISIBLE) {
					linearLayoutConfig.setVisibility(View.VISIBLE);
					linearLayoutEdit.setVisibility(View.INVISIBLE);
				} else {
					linearLayoutConfig.setVisibility(View.INVISIBLE);
					linearLayoutEdit.setVisibility(View.VISIBLE);
				}
			}
        });
        /*
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.write_input;
			}

			@Override
			public void performAction(View view) {
				startActivityForResult(new Intent(JkanjiHandActivity.this, 
					KradChart.class), REQUEST_KRAD);
			}
        });
        */
        
        adapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        adapterSamples = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_spinner_item);
        adapterSamples.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        {
	        ditems = HiraganaDictionary.getDictionary();
	        for (int i = 0; i < ditems.length; i++) {
	        	DictionaryItem item = ditems[i];
	        	adapterSamples.add(Character.toString(item.c));
	        }
        }
        boolean useHiraganaExtra = JkanjiSettingActivity.getUseHiraganaExtra(this);
        if (useHiraganaExtra) {
	        ditems2 = HiraganaExtraDictionary.getDictionary();
	        for (int i = 0; i < ditems2.length; i++) {
	        	DictionaryItem item = ditems2[i];
	        	adapterSamples.add(Character.toString(item.c));
	        }
        }
        
        choise.setAdapter(adapter);
        choise.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (checkBoxChoise.isChecked()) {
					int pos = choise.getSelectedItemPosition();
					if (pos >= 0 && pos < ditems.length) {
						handinput.setDictionaryItem(ditems[pos]);
						handinput.invalidate();
					} else if (pos >= ditems.length && pos < ditems.length + ditems2.length){
						handinput.setDictionaryItem(ditems2[pos - ditems.length]);
						handinput.invalidate();						
					}
				} else {
					
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
        });
        
        handinput.setChoiseAdapter(adapter);
        handinput.setOnChoiseChangedHandler(new OnChoiseChangedHandler() {
			@Override
			public void onChoiseChanged(String text) {
				onChoose(text);
			}
			
			@Override
			public void onTouchDown() {
				hideKeyboard();
			}
        });
        
        ok = (Button) this.findViewById(R.id.ok);
        ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (checkBoxChoise.isChecked()) {
					outputChoise();					
				} else {
					outputChoise();
					handinput.clearBitmap();
					adapter.clear();
					
					adapterSel.clear();
					adapterSel.notifyDataSetChanged();
					initButtonSels();
				}
			}
        });
        
        finish = (Button) this.findViewById(R.id.finish);
        finish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent data = new Intent();
				data.putExtra(EXTRA_KEY_RESULT_STRING, output.getText().toString());
				setResult(RESULT_OK, data);
				finish();
			}
        });
        
        checkBoxChoise = (CheckBox) this.findViewById(R.id.checkBoxChoise);
        checkBoxChoise.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					choise.setAdapter(adapterSamples);
					handinput.setDictionaryItem(null);
					handinput.invalidate();
				} else {
					choise.setAdapter(adapter);
					handinput.setDictionaryItem(null);
					handinput.invalidate();
				}				
			}
        });
        radioButtonHand.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					handinput.setVisibility(View.VISIBLE);
					gridViewSel.setVisibility(View.INVISIBLE);
				}
			}
        });
        radioButtonSel.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					handinput.setVisibility(View.INVISIBLE);
					gridViewSel.setVisibility(View.VISIBLE);					
				}
			}
        });
        gridViewSel.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				output.getEditableText().replace(output.getSelectionStart(),
							output.getSelectionEnd(), adapterSel.getItem(position));
				handinput.clearBitmap();
				adapter.clear();
				adapterSel.clear();
				adapterSel.notifyDataSetChanged();
				initButtonSels();
				radioButtonHand.setChecked(true);
			}
        });
        buttonSel1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				buttonInput(buttonSel1.getText().toString());
			}
        });
        buttonSel1.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				buttonLongInput(buttonSel1.getText().toString());
				return true;
			}
        });
        buttonSel2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				buttonInput(buttonSel2.getText().toString());
			}
        });
        buttonSel3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				buttonInput(buttonSel3.getText().toString());
			}
        });
        buttonSel4.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				buttonInput(buttonSel4.getText().toString());
			}
        });
        buttonClean.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onButtonClean();
			}
        });
        buttonTrans.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				transformChar();
			}
        });
        buttonKrad.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onButtonKrad();
			}
        });
        initButtonSels();
        
        if (JKanjiActivity.getCacheGB2SJ() == null) {
        	textViewMessage.setVisibility(View.VISIBLE);
        	textViewMessage.setText("无法变换汉字，请进入搜索器加载汉字变换表");
        } else {
        	textViewMessage.setVisibility(View.GONE);
        }
        
        String initString = this.getIntent().getStringExtra(EXTRA_KEY_INIT_STRING);
        if (initString != null) {
        	output.getEditableText().append(initString);
        }
    }

    private void transformChar() {
		if (output != null) {
			String str = output.getEditableText().toString();
			int selpos = output.getSelectionStart() - 1;
			String right = JapaneseKeyboard.nextChar(str, selpos);
			if (right != null) {
				output.getEditableText().replace(selpos,
						output.getSelectionEnd(), right);
			} else {
				String right2 = nextKanji(str, selpos);
				if (right2 != null) {
					output.getEditableText().replace(selpos,
							output.getSelectionEnd(), right2);
				}
			}
		}
    }
    
    private String nextKanji(String str, int sepIndex) {
    	ArrayList<String> cacheGB2SJ = JKanjiActivity.getCacheGB2SJ();
    	if (str == null || str.length() <= 0 || 
			sepIndex < 0 || sepIndex >= str.length() ||
			cacheGB2SJ == null) {
			return null;
		}
		Character right = str.charAt(sepIndex);
		for (String entry : cacheGB2SJ) {
			if (entry.indexOf(right) >= 0 && entry.length() > 1) {
				int index = entry.indexOf(right) + 1;
				if (index > entry.length() - 1) {
					index = 0;
				}
				return Character.toString(entry.charAt(index));
			}
		}
		return null;
    }
    
    private void outputChoise() {
    	if (adapter != null) {
			int position = choise.getSelectedItemPosition();
			if (position != AdapterView.INVALID_POSITION) {
				//output.append(item);
				if (checkBoxChoise.isChecked()) {
					output.getEditableText().replace(output.getSelectionStart(),
							output.getSelectionEnd(), adapterSamples.getItem(position));					
				} else {
					output.getEditableText().replace(output.getSelectionStart(),
							output.getSelectionEnd(), adapter.getItem(position));
				}
			}
    	}
    }

    
    
    private void onChoose(String text) {
    	if (text != null) {
    		initButtonSels();
    		for (int i = 0; i < text.length() && i < 4; i++) {
    			if (i == 0) {
    				buttonSel1.setText(Character.toString(text.charAt(0)));
    			} else if (i == 1) {
    				buttonSel2.setText(Character.toString(text.charAt(1)));
        		} else if (i == 2) {
    				buttonSel3.setText(Character.toString(text.charAt(2)));
        		} else if (i == 3) {
    				buttonSel4.setText(Character.toString(text.charAt(3)));
        		}
    		}
    		text = JapaneseKeyboard.getAllTransformChars(text);
			adapterSel.clear();
	        for (int i = 0; i < text.length(); i++) {
	        	adapterSel.add(Character.toString(text.charAt(i)));
	        }
			adapterSel.notifyDataSetChanged();
    	}
    }
    
    private void initButtonSels() {
		buttonSel1.setText("退格");
		buttonSel2.setText("变换");
		buttonSel3.setText("假名");
		buttonSel4.setText("清除");
    }
    
    private void buttonInput(String str) {
		if (str != null) {
			if (str.length() == 1) {
				output.getEditableText().replace(output.getSelectionStart(),
						output.getSelectionEnd(), str);
				handinput.clearBitmap();
				adapter.clear();
				adapterSel.clear();
				adapterSel.notifyDataSetChanged();
				initButtonSels();
				radioButtonHand.setChecked(true);
			} else if (str.equals("假名")) {
				String text = JapaneseKeyboard.getAllJapChars();
				adapterSel.clear();
		        for (int i = 0; i < text.length(); i++) {
		        	adapterSel.add(Character.toString(text.charAt(i)));
		        }
				adapterSel.notifyDataSetChanged();
				if (radioButtonSel.isChecked()) {
					radioButtonHand.setChecked(true);
					radioButtonSel.setChecked(false);
				} else {
					radioButtonSel.setChecked(true);
					radioButtonHand.setChecked(false);
		    	}
			} else if (str.equals("退格")) {
				onButtonDel();
			} else if (str.equals("变换")) {
				transformChar();
			} else if (str.equals("清除")) {
				onButtonClean();
			}
		}
    }
    
    private void buttonLongInput(String str) {
		if (str != null) {
			if (str.equals("退格")) {
				onButtonClear();
			}
		}
    }
    
    private void onButtonDel() {
    	int st = output.getSelectionStart() - 1;
		int ed = output.getSelectionEnd();
		if (st >= 0 && ed >= 0) {
			output.getEditableText().delete(st, ed);				
		}
    }
    
    private void onButtonKrad() {
		startActivityForResult(new Intent(JkanjiHandActivity.this, 
    			KradChart.class), REQUEST_KRAD);
    }
    
    private void onButtonClear() {
    	output.setText("");
    }
    
    private void onButtonClean() {
		handinput.clearBitmap();
		adapter.clear();
		adapterSel.clear();
		adapterSel.notifyDataSetChanged();
		initButtonSels();
    }
    
	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager)
				getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(output.getWindowToken(), 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_KRAD && data != null) {
			if (data.hasExtra(KradChart.KEY_CHAR_SELECT)) {
				String charSelect = data.getStringExtra(KradChart.KEY_CHAR_SELECT);
				if (charSelect != null && charSelect.length() > 0) {
					output.getEditableText().replace(output.getSelectionStart(),
						output.getSelectionEnd(), charSelect);
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
}
