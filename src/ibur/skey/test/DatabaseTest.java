package ibur.skey.test;

import ibur.lib.B64;
import ibur.skey.Crypto;
import ibur.skey.Database;
import ibur.skey.PasswordGen;
import ibur.skey.Util;
import ibur.skey.PasswordGen.PwReq;
import ibur.skey.desktop.Dropbox;
import ibur.skey.PasswordProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

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
		Database d = new Database();
		for(char a = 'A'; a <= 'Z'; a++) {
			String pass = PasswordGen.generatePassword(100);
			System.out.println(a + ": " + pass);
			d.putPassword(a + "", pass, "AES128");
		}
		System.out.println(d);
		File dropbox = Dropbox.getSkeyDbFile();
		d.writeToFile(dropbox, Util.getPassword(false), true);
		Database nd = new Database(dropbox);
		System.out.println(nd);
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
