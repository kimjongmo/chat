


import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

public class Room {
	private int max; 			//최대 인원
	private String room_master;	//방장
	private ConcurrentHashMap<String,PrintWriter> room_userList;	//방에 있는 유저리스트
	private String room_name;
	
	public Room(int max,String room_master,PrintWriter pw,String room_name){
		this.max = max;
		this.room_master = room_master;
		this.room_name = room_name;
		room_userList = new ConcurrentHashMap<String,PrintWriter>(max);		//최대 인원 결정	
		room_userList.put(room_master,pw);
	}
	
	public String getRoom_name(){
		return room_name;
	}
	public int getMax() {
		return max;
	}

	public String getRoom_master() {
		return room_master;
	}

	public void setRoom_master(String room_master) {
		this.room_master = room_master;
	}

	public ConcurrentHashMap<String, PrintWriter> getRoom_userList() {
		return room_userList;
	}

	public boolean deleteID(String ID) throws NullPointerException{
		room_userList.remove(ID);
		
		if(room_userList.size()==0)
			return false;
		else
			return true;
	}
	
	public void Add(String ID,PrintWriter pw){
		room_userList.put(ID,pw);
	}
}
