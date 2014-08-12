import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Scanner;

public class LOCCounter {
	private static String[] libPaths = { "/Users/jspark/projects/jdk", "/Users/jspark/projects/jdk/openjdk/jdk/src/share/classes" };
	private static String[] excludeLibs = { "com/sun/", "com/oracle/", "sun/"};
	private static String[] appPaths;
	private static long app = 0;
	private static long lib = 0;
	private static int miss = 0;

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {

		if (args.length < 2) {
			System.out.println("Usage: java LOCCOunter [inputfilepath] [srcdir1] [srcdir2] ...");
			System.exit(0);
		}

		String input = args[0];
		appPaths = new String[args.length - 1];
		for(int i = 1; i < args.length; i++)
			appPaths[i-1] = args[i];

		System.out.println("=============Processing: " + input+"=============");
		app = 0;
		lib = 0;
		miss = 0;
		Scanner sc = new Scanner(new File(input));
		while (sc.hasNext()) {
			String file = sc.nextLine().trim();
			if (!file.equals("")) {
				String files[] = file.split(" ");
				String path = files[1];
				fileCount(path);
			}
		}
		System.out.println("App: "+app);
		System.out.println("Lib: "+lib);
		System.out.println("Total: "+(app+lib));
		System.out.println("Miss: "+miss);
	}

	private static long fileCount(String path) {
		long result;
		OUT: for (String libdir : libPaths) {
			LineNumberReader lnr;
			try {
				lnr = new LineNumberReader(new FileReader(new File(libdir
						+ File.separator + path)));
				for (String exc : excludeLibs) {
					if (path.contains(exc)) {
						System.out.println("[" + path + "] is excluded");
						continue OUT;
					}
				}
				lnr.skip(Long.MAX_VALUE);
				result = lnr.getLineNumber();
				System.out.println(libdir + File.separator + path + " " + result);
			} catch (Exception e) {
				continue;
			}
			lib += result;
			return result;
		}
		for (String appPath: appPaths) {
			LineNumberReader lnr;
			try {
				lnr = new LineNumberReader(new FileReader(new File(appPath
						+ File.separator + path)));
				lnr.skip(Long.MAX_VALUE);
				result = lnr.getLineNumber();
				System.out.println(appPath + File.separator + path + " " + result);
			} catch (Exception e) {
				continue;
			}
			app += result;
			return result;
		}
		System.out.println("miss: " + path);
		miss++;
		return -1;
	}
}
