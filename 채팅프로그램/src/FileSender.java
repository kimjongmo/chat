


import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileSender extends Thread{
	private static ServerSocket serverSocket;
	private static int serverPort = 5678; // 서버 포트
	private Socket socket;
	private FileInputStream fis;
	private BufferedInputStream bis;
	private DataOutputStream dos;
	private String file_name;
	private String dir;
	
	public FileSender(String file_name,String dir){
		this.file_name = file_name;
		this.dir = dir;
	}
	
	public void run(){
		try{
			serverSocket = new ServerSocket(serverPort);
			System.out.println("FileSender On");
			Socket socket = serverSocket.accept();
			
			File file = new File(dir+file_name);
			
			dos = new DataOutputStream(socket.getOutputStream());
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			
			byte[] bytes = new byte[8192];
		
			int length;
			while ((length = bis.read(bytes)) != -1) {
				dos.write(bytes,0,length);
				dos.flush();
			}
			
			System.out.println("["+file_name+"]파일 전송 완료");
			dos.close();
			bis.close();
			fis.close();
			socket.close();
			serverSocket.close();
			
			
		}catch(IOException e){
		}
	}
	

}
