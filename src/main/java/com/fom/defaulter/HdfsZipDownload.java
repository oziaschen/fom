package com.fom.defaulter;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

import com.fom.context.Context;
import com.fom.context.Executor;
import com.fom.context.executor.ZipDownloader;
import com.fom.context.helper.HdfsZipDownloaderHelper;
import com.fom.context.helper.ZipDownloaderHelper;
import com.fom.util.HdfsUtil;
import com.fom.util.ScanUtil;

/**
 * 
 * @author shanhm
 *
 */
public final class HdfsZipDownload extends Context<HdfsZipDownloadConfig> {

	protected HdfsZipDownload(String name) {
		super(name);
	}

	@Override
	protected List<String> scan(String srcUri, HdfsZipDownloadConfig config) throws Exception {
		return ScanUtil.scan(config.getFs(), srcUri, config.getPattern(), config.getSignalFileName());
	}

	@Override
	protected Executor createExecutor(String sourceUri, HdfsZipDownloadConfig config) throws Exception {
		HdfsZipDownloaderHelper helper = new HdfsZipDownloaderHelper(config.getFs());
		List<String> pathList = HdfsUtil.listPath(config.getFs(), sourceUri, new PathFilter(){
			@Override
			public boolean accept(Path path) {
				if(StringUtils.isBlank(config.getSignalFileName())){
					return true;
				}
				return ! config.getSignalFileName().equals(path.getName());
			}
		});  
		
		String sourceName = new File(sourceUri).getName();
		HdfsZipDownloader zipDownloader = new HdfsZipDownloader(name, sourceName, pathList, config.getDestPath(), 
				config.getEntryMax(), config.getSizeMax(), config.isDelSrc(), helper, sourceUri);
		return zipDownloader;
	}
	
	private static class HdfsZipDownloader extends ZipDownloader { 
		
		private final boolean isDelSrc;
		
		private final String sourceUri;

		public HdfsZipDownloader(String name, String zipName, List<String> uriList, String destPath, 
				int zipEntryMax, long zipSizeMax, boolean isDelSrc, ZipDownloaderHelper helper, String sourceUri) {
			super(name, zipName, uriList, destPath, zipEntryMax, zipSizeMax, isDelSrc, helper);
			this.isDelSrc = isDelSrc;
			this.sourceUri = sourceUri;
		}
		
		@Override
		public void callback(boolean result) throws Exception {
			if(result && isDelSrc && !helper.delete(sourceUri)){
				log.error("删除源目录失败."); 
			}
		}
	}
	
}
