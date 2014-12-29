package com.iteye.weimingtom.jkanji;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.Time;
import android.util.Log;

public class JkanjiShelfHistoryItem {
	private final static boolean D = false;
	private final static String TAG = "JkanjiShelfHistoryItem";
	
	private long id = -1L;
	private String content;

	private String plainFileName;
	private String plainEncoding;
	private int plainCharPos;
	private int plainCharLength;
	private Time plainTime;
	private int parserType;
	private String plainDesc;
	
	//parserType
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
		String strProgress = "进度：---";
		int charPos = getPlainCharPos();
		int length = getPlainCharLength();
		if (length != 0) {
			strProgress = String.format("进度: %d%%(%d/%d)", charPos * 100 / length, charPos, length);
		}
		sb.append(strProgress);
		sb.append('\n');

		sb.append("路径：");
		if (getPlainFileName() != null) {
			sb.append(getPlainFileName());
		} else {
			sb.append("---");
		}
		sb.append('\n');
		
		sb.append("文本编码：");
		if (getPlainEncoding() != null) {
			sb.append(getPlainEncoding());
		} else {
			sb.append("---");
		}
		sb.append('\n');
		
		sb.append("更新时间：");
		if (getPlainTime() != null) {
			sb.append(getPlainTime().format("%Y-%m-%d %H:%M:%S"));
		} else {
			sb.append("---");
		}
		sb.append('\n');
		
		sb.append("解释器：");
		if (getParserType() == PLAIN_FORMAT_AOZORA) {
			sb.append("青空文库格式");
		} else {
			sb.append("纯文本格式");
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
		sb.append("plainEncoding:" + plainEncoding + "\n");
		sb.append("plainCharPos:" + plainCharPos + "\n");
		sb.append("plainCharLength:" + plainCharLength + "\n");
		sb.append("plainTime:" + plainTime + "\n");
		sb.append("parserType:" + parserType + "\n");
		sb.append("plainDesc:" + plainDesc + "\n");
		return sb.toString();
	}
	
	public String getContent() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("plainFileName", plainFileName);
			jsonObject.put("plainEncoding", plainEncoding);
			jsonObject.put("plainCharPos", plainCharPos);
			jsonObject.put("plainCharLength", plainCharLength);
			if (plainTime == null) {
				plainTime = new Time();
			}
			jsonObject.put("plainTime", plainTime.format2445());
			jsonObject.put("parserType", parserType);
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
			plainEncoding = jsonObject.optString("plainEncoding");
			plainCharPos = jsonObject.optInt("plainCharPos");
			plainCharLength = jsonObject.optInt("plainCharLength");
			String strPlainTime = jsonObject.optString("plainTime");
			plainTime = new Time();
			try {
				plainTime.parse(strPlainTime);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			parserType = jsonObject.optInt("parserType");
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

	public String getPlainEncoding() {
		return plainEncoding;
	}

	public void setPlainEncoding(String plainEncoding) {
		this.plainEncoding = plainEncoding;
	}

	public int getPlainCharPos() {
		return plainCharPos;
	}

	public void setPlainCharPos(int plainCharPos) {
		this.plainCharPos = plainCharPos;
	}

	public int getPlainCharLength() {
		return plainCharLength;
	}

	public void setPlainCharLength(int plainCharLength) {
		this.plainCharLength = plainCharLength;
	}

	public Time getPlainTime() {
		return plainTime;
	}

	public void setPlainTime(Time plainTime) {
		this.plainTime = plainTime;
	}

	public int getParserType() {
		return parserType;
	}

	public void setParserType(int parserType) {
		this.parserType = parserType;
	}
	
	public void setPlainDesc(String plainDesc) {
		this.plainDesc = plainDesc;
	}
	
	public String getPlainDesc() {
		return this.plainDesc;
	}
}
