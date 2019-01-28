package com.fom.context;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.quartz.CronExpression;

import com.fom.log.LoggerFactory;
import com.fom.util.XmlUtil;

/**
 * cron   执行时机的定时表达式<br>
 * pattern 匹配资源名称的正则表达式<br>
 * thread.min   处理线程最小数<br>
 * thread.max   处理线程最大数<br>
 * thread.aliveTime  处理线程空闲存活最长时间<br>
 * thread.overTime   处理线程执行超时时间<br>
 * thread.cancellable    处理线程执行超时是否中断<br>
 * 
 * @author shanhm
 *
 */
public abstract class RuntimeConfig {

	protected static final Logger LOG = LoggerFactory.getLogger("context"); 

	protected final String name;

	Element element;

	String regex;

	String cron;

	int threadCore;

	int threadMax;

	int threadAliveTime;

	int threadOverTime;

	boolean cancellable;
	
	private Pattern pattern;

	private CronExpression cronExpression;

	boolean valid;

	long loadTime;

	protected RuntimeConfig(String name){
		this.name = name;
	}

	void load() throws Exception {
		cron = load("cron", "");
		regex = load("pattern", "");
		threadCore = load("thread.core", 4, 1, 10); 
		threadMax = load("thread.max", 20, 10, 50);
		threadAliveTime = load("thread.aliveTime", 30, 3, 300);
		threadOverTime = load("thread.overTime", 3600, 300, 86400);
		cancellable = load("thread.cancellable", false); 
		
		if(!StringUtils.isBlank(regex)){
			pattern = Pattern.compile(regex);
		}
		if(!StringUtils.isBlank(cron)){
			cronExpression = new CronExpression(cron);
		}
		
		loadExtends();
		
		valid = valid();
	}
	
	/**
	 * 子类自定义配置加载
	 * @throws Exception
	 */
	protected abstract void loadExtends() throws Exception;

	/**
	 * 子类自定义配置校验，默认返回true
	 * @return
	 * @throws Exception
	 */
	protected boolean valid() throws Exception {
		return true;
	}

	public final CronExpression getCron(){
		return cronExpression;
	}

	public final Pattern getPattern(){
		return pattern;
	}

	public final String getXml(){
		return element.asXML();
	}

	/**
	 * 加载String配置
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	protected final String load(String key, String defaultValue) {
		String value =  XmlUtil.getString(element, key, defaultValue);
		entryMap.put(key, value);
		return value;
	}

	/**
	 * 加载int配置
	 * @param key
	 * @param defaultValue
	 * @param min
	 * @param max
	 * @return
	 */
	protected final int load(String key, int defaultValue, int min, int max){
		int value = XmlUtil.getInt(element, key, defaultValue, min, max);
		entryMap.put(key, String.valueOf(value)); 
		return value;
	}

	/**
	 * 加载long配置
	 * @param key
	 * @param defaultValue
	 * @param min
	 * @param max
	 * @return
	 */
	protected final long load(String key, long defaultValue, long min, long max){
		long value = XmlUtil.getLong(element, key, defaultValue, min, max);
		entryMap.put(key, String.valueOf(value)); 
		return value;
	}

	/**
	 * 加载boolean配置
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	protected final boolean load(String key, boolean defaultValue){
		boolean value = XmlUtil.getBoolean(element, key, defaultValue);
		entryMap.put(key, String.valueOf(value)); 
		return value;
	}

	private Map<String,String> entryMap = new LinkedHashMap<>();

	@Override
	public final boolean equals(Object object){
		if(!(object instanceof RuntimeConfig)){
			return false;
		}
		if(object == this){
			return true;
		}
		RuntimeConfig config = (RuntimeConfig)object;
		return entryMap.equals(config.entryMap);
	}

	@Override
	public final String toString() {
		StringBuilder builder = new StringBuilder();
		Iterator<Entry<String, String>> it = entryMap.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, String> entry = it.next();
			builder.append("\n" + entry.getKey() + "=" + entry.getValue());
		}
		return builder.toString();
	}

}