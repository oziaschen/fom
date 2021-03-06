package com.fom.context.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.fom.util.IoUtil;

/**
 * BufferedReader适配，读取普通文本文件
 * 
 * @author shanhm
 *
 */
public class TextReader implements Reader {
	
	private BufferedReader reader;
	
	/**
	 * 
	 * @param sourceUri sourceUri
	 * @throws Exception Exception
	 */
	public TextReader(String sourceUri) throws Exception {
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(sourceUri)),"UTF-8"));
	}
	
	/**
	 * 
	 * @param file file
	 * @throws Exception Exception
	 */
	public TextReader(File file) throws Exception {
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
	}
	
	/**
	 * 
	 * @param inputStream inputStream
	 * @throws Exception Exception
	 */
	public TextReader(InputStream inputStream) throws Exception {
		reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
	}

	@Override
	public String readLine() throws Exception {
		return reader.readLine();
	}

	@Override
	public void close() throws IOException {
		IoUtil.close(reader);
	}

}
