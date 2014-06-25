package com.rwth.i10.exercisegroups.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

	public static final String 	TABLE_CREATE = "Groups",
								TABLE_ID = "id",
								TABLE_AVTIVITY = "activity",
								TABLE_COURSE = "course",
								TABLE_ADDRESS = "address",
								TABLE_STATUS = "status",
								TABLE_MAX_PART = "max_part",
								TABLE_LAT = "lat",
								TABLE_LNG = "lng",
								TABLE_IMAGE = "image";
	
	public static final String UTABLE_CREATE = "Users",
								UTABLE_ID = "id",
								UTABLE_NAME = "user_name",
								UTABLE_PUB = "is_public",
								UTABLE_DNAME = "display_name",
								UTABLE_EMAIL = "email",
								UTABLE_DETAILS = "details",
								UTABLE_STATUS = "status";
	
	public static final String GROUP_MSG_TABLE = "GroupMsgs",
								GROUP_MSG_ID = "id",
								GROUP_MSG_MSGs = "msgs";
	
	private static final String DATABASE_NAME = "groups.db";
	private static final int DATABASE_VERSION = 2;
	
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_CREATE + "(" + TABLE_ID + " text primary key, "
			+ TABLE_AVTIVITY + " text not null, "
			+ TABLE_COURSE + " text not null, "
			+ TABLE_ADDRESS + " text, "
			+ TABLE_STATUS + " text not null, "
			+ TABLE_MAX_PART + " int not null, "
			+ TABLE_LAT + " double, "
			+ TABLE_LNG + " double, "
			+ TABLE_IMAGE + " blob);"; 
	
	private static final String USER_CREATE_TABLE = "create table "
			+ UTABLE_CREATE + " (" + UTABLE_NAME + " text primary key, "
			+ UTABLE_ID + " text not null, "
			+ UTABLE_PUB + " int not null, "
			+ UTABLE_DNAME + " text, "
			+ UTABLE_EMAIL + " text, "
			+ UTABLE_DETAILS + " text, " 
			+ UTABLE_STATUS + " text);";
	
	private static final String GROUP_MSG_CREATE_TABLE = "create table "
			+ GROUP_MSG_TABLE + " (" + GROUP_MSG_ID + " text primary key, "
			+ GROUP_MSG_MSGs + " text);";
	
	public SQLiteHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		// TODO Auto-generated method stub
		database.execSQL(DATABASE_CREATE);
		database.execSQL(USER_CREATE_TABLE);
		database.execSQL(GROUP_MSG_CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int arg1, int arg2) {
		// TODO Auto-generated method stub
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_CREATE);
		database.execSQL("DROP TABLE IF EXISTS " + USER_CREATE_TABLE);
		database.execSQL("DROP TABLE IF EXISTS " + GROUP_MSG_CREATE_TABLE);
		onCreate(database);
	}

}
