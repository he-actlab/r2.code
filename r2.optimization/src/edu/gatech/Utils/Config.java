package edu.gatech.Utils;

public final class Config {
	
	public final String inJar;
	public final String outJar;
	public final String libJar;
	
	public String appName;
	public String bitVector;
	public String mode;

	private static Config config;
	
	public static Config g() {
		if (config == null)
			config = new Config();
		return config;
	}
	
	private Config() {
		inJar = System.getProperty("R2Inst.in.jar", "input/old.jar");
		outJar = System.getProperty("R2Inst.out.jar", "output/new.jar");
		libJar = System.getProperty("R2Inst.lib.jar", ".:libs/rt.jar:libs/jce.jar:libs/soot-2.5.0.jar:libs/cfrt.jar:libs/enerj.jar:bin:libs/core.jar:jars/R2Inst.jar:../r2.analysis/r2-analysis.jar");
		appName = null;
		bitVector = null;
		mode = null;
	}
}
