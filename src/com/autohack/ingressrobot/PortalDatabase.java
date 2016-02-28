package com.autohack.ingressrobot;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PortalDatabase extends SQLiteOpenHelper {

	public static final String TABLE_NAME = "Portal";
	public static final String NO = "NO";
	public static final String LAT = "LAT";
	public static final String LON = "LON";
	public static final String FIRST = "First";
	public static final String LAST = "LAST";
	public static final String COUNT = "count";

	public static final String INFO_TABLE_NAME = "Info";
	public static final String TOTAL = "TOTAL";
	public static final String NEXT = "NEXT";
	public static final String WAIT = "Wait";
	
	private final static String DATABASE_NAME = "portal.db";
	private final static int DATABASE_VERSION = 1;

	public PortalDatabase(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	
	
	@Override
	public void onCreate(SQLiteDatabase db) {
        final String INIT_TABLE = 
        		"CREATE TABLE " + TABLE_NAME + " (" +
                NO + " INTEGER PRIMARY KEY, " +
                LAT + " CHAR, " +
                LON + " CHAR, " +
                FIRST + " BIGINT, " +
                LAST + " BIGINT, " +
                COUNT + " INT);"; 
        db.execSQL(INIT_TABLE);
        
        final String INIT_INFO_TABLE = 
        		"CREATE TABLE " + INFO_TABLE_NAME + " (" +
                TOTAL + " INTEGER PRIMARY KEY, " +
                NEXT + " INTEGER,"+
                WAIT + " BIGINT);"; 
        db.execSQL(INIT_INFO_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		 final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	     db.execSQL(DROP_TABLE);
	     onCreate(db);

	}

}
