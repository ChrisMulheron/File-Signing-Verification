package javaFX;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.sql.Date;
import java.util.Calendar;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import java.security.cert.*;

public class PGPCertificates {
	
	public static void main(String args[]) throws IOException {
		Security.addProvider(new BouncyCastleProvider());
		File publicKeyFilename = new File("public_key.txt");
		File privateKeyFilename = new File("private_key.txt");
	}
	
	public void genCert(PGPKeyPair keyPair, String subject) throws IOException {
		Security.setProperty("crypto.policy", "unlimited");
		
		long currentTime = System.currentTimeMillis();
		Date startDate = new Date(currentTime);
		X500Name dnName = new X500Name(subject);
		BigInteger serialNumber = new BigInteger(Long.toString(currentTime));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		calendar.add(Calendar.YEAR, 1);
		
		Date endDate = (Date) calendar.getTime();
	}
		
}
