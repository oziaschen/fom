package com.fom.context;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.quartz.CronExpression;

import com.fom.util.XmlUtil;
import com.fom.util.log.LoggerFactory;

/**
 * 
 * @author shanhm1991
 *
 */
public abstract class Config implements IConfig {
	
	protected static final Logger LOG = LoggerFactory.getLogger("config");

	protected final String name;

	Element element;

	ConfigLoader loader;

	protected Config(String name){
		this.name = name;
	}

	public final String toXml(){
		return element.asXML();
	}

	public final File locationResource(String location) throws IOException{ 
		return loader.getResource(location).getFile();
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
		srcPath = XmlUtil.getString(element, "src.path", "");
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

	protected void load(Element extendedElement) throws Exception {

	}

	@SuppressWarnings("rawtypes")
	Scanner scanner;

	Pattern pattern;

	private CronExpression cronExpression;

	@SuppressWarnings("rawtypes")
	boolean valid() throws Exception {
		if(!StringUtils.isBlank(reg)){
			pattern = Pattern.compile(reg);
		}
		cronExpression = new CronExpression("0 0/1 * * * ?");
		Class<?> clzz = Class.forName(scannerClzz);
		Constructor<?> constructor = clzz.getConstructor(String.class, Config.class);
		scanner = (Scanner)constructor.newInstance(name, this);

		return isValid(element.element("extended"));
	}

	protected boolean isValid(Element extendedElement) throws Exception {
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
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
		Date nextDate = cronExpression.getNextValidTimeAfter(new Date());
		return nextDate.getTime() - System.currentTimeMillis();
	}
	
	public final boolean matchSrc(String srcName){
		if(pattern == null){
			return true;
		}
		return pattern.matcher(srcName).find();
	}
}