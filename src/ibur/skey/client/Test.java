package ibur.skey.client;

import ibur.lib.B64;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Test {
	public static void main(String[] args) {
		String strDataToEncrypt = new String();
		String strCipherText = new String();
		String strDecryptedText = new String();
		
		try{
		/**
		 *  Step 1. Generate an AES key using KeyGenerator
		 *  		Initialize the keysize to 128 
		 * 
		 */
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(128);
		SecretKey secretKey = keyGen.generateKey();
		
		System.out.println("Key: " + B64.encode(secretKey.getEncoded()));
		
		/**
		 *  Step2. Create a Cipher by specifying the following parameters
		 * 			a. Algorithm name - here it is AES
		 */
		
		Cipher aesCipher = Cipher.getInstance("AES");
		
		/**
		 *  Step 3. Initialize the Cipher for Encryption 
		 */
		
		aesCipher.init(Cipher.ENCRYPT_MODE,secretKey);
		
		/**
		 *  Step 4. Encrypt the Data
		 *  		1. Declare / Initialize the Data. Here the data is of type String
		 *  		2. Convert the Input Text to Bytes
		 *  		3. Encrypt the bytes using doFinal method 
		 */
		strDataToEncrypt = "Hello World of Encryption using AES ";
		byte[] byteDataToEncrypt = strDataToEncrypt.getBytes();
		byte[] byteCipherText = aesCipher.doFinal(byteDataToEncrypt); 
		strCipherText = B64.encode(byteCipherText);
		System.out.println("Cipher Text generated using AES is " +strCipherText);
		
		/**
		 *  Step 5. Decrypt the Data
		 *  		1. Initialize the Cipher for Decryption 
		 *  		2. Decrypt the cipher bytes using doFinal method 
		 */
		aesCipher.init(Cipher.DECRYPT_MODE,secretKey,aesCipher.getParameters());
		byte[] byteDecryptedText = aesCipher.doFinal(byteCipherText);
		strDecryptedText = new String(byteDecryptedText);
		System.out.println(" Decrypted Text message is " +strDecryptedText);
		}
		
		catch (NoSuchAlgorithmException noSuchAlgo)
		{
			System.out.println(" No Such Algorithm exists " + noSuchAlgo);
		}
		
			catch (NoSuchPaddingException noSuchPad)
			{
				System.out.println(" No Such Padding exists " + noSuchPad);
			}
		
				catch (InvalidKeyException invalidKey)
				{
					System.out.println(" Invalid Key " + invalidKey);
				}
				
				catch (BadPaddingException badPadding)
				{
					System.out.println(" Bad Padding " + badPadding);
				}
				
				catch (IllegalBlockSizeException illegalBlockSize)
				{
					System.out.println(" Illegal Block Size " + illegalBlockSize);
				}
				
				catch (InvalidAlgorithmParameterException invalidParam)
				{
					System.out.println(" Invalid Parameter " + invalidParam);
				}
	}
}
