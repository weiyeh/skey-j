package ibur.skey.client;

import ibur.skey.PasswordProvider;
import ibur.skey.Util;

public class CLI {
	public static void main(String[] args) {
		Util.setPasswordProvider(new CLIPasswordProvider());
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
