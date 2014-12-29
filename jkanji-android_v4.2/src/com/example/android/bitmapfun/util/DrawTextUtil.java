package com.example.android.bitmapfun.util;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * @see http://aina-hk55hk.iteye.com/blog/690162
 * @author Administrator
 *
 * @see http://markmail.org/message/6fxo36tjmbdtvw3q
 * paint.setTextSize(24.0f * getResources().getDisplayMetrics().scaledDensity);
 *
 */
public class DrawTextUtil {	
	private String text;
	private int textPosX;
	private int textPosY;
	private int textWidth;
	private int textHeight;
	private float textSize;
	private int textColor;
	private boolean showOutline;
	private int outlineColor;
	
	private ArrayList<String> lines;
	private float fontHeight;
	private int pageLineNum;
	private int currentLine;
	
	private Paint textPaint;
	private Path textPath;
	
	public DrawTextUtil() {
		textPaint = new Paint();
		textPath = new Path();
		lines = new ArrayList<String>();
	}
	
	public void initText(String text, int textPosX, int textPosY, int textWidth, int textHeight, float textSize, int textColor, boolean showOutline, int outlineColor) {
		this.text = text;
		this.textPosX = textPosX;
		this.textPosY = textPosY;
		this.textWidth = textWidth;
		this.textHeight = textHeight;
		this.textSize = textSize;
		this.textColor = textColor;
		this.showOutline = showOutline;
		this.outlineColor = outlineColor;
		
		//FIXME:
		this.currentLine = 0;
		
		lines.clear();
		textPaint.setDither(true);
		textPaint.setFilterBitmap(true);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(this.textSize);
		textPaint.setColor(this.textColor);
		getTextInfo();
	}
	
	/**
	 * FIXME:
	 * FontMetrics fm = mPaint.getFontMetrics();
	 * mFontHeight = (int) (Math.ceil(fm.descent - fm.top) + 2);
	 */
	private void getTextInfo() {
		fontHeight = -textPaint.ascent() + textPaint.descent();
		pageLineNum = (int) (textHeight / fontHeight);
		float w = 0;
		int istart = 0;
		final int count = text.length();
		for (int i = 0; i < count; i++) {
			char ch = this.text.charAt(i);
			float[] widths = new float[1];
			textPaint.getTextWidths(String.valueOf(ch), widths);
			if (ch == '\n') {
				lines.add(text.substring(istart, i));
				istart = i + 1;
				w = 0;
			} else {
				w += widths[0];
				if (w > textWidth) {
					lines.add(text.substring(istart, i));
					istart = i;
					i--;
					w = 0;
				} else {
					if (i == count - 1) {
						lines.add(text.substring(istart, count));
					}
				}
			}
		}
	}
	
	public void drawText(Canvas canvas) {
		for (int i = this.currentLine, j = 0; 
			i < lines.size() && j <= this.pageLineNum; 
			i++, j++) {
			String text = lines.get(i);
			float x = this.textPosX;
			float y = this.textPosY + this.fontHeight * j - textPaint.ascent();
			
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
				canvas.drawPath(textPath, textPaint);
				textPaint.setColor(this.textColor);
				textPaint.setStyle(Paint.Style.FILL);
			}
			canvas.drawText(text, x, y, textPaint);
		}
	}
	
	public void lineUp() {
		if (this.currentLine > 0) {
			this.currentLine--;
		}
	}
	
	public void lineDown() {
		if (this.currentLine + this.pageLineNum < lines.size() - 1) {
			this.currentLine++;
		}		
	}
}
