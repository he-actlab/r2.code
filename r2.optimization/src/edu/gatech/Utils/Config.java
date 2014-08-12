package edu.gatech.Utils;

public final class Config {
	
	public final String inJar;
	public final String outJar;
	public final String libJar;
	
	public String appName;
	public String bitVector;

	private static Config config;
	
	public static Config g() {
		if (config == null)
			config = new Config();
		return config;
	}
	
	private Config() {
		inJar = System.getProperty("ExpaxInst.in.jar", "input/old.jar");
		outJar = System.getProperty("ExpaxInst.out.jar", "output/new.jar");
		libJar = System.getProperty("ExpaxInst.lib.jar", ".:libs/rt.jar:libs/jce.jar:libs/soot-2.5.0.jar:libs/cfrt.jar:libs/enerj.jar:bin:libs/core.jar");
		appName = null;
		bitVector = null;
	}
}
