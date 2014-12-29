package com.iteye.weimingtom.jkanji;

import java.util.ArrayList;

import jp.tomorrowkey.android.vtextviewer.CharSetting;

import com.iteye.weimingtom.jkanji.AozoraParser.RubyInfo;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

/**
 * @see http://aina-hk55hk.iteye.com/blog/690162
 * @author Administrator
 *
 * @see http://markmail.org/message/6fxo36tjmbdtvw3q
 * paint.setTextSize(24.0f * getResources().getDisplayMetrics().scaledDensity);
 *
 */
public class BookDrawTextUtil {	
	private final static boolean D = false;
	private final static String TAG = "BookDrawTextUtil";
	
	public final static class DrawTextInfo {
		public String str;
		public int startPos;
	}
	
	public final static class ExtraRubyInfo {
		public String rb;
		public String rt;
		public int rbStartPos;
		
		public ExtraRubyInfo(String rb, String rt, int rbStartPos) {
			this.rb = rb;
			this.rt = rt;
			this.rbStartPos = rbStartPos;
		}
	}
	
	private String text;
	private float textPosX;
	private float textPosY;
	private float textWidth;
	private float textHeight;
	private float textSize;
	private int textColor;
	private boolean showOutline;
	private int outlineColor;
	private float textRubySize;
	private int textRubyColor;
	private float rubyMargin;
	private boolean isVertical;
	
	private ArrayList<RubyInfo> rubyInfoList;
	
	private final class LineInfo {
		String line;
		int startPos;
		int endPos;
		
		public LineInfo(String line, int startPos, int endPos) {
			this.line = line;
			this.startPos = startPos;
			this.endPos = endPos;
		}
	}
	private ArrayList<LineInfo> lines;
	private float fontHeight, fontWidth;
	private int pageLineNum;
	private int currentLine;
	
	private Paint textPaint;
	private Paint textRubyPaint;
	private Path textPath;
	private Typeface typeface;
	
	
	
	public BookDrawTextUtil() {
		textPaint = new Paint();
		textRubyPaint = new Paint();
		textPath = new Path();
		lines = new ArrayList<LineInfo>();
	}
	
	public void initText(String text, float textPosX, float textPosY, float textWidth, float textHeight, float textSize, int textColor, boolean showOutline, int outlineColor, Typeface typeface, ArrayList<RubyInfo> rubyInfoList, float textRubySize, int textRubyColor, float rubyMargin, boolean isVertical) {		
		this.text = text;
		this.textPosX = textPosX;
		this.textPosY = textPosY;
		this.textWidth = textWidth;
		this.textHeight = textHeight;
		this.textSize = textSize;
		this.textColor = textColor;
		this.showOutline = showOutline;
		this.outlineColor = outlineColor;
		this.typeface = typeface;
		this.rubyInfoList = rubyInfoList;
		this.textRubySize = textRubySize;
		this.textRubyColor = textRubyColor;
		this.rubyMargin = rubyMargin;
		this.isVertical = isVertical;
		
		//FIXME:
		this.currentLine = 0;
		
		lines.clear();
		
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(this.textSize);
		textPaint.setColor(this.textColor);
		textPaint.setTypeface(this.typeface);

		textRubyPaint.setTextAlign(Paint.Align.LEFT);
		textRubyPaint.setAntiAlias(true);
		textRubyPaint.setTextSize(this.textRubySize);
		textRubyPaint.setColor(this.textRubyColor);
		textRubyPaint.setTypeface(this.typeface);
		
		getTextInfo();
	}
	
	/**
	 * FIXME:
	 * FontMetrics fm = mPaint.getFontMetrics();
	 * mFontHeight = (int) (Math.ceil(fm.descent - fm.top) + 2);
	 */
	private void getTextInfo() {
		if (isVertical) {
			getTextInfoV();
		} else {
			getTextInfoH();
		}
	}

