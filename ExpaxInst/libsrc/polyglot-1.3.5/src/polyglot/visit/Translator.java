package polyglot.visit;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import polyglot.ast.Import;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.SourceCollection;
import polyglot.ast.SourceFile;
import polyglot.ast.TopLevelDecl;
import polyglot.ast.JL;
import polyglot.frontend.Job;
import polyglot.frontend.TargetFactory;
import polyglot.types.ClassType;
import polyglot.types.Context;
import polyglot.types.Package;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Copy;
import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;
import polyglot.main.*;

/**
 * A Translator generates output code from the processed AST.
 * Output is sent to one or more java file in the directory
 * <code>Options.output_directory</code>.  Each SourceFile in the AST
 * is output to exactly one java file.  The name of that file is
 * determined as follows:
 * <ul>
 * <li> If the SourceFile has a declaration of a public top-level class "C",
 * file name is "C.java".  It is an error for there to be more than one
 * top-level public declaration.
 * <li> If the SourceFile has no public declarations, the file name
 * is the input file name (e.g., "X.jl") with the suffix replaced with ".java"
 * (thus, "X.java").
 * </ul>
 *
 * To use:
 * <pre>
 *     new Translator(job, ts, nf, tf).translate(ast);
 * </pre>
 * The <code>ast</code> must be either a SourceFile or a SourceCollection.
 */
public class Translator extends PrettyPrinter implements Copy
{
    protected Job job;
    protected NodeFactory nf;
    protected TargetFactory tf;
    protected TypeSystem ts;
    protected Context context;
    protected ClassType outerClass = null;

    
    private static HashMap createdFiles = new HashMap();
    public static HashMap getFileNames() { return createdFiles; }
    
    /**
     * Create a Translator.  The output of the visitor is a collection of files
     * whose names are added to the collection <code>outputFiles</code>.
     */
    public Translator(Job job, TypeSystem ts, NodeFactory nf, TargetFactory tf) {
        super();

        this.job = job;
        this.nf = nf;
        this.tf = tf;
        this.ts = ts;
        this.context = job.context();

        if (this.context == null) {
            this.context = ts.createContext();
        }
    }

    /**
     * Return the job associated with this Translator.
     */
    public Job job() { 
        return job;
    }
    
    /** Create a new <code>Translator</code> identical to <code>this</code> but
     * with new context <code>c</code> */
    public Translator context(Context c) {
        if (c == this.context) {
            return this;
        }
        Translator tr = (Translator) copy();
        tr.context = c;
        return tr;
    }
    
