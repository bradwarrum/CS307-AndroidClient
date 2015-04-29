package com.example.android.virtualpantry.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Network;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Network.NetworkUtility;
import com.example.android.virtualpantry.Network.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brad on 4/28/2015.
 */
public class InventoryDataSource {

    private Context context;
    private VPDatabaseHandler dbHandler = null;


    /**
     * Instantiates a wrapper around the Database Handler singleton.  <br/>
     * There is very little overhead to instantiating this class.  There is no need to maintain a static reference to this class.
     * @param context
     */
    public InventoryDataSource(Context context) {
        this.context = context;
        dbHandler = VPDatabaseHandler.getInstance(context);
    }

    /**
     * Attempts to generate an internal UPC for an item.<br/><br/>
     * If there is a versioning problem, the new inventory is fetched and the request is resubmitted.<br/>
     * If there is a failure at any point, the local database is rolled back to its previous state. The only successfull response code for this call is SUCCESS.
     * @param callback Executed after this method is complete. ReturnValue is the UPC that was generated (type String)
     */
    public void generateUPC(final int householdID, final String description, final String packageName,
                            final int packageUnits, final float packageSize, PersistenceCallback callback ) {
        linkUPC(householdID, null, description, packageName, packageUnits, packageSize, callback);
    }
    /**
     * Attempts to link a UPC to a household, updating the local household inventory in the process. <br/><br/>
     * If there is a versioning problem, the new inventory is fetched and the request is resubmitted.<br/>
     * If there is a failure at any point, the local database is rolled back to its previous state. The only successfull response code for this call is SUCCESS.
     * @param callback Executed after this method is complete. ReturnValue is the UPC that was linked (type String)
     */
    public void linkUPC(final int householdID, final String UPC, final String description, final String packageName,
                        final int packageUnits, final float packageSize, PersistenceCallback callback) {
        PersistenceTask task = new PersistenceTask(callback) {
            @Override
            protected void doInBackground() {
                SQLiteDatabase database = dbHandler.getWritableDatabase();
                database.beginTransaction();
                try {
                    JSONModels.LinkRequest model = new JSONModels.LinkRequest(description, packageName, packageUnits, packageSize, 1);
                    //Get version number
                    Cursor c = database.query("Households", new String[]{"Version"}, "ID=?", new String[]{String.valueOf(householdID)}, null, null, null);
                    if (!c.moveToFirst()) {
                        c.close();
                        pullInventoryStage(database, householdID);
                        return;
                    }
                    long version = c.getLong(0);
                    c.close();

                    //Try to link the UPC

                    Request request;
                    if (UPC == null) {
                        request = new Request(NetworkUtility.createLinkNoUPCString(householdID,
                                context.getSharedPreferences(PreferencesHelper.USER_INFO, Context.MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null)),
                                Request.POST, model);
                    } else {
                        request = new Request(NetworkUtility.createLinkUPCString(householdID, UPC,
                                context.getSharedPreferences(PreferencesHelper.USER_INFO, Context.MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null)),
                                Request.POST, model);
                    }
                    if (request.openConnection()) {
                        request.execute();
                        if (request.getResponseCode() == 200) {
                            JSONModels.CreateUPCResponse resp = parseWebResponse(request, JSONModels.CreateUPCResponse.class);
                            if (resp == null) return;
                            linkStage(database, model, resp.version, resp.UPC);
                        } else {
                            parseWebResponse(request, JSONModels.ErrorResponse.class);
                            if (status == PersistenceResponseCode.ERR_OUTDATED_TIMESTAMP) {
                                pullInventoryStage(database, householdID);
                            } else {
                                return;
                            }
                        }
                    } else {
                        status = PersistenceResponseCode.ERR_CLIENT_CONNECT;
                        return;
                    }
                } finally {
                    database.endTransaction();
                    database.close();
                }
            }

            private void pullInventoryStage(SQLiteDatabase database, int householdID) {
                //Request inventory fetch for new information
                Request req = new Request(NetworkUtility.createGetInventoryString(householdID,
                        context.getSharedPreferences(PreferencesHelper.USER_INFO, Context.MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null)),
                        Request.GET);
                if (req.openConnection()) {
                    req.execute();
                    JSONModels.GetInventoryResponse resp = parseWebResponse(req, JSONModels.GetInventoryResponse.class);
                    if (resp == null) return;
                    if (updateLocalInventory(database, householdID, resp)) {
                        JSONModels.LinkRequest newreq = new JSONModels.LinkRequest(description,packageName, packageUnits, packageSize, resp.version);
                        requeryStage(database, householdID, newreq);
                    } else {
                        status = PersistenceResponseCode.ERR_DB_INTERNAL;
                        return;
                    }
                } else {
                    status = PersistenceResponseCode.ERR_CLIENT_CONNECT;
                    return;
                }
            }

            private void requeryStage(SQLiteDatabase database, int householdID, JSONModels.LinkRequest model) {
                Request request;
                if (UPC == null) {
                    request = new Request(NetworkUtility.createLinkNoUPCString(householdID,
                            context.getSharedPreferences(PreferencesHelper.USER_INFO, Context.MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null)),
                            Request.POST, model);
                } else {
                    request = new Request(NetworkUtility.createLinkUPCString(householdID, UPC,
                            context.getSharedPreferences(PreferencesHelper.USER_INFO, Context.MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null)),
                            Request.POST, model);
                }
                if (request.openConnection()) {
                    request.execute();
                    JSONModels.CreateUPCResponse resp = parseWebResponse(request, JSONModels.CreateUPCResponse.class);
                    if (resp == null) return;
                    linkStage(database, model, resp.version, resp.UPC);

                } else {
                    status = PersistenceResponseCode.ERR_CLIENT_CONNECT;
                    return;
                }
            }

            private void linkStage(SQLiteDatabase database, JSONModels.LinkRequest model, long newVersion, String finalUPC) {
                ContentValues params = new ContentValues();
                params.put("HouseholdID", householdID);
                params.put("UPC", finalUPC);
                params.put("Description", model.description);
                params.put("PackageQuantity", model.packageSize);
                params.put("PackageUnits", model.packageUnits);
                params.put("PackageName", model.packageName);
                params.put("Quantity", 0);
                params.put("Fractional", 0);
                if (-1 == database.insert("InventoryItems", null, params)) {
                    status = PersistenceResponseCode.ERR_DB_INTERNAL;
                    return;
                } else {
                    params.clear();
                    params.put("Version", newVersion);
                    if (1 != database.update("Households", params, "ID=?", new String[] {String.valueOf(householdID)})) {
                        status = PersistenceResponseCode.ERR_DB_INTERNAL;
                        return;
                    }
                    returnValue = finalUPC;
                    returnType = String.class;
                    database.setTransactionSuccessful();
                }
            }
        };
        task.execute((Void)null);
    }

