package ibur.skey.test;

import ibur.skey.PasswordGen.PwReq;

import org.junit.Test;

public class PwReqTest {
	@Test
	public void test() {
		new PwReq("a-zA-Z9-0._\\-");
	}
}
