package fuku.eb4j.util;

public class ArrayUtils {
	public static boolean isEmpty(byte[] array) {
		return (array == null) || (array.length == 0);
	}
	
	public static boolean isEmpty(Object[] array) {
	    return (array == null) || (array.length == 0);
	}
}
