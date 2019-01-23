package com.fom.defaulter;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

import com.fom.context.Context;
import com.fom.context.exception.WarnException;
import com.fom.context.executor.Executor;
import com.fom.context.executor.ZipDownloader;
import com.fom.context.executor.helper.downloader.DownloaderHelper;
import com.fom.context.executor.helper.downloader.HdfsDownloaderHelper;
import com.fom.util.HdfsUtil;

/**
 * 
 * @author shanhm
 * @date 2018年12月23日
 *
 */
public class HdfsZipDownloader<E extends HdfsZipDownloaderConfig> extends Context<E> {

	private DownloaderHelper helper;

	protected HdfsZipDownloader(String name, String path) {
		super(name, path);
	}

	@Override
	protected final void exec(E config) throws Exception {
		List<String> pathList = HdfsUtil.listPath(config.getFs(), uri, new PathFilter(){
			@Override
			public boolean accept(Path path) {
				if(StringUtils.isBlank(config.getSignalFileName())){
					return true;
				}
				return ! config.getSignalFileName().equals(path.getName());
			}
		}); 
		helper = new HdfsDownloaderHelper(config.getFs(), config.isDelSrc());
		Executor executor = new ZipDownloader(name, pathList, config, helper);
		executor.exec();
	}

	@Override
	protected void onComplete(final E config) throws Exception{ 
		if(config.isDelSrc() && !helper.delete(uri)){
			throw new WarnException("删除源目录失败."); 
		}
	}
}