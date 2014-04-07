package ibur.skey.client;

import ibur.lib.B64;
import ibur.skey.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class DesktopFS {

	public static File getSkeyDropboxFile() throws IOException {
		File db = DesktopFS.getDropboxFolder();
		return new File(db.getAbsolutePath() + File.separator + "skey.dat");
	}
	
	public static File getDropboxFolder() throws IOException {
		BufferedReader br = null;
		try{
			String dbPath;
			if(Util.isWindows()) {
				dbPath = System.getenv("APPDATA") + File.separator
						+ "Dropbox" + File.separator + 
						"host.db";
				
			} else {
				dbPath = System.getProperty("user.home") + File.separator
						+ ".dropbox" + File.separator + 
						"host.db"; 
			}
			File db = new File(dbPath);
			
			if(!db.exists()) {
				throw new RuntimeException("No Dropbox folder found");
			}
			br = new BufferedReader(new FileReader(db));
			br.readLine();
			String encodedPath = br.readLine();
			byte[] decodedPath = B64.decode(encodedPath);
			String dropboxPath = new String(decodedPath, "UTF-8");
			
			File dropboxFolder = new File(dropboxPath);
			if(!dropboxFolder.exists()) {
				throw new RuntimeException("No Dropbox folder found");
			}
			return dropboxFolder;
		}
		catch(IOException e) {
			throw e;
		}
		finally {
			if(br != null) {
				br.close();
			}
		}
	}
	
	public static File getStorageFolder() {
		String path;
		if(Util.isWindows()) {
			path = System.getenv("APPDATA");
			
		} else {
			path = System.getProperty("user.home");
		}
		path += File.separator;
		path += ".skey";
		File folder = new File(path);
		if(folder.exists() && !folder.isDirectory()) {
			throw new RuntimeException(".skey storage folder (" + 
										path + 
										") exists and is not a directory");
		}
		if(!folder.exists()) { 
			boolean created = folder.mkdir();
			if(!created) {
				throw new RuntimeException("Could not create storage folder");
			}
		}
		return folder;
	}

	public static Properties getPrefs() {
		File storageFolder = getStorageFolder();
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(new File(storageFolder.getAbsoluteFile() + File.separator + "skey.prefs")));
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not read preferences from file");
		}
		return p;
	}
	
	public static void storePrefs(Properties prefs) {
		File storageFolder = getStorageFolder();
		try {
			prefs.store(new FileOutputStream(new File(storageFolder.getAbsoluteFile() + File.separator + "skey.prefs")), null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not write preferences to file");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not write preferences to file");
		}
	}
}
