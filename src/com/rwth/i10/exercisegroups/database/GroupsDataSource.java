package com.rwth.i10.exercisegroups.database;

import java.io.ByteArrayOutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.rwth.i10.exercisegroups.Util.Constants;
import com.rwth.i10.exercisegroups.Util.GroupData;
import com.rwth.i10.exercisegroups.Util.ProfileData;

public class GroupsDataSource {

	private SQLiteHelper dbHelper;
	private SQLiteDatabase writeDatabase, readDatabase;
	private String[] allColumns = {SQLiteHelper.TABLE_ID, SQLiteHelper.TABLE_AVTIVITY,
			SQLiteHelper.TABLE_COURSE, SQLiteHelper.TABLE_ADDRESS, SQLiteHelper.TABLE_STATUS,
			SQLiteHelper.TABLE_MAX_PART, SQLiteHelper.TABLE_LAT, SQLiteHelper.TABLE_LNG,
			SQLiteHelper.TABLE_IMAGE};
	
	private String[] userAllColumns = { SQLiteHelper.UTABLE_NAME, SQLiteHelper.UTABLE_ID,
			SQLiteHelper.UTABLE_PUB, SQLiteHelper.UTABLE_DNAME, SQLiteHelper.UTABLE_EMAIL, SQLiteHelper.UTABLE_DETAILS, SQLiteHelper.UTABLE_STATUS};
	
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
		contValues.put(allColumns[0], group.getGroup_id());
		contValues.put(allColumns[1], group.getName());
		contValues.put(allColumns[2], group.getCourse());
		contValues.put(allColumns[3], group.getAddress());
		contValues.put(allColumns[4], group.getStatus());
		contValues.put(allColumns[5], group.getMaxNumber());
		contValues.put(allColumns[6], group.getLat());
		contValues.put(allColumns[7], group.getLng());
		
		if(group.getImage() != null){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			group.getImage().compress(Bitmap.CompressFormat.JPEG, 100, baos);
			contValues.put(allColumns[9], baos.toByteArray());
		}
		long value = 0;
		try {
			value = writeDatabase.insertOrThrow(SQLiteHelper.TABLE_CREATE, null, contValues);
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			value = writeDatabase.update(SQLiteHelper.TABLE_CREATE, contValues, allColumns[0] + " = ?", new String[]{group.getGroup_id()});
		}
		if(value > 0)
			return true;
		else
			return false;	
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
	
	public int deleteGroup(String id){
		return writeDatabase.delete(SQLiteHelper.TABLE_CREATE, allColumns[0] + " = ?", new String[]{id});
	}
	public int deleteGroup(GroupData group){
		return writeDatabase.delete(SQLiteHelper.TABLE_CREATE, allColumns[0] + " = ?, " +
				allColumns[1] + " = ?, " + allColumns[2] + " = ?", new String[]{group.getGroup_id(),
				group.getName(), group.getCourse()});
	}
	
	
	public boolean createUser(ProfileData data){
		ContentValues contValues = new ContentValues();
		contValues.put(userAllColumns[0], data.getUsername());
		contValues.put(userAllColumns[1], data.getSession());
		contValues.put(userAllColumns[2], (data.isPublicProfile() ? 1 : 0));
		contValues.put(userAllColumns[3], data.getDisplayName());
		contValues.put(userAllColumns[4], data.getEmail());
		contValues.put(userAllColumns[5], data.getDesc());
		contValues.put(userAllColumns[6], data.getStatus());
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
			value = writeDatabase.update(SQLiteHelper.UTABLE_CREATE, contValues, allColumns[0] + " = ?", new String[]{data.getUsername()});
		}
		if(value > 0)
			return true;
		else
			return false;
	}
	
	public ProfileData[] getUserData(String username, String id){
		ProfileData []data = null;
		String selection = "", selectionArgs = "";
		
		if(TextUtils.isEmpty(username)){
			selection = SQLiteHelper.UTABLE_ID;
			selectionArgs = id;
		}
		else{
			selection = SQLiteHelper.UTABLE_NAME;
			selectionArgs = username;
		}
		
		Cursor cursor = readDatabase.query(SQLiteHelper.UTABLE_CREATE, userAllColumns, selection + " = ?", new String[]{selectionArgs}, null, null, null);
		
		int index = 0;
		while(cursor.moveToNext()){
			data[++index] = new ProfileData(
					cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_NAME)),
					cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_ID)),
					cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_EMAIL)),
					cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_DETAILS)),
					cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_DNAME)),
					cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_STATUS)),
					(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.UTABLE_PUB)) == 0 ? false : true)
					);
		}
		
		return data;
	}
	
	
	public boolean addNewGroup(String id, String msgs){
		
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
		String prevMsgs = getGroupData(id);
		String allMsgs = prevMsgs + Constants.KEY_SEPRATOR + newMsg;
		
		return false;
	}
	
	public String getGroupData(String id){
		Cursor cursor = readDatabase.query(SQLiteHelper.GROUP_MSG_TABLE, groupMsgAllColumns, SQLiteHelper.GROUP_MSG_ID + " = ?", new String[]{id}, null, null, null);
		if(cursor == null || cursor.isAfterLast())
			cursor = readDatabase.rawQuery("SELECT * FROM " + SQLiteHelper.GROUP_MSG_TABLE + " WHERE " + SQLiteHelper.GROUP_MSG_ID + " = ?", new String[]{id});
		
		if(cursor.moveToFirst()){
			String msg = cursor.getString(cursor.getColumnIndex(SQLiteHelper.GROUP_MSG_MSGs));
			return msg;
		}
		
		return null;
	}
}
