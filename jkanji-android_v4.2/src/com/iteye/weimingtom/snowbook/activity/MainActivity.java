package com.iteye.weimingtom.snowbook.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;

import com.iteye.weimingtom.jkanji.JkanjiSettingActivity;
import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.snowbook.demo.DemoMainMenuFragment;
import com.iteye.weimingtom.snowbook.demo.TabPageFragment;
import com.iteye.weimingtom.snowbook.fragment.FindListFragment;
import com.iteye.weimingtom.snowbook.fragment.MainMenuFragment;
import com.iteye.weimingtom.snowbook.fragment.OldVersionFragment;
import com.iteye.weimingtom.snowbook.fragment.WelcomeFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenListener;

public class MainActivity extends BaseActivity implements OnOpenListener {
	private static final boolean D = false;
	private static final String TAG = "MainActivity";
	private static final boolean USE_DEMO_MENU = false;
	
	private static final String KEY_POSITION = "MainActivity.KEY_POSITION";
	
	private final static boolean SHOW_OPTIONS_MENU = false;
	private final static boolean SHOW_OPTIONS_SEARCH_MENU = true;
	
	private static final int ITEM_ID_MEMO = Menu.FIRST;
	private static final int ITEM_ID_GALLERY = Menu.FIRST + 1;
	private static final int ITEM_ID_BOOK = Menu.FIRST + 2;
	
	private Fragment mContent;
	private Fragment mMenu;
	private int mPosition;
    
	/*
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.creader_main);
        FragmentPagerAdapter adapter = new MainFragmentAdapter(getSupportFragmentManager());
        ViewPager pager = (ViewPager)findViewById(R.id.pager_main);
        pager.setAdapter(adapter);
	}
	*/
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		//requestSupportWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

		//this.setSupportProgressBarIndeterminate(true);
		//this.setSupportProgressBarIndeterminateVisibility(false);
		
		// set the Above View
		if (savedInstanceState != null) {
			mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
		    mPosition = savedInstanceState.getInt(KEY_POSITION, 0);
			if (D) {
				Log.e(TAG, "mContent " + mContent.getClass().getName());
			}
	    }
		
		// set the Above View
		setContentView(R.layout.snowbook_content_frame);
		
