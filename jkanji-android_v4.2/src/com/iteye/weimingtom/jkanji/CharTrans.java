package com.iteye.weimingtom.jkanji;

public class CharTrans {
	  /* 
	   * ぁあぃいぅうぇえぉおかがきぎくぐけげこごさざしじすずせぜそぞ 
	   * ただちぢっつづてでとどなにぬねのはばぱひびぴふぶぷへべぺほぼぽ 
	   * まみむめもゃやゅゆょよらりるれろゎわゐゑをん 
	   *  
	   * ァアィイゥウェエォオカガキギクグケゲコゴサザシジスズセゼソゾ 
	   * タダチヂッツヅテデトドナニヌネノハバパヒビピフブプヘベペホボポ 
	   * マミムメモャヤュユョヨラリルレロヮワヰヱヲンヴヵヶ 
	   */ 
	 public static String zenkakuHiraganaToZenkakuKatakana(String s) { 
		 StringBuffer sb = new StringBuffer(s); 
		 for (int i = 0; i  < sb.length(); i++) { 
			 char c = sb.charAt(i); 
			 if (c >= 'ァ' && c  <= 'ン') { 
				 sb.setCharAt(i, (char)(c - 'ァ' + 'ぁ')); 
			 } else if (c == 'ヵ') { 
				 sb.setCharAt(i, 'か'); 
			 } else if (c == 'ヶ') { 
				 sb.setCharAt(i, 'け'); 
			 } else if (c == 'ヴ') { 
				 sb.setCharAt(i, 'う'); 
				 sb.insert(i + 1, '゛'); 
				 i++; 
			 } 
		 } 
		 return sb.toString();     
	 }
	 
	 public static String formatMean(String mean, boolean isIndent) {
		 if (mean == null) {
			 return "";
		 }
		 String[] strs = mean.split("；");
		 StringBuffer formatStr = new StringBuffer();
		 if (strs != null) {
			 int id = 1;
			 int len = strs.length;
			 for (int i = 0; i < strs.length; i++) {
				 if (strs[i] != null && strs[i].length() > 0) {
					 if (len > 1) {
						 if (isIndent) {
							 formatStr.append("  " + id + ". ");
						 } else {
							 formatStr.append("" + id + ". "); 
						 }
					 } else {
						 if (isIndent) {
							 formatStr.append("  ");
						 }				 
					 }
					 formatStr.append(strs[i]);
					 if (i != strs.length - 1) {
						 //formatStr.append("；\n");
						 formatStr.append("\n");
					 } else {
						 if (isIndent) {
							 formatStr.append("\n"); 
						 }
					 }
					 id++;
				 }
			 }
		 }
		 return formatStr.toString();
	 }
}
