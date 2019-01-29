package com.fom.examples;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

import com.fom.context.Context;
import com.fom.context.Executor;
import com.fom.context.FomContext;
import com.fom.context.ResultHandler;
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
@FomContext(remark="扫描下载Hdfs指定目录下的目录并打包成zip")
public class DownloadHdfsZipExample extends Context {

	private FileSystem fs;

	private String srcPath = "/test";

	private String destPath = "${webapp.root}/download"; 

	private boolean isDelSrc = false; 

	private int entryMax = 10;

	private long sizeMax = 100 * 1024 * 1024;
	
	private String signalName;
	
	public DownloadHdfsZipExample() throws IOException{ 
		String hdfsMaster = "";
		String hdfsSlave = "";
		fs = HdfsUtil.getFileSystem(hdfsMaster, hdfsSlave);
	}

	@Override
	protected List<String> getUriList() throws Exception {
		String hdfsMaster = "";
		String hdfsSlave = "";
		fs = HdfsUtil.getFileSystem(hdfsMaster, hdfsSlave);
		return ScanUtil.scan(fs, srcPath, null, null);
	}

	@Override
	protected Executor createExecutor(String sourceUri) throws Exception {
		HdfsZipDownloaderHelper helper = new HdfsZipDownloaderHelper(fs);
		List<String> pathList = HdfsUtil.listPath(fs, sourceUri, new PathFilter(){
			@Override
			public boolean accept(Path path) {
				if(StringUtils.isBlank(signalName)){
					return true;
				}
				return ! signalName.equals(path.getName());
			}
		});  

		String sourceName = new File(sourceUri).getName();
		Handler handler = new Handler(sourceUri, isDelSrc, helper);
		ZipDownloader zipDownloader = new ZipDownloader(sourceName, pathList, destPath, 
				entryMax, sizeMax, isDelSrc, helper, handler);
		return zipDownloader;

	}

	private class Handler implements ResultHandler {

		private String sourceUri;

		private boolean isDelSrc;

		private ZipDownloaderHelper helper;

		public Handler(String sourceUri, boolean isDelSrc, ZipDownloaderHelper helper){
			this.sourceUri = sourceUri;
			this.isDelSrc = isDelSrc;
			this.helper = helper;
		}

		@Override
		public void handle(boolean result) throws Exception { 
			if(result && isDelSrc && !helper.delete(sourceUri)) {
				DownloadHdfsZipExample.this.log.error("删除源目录失败."); 
			}
		} 
	}

}