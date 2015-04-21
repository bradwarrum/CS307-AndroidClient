package com.example.android.virtualpantry.Database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

/**
 * Created by Brad on 4/19/2015.
 */
class VPDatabaseHandler extends SQLiteOpenHelper {


    private static final int VP_SCHEMA_VERSION = 1;
    private static final String VP_SCHEMA_NAME = "virtualpantry.db";

    private static VPDatabaseHandler dbhandler = null;

    public static VPDatabaseHandler getInstance(Context context) {
        if (dbhandler == null) {
            dbhandler = new VPDatabaseHandler(context.getApplicationContext());
        }
        return dbhandler;
    }


    /**
     * Private constructor to prevent connection leaks.  Use static method to fetch instance.
     * @param context
     */
    private VPDatabaseHandler(Context context) {
        super(context, VP_SCHEMA_NAME, null, VP_SCHEMA_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Households ("
                +"ID LONG PRIMARY KEY,"
                +"Name TEXT NOT NULL,"
                +"Description TEXT,"
                +"Version LONG);");
        db.execSQL("CREATE TABLE UserInfo ("
                + "ID LONG PRIMARY KEY,"
                + "Email TEXT,"
                + "FirstName TEXT,"
                + "LastName TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Households;");
        db.execSQL("DROP TABLE IF EXISTS UserInfo;");
        onCreate(db);
    }

}
