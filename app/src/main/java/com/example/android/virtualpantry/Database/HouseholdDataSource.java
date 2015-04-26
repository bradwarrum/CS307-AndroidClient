package com.example.android.virtualpantry.Database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.Preference;
import android.util.Log;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Data.JSONModels.*;
import com.example.android.virtualpantry.HouseholdActivity;
import com.example.android.virtualpantry.Network.*;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brad on 4/20/2015.
 */

/**
 * Exposes data retrieval and modification functions related to Household information and management.
 * @see #createHousehold(String, String, PersistenceCallback)
 * @see #getUserInformation(boolean, PersistenceCallback)
 */
public class HouseholdDataSource {
    private Context context;

    private VPDatabaseHandler dbHandler = null;


    /**
     * Instantiates a wrapper around the Database Handler singleton.  <br/>
     * There is very little overhead to instantiating this class.  There is no need to maintain a static reference to this class.
     * @param context
     */
    public HouseholdDataSource(Context context) {
        this.context = context;
        dbHandler = VPDatabaseHandler.getInstance(context);
    }
    /**
     * Creates a household and updates the persistence layer. <br/>
     * This method will NOT automatically pull other households from the server. <br/>
     * Since the database is updated locally during this request, there is no need to pull households from the server after this request is complete.
     * @param name Name of the household
     * @param description Description of the household
     * @param callback Called after the operation is completed.  The returnType will be UserInfoResponse.Household, and returnValue holds info about the created household.
     * @see com.example.android.virtualpantry.Data.JSONModels.UserInfoResponse.Household
     */
    public void createHousehold(final String name, final String description, PersistenceCallback callback){
        PersistenceTask task = new PersistenceTask(callback) {
            HouseholdCreateResponse hcr = null;
            @Override
            protected void doInBackground() {

                this.requestType = PersistenceRequestCode.CREATE_HOUSEHOLD;

                // WEB REQUEST PORTION
                Request req = new Request(NetworkUtility.createCreateHouseholdString(
                        context.getSharedPreferences(PreferencesHelper.USER_INFO, Context.MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null)
                ), Request.POST, new HouseholdCreateRequest(name, description));
                if (req.openConnection()) {
                    req.execute();
                    hcr = parseWebResponse(req, HouseholdCreateResponse.class);
                    if (hcr == null) return;
                    else {
                        this.returnType = JSONModels.UserInfoResponse.Household.class;
                        this.returnValue = new UserInfoResponse.Household(hcr.householdID, name, description);
                    }
                } else {
                    status = PersistenceResponseCode.ERR_CLIENT_CONNECT;
                    return;
                }

                // DATABASE QUERY PORTION (GUARANTEED VALID DATA)
                SQLiteDatabase database = dbHandler.getWritableDatabase();
                ContentValues insertParams = new ContentValues();
                insertParams.put("ID", hcr.householdID);
                insertParams.put("Name", name);
                insertParams.put("Description", description);
                insertParams.put("Version", hcr.version);
                database.replace("Households",null, insertParams);
                database.close();

                Log.d("HOUSEHOLD DATA SOURCE", "Finished");
                return;
            }

        };
        task.execute((Void) null);
    }

    /**
     * Retrieves a list of a user's households and personal information.
     * @param forceRefresh Setting this to true will force the method to retrieve information from the server, updating the backing database in the process.  Setting this to false will pull information from the local database.<br/><br/>
     *                     Use the forceRefresh only on application startup, or when the user explicitly asks for the information to be refreshed.<br/><br/>
     *                     If forceRefresh is true, and the client fails to connect to the server, the status is set to ERR_CLIENT_CONNECT, but any local information is returned in the callback.  Note that this information is not guaranteed to be up to date.
     * @param callback Called after the operation is completed. The returnValue type will be UserInfoResponse.
     * @see com.example.android.virtualpantry.Data.JSONModels.UserInfoResponse
     */
    public void getUserInformation(final boolean forceRefresh, PersistenceCallback callback) {
        PersistenceTask task = new PersistenceTask(callback) {
            UserInfoResponse uir = null;
            @Override
            protected void doInBackground() {
                this.requestType = PersistenceRequestCode.FETCH_USER_INFORMATION;

                if (forceRefresh) {
                    Request req = new Request(NetworkUtility.createGetUserInfoString(
                            context.getSharedPreferences(PreferencesHelper.USER_INFO, Context.MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null)
                    ), Request.GET);
                    if (req.openConnection()) {
                        req.execute();
                        uir = parseWebResponse(req, UserInfoResponse.class);
                        if (uir == null) return;
                        else {
                            this.returnType = UserInfoResponse.class;
                            this.returnValue = uir;
                        }
                    } else {
                        status = PersistenceResponseCode.ERR_CLIENT_CONNECT;
                        selectLocal();
                        return;
                    }
                    SQLiteDatabase database = dbHandler.getWritableDatabase();

                    database.delete("UserInfo", null, null);
                    ContentValues params = new ContentValues();
                    params.put("Email", uir.emailAddress);
                    params.put("FirstName", uir.firstName);
                    params.put("LastName", uir.lastName);
                    database.insert("UserInfo", null, params);


                    for (UserInfoResponse.Household h : uir.households) {
                        params.clear();
                        params.put("Description", h.householdDescription);
                        params.put("Name", h.householdName);
                        if (1 != database.update("Households", params, "ID=?", new String[] {String.valueOf(h.householdID)})) {
                            params.put("ID", h.householdID);
                            if (-1 == database.insert("Households", null, params)) {
                                status = PersistenceResponseCode.ERR_DB_INTERNAL;
                                return;
                            }
                        }
                    }

                    database.close();
                } else {
                    selectLocal();

                }
            }

            public void selectLocal() {
                SQLiteDatabase database = dbHandler.getReadableDatabase();
                Cursor c = database.rawQuery("SELECT (ID, Email, FirstName, LastName) FROM UserInfo;", null);
                if (!c.moveToFirst()) {
                    this.status = PersistenceResponseCode.ERR_DB_DATA_NOT_FOUND;
                    c.close();
                    database.close();
                    return;
                }
                int userId = c.getInt(0);
                String emailAddress = c.getString(1);
                String firstName = c.getString(2);
                String lastName = c.getString(3);
                c.close();

                List<UserInfoResponse.Household> householdList = new ArrayList<UserInfoResponse.Household>();
                c = database.rawQuery("SELECT (ID, Name, Description) FROM Households;", null);
                while (c.moveToNext()) {
                    householdList.add(new UserInfoResponse.Household(c.getLong(0), c.getString(1), c.getString(2)));
                }
                c.close();
                database.close();

                uir  = new UserInfoResponse(userId, firstName, lastName, emailAddress, householdList);
                this.returnType = UserInfoResponse.class;
                this.returnValue = uir;
            }
        };
        task.execute((Void)null);
    }

