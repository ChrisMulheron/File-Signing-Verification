package javaFX;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class FileTransfer {
	Socket socket;
	InputStream input;
	FileOutputStream file_os;
	BufferedOutputStream buf_os;
	int bufferSize;
	
	FileTransfer(Socket client){
		socket = client;
		input = null;
		file_os = null;
		buf_os = null;
		bufferSize = 0;
	}

	public void receiveFile(File file) {
		try {
			input = socket.getInputStream();
			bufferSize = socket.getReceiveBufferSize();
			System.out.println("Buffer size: " + bufferSize);
			file_os = new FileOutputStream(file);
			buf_os = new BufferedOutputStream(file_os);
			byte[] bytes = new byte[bufferSize];
			int count;
			
			while ((count = input.read(bytes)) >= 0) {
				buf_os.write(bytes, 0, count);
			}
			buf_os.close();
			input.close();
			
			
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendFile(File file) {
		
		FileInputStream file_input;
		BufferedInputStream buf_input;
		BufferedOutputStream output;
		byte[] buffer = new byte[8192];
		
		try {
			file_input = new FileInputStream(file);
			buf_input = new BufferedInputStream(file_input);
			output = new BufferedOutputStream(socket.getOutputStream());
			int count;
			while((count = buf_input.read(buffer)) > 0) {
				output.write(buffer, 0, count);
			}
			output.close();
			file_input.close();
			buf_input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
