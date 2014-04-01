package ibur.skey.test;

import static org.junit.Assert.*;
import ibur.skey.PasswordGen;
import ibur.skey.Util;
import ibur.skey.PasswordGen.PwReq;

import org.junit.Test;

public class PasswordGenTest {

	@Test
	public void PwReqTest() {
		PwReq r = new PwReq();
		assertTrue(r.validatePW("12awfasv15"));
		r.numbers = -1;
		assertFalse(r.validatePW("1awfasomdaw-_"));
	}
	
	@Test
	public void generatePasswordTest() {
		PwReq req = new PwReq();
		for(int i = 0; i < 10; i++) {
			System.out.println(PasswordGen.generatePassword(30, req));
		}
	}
}