    /**
     * Retrieves a household's inventory from either the local database or from the server.
     * @param householdID The household to fetch the inventory from
     * @param forceRefresh If this is true, inventory items will be fetched from the server. The database layer will automatically be updated.<br/>
     *                     If this is false, inventory items will be fetched from the database layer, if any such data exists.
     * @param callback Executed after this method is complete. ReturnValue is a List<InventoryItem>.
     * @see com.example.android.virtualpantry.Data.JSONModels.GetInventoryResponse.InventoryItem
     */
    public void getInventory(final int householdID, final boolean forceRefresh, PersistenceCallback callback) {
        PersistenceTask task = new PersistenceTask(callback) {
            @Override
            protected void doInBackground() {
                SQLiteDatabase database = dbHandler.getWritableDatabase();
                database.beginTransaction();
                try {
                    Cursor c = database.rawQuery("SELECT Version FROM Households WHERE ID=?;", new String[]{String.valueOf(householdID)});
                    if (!c.moveToFirst()) {
                        c.close();
                        status = PersistenceResponseCode.ERR_DB_INTERNAL;
                        return;
                    }
                    long version = c.getLong(0);
                    c.close();

                    if (forceRefresh) {
                        Request req = new Request(NetworkUtility.createGetInventoryString(householdID,
                                context.getSharedPreferences(PreferencesHelper.USER_INFO, Context.MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null)),
                                Request.GET);
                        req.setHeader("If-None-Match", "\"" + String.valueOf(version) + "\"");
                        if (req.openConnection()) {
                            req.execute();
                            if (req.getResponseCode() != 304) {
                                JSONModels.GetInventoryResponse resp = parseWebResponse(req, JSONModels.GetInventoryResponse.class);
                                if (resp == null) return;
                                if (!updateLocalInventory(database, householdID, resp)) {
                                    status = PersistenceResponseCode.ERR_DB_INTERNAL;
                                    return;
                                }
                                returnValue = resp.items;
                                returnType = resp.items.getClass();
                                return;
                            }
                        } else {
                            status = PersistenceResponseCode.ERR_CLIENT_CONNECT;
                            return;
                        }
                    }
                    List<JSONModels.GetInventoryResponse.InventoryItem> items = new ArrayList<JSONModels.GetInventoryResponse.InventoryItem>();
                    c = database.rawQuery("SELECT UPC, Description, PackageQuantity, PackageUnits, PackageName, Quantity, Fractional FROM "
                            + "InventoryItems WHERE HouseholdID=?;", new String[] {String.valueOf(householdID)});
                    while (c.moveToNext()) {
                        UnitTypes units = UnitTypes.fromID(c.getInt(3));
                        String UPC = c.getString(0);
                        JSONModels.GetInventoryResponse.InventoryItem.InventoryItemPackaging packaging = new JSONModels.GetInventoryResponse.InventoryItem.InventoryItemPackaging(
                                c.getFloat(2), units.getUnitID(), units.getUnitName(), units.getUnitAbbrev(), c.getString(4));
                        JSONModels.GetInventoryResponse.InventoryItem item = new JSONModels.GetInventoryResponse.InventoryItem(
                                UPC, UPC.length() == 5, c.getString(1), c.getInt(5), c.getInt(6), packaging);
                        items.add(item);
                    }
                    c.close();
                    returnValue = items;
                    returnType = items.getClass();
                    database.setTransactionSuccessful();
                    return;

                }finally {
                    database.endTransaction();
                    database.close();
                }
            }
        };
        task.execute((Void)null);
    }

