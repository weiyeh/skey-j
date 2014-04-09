package ibur.skey.test;

import static org.junit.Assert.*;

import org.junit.Test;
import static ibur.skey.PasswordGen.PwReq;

public class PwReqTest {
	@Test
	public void test() {
		assertEquals(new String(new PwReq("a-zA-Z9-0._\\-").getCharset()), "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789._-");
	}
}
