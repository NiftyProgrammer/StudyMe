package com.rwth.i10.exercisegroups.Util;

public class ProfileData {

	
	private String username, displayName, msg_id, session, event_id, email, desc;
	
	public ProfileData() {
		// TODO Auto-generated constructor stub
	}
	
	public void setUsername(String username) {
		this.username = username;
	}public void setMsg_id(String msg_id) {
		this.msg_id = msg_id;
	}public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}public void setSession(String session) {
		this.session = session;
	}public void setEvent_id(String event_id) {
		this.event_id = event_id;
	}public void setEmail(String email) {
		this.email = email;
	}public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public String getUsername() {
		return username;
	}public String getMsg_id() {
		return msg_id;
	}public String getDisplayName() {
		return displayName;
	}public String getSession() {
		return session;
	}public String getEvent_id() {
		return event_id;
	}public String getEmail() {
		return email;
	}public String getDesc() {
		return desc;
	}
	
	
	public static final String PROFILE_USERNAME = "user_name";
	public static final String PROFILE_MSG_ID = "msg_id";
	public static final String PROFILE_SESSION = "session";
	public static final String PROFILE_DISPLAY_NAME = "display_name";
	public static final String PROFILE_ID = "id";
	public static final String PROFILE_EMAIL = "email";
	public static final String PROFILE_DESC = "description";
	
}
