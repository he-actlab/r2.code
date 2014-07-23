//import enerj.lang.*;

import chord.analyses.expax.lang.*;

public class Simple{
	
	public static void main(String[] args) {
		int a = 1;	// op: 1: MOVE_I R1, IConst: 1
		int b = (a == 1 ? 2 : 3);	// op: 3: MOVE_I T5, IConst: 3	// op: 4: MOVE_I T4, IConst: 2	// op: 6: MOVE_I R3, T6
	}
}

