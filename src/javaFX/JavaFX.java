package javaFX;

import org.bouncycastle.*;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;

import java.net.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.security.Security;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.css.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public class JavaFX extends Application {
	
	@Override
	public void start(Stage primaryStage) throws Exception {

		Security.addProvider(new BouncyCastleProvider());
		//FileTransfer ft = new FileTransfer(socket);
		Group root = new Group();
		Scene scene = new Scene(root, 1200, 800);
		scene.setFill(Color.WHITE);
		primaryStage.setTitle("PGP Application");

		
		Button importFileButton = new Button ("Import File");
		importFileButton.setTranslateX(100);
		importFileButton.setTranslateY(100);
		importFileButton.setAlignment(Pos.CENTER);
		//root.getChildren().add(importFileButton);
		
		Label fileNameLabel = new Label();
		fileNameLabel.setTranslateX(100);
		fileNameLabel.setTranslateY(100);
		fileNameLabel.setAlignment(Pos.CENTER_LEFT);
		fileNameLabel.setTextFill(Color.BLUE);
		fileNameLabel.setText("fileNameLabel....");
		//root.getChildren().add(fileNameLabel);
		
	/*	Button saveFileButton = new Button ("Save File");
		saveFileButton.setTranslateX(100);
		saveFileButton.setTranslateY(100);
		saveFileButton.setAlignment(Pos.CENTER);
		//root.getChildren().add(saveFileButton);
		
		Button sendFileButton = new Button ("Send File");
		sendFileButton.setTranslateX(100);
		sendFileButton.setTranslateY(100);
		sendFileButton.setAlignment(Pos.CENTER);*/
		
		ArrayList<File> fileArray = new ArrayList<File>();
		importFileButton.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("File Import");
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text files", "*.txt"));
			File selectedFile = fileChooser.showOpenDialog(null);
			fileArray.add(selectedFile);
			if (selectedFile != null) {
				ArrayList<String> list = new ArrayList<String>();
				String fileName= selectedFile.toString();
				fileNameLabel.setText("File selected: " + fileName);
				try {
					list = readFileScanner(selectedFile);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					System.out.println("FNF_Exception...");
					e.printStackTrace();
				}
				for(String s: list) {
					System.out.println(s);
				}
				
			}else {
				fileNameLabel.setText(selectedFile + ": NULL");
			}
		});
		
	/*	saveFileButton.setOnAction(event ->{
			FileChooser fileChooser = new FileChooser();
			//fileChooser.setInitialDirectory("C:\\Users\\Chris\\Documents\\Personal Files\\University_Documents\\Fourth (final) Year\\Computer Network Security\\coursework_2");
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text files", "*.txt"));
			File file = fileChooser.showSaveDialog(primaryStage);
			if (file != null) {
				saveFile(displayFileContents.getText(), file);
			}
			
			
		});*/
		
		/*sendFileButton.setOnAction(event ->{
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text files", "*.txt"));
			File file = fileChooser.showOpenDialog(primaryStage);
			if (file != null) {
				System.out.println("Attempting to send file....");
				//ft.sendFile(file);
			}
		});*/
		
		Button signFile = new Button ("signFile");
		signFile.setTranslateX(100);
		signFile.setTranslateY(100);
		signFile.setAlignment(Pos.CENTER);
		
		TextArea displayEncryptedContents = new TextArea("PGP Encrypted contents: \n");
		displayEncryptedContents.setTranslateX(100);
		displayEncryptedContents.setTranslateY(100);
		displayEncryptedContents.setFocusTraversable(false);
		displayEncryptedContents.setMouseTransparent(true);
		

		RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();
		kpg.init(new RSAKeyGenerationParameters(BigInteger.valueOf(0x10001), new SecureRandom(), 2048, 12));
		PGPKeyPair privateRSAKey = new BcPGPKeyPair(PGPPublicKey.RSA_SIGN, kpg.generateKeyPair(), new Date());
		PGPKeyPair wrongPubKey = new BcPGPKeyPair(PGPPublicKey.RSA_ENCRYPT, kpg.generateKeyPair(), new Date());
		ArrayList<PGPKeyPair> PGPKeyPairArray = new ArrayList<PGPKeyPair>();
		PGPKeyPairArray.add(privateRSAKey);
		
		TextArea password = new TextArea();
		password.setTranslateX(100);
		password.setTranslateY(100);
		password.setPromptText("Enter a passphrase to be used for file signing...");
		password.getText();
		
		signFile.setOnAction(event -> {
			password.getText();
			char[] passphrase = password.getText().toCharArray();
			File file_out = new File("signedFileJavaFX");
			
			try {
				signFile(privateRSAKey, fileArray.get(0), file_out, passphrase, true);
			} catch (PGPException e) {
				//System.out.println("PGPException error...");
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(fileArray.get(0));
			//PGPEncryptFile(file);
			fileArray.add(file_out);
		});
		
		Button checkSignatureCorrectPubKey = new Button ("Check Signature with correct public key");
		checkSignatureCorrectPubKey.setTranslateX(100);
		checkSignatureCorrectPubKey.setTranslateY(100);
		checkSignatureCorrectPubKey.setAlignment(Pos.CENTER);
		
		Label signatureVerificationTrue = new Label();
		signatureVerificationTrue.setTranslateX(100);
		signatureVerificationTrue.setTranslateY(100);
		signatureVerificationTrue.setAlignment(Pos.CENTER_LEFT);
		signatureVerificationTrue.setText("signature verification label...");
		
		Label signatureVerificationFalse = new Label();
		signatureVerificationFalse.setTranslateX(100);
		signatureVerificationFalse.setTranslateY(100);
		signatureVerificationFalse.setAlignment(Pos.CENTER_LEFT);
		signatureVerificationFalse.setText("signature verification label...");
		
		checkSignatureCorrectPubKey.setOnAction(event -> {
			File signature = fileArray.get(1);
			
			try {
				checkSignature(privateRSAKey.getPublicKey(), signature);
				signatureVerificationTrue.setText("File signer has been verified as signer");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		});
		GridPane gridPane = new GridPane();
		
		gridPane.add(importFileButton, 0, 0, 1, 1);
		gridPane.setVgap(10);
		gridPane.add(fileNameLabel, 0, 1, 1, 1);
		gridPane.add(password, 0, 3, 1, 1);
		gridPane.add(signFile, 0, 4, 1, 1);
		gridPane.add(checkSignatureCorrectPubKey, 0, 5, 1, 1);
		gridPane.add(signatureVerificationFalse, 0, 6, 1, 1);
		gridPane.add(signatureVerificationTrue, 0, 7, 1, 1);
		root.getChildren().add(gridPane);
		
		ScrollPane scrollPane = new ScrollPane(gridPane);
		scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}
	
	public static void signFile(PGPKeyPair keyPair,  File file_in, File file_out, char[] password, boolean asciiArmor) throws PGPException, IOException{
		
		PGPPrivateKey privateKey = keyPair.getPrivateKey();
		PGPPublicKey publicKey = keyPair.getPublicKey();
		
		PGPSignatureGenerator sigGen = new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(publicKey.getAlgorithm(), PGPUtil.SHA1).setProvider("BC"));
		sigGen.init(PGPSignature.BINARY_DOCUMENT, privateKey);
		
		@SuppressWarnings(value = { "unchecked" })
		Iterator<String> it = publicKey.getUserIDs(); // get the user ID of the PGP key
		while(it.hasNext()) {
			
			//while there is a user ID, generate the PGP signature sub packets
			PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
			spGen.setSignerUserID(false, it.next());
			sigGen.setHashedSubpackets(spGen.generate());
		}
		
		OutputStream outputStream = null;
		
		// Ascii armor used to make the binary document into a more readable format
		if(asciiArmor) {
			outputStream = new ArmoredOutputStream(new FileOutputStream(file_out));
		}else {
			outputStream = new FileOutputStream(file_out);
		}
		
		// compress file data 
		PGPCompressedDataGenerator  compressDataGenerator = new PGPCompressedDataGenerator(PGPCompressedData.ZLIB);
        BCPGOutputStream bcOutputStream = new BCPGOutputStream(compressDataGenerator.open(outputStream));
        sigGen.generateOnePassVersion(false).encode(bcOutputStream);
 
        // generate literal data objects to write the signature files
        PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
        OutputStream literalDataGenOutputStream = literalDataGenerator.open(bcOutputStream, PGPLiteralData.BINARY, file_in);
       
        int character;
        FileInputStream input_file = new FileInputStream(file_in);
        
        // while there are characters in file to be signed, update the signature generator object with characters
		while((character = input_file.read()) >= 0) {
			literalDataGenOutputStream.write(character);
			sigGen.update((byte) character);
		}
		
		literalDataGenerator.close();
        input_file.close();
        
        //Generate signature encoded with compressed data stream
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
         
        FileInputStream keyIn = new FileInputStream(file_in);
        // build the signature verifier object
        onePassSignature.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), publicKey);
        FileOutputStream literalDataOutputStream = new FileOutputStream(pgpLiteralData.getFileName());
        
        int character;
        while((character = literalDataStream.read())>=0) {
        	onePassSignature.update((byte)character);
        	literalDataOutputStream.write(character);
        }
        
        //literalDataOutputStream.write(character);
        
        // list with the files associated signatures
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
	
	private ArrayList<String> readFileScanner(File file) throws FileNotFoundException{
		Scanner scanner = new Scanner(new FileReader(file));
		ArrayList<String> list = new ArrayList<String>();
		
		while(scanner.hasNextLine()) {
			list.add(scanner.nextLine());
		}
		scanner.close();
		return list;
	}
	
	public static void main (String[] args) {
		launch(args);
	}
	
}
