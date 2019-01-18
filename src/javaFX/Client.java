package javaFX;
import java.io.IOException;
import java.net.*;
import java.io.*;

import org.omg.CORBA_2_3.portable.OutputStream;


public class Client {
	public static int PORT = 6001;
	public static String LOCAL_HOST = "127.0.0.1";
	public static int FILE_SIZE;
	public static int bytesRead;
	public static int currentNumberOfBytes;
	
	public static void main (String argv[]) throws IOException {
		try {
		Socket socket = new Socket(argv[0],(6001));	//connect to socket port
		OutputStream output = (OutputStream) socket.getOutputStream();
		output.write(argv[1].getBytes()); //send message
		output.flush(); //flush connection
		}catch(IOException e){
			System.err.println(e.getMessage());
		}
		File file = null;
		
		Socket socket = new Socket (LOCAL_HOST, PORT);
		byte[] byteArray = new byte[FILE_SIZE];
		InputStream input = socket.getInputStream();
		FileOutputStream file_os = new FileOutputStream(file);
		BufferedOutputStream buf_os = new BufferedOutputStream(file_os);
		bytesRead = input.read(byteArray, 0, byteArray.length);
		
		currentNumberOfBytes = bytesRead;
		
		while(bytesRead > -1) {
			bytesRead = input.read(byteArray, currentNumberOfBytes, (byteArray.length-currentNumberOfBytes));
			if(bytesRead >= 0) {
				currentNumberOfBytes += bytesRead;
			}
		}
		
		buf_os.write(byteArray, 0, currentNumberOfBytes);
		buf_os.flush();
		buf_os.close();
		socket.close();

	}
	
}