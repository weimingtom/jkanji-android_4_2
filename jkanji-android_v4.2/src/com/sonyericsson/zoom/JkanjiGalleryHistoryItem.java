package com.sonyericsson.zoom;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.Time;
import android.util.Log;

public class JkanjiGalleryHistoryItem {
	private final static boolean D = false;
	private final static String TAG = "JkanjiGalleryHistoryItem";
	
	private long id = -1L;
	private String content;

	private String plainFileName;
	private String plainPathName;
	private int plainTotalPage;
	private int plainPage;
	private Time plainTime;
	private float plainZoom;
	private float plainPanX;
	private float plainPanY;
	private boolean plainEnableMulti;
	private String plainDesc;

	public final static int PLAIN_FORMAT_DEFAULT = 0;
	public final static int PLAIN_FORMAT_AOZORA = 1;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return getContent();
	}

	public String toHistoryDesc() {
		StringBuffer sb = new StringBuffer();
		String strProgress = "索引：---";
		int page = getPlainPage();
		int totalPage = getPlainTotalPage();
		if (totalPage != 0) {
			strProgress = String.format("进度: %d%%", page * 100 / totalPage);
		}
		sb.append(strProgress);
		sb.append('\n');

		sb.append("文件名：");
		if (getPlainFileName() != null) {
			sb.append(getPlainFileName());
		} else {
			sb.append("---");
		}
		sb.append('\n');
		
		sb.append("路径：");
		if (getPlainPathName() != null) {
			sb.append(getPlainPathName());
		} else {
			sb.append("---");
		}
		sb.append('\n');

		sb.append("多指手势控制：");
		if (isPlainEnableMulti()) {
			sb.append("开启");
		} else {
			sb.append("关闭");
		}
		sb.append('\n');
		
		sb.append("更新时间：");
		if (getPlainTime() != null) {
			sb.append(getPlainTime().format("%Y-%m-%d %H:%M:%S"));
		} else {
			sb.append("---");
		}
		sb.append('\n');

		sb.append("备注：");
		if (getPlainDesc() != null) {
			sb.append(getPlainDesc());
		} else {
			sb.append("---");
		}
		//sb.append('\n');
		
		return sb.toString();
	}
	
	public String toShareString() {
		StringBuffer sb = new StringBuffer();
		sb.append("plainFileName:" + plainFileName + "\n");
		sb.append("plainPathName:" + plainPathName + "\n");
		sb.append("plainPage:" + plainPage + "\n");
		sb.append("plainTotalPage:" + plainTotalPage + "\n");
		sb.append("plainTime:" + plainTime + "\n");
		sb.append("plainZoom:" + plainZoom + "\n");
		sb.append("plainPanX:" + plainPanX + "\n");
		sb.append("plainPanY:" + plainPanY + "\n");
		sb.append("plainEnableMulti:" + plainEnableMulti + "\n");
		sb.append("plainDesc:" + plainDesc + "\n");
		return sb.toString();
	}
	
	public String getContent() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("plainFileName", plainFileName);
			jsonObject.put("plainPathName", plainPathName);
			jsonObject.put("plainPage", plainPage);
			jsonObject.put("plainTotalPage", plainTotalPage);
			if (plainTime == null) {
				plainTime = new Time();
			}
			jsonObject.put("plainTime", plainTime.format2445());
			jsonObject.put("plainZoom", (double)plainZoom);
			jsonObject.put("plainPanX", (double)plainPanX);
			jsonObject.put("plainPanY", (double)plainPanY);
			jsonObject.put("plainEnableMulti", plainEnableMulti);
			jsonObject.put("plainDesc", plainDesc);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		content = jsonObject.toString();
		return content;
	}
	
	/**
	 * @see http://blog.csdn.net/jj120522/article/details/7710859
	 * @param content
	 */
	public void setContent(String content) {
		this.content = content;
		try {
			JSONObject jsonObject = new JSONObject(content);
			plainFileName = jsonObject.optString("plainFileName");
			plainPathName = jsonObject.optString("plainPathName");
			plainPage = jsonObject.optInt("plainPage");
			plainTotalPage = jsonObject.optInt("plainTotalPage");
			String strPlainTime = jsonObject.optString("plainTime");
			plainTime = new Time();
			try {
				plainTime.parse(strPlainTime);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			plainZoom = (float)jsonObject.optDouble("plainZoom", 1.0);
			plainPanX = (float)jsonObject.optDouble("plainPanX", 0.5);
			plainPanY = (float)jsonObject.optDouble("plainPanY", 0.5);
			plainEnableMulti = jsonObject.optBoolean("plainEnableMulti", true);
			plainDesc = jsonObject.optString("plainDesc");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getPlainFileName() {
		return plainFileName;
	}

	public void setPlainFileName(String plainFileName) {
		this.plainFileName = plainFileName;
	}

	public String getPlainPathName() {
		return plainPathName;
	}

	public void setPlainPathName(String plainPathName) {
		this.plainPathName = plainPathName;
	}
	
	/**
	 * 对应EXTRA_KEY_FILEID
	 * @return
	 */
	public int getPlainPage() {
		return plainPage;
	}

	public void setPlainPage(int plainPage) {
		this.plainPage = plainPage;
	}

	public int getPlainTotalPage() {
		return plainTotalPage;
	}

	public void setPlainTotalPage(int plainTotalPage) {
		this.plainTotalPage = plainTotalPage;
	}
	
	public Time getPlainTime() {
		return plainTime;
	}

	public void setPlainTime(Time plainTime) {
		this.plainTime = plainTime;
	}

	public float getPlainZoom() {
		return plainZoom;
	}

	public void setPlainZoom(float plainZoom) {
		this.plainZoom = plainZoom;
	}

	public float getPlainPanX() {
		return plainPanX;
	}

	public void setPlainPanX(float plainPanX) {
		this.plainPanX = plainPanX;
	}

	public float getPlainPanY() {
		return plainPanY;
	}

	public void setPlainPanY(float plainPanY) {
		this.plainPanY = plainPanY;
	}

	public boolean isPlainEnableMulti() {
		return plainEnableMulti;
	}

	public void setPlainEnableMulti(boolean plainEnableMulti) {
		this.plainEnableMulti = plainEnableMulti;
	}
	
	public void setPlainDesc(String plainDesc) {
		this.plainDesc = plainDesc;
	}
	
	public String getPlainDesc() {
		return this.plainDesc;
	}
}
