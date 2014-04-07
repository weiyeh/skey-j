package ibur.skey;

import java.io.Console;
import java.util.Scanner;

public class Util {
	public static final String OS = System.getProperty("os.name");
	
	public static boolean isWindows() {
		return OS.startsWith("Windows");
	}
	
	public static final Scanner sin = new Scanner(System.in);
	
	public static final Console console = System.console();
	
	public static PasswordProvider pwProv;
	
	private static byte[] password;
	
	public static byte[] getPassword(boolean forceRefresh) {
		try {
			if(password == null || forceRefresh) {
				password = pwProv.getPassword("Enter master password: ");
			}
			return password;
		}
		catch(NullPointerException e) {
			throw new RuntimeException("ERROR: No password provider.");
		}
	}
	
	public static void setPasswordProvider(PasswordProvider p) {
		pwProv = p;
	}
	
	public static boolean byteArrEq(byte[] a, byte[] b) {
		boolean res = (a != null) && (b != null) && a.length == b.length;
		for(int i = 0; (a != null && i < a.length) && (b != null && i < b.length); i++) {
			res = res && (a[i] == b[i]);
		}
		return res;
	}
}
