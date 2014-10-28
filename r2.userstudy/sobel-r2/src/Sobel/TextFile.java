package Sobel;

import java.io.*;
import java.util.Arrays;

public class TextFile {
	public static enum Mode {
		WRITE, READ	
	}

  Mode mode;

	FileOutputStream outputStream;
	PrintWriter streamWriter;

	FileInputStream inputStream;
	BufferedReader streamReader;
	StreamTokenizer streamTokenizer;

	boolean openStatus;

	public TextFile () {
		openStatus = false;
	}

	public TextFile (String filePath, Mode mode) throws FileNotFoundException {
		open (filePath, mode);
	}

	public void open (String filePath, Mode mode) throws FileNotFoundException {
		openStatus = true;
		this.mode = mode;
		if (mode == Mode.WRITE) {
			outputStream = new FileOutputStream (filePath);
			streamWriter = new PrintWriter (outputStream);
		}
		else {
			inputStream = new FileInputStream (filePath);
			streamReader = new BufferedReader (new InputStreamReader (inputStream));
			streamTokenizer = new StreamTokenizer (streamReader);
		}
	}

	public void close () throws IOException {
		if (openStatus && mode == Mode.WRITE) {
			streamWriter.close ();
			outputStream.close ();
		}
	}

	public void save (boolean b) {
		if (openStatus && mode == Mode.WRITE)
			streamWriter.print (b);
	}

	public void save (char c) {
		if (openStatus && mode == Mode.WRITE)
			streamWriter.print (c);
	}

	public void save (int i) {
		if (openStatus && mode == Mode.WRITE)
			streamWriter.print (i);
	}

	public void save (long l) {
		if (openStatus && mode == Mode.WRITE)
			streamWriter.print (l);
	}

	public void save (double d) {
		if (openStatus && mode == Mode.WRITE)
			streamWriter.print (d);
	}

	public void save (char[] s) {
		if (openStatus && mode == Mode.WRITE)
			streamWriter.print (s);
	}

	public void save (String s) {
		if (openStatus && mode == Mode.WRITE)
			streamWriter.print (s);
	}

	public void save (Object obj) {
		if (openStatus && mode == Mode.WRITE)
			streamWriter.print (obj);
	}

	public void save (boolean[] b) {
		if (openStatus && mode == Mode.WRITE) {
			streamWriter.print ("(");
			streamWriter.print (b.length);
			streamWriter.print (", ");
			streamWriter.print (Arrays.toString (b));
			streamWriter.print (")");
		}
	}

	public void save (int[] i) {
		if (openStatus && mode == Mode.WRITE) {
			streamWriter.print ("(");
			streamWriter.print (i.length);
			streamWriter.print (", ");
			streamWriter.print (Arrays.toString (i));
			streamWriter.print (")");
		}
	}

	public void save (long[] l) {
		if (openStatus && mode == Mode.WRITE) {
			streamWriter.print ("(");
			streamWriter.print (l.length);
			streamWriter.print (", ");
			streamWriter.print (Arrays.toString (l));
			streamWriter.print (")");
		}
	}

	public void save (double[] d) {
		if (openStatus && mode == Mode.WRITE) {
			streamWriter.print ("(");
			streamWriter.print (d.length);
			streamWriter.print (", ");
			streamWriter.print (Arrays.toString (d));
			streamWriter.print (")");
		}
	}

	public void save (Object[] obj) {
		if (openStatus && mode == Mode.WRITE) {
			streamWriter.print ("(");
			streamWriter.print (obj.length);
			streamWriter.print (", ");
			streamWriter.print (Arrays.toString (obj));
			streamWriter.print (")");
		}
	}

	public boolean loadBoolean () throws IOException {
		boolean b = false;

		if (openStatus && mode == Mode.READ) {
			streamTokenizer.nextToken ();
			if (streamTokenizer.sval.compareTo ("true") == 0)
				b = true;
		}

		return b;
	}

	public char loadChar () throws IOException {
		char c = 0;	

		if (openStatus && mode == Mode.READ) {
			streamTokenizer.nextToken ();
			if (
			    streamTokenizer.ttype != StreamTokenizer.TT_WORD &&
			    streamTokenizer.ttype != StreamTokenizer.TT_NUMBER &&
			    streamTokenizer.ttype != StreamTokenizer.TT_EOL
			)
				c = (char)streamTokenizer.ttype;	
		}

		return c;
	}

