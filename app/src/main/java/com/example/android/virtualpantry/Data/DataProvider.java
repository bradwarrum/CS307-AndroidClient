package com.example.android.virtualpantry.Data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.example.android.virtualpantry.Database.PantryDbHelper;
import com.example.android.virtualpantry.Database.VirtualPantryContract;

/**
 * Created by Garrett on 4/4/2015.
 */
public class DataProvider extends ContentProvider{

    private PantryDbHelper database;

    //URI CODES
    static final int LISTS = 100;
    static final int HOUSEHOLDS = 101;
    static final int LIST_ITEMS = 102;
    static final int INVENTORY_ITEMS = 103;

    //Should be done
    @Override
    public boolean onCreate() {
        database = new PantryDbHelper(getContext());
        return false;
    }

    //TODO:
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    //TODO:
    @Override
    public String getType(Uri uri){
        return null;
    }

    //TODO:
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    //TODO:
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    //TODO:
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = VirtualPantryContract.CONTENT_AUTHORITY;

        //create each URI path
        matcher.addURI(authority, VirtualPantryContract.PATH_HOUSEHOULDS, HOUSEHOLDS);
        matcher.addURI(authority, VirtualPantryContract.PATH_LISTS, LISTS);
        matcher.addURI(authority, VirtualPantryContract.PATH_INVENTORY_ITEMS, INVENTORY_ITEMS);
        matcher.addURI(authority, VirtualPantryContract.PATH_LIST_ITMES, LIST_ITEMS);
        return matcher;
    }
}