    /**
     * Retrieves information about a household from the local database or from the server.  If information is fetched from the server, any shopping list deletions are cascaded locally.<br/>
     * If the operation fails with an ERR_CLIENT_CONNECT when forceRefresh is true, any local information will still be returned through returnType.
     * @param householdID The household that information should be collected for.
     * @param forceRefresh Setting this to true will force the method to retrieve information from the server, updating the backing database in the process.  Setting this to false will pull information from the local database.<br/><br/>
     * @param callback Called after the operation is completed. The returnValue type will be Household.
     * @see com.example.android.virtualpantry.Data.JSONModels.Household
     */
    public void getHouseholdInfo(final int householdID, final boolean forceRefresh, PersistenceCallback callback) {
        PersistenceTask task = new PersistenceTask(callback) {
            @Override
            protected void doInBackground() {
                requestType = PersistenceRequestCode.FETCH_HOUSEHOLD;
                if (forceRefresh) {
                    Request req = new Request(NetworkUtility.createGetHouseholdString(householdID,
                            context.getSharedPreferences(PreferencesHelper.USER_INFO, Context.MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null)),
                            Request.GET);
                    if (req.openConnection()) {
                        req.execute();
                        Household h = parseWebResponse(req, Household.class);
                        if (h == null) return;
                        SQLiteDatabase database = dbHandler.getWritableDatabase();
                        database.beginTransaction();
                        try {
                            ContentValues params = new ContentValues();
                            params.put("Name", h.householdName);
                            params.put("Description", h.householdDescription);

                            if (1 != database.update("Households", params, "ID=?", new String[]{String.valueOf(h.householdId)})) {
                                params.put("ID", h.householdId);
                                if (-1 == database.insert("Households", null, params)) {
                                    status = PersistenceResponseCode.ERR_DB_INTERNAL;
                                    return;
                                }
                            }
                            params.clear();
                            params.put("Orphaned", 1);
                            database.update("ShoppingLists", params, "HouseholdID=?", new String[]{String.valueOf(householdID)});

                            for (Household.HouseholdList list : h.lists) {
                                params.clear();
                                params.put("Orphaned", 0);
                                params.put("Name", list.listName);
                                if (1 != database.update("ShoppingLists", params, "ListID=?", new String[]{String.valueOf(list.listID)})) {
                                    params.put("ListID", list.listID);
                                    if (-1 == database.insert("ShoppingLists", null, params)) {
                                        status = PersistenceResponseCode.ERR_DB_INTERNAL;
                                        return;
                                    }
                                }
                            }
                            //Warning: cascading deletes here
                            database.delete("ShoppingLists", "HouseholdID=? AND Orphaned=1", new String[]{String.valueOf(householdID)});


                            database.setTransactionSuccessful();
                        } finally {
                            database.endTransaction();
                            database.close();
                        }
                    }
                }
                fetchLocalInfo(householdID);
            }

            private void fetchLocalInfo(int householdID) {
                SQLiteDatabase database = dbHandler.getReadableDatabase();
                try {
                    Cursor c = database.rawQuery("SELECT (Name, Description) FROM Households WHERE ID=?;", new String[] {String.valueOf(householdID)});
                    if (!c.moveToFirst()) {
                        status = PersistenceResponseCode.ERR_DB_DATA_NOT_FOUND;
                        return;
                    }
                    String name = c.getString(0);
                    String description = c.getString(1);
                    c.close();
                    List<Household.HouseholdList> lists = new ArrayList<Household.HouseholdList>();
                    c = database.rawQuery("SELECT (ListID, Name) FROM ShoppingLists WHERE HouseholdID=?;", new String[] {String.valueOf(householdID)});
                    if (c.moveToFirst()) {
                        do {
                            lists.add(new Household.HouseholdList(c.getInt(0), c.getString(1)));
                        } while (c.moveToNext());
                    }
                    c.close();
                    returnType = Household.class;
                    returnValue = new Household(householdID, name, description, null, null, lists);
                } finally {
                    database.close();
                }
            }
        };
        task.execute((Void)null);
    }



}
