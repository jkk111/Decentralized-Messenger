package com.maximus.dm.decentralizedmessenger.helper;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

public class Encrypt {
	public static final int KEY_SIZE = 2048;
	
	public static void main(String[] args) throws Exception {
		Encrypt e = new Encrypt();
		Security.addProvider(new BouncyCastleProvider()); // Very important, registers bouncycastle as a security provider
		byte[] tmp = fileToByteArray("pubkey.txt"); // read in pub/private keys
		String pubkeyStr = new String(tmp, StandardCharsets.UTF_8);
		tmp = fileToByteArray("privkey.txt");
		String privkeyStr = new String(tmp, StandardCharsets.UTF_8);

		String enc = e.encrypt("Hello", pubkeyStr); // pass string, pubKeyStr to encrypt a string
		String dec = e.decrypt(enc, privkeyStr); // pass encrypted string and privKeyStr to decrypt a string
		System.out.println("Hello => " + dec);

		StringKeyPair keys = e.generatePrivateKey();
		String enc2 = e.encrypt("testingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtestingtesting", keys.getPublicKey());
		String dec2 = e.decrypt(enc2, keys.getPrivateKey());
		System.out.println(dec2);
	}


    public String[] chunkString(String str, int chunkSize) { 
        char[] data = str.toCharArray();
        ArrayList<String> chunks = new ArrayList<String>();
        int base = 0;
        while(base < str.length()) {
        	String tmp = new String(data, base, Math.min(chunkSize, data.length - base));
        	chunks.add(tmp);
        	base += chunkSize;
        }
        String[] result = new String[chunks.size()];
        chunks.toArray(result);
        return result;
    }

	
	public String encrypt(String message, String publicKey)  throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException  {
		String[] chunks = chunkString(message, 100);
		String encStr = "";
		PublicKey pubKey = toPubKey(publicKey);
		Cipher encrypter = Cipher.getInstance("RSA");
		encrypter.init(Cipher.ENCRYPT_MODE, pubKey);
		for(int i = 0 ; i < chunks.length; i++) {
			if(i > 0)
				encStr += ",";
			byte[] tmp = chunks[i].getBytes();
			tmp = Base64.encode(tmp, Base64.DEFAULT);
			tmp = encrypter.doFinal(tmp);
			encStr += new String(tmp);
		}
		return encStr;
	}

	public String decrypt(String message, String privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String[] chunks = message.split(",");
		PrivateKey privKey = toPrivKey(privateKey);
		Cipher decrypter = Cipher.getInstance("RSA");
		decrypter.init(Cipher.DECRYPT_MODE, privKey);
		String decStr = "";
		for(int i = 0; i < chunks.length; i++) {
			byte[] tmp = chunks[i].getBytes();
			tmp = Base64.decode(tmp, Base64.DEFAULT);
			tmp = decrypter.doFinal(tmp);
			decStr += new String(tmp);
		}
		return decStr;
	}

	public PublicKey toPubKey(String str) throws InvalidKeySpecException, NoSuchAlgorithmException {
		str = str.replace("-----BEGIN PUBLIC KEY-----",  "").replace("-----END PUBLIC KEY-----", "");
		str = str.replace(((char) 0xa)+ "", "");
		str = str.replace(((char) 0xd)+ "", "");
		byte[] tmp = Base64.decode(str.getBytes(), Base64.DEFAULT);
		X509EncodedKeySpec pub = new X509EncodedKeySpec(tmp);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(pub);
	}

	public PrivateKey toPrivKey(String str) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
		PemReader pemReader = new PemReader(new StringReader(str));
		PEMParser p = new PEMParser(pemReader);
		Object key = p.readObject();
		byte[] tmp;
		if(key instanceof PrivateKeyInfo)
			tmp = ((PrivateKeyInfo) key).getEncoded();
		else
			tmp = (byte[])((PEMKeyPair) key).getPrivateKeyInfo().getEncoded();
		p.close();
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec keyspec = new PKCS8EncodedKeySpec(tmp);
		return kf.generatePrivate(keyspec);
	}

	public StringKeyPair generatePrivateKey() throws Exception {
		KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
		keygen.initialize(KEY_SIZE);
		KeyPair keypair = keygen.generateKeyPair();
		PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(keypair.getPrivate().getEncoded());
		String priv = "-----BEGIN RSA PRIVATE KEY-----\n" + keyFormatter(pkInfo.parsePrivateKey().toASN1Primitive().getEncoded())
		+"-----END RSA PRIVATE KEY-----";	  
		StringWriter publicKey = new StringWriter();
		JcaPEMWriter publicWriter = new JcaPEMWriter(publicKey);
		publicWriter.writeObject(new PemObject("PUBLIC KEY", keypair.getPublic().getEncoded()));
		publicWriter.close();
		StringKeyPair keys = new StringKeyPair(publicKey.getBuffer().toString(), priv);
		return keys;
	}

	public String keyFormatter(byte[] keyData) {
		keyData = Base64.encode(keyData, Base64.DEFAULT);
		String keyString = "";
		String keyStringData = new String(keyData);
		while(keyStringData.length() > 0) {
			int sublen = Math.min(64, keyStringData.length());
			String sub = keyStringData.substring(0, sublen);
			keyStringData = keyStringData.substring(sublen);
			keyString += sub + '\n';
		}
		return keyString;
	}

	public class StringKeyPair {
		private String publicKey, privateKey;

		public StringKeyPair(String pub, String priv) {
			this.publicKey = pub;
			this.privateKey = priv;
		}

		public String getPublicKey() {
			return publicKey;
		}

		public String getPrivateKey() {
			return privateKey;
		}
	}

	private static byte[] fileToByteArray(String filename) {
		FileInputStream fileInputStream=null;

		File file = new File(filename);

		byte[] bFile = new byte[(int) file.length()];

		try {
			//convert file into array of bytes
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(bFile);
			fileInputStream.close();

			for (int i = 0; i < bFile.length; i++) {
				System.out.print((char)bFile[i]);
			}

		}catch(Exception e){
			e.printStackTrace();
		}

		return bFile;
	}
}
