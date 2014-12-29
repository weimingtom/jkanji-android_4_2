package com.iteye.weimingtom.jkanji;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class JkanjiHistoryActivity extends Activity {
	public static final String EXTRA_GET = "com.iteye.weimingtom.jkanji.JkanjiHistoryActivity.EXTRA_GET";
	
	private ListView listView1;
	private RadioButton radioButton1, radioButton2;
	private CheckBox checkBox1;
	private CheckBox checkBox2;
	private Button buttonCleanHistory;
	private Button buttonCleanFavourite;
	private ActionBar actionBar;
	
	private ArrayList<String> items;
	private HistoryItemsAdapter adapter;
	
	private boolean isGetting = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.history);
		
		listView1 = (ListView) this.findViewById(R.id.listView1);
		radioButton1 = (RadioButton) this.findViewById(R.id.radioButton1);
		radioButton2 = (RadioButton) this.findViewById(R.id.radioButton2);
		checkBox1 = (CheckBox) this.findViewById(R.id.checkBox1);
		checkBox2 = (CheckBox) this.findViewById(R.id.checkBox2);
		buttonCleanHistory = (Button) this.findViewById(R.id.buttonCleanHistory);
		buttonCleanFavourite = (Button) this.findViewById(R.id.buttonCleanFavourite);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("历史与收藏夹");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.view;
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
				return R.drawable.shareto;
			}

			@Override
			public void performAction(View view) {
				StringBuffer sb = new StringBuffer();
				for (String str : items) {
					if (str != null) {
						sb.append(str + "\n");
					}
				}
				
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.setType("text/plain");
				if (radioButton1.isChecked()) {
					intent.putExtra(Intent.EXTRA_SUBJECT, JkanjiAozoraReaderActivity.getTimeString() + " : 日语简易词典搜索历史");
				} else {
					intent.putExtra(Intent.EXTRA_SUBJECT, JkanjiAozoraReaderActivity.getTimeString() + " : 日语简易词典搜索收藏夹");
				}
                intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
                //startActivity(Intent.createChooser(intent, "共享方式"));
                try {
                	startActivity(intent);
                } catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(JkanjiHistoryActivity.this, 
						"共享方式出错", Toast.LENGTH_SHORT)
						.show();
                }
			}
        });
        
		items = new ArrayList<String>();
		adapter = new HistoryItemsAdapter(this, items);
		listView1.setAdapter(adapter);
		listView1.setFastScrollEnabled(true);
		listView1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String str = items.get(position);
				if (str != null) {
					if (checkBox1.isChecked() && radioButton1.isChecked()) {
						writeItem(JkanjiHistoryActivity.this, str, DataContext.FAVOURITE_FILENAME);
						Toast.makeText(JkanjiHistoryActivity.this, 
								"添加收藏：" + str, 
								Toast.LENGTH_SHORT)
								.show();
						//FIXME: not necessary
						//updateData();
						adapter.notifyDataSetChanged();
					} else if (checkBox2.isChecked()) {
						if (radioButton2.isChecked()) {
							removeItem(JkanjiHistoryActivity.this, items, str, DataContext.FAVOURITE_FILENAME);
							Toast.makeText(JkanjiHistoryActivity.this, 
									"移除收藏：" + str, 
									Toast.LENGTH_SHORT)
									.show();
						} else if (radioButton1.isChecked()) {
							removeItem(JkanjiHistoryActivity.this, items, str, DataContext.HISTORY_FILENAME);
							Toast.makeText(JkanjiHistoryActivity.this, 
									"移除历史：" + str, 
									Toast.LENGTH_SHORT)
									.show();							
						}
						adapter.notifyDataSetChanged();
					} else {
						if (isGetting) {
							Intent data = new Intent();
							data.putExtra(JKanjiActivity.EXTRA_KEY_SHEARCHTEXT, str);
							setResult(RESULT_OK, data);
							finish();
						} else {
							startActivity(new Intent(JkanjiHistoryActivity.this, JKanjiActivity.class)
								.putExtra(JKanjiActivity.EXTRA_KEY_SHEARCHTEXT, str)
							);							
						}
					}
				}
			}
		});
		
		radioButton1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				readItems(JkanjiHistoryActivity.this, items, DataContext.HISTORY_FILENAME);
				adapter.notifyDataSetChanged();
				
				checkBox1.setVisibility(CheckBox.VISIBLE);
				checkBox2.setVisibility(CheckBox.VISIBLE);
				buttonCleanHistory.setVisibility(Button.VISIBLE);
				buttonCleanFavourite.setVisibility(Button.GONE);
			}
		});
		
		radioButton2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				readItems(JkanjiHistoryActivity.this, items, DataContext.FAVOURITE_FILENAME);
				adapter.notifyDataSetChanged();
				
				checkBox1.setVisibility(CheckBox.GONE);
				checkBox2.setVisibility(CheckBox.VISIBLE);
				buttonCleanHistory.setVisibility(Button.GONE);
				buttonCleanFavourite.setVisibility(Button.VISIBLE);
			}
		});
		
		buttonCleanHistory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cleanItems(JkanjiHistoryActivity.this, DataContext.HISTORY_FILENAME);
				updateData();
			}
		});
		buttonCleanFavourite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cleanItems(JkanjiHistoryActivity.this, DataContext.FAVOURITE_FILENAME);
				updateData();
			}
		});		
		
		readItems(JkanjiHistoryActivity.this, items, DataContext.HISTORY_FILENAME);
		adapter.notifyDataSetChanged();
		
		checkBox1.setVisibility(CheckBox.VISIBLE);
		checkBox2.setVisibility(CheckBox.VISIBLE);
		buttonCleanHistory.setVisibility(Button.VISIBLE);
		buttonCleanFavourite.setVisibility(Button.GONE);
		
		Intent intent = this.getIntent();
		if (intent != null) {
			this.isGetting = intent.getBooleanExtra(EXTRA_GET, false);
		}
		
		checkBox1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked && checkBox2.isChecked()) {
					checkBox2.setChecked(false);
				}
			}
		});
		checkBox2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked && checkBox1.isChecked()) {
					checkBox1.setChecked(false);
				}
			}
		});
	}
	
	private void updateData() {
		if (radioButton1.isChecked()) {
			readItems(JkanjiHistoryActivity.this, items, DataContext.HISTORY_FILENAME);
			adapter.notifyDataSetChanged();
		} else {
			readItems(JkanjiHistoryActivity.this, items, DataContext.FAVOURITE_FILENAME);
			adapter.notifyDataSetChanged();			
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	public static void cleanItems(Context context, String filename) {
		FileOutputStream outstr = null;
		OutputStreamWriter writer = null;
		BufferedWriter buffer = null;
		try {
			outstr = context.openFileOutput(filename, Context.MODE_PRIVATE);
			writer = new OutputStreamWriter(outstr, "utf-8");
			buffer = new BufferedWriter(writer);
			buffer.write("");
			buffer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (buffer != null) {
				try {
					buffer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outstr != null) {
				try {
					outstr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static int writeItem(Context context, String item, String filename) {
		if (item == null || item.length() == 0) {
			return -1;
		}
		item = item.replace("\n", " ");
		
		
		ArrayList<String> oriItems = new ArrayList<String>();
		oriItems.add(item);
		
		FileInputStream instr = null;
		InputStreamReader reader = null;
		BufferedReader buffer = null;
		try {
			instr = context.openFileInput(filename);
			reader = new InputStreamReader(instr, "utf-8");
			buffer = new BufferedReader(reader);
			String line;
			while (null != (line = buffer.readLine())) {
				boolean isExist = false;
				for (String str : oriItems) {
					if (str != null && str.equals(line)) {
						isExist = true;
						break;
					}
				}
				if (!isExist) {
					oriItems.add(line); 
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (buffer != null) {
				try {
					buffer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (instr != null) {
				try {
					instr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		
		
		FileOutputStream outstr = null;
		OutputStreamWriter writer = null;
		BufferedWriter buffer2 = null;
		try {
			outstr = context.openFileOutput(filename, Context.MODE_PRIVATE);
			writer = new OutputStreamWriter(outstr, "utf-8");
			buffer2 = new BufferedWriter(writer);

			for (int i = 0; i < oriItems.size(); i++) {
				String item2 = oriItems.get(i);
				if (item != null) {
					buffer2.write(item2 + "\n");
					buffer2.flush();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (buffer2 != null) {
				try {
					buffer2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outstr != null) {
				try {
					outstr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return oriItems.size();
	}
	
	public static void removeItem(Context context, ArrayList<String> items, String item, String filename) {
		if (item == null || item.length() == 0) {
			return;
		}
		item = item.replace("\n", " ");
		
		if (items != null) {
			items.clear();
		}
		ArrayList<String> oriItems = new ArrayList<String>();
		
		FileInputStream instr = null;
		InputStreamReader reader = null;
		BufferedReader buffer = null;
		try {
			instr = context.openFileInput(filename);
			reader = new InputStreamReader(instr, "utf-8");
			buffer = new BufferedReader(reader);
			String line;
			while (null != (line = buffer.readLine())) {
				boolean isExist = false;
				for (String str : oriItems) {
					if (str != null && str.equals(line)) {
						isExist = true;
						break;
					}
				}
				if (!isExist && !line.equals(item)) {
					oriItems.add(line); 
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (buffer != null) {
				try {
					buffer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (instr != null) {
				try {
					instr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		
		
		FileOutputStream outstr = null;
		OutputStreamWriter writer = null;
		BufferedWriter buffer2 = null;
		try {
			outstr = context.openFileOutput(filename, Context.MODE_PRIVATE);
			writer = new OutputStreamWriter(outstr, "utf-8");
			buffer2 = new BufferedWriter(writer);

			for (int i = 0; i < oriItems.size(); i++) {
				String item2 = oriItems.get(i);
				if (item != null) {
					buffer2.write(item2 + "\n");
					buffer2.flush();
					if (items != null) {
						items.add(item2);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (buffer2 != null) {
				try {
					buffer2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outstr != null) {
				try {
					outstr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public static void readItems(Context context, ArrayList<String> items, String filename) {
		items.clear();
		FileInputStream instr = null;
		InputStreamReader reader = null;
		BufferedReader buffer = null;
		try {
			instr = context.openFileInput(filename);
			reader = new InputStreamReader(instr, "utf-8");
			buffer = new BufferedReader(reader);
			String line;
			while (null != (line = buffer.readLine())) {
				items.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (buffer != null) {
				try {
					buffer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (instr != null) {
				try {
					instr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private final static class HistoryItemsAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private List<String> items;
		
		public HistoryItemsAdapter(Context context, List<String> items) {
			this.mInflater = LayoutInflater.from(context);
			this.items = items;
		}
		
		@Override
		public int getCount() {
			if (items != null) {
				return items.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
           ViewHolder holder;
            if (convertView == null) {        
				convertView = mInflater.inflate(R.layout.list_item, null);
				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.title);
			
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (items != null && position >= 0 && position < items.size()) {
            	holder.title.setText(items.get(position));
            } else {
            	holder.title.setText("");
            }
            return convertView;
		}
		
        private static final class ViewHolder {
        	TextView title;
        }
	}
}
