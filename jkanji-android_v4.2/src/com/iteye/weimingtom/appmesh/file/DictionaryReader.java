package com.iteye.weimingtom.appmesh.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.iteye.weimingtom.appmesh.dictionary.Node;
import com.iteye.weimingtom.appmesh.dictionary.Record;

/**
 * 
 * A file reader that reads a list of newline delimited words into a Dictionary.
 *
 */
public class DictionaryReader implements DictionaryInput {
	private static final boolean RENAME_REPEAT = true;
	private static final int BYTES_MAX = 1024 * 1024 * 30;
	
	private ArrayList<Node> nodeList = new ArrayList<Node>();
	private Node root = new Node();
    
	private Scanner scanner;
	private Map<String, Integer> map = new HashMap<String, Integer>();

	/**
     * Create a new dictionary reader for the file at fileLoc.
     * @param fileLoc
	 * @throws FileNotFoundException 
     */
    public DictionaryReader(String fileLoc) throws FileNotFoundException {
        this.scanner = new Scanner(new File(fileLoc));
		root.setTerminal(false);
		nodeList.add(root);
		init();
    }
    
    /**
     * Fill the Dictionary with all words in the file.
     * @param dictionary
     */
    private void init() {
        while (this.scanner.hasNextLine()) {
        	this.addWord(scanner.nextLine());
        }
    }
    
    @Override
	public Node getRoot() {
		return root;
	}
	
    @Override
	public Node getNodeById(Integer id) {
		if (id == null) {
			return null;
		} else {
			return nodeList.get(id);
		}
	}
	
	private void addWord(String line) {
		Record record = new Record();
		record.parse(line);
		if (RENAME_REPEAT) {
			Integer ir = this.map.get(record.getRomaji());
			if (ir == null) {
				this.map.put(record.getRomaji(), Integer.valueOf(1));
			} else {
				System.err.println("repeat : " + ir + " , " + record.getRomaji());
				record.setRomaji(record.getRomaji() + ir.intValue());
				this.map.put(record.getRomaji(), Integer.valueOf(ir.intValue() + 1));
			}
		}
		insert(root, record.getRomaji(), 0, record);
	}
	
	private void insert(Node currNode, String key, int pos, Record value) {
		Character c = key.charAt(pos);
		Node nextNode = getNodeById(currNode.getChild(c));
		if (nextNode == null) {
			nextNode = makeNode(c);
			if (pos < key.length() - 1) {
				insert(nextNode, key, pos + 1, value);
			} else {
				nextNode.setPrefix(key);
				nextNode.setValue(value);
				nextNode.setTerminal(true);
			}
			currNode.putChild(c, nextNode.getId());
		} else {
			if (pos < key.length() - 1) {
				insert(nextNode, key, pos + 1, value);
			} else {
				
			}
		}
	}
	
	private Node makeNode(Character c) {
		Node node = new Node();
		node.setId(nodeList.size());
		node.setChar(c);
		nodeList.add(node);
		return node;
	}
	
	@Override
	public void dumpTree() {
		dump(root, 0);
	}
	
	private ByteBuffer toByteBufferIndex(boolean isLargeFile) {
		ByteBuffer bytes = ByteBuffer.allocate(BYTES_MAX);
		bytes.position(0);
		bytes.order(ByteOrder.LITTLE_ENDIAN);
		bytes.clear();
		
		bytes.putInt(nodeList.size()); // 4 bytes
		int position = 0;
		// 6 bytes * id
		// n = 16, 4 + n * (4 + 2) = 100
		for (Node node : nodeList) {
			bytes.putInt(position); // 4 bytes
			Character ch = node.getChar();
			if (ch != null) {
				bytes.putChar(ch); // 2 bytes
			} else {
				bytes.putChar((char)0);
			}
			int nodeBlockSize = node.toByteBuffer().position();
			if (!isLargeFile) {
				System.out.println("Index id == " + node.getId() + ", nodeBlockSize == " + nodeBlockSize);
			} else {
				if (node.getId() % 10000 == 0) {
					System.out.println("Index id == " + node.getId() + ", nodeBlockSize == " + nodeBlockSize);
				}
			}
			position += nodeBlockSize;
		}
		return bytes;
	}
	
	private ByteBuffer toByteBufferNode(int id) {
		return nodeList.get(id).toByteBuffer();
	}
	
	private int getNodeListSize() {
		return nodeList.size();
	}
	
	@Override
	public void dumpNodeList(boolean isLargeFile) {
		printBuffer(toByteBufferIndex(isLargeFile));
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			System.out.println("id == " + node.getId() + ", key == " + node.getChar());
			printBuffer(node.toByteBuffer());
		}
	}
	
	@Override
	public void dumpFile(String filename, boolean isLargeFile) throws IOException {
		OutputStream ostr = new FileOutputStream(filename);
		byte[] indexBytes = toByteArray(toByteBufferIndex(isLargeFile));
		System.out.println("nodeBytes size == " + indexBytes.length);
		ostr.write(indexBytes);
		for (int i = 0; i < getNodeListSize(); i++) {
			byte[] nodeBytes = toByteArray(toByteBufferNode(i));
			if (!isLargeFile) {
				System.out.println("nodeBytes size == " + nodeBytes.length);
			} else {
				if (i % 10000 == 0) {
					System.out.println("nodeBytes id == " + i + " size == " + nodeBytes.length);
				}
			}
			ostr.write(nodeBytes);
		}
		ostr.flush();
		ostr.close();
	}
	
	private static byte[] toByteArray(ByteBuffer buffer) {
		buffer.flip();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return bytes;
	}
	
	private void dump(Node node, int level) {
		if (node != null) {
			String space = "";
			for (int i = 0; i < level; i++) {
				space += " ";
			}
			System.out.println(space + node.toString());
			for (Integer id : node.getChildren()) {
				Node child = getNodeById(id);
				dump(child, level + 1);
			}
		}
	}
	
	private static void printBuffer(ByteBuffer buffer) {
        ByteBuffer bytes = ByteBuffer.allocate(BYTES_MAX);
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        bytes.put(buffer.array(), 0, buffer.position());
        int num = bytes.position();
        System.out.println("printBuffer: " + num);
        String str = "";
        bytes.position(0);
        if (num > 0) {
            for (int i = 0; i < num; i++) {
                int value = bytes.get() & 0xFF;
                if (i % 16 == 0) {
                    String address = Integer.toHexString(i);
                    for (int k = 0; k < 8 - address.length(); k++) {
                    	str += "0";
                    }
                    str += address + ":";
                }
                if(value >= 16) {
                    str += Integer.toHexString(value) + " ";
                } else {
                    str += "0" + Integer.toHexString(value) + " ";
                }
                if (i % 16 == 15) {
                    str += '\n';
                }
            }
        }
        bytes.position(num);
        System.out.println(str);
	}
}
