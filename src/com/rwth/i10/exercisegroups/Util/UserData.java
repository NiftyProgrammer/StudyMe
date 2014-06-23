package com.rwth.i10.exercisegroups.Util;

public class UserData {

	private String username, id, display_name, email, detail;
	private boolean isPublic;
	
	public UserData() {
		// TODO Auto-generated constructor stub
	}

	public UserData(String name, String id, String email,
			String detail, String dname, boolean pub) {
		// TODO Auto-generated constructor stub
		this.username = name; this.id = id; this.email = email;
		this.detail = detail; this.display_name = dname; this.isPublic = pub;
	}

	public void setUsername(String username) {
		this.username = username;
	}public void setId(String id) {
		this.id = id;
	}public void setEmail(String email) {
		this.email = email;
	}public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}public void setDetail(String detail) {
		this.detail = detail;
	}public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}
	
	public String getUsername() {
		return username;
	}public String getId() {
		return id;
	}public String getEmail() {
		return email;
	}public String getDisplay_name() {
		return display_name;
	}public String getDetail() {
		return detail;
	}public boolean isPublic() {
		return isPublic;
	}

}
