package chord.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/** Workaround for old Java's poor type inference.
 * Say {@code import static chord.Util.CollectionUtil.*;}*/
public class CollectionUtil {
	public static <T> ArrayList<T> newArrayList() { return new ArrayList<T>(); }
	public static <T> HashSet<T> newHashSet() { return new HashSet<T>(); }
	public static <K,V> HashMap<K,V> newHashMap() { return new HashMap<K,V>(); }
}
