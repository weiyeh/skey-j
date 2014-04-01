package ibur.skey;

import ibur.lib.B64;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bouncycastle.crypto.params.KeyParameter;

public class Database {
	private Map<String, Map<String, String>> db;

	public Database(File in) throws Exception {
		try{
			db = new HashMap<String, Map<String,String>>();
			BufferedReader br = new BufferedReader(new FileReader(in));
			String enc = br.readLine();

			boolean encrypted = false;
			if(enc.startsWith("ENCRYPTED:")) {
				encrypted = enc.charAt(10) == 'Y' || enc.charAt(10) == 'T' || enc.charAt(10) == '1'; 
			}

			String password = null;
			byte[] salt = null;
			KeyParameter kp = null;
			if(encrypted) {
				password = Util.getPassword();
				salt = B64.decode(br.readLine());
				kp = Crypto.deriveKey(password, salt);
			}

			String line;
			while((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				if(encrypted) {
					if(!"".equals(parts[0])) {
						parts[0] = new String(Crypto.decrypt(kp, B64.decode(parts[0])), "UTF-8");
					}
					if(!"".equals(parts[1])) {
						parts[1] = new String(Crypto.decrypt(kp, B64.decode(parts[1])), "UTF-8");
					}
				}
				if(parts[0] != "") {
					HashMap<String, String> subdb = new HashMap<String, String>();
					subdb.put("NAME", parts[0]);
					subdb.put("URL", parts[1]);
					subdb.put("ENCTYPE", parts[2]);
					subdb.put("PW", parts[3]);
					db.put(parts[0], subdb);
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new Exception("File parse failed");
		}
	}

	public Set<String> names() {
		return db.keySet();
	}

	public String getPassword(String key) throws Exception {
		try{
			byte[] enc = B64.decode(db.get(key).get("PW"));

			String scheme = db.get(key).get("ENCTYPE");
			if("NONE".equals(scheme)) {
				return new String(enc, "UTF-8");
			} else if("AES128".equals(scheme)) {
				return new String(Crypto.decryptBlob(Util.getPassword(), enc), "UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		return "Database: " + db.toString();
	}
}
