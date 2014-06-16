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
	
	private static final String DATABASE_NAME = "groups.db";
	private static final int DATABASE_VERSION = 1;
	
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
	
	public SQLiteHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		// TODO Auto-generated method stub
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int arg1, int arg2) {
		// TODO Auto-generated method stub
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_CREATE);
		onCreate(database);
	}

}
