package com.iteye.weimingtom.snowbook.fragment;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.fontawesome.example.TextAwesome;
import com.iteye.weimingtom.jkanji.JkanjiSettingActivity;
import com.iteye.weimingtom.jkanji.MersenneTwisterRandom;
import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.snowbook.activity.BaseActivity;
import com.iteye.weimingtom.snowbook.activity.MainActivity;
import com.iteye.weimingtom.snowbook.util.BitmapDrawableLruCache;
import com.iteye.weimingtom.snowbook.widget.RecyclingImageView;

public class WelcomeFragment extends Fragment {
	private Context mContext;
	private BitmapDrawableLruCache mMemoryCache;
	
	private Button btnNewVersion;
	private Button btnOldVersion;
	private Button btnCheckShow;
	private TextAwesome textAwesomeCheck;
	
	private RecyclingImageView ivBG, ivChar, welcome_dlg, ivFace;
	
	private static MersenneTwisterRandom mt = null;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
    	return inflater.inflate(R.layout.snowbook_welcome_fragment, null);
    }
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		this.mContext = getActivity();
		this.mMemoryCache = ((BaseActivity)getActivity()).getBitmapDrawableLruCache();
		
		btnNewVersion = (Button) view.findViewById(R.id.btnNewVersion);
		btnOldVersion = (Button) view.findViewById(R.id.btnOldVersion);
		btnCheckShow = (Button) view.findViewById(R.id.btnCheckShow);
		textAwesomeCheck = (TextAwesome) view.findViewById(R.id.textAwesomeCheck);
		
		final int[] charIds = new int[]{
				R.drawable.snowbook_character_ako,
				R.drawable.snowbook_character_bko,
				R.drawable.snowbook_character_cko,
		};
		final int[] faceIds = new int[]{
				R.drawable.snowbook_face_ako,
				R.drawable.snowbook_face_bko,
				R.drawable.snowbook_face_cko,
		};
		if (mt == null) {
			mt = new MersenneTwisterRandom();
			mt.init_genrand((int)System.currentTimeMillis());
		}
		int total = charIds.length;
		int indexId = mt.nextInt(0, total - 1);
		int charId = charIds[0];
		int faceId = faceIds[0];
		if (indexId >= 0 && indexId < charIds.length) {
			charId = charIds[indexId];
			faceId = faceIds[indexId];
		}
		
		ivBG = (RecyclingImageView) view.findViewById(R.id.ivBG);
		BitmapDrawable drawable = this.mMemoryCache.getDrawable(
				mContext.getResources(),
				R.drawable.snowbook_nc70938);
		ivBG.setImageDrawable(drawable);

		ivChar = (RecyclingImageView) view.findViewById(R.id.ivChar);
		drawable = this.mMemoryCache.getDrawable(
				mContext.getResources(),
				charId);
		ivChar.setImageDrawable(drawable);
		
		welcome_dlg = (RecyclingImageView) view.findViewById(R.id.welcome_dlg);
		drawable = this.mMemoryCache.getDrawable(
				mContext.getResources(),
				R.drawable.snowbook_nc73313);
		welcome_dlg.setImageDrawable(drawable);
		
		ivFace = (RecyclingImageView) view.findViewById(R.id.ivFace);
		drawable = this.mMemoryCache.getDrawable(
				mContext.getResources(),
				faceId);
		ivFace.setImageDrawable(drawable);
		
		if (!JkanjiSettingActivity.getShowSplashScreen(getActivity())) {
			textAwesomeCheck.setText(R.string.fa_check_square);
		} else {
			textAwesomeCheck.setText(R.string.fa_square);
		}
		
		btnNewVersion.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!JkanjiSettingActivity.getShowSplashScreen(getActivity())) {
					JkanjiSettingActivity.setJumpOldVersion(
							getActivity(), false);
				}
				MainActivity activity = ((MainActivity)getActivity());
				Fragment menuFragment = activity.getMenuFragment();
				if (menuFragment instanceof MainMenuFragment) {
					((MainMenuFragment)menuFragment).selectMenu(MainMenuFragment.MENU_INDEX_NEW_VERSION);
				}
			}
		});
		btnOldVersion.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!JkanjiSettingActivity.getShowSplashScreen(getActivity())) {
					JkanjiSettingActivity.setJumpOldVersion(
						getActivity(), true);
				}
				MainActivity activity = ((MainActivity)getActivity());
				Fragment menuFragment = activity.getMenuFragment();
				if (menuFragment instanceof MainMenuFragment) {
					((MainMenuFragment)menuFragment).selectMenu(MainMenuFragment.MENU_INDEX_OLD_VERSION);
				}
			}
		});
		btnCheckShow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (textAwesomeCheck.getText().toString().equals(getString(R.string.fa_square))) {
					textAwesomeCheck.setText(R.string.fa_check_square);
					JkanjiSettingActivity.setShowSplashScreen(
							getActivity(), false);
				} else {
					textAwesomeCheck.setText(R.string.fa_square);
					JkanjiSettingActivity.setShowSplashScreen(
							getActivity(), true);
				}
			}
		});
	}

}
