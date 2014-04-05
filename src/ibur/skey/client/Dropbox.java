package ibur.skey.client;

import ibur.lib.B64;
import ibur.skey.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Dropbox {
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
	
	public static File getSkeyDbFile() throws IOException {
		File db = getDropboxFolder();
		return new File(db.getAbsolutePath() + File.separator + "skey.dat");
	}
	
	public static void main(String[] args) throws Exception{
		File db = getDropboxFolder();
		File pws = new File(db.getAbsolutePath() + File.separator + ".skey.dat");
		pws.createNewFile();
	}
}
