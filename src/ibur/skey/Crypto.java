package ibur.skey;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
	public static final String AES256 = "AES256";

	public static KeyParameter deriveKey(byte[] password, byte[] salt) {
		PKCS5S2ParametersGenerator pcksgen = new PKCS5S2ParametersGenerator(new SHA256Digest());
		pcksgen.init(password, salt, KDF_ROUNDS);
		return (KeyParameter) pcksgen.generateDerivedParameters(256);
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

	public static byte[] decryptBlob(byte[] password, byte[] blob) throws CryptoException {
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

	public static byte[] decrypt(KeyParameter key, byte[] ctext) throws CryptoException {
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
			throw new CryptoException("Decryption failed");
		}
	}

	public static byte[] encryptBlob(byte[] pw, byte[] ptext) throws CryptoException {
		byte[] salt = new byte[16];
		r.nextBytes(salt);
		KeyParameter kp = deriveKey(pw, salt);
		return createBlob(salt, encrypt(kp, ptext));
	}
	
	public static byte[] encrypt(KeyParameter key, byte[] ptext) throws CryptoException {
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
			throw new CryptoException("Encryption failed");
		}
	}
	
	public static byte[] encryptScheme(byte[] password, byte[] ptext, String scheme) throws CryptoException{
		if(AES256.equals(scheme)) {
			return encryptBlob(password, ptext);
		} else if("NONE".equals(scheme)){
			return ptext;
		} else {
			throw new RuntimeException("Scheme not recognized");
		}
	}
	
	public static byte[] decryptScheme(byte[] password, byte[] ctext, String scheme) throws CryptoException{
		if(AES256.equals(scheme)) {
			return decryptBlob(password, ctext);
		} else if("NONE".equals(scheme)){
			return ctext;
		} else {
			throw new RuntimeException("Scheme not recognized");
		}
	}
}
