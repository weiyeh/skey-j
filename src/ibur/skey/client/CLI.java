package ibur.skey.client;

import ibur.skey.PasswordProvider;
import ibur.skey.Util;

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
				
			} else if("initd".equals(args[0])) {
				
			}
		}
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
}
