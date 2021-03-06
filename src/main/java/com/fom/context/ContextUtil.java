package com.fom.context;

import javax.servlet.ServletContext;

public class ContextUtil {
	
	private static volatile ServletContext scontext;

	public static void setContext(ServletContext context) {
		if(scontext == null){
			scontext = context;
		}
	}

	/**
	 * Return the real path for a given virtual path, if possible; otherwise return <code>null</code>.
	 * @param path path
	 * @return context location
	 */
	public static String getContextPath(String path) {
		return scontext.getRealPath(getEnvStr(path));
	}

	/**
	 * 解析带环境变量的字符串值，如${webapp.root}/test
	 * @param val string
	 * @return string
	 * @throws IllegalArgumentException IllegalArgumentException
	 */
	public static String getEnvStr(String val) throws IllegalArgumentException {
		String DELIM_START = "${";
		char   DELIM_STOP  = '}';
		int DELIM_START_LEN = 2;
		int DELIM_STOP_LEN  = 1;
		StringBuffer buffer = new StringBuffer();
		int i = 0;
		int j, k;
		while(true) {
			j = val.indexOf(DELIM_START, i);
			if(j == -1) {
				if(i==0) {
					return val;
				} else { 
					buffer.append(val.substring(i, val.length()));
					return buffer.toString();
				}
			} else {
				buffer.append(val.substring(i, j));
				k = val.indexOf(DELIM_STOP, j);
				if(k == -1) {
					throw new IllegalArgumentException('"' 
							+ val + "\" has no closing brace. Opening brace at position " + j + '.');
				} else {
					j += DELIM_START_LEN;
					String key = val.substring(j, k);
					String replacement = System.getProperty(key);
					if(replacement != null) {
						String recursiveReplacement = getEnvStr(replacement);
						buffer.append(recursiveReplacement);
					}
					i = k + DELIM_STOP_LEN;
				}
			}
		}
	}

}
