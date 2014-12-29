package com.iteye.weimingtom.appmesh.dictionary;

import java.util.LinkedList;
import java.util.Queue;

import com.iteye.weimingtom.appmesh.file.DictionaryInput;

public class Dictionary {
	private boolean isStop = false;
	private OnAddNodeListener onAddNodeListener = null;
	
	public void setStop(boolean value) {
		this.isStop = value;
	}
	
	public void setOnAddNodeListener(OnAddNodeListener onAddNodeListener) {
		this.onAddNodeListener = onAddNodeListener;
	}
	
	public Dictionary() {

	}
	
	public void findPrefix(String prefix, DictionaryInput input) {
		char[] ch = prefix.toCharArray();
		Node node = input.getRoot();
		for (int i = 0; i < ch.length; i++) {
			if (isStop) {
				return;
			}
			node = input.getNodeById(node.getChild(ch[i]));
			if (node == null) {
				break; 
			}
		}
		Queue<Node> searchQueue = new LinkedList<Node>();
		searchQueue.offer(node);
		Node currNode;
		while ((currNode = searchQueue.poll()) != null) {
			if (isStop) {
				return;
			}
			if (currNode.isTerminal()) {
				if (onAddNodeListener != null) {
					onAddNodeListener.onAddNode(currNode);
				}
			}
			for (Integer id : currNode.getChildren()) {
				if (isStop) {
					return;
				}
				Node nextNode = input.getNodeById(id);
				searchQueue.offer(nextNode);
			}
		}
	}
}
