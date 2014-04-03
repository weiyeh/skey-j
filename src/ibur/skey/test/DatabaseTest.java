package ibur.skey.test;

import static org.junit.Assert.assertTrue;
import ibur.lib.B64;
import ibur.skey.Crypto;
import ibur.skey.Database;
import ibur.skey.PasswordGen;
import ibur.skey.PasswordGen.PwReq;
import ibur.skey.PasswordProvider;
import ibur.skey.Util;
import ibur.skey.desktop.Dropbox;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.crypto.params.KeyParameter;
import org.junit.Test;

public class DatabaseTest {

	@Test
	public void dbTest() throws Exception{
		Util.setPasswordProvider(new StaticPasswordProvider());
		//generateDBTest();
		//loadDBTest();
		overallDBTest();
	}
	
	public void overallDBTest() throws Exception {
		Map<String, String> passwords = new HashMap<String, String>();
		Database d = new Database();
		for(char a = 'A'; a <= 'Z'; a++) {
			String pass = PasswordGen.generatePassword(100);
			System.out.println(a + ": " + pass);
			d.putPassword(a + "", pass, "AES128");
			passwords.put(a + "", pass);
		}
		System.out.println(d);
		File dropbox = Dropbox.getSkeyDbFile();
		d.writeToFile(dropbox, Util.getPassword(false), true);
		Database nd = new Database(dropbox);
		System.out.println(nd);
		
		for(char a = 'A'; a <= 'Z'; a++) {
			System.out.println(passwords.get(a + ""));
			System.out.println(nd.getPassword(a+""));
			assertTrue(passwords.get(a + "").equals(nd.getPassword(a+"")));
		}
	}
	
	public void generateDBTest() throws Exception {
		File db = new File("skey.dat");
		BufferedWriter bw = new BufferedWriter(new FileWriter(db));
		String password = "iburinoc";
		byte[] mastersalt = new byte[16];
		Crypto.r.nextBytes(mastersalt);
		
		bw.write("ENCRYPTED:1\n");
		bw.write(B64.encode(mastersalt) + "\n");
		
		KeyParameter key = Crypto.deriveKey(password, mastersalt);
		PwReq req = new PwReq();
		
		String[] sites = new String[] {"Facebook", "Google", "Stanford"};
		for(String s : sites) {
			String res = B64.encode(Crypto.encrypt(key, s.getBytes("UTF-8")))
					+ "," +
					"AES128,";
			byte[] salt = new byte[16];
			Crypto.r.nextBytes(salt);
			
			String passw = PasswordGen.generatePassword(50, req);
			System.out.println(s + ": " + passw);
			byte[] blob = Crypto.createBlob(salt, Crypto.encrypt(Crypto.deriveKey(password, salt), passw.getBytes("UTF-8")));
			
			res += B64.encode(blob);
			res += "\n";
			bw.write(res);
		}
		bw.close();
	}

	public void loadDBTest() throws Exception {
		File db = new File("skey.dat");
		Database d = new Database(db);
		System.out.println(d);
		for(String s : d.names()) {
			System.out.println(s + ": " + d.getPassword(s));
		}
	}
	
	private static class StaticPasswordProvider implements PasswordProvider {

		@Override
		public String getPassword() {
			return "iburinoc";
		}
		
	}

}
