package com.fom.context;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.fom.util.XmlUtil;

/**
 * 
 * @author X4584
 * @date 2018年12月12日
 *
 */
public class ZipImporterConfig extends ImporterConfig {

	protected ZipImporterConfig(String name) {
		super(name);
	}

	String subReg;

	Pattern subPattern;
	
	@Override
	void load() throws Exception {
		super.load();
		subReg = XmlUtil.getString(element, "importer.zip.subPattern", "");
		if(!StringUtils.isBlank(subReg)){
			subPattern = Pattern.compile(subReg);
		}
	}
	
	public final boolean matchZipFile(String fileName){
		if(subPattern == null){
			return true;
		}
		return subPattern.matcher(fileName).find();
	}

}
