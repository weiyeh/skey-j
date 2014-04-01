package ibur.skey;

import java.security.SecureRandom;

public class Util {
	public static final SecureRandom r = new SecureRandom();
	
	public static final String OS = System.getProperty("os.name");
	
	public static boolean isWindows() {
		return OS.startsWith("Windows");
	}
}