	private void getTextInfoH() {
		fontHeight = -textPaint.ascent() + textPaint.descent();
		pageLineNum = (int) (textHeight / fontHeight) / 2 - 1;
		float w = 0;
		int istart = 0;
		final int count = text.length();
		for (int i = 0; i < count; i++) {
			char ch = this.text.charAt(i);
			float[] widths = new float[1];
			textPaint.getTextWidths(String.valueOf(ch), widths);
			if (ch == '\n') {
				lines.add(new LineInfo(text.substring(istart, i), istart, i));
				istart = i + 1;
				w = 0;
			} else {
				w += widths[0];
				if (w > textWidth) {
					lines.add(new LineInfo(text.substring(istart, i), istart, i - 1));
					istart = i;
					i--;
					w = 0;
				} else {
					if (i == count - 1) {
						lines.add(new LineInfo(text.substring(istart, count), istart, count - 1));
					}
				}
			}
		}
		if (D) {
			for (int i = 0; i < lines.size(); i++) {
				LineInfo info = lines.get(i);
				if (info != null) {
					Log.e(TAG, ((i % (this.pageLineNum + 1) == 0) ? ">>" : "") + "info[" + i + "] = (" + info.startPos + "," + info.endPos + ")");
				}
			}
		}
	}
	
	/**
	 * @see http://code.google.com/p/tomorrowkey/source/browse/#svn%2Ftrunk%2FVTextViewer
	 */
	private void getTextInfoV() {
		fontWidth = textPaint.getFontSpacing();
		pageLineNum = (int) (textWidth / fontWidth) / 2 - 1; //FIXME:
		float h = 0;
		int istart = 0;
		final int count = text.length();
		for (int i = 0; i < count; i++) {
			char ch = this.text.charAt(i);
			float height = textPaint.getFontSpacing();
			if (ch == '\n') {
				lines.add(new LineInfo(text.substring(istart, i), istart, i));
				istart = i + 1;
				h = 0;
			} else {
				h += height;
				if (h > textHeight) {
					lines.add(new LineInfo(text.substring(istart, i), istart, i));
					istart = i;
					i--;
					h = 0;
				} else {
					if (i == count - 1) {
						lines.add(new LineInfo(text.substring(istart, count), istart, count));
					}
				}
			}
		}
	}
	
	public int getLineFirstPos(int line) {
		if (lines.isEmpty()) {
			return -1;
		}
		if (this.currentLine + line < 0 || this.currentLine + line > lines.size() - 1) {
			return -1;
		}
		return lines.get(this.currentLine + line).startPos;
	}

	public int getLineLastPos(int line) {
		if (lines.isEmpty()) {
			return -1;
		}
		if (this.currentLine + line < 0 || this.currentLine + line > lines.size() - 1) {
			return -1;
		}
		return lines.get(this.currentLine + line).endPos;
	}
	
	public int getPageFirstPos() {
		if (lines.isEmpty()) {
			return -1;
		}
		//FIXME: see BookLoader.setCurPage
		//this.curPosition = this.textUtil.getPageFirstPos();
		if (this.currentLine < 0) {
			this.currentLine = 0;
			return lines.get(0).startPos;
		} else if (this.currentLine >= lines.size()) {
			this.currentLine = lines.size() - 1;
		}
		return lines.get(this.currentLine).startPos;
	}
	
	public int getPageLastPos() {
		if (lines.isEmpty()) {
			return -1;
		}
		int endLine = 0;
		if (this.currentLine + this.pageLineNum < lines.size() - 1) {
			endLine = this.currentLine + this.pageLineNum;
		} else {
			endLine = lines.size() - 1;
		}
		return lines.get(endLine).endPos;
	}
	