    /** Copy the translator. */
    public Object copy() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Java clone() weirdness.");
        }
    }
    
    
    /** Create a new <code>Translator</code> identical to <code>this</code>,
     * except: a) wrapped inside a HeaderTranslator object, and b) with
     * a new context <code>c</code>
     * @param c - the new context to use
     * @return - a header translator identical to this one, but with new context.
     */
    public HeaderTranslator headerContext(Context c) {
      HeaderTranslator ht = new HeaderTranslator(this);
      ht.context = c;
      return ht;      
    } 

    /** Set the outer class context of the translator.  This class is used when
     * translating "new" expressions for nested classes.  For the expression
     * "e.new C()", the name "C" needs to be looked up in the context of the
     * static type of expression "e" (i.e., <code>outerClass</code>), rather
     * than in the current context returned by <code>context()</code>.
     */
    public ClassType outerClass() {
        return outerClass;
    }

    /** Destructively set the outer class context of the translator. */
    public void setOuterClass(ClassType ct) {
        this.outerClass = ct;
    }

    /** Get the extension's type system. */
    public TypeSystem typeSystem() {
        return ts;
    }

    /** Get the current context in which we are translating. */
    public Context context() {
        return context;
    }

    /** Get the extension's node factory. */
    public NodeFactory nodeFactory() {
        return nf;
    }

    public TargetFactory targetFactory() { 
      return tf;
    }
    
    /** Print an ast node using the given code writer.  This method should not
     * be called directly to translate a source file AST; use
     * <code>translate(Node)</code> instead.  This method should only be called
     * by nodes to print their children.
     */
    public void print(Node parent, Node child, CodeWriter w) {
        Translator tr;

        if (parent != null) {
            Context c = parent.del().enterScope(child, context);
            tr = this.context(c);
        }
        else {
            Context c = child.del().enterScope(context);
            tr = this.context(c);
        }

        child.del().translate(w, tr);

        if (parent != null) {
            parent.addDecls(context);
        }
    }

    /** Translate the entire AST. */
    public boolean translate(Node ast) {
        if (ast instanceof SourceFile) {
            SourceFile sfn = (SourceFile) ast;
            return translateSource(sfn);
        }
        else if (ast instanceof SourceCollection) {
            SourceCollection sc = (SourceCollection) ast;

            boolean okay = true;

            for (Iterator i = sc.sources().iterator(); i.hasNext(); ) {
                SourceFile sfn = (SourceFile) i.next();
                okay &= translateSource(sfn);
            }

            return okay;
        }
        else {
            throw new InternalCompilerError("AST root must be a SourceFile; " +
                                            "found a " + ast.getClass().getName());
        }
    }


    /** Transate a single SourceFile node */
    protected boolean translateSource(SourceFile sfn) {
        TypeSystem ts = typeSystem();
        NodeFactory nf = nodeFactory();
	TargetFactory tf = this.tf;
	int outputWidth = job.compiler().outputWidth();
	Collection outputFiles = job.compiler().outputFiles();

        // Find the public declarations in the file.  We'll use these to
        // derive the names of the target files.  There will be one
        // target file per public declaration.  If there are no public
        // declarations, we'll use the source file name to derive the
        // target file name.
        List exports = exports(sfn);

        try {
            File of, headerFile;
            Writer ofw, headerWriter = null;            
            CodeWriter w;
            CodeWriter wH = null;

            String pkg = "";

            if (sfn.package_() != null) {
                Package p = sfn.package_().package_();
                pkg = p.toString();
            }

            Context c = sfn.del().enterScope(context);

            TopLevelDecl first = null;

            if (exports.size() == 0) {
                // Use the source name to derive a default output file name.
                of = tf.outputFile(pkg, sfn.source());
            }
            else {
                first = (TopLevelDecl) exports.get(0);
                of = tf.outputFile(pkg, first.name(), sfn.source());
            }

            String opfPath = of.getPath();
            if (!opfPath.endsWith("$")) outputFiles.add(of.getPath());
            ofw = tf.outputWriter(of);
            w = new CodeWriter(ofw, outputWidth);
            createdFiles.put(of.getPath(), null);

            if(Options.global.cppBackend()) {
              //if we're generating a c++ class, we need to also generate 
              //the .h file              
              headerFile = new File(tf.headerNameForFileName(of.getPath()));
              headerWriter = tf.outputWriter(headerFile);
              wH = new CodeWriter(headerWriter, outputWidth);
              
              String className = null;
              if(!exports.isEmpty())
              {
                first = (TopLevelDecl) exports.get(0);
                className = first.name();
              }
              else
              {
            	String name;
            	name = sfn.source().name();
            	className = name.substring(0, name.lastIndexOf('.'));                
              }
              
              writeHFileHeader(sfn, className, wH);
            }

            writeHeader(sfn, w);

            for (Iterator i = sfn.decls().iterator(); i.hasNext(); ) {
                TopLevelDecl decl = (TopLevelDecl) i.next();

                if (decl.flags().isPublic() && decl != first 
                    && !(Options.global.cppBackend())) {
                    // We hit a new exported declaration, open a new file.
                    // But, first close the old file.
                    w.flush();
                    ofw.close();

                    of = tf.outputFile(pkg, decl.name(), sfn.source());
                    outputFiles.add(of.getPath());
                    ofw = tf.outputWriter(of);
                    w = new CodeWriter(ofw, outputWidth);

                    writeHeader(sfn, w);
                }

                decl.del().translate(w, this.context(c));
                
                if(Options.global.cppBackend())
                  decl.del().translate(wH, this.headerContext(c));

                if (i.hasNext()) {
                    w.newline(0);
                }
            }

            writeFooter(sfn, w);
            if(Options.global.cppBackend())
            {
              writeHFileFooter(sfn, wH);
              wH.flush();
              headerWriter.close();
            }
            
            w.flush();
            ofw.close();
            return true;
        }
        catch (IOException e) {
            job.compiler().errorQueue().enqueue(ErrorInfo.IO_ERROR,
                      "I/O error while translating: " + e.getMessage());
            return false;
        }
    }
    

    
    /**
     * "Escapes" an input string "s" so that it can be used as a macro.
     * Removes all '.' and ':' chars and substitutes in '_' instead.
     * @param s - the input string to escape.
     * @return an escaped string.
     */
    public static String macroEscape(String s)
    {
      String out = "_";
      
      for(int i = 0; i < s.length(); i++)
      {
        char c = s.charAt(i);
        if(c == '.' || c == ':')
          out = out + "_";
        else
          out = out + c;
      }
      
      return out;
    }
    
    /**
     * Turns a package or class name from Java "x.y.z" format 
     * into a C-style scope ("x::y::z")
     * @param s the input package or class name
     * @return A c-scoped version of s
     */
    public static String cScope(String s)
    {
      String out = "";
      
      for(int i = 0; i < s.length(); i++)
      {
        char c = s.charAt(i);
        if(c == '.')
          out = out + "::";
        else
          out = out + c;
      }
      
      return out;      
    }    
    

    
    /** 
     * Write the opening lines of the header file for a given class
     * @param sfn - representation of the source file we're compiling; used for Imports.
     * @param className - The name of the class we're describing
     * @param w - The CodeWriter to write it all out to (the .h file)
     */
    protected void writeHFileHeader(SourceFile sfn, String className, CodeWriter w) {
      String pkg = null;
      
      if(sfn.package_() != null)
      {
        Package p = sfn.package_().package_();
        pkg = p.fullName();
      }
      
      if(pkg == null || pkg.equals(""))
        pkg = "jmatch_primary";
      
      String macroName = "_" + macroEscape(pkg) + "_" + macroEscape(className) + "_H";
      w.write("#ifndef " + macroName);
      w.newline(0);
      w.write("#define " + macroName);
      w.newline(0);

      if(sfn.package_() != null)
        sfn.package_().del().translate(w, this);
      else
        w.write("namespace " + cScope(pkg) + " {");
      w.newline(0);      
      
      w.write("using namespace jmatch_primary;");
      w.newline(0);
      w.write("using namespace java::lang;");
      w.newline(0);
      
      //now make any more imports.
      for(Iterator i = sfn.imports().iterator(); i.hasNext(); ) {
        Import imp = (Import)i.next();
        imp.del().translate(w, this);
        w.newline(0);
      }      
    }
    
    /**
     * Write the footer of the .h file if we're in C++ mode
     * @param w
     */
    protected void writeHFileFooter(SourceFile sfn, CodeWriter w) {
     
      
      int packageDepth = 0;
      int i;
      if(null != sfn.package_())
      {          
        Package p = sfn.package_().package_();
        String pkgName = p.toString();

        if(pkgName.length() > 0)
          packageDepth++;
        
        for(i = 0; i < pkgName.length(); i++)
          if(pkgName.charAt(i) == '.')
            packageDepth++;

        w.write("/* closing namespace */");
        w.newline(0);
        for(i = 0; i < packageDepth; i++)
          w.write("}");

        w.newline(0);
        w.newline(0);
      }        
      
      if(packageDepth == 0)
      {
        w.newline(0);
        w.write("} /* namespace */");
        w.newline(0);
        w.newline(0);
      }
      
      w.write("#endif");
      w.newline(0);
      w.newline(0);
    }

    /** 
     * C++ files also require a footer terminal '}' because they
     * need to close the namespace they're opening.
     */
    protected void writeFooter(SourceFile sfn, CodeWriter w) {
      if(Options.global.cppBackend()) {
        int packageDepth = 0;
        int i;
        if(null != sfn.package_())
        {          
          Package p = sfn.package_().package_();
          String pkgName = p.toString();

          if(pkgName.length() > 0)
            packageDepth++;
          
          for(i = 0; i < pkgName.length(); i++)
            if(pkgName.charAt(i) == '.')
              packageDepth++;

          w.write("/* closing namespace */");
          w.newline(0);
          for(i = 0; i < packageDepth; i++)
            w.write("}");

          w.newline(0);
          w.newline(0);
        }        
        
        if(packageDepth == 0)
        {
          w.newline(0);
          w.write("} /* namespace */");
          w.newline(0);
          w.newline(0);
        }
      }
    }
    
    /** Write the package and import declarations for a source file. */
    protected void writeHeader(SourceFile sfn, CodeWriter w) {
      if(Options.global.cppBackend())
      {
        //package --> namespace and imports --> header includes
        
        String pkg = "";

        if (sfn.package_() != null) {
            Package p = sfn.package_().package_();
            pkg = p.toString() + ".";
        }
        
        int i = 0;
        int dots = 0;
        for(i = 0; i < pkg.length(); i++)
          if(pkg.charAt(i) == '.')
            dots++;
        
        //start out with global project include
        w.write("#include\"");
        for(i = 0; i < dots; i++)
          w.write("../");        
        w.write("mainproj.h\"");
        w.newline(0);
        
        //in C++, open the package (namespace), and then 'using' others:
        if(null != sfn.package_())
        {          
	        sfn.package_().del().translate(w, this);
	        w.newline(0);
	        w.newline(0);
        }
        else
        {
          w.write("namespace jmatch_primary {");
          w.newline(0);
          w.newline(0);
        }
        
        //we always are using the global jmatch namespace
        w.write("using namespace jmatch_primary;");
        w.newline(0);
        
        //and java.lang.*;
        w.write("using namespace java::lang;");
        w.newline(0);
        
        //now make any more imports.
        for(Iterator it = sfn.imports().iterator(); it.hasNext(); ) {
          Import imp = (Import)it.next();
          imp.del().translate(w, this);
          w.newline(0);
        }
        
        
      }
      else
      {
        //standard Java header
		if (sfn.package_() != null) {
		    w.write("package ");
		    sfn.package_().del().translate(w, this);
		    w.write(";");
		    w.newline(0);
		    w.newline(0);
		}
	
		boolean newline = false;
	
		for (Iterator i = sfn.imports().iterator(); i.hasNext(); ) {
		    Import imp = (Import) i.next();
		    imp.del().translate(w, this);
		    newline = true;
		}
	
		if (newline) {
		    w.newline(0);
		}
      }
    }

    /** Get the list of public top-level classes declared in the source file. */
    protected List exports(SourceFile sfn) {
	List exports = new LinkedList();

	for (Iterator i = sfn.decls().iterator(); i.hasNext(); ) {
	    TopLevelDecl decl = (TopLevelDecl) i.next();

	    if (decl.flags().isPublic()) {
		exports.add(decl);
	    }
	}

	return exports;
    }

    public String toString() {
	return "Translator";
    }
}
