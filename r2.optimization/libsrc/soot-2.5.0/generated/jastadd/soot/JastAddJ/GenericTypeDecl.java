package soot.JastAddJ;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.io.File;
import java.util.*;
import beaver.*;
import java.util.ArrayList;
import java.util.zip.*;
import java.io.*;
import java.io.FileNotFoundException;
import java.util.Collection;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import soot.coffi.ClassFile;
import soot.coffi.method_info;
import soot.coffi.CONSTANT_Utf8_info;
import soot.tagkit.SourceFileTag;
import soot.coffi.CoffiMethodSource;

/**
 * @ast interface
 * @declaredat :0
 */
public interface GenericTypeDecl {

     
    TypeDecl original();

     
    int getNumTypeParameter();

     
    TypeVariable getTypeParameter(int index);

     
    List getTypeParameterList();

     
    public String fullName();

     
    public String typeName();

     
    public int getNumParTypeDecl();

     
    public ParTypeDecl getParTypeDecl(int index);
public TypeDecl makeGeneric(Signatures.ClassSignature s);

public SimpleSet addTypeVariables(SimpleSet c, String name);

public List createArgumentList(ArrayList params);

  /**
   * @attribute syn
   * @aspect Generics
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/Generics.jrag:139
   */
  @SuppressWarnings({"unchecked", "cast"})
  public boolean isGenericType();
  /**
   * @attribute syn
   * @aspect Generics
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/Generics.jrag:144
   */
  @SuppressWarnings({"unchecked", "cast"})
  public TypeDecl rawType();
  /**
   * @attribute syn
   * @aspect LookupParTypeDecl
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/Generics.jrag:595
   */
  @SuppressWarnings({"unchecked", "cast"})
  public TypeDecl lookupParTypeDecl(ParTypeAccess p);
  /**
   * @attribute syn
   * @aspect LookupParTypeDecl
   * @declaredat /Users/eric/Documents/workspaces/clara-soot/JastAddJ/Java1.5Frontend/Generics.jrag:632
   */
  @SuppressWarnings({"unchecked", "cast"})
  public TypeDecl lookupParTypeDecl(ArrayList list);
}
