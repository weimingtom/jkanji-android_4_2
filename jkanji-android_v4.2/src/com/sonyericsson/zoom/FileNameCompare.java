package com.sonyericsson.zoom;

import java.util.ArrayList;

public class FileNameCompare {
	private static class NamePart {
		public String strPart;
	}
	
	private static class StringNamePart extends NamePart {
		
	}

	private static class NumberNamePart extends NamePart {
		public long numPart;
	}
	
	public static int compareParts(String text1, String text2) {
		ArrayList<NamePart> parts1 = getParts(text1);
		ArrayList<NamePart> parts2 = getParts(text2);
		if (parts1 == null) {
			return -1;
		} else if (parts2 == null) {
			return 1;
		} else {
			for (int i = 0; i < parts1.size() || i < parts2.size(); i++) {
				if (i >= parts1.size()) {
					return -1;
				} else if (i >= parts2.size()) {
					return 1;
				}
				NamePart part1 = parts1.get(i);
				NamePart part2 = parts2.get(i);
				if (part1 instanceof NumberNamePart &&
					part2 instanceof NumberNamePart) {
					long num1 = ((NumberNamePart) part1).numPart;
					long num2 = ((NumberNamePart) part2).numPart;
					if (num1 != num2) {
						if (num1 < num2) {
							return -1;
						} else {
							return 1;
						}
					}
				} else {
					String str1 = part1.strPart;
					String str2 = part2.strPart;
					int comp = str1.compareToIgnoreCase(str2);
//					System.out.println(str1 + ", " + str2);
					if (comp != 0) {
						return comp;
					}
				}
			}
			return 0;
		}
	}
	
	private static ArrayList<NamePart> getParts(String text) {
		ArrayList<NamePart> parts = new ArrayList<NamePart>();
		StringBuffer sb = new StringBuffer();
		int type = 0;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (Character.isDigit(ch)) {
				if (type < 0) {
					StringNamePart part = new StringNamePart();
					part.strPart = sb.toString();
					parts.add(part);
					sb.setLength(0);
				}
				type = 1;
			} else {
				if (type > 0) {
					NumberNamePart part = new NumberNamePart();
					part.strPart = sb.toString();
					try {
						part.numPart = Long.parseLong(part.strPart);
					} catch (Throwable e) {
						e.printStackTrace();
						part.numPart = 0L;
					}
					parts.add(part);
					sb.setLength(0);
				}
				type = -1;
			}
			sb.append(ch);
		}
		if (type < 0) {
			StringNamePart part = new StringNamePart();
			part.strPart = sb.toString();
			parts.add(part);
			sb.setLength(0);
		} else {
			NumberNamePart part = new NumberNamePart();
			part.strPart = sb.toString();
			try {
				part.numPart = Long.parseLong(part.strPart);
			} catch (Throwable e) {
				e.printStackTrace();
				part.numPart = 0L;
			}
			parts.add(part);
			sb.setLength(0);
		}
		return parts;
	}
}
