package javaFX;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.jcajce.provider.symmetric.util.BCPBEKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;


import sun.misc.BASE64Encoder;

import java.io.*;
import java.math.BigInteger;

public class PGP {

		private static String signed_file = "";
		private static String ID = "id";
		private static String file_in = "/C:/Users/Chris/Oxygen_workspace_NOV2017/FileSigningVerification/src/javaFX/sign_test";
		private static String file_out = "/C:/Users/Chris/Oxygen_workspace_NOV2017/FileSigningVerification/src/javaFX/signed_file_out";
		
		 static PGPPublicKey publicKey;
		 static PGPPrivateKey privateKey;
		 static char [] passphrase = "passphrase".toCharArray();
		
		public static void main (String[] args) throws Exception {
			Security.addProvider(new BouncyCastleProvider());
			System.out.println("1. Generating PGP key pair...");
			
			
			RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();
			kpg.init(new RSAKeyGenerationParameters(BigInteger.valueOf(0x10001), new SecureRandom(), 2048, 12));
			PGPKeyPair privateRSAKey = new BcPGPKeyPair(PGPPublicKey.RSA_SIGN, kpg.generateKeyPair(), new Date());
		
			File input_file = new File(file_in);
			File output_file = new File(file_out);
			System.out.println("3. Signing " + file_in);
			signFile(privateRSAKey, input_file, output_file, passphrase, true);
			System.out.println("4. File has now been signed");
			System.out.println("5. File is about to be verified");
			
			File signature_file = new File(file_out);
			checkSignature(privateRSAKey.getPublicKey(), signature_file);
			System.out.println("6. FIle has now been verified");
			
			PGPKeyPair sigPubKey = new BcPGPKeyPair(PGPPublicKey.RSA_ENCRYPT, kpg.generateKeyPair(), new Date());
			System.out.println("7. Checking if someone elses public key can verify the signature, should return false.");
			checkSignature(sigPubKey.getPublicKey(), signature_file);
			System.out.println("8. Different public key finished verifying, should have returned false");
		
			
		}
			
		
		public static void signFile(PGPKeyPair keyPair,  File file_in, File file_out, char[] password, boolean asciiArmor) throws PGPException, IOException{
			
			PGPPrivateKey privateKey = keyPair.getPrivateKey();
			PGPPublicKey publicKey = keyPair.getPublicKey();
			PGPSignatureGenerator sigGen = new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(publicKey.getAlgorithm(), PGPUtil.SHA1).setProvider("BC"));
			sigGen.init(PGPSignature.BINARY_DOCUMENT, privateKey);
			
			@SuppressWarnings(value = { "unchecked" })
			Iterator<String> it = publicKey.getUserIDs();
			while(it.hasNext()) {
				PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
				spGen.setSignerUserID(false, it.next());
				sigGen.setHashedSubpackets(spGen.generate());
			}
			
			OutputStream outputStream = null;
			
			if(asciiArmor) {
				outputStream = new ArmoredOutputStream(new FileOutputStream(file_out));
			}else {
				outputStream = new FileOutputStream(file_out);
			}
			
			
			PGPCompressedDataGenerator  compressDataGenerator = new PGPCompressedDataGenerator(PGPCompressedData.ZLIB);
	        BCPGOutputStream bcOutputStream = new BCPGOutputStream(compressDataGenerator.open(outputStream));
	        sigGen.generateOnePassVersion(false).encode(bcOutputStream);
	 
	        PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
	        OutputStream literalDataGenOutputStream = literalDataGenerator.open(bcOutputStream, PGPLiteralData.BINARY, file_in);
	        FileInputStream input_file = new FileInputStream(file_in);
		
			int character;
			while((character = input_file.read())>=0) {
				literalDataGenOutputStream.write(character);
				sigGen.update((byte) character);
			}
			literalDataGenerator.close();
	        input_file.close();
	 
	        sigGen.generate().encode(bcOutputStream);
	        compressDataGenerator.close();
	        outputStream.close();
			
		}
				
		public static boolean checkSignature(PGPPublicKey pgpPublicKey, File file_in) throws Exception {
			
			PGPPublicKey publicKey = pgpPublicKey;
		
			InputStream inputStream = PGPUtil.getDecoderStream(new FileInputStream(file_in));
	         
	        PGPObjectFactory pgpObjFactory = new PGPObjectFactory(inputStream, null);
	        PGPCompressedData compressedData = (PGPCompressedData)pgpObjFactory.nextObject();
	         
	        //Get the signature from the file
	          
	        pgpObjFactory = new PGPObjectFactory(compressedData.getDataStream(), null);
	        PGPOnePassSignatureList onePassSignatureList = (PGPOnePassSignatureList)pgpObjFactory.nextObject();
	        PGPOnePassSignature onePassSignature = onePassSignatureList.get(0);
			
	        //Get the literal data from the file
	        
	        PGPLiteralData pgpLiteralData = (PGPLiteralData)pgpObjFactory.nextObject();
	        InputStream literalDataStream = pgpLiteralData.getInputStream();
	         
	        onePassSignature.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), publicKey);
	        FileOutputStream literalDataOutputStream = new FileOutputStream(pgpLiteralData.getFileName());
	        
	        int character;
	        while((character = literalDataStream.read())>=0) {
	        	onePassSignature.update((byte)character);
	        	literalDataOutputStream.write(character);
	        }
	        
	        literalDataOutputStream.write(character);
	        
	        PGPSignatureList p3 = (PGPSignatureList)pgpObjFactory.nextObject();
	        PGPSignature signature = p3.get(0);
	        
	        if(onePassSignature.verify(signature)) {
	        	System.out.println("File is from sender and is verified as TRUE!...");
	        	return true;
	        }else {
	        	System.out.println("**File is NOT from sender and is FALSE!...**");
	        	return false;
	        }
		}
		
		public String readFile(String file) throws IOException {
			 
			BufferedReader br = new BufferedReader (new FileReader(file));
			String line = null;
			StringBuilder stringBuilder = new StringBuilder();
			String lineSeperator = System.getProperty("line.seperator");
			
			try {
				while((line = br.readLine()) != null){
					stringBuilder.append(line);
					stringBuilder.append(lineSeperator);
				}
			} finally {
				br.close();
			}
			return stringBuilder.toString();
		}
		
		
		
}