package com.example.android.virtualpantry.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.app.INotificationSideChannel;

import com.example.android.virtualpantry.Database.VirtualPantryContract.HouseholdEntry;
import com.example.android.virtualpantry.Database.VirtualPantryContract.InventoryEntry;
import com.example.android.virtualpantry.Database.VirtualPantryContract.ShoppingListEntry;
import com.example.android.virtualpantry.Database.VirtualPantryContract.ShoppingListItemEntry;
/**
 * Created by Garrett on 4/2/2015.
 */
public class PantryDbHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "pantry.db";

    public PantryDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_SHOPPING_LIST_TABLE =
                "CREATE TABLE " + ShoppingListEntry.TABLE_NAME + "(" +
                ShoppingListEntry._ID + " INTEGER PRIMARY KEY," +
                ShoppingListEntry.COLUMN_LIST_ID + " TEXT UNIQUE NOT NULL," +
                ShoppingListEntry.COLUMN_LIST_NAME + " TEXT NOT NULL," +
                ShoppingListEntry.COLUMN_LIST_VERSION + " TEXT NOT NULL" +
                " );";
        final String SQL_CREATE_HOUSEHOLD_TABLE =
                "CREATE TABLE " + HouseholdEntry.TABLE_NAME + " (" +
                 HouseholdEntry._ID + " INTEGER PRIMARY KEY," +
                 HouseholdEntry.COLUMN_HOUSEHOLD_ID + " TEXT UNIQUE NOT NULL," +
                 HouseholdEntry.COLUMN_HOUSEHOLD_NAME + " TEXT NOT NULL," +
                 HouseholdEntry.COLUMN_HOUSEHOLD_DESCRIPTION + " TEXT NOT NULL" +
                " );";
        final String SQL_CREATE_SHOPPING_LIST_ITEM_TABLE =
                "CREATE TABLE " + ShoppingListItemEntry.TABLE_NAME + " (" +
                ShoppingListItemEntry._ID + " INTEGER PRIMARY KEY," +
                ShoppingListEntry.COLUMN_LIST_ID + " TEXT NOT NULL," +
                ShoppingListItemEntry.COLUMN_LIST_ID + " TEXT NOT NULL," +
                ShoppingListItemEntry.COLUMN_UPC + " TEXT NOT NULL, " +
                ShoppingListItemEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL," +
                ShoppingListItemEntry.COLUMN_QUANTITY + " TEXT NOT NULL," +
                ShoppingListItemEntry.COLUMN_PACKAGE_NAME + " TEXT NOT NULL," +
                ShoppingListItemEntry.COLUMN_IN_CART + "TEXT NOT NULL" +
                " );";
        final String SQL_CREATE_INVENTORY_ITEM_TABLE =
                "CREATE TABLE " + InventoryEntry.TABLE_NAME + " (" +
                InventoryEntry._ID + " INTEGER PRIMARY KEY," +
                InventoryEntry.COLUMN_UPC + " TEXT NOT NULL," +
                InventoryEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL," +
                InventoryEntry.COLUMN_PACKAGE_SIZE + " TEXT NOT NULL," +
                InventoryEntry.COLUMN_PACKAGE_UNITS + " TEXT NOT NULL," +
                InventoryEntry.COLUMN_PACKAGE_NAME + " TEXT NOT NULL," +
                InventoryEntry.COLUMN_QUANTITY + " TEXT NOT NULL," +
                InventoryEntry.COLUMN_FRACTIONAL + " TEXT NOT NULL" +
                " );";
        db.execSQL(SQL_CREATE_SHOPPING_LIST_TABLE);
        db.execSQL(SQL_CREATE_HOUSEHOLD_TABLE);
        db.execSQL(SQL_CREATE_SHOPPING_LIST_ITEM_TABLE);
        db.execSQL(SQL_CREATE_INVENTORY_ITEM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS  " + ShoppingListEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HouseholdEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ShoppingListItemEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + InventoryEntry.TABLE_NAME);
        onCreate(db);
    }
}
