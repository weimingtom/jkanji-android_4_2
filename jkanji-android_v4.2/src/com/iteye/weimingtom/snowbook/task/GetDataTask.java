package com.iteye.weimingtom.snowbook.task;

import android.os.AsyncTask;

public class GetDataTask extends AsyncTask<Integer, Void, String[]> {
	@Override
	protected String[] doInBackground(Integer... params) {
		String[] temp_max_chapter = new String[2];
		return temp_max_chapter;
	}

	@Override
	protected void onPostExecute(String[] data) {
		super.onPostExecute(data);
	}
}
