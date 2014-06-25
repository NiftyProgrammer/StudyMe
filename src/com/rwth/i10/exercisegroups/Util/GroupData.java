package com.rwth.i10.exercisegroups.Util;

import android.graphics.Bitmap;

public class GroupData {

	private String name, course, group_id, address, status, description;
	private String admin;
	private Bitmap image;
	private double lat, lng;
	private long timestamp;
	private int MaxNumber;
	
	public GroupData(String name, String course, String address, int MaxNumber, Bitmap image){
		this.name = name;
		this.course = course;
		this.address = address;
		this.MaxNumber = MaxNumber;
		this.image = image;
	}
	public GroupData() {
		// TODO Auto-generated constructor stub
	}
	
	public void setName(String name) {
		this.name = name;
	}public void setMaxNumber(int maxNumber) {
		MaxNumber = maxNumber;
	}public void setImage(Bitmap image) {
		this.image = image;
	}public void setCourse(String course) {
		this.course = course;
	}public void setGroup_id(String group_id) {
		this.group_id = group_id;
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
	}
	public String getName() {
		return name;
	}public int getMaxNumber() {
		return MaxNumber;
	}public Bitmap getImage() {
		return image;
	}public String getCourse() {
		return course;
	}public String getGroup_id() {
		return group_id;
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
	}	
}