		// set the Behind View
		setBehindContentView(R.layout.snowbook_menu_frame);
		if (USE_DEMO_MENU) {
			mMenu = new DemoMainMenuFragment();
			getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.menu_frame, mMenu)
				.commit();
			
		} else {
			mMenu = new MainMenuFragment();
			getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.menu_frame, mMenu)
				.commit();
		}
		
		// customize the SlidingMenu
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		//getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		getSlidingMenu().setOnOpenListener(this);
		
		if (mContent == null) {
			//mContent = new ColorFragment(R.color.red);
			if (USE_DEMO_MENU) {
				this.setPosition(0);
				switchContent(new TabPageFragment());
			} else {
				//mContent = new ExpandListFragment();
				if (!JkanjiSettingActivity.getShowSplashScreen(this)) {
					if (!JkanjiSettingActivity.getJumpOldVersion(this)) {
						this.setPosition(MainMenuFragment.MENU_INDEX_NEW_VERSION);
						//switchContent(new ExpandListFragment());
						switchContent(new FindListFragment());
					} else {
						this.setPosition(MainMenuFragment.MENU_INDEX_OLD_VERSION);
						switchContent(new OldVersionFragment());
					}
				} else {
					this.setPosition(0);
					switchContent(new WelcomeFragment());
				}
			}
		}
	}
	
	public Fragment getMenuFragment() {
		return this.mMenu;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, "mContent", mContent);
		if (outState != null) {
			outState.putInt(KEY_POSITION, mPosition);
		}
	}
	
	public int getPosition() {
		return mPosition;
	}
	
	public void setPosition(int position) {
		this.mPosition = position;
	}
	
	public void switchContent(Fragment fragment) {
		mContent = fragment;
		getSupportFragmentManager()
			.beginTransaction()
			.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
			.replace(R.id.content_frame, fragment)
			.commit();
		getSlidingMenu().showContent();
		//this.invalidateOptionsMenu();
	}

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
////        menu.add("选项")
////            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
//        if (SHOW_OPTIONS_MENU) {
//	    	SubMenu subMenu1 = menu.addSubMenu("Action Item");
//	        subMenu1.add("Sample");
//	        subMenu1.add("Menu");
//	        subMenu1.add("Items");
//	        
//	        MenuItem subMenu1Item = subMenu1.getItem();
//	        subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
//	
//	        SubMenu subMenu2 = menu.addSubMenu("Overflow Item");
//	        subMenu2.add("These");
//	        subMenu2.add("Are");
//	        subMenu2.add("Sample");
//	        subMenu2.add("Items");
//	
//	        MenuItem subMenu2Item = subMenu2.getItem();
//	        subMenu2Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
//	    }
//        if (SHOW_OPTIONS_SEARCH_MENU) {
//        	if (this.mContent instanceof ExpandListFragment) {
//		        //Create the search view
//		        SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());
//		        searchView.setQueryHint("请输入罗马音");
//		        searchView.setOnQueryTextListener(new OnQueryTextListener() {
//		        	@Override
//					public boolean onQueryTextSubmit(String query) {
//						if (mContent != null && mContent instanceof ExpandListFragment) {
//							((ExpandListFragment)mContent).onQueryTextSubmit(query);
//							return true;
//						}
//		        		return false;
//					}
//	
//					@Override
//					public boolean onQueryTextChange(String newText) {
//						if (mContent != null && mContent instanceof ExpandListFragment) {
//							((ExpandListFragment)mContent).onQueryTextChange(newText);
//							return true;
//						}					
//						return false;
//					}
//		        });
//		        menu.add("搜索")
//		            //.setIcon(R.drawable.abs__ic_search)
//		            .setIcon(R.drawable.search)
//		        	.setActionView(searchView)
//		            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
//	        } else if (this.mContent instanceof OldVersionFragment) {
//	        	menu.add(0, ITEM_ID_MEMO, 0, "备忘录")
//	        		.setIcon(R.drawable.memo)
//	                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS/*MenuItem.SHOW_AS_ACTION_IF_ROOM*/);
//	        	menu.add(0, ITEM_ID_GALLERY, 0, "图库")
//	        		.setIcon(R.drawable.gallery)
//	                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS/*MenuItem.SHOW_AS_ACTION_IF_ROOM*/);
//	        	menu.add(0, ITEM_ID_BOOK, 0, "书架")
//	        		.setIcon(R.drawable.book)
//	                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS/*MenuItem.SHOW_AS_ACTION_IF_ROOM*/);
//		    }
//        }
//        return true;
//    }
    
//    @Override 
//    public boolean onOptionsItemSelected(MenuItem item) {
//    	switch (item.getItemId()) {
//    	case ITEM_ID_MEMO:
//			startActivity(new Intent(this, 
//					ShareToClipboardActivity.class));
//    		break;
//    		
//    	case ITEM_ID_GALLERY:
//			startActivity(new Intent(this, 
//					JkanjiGalleryHistoryActivity.class));
//    		break;
//    		
//    	case ITEM_ID_BOOK:
//			startActivity(new Intent(this,
//					JkanjiShelfHistoryActivity.class));
//    		break;
//    	}
//		return super.onOptionsItemSelected(item);
//    }
    
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    showRecycleDebug();
	}

	@Override
	public void onOpen() {
//		if (mContent != null && mContent instanceof ExpandListFragment) {
//			((ExpandListFragment)mContent).onSlidingMenuOpen();
//		}
		if (mContent != null && mContent instanceof FindListFragment) {
			((FindListFragment)mContent).onSlidingMenuOpen();
		}
	}
}