	public DrawTextInfo getDrawText() {
		int beginLine = 0;
		int endLine = 0;
		if (this.currentLine > 0) {
			beginLine = this.currentLine - 1; //minus one line
		} else {
			beginLine = this.currentLine;
		}
		if (this.currentLine + this.pageLineNum < lines.size() - 1) {
			endLine = this.currentLine + this.pageLineNum + 1; //plus one line
		} else {
			endLine = lines.size() - 1;
		}
		int begin;
		int end;
		//FIXME:
		if (lines != null && !lines.isEmpty()) {
			if (beginLine >= 0 && beginLine < lines.size()) {
				begin = lines.get(beginLine).startPos;
			} else {
				begin = 0;
			}
		} else {
			begin = 0;
		}
		if (lines != null && !lines.isEmpty()) {
			if (endLine >= 0 && endLine < lines.size()) {
				end = lines.get(endLine).endPos;
			} else {
				end = 0;
			}
		} else {
			end = 0;
		}
		DrawTextInfo result = new DrawTextInfo();
		if (!lines.isEmpty()) {
			result.str = text.substring(begin, end);
			result.startPos = begin;
		}
		return result;
	}
	
	public int getPositionLine(float x, float y) {
		// plus one, because the text is drawn on bottom base line
		if (this.isVertical) {
			return (int)Math.floor(((this.textWidth - x) / this.fontWidth) / 2);
		} else {
			return (int)Math.floor(((y + textPaint.ascent() - this.textPosY) / this.fontHeight - 1 ) / 2) + 1;
		}
	}
	
	public int getTextIndexOnPosXY(int startPos, int endPos, float x, float y) {
		if (this.isVertical) {
			return getTextIndexOnPosYV(startPos, endPos, y);
		} else {
			return getTextIndexOnPosXH(startPos, endPos, x);
		}
	}
	
	private int getTextIndexOnPosXH(int startPos, int endPos, float x) {
		if (startPos >= endPos || text == null) {
			return -1;
		}
		
		float w = 0;
		for (int index = startPos; index /*<*/<= endPos; index++) {
			char c = text.charAt(index);
			float[] widths = new float[1];
			textPaint.getTextWidths(String.valueOf(c), widths);
			w += widths[0];
			if (w > x) {
				return index;
			}
		}
		return -1;
	}
	
	private int getTextIndexOnPosYV(int startPos, int endPos, float y) {
		if (startPos >= endPos || text == null) {
			return -1;
		}
		
		float h = 0;
		for (int index = startPos; index /*<*/<= endPos; index++) {
			h += textPaint.getFontSpacing();
			if (h > y) {
				return index;
			}
		}
		return -1;
	}
	
	public void drawText(Canvas canvas, ArrayList<ExtraRubyInfo> extraRubyInfoList) {
		if (this.isVertical) {
			drawTextV(canvas, extraRubyInfoList);
		} else {
			drawTextH(canvas, extraRubyInfoList);
		}
	}
	
