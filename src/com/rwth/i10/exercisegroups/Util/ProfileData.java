package com.rwth.i10.exercisegroups.Util;

public class ProfileData {

	
	private String username, displayName, msg_id, session, email, desc, status;
	private int event_id;
	private boolean publicProfile;
	
	public ProfileData() {
		// TODO Auto-generated constructor stub
	}
	
	public ProfileData(String username, String session, String email,
			String desc, String displayName, String status, boolean isPublic) {
		// TODO Auto-generated constructor stub
		this.username = username; this.session = session; this.email = email;
		this.desc = desc; this.displayName = displayName; this.status = status;
		this.publicProfile = isPublic;
	}

	public void setUsername(String username) {
		this.username = username;
	}public void setMsg_id(String msg_id) {
		this.msg_id = msg_id;
	}public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}public void setSession(String session) {
		this.session = session;
	}public void setEvent_id(int event_id) {
		this.event_id = event_id;
	}public void setEmail(String email) {
		this.email = email;
	}public void setDesc(String desc) {
		this.desc = desc;
	}public void setPublicProfile(boolean publicProfile) {
		this.publicProfile = publicProfile;
	}public void setStatus(String status) {
		this.status = status;
	}
	
	public String getUsername() {
		return username;
	}public String getMsg_id() {
		return msg_id;
	}public String getDisplayName() {
		return displayName;
	}public String getSession() {
		return session;
	}public int getEvent_id() {
		return event_id;
	}public String getEmail() {
		return email;
	}public String getDesc() {
		return desc;
	}public boolean isPublicProfile() {
		return publicProfile;
	}public String getStatus() {
		return status;
	}
	
	
	public static final String PROFILE_USERNAME = "user_name";
	public static final String PROFILE_MSG_ID = "msg_id";
	public static final String PROFILE_SESSION = "session";
	public static final String PROFILE_DISPLAY_NAME = "display_name";
	public static final String PROFILE_ID = "id";
	public static final String PROFILE_EMAIL = "email";
	public static final String PROFILE_DESC = "description";
	public static final String PROFILE_PUBLIC = "public_profile";
}