	public int loadInt () throws IOException {
		int i = 0;

		if (openStatus && mode == Mode.READ) {
			streamTokenizer.nextToken ();
			i = (int)streamTokenizer.nval;
		}

		return i;
	}

	public long loadLong () throws IOException {
		long l = 0;

		if (openStatus && mode == Mode.READ) {
			streamTokenizer.nextToken ();
			l = (long)streamTokenizer.nval;
		}

		return l;
	}

	public double loadDouble () throws IOException {
		double d = 0.;

		if (openStatus && mode == Mode.READ) {
			streamTokenizer.nextToken ();
			d = streamTokenizer.nval;
		}

		return d;
	}

	public char[] loadCharArray () throws IOException {
		char[] s;

		if (openStatus && mode == Mode.READ) {
			streamTokenizer.nextToken ();
			s = streamTokenizer.sval.toCharArray ();
		}
		else {
			s = new char[1];
			s[0] = 0;
		}

		return s;
	}

	public String loadString () throws IOException {
		String s;

		if (openStatus && mode == Mode.READ) {
			streamTokenizer.nextToken ();
			s = streamTokenizer.sval;
		}
		else {
			s = "";
		}

		return s;
	}

	public boolean[] loadBooleanArray () throws IOException {
		boolean[] b;

		if (openStatus && mode == Mode.READ) {
			//(
			loadChar ();
			//length
			b = new boolean[loadInt ()];
			//,
			loadChar ();
			//[
			loadChar ();
			for (int i = 0; i < b.length; i++) {
				//data
				b[i] = loadBoolean ();
				//, ]
				loadChar ();
			}
			//)
			loadChar ();
		}
		else {
			b = new boolean[0];
		}

		return b;
	}

	public int[] loadIntArray () throws IOException {
		int[] n;

		if (openStatus && mode == Mode.READ) {
			//(
			loadChar ();
			//length
			n = new int[loadInt ()];
			//,
			loadChar ();
			//[
			loadChar ();
			for (int i = 0; i < n.length; i++) {
				//data
				n[i] = loadInt ();
				//, ]
				loadChar ();
			}
			//)
			loadChar ();
		}
		else {
			n = new int[0];
		}

		return n;
	}

	public long[] loadLongArray () throws IOException {
		long[] l;

		if (openStatus && mode == Mode.READ) {
			//(
			loadChar ();
			//length
			l = new long[loadInt ()];
			//,
			loadChar ();
			//[
			loadChar ();
			for (int i = 0; i < l.length; i++) {
				//data
				l[i] = loadLong ();
				//, ]
				loadChar ();
			}
			//)
			loadChar ();
		}
		else {
			l = new long[0];
		}

		return l;
	}

	public double[] loadDoubleArray () throws IOException {
		double[] d;

		if (openStatus && mode == Mode.READ) {
			//(
			loadChar ();
			//length
			d = new double[loadInt ()];
			//,
			loadChar ();
			//[
			loadChar ();
			for (int i = 0; i < d.length; i++) {
				//data
				d[i] = loadDouble ();
				//, ]
				loadChar ();
			}
			//)
			loadChar ();
		}
		else {
			d = new double[0];
		}

		return d;
	}

	public static void main (String[] args) throws IOException {
		TextFile textFile = new TextFile ("Sample.txt", Mode.WRITE);
		String s = new String ();
		boolean[] b = new boolean[10];

		for (int i = 0; i < b.length; i++) {
			b[i] = i % 2 == 0;
		}
		textFile.save (b);
		textFile.save ('\n');
		for (int i = 0; i < 10; i++) {
			s = "(" + i + ", " + i * i + ")\n";
			textFile.save (s);
		}
		textFile.close ();

		//--------------------------------------------
		textFile.open ("Sample.txt", Mode.READ);

		b = textFile.loadBooleanArray ();
		int n;
		char c;

		System.out.println (Arrays.toString (b));
		for (int i = 0; i < 10; ++i) {
			c = textFile.loadChar ();
			n = textFile.loadInt ();
			s = "" + c + n;
			c = textFile.loadChar ();
			n = textFile.loadInt ();
			s += c + " " + n;
			c = textFile.loadChar ();
			s += c;

			System.out.println (s);
		}
	}
}
