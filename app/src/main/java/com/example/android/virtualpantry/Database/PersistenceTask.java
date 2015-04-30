package com.example.android.virtualpantry.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Network.NetworkUtility;
import com.example.android.virtualpantry.Network.Request;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

/**
 * Created by Brad on 4/20/2015.
 */
public abstract class PersistenceTask extends AsyncTask<Void, Void, Void> {

    private PersistenceCallback callback;
    protected PersistenceResponseCode status;
    protected Object returnValue;
    protected Type returnType;
    protected PersistenceRequestCode requestType;

    public PersistenceTask(PersistenceCallback callback) {
        this.callback = callback;
        status = PersistenceResponseCode.SUCCESS;
        returnValue = null;
        returnType = null;
        requestType = null;
    }
    @Override
    protected final Void doInBackground(Void... params) {
        doInBackground();
        return null;
    }

    protected abstract void doInBackground();

    @Override
    protected final void onPostExecute(Void aVoid) {
        if (callback != null)
            callback.callback(requestType, status, returnValue, returnType);
    }

    /**
     *
     * @param req
     * @param parseType
     * @param <T>
     * @return
     */
    protected final <T> T parseWebResponse(Request req, Class<T> parseType) {
        T parseObj = null;
        int code = req.getResponseCode();
        String content = req.getResponse();
        //If success code
        if (code >= 200 && code < 400) {
            if (content != null) {
                try {
                    parseObj = JSONModels.gson.fromJson(content, parseType);
                    return parseObj;
                } catch (JsonSyntaxException e) {
                    this.status = PersistenceResponseCode.ERR_SERVER_MALFORMED_RESPONSE;
                }
            } else {
                this.status = PersistenceResponseCode.ERR_SERVER_MALFORMED_RESPONSE;
            }
        } else {
            if (content != null) {
                try {
                    JSONModels.ErrorResponse errResp = JSONModels.gson.fromJson(content, JSONModels.ErrorResponse.class);
                    this.returnValue = errResp;
                    this.status = PersistenceResponseCode.fromBackingCode(errResp.errorCode);
                    this.returnType = JSONModels.ErrorResponse.class;

                } catch (JsonSyntaxException e) {
                    this.returnValue = null;
                    this.status = PersistenceResponseCode.ERR_SERVER_MALFORMED_RESPONSE;
                }
            } else this.status = PersistenceResponseCode.ERR_SERVER_MALFORMED_RESPONSE;
        }
        return null;
    }

    protected final boolean updateLocalInventory(SQLiteDatabase database, int householdID, JSONModels.GetInventoryResponse data) {
        ContentValues params = new ContentValues();
        for (JSONModels.GetInventoryResponse.InventoryItem item : data.items) {
            params.clear();

            params.put("PackageQuantity", item.packaging.packageSize);
            params.put("PackageUnits", item.packaging.unitID);
            params.put("PackageName", item.packaging.packageName);
            params.put("Quantity", item.quantity);
            params.put("Fractional", item.fractional);
            params.put("Description", item.description);
            if (1 != database.update("InventoryItems", params, "UPC=? AND HouseholdID=?", new String[] {item.UPC, String.valueOf(householdID)})) {
                params.put("UPC", item.UPC);
                params.put("HouseholdID", householdID);
                if (-1 == database.insert("InventoryItems", null, params)) {
                    return false;
                }
            }

        }
        return true;
    }

    protected final long updateLocalList(SQLiteDatabase database, int householdID, int listID, Context context, long version) {
        Cursor c = database.query("Households", new String[] {"Version"}, "ID=?", new String[] {String.valueOf(householdID)}, null, null, null);
        if (!c.moveToFirst()) {
            c.close();
            status = PersistenceResponseCode.ERR_DB_DATA_NOT_FOUND;
            return -1;
        }
        long invVersion = c.getLong(0);
        c.close();

        Request req = new Request(NetworkUtility.createGetInventoryString(householdID, context.getSharedPreferences(PreferencesHelper.USER_INFO, Context.MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null))
                , Request.GET);

        if (req.openConnection()) {
            req.setHeader("If-None-Match", "\"" + String.valueOf(invVersion) + "\"");
            req.execute();
            if (req.getResponseCode() != 304) {
                JSONModels.GetInventoryResponse invresp = parseWebResponse(req, JSONModels.GetInventoryResponse.class);
                if (invresp == null) {
                    return -1;
                }
                if (!updateLocalInventory(database, householdID, invresp)) return -1;
                ContentValues v = new ContentValues();
                v.put("Version", invresp.version);
                if (1 != database.update("Households", v, "ID=?", new String[] {String.valueOf(householdID)})) {
                    status = PersistenceResponseCode.ERR_DB_INTERNAL;
                    return -1;
                }
            }

        }



        ContentValues setOrphan = new ContentValues();
        setOrphan.put("Orphaned", 1);
        database.update("ShoppingListItems", setOrphan, "ListID = ?", new String[] {String.valueOf(listID)});

        req = new Request(NetworkUtility.createGetListString(householdID, listID,
                context.getSharedPreferences(PreferencesHelper.USER_INFO, Context.MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null)
        ), Request.GET);
        if (req.openConnection()) {
            req.setHeader("If-None-Match", "\"" + String.valueOf(version) + "\"");
            req.execute();
            if (req.getResponseCode() == 304) {
                setOrphan.clear();
                setOrphan.put("Orphaned", 0);
                database.update("ShoppingListItems", setOrphan, "ListID=?", new String[] {String.valueOf(listID)});
                return version;
            }
            JSONModels.GetShoppingListResponse listresp = parseWebResponse(req, JSONModels.GetShoppingListResponse.class);
            if (listresp == null) return -1;
            ContentValues params = new ContentValues();
            for (JSONModels.GetShoppingListResponse.Item i : listresp.items) {
                params.clear();
                params.put("Orphaned", 0);
                params.put("DefinedFractional", i.fractional);
                params.put("DefinedQuantity", i.quantity);
                if (1 != database.update("ShoppingListItems", params, "UPC=? AND HouseholdID=? AND ListID=?", new String[] {i.UPC, String.valueOf(householdID), String.valueOf(listID)})) {
                    params.put("HouseholdID", householdID);
                    params.put("UPC", i.UPC);
                    params.put("ListID", listID);
                    if (-1 == database.insert("ShoppingListItems", null, params)) {
                        status = PersistenceResponseCode.ERR_DB_INTERNAL;
                        return -1;
                    }
                }
            }
            database.delete("ShoppingListItems", "Orphaned=1", null);
            params.clear();
            params.put("Version", listresp.version);
            version = listresp.version;
            database.update("ShoppingLists", params, "ListID=?", new String[] {String.valueOf(listID)});
            /*database.setTransactionSuccessful();
            database.endTransaction();
            database.beginTransaction();*/
        } else {
            status = PersistenceResponseCode.ERR_CLIENT_CONNECT;
            return -1;
        }
        return version;
    }

}