	private void drawTextH(Canvas canvas, ArrayList<ExtraRubyInfo> extraRubyInfoList) {
		String lastBreakRb = null;
		String lastBreakRt = null;
		boolean isDummyDraw = false;
		String lastExtraBreakRb = null;
		String lastExtraBreakRt = null;
		/**
		 * TODO: i = this.currentLine - 1, j = -1
		 * -> 
		 * i want to read last line, 
		 * only for broken rt part in last page, 
		 * when isDummyDraw == true.
		 */
		for (int i = this.currentLine - 1, j = -1; 
			i < lines.size() && j <= this.pageLineNum; 
			i++, j++) {
			if (i < 0) {
				continue; // -1 line
			}
			if (i == this.currentLine - 1) {
				isDummyDraw = true;
			} else {
				isDummyDraw = false;
			}
			String text = lines.get(i).line;
			int startPos = lines.get(i).startPos;
			int endPos = lines.get(i).endPos;
			float x = this.textPosX;
			float y = this.textPosY + this.fontHeight * (2 * j + 1) - textPaint.ascent();
			
			/**
			 * bug?
			 * @see http://www.kaede-software.com/2011/01/post_548.html
			 * 
			 */
			if (this.showOutline) {
				textPaint.getTextPath(text, 0, text.length(), x, y, textPath);
				textPaint.setColor(this.outlineColor);
				textPaint.setStyle(Paint.Style.STROKE);
				textPaint.setStrokeWidth(1);
				if (!isDummyDraw) {
					canvas.drawPath(textPath, textPaint);
				}
				textPaint.setColor(this.textColor);
				textPaint.setStyle(Paint.Style.FILL);
			}
			//ruby text
			if (this.rubyInfoList != null) {
				Rect bounds = new Rect();
				
				//rt is broken, the right part goes to head of next line 
				//FIXME: P49
				if (lastBreakRb != null && lastBreakRt != null) {
					int index = 0;
					//align center
					float[] widths = new float[index + 1];
					textPaint.getTextWidths(text, 0, index, widths);
					float totalWidth = 0;
					for (int m = 0; m < widths.length; m++) {
						totalWidth += widths[m];
					}
					widths = new float[index + 1 + lastBreakRb.length()];
					//FIXME:
					if (index + lastBreakRb.length() > text.length()) {
						textPaint.getTextWidths(text, 0, text.length(), widths);
					} else {
						textPaint.getTextWidths(text, 0, index + lastBreakRb.length(), widths);
					}
					float totalWidth2 = 0;
					for (int m = 0; m < widths.length; m++) {
						totalWidth2 += widths[m];
					}
					textRubyPaint.setTextAlign(Paint.Align.CENTER);
					//this.fontHeight
					if (!isDummyDraw) {
						canvas.drawText(lastBreakRt, x + (totalWidth + totalWidth2) / 2, y + 1 * textRubyPaint.ascent() - textRubyPaint.descent() - this.rubyMargin - textPaint.descent(), textRubyPaint);	
					}
					//clear history
					lastBreakRb = lastBreakRt = null;
				}
				for (int k = 0; k < rubyInfoList.size(); k++) {
					RubyInfo item = rubyInfoList.get(k);
					if (item.rbStartPos > endPos) {
						break;
					}
					int index = item.rbStartPos - startPos;
					if (index >= 0 && index < text.length()) {
						if (false) {
							textPaint.getTextBounds(text, 0, index, bounds);
							canvas.drawText(item.rt, x + bounds.width(), y - this.fontHeight, textRubyPaint);
						} else if (false) {
							//align left
							float[] widths = new float[index + 1];
							textPaint.getTextWidths(text, 0, index, widths);
							float totalWidth = 0;
							for (int m = 0; m < widths.length; m++) {
								totalWidth += widths[m];
							}
							canvas.drawText(item.rt, x + totalWidth, y - this.fontHeight, textRubyPaint);
						} else {
							//align center
							float[] widths = new float[index + 1];
							textPaint.getTextWidths(text, 0, index, widths);
							float totalWidth = 0;
							for (int m = 0; m < widths.length; m++) {
								totalWidth += widths[m];
							}
							widths = new float[index + 1 + item.rb.length()];
							//FIXME:
							if (index + item.rb.length() > text.length()) {
								int rbHaftIndex = text.length() - index;
								int rtHaftIndex = (int)(((double)rbHaftIndex / item.rb.length()) * item.rt.length()); 
								textPaint.getTextWidths(text, 0, text.length(), widths);
								String rbLeft = item.rb.substring(0, rbHaftIndex);
								String rbRight = item.rb.substring(rbHaftIndex);
								String rtLeft = item.rt.substring(0, rtHaftIndex);
								String rtRight = item.rt.substring(rtHaftIndex);
								
								lastBreakRb = rbRight;
								lastBreakRt = rtRight;
								
								float totalWidth2 = 0;
								for (int m = 0; m < widths.length; m++) {
									totalWidth2 += widths[m];
								}
								textRubyPaint.setTextAlign(Paint.Align.CENTER);
								if (!isDummyDraw) {
									canvas.drawText(rtLeft, x + (totalWidth + totalWidth2) / 2, y + 1 * textRubyPaint.ascent() - textRubyPaint.descent() - rubyMargin - textPaint.descent(), textRubyPaint);
								}
							} else {
								textPaint.getTextWidths(text, 0, index + item.rb.length(), widths);
								float totalWidth2 = 0;
								for (int m = 0; m < widths.length; m++) {
									totalWidth2 += widths[m];
								}
								textRubyPaint.setTextAlign(Paint.Align.CENTER);
								//this.fontHeight
								if (!isDummyDraw) {
									canvas.drawText(item.rt, x + (totalWidth + totalWidth2) / 2, y + 1 * textRubyPaint.ascent() - textRubyPaint.descent() - rubyMargin - textPaint.descent(), textRubyPaint);
								}
							}
						}
						
					}
				}
			}
			
			
			//extra ruby text
			if (extraRubyInfoList != null) {
				//rt is broken, the right part goes to head of next line 
				//FIXME: P49
				if (lastExtraBreakRb != null && lastExtraBreakRt != null) {
					int index = 0;
					//align center
					float[] widths = new float[index + 1];
					textPaint.getTextWidths(text, 0, index, widths);
					float totalWidth = 0;
					for (int m = 0; m < widths.length; m++) {
						totalWidth += widths[m];
					}
					widths = new float[index + 1 + lastExtraBreakRb.length()];
					//FIXME:
					if (index + lastExtraBreakRb.length() > text.length()) {
						textPaint.getTextWidths(text, 0, text.length(), widths);
					} else {
						textPaint.getTextWidths(text, 0, index + lastExtraBreakRb.length(), widths);
					}
					float totalWidth2 = 0;
					for (int m = 0; m < widths.length; m++) {
						totalWidth2 += widths[m];
					}
					textRubyPaint.setTextAlign(Paint.Align.CENTER);
					//this.fontHeight
					if (!isDummyDraw) {
						canvas.drawText(lastExtraBreakRt, x + (totalWidth + totalWidth2) / 2, y + (textRubyPaint.ascent() - textRubyPaint.descent() - this.rubyMargin) * 2, textRubyPaint);	
					}
					//clear history
					lastExtraBreakRb = lastExtraBreakRt = null;
				}
				for (int k = 0; k < extraRubyInfoList.size(); k++) {
					ExtraRubyInfo item = extraRubyInfoList.get(k);
					if (item.rbStartPos > endPos) {
						break;
					}
					int index = item.rbStartPos - startPos;
					if (index >= 0 && index < text.length()) {
						{
							//align center
							float[] widths = new float[index + 1];
							textPaint.getTextWidths(text, 0, index, widths);
							float totalWidth = 0;
							for (int m = 0; m < widths.length; m++) {
								totalWidth += widths[m];
							}
							widths = new float[index + 1 + item.rb.length()];
							//FIXME:
							if (index + item.rb.length() > text.length()) {
								int rbHaftIndex = text.length() - index;
								int rtHaftIndex = (int)(((double)rbHaftIndex / item.rb.length()) * item.rt.length()); 
								textPaint.getTextWidths(text, 0, text.length(), widths);
								String rbLeft = item.rb.substring(0, rbHaftIndex);
								String rbRight = item.rb.substring(rbHaftIndex);
								String rtLeft = item.rt.substring(0, rtHaftIndex);
								String rtRight = item.rt.substring(rtHaftIndex);
								
								lastExtraBreakRb = rbRight;
								lastExtraBreakRt = rtRight;
								
								float totalWidth2 = 0;
								for (int m = 0; m < widths.length; m++) {
									totalWidth2 += widths[m];
								}
								textRubyPaint.setTextAlign(Paint.Align.CENTER);
								if (!isDummyDraw) {
									canvas.drawText(rtLeft, x + (totalWidth + totalWidth2) / 2, y + (textRubyPaint.ascent() - textRubyPaint.descent() - rubyMargin) * 2 - rubyMargin, textRubyPaint);
								}
							} else {
								textPaint.getTextWidths(text, 0, index + item.rb.length(), widths);
								float totalWidth2 = 0;
								for (int m = 0; m < widths.length; m++) {
									totalWidth2 += widths[m];
								}
								textRubyPaint.setTextAlign(Paint.Align.CENTER);
								//this.fontHeight
								if (!isDummyDraw) {
									canvas.drawText(item.rt, x + (totalWidth + totalWidth2) / 2, y + (textRubyPaint.ascent() - textRubyPaint.descent() - rubyMargin) * 2 - rubyMargin, textRubyPaint);
								}
							}
						}
						
					}
				}
			}
			
			// original text
			if (!isDummyDraw) {
				canvas.drawText(text, x, y, textPaint);
			}
		}
	}
	
