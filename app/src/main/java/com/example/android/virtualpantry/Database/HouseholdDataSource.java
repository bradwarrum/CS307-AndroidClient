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
     * This method will NOT automatically pull all households from the server.  It is assumed that the database has already been updated.<br/>
     * Since the database is updated, there is no need to pull households from the server after this request is complete.
     * @param name Name of the household
     * @param description Description of the household
     * @param callback Called after the operation is completed.  The returnValue type will be HouseholdCreateResponse
     * @see com.example.android.virtualpantry.Data.JSONModels.HouseholdCreateResponse
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
                        this.returnType = HouseholdCreateResponse.class;
                        this.returnValue = hcr;
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
                insertParams.putNull("Version");
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
     * @param callback Called after the operation is completed. The returnValue type will be UserInfoResponse.
     * @see com.example.android.virtualpantry.Data.JSONModels.UserInfoResponse
     */
    public void getUserInformation(final boolean forceRefresh, PersistenceCallback callback) {
        PersistenceTask task = new PersistenceTask(callback) {
            UserInfoResponse uir = null;
            @Override
            protected void doInBackground() {
                this.requestType = PersistenceRequestCode.USER_INFORMATION;

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
                        params.put("ID", h.householdID);
                        params.put("Description", h.householdDescription);
                        params.put("Name", h.householdName);
                        database.replace("Households", null, params);
                    }

                    database.close();
                } else {
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
            }
        };
        task.execute((Void)null);
    }



}
