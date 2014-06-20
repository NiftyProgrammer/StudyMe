package com.rwth.i10.exercisegroups.database;

import com.rwth.i10.exercisegroups.Util.GroupData;
import com.rwth.i10.exercisegroups.Util.UserData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

public class GroupsDataSource {

	private SQLiteHelper dbHelper;
	private SQLiteDatabase database;
	private String[] allColumns = {SQLiteHelper.TABLE_ID, SQLiteHelper.TABLE_AVTIVITY,
			SQLiteHelper.TABLE_COURSE, SQLiteHelper.TABLE_ADDRESS, SQLiteHelper.TABLE_STATUS,
			SQLiteHelper.TABLE_MAX_PART, SQLiteHelper.TABLE_LAT, SQLiteHelper.TABLE_LNG, 
			SQLiteHelper.TABLE_IMAGE};
	
	private String[] userAllColumns = { SQLiteHelper.UTABLE_NAME, SQLiteHelper.UTABLE_ID,
			SQLiteHelper.UTABLE_PUB, SQLiteHelper.UTABLE_DNAME, SQLiteHelper.UTABLE_EMAIL, SQLiteHelper.UTABLE_DETAILS};
	
	
	public GroupsDataSource(Context context) {
		// TODO Auto-generated constructor stub
		dbHelper = new SQLiteHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}
	
	public void close(){
		dbHelper.close();
		database.close();
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
		Log.d("values", group.getGroup_id() + " - " + group.getName()
				+ " - " + group.getCourse() + " - " + group.getAddress()
				+ " - " + group.getStatus() + " - " + group.getMaxNumber()
				+ " - " + group.getLat() + " - " + group.getLng());
		/*
		if(group.getImage() != null)
			contValues.put(allColumns[8], group.getImage().getNinePatchChunk());
		else
			contValues.put(allColumns[8], new byte[]{});*/
		long value = 0;
		try {
			value = database.insertOrThrow(SQLiteHelper.TABLE_CREATE, null, contValues);
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			value = database.update(SQLiteHelper.TABLE_CREATE, contValues, allColumns[0] + " = ?", new String[]{group.getGroup_id()});
		}
		if(value > 0)
			return true;
		else
			return false;	
	}
		
	public Cursor getAllStartGroups(){
		return database.query(SQLiteHelper.TABLE_CREATE, allColumns, SQLiteHelper.TABLE_STATUS + " = ?", 
				new String[]{"START"}, null, null, null);
	}
	public Cursor getAllGroups(){
		return database.query(SQLiteHelper.TABLE_CREATE, allColumns, null, null, null, null, null);
	}
	
	public int deleteGroup(String id){
		return database.delete(SQLiteHelper.TABLE_CREATE, allColumns[0] + " = ?", new String[]{id});
	}
	public int deleteGroup(GroupData group){
		return database.delete(SQLiteHelper.TABLE_CREATE, allColumns[0] + " = ?, " +
				allColumns[1] + " = ?, " + allColumns[2] + " = ?", new String[]{group.getGroup_id(),
				group.getName(), group.getCourse()});
	}
	
	public boolean createUser(UserData data){
		ContentValues contValues = new ContentValues();
		contValues.put(userAllColumns[0], data.getUsername());
		contValues.put(userAllColumns[1], data.getId());
		contValues.put(userAllColumns[2], (data.isPublic() ? 1 : 0));
		contValues.put(userAllColumns[3], data.getDisplay_name());
		contValues.put(userAllColumns[4], data.getEmail());
		contValues.put(userAllColumns[5], data.getDetail());
		/*
		if(group.getImage() != null)
			contValues.put(allColumns[8], group.getImage().getNinePatchChunk());
		else
			contValues.put(allColumns[8], new byte[]{});*/
		long value = 0;
		try {
			value = database.insertOrThrow(SQLiteHelper.UTABLE_CREATE, null, contValues);
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			value = database.update(SQLiteHelper.UTABLE_CREATE, contValues, allColumns[0] + " = ?", new String[]{data.getUsername()});
		}
		if(value > 0)
			return true;
		else
			return false;
	}
	
	public UserData[] getUserData(String username, String id){
		UserData []data = null;
		String selection = "", selectionArgs = "";
		
		if(TextUtils.isEmpty(username)){
			selection = SQLiteHelper.UTABLE_ID;
			selectionArgs = id;
		}
		else{
			selection = SQLiteHelper.UTABLE_NAME;
			selectionArgs = username;
		}
		
		Cursor cursor = database.query(SQLiteHelper.UTABLE_CREATE, userAllColumns, selection + " = ?", new String[]{selectionArgs}, null, null, null);
		
		int index = 0;
		while(cursor.moveToNext()){
			data[++index] = new UserData(
					cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_NAME)),
					cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_ID)),
					cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_EMAIL)),
					cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_DETAILS)),
					cursor.getString(cursor.getColumnIndex(SQLiteHelper.UTABLE_DNAME)),
					(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.UTABLE_PUB)) == 0 ? false : true)
					);
		}
		
		return data;
	}
}