	/**
	 * @see getTextInfoV
	 * 
	 * @param canvas
	 * @param extraRubyInfoList
	 */
	private void drawTextV(Canvas canvas, ArrayList<ExtraRubyInfo> extraRubyInfoList) {
		{
			float x = textPosX + textWidth - fontWidth * 2;
	        float y = textPosY + textPaint.getFontSpacing(); //FIXME:
	        //this.rubyInfoList;
			for (int i = this.currentLine; 
				i <= this.currentLine + this.pageLineNum && 
				i < this.lines.size(); 
				++i) {
				String text = lines.get(i).line;
				for (int j = 0; j < text.length(); ++j) {
					String si = text.substring(j, j + 1);
					CharSetting setting = CharSetting.getSetting(si);
		            if (setting == null) {
		                canvas.drawText(si, x, y, textPaint);
		            } else {
		                canvas.save();
		                canvas.rotate(setting.angle, x, y);
		                canvas.drawText(si, x + fontWidth * setting.x, y + fontWidth * setting.y, textPaint);
		                canvas.restore();
		            }
		            y += textPaint.getFontSpacing();
				}
				y = textPosY + textPaint.getFontSpacing();
				x -= fontWidth * 2;
			}
		}
	
		
		drawRubyList(canvas, this.rubyInfoList);
		drawExtraRubyList(canvas, extraRubyInfoList);
	}
	
