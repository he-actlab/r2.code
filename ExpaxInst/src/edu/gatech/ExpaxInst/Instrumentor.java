package edu.gatech.ExpaxInst;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.IntConstant;
import soot.jimple.Stmt;
import soot.util.Chain;

public class Instrumentor {
	public static BitSet approxBitVector;
	public int count;
	
	public Instrumentor(String bitVector) {
		approxBitVector = new BitSet(bitVector.length());
		for (int i = 0; i < bitVector.length(); i++)
			if (bitVector.charAt(i) == '1')
				approxBitVector.flip(i);
		count = 0;
	}

	public void instrument(List<SootClass> classes) {

		for (SootClass klass : classes) {
			klass.setApplicationClass();
		}
		
		for (SootClass klass : classes) {
			List<SootMethod> origMethods = klass.getMethods();
			for (SootMethod m : origMethods) {
				// Returns true if this method is not phantom, abstract or native, i.e.
				if (!m.isConcrete()) 
					continue;
				instrument(m);
			}
		}
	}
	
	private void instrument(SootMethod method) {
		Body body = method.getActiveBody();
		Chain<Unit> units = body.getUnits();
		Iterator<Unit> iter = units.iterator();
		Stmt currentStmt;
		ValueBox vb;
		
		while (iter.hasNext()) {
			currentStmt = (Stmt) iter.next();
			String stmtSignature = currentStmt.toString();
			System.out.println("stmt = " + stmtSignature);
			if (currentStmt.containsInvokeExpr() && stmtSignature.contains("enerj")) {
				if (stmtSignature.contains("<init>")) {
					System.out.println("stmt = " + stmtSignature);
					Value value = currentStmt.getUseBoxes().get(2).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(count)) {
							vb = currentStmt.getUseBoxes().get(2);
							vb.setValue(IntConstant.v(0));
						}
						count++;
					}
				}
				if (stmtSignature.contains("loadLocal")) {
					System.out.println("stmt = " + stmtSignature);
					Value value = currentStmt.getUseBoxes().get(2).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(count)) {
							vb = currentStmt.getUseBoxes().get(2);
							vb.setValue(IntConstant.v(0));
						}
						count++;
					}
				}
				if (stmtSignature.contains("storeValue")) {
					System.out.println("stmt = " + stmtSignature);
					Value value = currentStmt.getUseBoxes().get(2).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(count)) {
							vb = currentStmt.getUseBoxes().get(2);
							vb.setValue(IntConstant.v(0));
						}
						count++;
					}
				}
				if (stmtSignature.contains("loadArray")) {
					System.out.println("stmt = " + stmtSignature);
					Value value = currentStmt.getUseBoxes().get(3).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(count)) {
							vb = currentStmt.getUseBoxes().get(3);
							vb.setValue(IntConstant.v(0));
						}
						count++;
					}
				}
				if (stmtSignature.contains("storeArray")) {
					System.out.println("stmt = " + stmtSignature);
					Value value = currentStmt.getUseBoxes().get(3).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(count)) {
							vb = currentStmt.getUseBoxes().get(3);
							vb.setValue(IntConstant.v(0));
						}
						count++;
					}
				}
				if (stmtSignature.contains("loadField")) {
					System.out.println("stmt = " + stmtSignature);
					Value value = currentStmt.getUseBoxes().get(3).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(count)) {
							vb = currentStmt.getUseBoxes().get(3);
							vb.setValue(IntConstant.v(0));
						}
						count++;
					}
				}
				if (stmtSignature.contains("storeField")) {
					System.out.println("stmt = " + stmtSignature);
					Value value = currentStmt.getUseBoxes().get(3).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(count)) {
							vb = currentStmt.getUseBoxes().get(3);
							vb.setValue(IntConstant.v(0));
						}
						count++;
					}
				}
				if (stmtSignature.contains("newArray")) {
					System.out.println("stmt = " + stmtSignature);
					Value value = currentStmt.getUseBoxes().get(3).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(count)) {
							vb = currentStmt.getUseBoxes().get(3);
							vb.setValue(IntConstant.v(0));
							vb = currentStmt.getUseBoxes().get(4);
							vb.setValue(currentStmt.getUseBoxes().get(5).getValue());
							vb = currentStmt.getUseBoxes().get(5);
							vb.setValue(IntConstant.v(0));
						}
						count++;
					}
				}
				if (stmtSignature.contains("binaryOp")) {
					System.out.println("stmt = " + stmtSignature);
					Value value = currentStmt.getUseBoxes().get(5).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(count)) {
							vb = currentStmt.getUseBoxes().get(5);
							vb.setValue(IntConstant.v(0));
						}
						count++;
					}
				}
				/*** jspark added ***/
				// storeLocal :  2
				if (stmtSignature.contains("storeLocal")) {
					System.out.println("stmt = " + stmtSignature);
					Value value = currentStmt.getUseBoxes().get(2).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(count)) {
							vb = currentStmt.getUseBoxes().get(2);
							vb.setValue(IntConstant.v(0));
						}
						count++;
					}
				}
				// assignopArray :  7
				if (stmtSignature.contains("assignopArray")) {
					System.out.println("stmt = " + stmtSignature);
					Value value = currentStmt.getUseBoxes().get(7).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(count)) {
							vb = currentStmt.getUseBoxes().get(7);
							vb.setValue(IntConstant.v(0));
						}
						count++;
					}
				}
				// assignopLocal:  6
				if (stmtSignature.contains("assignopLocal")) {
					System.out.println("stmt = " + stmtSignature);
					Value value = currentStmt.getUseBoxes().get(6).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(count)) {
							vb = currentStmt.getUseBoxes().get(6);
							vb.setValue(IntConstant.v(0));
						}
						count++;
					}
				}
				/*** jspark added ***/
			}
		}
	}
}
