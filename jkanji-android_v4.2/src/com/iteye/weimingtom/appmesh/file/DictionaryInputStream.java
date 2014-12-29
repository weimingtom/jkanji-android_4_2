package com.iteye.weimingtom.appmesh.file;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.iteye.weimingtom.appmesh.dictionary.Node;
import com.iteye.weimingtom.appmesh.dictionary.Record;

public class DictionaryInputStream implements DictionaryInput {
	
	private static final boolean TEST_LOAD_ALL_NODE = false;
	protected static final boolean USE_BUFFERD_INPUTSTREAM = false;
	protected static final int BUFFER_INPUTSTREAM_SIZE = 1024 * 1024 * 50;
	
	//TODO: Android mod
	protected String filename;
	protected InputStream istr;
	private int nodeListSize = 0;
	private int indexBlockSize = 0;

	public DictionaryInputStream() {
		
	}
	
	public DictionaryInputStream(String filename) throws IOException {
		this.filename = filename;
		reset();
		init();
	}
	
	/**
	 * @see DictionaryReader#dumpFile(String, boolean)
	 * @throws IOException
	 */
    protected void init() throws IOException {
    	//test();
    	nodeListSize = readNodeListSize();
    	indexBlockSize = 4 + nodeListSize * 6;
//    	trace("nodeListSize = " + nodeListSize);
//    	trace("indexBlockSize = " + indexBlockSize);
    	
    	if (TEST_LOAD_ALL_NODE) {
	    	for (int i = 0; i < nodeListSize; i++) {
// 		   		trace("nodeList [" + i + "] = " + readPositionById(i));
	    		readNodeById(i);
	    	}
    	}
    	
    }

    public void skip(int n) throws IOException {
		if (istr!= null) {
			istr.skip(n);
		}
    }
    
    public void reset() throws IOException {
    	if (istr != null && istr.markSupported()) {
	    	istr.reset();
    	} else {
    		if (istr != null) {
    			istr.close();
    		}
    		//TODO: Android mod
    		//Exception in thread "main" java.io.IOException: Resetting to invalid mark
    		InputStream inputStream;
    		if (USE_BUFFERD_INPUTSTREAM) {
    			inputStream = new BufferedInputStream(new FileInputStream(filename), BUFFER_INPUTSTREAM_SIZE);
    		} else {
    			inputStream = new FileInputStream(filename);
    		}
    		istr = inputStream;
    		if (istr.markSupported()) {
        		istr.mark(0);
    		}
    	}
    }
    
    /**
     * @see Node#toByteBuffer()
     * @param id
     * @return
     * @throws IOException 
     */
    public Node readNodeById(int id) throws IOException {
    	int position = readPositionById(id);
    	char character = readCharById(id);
    	Node node = new Node();
    	node.setId(id);
    	node.setChar(character);
    	if (istr != null) {
    		reset();
    		skip(indexBlockSize + position);
    		node.setTerminal(getByte8() != 0 ? true : false);
    		int childSize = this.getInt32();
    		for (int i = 0; i < childSize; i++) {
    			char key = this.getChar16();
//    			trace("--key[" + i + "] == " + key);
    			node.addChildrenKey(key);
    		}
    		for (int i = 0; i < childSize; i++) {
    			int value = this.getInt32();
//    			trace("--value[" + i + "] == " + value);
        		node.addChildrenValue(value);
    		}
			int bytesStrLength = this.getInt32();
    		byte[] bytesStr = new byte[bytesStrLength];
    		for (int i = 0; i < bytesStrLength; i++) {
    			bytesStr[i] = this.getByte8();
    		}
    		String prefixVal = new String(bytesStr, "UTF-8");
    		node.setPrefix(prefixVal);
    		
    		//FIXME:Record
    		int bytesValueLength = this.getInt32();
    		if (bytesValueLength > 0) {
	    		byte[] bytesValue = new byte[bytesValueLength];
	    		for (int i = 0; i < bytesValueLength; i++) {
	    			bytesValue[i] = this.getByte8();
	    		}
	    		Record record = new Record();
	    		ByteBuffer recordBuffer = ByteBuffer.wrap(bytesValue);
	    		record.fromByteBuffer(recordBuffer);
	    		node.setValue(record);
    		}
    		
    		//trace(node.toString());
    		
    		return node;
    	}
    	return null;
    }

    /**
     * @see DictionaryReader#toByteBufferIndex(boolean isLargeFile)
     * @return
     * @throws IOException
     */
    public int readNodeListSize() throws IOException {
    	if (istr != null) {
    		reset();
    		skip(0);
    		return getInt32();
    	}
    	return 0;
    }
    
    /**
     * @see DictionaryReader#toByteBufferIndex(boolean isLargeFile)
     * @param id
     * @return
     * @throws IOException
     */
    public int readPositionById(int id) throws IOException {
    	if (istr != null) {
    		reset();
    		skip(4 + id * 6);
    		return getInt32();
    	}
    	return 0;    	
    }

    /**
     * @see DictionaryReader#toByteBufferIndex(boolean isLargeFile)
     * @param id
     * @return
     * @throws IOException
     */
    public char readCharById(int id) throws IOException {
    	if (istr != null) {
    		reset();
    		skip(4 + id * 6 + 4);
    		return getChar16();
    	}
    	return 0;    	
    }
    
    public byte getByte8() throws IOException {
    	return (byte)istr.read();
    }

    public char getChar16() throws IOException {
    	int result = 0;
		result |= (istr.read() & 0xff) << 0;
		result |= (istr.read() & 0xff) << 8;
		return (char)result;
    }
    
    public int getInt32() throws IOException {
    	int result = 0;
		result |= (istr.read() & 0xff) << 0;
		result |= (istr.read() & 0xff) << 8;
		result |= (istr.read() & 0xff) << 16;
		result |= (istr.read() & 0xff) << 24;
    	return result;
    }
    
    public void test() {
    	byte[] bytes = new byte[16];
    	try {
    		skip(1);
    		istr.read(bytes);
	    	trace(Arrays.toString(bytes));
	    	reset();
	    	istr.read(bytes);
	    	trace(Arrays.toString(bytes));
	    	reset();
	    	istr.read(bytes);
	    	trace(Arrays.toString(bytes));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	@Override
	public Node getRoot() {
		try {
			return readNodeById(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Node getNodeById(Integer id) {
		if (id != null) {
			try {
				return readNodeById(id);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void dumpTree() {
		//no implements
	}

	@Override
	public void dumpNodeList(boolean isLargeFile) {
		//no implements
	}

	@Override
	public void dumpFile(String filename, boolean isLargeFile) throws IOException {
		//no implements
	}
	
	private void trace(String str) {
		//TODO: Android mod
		System.out.println(str);
	}
}
