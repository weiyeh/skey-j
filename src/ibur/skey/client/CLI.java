package ibur.skey.client;

import ibur.skey.CryptoException;
import ibur.skey.Database;
import ibur.skey.PasswordProvider;
import ibur.skey.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class CLI {
	
	private static Scanner in = new Scanner(System.in);
	
	public static void main(String[] args) {
		try{
			Util.setPasswordProvider(new CLIPasswordProvider());
			if(args.length == 0) {
				printUsage();
			} else {
				if("add".equals(args[0])) {

				} else if("get".equals(args[0])) {

				} else if("new".equals(args[0])) {
	
				} else if("init".equals(args[0])) {
					initRun(removeStart(args, 1));
				} else if("initd".equals(args[0])) {
					initdRun(removeStart(args, 1));
				} else {
					printUsage();
				}
			}
		}
		catch(Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
		finally {
			in.close();
		}
	}
	
	private static void initRun(String[] args) {
		OptionParser parser = new OptionParser();
		parser.accepts("file", "File for the password database").withRequiredArg();
		parser.accepts("f", "File for the password database").withRequiredArg();
		parser.accepts("no-encrypt-line", "Don't encrypt the password names and schemes");
		parser.accepts("debug", "Print all stack traces");
		OptionSet options = parser.parse(args);
		String fileName = null;
		if(options.has("f")) {
			fileName = (String) options.valueOf("f");
		} else if(options.has("file")) {
			fileName = (String) options.valueOf("file");
		}
		initDatabase(fileName, options);
	}
	
	private static void initdRun(String[] args) {
		OptionParser parser = new OptionParser();
		parser.accepts("no-encrypt-line", "Don't encrypt the password names and schemes");
		parser.accepts("debug", "Print all stack traces");
		OptionSet options = parser.parse(args);
		boolean debug = options.has("debug");
		try{
			String fileName = Dropbox.getSkeyDbFile().getAbsolutePath();
			initDatabase(fileName, options);
		}
		catch(IOException e) {
			if(debug) {
				e.printStackTrace();
			}
			throw new RuntimeException("File exception occurred");
		}
	}
	
	private static void initDatabase(String fname, OptionSet options) {
		boolean debug = options.has("debug");
		if(fname == null) {
			System.out.println("File name: ");
			fname = in.nextLine();
		}
		File dbfile = new File(fname);
		if(dbfile.exists()) {
			throw new RuntimeException("File exists already");
		}
		try {
			Database d = new Database("AES256");
			byte[] pw = Util.getPassword(true);
			d.writeToFile(dbfile, pw, "AES256");
		}
		catch(FileNotFoundException e) {
			if(debug) {
				e.printStackTrace();
			}
			throw new RuntimeException("Invalid file path");
		}
		catch(IOException e) {
			if(debug) {
				e.printStackTrace();
			}
			throw new RuntimeException("Could not write to file");
		}
		catch(CryptoException e) {
			throw new RuntimeException("A cryptography error occurred");
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
		System.out.println("   help    Show this dialog");
	}
	
	private static String[] removeStart(String[] arr, int offset) {
		String[] n = new String[arr.length - offset];
		System.arraycopy(arr, offset, n, 0, n.length);
		return n;
	}
	
	private static class CLIPasswordProvider implements PasswordProvider {
		@Override
		public byte[] getPassword() {
			System.out.print("Enter encryption/decryption password: ");
			try{
			if(Util.console == null) {
					return Util.sin.nextLine().getBytes("UTF-8");
			} else {
					return new String(Util.console.readPassword()).getBytes("UTF-8");
				}
			}
			catch(UnsupportedEncodingException e) {
				e.printStackTrace();
				System.err.println("ERROR: UTF-8 NOT SUPPORTED.  EXITING.");
				System.exit(-1);
			}
			return null;
		}
	}
}
