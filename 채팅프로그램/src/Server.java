
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

	private ExecutorService executorService;
	private ServerSocket serverSocket;
	private int serverPort = 9999; // 포트번호
	private ConcurrentHashMap<String, PrintWriter> userList = new ConcurrentHashMap<String, PrintWriter>();
	// 대기실 리스트
	private ConcurrentHashMap<String, Room> roomList = new ConcurrentHashMap<String, Room>();
	// 방 리스트
	private String dir = "C:/Users/diat/Desktop/채팅/서버/";

	// 서버 폴더 위치
	public static void main(String[] args) {
		new Server().serverStart();
	}

	private void serverStart() {
		 executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		// 스레드풀 생성
		 System.out.println(Runtime.getRuntime().availableProcessors());
		try {
			serverSocket = new ServerSocket(serverPort);
			System.out.println("┌-----------아무나 채팅------------┐");
			System.out.println();
		} catch (IOException e) {
			System.out.println("Server_serverStart() err");
			if (!serverSocket.isClosed()) {
				serverStop();
			}
			return;
		}
		

		Runnable runnable = new Runnable() {
			public void run() {
				while (true) {
					try {
						Socket socket = serverSocket.accept();
						System.out.println();
						String clientIp = socket.getInetAddress().getHostAddress();
						System.out.println(clientIp + "에서 접속이 일어남");
						new ServerThread(socket);
						System.out.println(Thread.currentThread().getName() + "이 처리함");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		executorService.submit(runnable);

	}

	private void serverStop() {
		Iterator<String> iterator1 = userList.keySet().iterator();
		while (iterator1.hasNext()) {
			try {
				PrintWriter pw = userList.get(iterator1.next());
				pw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Iterator<String> iterator2 = roomList.keySet().iterator();
		while (iterator2.hasNext()) {
			try {
				PrintWriter pw = userList.get(iterator2.next());
				pw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!serverSocket.isClosed() && serverSocket != null) {
			try {
				serverSocket.close();
			} catch (Exception e) {
			}
		}
		if (executorService != null && !executorService.isShutdown()) {
			try {
				executorService.shutdown();
			} catch (Exception e) {
			}
		}
	}

	private class ServerThread {
		private Socket threadSocket;
		private BufferedReader input;
		private PrintWriter output;
		private String ID; // 사용자 ID
		private boolean mute = false;// 채팅 금지 당했는지 안당햇는지 구분
		private String enter_room; // 사용자가 입장한 방

		private ServerThread(Socket threadSocket) {
			this.threadSocket = threadSocket;
			connect(threadSocket);
			receive();
		}

		private void connect(Socket threadSocket) {
			try {
				input = new BufferedReader(new InputStreamReader(threadSocket.getInputStream(), "MS949"));
				output = new PrintWriter(new OutputStreamWriter(threadSocket.getOutputStream(), "MS949"));
			} catch (IOException e) {
				System.out.println("ServerThread_connect() err");
			}
		}

		public void receive() {
			Runnable runnable = new Runnable() {
				public void run() {
					String msg; // 메시지
					try {
						while ((msg = input.readLine()) != null) {
							split(msg);
							System.out.println(Thread.currentThread().getName() + "이 처리함");
						}
					} catch (Exception e) {

					} finally {
						if (enter_room == null)
							userList.remove(ID);// 유저리스트에서 id삭제
						else
							delete(ID);// 방에 입장되어 잇는 상태라면
						sendToAll("**" + ID + "님이 채팅프로그램을 나갔습니다**");
						try {
							threadSocket.close();//
						} catch (IOException e) {

						}
					}
				}
			};
			executorService.submit(runnable);
		}

		// 방속 유저리스트에서 삭제
		private void delete(String ID) {
			Room room = roomList.get(enter_room);
			Iterator<String> iterator = room.getRoom_userList().keySet().iterator();
			while (iterator.hasNext()) {
				if (iterator.next().equals(ID)) {
					if (!room.deleteID(ID))
						roomList.remove(enter_room);
					else
						sendToRoom("**" + ID + "님이 채팅 방을 나가셨습니다**");
				}
			}

			enter_room = null; // 입장한 방 초기화
		}

		// 대기실 채팅
		private void sendToAll(String msg) {
			Iterator<String> iterator = userList.keySet().iterator();
			while (iterator.hasNext()) {
				try {
					PrintWriter pw = userList.get(iterator.next());
					pw.println(msg);
					pw.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private void split(String msg) {
			System.out.println(msg);
			StringTokenizer token = new StringTokenizer(msg, ":");
			String protocol = token.nextToken();
			switch (protocol) {
			case Protocol.LOGIN:
				while (token.hasMoreElements()) {
					ID = token.nextToken();
				}
				if (duplication(ID)) {
					if (ID != null) {// ID가 널값이 아니라면
						output.println(Protocol.LOGIN_SUCCRESS);
						output.flush();
						userList.put(ID, output);
						sendToAll("****" + ID + "님이 대기방에 입장.****");
					}
				} else {
					output.println(Protocol.LOGIN_FAIL);
					output.flush();
				}
				break;

			case Protocol.END:
				sendToAll("**" + ID + "님이 채팅프로그램을 종료하셨습니다.**");
				try {
					input.close();
					output.close();
					threadSocket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			case Protocol.ALL:
				String amsg = token.nextToken();
				amsg = "[" + ID + "]" + amsg;
				sendToAll(amsg);
				break;
			case Protocol.FILE_LIST:
				file_list();
				break;
			case Protocol.ROOM:
				if (mute == false) {
					String rmsg = token.nextToken();
					rmsg = "[" + ID + "]" + rmsg;
					sendToRoom(rmsg);
				} else {
					output.println("##" + ID + "님은 현재 채팅금지이십니다##");
					output.flush();
				}
				break;
			case Protocol.USER_LIST:
				if (enter_room == null)
					user_list();
				else
					ruser_list();
				break;
			case Protocol.ROOM_LIST:
				room_list();
				break;
			case Protocol.ROOM_CREATE:
				String room_name = token.nextToken();
				int max = Integer.parseInt(token.nextToken());

				if (checkRoomName(room_name)) {
					room_create(room_name, max);
				} else {
					output.println(Protocol.ROOM_CREATE_FAIL + ":" + "※중복된 방 이름이 이미 존재합니다.※");
					output.flush();
				}
				break;
			case Protocol.ROOM_ENTER:
				String room_enter = token.nextToken();
				if (roomList.containsKey(room_enter)) {
					if (roomList.get(room_enter).getRoom_userList().size() < roomList.get(room_enter).getMax()) {

						roomList.get(room_enter).Add(ID, output);
						userList.remove(ID);
						output.println(Protocol.ROOM_ENTER + ":" + room_enter);
						output.flush();
						enter_room = room_enter;
						sendToRoom("**" + ID + "님이 방에 입장하셨습니다**");

					} else {
						output.println(Protocol.ROOM_ENTER_FAIL + ":" + "※방이 꽉찼습니다.※");
						output.flush();
					}
				} else {
					output.println(Protocol.ROOM_ENTER_FAIL + ":" + "※방이 존재하지 않습니다.※");
					output.flush();
				}
				break;
			case Protocol.ROOM_EXIT:
				delete(ID);
				userList.put(ID, output);
				break;
			case Protocol.WHISPER:
				String desID = token.nextToken();// 목적지 ID
				String wmsg = token.nextToken();// 보낼 메시지
				sendToSingle(desID, wmsg);
				break;
			case Protocol.MUTE:
				String mute_name = token.nextToken();
				if (ID.equals(roomList.get(enter_room).getRoom_master())) {
					if (roomList.get(enter_room).getRoom_userList().containsKey(mute_name)) {
						PrintWriter pw = roomList.get(enter_room).getRoom_userList().get(mute_name);
						pw.println(Protocol.MUTED);
						pw.flush();
						sendToRoom(ID + "님이 " + mute_name + "님을 채팅 금지시켰습니다");
					} else {
						output.println("※존재하지 않는 ID입니다※");
						output.flush();
					}
				} else {
					output.println("※권한이 없습니다.※");
					output.flush();
				}
				break;
			case Protocol.FILE_SEND:
				String file_name = token.nextToken();
				String client_IP = token.nextToken();
				new FileReceiver(client_IP, file_name, dir).start();
				break;

			case Protocol.FILE_DOWN:
				String file_down = token.nextToken();
				new FileSender(file_down, dir).start();
				;
				break;
			case Protocol.GET_OUT:// 강퇴 할 때
				String out_name = token.nextToken();
				get_out(out_name);
				break;
			case Protocol.GET_OUTED:// 강퇴 당할 때
				get_outed();
				break;
			case Protocol.MAN_DATE:// 방장 권한 넘기기
				String md = token.nextToken();
				if (ID.equals(roomList.get(enter_room).getRoom_master())) {
					roomList.get(enter_room).setRoom_master(md);
					sendToRoom("***방장이 " + md + "님으로 변경되었습니다.***");
				} else {
					output.println("※권한이 없습니다※");
					output.flush();
				}
				break;
			}
		}

		private void file_list() {
			String msg = Protocol.FILE_LIST;
			File files = new File(dir);
			String[] list = files.list();
			for (int i = 0; i < list.length; i++) {
				File file = new File(dir, list[i]);
				if (file.isFile()) {
					msg += ":" + list[i];
				}
			}

			output.println(msg);
			output.flush();
		}

		private void get_outed() {
			Room room = roomList.get(enter_room);
			room.getRoom_userList().remove(ID);
			userList.put(ID, output);

			output.println(enter_room + "방에서 강퇴당하셨습니다.");
			output.flush();

			enter_room = null;// 초기화

		}

		// ????
		private void get_out(String out_name) {
			if (roomList.get(enter_room).getRoom_userList().containsKey(out_name)) {
				if (ID.equals(roomList.get(enter_room).getRoom_master())) {
					Room room = roomList.get(enter_room);
					PrintWriter pw = room.getRoom_userList().get(out_name);
					pw.println(Protocol.GET_OUTED);
					pw.flush();
					sendToRoom(out_name + "님이 방장에 의해 강퇴당하셨습니다");
				} else {
					output.println("※권한이 없습니다.※");
					output.flush();
				}
			} else {
				output.println("※존재 하지 않는 ID입니다※");
				output.flush();

			}
		}

		private void ruser_list() {

			String msg = Protocol.USER_LIST;

			Room room = roomList.get(enter_room);
			Iterator<String> iterator = room.getRoom_userList().keySet().iterator();
			while (iterator.hasNext()) {
				String id = iterator.next();
				if (id.equals(roomList.get(enter_room).getRoom_master()))
					id = "[방장]" + id;
				msg = msg + ":" + id;
			}

			output.println(msg);
			output.flush();

		}

		//
		private void sendToSingle(String desID, String wmsg) {
			if (enter_room == null) {
				if (userList.containsKey(desID)) {
					PrintWriter pw = userList.get(desID);
					pw.println("[귓속말|" + ID + "]" + wmsg);
					pw.flush();
				} else {
					output.println("※해당 유저가 존재하지 않습니다!※");
					output.flush();
				}
			} else { //
				if (roomList.get(enter_room).getRoom_userList().containsKey(desID)) {
					PrintWriter pw = roomList.get(enter_room).getRoom_userList().get(desID);
					pw.println("[귓속말|" + ID + "]" + wmsg);
					pw.flush();
				} else {
					output.println("※해당 유저가 존재하지 않습니다!※");
					output.flush();
				}
			}

		}

		private void sendToRoom(String msg) {
			Iterator<String> iterator = roomList.get(enter_room).getRoom_userList().keySet().iterator();
			while (iterator.hasNext()) {
				PrintWriter pw = roomList.get(enter_room).getRoom_userList().get(iterator.next());
				pw.println(msg);
				pw.flush();

			}
		}

		// ?? ?????
		private void room_list() {
			String msg = Protocol.ROOM_LIST;
			if (roomList.size() != 0) {
				Iterator<String> iterator = roomList.keySet().iterator();
				while (iterator.hasNext()) {
					msg = msg + ":" + iterator.next();
				}
			} else {
				msg += ": **생성된 방이 없음**";
			}
			output.println(msg);
			output.flush();
		}

		private void room_create(String room_name, int max) {
			userList.remove(ID);
			Room room = new Room(max, ID, output, room_name);
			roomList.put(room_name, room);
			output.println(Protocol.ROOM_CREATE + ":" + room_name);
			output.flush();
			enter_room = room_name;
			sendToAll("**" + ID + "님이 " + room_name + "방을 개설하셨습니다.**");

		}

		private void user_list() {
			String msg = Protocol.USER_LIST;
			Iterator<String> iterator = userList.keySet().iterator();
			while (iterator.hasNext()) {
				msg = msg + ":" + iterator.next();
			}
			output.println(msg);
			output.flush();
		}

		private boolean duplication(String ID) {
			boolean check = true;
			boolean check2 = true;
			Iterator<String> iterator = userList.keySet().iterator();
			while (iterator.hasNext()) {
				if (iterator.next().equals(ID)) {
					check = false;
				}
			}
			Iterator<String> riterator = roomList.keySet().iterator();
			while (riterator.hasNext()) {
				Room room = roomList.get(riterator.next());
				Iterator<String> uiterator = room.getRoom_userList().keySet().iterator();
				while (uiterator.hasNext()) {
					if (uiterator.next().equals(ID)) {
						check2 = false;
					}
				}

			}
			return check && check2;
		}

		private boolean checkRoomName(String room_name) {
			Iterator<String> iterator = roomList.keySet().iterator();
			while (iterator.hasNext()) {
				if (iterator.next().equals(room_name))
					return false;
			}
			return true;
		}
	}

}
