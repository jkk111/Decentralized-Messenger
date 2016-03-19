import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

public class Encrypt {
	public static final int KEY_SIZE = 2048;

	public static void main(String[] args) throws Exception {
		Encrypt e = new Encrypt();
		Security.addProvider(new BouncyCastleProvider()); // Very important, registers bouncycastle as a security provider
		byte[] tmp = Files.readAllBytes(Paths.get("pubkey.txt")); // read in pub/private keys
		String pubkeyStr = new String(tmp, StandardCharsets.UTF_8);
		tmp = Files.readAllBytes(Paths.get("privkey.txt"));
		String privkeyStr = new String(tmp, StandardCharsets.UTF_8);

		String enc = e.encrypt("Hello", pubkeyStr); // pass string, pubKeyStr to encrypt a string
		String dec = e.decrypt(enc, privkeyStr); // pass encrypted string and privKeyStr to decrypt a string
		System.out.println("Hello => " + dec);

		StringKeyPair keys = e.generatePrivateKey();
		String enc2 = e.encrypt("testing", keys.getPublicKey());
		String dec2 = e.decrypt(enc2, keys.getPrivateKey());
		System.out.println(dec2);
	}

	public String encrypt(String message, String publicKey)  throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException  {
		PublicKey pubKey = toPubKey(publicKey);
		byte[] tmp = message.getBytes();
		Cipher encrypter = Cipher.getInstance("RSA");
		encrypter.init(Cipher.ENCRYPT_MODE, pubKey);
		tmp = encrypter.doFinal(tmp);
		Encoder encoder = Base64.getEncoder();
		tmp = encoder.encode(tmp);
		return new String(tmp);
	}

	public String decrypt(String message, String privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		PrivateKey privKey = toPrivKey(privateKey);
		byte[] tmp = message.getBytes();
		Decoder decoder = Base64.getDecoder();
		tmp = decoder.decode(tmp);
		Cipher decrypter = Cipher.getInstance("RSA");
		decrypter.init(Cipher.DECRYPT_MODE, privKey);
		tmp = decrypter.doFinal(tmp);
		return new String(tmp);
	}

	public PublicKey toPubKey(String str) throws InvalidKeySpecException, NoSuchAlgorithmException {
		Decoder d = Base64.getDecoder();
		System.out.println(str);
		str = str.replace("-----BEGIN PUBLIC KEY-----",  "").replace("-----END PUBLIC KEY-----","");
		str = str.replace(((char) 0xa)+ "", "");
		str = str.replace(((char) 0xd)+ "", "");
		byte[] tmp = d.decode(str.getBytes());
		X509EncodedKeySpec pub = new X509EncodedKeySpec(tmp);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(pub);
	}

	public PrivateKey toPrivKey(String str) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
		//	  str = str.replace('\r' + '\n' + "", ""+'\n');
		System.out.println(str);
		PemReader pemReader = new PemReader(new StringReader(str));
		PEMParser p = new PEMParser(pemReader);
		Object key = p.readObject();
		byte[] tmp;
		if(key instanceof PrivateKeyInfo)
			tmp = ((PrivateKeyInfo) key).getEncoded();
		else
			tmp = (byte[])((PEMKeyPair) key).getPrivateKeyInfo().getEncoded();
		//    System.exit(0);;
		//    byte[] tmp = ((PEMKeyPair) p.readObject()).getPrivateKeyInfo().getEncoded();
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
		Encoder encoder = Base64.getEncoder();
		keyData = encoder.encode(keyData);
		String keyString = "";
		String keyStringData = new String(keyData);
		System.out.println(keyStringData.length());
		System.out.println(") " + keyStringData);
		while(keyStringData.length() > 0) {
			int sublen = Math.min(64, keyStringData.length());
			String sub = keyStringData.substring(0, sublen);
			keyStringData = keyStringData.substring(sublen);
			keyString += sub + '\n';
		}
		System.out.println(keyString);
		return keyString;
	}

	private class StringKeyPair {
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
}
