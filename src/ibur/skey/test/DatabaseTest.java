package ibur.skey.test;

import static org.junit.Assert.assertTrue;
import ibur.lib.B64;
import ibur.skey.Crypto;
import ibur.skey.Database;
import ibur.skey.PasswordGen;
import ibur.skey.PasswordGen.PwReq;
import ibur.skey.PasswordProvider;
import ibur.skey.Util;
import ibur.skey.client.Dropbox;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.crypto.params.KeyParameter;
import org.junit.Test;

import static ibur.skey.Crypto.AES256;

public class DatabaseTest {

	@Test
	public void dbTest() throws Exception{
		Util.setPasswordProvider(new StaticPasswordProvider());
		overallDBTest();
	}

	public void overallDBTest() throws Exception {
		Map<String, String> passwords = new HashMap<String, String>();
		Database d = new Database(AES256);
		for(char a = 'A'; a <= 'Z'; a++) {
			String pass = PasswordGen.generatePassword(100);
			System.out.println(a + ": " + pass);
			d.putPassword(a + "", pass, AES256);
			passwords.put(a + "", pass);
		}
		System.out.println(d);
		File dropbox = DesktopFS.getSkeyDropboxFile();
		d.writeToFile(dropbox, Util.getPassword(false), AES256);
		Database nd = new Database(dropbox);
		System.out.println(nd);

		for(char a = 'A'; a <= 'Z'; a++) {
			System.out.println(a + ":" + passwords.get(a + ""));
			System.out.println(a + ":" + nd.getPassword(a+""));
			assertTrue(passwords.get(a + "").equals(nd.getPassword(a+"")));
		}
		System.out.println();
		for(int i = 0; i < 13; i++) {
			char c = (char) (Crypto.r.nextInt(26) + (Crypto.r.nextBoolean() ? 'A' : 'a'));
			String pass = PasswordGen.generatePassword(100);
			passwords.put(c + "", pass);
			nd.putPassword(c + "", pass, AES256);
			System.out.println(c + ":" + pass);
		}
		nd.writeToFile(dropbox, Util.getPassword(false), AES256);
		Database nnd = new Database(dropbox);
		System.out.println();
		for(String s : passwords.keySet()) {
			System.out.println(s + ":" + passwords.get(s));
			System.out.println(s + ":" + nd.getPassword(s));
			assertTrue(passwords.get(s).equals(nnd.getPassword(s)));
		}
	}

	public void generateDBTest() throws Exception {
		File db = new File("skey.dat");
		BufferedWriter bw = new BufferedWriter(new FileWriter(db));
		byte[] password = "iburinoc".getBytes("UTF-8");
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
					"AES256,";
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
		public byte[] getPassword() {
			try{
				return "iburinoc skey database gen test*!D".getBytes("UTF-8");
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
