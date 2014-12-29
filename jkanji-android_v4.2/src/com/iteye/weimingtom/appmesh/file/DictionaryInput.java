package com.iteye.weimingtom.appmesh.file;

import java.io.IOException;

import com.iteye.weimingtom.appmesh.dictionary.Node;

public interface DictionaryInput {
	public Node getRoot();
	
	public Node getNodeById(Integer id);
	
	public void dumpTree();
	
	public void dumpNodeList(boolean isLargeFile);
	
	public void dumpFile(String filename, boolean isLargeFile) throws IOException;
}
