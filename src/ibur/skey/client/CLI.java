package ibur.skey.client;

import static ibur.skey.Crypto.AES256;
import ibur.skey.CryptoException;
import ibur.skey.Database;
import ibur.skey.PasswordGen;
import ibur.skey.PasswordProvider;
import ibur.skey.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
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
					getRun(removeStart(args, 1));
				} else if("gen".equals(args[0])) {
					genRun(removeStart(args, 1));
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

	private static void getRun(String[] args) {
		OptionParser parser = new OptionParser();
		parser.accepts("file", "File for the password database").withRequiredArg();
		parser.accepts("f", "File for the password database").withRequiredArg();
		parser.accepts("name", "Name of password").withRequiredArg();
		parser.accepts("n", "Name of password").withRequiredArg();
		parser.accepts("p", "Print the password instead of copying it to clipboard");
		parser.accepts("print", "Print the password instead of copying it to clipboard");
		parser.accepts("debug", "Print all stack traces");
		parser.accepts("help", "Print usage information");
		OptionSet options = parser.parse(args);
		if(options.has("help")) {
			try {
				parser.printHelpOn(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
//		boolean debug = options.has("debug");
		String fname = null;
		if(options.has("file")) {
			fname = (String) options.valueOf("file");
		} else if(options.has("f")) {
			fname = (String) options.valueOf("f");
		} else {
			Properties prefs = DesktopFS.getPrefs();
			fname = prefs.getProperty("Default-DB"); // will stay null if does not exist
		}
		if(fname == null) {
			System.out.println("File name: ");
			fname = in.nextLine();
		}
		if (fname.startsWith("~" + File.separator)) {
			fname = System.getProperty("user.home") + fname.substring(1);
		}

		Database db = new Database(new File(fname));

		String pwname = null;
		if(options.has("name")) {
			pwname = (String) options.valueOf("name");
		} else if(options.has("n")) {
			pwname = (String) options.valueOf("n");
		}
		if(pwname == null) {
			System.out.println("Enter entry name of this password");
			pwname = in.nextLine();
		}
		if("".equals(pwname)) {
			throw new RuntimeException("Invalid password name");
		}
		
		try{
			String pw = db.getPassword(pwname);
			if(pw == null) {
				throw new RuntimeException("Password not found");
			}
			Util.setClipboard(pw);
			System.out.println("Password copied to clipboard");
		}
		catch(CryptoException e) {
			throw new RuntimeException("A cryptography error occurred");
		}

	}
	
	private static void genRun(String[] args) {
		OptionParser parser = new OptionParser();
		parser.accepts("file", "File for the password database").withRequiredArg();
		parser.accepts("f", "File for the password database").withRequiredArg();
		parser.accepts("name", "Name for new password").withRequiredArg();
		parser.accepts("n", "Name for new password").withRequiredArg();
		parser.accepts("length", "Length for new password").withRequiredArg();
		parser.accepts("l", "Length for new password").withRequiredArg();
		parser.accepts("no-backup", "Don't backup the password database before writing the new password");
		parser.accepts("debug", "Print all stack traces");
		parser.accepts("help", "Print usage information");
		OptionSet options = parser.parse(args);
		if(options.has("help")) {
			try {
				parser.printHelpOn(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		boolean debug = options.has("debug");
		String fname = null;
		if(options.has("file")) {
			fname = (String) options.valueOf("file");
		} else if(options.has("f")) {
			fname = (String) options.valueOf("f");
		} else {
			Properties prefs = DesktopFS.getPrefs();
			fname = prefs.getProperty("Default-DB"); // will stay null if does not exist
		}
		if(fname == null) {
			System.out.println("File name: ");
			fname = in.nextLine();
		}
		if (fname.startsWith("~" + File.separator)) {
			fname = System.getProperty("user.home") + fname.substring(1);
		}

		Database db = new Database(new File(fname));

		String pwname = null;
		if(options.has("name")) {
			pwname = (String) options.valueOf("name");
		} else if(options.has("n")) {
			pwname = (String) options.valueOf("n");
		}
		if(pwname == null) {
			System.out.println("Enter entry name for this password");
			pwname = in.nextLine();
		}
		if("".equals(pwname)) {
			throw new RuntimeException("Invalid password name");
		}

		if(db.names().contains(pwname)) {
			throw new RuntimeException("Name already exists in database");
		}

		int len = -1;
		if(options.has("length")) {
			try {
				len = Integer.parseInt((String)options.valueOf("length"));
			}
			catch(NumberFormatException e) {
				throw new RuntimeException("Invalid length of password");
			}
		} else if(options.has("l")) {
			try {
				len = Integer.parseInt((String)options.valueOf("l"));
			}
			catch(NumberFormatException e) {
				throw new RuntimeException("Invalid length of password");
			}
		} else {
			System.out.println("Password length:");
			try{
				len = Integer.parseInt(in.nextLine());
			}
			catch(NumberFormatException e) {
				throw new RuntimeException("Invalid length of password");
			}
		}
		if(len < 1) {
			throw new RuntimeException("Invalid length of password");
		}

		// TODO: Options for PwReq to use
		String pw = PasswordGen.generatePassword(len, new PasswordGen.PwReq());
		try{
			db.putPassword(pwname, pw, AES256);
			File dbout = new File(fname);
			if(!options.has("no-backup")) {
				try{
					String bak = fname.substring(0, fname.length() - 3) + "bak";
					File bakfile = new File(bak);
					DesktopFS.backupFile(dbout, bakfile);
				}
				catch(IOException e) {
					if(debug) {
						e.printStackTrace();
					}
					throw new RuntimeException("Could not create backup");
				}
			}
			try{
				db.writeToFile(dbout, Util.getPassword(false), AES256);
				Util.setClipboard(pw);
				System.out.println("Password copied to clipboard and written to file");
			}
			catch (IOException e) {
				if(debug) {
					e.printStackTrace();
				}
				throw new RuntimeException("Could not write database file");
			}
		}
		catch(CryptoException e) {
			throw new RuntimeException("A cryptography error occurred");
		}

	}

	private static void initRun(String[] args) {
		OptionParser parser = new OptionParser();
		parser.accepts("file", "File for the password database").withRequiredArg();
		parser.accepts("f", "File for the password database").withRequiredArg();
		parser.accepts("set-default", "Set this as the default password database if one already exists");
		parser.accepts("debug", "Print all stack traces");
		parser.accepts("help", "Print usage information");
		OptionSet options = parser.parse(args);
		String fileName = null;
		if(options.has("help")) {
			try {
				parser.printHelpOn(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		if(options.has("f")) {
			fileName = (String) options.valueOf("f");
		} else if(options.has("file")) {
			fileName = (String) options.valueOf("file");
		}
		initDatabase(fileName, options);
	}

	private static void initdRun(String[] args) {
		OptionParser parser = new OptionParser();
		parser.accepts("set-default", "Set this as the default password database if one already exists");
		parser.accepts("debug", "Print all stack traces");
		parser.accepts("help", "Print usage information");
		OptionSet options = parser.parse(args);
		if(options.has("help")) {
			try {
				parser.printHelpOn(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		boolean debug = options.has("debug");
		try{
			String fileName = DesktopFS.getSkeyDropboxFile().getAbsolutePath();
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
		if (fname.startsWith("~" + File.separator)) {
			fname = System.getProperty("user.home") + fname.substring(1);
		}
		File dbfile = new File(fname);
		if(dbfile.exists()) {
			throw new RuntimeException("File exists already");
		}
		try {
			Database d = new Database(AES256);
			byte[] pw = Util.getPassword(true);
			d.writeToFile(dbfile, pw, AES256);
			Properties prefs = DesktopFS.getPrefs();
			if(prefs.getProperty("Default-DB") == null || options.has("set-default")) {
				prefs.setProperty("Default-DB", fname);
				DesktopFS.storePrefs(prefs);
			}
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
		System.out.println("   gen     Create a new password");
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
		public byte[] getPassword(String prompt) {
			System.out.print(prompt);
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
