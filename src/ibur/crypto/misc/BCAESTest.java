package ibur.crypto.misc;

import ibur.lib.B64;

import java.security.SecureRandom;
import java.util.Scanner;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public class BCAESTest {
	public static void main(String[] args) throws Exception {
		Scanner sin = new Scanner(System.in);
		SecureRandom r = new SecureRandom();
		
		System.out.println("Password:");
		byte[] password = sin.nextLine().getBytes("UTF-8");
		byte[] salt = new byte[16];
		r.nextBytes(salt);
		byte[] iv = new byte[16];
		r.nextBytes(iv);
		
		PaddedBufferedBlockCipher b = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()), new PKCS7Padding());
		
		PKCS5S2ParametersGenerator pcksgen = new PKCS5S2ParametersGenerator(new SHA256Digest());
		pcksgen.init(password, salt, 10000);
		KeyParameter key = (KeyParameter) pcksgen.generateDerivedParameters(128);
		
		
		b.init(true, new ParametersWithIV(key, iv));
	
		System.out.println("Message:");
		byte[] message = sin.nextLine().getBytes("UTF-8");
		
		byte[] out = new byte[b.getOutputSize(message.length)];
		int off = b.processBytes(message, 0, message.length, out, 0);
		off += b.doFinal(out, off);
		
		System.out.println("Salt:" + B64.encode(salt));
		System.out.println("IV:" + B64.encode(iv));
		System.out.println("Ctext:" + B64.encode(out));
		
		b = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()), new PKCS7Padding());
		
		b.init(false, new ParametersWithIV(key, iv));
		
		byte[] orig = new byte[b.getOutputSize(out.length)];
		
		off = b.processBytes(out, 0, out.length, orig, 0);
		off += b.doFinal(orig, off);
		System.out.println(new String(orig, "UTF-8"));
		sin.close();
	}
}
