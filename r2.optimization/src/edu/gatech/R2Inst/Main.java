package edu.gatech.R2Inst;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.Transform;

import edu.gatech.R2Inst.Instrumentor;
import edu.gatech.Utils.Config;
import edu.gatech.R2Inst.AddUninstrClassesToJar;
import edu.gatech.Utils.Printer;

public class Main extends SceneTransformer {
	
	private static Config config;
	private static List<SootClass> classes = new ArrayList<SootClass>();
	private static Map<String,List<String>> uninstrumentedClasses = new HashMap<String, List<String>>(); 	
	private static final String dummyMainClassName = "edu.gatech.Utils.DummyMain"; 	

	public static void main(String[] args) {
		config = Config.g();
		config.appName = args[0];
		config.bitVector = args[1];
		config.mode = args[2];
		
		Scene.v().setSootClassPath(config.inJar + File.pathSeparator + config.libJar);
		
		loadClassesToInstrument();
		
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.ExpaxInst", new Main())); // whole jimple transformation pack
		StringBuilder builder = new StringBuilder();
		builder.append("-w -p cg off -keep-line-number -keep-bytecode-offset ");
		builder.append("-soot-classpath ");
		builder.append(config.inJar + File.pathSeparator + config.libJar + " ");
		builder.append("-outjar -d ");
		builder.append(config.outJar + " ");
		builder.append(dummyMainClassName);
		
		String[] sootArgs = builder.toString().split(" ");
		soot.Main.main(sootArgs);
		
		new AddUninstrClassesToJar(uninstrumentedClasses, config.outJar).apply();
	}
	
	@Override
	protected void internalTransform(String arg0, Map arg1) {
		printClasses("old.txt");
		Instrumentor inst = new Instrumentor(config.bitVector, config.mode);
		inst.instrument(classes);
		Scene.v().getApplicationClasses().remove(Scene.v().getSootClass(dummyMainClassName));
		printClasses("new.txt");
	}
	
	private static void printClasses(String fileName) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(fileName));
			for (SootClass klass : classes) {
				Printer.v().printTo(klass, out);
			}
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}
	
	private static void loadClassesToInstrument() {
		for (String jarName : config.inJar.split(File.pathSeparator)) {
			if (jarName.endsWith(".jar")) {
				System.out.println("jarName " + jarName);
				JarFile jar = null;
				try {
					jar = new JarFile(jarName);
				} catch(IOException e) {
					throw new RuntimeException(e.getMessage() + " " + jarName);
				}
				for (Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements();) {
					JarEntry entry = e.nextElement();
					String name = entry.getName();
					if (name.endsWith(".class")) {
						name = name.replace(".class", "").replace(File.separatorChar, '.');
						if(!toBeInstrumented(name)){ // config 
							System.out.println("Skipped instrumentation of class: " + name);
							addUninstrumentedClass(jarName, name);
							continue;
						}
						try{
							SootClass klass = Scene.v().loadClassAndSupport(name);
							classes.add(klass);
						} catch(RuntimeException ex) {
							System.out.println("Failed to load class: " + name);
							if (ex.getMessage().startsWith("couldn't find class:")) {
								System.out.println(ex.getMessage());
							}
						}
					}
				}
				try {
					jar.close();
				} catch (IOException e) {
					throw new RuntimeException(e.getMessage());
				}
			}
		}
	}

	private static boolean toBeInstrumented(String className)
	{
//		if (config.appName.equals("fft") || config.appName.equals("sor") || config.appName.equals("smm") || config.appName.equals("mc") || config.appName.equals("lu")) {
//			// pick a class given as argument
//			if (!className.contains(config.appName)){
//				return false;
//			}
//			return true;
//		}
		return true;
	}
	
	private static void addUninstrumentedClass(String jarName, String className) {
		List<String> cs = uninstrumentedClasses.get(jarName);
		if (cs == null) {
			cs = new ArrayList<String>();
			uninstrumentedClasses.put(jarName, cs);
		}
		cs.add(className);
	}	
}
