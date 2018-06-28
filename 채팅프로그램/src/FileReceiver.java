


import java.net.Socket;
import java.io.*;
public class FileReceiver extends Thread{
	private static int Port = 5678; //포트
	private Socket socket;
	private String serverIP;
	private DataInputStream dis;
	private FileOutputStream fos;
	private BufferedOutputStream bos;
	private String file_name;
	private String dir;
	
	public FileReceiver(String serverIP,String file_name,String dir){
		this.serverIP = serverIP;
		this.file_name = file_name;
		this.dir = dir;
	}
		
	public void run(){
		try{
			while(true){
				socket = new Socket(serverIP,Port);
				if(socket.isBound())
					break;
			}
			
			System.out.println("FileReceiver On");
			
			File file = new File(dir+file_name);
			dis = new DataInputStream(socket.getInputStream());
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			
			byte[] bytes = new byte[8192];
			
			int len;
			
			while ((len = dis.read(bytes))!=-1) {
				bos.write(bytes,0,len);
				bos.flush();
			}
			bos.close();
			fos.close();
			socket.close();
			System.out.println("["+file_name+"]"+"파일다운 완료");
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
