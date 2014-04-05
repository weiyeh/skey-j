package ibur.skey;

import ibur.lib.B64;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bouncycastle.crypto.params.KeyParameter;

public class Database {
	private Map<String, Map<String, String>> db;
	private Set<String> changed;
	private String origPassword;
	private byte[] origSalt;

	/**
	 * Loads a database from the file
	 * @param in
	 * @throws Exception
	 */
	public Database(File in) throws Exception {
		BufferedReader br = null;
		try{
			db = new HashMap<String, Map<String,String>>();
			changed = new HashSet<String>();
			br = new BufferedReader(new FileReader(in));
			String enc = br.readLine();

			boolean encrypted = false;
			if(enc.startsWith("ENCRYPTED:")) {
				encrypted = enc.charAt(10) == 'Y' || enc.charAt(10) == 'T' || enc.charAt(10) == '1'; 
			}

			String password = null;
			byte[] salt = null;
			KeyParameter kp = null;
			if(encrypted) {
				password = Util.getPassword(false);
				salt = B64.decode(br.readLine());
				kp = Crypto.deriveKey(password, salt);
			}

			origPassword = password;
			origSalt = salt;
			
			String line;
			while((line = br.readLine()) != null) {
				String origLine = line;
				if(encrypted) {
					line = new String(Crypto.decrypt(kp, B64.decode(line)), "UTF-8").trim();
				}
				String[] parts = line.split(",");
				for(int i = 0; i < parts.length; i++) {
					parts[i] = parts[i].trim();
				}
				HashMap<String, String> subdb = new HashMap<String, String>();
				subdb.put("NAME", parts[0]);
				subdb.put("SCHEME", parts[1]);
				subdb.put("PW", parts[2]);
				subdb.put("ORIG", origLine);
				db.put(parts[0], subdb);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new Exception("File parse failed");
		}
		finally {
			if(br != null) {
				br.close();
			}
		}
	}

	/**
	 * New empty password database
	 */
	public Database() {
		db = new HashMap<String, Map<String,String>>();
		changed = new HashSet<String>();
	}
	
	public void writeToFile(File out, String password, boolean encString) throws CryptoException, IOException {
		BufferedWriter bw = null;
		try{
			boolean samePass = password.equals(origPassword);
			
			bw = new BufferedWriter(new FileWriter(out));
			bw.write("ENCRYPTED:" + (encString ? "1" : "0") + "\n");

			byte[] salt = null;
			KeyParameter kp = null;
			if(encString) {
				if(samePass) {
					salt = origSalt;
				} else {
					salt = new byte[16];
					Crypto.r.nextBytes(salt);
				}
				kp = Crypto.deriveKey(password, salt);
				bw.write(B64.encode(salt) + "\n");
			}
			for(String key : db.keySet()) {
				Map<String, String> entry = db.get(key);
				if(!changed.contains(key) && samePass && entry.get("ORIG") != null) {
					bw.write(entry.get("ORIG") + "\n");
				} else {
					String line = entry.get("NAME");
					line += "," + entry.get("SCHEME") + ",";
					line += entry.get("PW");
					if(encString) {
						line = B64.encode(Crypto.encrypt(kp, line.getBytes("UTF-8")));
					}
					bw.write(line + "\n");
				}
			}
		}
		finally {
			if(bw != null) {
				bw.close();
			}
		}
	}

	public Set<String> names() {
		return db.keySet();
	}

	public String getPassword(String key) throws Exception {
		try{
			byte[] enc = B64.decode(db.get(key).get("PW"));

			String scheme = db.get(key).get("SCHEME");
			return new String(Crypto.decryptScheme(Util.getPassword(false), enc, scheme), "UTF-8").trim();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void putPassword(String name, String passString, String scheme) throws Exception {
		try{
			byte[] pw = passString.getBytes("UTF-8");
			Map<String, String> entry = new HashMap<String, String>();
			entry.put("NAME", name);
			entry.put("SCHEME", scheme);
			entry.put("PW", B64.encode(Crypto.encryptScheme(Util.getPassword(false), pw, scheme)));
			db.put(name, entry);
			changed.add(name);
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public String getScheme(String key) throws Exception {
		return db.get(key).get("SCHEME");
	}

	@Override
	public String toString() {
		return "Database: " + db.toString();
	}
}
