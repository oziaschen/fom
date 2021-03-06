package com.fom.context.reader;

import java.io.Closeable;

/**
 * 
 * 为ParseTask以及ZipParseTask定制的一个读取适配器
 * 
 * @author shanhm
 *
 */
public interface Reader extends Closeable {
	
	/**
	 * 读取下一行
	 * @return 行内容字符串
	 * @throws Exception Exception
	 */
	String readLine() throws Exception;
	
}
