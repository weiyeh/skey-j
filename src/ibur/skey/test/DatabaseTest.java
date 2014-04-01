package ibur.skey.test;

import ibur.lib.B64;
import ibur.skey.Crypto;
import ibur.skey.Database;
import ibur.skey.PasswordGen;
import ibur.skey.Util;
import ibur.skey.PasswordGen.PwReq;
import ibur.skey.PasswordProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.bouncycastle.crypto.params.KeyParameter;
import org.junit.Test;

public class DatabaseTest {

	@Test
	public void generateDBTest() throws Exception {
		File db = new File(".skey.dat");
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
					+ ",," +
					"AES128,";
			byte[] salt = new byte[16];
			Crypto.r.nextBytes(salt);
			res += B64.encode(Crypto.encrypt(Crypto.deriveKey(password, salt), PasswordGen.generatePassword(50, req).getBytes("UTF-8")));
			res += "\n";
			bw.write(res);
		}
		bw.close();
	}
	
	@Test
	public void loadDBTest() throws Exception {
		Util.setPasswordProvider(new StaticPasswordProvider());
		File db = new File(".skey.dat");
		Database d = new Database(db);
		System.out.println(d);
	}
	
	private static class StaticPasswordProvider implements PasswordProvider {

		@Override
		public String getPassword() {
			return "iburinoc";
		}
		
	}

}
