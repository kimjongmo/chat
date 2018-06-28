


public class Protocol {
	public static final String WHISPER = "/w";
	//귓속말 	/w:상대방 ID:msg
	public static final String USER_LIST = "/ulist";
	//대기방 인원
	public static final String ROOM_LIST = "/rlist";
	//방 리스트
	public static final String ROOM_CREATE = "/create";
	//방 생성 	/create :[방이름] : [최대인원]
	public static final String ROOM_ENTER = "/in";
	//방 입장	/in:[방 이름]
	public static final String ROOM_EXIT = "/exit";
	//방 퇴장			/exit
	public static final String FILE_SEND = "/file";
	//파일 업로드		/file
	public static final String MUTE = "/mute";
	//방장일 경우 아봉 시키기	/mute:[ID]
	public static final String LOGIN  = "/login";
	//서버에 로그인 한다고 알림   /login:[ID]
	public static final String ROOM_CREATE_FAIL = "/ncreate";
	//방 생성 실패 알림  /ncreate:[이유]
	public static final String ROOM_ENTER_FAIL = "/nin";
	//방 입장 실패 알림 /nin:[이유]
	public static final String LOGIN_SUCCRESS = "/avail";
	//로그인 성공 
	public static final String LOGIN_FAIL = "/nvail";
	//로그인 실패 알림  /navil:[이유]
	public static final String ALL = "/all";
	// 대기방끼리 통신하기 위한 프로토콜
	public static final String ROOM = "/room";
	// 방에서 통신하기 위한 프로토콜
	public static final String HELP = "/help";
	// 				/help 
	public static final String MUTED ="/ymute";
	//뮤트 당하는 사람에게 보내는 메시지 받은 사람은 몇 초간 아봉
	public static final String FILE_LIST = "/flist";
	//파일 리스트
	public static final String FILE_DOWN = "/down";
	//파일 요청		/down:[파일명]
	public static final String MAN_DATE = "/md";
	//방장 위임		/md:[destID]
	public static final String GET_OUT = "/getout";
	//강티			/getout:[destID]
	public static final String GET_OUTED ="/getouted";
	//방장이 강퇴시켰다는 신호
	public static final String END = "/end";
}

/*
 * [대기방]
 * 귓속말
 * 방만들기
 * 방입장하기
 * 대기유저리스트
 * 룸리스트
 * 파일 업/다운로드
 * 도움말
 * 
 * 
 * [룸]
 * 룸유저리스트
 * 귓속말
 * 방 나가기
 * 채팅 금지 시키기
 * 강퇴시키기
 * 방장 위임
 * 도움말
 * 파일 업/다운로드
 * */

