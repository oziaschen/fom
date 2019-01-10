package com.fom.context.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.quartz.CronExpression;

import com.fom.context.Scanner;
import com.fom.context.log.LoggerFactory;
import com.fom.util.XmlUtil;

/**
 * <src.path>    源文件目录
 * <src.pattern> 源文件正则表达式
 * <src.match.fail.del>  源文件匹配失败是否删除
 * <scanner.cron>  扫描源目录的cron表达式
 * <scanner>       扫描源目录的实现方式
 * <executor>      处理文件的实现方式
 * <executor.min>  任务线程最小数
 * <executor.max>  任务线程最大数
 * <executor.aliveTime.seconds>  任务线程空闲存活最长时间
 * <executor.overTime.seconds>   任务线程执行超时时间
 * <executor.overTime.cancle>    任务线程执行超时是否中断
 * 
 * @author shanhm
 * @date 2018年12月23日
 *
 */
public abstract class Config implements IConfig {
	
	protected static final Logger LOG = LoggerFactory.getLogger("config");
	
	static volatile ConfigListener ConfigListener;

	protected final String name;

	Element element;

	boolean valid;
	
	long loadTime;
	
	long startTime;
	
	volatile boolean isRunning = false;

	protected Config(String name){
		this.name = name;
	}

	public final String toXml(){
		return element.asXML();
	}

	String srcPath;

	String reg;

	boolean delMatchFailFile;

	String scannerClzz;

	String scannerCron;

	String executorClzz;

	int executorMin;

	int executorMax;

	int executorAliveTime;

	int executorOverTime;

	boolean executorCancelOnOverTime;

	void load() throws Exception {
		srcPath = parseEnvStr(XmlUtil.getString(element, "src.path", ""));
		reg = XmlUtil.getString(element, "src.pattern", "");
		delMatchFailFile = XmlUtil.getBoolean(element, "src.match.fail.del", false);
		scannerClzz = XmlUtil.getString(element, "scanner", "");
		scannerCron = XmlUtil.getString(element, "scanner.cron", "");
		executorClzz = XmlUtil.getString(element, "executor", "");
		executorMin = XmlUtil.getInt(element, "executor.min", 4, 1, 10);
		executorMax = XmlUtil.getInt(element, "executor.max", 20, 10, 50);
		executorAliveTime = XmlUtil.getInt(element, "executor.aliveTime.seconds", 30, 3, 300);
		executorOverTime = XmlUtil.getInt(element, "executor.overTime.seconds", 3600, 300, 86400);
		executorCancelOnOverTime = XmlUtil.getBoolean(element, "executor.overTime.cancle", false);
		load(element.element("extended"));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("type=" + getTypeName());
		builder.append("\nvalid=" + valid);
		builder.append("\nsrc.path=" + srcPath);
		builder.append("\nsrc.pattern=" + reg);
		builder.append("\nsrc.match.fail.del=" + delMatchFailFile);
		builder.append("\nscanner=" + scannerClzz);
		builder.append("\nscanner.cron=" + scannerCron);
		builder.append("\nexecutor=" + executorClzz);
		builder.append("\nexecutor.min=" + executorMin);
		builder.append("\nexecutor.max=" + executorMax);
		builder.append("\nexecutor.aliveTime.seconds=" + executorAliveTime);
		builder.append("\nexecutor.overTime.seconds=" + executorOverTime);
		builder.append("\nexecutor.overTime.cancle=" + executorCancelOnOverTime);
		return builder.toString();
	}

	@Override
	public boolean equals(Object o){
		if(!(o instanceof Config)){
			return false;
		}
		if(o == this){
			return true;
		}
		Config c = (Config)o;
		return srcPath.equals(c.srcPath)
				&& reg.equals(c.reg)
				&& delMatchFailFile == c.delMatchFailFile
				&& scannerClzz.equals(c.scannerClzz)
				&& scannerCron.equals(c.scannerCron)
				&& executorClzz.equals(c.executorClzz)
				&& executorMin == c.executorMin
				&& executorMax == c.executorMax
				&& executorAliveTime == c.executorAliveTime
				&& executorOverTime == c.executorOverTime
				&& executorCancelOnOverTime == c.executorCancelOnOverTime;
	}

	protected void load(Element extendedElement) throws Exception {

	}

	@SuppressWarnings("rawtypes")
	Scanner scanner;

	Pattern pattern;

	private CronExpression cronExpression;

	boolean valid() throws Exception {
		if(!StringUtils.isBlank(reg)){
			pattern = Pattern.compile(reg);
		}
		cronExpression = new CronExpression(scannerCron);
		refreshScanner();
		return valid(element.element("extended"));
	}
	
	@SuppressWarnings("rawtypes")
	void refreshScanner() throws Exception{
		Class<?> clzz = Class.forName(scannerClzz);
		Constructor<?> constructor = clzz.getDeclaredConstructor(String.class, Config.class);
		constructor.setAccessible(true); 
		scanner = (Scanner)constructor.newInstance(name, this);
	}

	protected boolean valid(Element extendedElement) throws Exception {
		return true;
	}
	
	@Override
	public final boolean isRunning(){
		return isRunning;
	}
	
	@Override
	public final String getSrcPath() {
		return srcPath;
	}

	@Override
	public final int getExecutorMin() {
		return executorMin;
	}

	@Override
	public final int getExecutorMax() {
		return executorMax;
	}

	@Override
	public final int getExecutorAliveTime() {
		return executorAliveTime;
	}

	@Override
	public final int getExecutorOverTime() {
		return executorOverTime;
	}

	@Override
	public final boolean getExecutorCancelOnOverTime() {
		return executorCancelOnOverTime;
	}

	@Override
	public final String getExecutorClass() {
		return executorClzz;
	}

	public final long getCronTime(){
		Date nextDate = cronExpression.getTimeAfter(new Date());
		return nextDate.getTime() - System.currentTimeMillis();
	}

	public final boolean matchSrc(String srcName){
		if(pattern == null){
			return true;
		}
		return pattern.matcher(srcName).find();
	}
	
	/**
	 * 通过spring上下文获取配置路径的文件
	 * @param location
	 * @return
	 * @throws IOException
	 */
	public final File locationResource(String location) throws IOException{ 
		return ConfigListener.getResource(location).getFile();
	}
	
	/**
	 * 获取带环境变量的字符串值，如${webapp.root}/test
	 * @param val
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static final String parseEnvStr(String val) throws IllegalArgumentException {
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
						String recursiveReplacement = parseEnvStr(replacement);
						buffer.append(recursiveReplacement);
					}
					i = k + DELIM_STOP_LEN;
				}
			}
		}
	}
	
	public final boolean isDelMatchFailFile() {
		return delMatchFailFile;
	}
}