	private void drawRubyList(Canvas canvas, ArrayList<RubyInfo> rubylist) {
		if (rubylist != null) {
			for (int m = this.currentLine; 
				m <= this.currentLine + this.pageLineNum && 
				m < this.lines.size(); 
				++m) {
				int lineStartPos = lines.get(m).startPos;
				int lineEndPos = lines.get(m).endPos;
				for (int k = 0; k < rubylist.size(); k++) {
					RubyInfo info = rubylist.get(k);
					int rbend = info.rbStartPos + info.rb.length() - 1;
					if ((info.rbStartPos >= lineStartPos && info.rbStartPos <= lineEndPos) ||
						(rbend >= lineStartPos && rbend <= lineEndPos)) {
						int line = m - this.currentLine;
						String rb, rt;
						int rbStartPos;
						if (info.rbStartPos < lineStartPos) {
							float left = (float)(lineStartPos - info.rbStartPos) / info.rb.length();
							int rbLeft = (int)(left * info.rb.length());
							int rtLeft = (int)(left * info.rt.length());
							rb = info.rb.substring(rbLeft);
							rt = info.rt.substring(rtLeft);
							rbStartPos = info.rbStartPos + rbLeft - lineStartPos;
						} else if (rbend >= lineEndPos) {
							float left = ((float)(lineEndPos - info.rbStartPos) / info.rb.length());
							int rbLeft = (int)(left * info.rb.length());
							int rtLeft = (int)(left * info.rt.length());
							rb = info.rb.substring(0, rbLeft);
							rt = info.rt.substring(0, rtLeft);
							rbStartPos = info.rbStartPos - lineStartPos;
						} else {
							rb = info.rb;
							rt = info.rt;
							rbStartPos = info.rbStartPos - lineStartPos;
						}
						drawRubyV(canvas, rb, rt, rbStartPos, line, false);
					}
				}
			}
		}
	}
	