    /**
     * Attempts to update an inventory item with a new quantity.  Simultaneously updates the local database with the new quantities as well.
     * @param callback Executed after this method is complete. ReturnValue is null. You must call getInventory with forceRefresh=false to retrieve the updated information.
     */
    public void updateInventoryQuantity(final int householdID, final List<JSONModels.UpdateInventoryRequest.UpdateInventoryItem> items, PersistenceCallback callback) {
        PersistenceTask task = new PersistenceTask(callback) {
            @Override
            protected void doInBackground() {
                SQLiteDatabase database = dbHandler.getWritableDatabase();
                database.beginTransaction();
                try {
                    Cursor c = database.rawQuery("SELECT Version FROM Households WHERE ID=?;", new String[] {String.valueOf(householdID)});
                    if (!c.moveToFirst()) {
                        c.close();
                        status = PersistenceResponseCode.ERR_DB_INTERNAL;
                        return;
                    }
                    long version = c.getLong(0);
                    c.close();

                    Request req = new Request(NetworkUtility.createUpdateInventoryString(householdID,
                            context.getSharedPreferences(PreferencesHelper.USER_INFO, Context.MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null)),
                            Request.POST, new JSONModels.UpdateInventoryRequest(version, items));
                    if (req.openConnection()) {
                        req.execute();
                        JSONModels.UpdateInventoryResponse resp = parseWebResponse(req, JSONModels.UpdateInventoryResponse.class);
                        if (resp == null) return;
                        ContentValues params = new ContentValues();
                        for (JSONModels.UpdateInventoryRequest.UpdateInventoryItem item : items) {
                            params.clear();
                            params.put("Quantity", item.quantity);
                            params.put("Fractional", item.fractional);
                            if (1 != database.update("InventoryItems", params, "HouseholdID=? AND UPC=?", new String[] {String.valueOf(householdID), item.UPC})) {
                                status = PersistenceResponseCode.ERR_DB_INTERNAL;
                                return;
                            }
                        }
                        params.clear();
                        params.put("Version", resp.version);
                        if (1 != database.update("Households", params, "ID=?", new String[] {String.valueOf(householdID)} )) {
                            status = PersistenceResponseCode.ERR_DB_INTERNAL;
                            return;
                        }
                        database.setTransactionSuccessful();
                    } else {
                        status = PersistenceResponseCode.ERR_CLIENT_CONNECT;
                        return;
                    }
                } finally {
                    database.endTransaction();
                    database.close();
                }
            }
        };
        task.execute((Void)null);
    }
}
