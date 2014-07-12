/*
 * Copyright (c) 2009-2010, The Hong Kong University of Science & Technology.
 * All rights reserved.
 * Licensed under the terms of the BSD License; see COPYING for details.
 */
package chord.analyses.atomizer;

/**
 * @author Zhifeng Lai (zflai.cn@gmail.com)
 */
public class LockAccessState {
	public static final int THREAD_LOCAL    = 1000;
	public static final int THREAD_LOCAL_2  = 1001;
	public static final int SHARED_MODIFIED = 1002;
	
	public static String getName(int state) {
		String name;
		switch (state) {
		case THREAD_LOCAL:
			name = "THREAD_LOCAL";
			break;
		case THREAD_LOCAL_2:
			name = "THREAD_LOCAL_2";
			break;
		case SHARED_MODIFIED:
			name = "SHARED_MODIFIED";
			break;
		default:
			throw new RuntimeException("Invalide memory access state.");
		}
		return name;
	} 
}
