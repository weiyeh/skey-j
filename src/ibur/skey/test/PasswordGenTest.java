package ibur.skey.test;

import ibur.skey.PasswordGen;
import ibur.skey.PasswordGen.PwReq;

import org.junit.Test;

public class PasswordGenTest {

	@Test
	public void generatePasswordTest() {
		PwReq req = new PwReq();
		for(int i = 0; i < 10; i++) {
			System.out.println(PasswordGen.generatePassword(30, req));
		}
		System.out.println(req.getEntropy(100));
	}
}
