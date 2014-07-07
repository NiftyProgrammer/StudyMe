package com.rwth.i10.exercisegroups.Util;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

/**
 * 
 * User to store group all data
 * 
 * */

public class GroupData {

	private String name, course, address, status, description, groupSession, groupId;
	private String admin;
	private Bitmap image;
	private double lat, lng;
	private long timestamp;
	private int MaxNumber;
	private ArrayList<String> users_joined;
	
	public GroupData(String name, String course, String address, int MaxNumber, Bitmap image){
		this.name = name;
		this.course = course;
		this.address = address;
		this.MaxNumber = MaxNumber;
		this.image = image;
		users_joined = new ArrayList<String>();
	}
	public GroupData() {
		// TODO Auto-generated constructor stub
		users_joined = new ArrayList<String>();
	}
	
	public void setName(String name) {
		this.name = name;
	}public void setMaxNumber(int maxNumber) {
		MaxNumber = maxNumber;
	}public void setImage(Bitmap image) {
		this.image = image;
	}public void setCourse(String course) {
		this.course = course;
	}public void setGroupId(String groupId) {
		this.groupId = groupId;
	}public void setAddress(String address) {
		this.address = address;
	}public void setStatus(String status) {
		this.status = status;
	}public void setLat(double lat) {
		this.lat = lat;
	}public void setLng(double lng) {
		this.lng = lng;
	}public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}public void setDescription(String description) {
		this.description = description;
	}public void setAdmin(String admin) {
		this.admin = admin;
	}public void setUsers_joined(ArrayList<String> users_joined) {
		this.users_joined = users_joined;
	}public void addUsers_joined(List<String> users_joined){
		this.users_joined.addAll(users_joined);
	}public void addUsers_joined(String user){
		this.users_joined.add(user);
	}public void setGroupSession(String groupSession) {
		this.groupSession = groupSession;
	}
	public String getName() {
		return name;
	}public int getMaxNumber() {
		return MaxNumber;
	}public Bitmap getImage() {
		return image;
	}public String getCourse() {
		return course;
	}public String getGroupId() {
		return groupId;
	}public String getAddress() {
		return address;
	}public String getStatus() {
		return status;
	}public double getLat() {
		return lat;
	}public double getLng() {
		return lng;
	}public long getTimestamp() {
		return timestamp;
	}public String getDescription() {
		return description;
	}public String getAdmin() {
		return admin;
	}public ArrayList<String> getUsers_joined() {
		return users_joined;
	}public boolean isUser_joined(String user){
		return users_joined.contains(user);
	}public String remove_user_joined(String user){
		for(String userName : users_joined){
			if(userName.equals(user))
				return userName;
		}
		return null;
	}public String getGroupSession() {
		return groupSession;
	}
	
	
	public boolean equals(GroupData o) {
		// TODO Auto-generated method stub
		boolean isEqual = true;
		if(!this.groupId.equals(o.getGroupId()))
			isEqual = false;
		if(!this.name.equals(o.getName()))
			isEqual = false;
		if(!this.course.equals(o.getCourse()))
			isEqual = false;
		if(!this.admin.equals(o.getAdmin()))
			isEqual = false;
		
		return isEqual;
	}
}
