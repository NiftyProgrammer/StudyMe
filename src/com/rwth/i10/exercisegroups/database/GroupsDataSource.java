package com.rwth.i10.exercisegroups.database;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.internal.gr;
import com.rwth.i10.exercisegroups.Activitys.MainActivity;
import com.rwth.i10.exercisegroups.Util.Constants;
import com.rwth.i10.exercisegroups.Util.GroupData;
import com.rwth.i10.exercisegroups.Util.ProfileData;
import com.rwth.i10.exercisegroups.Util.UserStatus;

public class GroupsDataSource {

	private SQLiteHelper dbHelper;
	private SQLiteDatabase writeDatabase, readDatabase;
	private String[] allColumns = {SQLiteHelper.TABLE_ID, SQLiteHelper.TABLE_SESSION, SQLiteHelper.TABLE_AVTIVITY,
			SQLiteHelper.TABLE_COURSE, SQLiteHelper.TABLE_ADDRESS, SQLiteHelper.TABLE_STATUS, SQLiteHelper.TABLE_DESCRIPTION,
			SQLiteHelper.TABLE_MAX_PART, SQLiteHelper.TABLE_LAT, SQLiteHelper.TABLE_LNG,
			SQLiteHelper.TABLE_IMAGE};
	
	private String[] userAllColumns = { SQLiteHelper.UTABLE_NAME, SQLiteHelper.UTABLE_MSGID, 
			SQLiteHelper.UTABLE_GROUPID, SQLiteHelper.UTABLE_STATUS};
	
	private String[] groupMsgAllColumns = { SQLiteHelper.GROUP_MSG_ID, SQLiteHelper.GROUP_MSG_MSGs };
	
	public GroupsDataSource(Context context) {
		// TODO Auto-generated constructor stub
		dbHelper = new SQLiteHelper(context);
	}
	
	public void open() throws SQLException{
		writeDatabase = dbHelper.getWritableDatabase();
		readDatabase = dbHelper.getReadableDatabase();
	}
	
	public void close(){
		readDatabase.close();
		writeDatabase.close();
		dbHelper.close();		
	}
	
	public boolean createGroup(GroupData group){
		ContentValues contValues = new ContentValues();
		contValues.put(allColumns[0], group.getGroupId());
		contValues.put(allColumns[1], group.getGroupSession());
		contValues.put(allColumns[2], group.getName());
		contValues.put(allColumns[3], group.getCourse());
		contValues.put(allColumns[4], group.getAddress());
		contValues.put(allColumns[5], group.getStatus());
		contValues.put(allColumns[6], group.getDescription());
		contValues.put(allColumns[7], group.getMaxNumber());
		contValues.put(allColumns[8], group.getLat());
		contValues.put(allColumns[9], group.getLng());
		
		if(group.getImage() != null){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			group.getImage().compress(Bitmap.CompressFormat.JPEG, 100, baos);
			contValues.put(allColumns[10], baos.toByteArray());
		}
		long value = 0;
		try {
			value = writeDatabase.insertOrThrow(SQLiteHelper.TABLE_CREATE, null, contValues);
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			value = writeDatabase.update(SQLiteHelper.TABLE_CREATE, contValues, allColumns[0] + " = ?", new String[]{group.getGroupId()});
		}
		if(value > 0)
			return true;
		else
			return false;	
	}
	
