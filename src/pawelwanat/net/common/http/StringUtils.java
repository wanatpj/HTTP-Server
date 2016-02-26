package pawelwanat.net.common.http;

import java.util.Arrays;
import java.util.Vector;

import com.google.common.primitives.Bytes;

public class StringUtils {
	
	private final static boolean DEBUG = true;
	
	public static String getString(Vector<Byte> vector, int from, int to){
		return new String(Bytes.toArray(vector.subList(from, to)));
	}
	public static String secure(String string){
		if(DEBUG) {
			return string;
		} 
		return Arrays.toString(string.getBytes());
	}
}
