package ibur.skey.client;

import ibur.lib.B64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AES256 {
	
	private static final Scanner in = new Scanner(System.in);
	
	private static final SecureRandom r = new SecureRandom();
	
	public static void main(String[] args) throws Exception {
		char choice;
		if(args.length > 0) {
			choice = args[0].charAt(0);
		} else {
			System.out.println("e for encryption, d for decryption");
			choice = in.nextLine().charAt(0);
		}
		
		if(choice == 'e') {
			aes256EncryptFull();
		} else if(choice == 'd') {
			aes256DecryptFull();
		} else {
			System.out.println("e for encryption, d for decryption");
		}
	}
	
	private static void aes256EncryptFull() throws IOException {
		try {
			System.out.println("Password:");
			String password = in.nextLine();
			System.out.println("Plain text:");
			String plaintext = in.nextLine();
			
			byte[] salt = new byte[8];
			r.nextBytes(salt);
			
			/* Derive the key, given password and salt. */
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
			/* Encrypt the message. */
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secret);
			byte[] iv = cipher.getIV();
			byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));
			
			System.out.println("Salt: " + B64.encode(salt));
			System.out.println("IV: " + B64.encode(iv));
			System.out.println("Ciphertext: " + B64.encode(ciphertext));
			
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			b.write(salt);
			b.write(iv);
			b.write(ciphertext);
			System.out.println(B64.encode(b.toByteArray()));
		} catch(GeneralSecurityException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	private static void aes256DecryptFull() throws IOException {
		try {
			System.out.println("Password:");
			String password = in.nextLine();
			System.out.println("Encrypted:");
			byte[] all = B64.decode(in.nextLine());
			byte[] salt = new byte[8];
			byte[] iv = new byte[16];
			byte[] ciphertext = new byte[all.length - 24];
			System.arraycopy(all, 0, salt, 0, 8);
			System.arraycopy(all, 8, iv, 0, 16);
			System.arraycopy(all, 24, ciphertext, 0, ciphertext.length);
			/* Derive the key, given password and salt. */
			SecretKeyFactory factory = SecretKeyFactory.getInstance	("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
			/* Encrypt the message. */
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			
			IvParameterSpec ips = new IvParameterSpec(iv);
			
			cipher.init(Cipher.DECRYPT_MODE, secret, ips);
			
			String plaintext = new String(cipher.doFinal(ciphertext), "UTF-8");
			System.out.println(plaintext);
		} catch(GeneralSecurityException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