	public boolean setGroupSession(String id, String session){
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.TABLE_SESSION, session);
		return writeDatabase.update(SQLiteHelper.TABLE_CREATE, values, SQLiteHelper.TABLE_ID + " = ?", new String[]{id}) > 0;
	}
	
	public Cursor getAllStartGroups(){
		Cursor cursor = readDatabase.query(SQLiteHelper.TABLE_CREATE, allColumns, SQLiteHelper.TABLE_STATUS + " = ?", 
				new String[]{"START"}, null, null, null);
		if(cursor == null || cursor.isAfterLast())
			cursor = readDatabase.rawQuery("SELECT * FROM " + SQLiteHelper.TABLE_CREATE
		+ " WHERE " + SQLiteHelper.TABLE_STATUS + "=?", new String[]{"START"});
		return cursor;
	}
	public Cursor getAllGroups(){
		Cursor cursor = readDatabase.query(SQLiteHelper.TABLE_CREATE, allColumns, null, null, null, null, null); 
		if(cursor == null || cursor.isAfterLast())
			cursor = readDatabase.rawQuery("SELECT * FROM " + SQLiteHelper.TABLE_CREATE, null);
		return cursor;
	}	
	public GroupData getGroupData(String id){				
		Cursor cursor = readDatabase.query(SQLiteHelper.TABLE_CREATE, allColumns, SQLiteHelper.TABLE_ID + " = ?", 
				new String[]{id}, null, null, null);
		if(cursor == null || cursor.isAfterLast())
			cursor = readDatabase.rawQuery("SELECT * FROM " + SQLiteHelper.TABLE_CREATE
		+ " WHERE " + SQLiteHelper.TABLE_ID + "=?", new String[]{id});
		
		if(cursor.moveToFirst()){
			GroupData data = new GroupData();
			data.setAddress(cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_ADDRESS)));
			data.setCourse(cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_COURSE)));
			data.setDescription(cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_DESCRIPTION)));
			data.setGroupId(cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_ID)));
			data.setGroupSession(cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_SESSION)));
			data.setLat(cursor.getDouble(cursor.getColumnIndex(SQLiteHelper.TABLE_LAT)));
			data.setLng(cursor.getDouble(cursor.getColumnIndex(SQLiteHelper.TABLE_LNG)));
			data.setMaxNumber(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.TABLE_MAX_PART)));
			data.setName(cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_AVTIVITY)));
			data.setStatus(cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_STATUS)));
			return data;
		}
		
		return null;
	}
	
	/*public GroupData getGroup(String id){
		
		Cursor cursor = readDatabase.query(SQLiteHelper.TABLE_CREATE, allColumns, SQLiteHelper.TABLE_ID + " = ?", new String[]{id}, null, null, null);
		if(cursor == null || cursor.isAfterLast())
			cursor = readDatabase.rawQuery("SELECT * FROM " + SQLiteHelper.TABLE_CREATE + " WHERE " + SQLiteHelper.TABLE_ID + " = ?", new String[]{id});
		if(cursor.moveToFirst()){
			GroupData data = new GroupData();
			data.setAddress(cursor.getString(cursor.getColumnIndex(SQLiteHelper.TABLE_ADDRESS)));
			data.setAdmin(cursor.getString(cursor.getColumnIndex(SQLiteHelper.)));
		}
		
		return null;
	}*/
	public boolean updateGroup(GroupData group){
		ContentValues contValues = new ContentValues();
		contValues.put(allColumns[1], group.getGroupSession());
		contValues.put(allColumns[2], group.getName());
		contValues.put(allColumns[3], group.getCourse());
		contValues.put(allColumns[4], group.getAddress());
		contValues.put(allColumns[5], group.getStatus());
		contValues.put(allColumns[6], group.getDescription());
		contValues.put(allColumns[7], group.getMaxNumber());
		contValues.put(allColumns[8], group.getLat());
		contValues.put(allColumns[9], group.getLng());
		
		if(group.getImage() != null){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			group.getImage().compress(Bitmap.CompressFormat.JPEG, 100, baos);
			contValues.put(allColumns[10], baos.toByteArray());
		}
		return writeDatabase.update(SQLiteHelper.TABLE_CREATE, contValues, SQLiteHelper.TABLE_ID + " = ?", new String[]{group.getGroupId()}) > 0;
	}
	
	public int deleteGroup(String id){
		return writeDatabase.delete(SQLiteHelper.TABLE_CREATE, allColumns[0] + " = ?", new String[]{id});
	}
	
	
	public boolean createUser(ProfileData data, String group_id){
		ContentValues contValues = new ContentValues();
		contValues.put(userAllColumns[0], data.getUsername());
		contValues.put(userAllColumns[1], data.getMsg_id());
		contValues.put(userAllColumns[2], group_id);
		contValues.put(userAllColumns[3], data.getStatus());
		/*
		if(group.getImage() != null)
			contValues.put(allColumns[8], group.getImage().getNinePatchChunk());
		else
			contValues.put(allColumns[8], new byte[]{});*/
		long value = 0;
		try {
			value = writeDatabase.insertOrThrow(SQLiteHelper.UTABLE_CREATE, null, contValues);
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			value = writeDatabase.update(SQLiteHelper.UTABLE_CREATE, contValues, userAllColumns[0] + " = ?", new String[]{data.getUsername()});
		}
		if(value > 0)
			return true;
		else
			return false;
	}
	
	public boolean updateUserStatus(String username, int status){
		ContentValues contValues = new ContentValues();
		contValues.put(userAllColumns[3], status);
		
		return writeDatabase.update(SQLiteHelper.UTABLE_CREATE, contValues, userAllColumns[0] + " = ?", new String[]{username}) > 0;
		
	}
	
	public ArrayList<ProfileData> getUserData(String username, String id){
		ArrayList<ProfileData> data = new ArrayList<ProfileData>();
		String selection = "", selectionArgs = "";
		
		if(TextUtils.isEmpty(username)){
			selection = SQLiteHelper.UTABLE_MSGID;
			selectionArgs = id;
		}
		else{
			selection = SQLiteHelper.UTABLE_NAME;
			selectionArgs = username;
		}
		
		Cursor cursor = readDatabase.query(SQLiteHelper.UTABLE_CREATE, userAllColumns, selection + " = ?", new String[]{selectionArgs}, null, null, null);
	
		while(cursor.moveToNext()){
			ProfileData user = new ProfileData();
			user.setUsername(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_NAME)));
			user.setMsg_id(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_MSGID)));
			user.setStatus(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.UTABLE_STATUS)));
			user.setSession(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_GROUPID)));
			data.add(user);
			
		}
		
		return data;
	}
	
	public ArrayList<ProfileData> getUsers(int status){
		ArrayList<ProfileData> data = new ArrayList<ProfileData>();
		
		Cursor cursor = readDatabase.query(SQLiteHelper.UTABLE_CREATE, userAllColumns, SQLiteHelper.UTABLE_STATUS + " = ?", new String[]{String.valueOf(status)}, null, null, null);
		
		while(cursor.moveToNext()){
			ProfileData user = new ProfileData();
			user.setUsername(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_NAME)));
			user.setMsg_id(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_MSGID)));
			user.setStatus(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.UTABLE_STATUS)));
			user.setSession(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_GROUPID)));
			data.add(user);			
		}
		
		return data;
	}
	
	public int getUsersCount(int status){
		Cursor cursor = readDatabase.query(SQLiteHelper.UTABLE_CREATE, userAllColumns, SQLiteHelper.UTABLE_STATUS + " = ?", new String[]{String.valueOf(status)}, null, null, null);
		
		return cursor.getCount();
	}
	
	public ProfileData getUserData(String username){
		Cursor cursor = readDatabase.query(
				SQLiteHelper.UTABLE_CREATE, userAllColumns, SQLiteHelper.UTABLE_NAME + " = ?", 
				new String[]{String.valueOf(username)}, null, null, null);

		ProfileData user = new ProfileData();
		if(cursor.moveToNext()){
			user.setUsername(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_NAME)));
			user.setMsg_id(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_MSGID)));
			user.setStatus(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.UTABLE_STATUS)));
			user.setSession(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_GROUPID)));			
		}
		
		return user;
	}
	
	public boolean deleteUser(String username){
		return writeDatabase.delete(SQLiteHelper.UTABLE_CREATE, SQLiteHelper.UTABLE_NAME + " = ?", new String[]{username}) > 0;
	}
	
	public boolean addNewGroupMessage(String id, String msgs){
		
		ContentValues values = new ContentValues();
		values.put(groupMsgAllColumns[0], id);
		values.put(groupMsgAllColumns[1], msgs);
		
		long value = 0;
		try {
			value = writeDatabase.insertOrThrow(SQLiteHelper.GROUP_MSG_TABLE, null, values);
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			value = writeDatabase.update(SQLiteHelper.GROUP_MSG_TABLE, values, groupMsgAllColumns[0] + " = ?", new String[]{id});
		}
		if(value > 0)
			return true;
		else
			return false;
	}
	
	public boolean addNewGroupMessages(String id, String newMsg){
		String prevMsgs = getGroupMsg(id);
		String allMsgs = prevMsgs + Constants.KEY_SEPRATOR + newMsg;
		
		ContentValues values = new ContentValues();
		values.put(groupMsgAllColumns[0], id);
		values.put(groupMsgAllColumns[1], allMsgs);
		
		return writeDatabase.update(SQLiteHelper.GROUP_MSG_TABLE, values, groupMsgAllColumns[0] + " = ?", new String[]{id}) > 0;
	}
	
	public String getGroupMsg(String id){
		Cursor cursor = readDatabase.query(SQLiteHelper.GROUP_MSG_TABLE, groupMsgAllColumns, SQLiteHelper.GROUP_MSG_ID + " = ?", new String[]{id}, null, null, null);
		if(cursor == null || cursor.isAfterLast())
			cursor = readDatabase.rawQuery("SELECT * FROM " + SQLiteHelper.GROUP_MSG_TABLE + " WHERE " + SQLiteHelper.GROUP_MSG_ID + " = ?", new String[]{id});
		
		if(cursor.moveToFirst()){
			String msg = cursor.getString(cursor.getColumnIndex(SQLiteHelper.GROUP_MSG_MSGs));
			return msg;
		}
		
		return null;
	}		
	
	public boolean isGroupMsgExists(String id){
		Cursor cursor = readDatabase.query(SQLiteHelper.GROUP_MSG_TABLE, groupMsgAllColumns, SQLiteHelper.GROUP_MSG_ID + " = ?", new String[]{id}, null, null, null);
		return cursor.getCount() > 0;
	}
	
	public boolean removeGroupMsg(String id){
		return writeDatabase.delete(SQLiteHelper.GROUP_MSG_TABLE, SQLiteHelper.GROUP_MSG_ID + " = ?", new String[]{id}) > 0;
	}
}
