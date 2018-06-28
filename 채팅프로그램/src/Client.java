


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;

public class Client {
	
	private Socket socket;
	private String serverIP = "localhost";//나중에는 IP로 전환
	private BufferedReader input;//자신의 키보드로부터 날라오는 것
	private PrintWriter output;
	private BufferedReader inputS;//서버로부터 날라오는 것
	private String ID;		//아이디
	private boolean chat = false;	//룸에 입장시 chat = true; 룸에서 나올시(대기방) chat = false;
	private boolean mute = false;	//아봉 상태
	private String dir = "C:/Users/diat/Desktop/채팅/클라이언트/";
	public static void main(String[] args){
		new Client().clientStart();
	}
	
	private void clientStart(){
		try{
			String msg="";		//서버로부터 ID확인 문자
			input = new BufferedReader(new InputStreamReader(System.in,"MS949"));
			socket = new Socket(serverIP,9999);
			System.out.println("서버와 연결되었습니다. 접속할 ID를 입력하십시요.");
			
			inputS = new BufferedReader(new InputStreamReader(socket.getInputStream(),"MS949"));
			output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),"MS949"));
			while(true){
				
				System.out.print("ID:");
				ID = input.readLine();//키보드로부터 id입력 받기
				
				output.println(Protocol.LOGIN+":"+ID);//서버에 보내기
				output.flush();
				
				if((msg=inputS.readLine()).equals(Protocol.LOGIN_SUCCRESS)){
					System.out.println("로그인 성공");
					help();
					Thread thread = new Thread(){
						public void run(){
							receiveKBD();//쓰레드
						}
					};
					thread.start();
					
					Thread thread2 = new Thread(){
						public void run(){
							receiveServer();//쓰레드
						}
					};
					thread2.start();
					
					break;
				}else
					System.out.println("중복됨 다시 ID를 입력하세요.");
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void send(String msg){
		try{
			output.println(msg);
			output.flush();
		}catch(Exception e){}
	}
	
	private void receiveServer(){
		String msg;
		try{
			while((msg=inputS.readLine())!=null){
				StringTokenizer token = new StringTokenizer(msg,":");
				String protocol = token.nextToken();
				switch(protocol){
				case Protocol.ROOM_CREATE_FAIL:	//방 생성 실패
					System.out.println(token.nextToken());//실패한 이유
					break;
				case Protocol.ROOM_CREATE:		//방 생성 성공
					System.out.println("-------방 입장------");
					System.out.println("방제:"+token.nextToken());
					System.out.println("------------------");
					chat = true;		//방에 입장 되었다는 것을 알림
					break;
				case Protocol.ROOM_ENTER_FAIL:	//방 입장 실패
					String reason = token.nextToken();
					System.out.println(reason);
					break;
				case Protocol.ROOM_ENTER:		//방 입장 성공
					System.out.println("-------방 입장------");
					System.out.println("방제:"+token.nextToken());
					System.out.println("------------------");
					chat = true;		//방에 입장 되었다는 것을 알림
					break;
				case Protocol.USER_LIST:		//유저리스트
					if(chat == false){
					System.out.println("대기방------");
					
					while(token.hasMoreTokens()){
						String name = token.nextToken();
						System.out.println(name);
					}
					
					System.out.println("-----------");
					}else{
						System.out.println("방 유저------");
						
						while(token.hasMoreTokens()){
							String name = token.nextToken();
							System.out.println(name);
						}
						
						System.out.println("-------------");
					}
					
					break;
				case Protocol.GET_OUTED:
					output.println(Protocol.GET_OUTED);
					output.flush();
					chat = false;	//방에 나가짐
					break;
				case Protocol.ROOM_LIST:		//룸 리스트
					System.out.println("방---------");
					
					while(token.hasMoreTokens()){
						String name = token.nextToken();
						System.out.println(name);
					}
					
					System.out.println("-----------");
					break;
				case Protocol.MUTED:
					mute = true;	//채팅 봉인
					
					Thread thread = new Thread(){
						public void run(){
							try{
								Thread.sleep(10000);	//10초
								mute = false;
							}catch(Exception e){}	
						}
					};
					thread.start();
					break;
				case Protocol.FILE_LIST:
					System.out.println("--------파일목록--------");
					while(token.hasMoreTokens()){
						String file = token.nextToken();
						System.out.println(file);
					}
					System.out.println("----------------------");
					break;
				default://위의 프로토콜 외에 메시지는 일반 메시지로 간주
						System.out.println(msg);
				}
			}
		}catch(Exception e){}
	}
	
	private void receiveKBD(){
		String msg;
			try{
				
				while((msg=input.readLine())!=null){
					
					StringTokenizer token = new StringTokenizer(msg,":");
					String protocol = token.nextToken();
					
					if(chat == false){//대기방일 때
						switch(protocol){
						case Protocol.FILE_SEND:
							System.out.print("파일명:");
							String file_name = input.readLine();
							new FileSender(file_name,dir).start();
							output.println(Protocol.FILE_SEND+":"+file_name+":"+InetAddress.getLocalHost().getHostAddress());
							output.flush();
							continue;
						case Protocol.FILE_DOWN:
							System.out.println("다운 받을 파일명을 입력하세요:");
							String down_file = input.readLine();
							new FileReceiver(serverIP,down_file,dir).start();;
							output.println(Protocol.FILE_DOWN+":"+down_file);
							output.flush();
							continue;
						case Protocol.HELP:
							help();
							continue;
						case Protocol.END:
							output.println(Protocol.END);
							output.flush();
							System.exit(0);
						case Protocol.FILE_LIST:
						case Protocol.ROOM_CREATE://방 생성
						case Protocol.USER_LIST:	//유저리스트(대기방+룸) 요청
						case Protocol.ROOM_ENTER:	//방 입장
						case Protocol.ROOM_LIST:	//방 리스트
						case Protocol.WHISPER:
							break;
							default://다른 프로토콜 외의 것은 그냥 채팅으로 간주!
								msg = Protocol.ALL+":"+msg;
						}
					}
					else{			//룸에 입장되어 있을 때
						switch(protocol){
						case Protocol.FILE_SEND:
							System.out.print("파일명:");
							String file_name = input.readLine();
							new FileSender(file_name,dir).start();
							continue;
						case Protocol.HELP:
							help1();
							continue;
						case Protocol.ROOM_EXIT://방 나가기
							chat = false;
							break;
						case Protocol.FILE_LIST:
						case Protocol.MUTE://채팅 금지시키기 (room_master이랑 ID가 같은 사람만 가능)
						case Protocol.USER_LIST://유저리스트 (대기방 +룸) 요청
						case Protocol.ROOM_LIST://방 리스트
						case Protocol.WHISPER://귓속말
						case Protocol.FILE_DOWN:
						case Protocol.GET_OUT:
						case Protocol.MAN_DATE:
							break;
							default:
								msg = Protocol.ROOM+":"+msg;//다른 프로토콜 외의 것은 그냥 방 채팅으로 간주!
						}
					}
					
					if(mute==false)
							send(msg);
					else
						System.out.println("채팅금지상태입니다.");
				}
				
			}catch(Exception e){
				System.out.println("왜 여기서 에러가 나냐");
			}
	}
	
	private void help(){
		System.out.println("-------------------------------------");
		System.out.println("귓속말=/w:[상대방ID]:[msg]");
		System.out.println("유저 리스트= /ulist");
		System.out.println("룸 리스트=/rlist");
		System.out.println("방 생성=/create:[방 제목]:[제한 인원]");
		System.out.println("방 입장=/in:[방 제목]");
		System.out.println("도움말=/help");
		System.out.println("파일업로드목록 = /flist");
		System.out.println("파일 업로드 = /file");
		System.out.println("파일 다운로드 = /down");
		System.out.println("-------------------------------------");
	}
	private void help1(){
		System.out.println("-------------------------------------");
		System.out.println("귓속말=/w:[상대방ID]:[msg]");
		System.out.println("유저 리스트= /ulist");
		System.out.println("도움말=/help");
		System.out.println("방 나가기=/exit");
		System.out.println("채팅봉인=/mute:[상대방ID]	*방장만 가능");
		System.out.println("방장위임=/md:[상대방ID]		*방장만 가능");
		System.out.println("강퇴시키기=/getout:[상대방ID]*방장만 가능");
		System.out.println("파일업로드목록 = /flist");
		System.out.println("파일 업로드 = /file");
		System.out.println("파일 다운로드 = /down");
		System.out.println("-------------------------------------");
	}
	
	
}
//파일 전송

//여기까지만 하자 ㄷㄷㅇ너림ㄴ러ㅣㅁ
