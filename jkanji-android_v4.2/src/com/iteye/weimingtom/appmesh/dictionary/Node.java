package com.iteye.weimingtom.appmesh.dictionary;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;

import com.iteye.weimingtom.appmesh.file.DictionaryInputStream;

public class Node {
	public static final int BYTES_MAX = 65536;
	
	private ArrayList<Character> childrenKey = new ArrayList<Character>(); 
	private ArrayList<Integer> childrenValue = new ArrayList<Integer>(); 
	private Character charKey;
	private String prefixVal;
	private boolean terminal;
	private int id;
	private Record value;
	private boolean expanded;
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[" + id + "] " + charKey + "," + terminal + "," + prefixVal + ":");
		for (int i = 0; i < childrenKey.size(); i++) {
			sb.append("(" + childrenKey.get(i) + ", " + childrenValue.get(i) + "),");
		}
		//FIXME:Record
		if (value != null) {
			sb.append(" // " + value.getKanji() + " // " + value.getMean());
		}
		return sb.toString();
	}
	
	/**
	 * @see DictionaryInputStream#readNodeById
	 * @return
	 */
	public ByteBuffer toByteBuffer() {
		ByteBuffer bytes = ByteBuffer.allocate(BYTES_MAX);
		bytes.position(0);
		bytes.order(ByteOrder.LITTLE_ENDIAN);
		bytes.clear();
		
		bytes.put(terminal ? (byte)1 : (byte)0); // 1 bytes
		bytes.putInt(childrenKey.size()); // 4 bytes
		for (Character key : childrenKey) {
			bytes.putChar(key); // 2 bytes
		}
		for (Integer value : childrenValue) {
			bytes.putInt(value); // 4 bytes
		}
		if (prefixVal != null) {
			try {
				byte[] bytesStr = prefixVal.getBytes("UTF-8");
				bytes.putInt(bytesStr.length);
				bytes.put(bytesStr);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else {
			bytes.putInt(0);
		}
		
		//FIXME:Record
		if (value != null) {
			ByteBuffer valueBuffer = value.toByteBuffer();
			byte[] valueBytes = toByteArray(valueBuffer);
			bytes.putInt(valueBytes.length);
			bytes.put(valueBytes);
		} else {
			bytes.putInt(0);
		}
		
		return bytes;
	}
	
	public Character getChar() {
		return charKey;
	}
	
	public void setChar(Character charKey) {
		this.charKey = charKey;
	}
	
	public String getPrefix() {
		return prefixVal;
	}

	public void setPrefix(String prefixVal) {
		this.prefixVal = prefixVal;
	}

	public boolean isTerminal() {
		return terminal;
	}

	public void setTerminal(boolean terminal) {
		this.terminal = terminal;
	}

	public Integer getChild(Character c) {
		int index = childrenKey.indexOf(c);
		if (index < 0) {
			return null;
		} else {
			return childrenValue.get(index);
		}
	}

	public void putChild(Character c, Integer id) {
		childrenKey.add(c);
		childrenValue.add(id);
	}
	
	public Collection<Integer> getChildren() {
		return childrenValue;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public void addChildrenKey(Character key) {
		childrenKey.add(key);
	}
	
	public void addChildrenValue(Integer value) {
		childrenValue.add(value);
	}
	
	public Record getValue() {
		return value;
	}
	
	public void setValue(Record value) {
		this.value = value;
	}
	
	private static byte[] toByteArray(ByteBuffer buffer) {
		buffer.flip();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return bytes;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}
	
	public void toggleExpanded() {
		this.expanded = !this.expanded;
	}
}
