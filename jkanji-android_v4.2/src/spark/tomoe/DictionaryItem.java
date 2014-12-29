package spark.tomoe;

public class DictionaryItem {
	public char c;
	public int[][][] d;
	
	public DictionaryItem(char c, int[][][] d) {
		this.c = c;
		this.d = d;
	}
	
	public int getStrokesLen() {
		return d.length;
	}
}
