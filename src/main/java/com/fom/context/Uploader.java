package com.fom.context;

/**
 * 
 * @author shanhm1991
 *
 * @param <E>
 */
public class Uploader<E extends UploaderConfig> extends Executor<E> {

	protected Uploader(String name, String path) {
		super(name, path);
	}

	@Override
	void execute() throws Exception {
		// TODO Auto-generated method stub
	}

}