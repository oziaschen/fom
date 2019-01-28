package com.fom.context;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.stereotype.Service;

import com.fom.util.XmlUtil;

/**
 * 
 * @author shanhm
 *
 */
@Service(value="fomService")
public class FomServiceImpl implements FomService {
	
	@Override
	public Map<String, Map<String,String>> list() {
		Map<String, RuntimeConfig> cmap = ConfigManager.getMap();
		DateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm:ss SSS");
		Map<String, Map<String,String>> map = new HashMap<>();
		for(Entry<String, RuntimeConfig> entry : cmap.entrySet()){
			Map<String,String> m = new LinkedHashMap<>();
			map.put(entry.getKey(), m);
			RuntimeConfig c = entry.getValue();
			m.put("lastLoad", format.format(c.loadTime));
//			if(c.isRunning){
//				m.put("state", "Running");
//			}else{
//				m.put("state", "Dead");
//			}
//			if(c.startTime == 0){
//				m.put("lastStart", "not started");
//			}else{
//				m.put("lastStart", format.format(c.startTime));
//			}
		}
		return map;
	}

	@Override
	public String detail(String name){
		RuntimeConfig config = ConfigManager.get(name);
		return config.toString();
	}

	@Override
	public String xml(String name) throws Exception{
		RuntimeConfig config = ConfigManager.get(name);
		SAXReader reader = new SAXReader();
		StringReader in=new StringReader(config.getXml());  
		Document doc=reader.read(in); 
		OutputFormat formater=OutputFormat.createPrettyPrint();  
		formater.setEncoding("UTF-8");  
		StringWriter out=new StringWriter();  
		XMLWriter writer=new XMLWriter(out,formater);
		writer.write(doc);  
		writer.close();  
		return out.toString();
	}
	
	@Override
	public String apply(String name, String data) throws Exception {
		SAXReader reader = new SAXReader();
		reader.setEncoding("UTF-8");
		StringReader in=new StringReader(data);  
		Document doc = reader.read(in); 
		Element element = doc.getRootElement();
		
		RuntimeConfig config = ConfigManager.load(element);
		if(!name.equals(config.name)){ 
			return "failed, [" + name + "]config.name can not be change.";
		}
		if(!config.valid){
			return "failed, [" + name + "]config not valid.";
		}
		
		RuntimeConfig oldConfig = ConfigManager.get(name);
		if(!config.getClass().getName().equals(oldConfig.getClass().getName())){
			return "failed, [" + name + "]config.class can not be change.";
		}
		if(config.equals(oldConfig)){ 
			return "failed, [" + name + "]config not changed.";
		}

		File apply = new File(System.getProperty("config.apply"));
		for(File file : apply.listFiles()){
			if(file.getName().startsWith(name + ".xml.") && !file.delete()){
				throw new RuntimeException("删除文件失败:" + file.getName());
			}
		}
		File xml = new File(apply + File.separator + name + ".xml." + config.loadTime);
		XmlUtil.writeDocToFile(doc, xml);
		FileUtils.copyFile(xml, new File(apply + File.separator + "history" + File.separator + xml.getName()));
		
		ConfigManager.register(config); 
		return "success, " + name + " update applyed.";
	}
	
	@Override
	public String stop(String name){
		RuntimeConfig config = ConfigManager.get(name);
		if(config == null){
			return "failed, " + name + " not exist.";
		}
		if(!config.valid){
			return "failed, " + name + " not valid.";
		}
//		if(!config.isRunning){
//			return "failed, " + name + " was not Running.";
//		}
//		config.isRunning = false;
//		config.scanner.interrupt();
		return "success, " + name + " stoped.";
	}
	
	@Override
	public String stopAll(){
		Map<String, RuntimeConfig> cmap = ConfigManager.getMap();
		StringBuilder builder = new StringBuilder();
		for(Entry<String, RuntimeConfig> entry : cmap.entrySet()){
			RuntimeConfig config = entry.getValue();
			if(!config.valid){
				builder.append("failed, " + config.name + " not valid.<br>");
				continue;
			}
//			if(!config.isRunning){
//				builder.append("failed, " + config.name + " was not Running.<br>");
//				continue;
//			}
//			config.isRunning = false;
//			config.scanner.interrupt();
			builder.append("success, " + config.name + " stoped.<br>");
		}
		return builder.toString();
	}
	
	@Override
	public String start(String name) throws Exception {
		RuntimeConfig config = ConfigManager.get(name);
		if(config == null){
			return "failed, " + name + " not exist.";
		}
//		if(config.isRunning){
//			return "failed, " + name + " was already Running.";
//		}
//		if(config.scanner.isAlive()){
//			return "failed, " + name + " was still alive, please try later.";
//		}
//		config.isRunning = true;
//		config.refreshScanner();
//		config.scanner.start();
//		config.startTime = System.currentTimeMillis();
		return "success, " + name + " started.";
	}

	@Override
	public String startAll() throws Exception {
		Map<String, RuntimeConfig> cmap = ConfigManager.getMap();
		StringBuilder builder = new StringBuilder();
		for(Entry<String, RuntimeConfig> entry : cmap.entrySet()){
			RuntimeConfig config = entry.getValue();
//			if(config.isRunning){
//				builder.append("failed, " + config.name + " was already Running.<br>");
//				continue;
//			}
//			if(config.scanner.isAlive()){
//				builder.append("failed, " + config.name + " was still alive, please try later.<br>");
//				continue;
//			}
//			config.isRunning = true;
//			config.refreshScanner();
//			config.scanner.start();
//			config.startTime = System.currentTimeMillis();
			builder.append("success, " + config.name + " started.<br>");
		}
		return builder.toString();
	}

	@Override
	public String restart(String name) throws Exception {
		RuntimeConfig config = ConfigManager.get(name);
		if(config == null){
			return "failed, " + name + " not exist.";
		}
//		if(!config.isRunning){
//			return "failed, " + name + " was not Running.";
//		}
//		config.scanner.interrupt();
//		config.startTime = System.currentTimeMillis();
		return "success, " + name + " restarted.";
	}
	
	@Override
	public String restartAll() {
		Map<String, RuntimeConfig> cmap = ConfigManager.getMap();
		StringBuilder builder = new StringBuilder();
		for(Entry<String, RuntimeConfig> entry : cmap.entrySet()){
			RuntimeConfig config = entry.getValue();
//			if(!config.isRunning){
//				builder.append("failed, " + config.name + " was not Running.<br>");
//				continue;
//			}
//			config.scanner.interrupt();
//			config.startTime = System.currentTimeMillis();
			builder.append("success, " + config.name + " restarted.<br>");
		}
		return builder.toString();
	}

}
