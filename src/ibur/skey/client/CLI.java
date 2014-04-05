package ibur.skey.client;

import ibur.skey.PasswordProvider;
import ibur.skey.Util;

import java.util.HashMap;
import java.util.Map;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class CLI {
	
	public static void main(String[] args) {
		Util.setPasswordProvider(new CLIPasswordProvider());
		if(args.length == 0) {
			printUsage();
		} else {
			if("add".equals(args[0])) {
				
			} else if("get".equals(args[0])) {
				
			} else if("new".equals(args[0])) {
				
			} else if("init".equals(args[0])) {
				init(removeStart(args, 1));
			} else if("initd".equals(args[0])) {
				
			} else {
				printUsage();
			}
		}
	}
	
	private static void init(String[] args) {
		OptionParser parser = new OptionParser();
		parser.accepts("file", "File for the password database").requiresArgument();
		parser.accepts("f", "File for the password database").requiresArgument();
		OptionSet options = parser.parse(args);
	}
	
	private static void printUsage() {
		System.out.println("usage: skey <command> [<args>]");
		System.out.println();
		System.out.println("commands:");
		System.out.println("   add     Add custom password to the database");
		System.out.println("   get     Get a password from the database");
		System.out.println("   new     Create a new password");
		System.out.println("   init    Create new password database");
		System.out.println("   initd   Create new password database in the Dropbox folder");
		System.out.println("   help    Show this dialog");
	}
	
	private static class CLIPasswordProvider implements PasswordProvider {
		@Override
		public String getPassword() {
			System.out.print("Enter password: ");
			if(Util.console == null) {
				return Util.sin.nextLine();
			} else {
				return new String(Util.console.readPassword());
			}
		}
	}
	
	private static String[] removeStart(String[] arr, int offset) {
		String[] n = new String[arr.length - offset];
		System.arraycopy(arr, offset, n, 0, n.length);
		return n;
	}
}
