package ibur.skey;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public class Crypto {
	private static final int KDF_ROUNDS = 65536;

	private static final PaddedBufferedBlockCipher AES = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()), new PKCS7Padding());

	public static final SecureRandom r = new SecureRandom();

	public static KeyParameter deriveKey(String password, byte[] salt) {
		PKCS5S2ParametersGenerator pcksgen = new PKCS5S2ParametersGenerator(new SHA256Digest());
		byte[] saltBuf = new byte[16];
		System.arraycopy(salt, 0, saltBuf, 0, 16);
		try {
			pcksgen.init(password.getBytes("UTF-8"), saltBuf, KDF_ROUNDS);
		} catch (UnsupportedEncodingException e) {
			System.err.println("ERROR: UTF-8 NOT SUPPORTED");
			System.exit(-1);
		}
		return (KeyParameter) pcksgen.generateDerivedParameters(128);
	}

	public static byte[] createBlob(byte[] salt, byte[] ctext) {
		try{
			ByteArrayOutputStream o = new ByteArrayOutputStream(salt.length + ctext.length);
			o.write(salt);
			o.write(ctext);
			return o.toByteArray();
		}
		catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] decryptBlob(String password, byte[] blob) throws Exception {
		try{
			ByteArrayInputStream i = new ByteArrayInputStream(blob);
			byte[] salt = new byte[16];
			byte[] ctext = new byte[blob.length - 16];

			i.read(salt);
			i.read(ctext);
			
			KeyParameter key = deriveKey(password, salt);
			return decrypt(key, ctext);
		}
		catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] decrypt(KeyParameter key, byte[] ctext) throws Exception {
		try{
			byte[] iv = new byte[16];
			System.arraycopy(ctext, 0, iv, 0, 16);
			AES.init(false, new ParametersWithIV(key, iv));
			byte[] out = new byte[AES.getOutputSize(ctext.length - 16)];
			int outoff = AES.processBytes(ctext, 16, ctext.length - 16, out, 0);
			AES.doFinal(out, outoff);
			return out;
		}
		catch(Exception e) {
			throw new Exception("Decryption failed");
		}
	}

	public static byte[] encrypt(KeyParameter key, byte[] ptext) throws Exception {
		try{
			byte[] iv = new byte[16];
			r.nextBytes(iv);
			AES.init(true, new ParametersWithIV(key, iv));
			byte[] out = new byte[AES.getOutputSize(ptext.length)];
			int outoff = AES.processBytes(ptext, 0, ptext.length, out, 0);
			AES.doFinal(out, outoff);
			ByteArrayOutputStream o = new ByteArrayOutputStream(iv.length + out.length);
			o.write(iv);
			o.write(out);
			return o.toByteArray();
		}
		catch(Exception e) {
			throw new Exception("Encryption failed");
		}
	}
}
