package com.fom.context;

/**
 * 
 * @author shanhm1991
 *
 */
public class UploaderConfig extends Config {

	protected UploaderConfig(String name) {
		super(name);
	}

	@Override
	void load() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public final String getType() {
		return TYPE_UPLOADER;
	}

	@Override
	public final String getTypeName() {
		return NAME_UPLOADER;
	}

}