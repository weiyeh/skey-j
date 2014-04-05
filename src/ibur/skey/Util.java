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
	
	private static PasswordProvider pwProv;
	
	private static String password;
	
	public static String getPassword(boolean forceRefresh) {
		try {
			if(password == null || forceRefresh) {
				password = pwProv.getPassword();
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
}
