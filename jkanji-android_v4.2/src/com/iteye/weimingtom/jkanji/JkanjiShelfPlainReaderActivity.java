package com.iteye.weimingtom.jkanji;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import fi.harism.curl.BookInfoUtils;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class JkanjiShelfPlainReaderActivity extends Activity {	
	private final static boolean D = false;
	private final static String TAG = "JkanjiShelfPlainReaderActivity";

	public final static String EXTRA_ID = "com.iteye.weimingtom.jkanji.JkanjiShelfPlainReaderActivity.EXTRA_ID";
	public final static String EXTRA_PLAIN_FILE_NAME = "com.iteye.weimingtom.jkanji.JkanjiShelfPlainReaderActivity.EXTRA_PLAIN_FILE_NAME";
	public final static String EXTRA_PLAIN_CHAR_POS = "com.iteye.weimingtom.jkanji.JkanjiShelfPlainReaderActivity.EXTRA_PLAIN_CHAR_POS";
	public final static String EXTRA_PLAIN_ENCODING = "com.iteye.weimingtom.jkanji.JkanjiShelfPlainReaderActivity.EXTRA_PLAIN_ENCODING";
	public final static String EXTRA_ALWAYS_SAVE = "com.iteye.weimingtom.jkanji.JkanjiShelfPlainReaderActivity.EXTRA_ALWAYS_SAVE";
	
	private final static String DEFAULT_ENCODING = "shift-jis";
	private final static int PROGRESS_MAX_LENGTH = 300 * 1024;
	
	private final static String KEY_CURR_ID = "currId";
	private long currId = -1L;
	private String currFile = "";
	private String currEncoding = DEFAULT_ENCODING;
	private int currCharPos = 0;
	private int currCharLength = 0;
	private int currParserType = JkanjiShelfHistoryItem.PLAIN_FORMAT_DEFAULT;

	private ActionBar actionBar;
	//private ArrayAdapter<TextChunk> listAdapter;
	private ArrayList<TextChunk> listChunks;
	private TextAdapter listChunkAdapter;
	
	private LoadFileTask loadFileTask;

	private ListView lvText;
	private boolean isLoaded = false;
	
	private LinearLayout linearLayoutHeader;
	private Spinner spinnerEncoding;
	private ArrayAdapter<String> spinnerEncodingAdapter;
	private Button buttonCancelSave;
	//private Button buttonReopen;
	
	private volatile boolean alwaysSave = false;
	private volatile boolean cancelSave = false;
	private volatile boolean dataLoadFinish = false;
	
	private Object lockLoadFile = new Object();
	
	private ScrollView scrollViewProgress;
	private ProgressBar progressBarLoading;
	private boolean isFirstLoad = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.shelf_plain_reader);
        
        scrollViewProgress = (ScrollView) this.findViewById(R.id.scrollViewProgress);
        progressBarLoading = (ProgressBar) this.findViewById(R.id.progressBarLoading);
        
    	actionBar = (ActionBar) this.findViewById(R.id.actionbar);
		actionBar.setTitle("简单查看器");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.shelf_file;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.config;
			}

			@Override
			public void performAction(View view) {
				if (linearLayoutHeader.getVisibility() == View.GONE) {
					linearLayoutHeader.setVisibility(View.VISIBLE);
				} else {
					linearLayoutHeader.setVisibility(View.GONE);
				}
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        ///inflater = LayoutInflater.from(this); 
        lvText = (ListView) findViewById(R.id.lvText);
        /*
        listAdapter = new ArrayAdapter<TextChunk>(this, R.layout.shelf_plain_reader_item) {
        	@Override
    		public View getView(int position, View convertView, ViewGroup parent) {
        		if (convertView == null) {
        			convertView = inflater.inflate(R.layout.shelf_plain_reader_item, null);
        		}
        		TextChunk tc = getItem(position);
        		TextView textViewLine = (TextView)convertView.findViewById(R.id.textViewLine);
        		textViewLine.setText(tc.getText());
        		convertView.requestLayout();
        		return convertView;
        	}
		};
		*/
        listChunks = new ArrayList<TextChunk>();
        listChunkAdapter = new TextAdapter(this, listChunks);
        lvText.setAdapter(listChunkAdapter);
        //listAdapter = new TextAdapter(this, R.layout.shelf_plain_reader_item);
        //lvText.setAdapter(listAdapter);
		lvText.setFastScrollEnabled(true);
		lvText.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position >= 0 && position < listChunks.size()) {
					TextChunk chunk = listChunks.get(position);
					if (chunk != null) {
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_SEND);
						intent.setType("text/plain");
			            intent.putExtra(Intent.EXTRA_TEXT, chunk.getText());
			            try {
							startActivity(intent);
						} catch (Throwable e) {
							e.printStackTrace();
							Toast.makeText(JkanjiShelfPlainReaderActivity.this, 
								"共享方式出错", Toast.LENGTH_SHORT)
								.show();
						}
					}
				}

			}
		});
		
		linearLayoutHeader = (LinearLayout) this.findViewById(R.id.linearLayoutHeader);
		spinnerEncoding = (Spinner) this.findViewById(R.id.spinnerEncoding);
		buttonCancelSave = (Button) this.findViewById(R.id.buttonCancelSave);
		//buttonReopen = (Button) this.findViewById(R.id.buttonReopen);
		
		spinnerEncoding.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				reopen();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
    	spinnerEncodingAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_spinner_item);
    	spinnerEncodingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spinnerEncodingAdapter.add("shift-jis");
    	spinnerEncodingAdapter.add("utf8");
    	spinnerEncodingAdapter.add("gbk");
    	spinnerEncodingAdapter.add("unicode");
    	spinnerEncoding.setAdapter(spinnerEncodingAdapter);
    	
    	/*
    	buttonReopen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				reopen();
			}
    	});
    	*/
    	
    	buttonCancelSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cancelSave = true;
				finish();
			}
    	});
		Intent intent = getIntent();
		if (intent != null) {
			String filename = intent.getStringExtra(EXTRA_PLAIN_FILE_NAME);
			if (filename != null) {
				currFile = filename;
			}
			currId = intent.getLongExtra(EXTRA_ID, -1L);
			currCharPos = intent.getIntExtra(EXTRA_PLAIN_CHAR_POS, 0);
			currEncoding = intent.getStringExtra(EXTRA_PLAIN_ENCODING);
			alwaysSave = intent.getBooleanExtra(EXTRA_ALWAYS_SAVE, false);
			if (alwaysSave) {
				buttonCancelSave.setEnabled(false);
				buttonCancelSave.setText("已添加");
			}
		}
		if (currEncoding == null) {
			currEncoding = DEFAULT_ENCODING;
		}
		if (D) {
			Log.d(TAG, "reader encoding == " + currEncoding);
		}
		if (savedInstanceState != null) {
			/**
			 * NOTE: savedInstanceState is prior to getIntent()
			 */
			currId = savedInstanceState.getLong(KEY_CURR_ID, -1L);
		}
	}
	
	private void reopen() {
		if (true /*dataLoadFinish*/) {
			JkanjiShelfHistoryItem item = new JkanjiShelfHistoryItem();
			item.setId(currId);
			item.setPlainFileName(currFile);
			int position = lvText.getFirstVisiblePosition();
			if (position >= 0 && position < listChunks.size()) {
				TextChunk chunk = listChunks.get(position);
				item.setPlainCharPos(chunk.getStartPos()); //currCharPos
			} else {
				item.setPlainCharPos(0);
			}
			item.setPlainCharLength(currCharLength);
			int pos = spinnerEncoding.getSelectedItemPosition();
			if (pos < 0 || pos >= spinnerEncodingAdapter.getCount()) {
				pos = 0;
			}
			currEncoding = spinnerEncodingAdapter.getItem(pos);
			item.setPlainEncoding(currEncoding);
			item.setParserType(currParserType);
			JkanjiShelfHistoryDataSource dataSrc = new JkanjiShelfHistoryDataSource(JkanjiShelfPlainReaderActivity.this);
			dataSrc.open();
			currId = dataSrc.createItem(item);
			dataSrc.close();
			loadFile(currFile, currEncoding);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		loadHistory();
		
		if (currFile != null && currFile.toLowerCase().endsWith(".epub")) {
			//linearLayoutHeader.setVisibility(LinearLayout.GONE);
			spinnerEncoding.setEnabled(false);
			spinnerEncodingAdapter.clear();
		} else {
			if (currEncoding != null) {
				if (currEncoding.equals("shift-jis")) {
					spinnerEncoding.setSelection(0);
				} else if (currEncoding.equals("utf8")) {
					spinnerEncoding.setSelection(1);
				} else if (currEncoding.equals("gbk")) {
					spinnerEncoding.setSelection(2);
				} else if (currEncoding.equals("unicode")) {
					spinnerEncoding.setSelection(3);
				} else {
					//linearLayoutHeader.setVisibility(LinearLayout.GONE);
					spinnerEncoding.setEnabled(false);
					spinnerEncodingAdapter.clear();
				}
			} else {
				//linearLayoutHeader.setVisibility(LinearLayout.GONE);
				spinnerEncoding.setEnabled(false);
				spinnerEncodingAdapter.clear();
			}
		}
		if (!spinnerEncoding.isEnabled() && isLoaded == false) {
			isLoaded = true;
			loadFile(currFile, currEncoding);
		}
	}
	
	private void loadHistory() {
		JkanjiShelfHistoryDataSource dataSrc = new JkanjiShelfHistoryDataSource(this);
		dataSrc.open();
		JkanjiShelfHistoryItem item = dataSrc.getItemById(currId);
		if (item != null) {
			currFile = item.getPlainFileName();
			currCharPos = item.getPlainCharPos();
			currCharLength = item.getPlainCharLength();
			currEncoding = item.getPlainEncoding();
			currParserType = item.getParserType();
		} else {
			currCharPos = 0;
		}
		dataSrc.close();
	}
	
	private void updataCurrCharPos() {
		if (!isFirstLoad && this.lvText.getVisibility() == View.VISIBLE) {
			int position = lvText.getFirstVisiblePosition();
			if (position >= 0 && position < listChunks.size()) {
				TextChunk chunk = listChunks.get(position);
				currCharPos = chunk.getStartPos();
			}
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		JkanjiShelfHistoryItem item = new JkanjiShelfHistoryItem();
		item.setId(currId);
		item.setPlainFileName(currFile);
		updataCurrCharPos();
		item.setPlainCharPos(currCharPos);
		item.setPlainCharLength(currCharLength);
		item.setPlainEncoding(this.currEncoding);
		item.setParserType(this.currParserType);
		JkanjiShelfHistoryDataSource dataSrc = new JkanjiShelfHistoryDataSource(this);
		dataSrc.open();
		if (cancelSave && !alwaysSave) {
			dataSrc.deleteItem(item);
		} else {
			currId = dataSrc.createItem(item);
		}
		dataSrc.close();	
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (outState != null) {
			outState.putLong(KEY_CURR_ID, currId);
		}
	}

	private boolean loadFile(String fileName, String encoding) {
		synchronized (lockLoadFile) {
			if (D) {
				Log.e(TAG, "loadFile");
			}
			File f = new File(fileName);
			if (!f.exists() || !f.isFile() || !f.canRead()) {
				return false;
			} else {
				if (loadFileTask == null) {
					listChunks.clear();
					loadFileTask = new LoadFileTask(encoding);
					loadFileTask.execute(f);
				} else {
					//Toast.makeText(this, "正在加载中", Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		}
	}
	
	private final class LoadFileTask extends AsyncTask<File, TextChunk, Integer> {
		private String encoding;

		public LoadFileTask(String encoding) {
			this.encoding = encoding;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			updataCurrCharPos();
			isFirstLoad = false;
			lvText.setVisibility(View.INVISIBLE);
			scrollViewProgress.setVisibility(View.VISIBLE);
			progressBarLoading.setIndeterminate(true);
			if (currCharLength > 0 && currCharLength < PROGRESS_MAX_LENGTH) {
				progressBarLoading.setMax(currCharLength);
				progressBarLoading.setProgress(0);
			}
		}

		@Override
		protected Integer doInBackground(File... params) {
			if (false) {
				return loadData2(params[0]);
			} else {
				return loadData(params[0]);
			}
		}
		
		private int loadData2(File file) {
			int pos = 0;
			FileInputStream fis = null;
			InputStreamReader reader = null;
			BufferedReader rbuf = null;
			try {
				fis = new FileInputStream(file);
				reader = new InputStreamReader(fis, encoding);
				rbuf = new BufferedReader(reader);
				String line;
				while (true) {
					line = rbuf.readLine();
					if (line != null) {
						TextChunk[] chunks = new TextChunk[1];
						chunks[0] = new TextChunk(pos, line);
						pos += line.length();
						this.publishProgress(chunks);
					} else {
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			} finally {
				if (rbuf != null) {
					try {
						rbuf.close();
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
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return (int)file.length();
		}

		public int loadData(File file) {
			int pos = 0;
			FileInputStream inputStream = null;
			try {
				if (file.getName().toLowerCase().endsWith(".epub")) {
					pos = openEpub(file);
				} else {
					inputStream = new FileInputStream(file);
					pos = openInputStream(inputStream, "\n", encoding);
				}
			} catch (IOException e) {
				e.printStackTrace();
				pos = 0;
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return pos; 
		}
		
		public int openInputStream(InputStream istr, String breakLine, String codePage) {
			String str;
			if (currParserType == JkanjiShelfHistoryItem.PLAIN_FORMAT_DEFAULT) {
				AozoraParser parser = new AozoraParser();
				parser.openInputStream(istr, "\n", codePage);
				str = parser.getLoadedText();
			} else {
				AozoraParser parser = new AozoraParser();
	        	parser.openInputStream(istr, "\n", codePage);
	        	parser.parseRuby();
				str = parser.getOutputString();
			}
			int pos = 0;
			String line;
			StringReader sr = null;
			BufferedReader rbuf = null;
			try {
				sr = new StringReader(str);
				rbuf = new BufferedReader(sr);
				final int breakLineLength = breakLine.length();
				while (null != (line = rbuf.readLine())) {
					TextChunk[] chunks = new TextChunk[1];
					chunks[0] = new TextChunk(pos, line);
					pos += line.length() + breakLineLength;
					this.publishProgress(chunks);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (rbuf != null) {
					try {
						rbuf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (sr != null) {
					sr.close();
				}
			}
			return pos;
		}
		
		private int openEpub(File file) {
            int pos = 0;
	        try {
	            InputStream is = new FileInputStream(file);  
	            Book book = new EpubReader().readEpub(is);
	            StringBuffer sb = new StringBuffer();
	            for (Resource res : book.getContents()) {
	            	sb.setLength(0); //FIXME:
	            	InputStreamReader ir = null;
	            	BufferedReader r = null;
	            	try {
	                	InputStream istr = res.getInputStream();
	                	String textEncoding = res.getInputEncoding();
	                	if (textEncoding != null) {
	                		currEncoding = textEncoding;
		                	ir = new InputStreamReader(istr, textEncoding);
		            		r = new BufferedReader(ir);
			                String line;
			                while ((line = r.readLine()) != null) {
			                    sb.append(line);
			                }
			                String inputHTML = sb.toString();
			                Spanned spanned = Html.fromHtml(inputHTML);
			                if (spanned != null) {
			                	String str = spanned.toString();
			                	if (currParserType == JkanjiShelfHistoryItem.PLAIN_FORMAT_AOZORA) {
			                		AozoraParser parser = new AozoraParser();
									parser.openText(str);
									parser.parseRuby();
									str = parser.getOutputString();
			                	}
								pos = loadString(pos, str, "\n", textEncoding);
			                }
	                	}
		            } catch (IOException e) {
		            	e.printStackTrace();
					} finally {
						if (r != null) {
							r.close();
						}
						if (ir != null) {
							ir.close();
						}
					}
	            }
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        }
	        return pos;
		}
		
		private int loadString(int pos, String inputStr, String breakLine, String encoding) {
			InputStream istr = null;
			InputStreamReader reader = null;
			BufferedReader rbuf = null;
			try {
				istr = new ByteArrayInputStream(inputStr.getBytes(encoding));
				reader = new InputStreamReader(istr, encoding);
				rbuf = new BufferedReader(reader);
				String line;
				final int breakLineLength = breakLine.length();
				while (null != (line = rbuf.readLine())) {
					
					TextChunk[] chunks = new TextChunk[1];
					chunks[0] = new TextChunk(pos, line);
					pos += line.length() + breakLineLength;
					this.publishProgress(chunks);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (rbuf != null) {
					try {
						rbuf.close();
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
				if (istr != null) {
					try {
						istr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return pos;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			lvText.setVisibility(View.VISIBLE);
			scrollViewProgress.setVisibility(View.INVISIBLE);
			if (!isFinishing()) {
				currCharLength = result;
				listChunkAdapter.notifyDataSetChanged();
				for (int i = 0; i < listChunks.size(); ++i) {
					TextChunk chunk = listChunks.get(i);
					if (chunk.getStartPos() >= currCharPos) {
						lvText.setSelection(i);
						break;
					}
				}
			}
			dataLoadFinish = true;
			loadFileTask = null;
		}

		@Override
		protected void onProgressUpdate(TextChunk... values) {
			if (!isFinishing() && values != null && values.length >= 1) {
				TextChunk chunk = values[0];
				listChunks.add(chunk);
				if (currCharLength > 0 && currCharLength < PROGRESS_MAX_LENGTH) {
					progressBarLoading.setIndeterminate(false);
					progressBarLoading.setProgress(chunk.getEndPos());
				}
			}
		}
	}
	
	private final static class ViewHolder {
		TextView textViewLine;
	}
	
	private final static class TextAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private ArrayList<TextChunk> chunks;
		
		public TextAdapter(Context context, ArrayList<TextChunk> chunks) {
			this.inflater = LayoutInflater.from(context);
			this.chunks = chunks;
		}
		
		@Override
		public int getCount() {
			if (chunks == null) {
				return 0;
			}
			return chunks.size();
		}

		@Override
		public Object getItem(int position) {
			if (chunks == null) {
				return null;
			}
			if (position < 0 || position >= getCount()){
				return null;
			}
			return chunks.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.shelf_plain_reader_item, null);
				holder = new ViewHolder();
				holder.textViewLine = (TextView)convertView.findViewById(R.id.tvLine);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			if (chunks != null && position >= 0 && position < chunks.size()) {
				TextChunk chunk = chunks.get(position);
				if (chunk != null) {
					holder.textViewLine.setText(chunk.getText());
				}
			} else {
				holder.textViewLine.setText("");
			}
			return convertView;
		}
	}
	
	private final static class TextChunk {
		private int startPos;
		private String text;
		
		public TextChunk(int startPos, String text) {
			this.startPos = startPos;
			this.text = text;
		}

		public int getStartPos() {
			return startPos;
		}

		public int getEndPos() {
			if (this.text != null) {
				return startPos + this.text.length();
			} else {
				return startPos;
			}
		}
		
		public String getText() {
			return text;
		}
		
		@Override
		public String toString() {
			return text;
		}
	}
}
