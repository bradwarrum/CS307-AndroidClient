package com.example.android.virtualpantry.Database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

/**
 * Created by Brad on 4/19/2015.
 */
class VPDatabaseHandler extends SQLiteOpenHelper {


    private static final int VP_SCHEMA_VERSION = 4;
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
        //Household table
        db.execSQL("CREATE TABLE Households ("
                +"ID INTEGER PRIMARY KEY,"
                +"Name TEXT NOT NULL,"
                +"Description TEXT NOT NULL,"
                +"Version BIGINT DEFAULT 0);");
        //UserInfo Table
        db.execSQL("CREATE TABLE UserInfo ("
                + "ID INTEGER PRIMARY KEY,"
                + "Email TEXT NOT NULL,"
                + "FirstName TEXT NOT NULL,"
                + "LastName TEXT NOT NULL);");
        //ShoppingList table
        db.execSQL("CREATE TABLE ShoppingLists ("
                + "ListID INTEGER PRIMARY KEY,"
                + "HouseholdID INTEGER NOT NULL REFERENCES Households (ID) ,"
                + "Name TEXT NOT NULL,"
                + "Version LONG DEFAULT 0,"
                + "Orphaned INTEGER DEFAULT 0);");
        //InventoryItems table
        db.execSQL("CREATE TABLE InventoryItems ("
                + "HouseholdID INTEGER NOT NULL REFERENCES Households(ID),"
                + "UPC TEXT NOT NULL,"
                + "Description TEXT NOT NULL,"
                + "PackageQuantity FLOAT NOT NULL," //The number of package units in a package
                + "PackageUnits INTEGER NOT NULL," // Units of packaging for the contents of the package
                + "PackageName TEXT NOT NULL," // Units of packaging for the package itself
                + "Quantity INTEGER NOT NULL,"
                + "Fractional INTEGER NOT NULL,"
                + "PRIMARY KEY (UPC, HouseholdID));"
                );
        //ShoppingListItems table
        db.execSQL("CREATE TABLE ShoppingListItems ("
                + "ListItemID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "ListID INTEGER NOT NULL REFERENCES ShoppingLists (ListID) ON DELETE CASCADE,"
                + "UPC TEXT NOT NULL,"
                + "HouseholdID INTEGER NOT NULL,"
                + "DefinedQuantity INTEGER NOT NULL,"
                + "DefinedFractional INTEGER NOT NULL,"
                + "CartQuantity INTEGER DEFAULT 0,"
                + "CartFractional INTEGER DEFAULT 0,"
                + "Orphaned INTEGER DEFAULT 0,"
                + "FOREIGN KEY (UPC, HouseholdID) REFERENCES InventoryItems(UPC, HouseholdID),"
                + "UNIQUE (UPC, ListID));"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS ShoppingListItems");
        db.execSQL("DROP TABLE IF EXISTS InventoryItems");
        db.execSQL("DROP TABLE IF EXISTS ShoppingLists");
        db.execSQL("DROP TABLE IF EXISTS Households;");
        db.execSQL("DROP TABLE IF EXISTS UserInfo;");
        onCreate(db);
    }

}