	private void drawExtraRubyList(Canvas canvas, ArrayList<ExtraRubyInfo> rubylist) {
		if (rubylist != null) {
			for (int m = this.currentLine; 
				m <= this.currentLine + this.pageLineNum && 
				m < this.lines.size(); 
				++m) {
				int lineStartPos = lines.get(m).startPos;
				int lineEndPos = lines.get(m).endPos;
				for (int k = 0; k < rubylist.size(); k++) {
					ExtraRubyInfo info = rubylist.get(k);
					int rbend = info.rbStartPos + info.rb.length() - 1;
					if ((info.rbStartPos >= lineStartPos && info.rbStartPos <= lineEndPos) ||
						(rbend >= lineStartPos && rbend <= lineEndPos)) {
						int line = m - this.currentLine;
						String rb, rt;
						int rbStartPos;
						if (info.rbStartPos < lineStartPos) {
							float left = (float)(lineStartPos - info.rbStartPos) / info.rb.length();
							int rbLeft = (int)(left * info.rb.length());
							int rtLeft = (int)(left * info.rt.length());
							rb = info.rb.substring(rbLeft);
							rt = info.rt.substring(rtLeft);
							rbStartPos = info.rbStartPos + rbLeft - lineStartPos;
						} else if (rbend >= lineEndPos) {
							float left = ((float)(lineEndPos - info.rbStartPos) / info.rb.length());
							int rbLeft = (int)(left * info.rb.length());
							int rtLeft = (int)(left * info.rt.length());
							rb = info.rb.substring(0, rbLeft);
							rt = info.rt.substring(0, rtLeft);
							rbStartPos = info.rbStartPos - lineStartPos;
						} else {
							rb = info.rb;
							rt = info.rt;
							rbStartPos = info.rbStartPos - lineStartPos;
						}
						drawRubyV(canvas, rb, rt, rbStartPos, line, true);
					}
				}
			}
		}
	}
	
	/**
	 * 单行垂直的rt绘画
	 * @param canvas
	 * @param info
	 * @param line
	 */
	private void drawRubyV(Canvas canvas, String rb, String rt, int rbStartPos, int line, boolean isExtra) {
		if (line >= 0) {
			float rtX = textPosX + textWidth - 2 * fontWidth + 
					textRubyPaint.getFontSpacing()/* * 2*/ + this.rubyMargin -
					2 * line * fontWidth;
			if (isExtra) {
				rtX = rtX + textRubyPaint.getFontSpacing();
			}
			float rtY = textPosY + 
					//textPaint.getFontSpacing() + 
					rbStartPos * textPaint.getFontSpacing() + 
					textRubyPaint.getFontSpacing();
			rtY = rtY + 
				rb.length() * textPaint.getFontSpacing() * 0.5f - 
				rt.length() * textRubyPaint.getFontSpacing() * 0.5f; 
			for (int p = 0; p < rt.length(); ++p) {
				String si = rt.substring(p, p + 1);
				CharSetting setting = CharSetting.getSetting(si);
	            if (setting == null) {
	                canvas.drawText(si, rtX, rtY, textRubyPaint);
	            } else {
	                canvas.save();
	                canvas.rotate(setting.angle, rtX, rtY);
	                canvas.drawText(si, rtX + fontWidth * setting.x, rtY + fontWidth * setting.y, textRubyPaint);
	                canvas.restore();
	            }
	            rtY += textRubyPaint.getFontSpacing();
			}
		}
	}
	
	public void setPage(int page) {
		this.currentLine = page * (pageLineNum + 1) ;
	}
	
	public int getTotalPage() {
		return (lines.size() / (pageLineNum + 1)) + 1;
	}
	
    public int positionToPage(int pos) {
    	if (D) {
    		Log.e(TAG, "positionToPage " + pos);
    	}
    	int len = lines.size();
    	if (len == 0) {
    		return 0;
    	} else if (len >= 0 && pos < lines.get(0).startPos) {
    		return 0;
    	}
    	for (int i = 0; i < len; i++) {
    		if (pos >= lines.get(i).startPos && pos <= lines.get(i).endPos) {
//	        	if (D) {
//	    			Log.d(TAG, "positionToPage " + i + "," + lines.get(i).startPos + "," + lines.get(i + this.pageLineNum).endPos + "," + this.pageLineNum);
//	    		}
    			return (int)Math.floor(i / (pageLineNum + 1));
    		}
    	}
    	return getTotalPage() - 1;
    }
}
