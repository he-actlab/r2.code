package edu.gatech.R2Inst;

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
	public int counter;
	public String mode;
	
	public Instrumentor(String bitVector, String mode) {
		approxBitVector = new BitSet(bitVector.length());
		for (int i = 0; i < bitVector.length(); i++)
			if (bitVector.charAt(i) == '1')
				approxBitVector.flip(i);
		this.mode = mode;
		counter = 0;
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
				if (this.mode.equalsIgnoreCase("inst"))
					instrument(m);
				else if (this.mode.equalsIgnoreCase("count"))
					count(m);
				else
					throw new RuntimeException("Wrong mode");
			}
		}
		if (this.mode.equalsIgnoreCase("count"))
			System.out.println("count="+counter);
	}
	
	private void count(SootMethod method) {
		Body body = method.getActiveBody();
		Chain<Unit> units = body.getUnits();
		Iterator<Unit> iter = units.iterator();
		Stmt currentStmt;
		ValueBox vb;
		
		while (iter.hasNext()) {
			currentStmt = (Stmt) iter.next();
			String stmtSignature = currentStmt.toString();
			if (currentStmt.containsInvokeExpr() && stmtSignature.contains("enerj")) {
				if (stmtSignature.contains("<init>")) {
					Value value = currentStmt.getUseBoxes().get(2).getValue();
					if (value.equals(IntConstant.v(1))) 
						counter++;
				}
				if (stmtSignature.contains("loadLocal")) {
					Value value = currentStmt.getUseBoxes().get(2).getValue();
					if (value.equals(IntConstant.v(1))) 
						counter++;
				}
				if (stmtSignature.contains("storeValue")) {
					Value value = currentStmt.getUseBoxes().get(2).getValue();
					if (value.equals(IntConstant.v(1))) 
						counter++;
				}
				if (stmtSignature.contains("loadArray")) {
					Value value = currentStmt.getUseBoxes().get(3).getValue();
					if (value.equals(IntConstant.v(1))) 
						counter++;
				}
				if (stmtSignature.contains("storeArray")) {
					Value value = currentStmt.getUseBoxes().get(3).getValue();
					if (value.equals(IntConstant.v(1))) 
						counter++;
				}
				if (stmtSignature.contains("loadField")) {
					Value value = currentStmt.getUseBoxes().get(3).getValue();
					if (value.equals(IntConstant.v(1))) 
						counter++;
				}
				if (stmtSignature.contains("storeField")) {
					Value value = currentStmt.getUseBoxes().get(3).getValue();
					if (value.equals(IntConstant.v(1))) 
						counter++;
				}
				if (stmtSignature.contains("newArray")) {
					Value value = currentStmt.getUseBoxes().get(3).getValue();
					if (value.equals(IntConstant.v(1))) 
						counter++;
				}
				if (stmtSignature.contains("binaryOp")) {
					Value value = currentStmt.getUseBoxes().get(5).getValue();
					if (value.equals(IntConstant.v(1))) 
						counter++;
				}
				// storeLocal :  2
				if (stmtSignature.contains("storeLocal")) {
					Value value = currentStmt.getUseBoxes().get(2).getValue();
					if (value.equals(IntConstant.v(1))) 
						counter++;
				}
				// assignopArray :  7
				if (stmtSignature.contains("assignopArray")) {
					Value value = currentStmt.getUseBoxes().get(7).getValue();
					if (value.equals(IntConstant.v(1))) 
						counter++;
				}
				// assignopLocal:  6
				if (stmtSignature.contains("assignopLocal")) {
					Value value = currentStmt.getUseBoxes().get(6).getValue();
					if (value.equals(IntConstant.v(1))) 
						counter++;
				}
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
			if (currentStmt.containsInvokeExpr() && stmtSignature.contains("enerj")) {
				if (stmtSignature.contains("<init>")) {
					Value value = currentStmt.getUseBoxes().get(2).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(counter)) {
							vb = currentStmt.getUseBoxes().get(2);
							vb.setValue(IntConstant.v(0));
						}
						counter++;
					}
				}
				if (stmtSignature.contains("loadLocal")) {
					Value value = currentStmt.getUseBoxes().get(2).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(counter)) {
							vb = currentStmt.getUseBoxes().get(2);
							vb.setValue(IntConstant.v(0));
						}
						counter++;
					}
				}
				if (stmtSignature.contains("storeValue")) {
					Value value = currentStmt.getUseBoxes().get(2).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(counter)) {
							vb = currentStmt.getUseBoxes().get(2);
							vb.setValue(IntConstant.v(0));
						}
						counter++;
					}
				}
				if (stmtSignature.contains("loadArray")) {
					Value value = currentStmt.getUseBoxes().get(3).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(counter)) {
							vb = currentStmt.getUseBoxes().get(3);
							vb.setValue(IntConstant.v(0));
						}
						counter++;
					}
				}
				if (stmtSignature.contains("storeArray")) {
					Value value = currentStmt.getUseBoxes().get(3).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(counter)) {
							vb = currentStmt.getUseBoxes().get(3);
							vb.setValue(IntConstant.v(0));
						}
						counter++;
					}
				}
				if (stmtSignature.contains("loadField")) {
					Value value = currentStmt.getUseBoxes().get(3).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(counter)) {
							vb = currentStmt.getUseBoxes().get(3);
							vb.setValue(IntConstant.v(0));
						}
						counter++;
					}
				}
				if (stmtSignature.contains("storeField")) {
					Value value = currentStmt.getUseBoxes().get(3).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(counter)) {
							vb = currentStmt.getUseBoxes().get(3);
							vb.setValue(IntConstant.v(0));
						}
						counter++;
					}
				}
				if (stmtSignature.contains("newArray")) {
					Value value = currentStmt.getUseBoxes().get(3).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(counter)) {
							vb = currentStmt.getUseBoxes().get(3);
							vb.setValue(IntConstant.v(0));
							vb = currentStmt.getUseBoxes().get(4);
							vb.setValue(currentStmt.getUseBoxes().get(5).getValue());
							vb = currentStmt.getUseBoxes().get(5);
							vb.setValue(IntConstant.v(0));
						}
						counter++;
					}
				}
				if (stmtSignature.contains("binaryOp")) {
					Value value = currentStmt.getUseBoxes().get(5).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(counter)) {
							vb = currentStmt.getUseBoxes().get(5);
							vb.setValue(IntConstant.v(0));
						}
						counter++;
					}
				}
				// storeLocal :  2
				if (stmtSignature.contains("storeLocal")) {
					Value value = currentStmt.getUseBoxes().get(2).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(counter)) {
							vb = currentStmt.getUseBoxes().get(2);
							vb.setValue(IntConstant.v(0));
						}
						counter++;
					}
				}
				// assignopArray :  7
				if (stmtSignature.contains("assignopArray")) {
					Value value = currentStmt.getUseBoxes().get(7).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(counter)) {
							vb = currentStmt.getUseBoxes().get(7);
							vb.setValue(IntConstant.v(0));
						}
						counter++;
					}
				}
				// assignopLocal:  6
				if (stmtSignature.contains("assignopLocal")) {
					Value value = currentStmt.getUseBoxes().get(6).getValue();
					if (value.equals(IntConstant.v(1))) {
						if (!approxBitVector.get(counter)) {
							vb = currentStmt.getUseBoxes().get(6);
							vb.setValue(IntConstant.v(0));
						}
						counter++;
					}
				}
			}
		}
	}
}
