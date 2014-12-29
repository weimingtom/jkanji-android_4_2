package com.iteye.weimingtom.snowbook.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.iteye.weimingtom.snowbook.activity.BaseActivity;
import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.snowbook.activity.MainActivity;
import com.iteye.weimingtom.snowbook.adapter.BannerItemAdapter;
import com.iteye.weimingtom.snowbook.adapter.ListItemAdapter;
import com.iteye.weimingtom.snowbook.pojo.ListItemModel;

public class MainMenuFragment extends Fragment implements OnItemClickListener {
	public static final int MENU_INDEX_NEW_VERSION = 1;
	public static final int MENU_INDEX_OLD_VERSION = 2;
	
	private ListView mListView;
	private Context mContext;
	//private ListItemAdapter mAdapter;
	private BannerItemAdapter mAdapter;
	private List<ListItemModel> models;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
    	return inflater.inflate(R.layout.snowbook_list_menu, null);
    }
    
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mListView = (ListView) view.findViewById(R.id.listViewMenu);
		
		mContext = this.getActivity();
		models = new ArrayList<ListItemModel>();
		models.add(new ListItemModel(mContext, "欢迎屏幕", "选择要跳转的界面", /*"snowbook/icon/asumi_1.jpg"*/"erica/title_down.png"));
		models.add(new ListItemModel(mContext, "N5单词", "新日本语能力测试词汇必备", /*"snowbook/icon/asumi_2.jpg"*/"erica/default_down.png"));
      	models.add(new ListItemModel(mContext, "旧版本主菜单", "旧版本主菜单", /*"snowbook/icon/asumi_3.jpg"*/"erica/back_down.png"));
      	models.add(new ListItemModel(mContext, "退出", "退出程序", /*"snowbook/icon/asumi_4.jpg"*/"erica/exit_down.png"));
		mAdapter = new BannerItemAdapter(mContext, models, ((BaseActivity)getActivity()).getBitmapDrawableLruCache());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
	}

	/**
	 * @see R.array.fragment_names
	 */
	@Override
	public void onItemClick(AdapterView<?> lv, View v, int position, long id) {		
		selectMenu(position);
	}
	
	public void selectMenu(int position) {
		if (getActivity() instanceof MainActivity) {
			MainActivity fca = (MainActivity) getActivity();
			int curPosition = fca.getPosition();
			if (curPosition == position) {
				((MainActivity)this.getActivity())
					.getSlidingMenu().showContent();
				return;
			}
			fca.setPosition(position);
		}
		Fragment newContent = null;
		switch (position) {
		case 0:
			newContent = new WelcomeFragment();
			break;
			
		case 1:
			//newContent = new ExpandListFragment();
			newContent = new FindListFragment();
			break;
		
		case 2:
			newContent = new OldVersionFragment();
			break;
			
		case 3:
			this.getActivity().finish();
			return;
		}
		if (newContent != null) {
			switchFragment(newContent);
		}		
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
