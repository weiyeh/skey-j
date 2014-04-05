package ibur.skey.desktop;

import ibur.lib.B64;
import ibur.skey.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class Dropbox {
	public static File getDropboxFolder() throws Exception {
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
			System.out.println(dbPath);
			System.out.println(Arrays.toString(dbPath.toCharArray()));
			if(!db.exists()) {
				throw new RuntimeException("No Dropbox host.db, Dropbox is required for this program to sync");
			}
			br = new BufferedReader(new FileReader(db));
			br.readLine();
			String encodedPath = br.readLine();
			byte[] decodedPath = B64.decode(encodedPath);
			String dropboxPath = new String(decodedPath, "UTF-8");
			System.out.println(dropboxPath);
			File dropboxFolder = new File(dropboxPath);
			if(!dropboxFolder.exists()) {
				throw new RuntimeException("No Dropbox folder, Dropbox is required for this program to sync");
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
	
	public static File getSkeyDbFile() throws Exception {
		File db = getDropboxFolder();
		return new File(db.getAbsolutePath() + File.separator + "skey.dat");
	}
	
	public static void main(String[] args) throws Exception{
		File db = getDropboxFolder();
		File pws = new File(db.getAbsolutePath() + File.separator + ".skey.dat");
		pws.createNewFile();
	}
}
