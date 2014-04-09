package ibur.skey.client;

import static ibur.skey.Crypto.AES256;
import ibur.skey.CryptoException;
import ibur.skey.Database;
import ibur.skey.PasswordGen;
import ibur.skey.PasswordGen.PwReq;
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
					addRun(removeStart(args, 1));
				} else if("get".equals(args[0])) {
					getRun(removeStart(args, 1));
				} else if("gen".equals(args[0])) {
					genRun(removeStart(args, 1));
				} else if("list".equals(args[0])) {
					listRun(removeStart(args, 1));
				} else if("init".equals(args[0])) {
					initRun(removeStart(args, 1));
				} else if("initd".equals(args[0])) {
					initdRun(removeStart(args, 1));
				} else if("rm".equals(args[0])) {
					rmRun(removeStart(args, 1));
				} else if("setd".equals(args[0])) {
					setdRun(removeStart(args, 1));
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

	private static void addRun(String[] args) {
		OptionParser parser = new OptionParser();
		parser.accepts("file", "File for the password database").withRequiredArg();
		parser.accepts("f", "File for the password database").withRequiredArg();
		parser.accepts("name", "Name for new password").withRequiredArg();
		parser.accepts("n", "Name for new password").withRequiredArg();
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
			System.out.println("Enter name for this password:");
			pwname = in.nextLine();
		}
		if("".equals(pwname)) {
			throw new RuntimeException("Invalid password name");
		}

		if(db.names().contains(pwname)) {
			throw new RuntimeException("Name already exists in database");
		}

		String pw = null;
		try {
			pw = new String(Util.pwProv.getPassword("Enter password to store:"), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
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
			if(options.has("p") || options.has("print")) {
				System.out.println(pw);
			} else {
				Util.setClipboard(pw);
			}
			System.out.println("Password copied to clipboard/printed");
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
		parser.accepts("charset", "Set the characeters to be used in the generation, of the form a-zA-Z0-9._- for example").withRequiredArg();
		parser.accepts("no-backup", "Don't backup the password database before writing the new password");
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
			System.out.println("Enter name for this password:");
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

		PwReq p = null;
		if(options.has("charset")) {
			p = new PwReq((String) options.valueOf("charset"));
		} else {
			p = new PwReq();
		}
		String pw = PasswordGen.generatePassword(len, p);
		System.out.println("Password generated with " + (int) p.getEntropy(len) + " bits of entropy");
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
				if(options.has("p") || options.has("print")) {
					System.out.println(pw);
				} else {
					Util.setClipboard(pw);
				}
				System.out.println("Password copied to clipboard/printed and written to file");
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

	private static void listRun(String[] args) {
		OptionParser parser = new OptionParser();
		parser.accepts("file", "File for the password database").withRequiredArg();
		parser.accepts("f", "File for the password database").withRequiredArg();
		parser.accepts("set-default", "Set this as the default password database if one already exists");
		parser.accepts("debug", "Print all stack traces");
		parser.accepts("help", "Print usage information");
		OptionSet options = parser.parse(args);

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

		for(String k : db.names()) {
			System.out.println(k);
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

	private static void rmRun(String[] args) {
		OptionParser parser = new OptionParser();
		parser.accepts("file", "File for the password database").withRequiredArg();
		parser.accepts("f", "File for the password database").withRequiredArg();
		parser.accepts("name", "Name of password").withRequiredArg();
		parser.accepts("n", "Name of password").withRequiredArg();
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
			File dbout = new File(fname);
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

			db.remove(pwname);
			try {
				db.writeToFile(dbout, Util.getPassword(false), AES256);
			} catch (IOException e) {
				if(debug) {
					e.printStackTrace();
				}
				throw new RuntimeException("Could not write database file");
			}
			System.out.println("Removed");
		}
		catch(CryptoException e) {
			throw new RuntimeException("A cryptography error occurred");
		}

	}

	private static void setdRun(String[] args) {
		OptionParser parser = new OptionParser();
		parser.accepts("file", "File for the new default password database").withRequiredArg();
		parser.accepts("f", "File for the new default password database").withRequiredArg();
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
		String fname = null;
		if(options.has("file")) {
			fname = (String) options.valueOf("file");
		} else if(options.has("f")) {
			fname = (String) options.valueOf("f");
		}
		if(fname == null) {
			System.out.println("File name for new database: ");
			fname = in.nextLine();
		}
		if (fname.startsWith("~" + File.separator)) {
			fname = System.getProperty("user.home") + fname.substring(1);
		}
		Properties prefs = DesktopFS.getPrefs();
		prefs.setProperty("Default-DB", fname);
		DesktopFS.storePrefs(prefs);
		System.out.println("New default set");
	}

	private static void printUsage() {
		System.out.println("usage: skey <command> [<args>]");
		System.out.println();
		System.out.println("commands:");
		System.out.println("   add     Add custom password to the database");
		System.out.println("   get     Get a password from the database");
		System.out.println("   gen     Create a new password");
		System.out.println("   list    List all names in this database");
		System.out.println("   init    Create new password database");
		System.out.println("   initd   Create new password database in the Dropbox folder");
		System.out.println("   help    Show this dialog");
		System.out.println("   rm      Remove a password");
		System.out.println("   setd    Change the default database");
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
