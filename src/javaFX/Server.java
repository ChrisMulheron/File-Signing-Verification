package javaFX;
import java.net.*;
import java.io.*;

import org.omg.CORBA_2_3.portable.OutputStream;


public class Server {
	public int port = 6001;
	public String localhost = "127.0.0.1";
	public static void main (String [] args) throws IOException {
		
		ServerSocket serverSocket = new ServerSocket(6001);
		Socket socket = serverSocket.accept();
		System.out.println("Accepted connection: " + socket);
		
		File transferFile = new File("");
		byte[] byteArray = new byte[(int) transferFile.length()];
		FileInputStream fin = new FileInputStream(transferFile);
		BufferedInputStream bin = new BufferedInputStream(fin);
		bin.read(byteArray, 0, byteArray.length);
		OutputStream os = (OutputStream) socket.getOutputStream();
		System.out.println("Sending file...");
		os.write(byteArray, 0, byteArray.length);
		os.flush();
		socket.close();
		System.out.println("File transfer complete");
	}
	
	
	public void sendFile(File file ) {
		
	}
}