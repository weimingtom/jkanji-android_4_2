package com.iteye.weimingtom.snowbook.demo;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.snowbook.adapter.BookGridAdapter;
import com.iteye.weimingtom.snowbook.pojo.BookInfo;

public class GridFragment extends Fragment {
	private GridView gridview;
	private BookGridAdapter bookGridAdapter;
	
	private List<BookInfo> bookInfoList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	final View layout = inflater.inflate(R.layout.creader_bookgridview, container, false);
    	
		gridview = (GridView) layout.findViewById(R.id.bookgridview);
		
		gridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridview.setBackgroundColor(Color.WHITE);
		bookInfoList = new ArrayList<BookInfo>();
		bookGridAdapter = new BookGridAdapter(this.getActivity(), bookInfoList);
		gridview.setAdapter(bookGridAdapter);
		gridview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				int bookType = bookInfoList.get(position).getBookType();
				switch (bookType) {
				case BookInfo.BOOK_TYPE_SPACE:
					break;
					
				case BookInfo.BOOK_TYPE_DEFAULT:
				default:
					break;
				}
			}
		});
		gridview.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				return false;
			}
		});
		
		updateBookInfoList();
        return layout;
    }
	
	private void updateBookInfoList() {
		bookInfoList.clear();
		for (int i = 0; i < 10; i++) {
			BookInfo book = new BookInfo();
			book.setBookName("测试多个文件名");
			bookInfoList.add(book);
		}
		bookGridAdapter.notifyDataSetChanged();
	}
}
