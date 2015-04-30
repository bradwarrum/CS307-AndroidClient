package com.example.android.virtualpantry.Database;

/**
 * Created by Brad on 4/22/2015.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Data.JSONModels.ListCreateRequest;
import com.example.android.virtualpantry.Data.JSONModels.ListCreateResponse;
import com.example.android.virtualpantry.Data.JSONModels.UpdateListRequest;
import com.example.android.virtualpantry.Data.JSONModels.UpdateListRequest.UpdateListItem;
import com.example.android.virtualpantry.Network.NetworkUtility;
import com.example.android.virtualpantry.Network.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * Exposes data retrieval and modification functions related to Shopping list CRUD operations and progress tracking.
 * @see #createShoppingList(int, String, PersistenceCallback)
 */
public class ListDataSource {
    private Context context;
    private VPDatabaseHandler dbHandler = null;


    /**
     * Instantiates a wrapper around the Database Handler singleton.  <br/>
     * There is very little overhead to instantiating this class.  There is no need to maintain a static reference to this class.
     * @param context
     */
    public ListDataSource(Context context) {
        this.context = context;
        dbHandler = VPDatabaseHandler.getInstance(context);
    }

    /**
     * Creates a shopping list under the given household ID, and adds a new shopping list to the local database.
     * @param householdID The household that the shopping list should be created for.
     * @param listName The desired name for the new list.
     * @param callback Called after execution is complete.  The returnType is JSONModels.Household.HouseholdList, and the returnValue holds info about the newly created list.
     */
    public void createShoppingList(final int householdID, final String listName, PersistenceCallback callback) {
        PersistenceTask task = new PersistenceTask(callback) {
            ListCreateResponse lcr = null;
            @Override
            protected void doInBackground() {

                this.requestType = PersistenceRequestCode.CREATE_LIST;

                // WEB REQUEST PORTION
                Request req = new Request(NetworkUtility.createCreateListString(householdID,
                        context.getSharedPreferences(PreferencesHelper.USER_INFO, Context.MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null)
                ), Request.POST, new ListCreateRequest(listName));
                if (req.openConnection()) {
                    req.execute();
                    lcr = parseWebResponse(req, ListCreateResponse.class);
                    if (lcr == null) return;
                    else {
                        this.returnType = JSONModels.Household.HouseholdList.class;
                        this.returnValue = new JSONModels.Household.HouseholdList(lcr.listID, listName);
                    }
                } else {
                    status = PersistenceResponseCode.ERR_CLIENT_CONNECT;
                    return;
                }

                // DATABASE QUERY PORTION (GUARANTEED VALID DATA)
                SQLiteDatabase database = dbHandler.getWritableDatabase();
                ContentValues insertParams = new ContentValues();
                insertParams.put("ListID", lcr.listID);
                insertParams.put("HouseholdID", householdID);
                insertParams.put("Name", listName);
                insertParams.put("Version", lcr.version);
                database.replace("ShoppingLists",null, insertParams);
                database.close();
                return;
            }

        };
        task.execute((Void) null);
    }

    /**
     * Updates the quantities of a list definition locally and remotely.  Any status other than SUCCESS guarantees that the list was not updated.<br/>
     * If the server version of the list does not match the local version, the most recent version of the list is fetched and stored.  The update is not processed.<br/>
     * If the server version of the list is different and has had items removed from it, then the shopping cart elements that still exist locally will be deleted.<br/>
     * Passing an UpdateListItem with quantity and fractional set to 0 will remove it from the list, both locally and remotely.
     * @param listID The list that will be updated
     * @param items A list of items to modify quantities for, and their modified quantities.
     * @param callback Called after execution is complete.  The returnType is null, except when returning a server-enumerated error response.  You must run getList(false) to retrieve the updated information.
     */
    public void updateList(final int listID, final List<UpdateListItem> items, PersistenceCallback callback) {
        PersistenceTask task = new PersistenceTask(callback) {
            @Override
            protected void doInBackground() {
                this.requestType = PersistenceRequestCode.UPDATE_LIST;
                //Retrieve the version number locally
                SQLiteDatabase database = dbHandler.getReadableDatabase();
                Cursor c = database.query("ShoppingLists", new String[] {"Version, HouseholdID"}, "ListID=?", new String[] {String.valueOf(listID)}, null, null, null);
                if (!c.moveToFirst()) {
                    c.close();
                    database.close();
                    this.status = PersistenceResponseCode.ERR_DB_DATA_NOT_FOUND;
                    return;
                }
                long version = c.getLong(0);
                int householdID = c.getInt(1);
                c.close();
                database.close();

                Request req = new Request(NetworkUtility.createUpdateShoppingListString(householdID, listID,
                        context.getSharedPreferences(PreferencesHelper.USER_INFO, Context.MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null)
                ), Request.POST, new UpdateListRequest(version, items));
                if (req.openConnection())
                {
                    req.execute();

                    if (req.getResponseCode() == 200) {
                        JSONModels.ListCreateResponse lcr = parseWebResponse(req, JSONModels.ListCreateResponse.class);
                        if (lcr == null) return;
                        handleUpdateSuccess(listID, householdID, version, items);
                    } else {
                        JSONModels.ErrorResponse err = parseWebResponse(req, JSONModels.ErrorResponse.class);
                        if (err == null) return;
                        status = PersistenceResponseCode.fromBackingCode(err.errorCode);
                        if (err.errorCode == Request.ERR_OUTDATED_TIMESTAMP)

                            handleOutdatedError(listID, householdID);
                        else {
                            returnType = JSONModels.ErrorResponse.class;
                            returnValue = err;
                            return;
                        }
                    }
                } else {
                    status = PersistenceResponseCode.ERR_CLIENT_CONNECT;
                    return;
                }

            }

            private void handleUpdateSuccess(final int listID, final int householdID, final long version, final List<UpdateListItem> items) {
                SQLiteDatabase database = dbHandler.getWritableDatabase();
                database.beginTransaction();
                ContentValues params = new ContentValues();
                Cursor c = null;
                try {
                    for (UpdateListItem item : items) {

                        if (item.quantity == 0 && item.fractional == 0) {
                            if (1 != database.delete("ShoppingListItems", "UPC=? AND HouseholdID=? AND ListID=?", new String[]{String.valueOf(item.UPC), String.valueOf(householdID), String.valueOf(listID)})) {
                                this.status = PersistenceResponseCode.ERR_DB_DATA_NOT_FOUND;
                                return;
                            }
                        } else {
                            params.clear();
                            params.put("DefinedQuantity", item.quantity);
                            params.put("DefinedFractional", item.fractional);
                            if (1 != database.update("ShoppingListItems", params, "UPC=? AND HouseholdID=? AND ListID=?", new String[]{String.valueOf(item.UPC), String.valueOf(householdID), String.valueOf(listID)})) {
                                params.put("ListID", listID);
                                params.put("UPC", item.UPC);
                                params.put("HouseholdID", householdID);
                                if (-1 == database.insert("ShoppingListItems", null, params)) {
                                    status = PersistenceResponseCode.ERR_DB_INTERNAL;
                                    return;
                                }
                            }
                        }
                        params.clear();
                        params.put("Version", version);
                        if (1 != database.update("ShoppingLists", params, "ListID=?", new String[] {String.valueOf(listID)})) {
                            status = PersistenceResponseCode.ERR_DB_INTERNAL;
                            return;
                        }
                    }
                    database.setTransactionSuccessful();
                }finally {
                    database.endTransaction();
                    database.close();
                }
            }
            private void handleOutdatedError(final int listID, final int householdID) {
                SQLiteDatabase database = dbHandler.getWritableDatabase();
                database.beginTransaction();
                ContentValues params = new ContentValues();
                try {
                    //Fetch new household information
                    Cursor c = database.query("Households", new String[] {"Version"}, "ID=?", new String[] {String.valueOf(householdID)}, null, null, null);
                    if (!c.moveToFirst()) {
                        c.close();
                        database.close();
                        status = PersistenceResponseCode.ERR_DB_DATA_NOT_FOUND;
                        return;
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
                            if (invresp == null) return;
                            if (!updateLocalInventory(database, householdID, invresp)) return;
                        }

                    }
                    if (updateLocalList(database, householdID, listID, context, 0) == -1) return;
                    database.setTransactionSuccessful();
                } finally {
                    database.endTransaction();
                    database.close();
                }
            }
        };
        task.execute((Void)null);
    }

    /**
     * Modifies a cart value for a single item in a given shopping list
     * @param householdID The household that contains the list
     * @param listID The list that should be modified
     * @param UPC The item that should have its quantity modified
     * @param quantity The new shopping cart quantity for this item.  Does not necessarily have to match the defined list quantity for the item, but the item must exist in the list.
     * @param fractional The new fractional shopping cart quantity for this item.  Does not necessarily have to match the defined list fractional quantity for the item, but the item must exist in the list.
     * @param callback Called after execution is complete.  The returnType and returnValue are always null.
     */
    public void updateCart(final int householdID, final int listID, final String UPC, final int quantity, final int fractional, PersistenceCallback callback) {
        PersistenceTask task = new PersistenceTask(callback) {

            @Override
            protected void doInBackground() {
                requestType = PersistenceRequestCode.UPDATE_CART;
                SQLiteDatabase database = dbHandler.getWritableDatabase();
                ContentValues params = new ContentValues();

                try {
                    params.put("CartQuantity", quantity);
                    params.put("CartFractional", fractional);
                    if (1 != database.update("ShoppingListItems", params, "HouseholdID=? AND ListID=? AND UPC=?", new String[]{String.valueOf(householdID), String.valueOf(listID), UPC})) {
                        status = PersistenceResponseCode.ERR_DB_DATA_NOT_FOUND;
                    }
                } finally {
                    database.close();
                }
            }
        };
        task.execute((Void)null);
    }

    /**
     * Fetches all values of a shopping list that have non-zero cart quantities.
     * @param listID The list that should be queried
     * @param callback Called after execution is complete.  The returnType is GetShoppingListResponse, and the returnValue holds all item information about each shopping list item.
     * @see com.example.android.virtualpantry.Data.JSONModels.GetShoppingListResponse
     */
    public void getCartItems(final int listID, PersistenceCallback callback) {
        PersistenceTask task = new PersistenceTask(callback) {
            @Override
            protected void doInBackground() {
                requestType = PersistenceRequestCode.FETCH_CART;
                SQLiteDatabase database = dbHandler.getReadableDatabase();
                List<JSONModels.GetShoppingListResponse.Item> items = new ArrayList<JSONModels.GetShoppingListResponse.Item>();
                try {

                    Cursor c = database.query("ShoppingLists", new String[]{"Name", "Version"}, "ListID=?", new String[]{String.valueOf(listID)}, null, null, null);
                    if (!c.moveToFirst()) {
                        c.close();
                        status = PersistenceResponseCode.ERR_DB_DATA_NOT_FOUND;
                        return;
                    }
                    String name = c.getString(0);
                    long version = c.getLong(1);
                    c.close();
                    c = database.rawQuery("SELECT I.PackageQuantity, I.PackageUnits, I.PackageName, I.Description, S.UPC, S.DefinedQuantity, S.DefinedFractional, S.CartQuantity, S.CartFractional "
                            + "FROM ShoppingListItems S INNER JOIN InventoryItems I ON (S.UPC=I.UPC AND S.HouseholdID=I.HouseholdID) "
                            + "WHERE S.ListID=? AND (S.CartQuantity>0 OR S.CartFractional>0);", new String[]{String.valueOf(listID)});

                    while (c.moveToNext()) {
                        int unitID = c.getInt(1);
                        UnitTypes t = UnitTypes.fromID(unitID);
                        JSONModels.GetShoppingListResponse.Item.ListItemPackaging packaging = new JSONModels.GetShoppingListResponse.Item.ListItemPackaging(c.getFloat(0), unitID, t.getUnitName(), t.getUnitAbbrev(), c.getString(2));
                        JSONModels.GetShoppingListResponse.Item item = new JSONModels.GetShoppingListResponse.Item(c.getString(4), c.getString(3), c.getInt(5), c.getInt(7), c.getInt(8), c.getInt(6), packaging);
                        items.add(item);
                    }
                    c.close();
                    returnType = JSONModels.GetShoppingListResponse.class;
                    returnValue = new JSONModels.GetShoppingListResponse(version, name, items);
                }finally {
                    database.close();
                }
            }
        };
        task.execute((Void)null);
    }

    /**
     * Returns all list items and information for a specified listID.  Cart quantities are included, but may be zero if items have not been added to the cart.
     * @param householdID The household that contains the list
     * @param listID The list that should be queried
     * @param forceRefresh If this is true, the function will always try to pull the most recent information from the server.  If this is set to true and status is returned as ERR_CLIENT_CONNECT, or if this is set to false, the most recent local data is returned, if there is any.  All other error cases will return enumerated error messages.
     * @param callback Called after execution is complete.  The returnType is GetShoppingListResponse, and the returnValue holds all item information about each shopping list item.
     */
    public void getListItems(final int householdID, final int listID, final boolean forceRefresh, PersistenceCallback callback) {
        PersistenceTask task = new PersistenceTask(callback) {
            @Override
            protected void doInBackground() {
                requestType = PersistenceRequestCode.FETCH_LIST;
                SQLiteDatabase database = dbHandler.getWritableDatabase();
                List<JSONModels.GetShoppingListResponse.Item> items = new ArrayList<JSONModels.GetShoppingListResponse.Item>();
                try {
                    database.beginTransaction();
                    Cursor c = database.query("ShoppingLists", new String[]{"Name", "Version"}, "ListID=?", new String[]{String.valueOf(listID)}, null, null, null);
                    if (!c.moveToFirst()) {
                        c.close();
                        status = PersistenceResponseCode.ERR_DB_DATA_NOT_FOUND;
                        return;
                    }
                    String name = c.getString(0);
                    long version = c.getLong(1);
                    c.close();
                    if (forceRefresh) {
                        long newVersion = updateLocalList(database, householdID, listID, context, version);
                        if (newVersion != -1) version = newVersion;
                        else if (status != PersistenceResponseCode.ERR_CLIENT_CONNECT) return;
                    }

                    c = database.rawQuery("SELECT I.PackageQuantity, I.PackageUnits, I.PackageName, I.Description, S.UPC, S.DefinedQuantity, S.DefinedFractional, S.CartQuantity, S.CartFractional "
                            + "FROM ShoppingListItems S INNER JOIN InventoryItems I ON (S.UPC=I.UPC AND S.HouseholdID=I.HouseholdID) "
                            + "WHERE S.ListID=?;", new String[]{String.valueOf(listID)});

                    while (c.moveToNext()) {
                        int unitID = c.getInt(1);
                        UnitTypes t = UnitTypes.fromID(unitID);
                        JSONModels.GetShoppingListResponse.Item.ListItemPackaging packaging = new JSONModels.GetShoppingListResponse.Item.ListItemPackaging(c.getFloat(0), unitID, t.getUnitName(), t.getUnitAbbrev(), c.getString(2));
                        JSONModels.GetShoppingListResponse.Item item = new JSONModels.GetShoppingListResponse.Item(c.getString(4), c.getString(3), c.getInt(5), c.getInt(6), c.getInt(7), c.getInt(8), packaging);
                        items.add(item);
                    }
                    c.close();
                    returnType = JSONModels.GetShoppingListResponse.class;
                    returnValue = new JSONModels.GetShoppingListResponse(version, name, items);
                    database.setTransactionSuccessful();
                }finally {
                    database.endTransaction();
                    database.close();
                }
            }
        };
        task.execute((Void)null);
    }

    /**
     * Attempts to commit the current items in the cart to a household's inventory and clears all quantities from the cart.  If there is any sort of error, this method does not modify the household inventory or cart.<br/>
     * Note that in case of success, the household is locally modified to the most updated state.
     * @param householdID The household that contains the list
     * @param listID The list for which the cart should be committed
     * @param callback Called after execution is complete.  The returnType and returnValue are always null.
     */
    public void commitCart(final int householdID, final int listID, PersistenceCallback callback) {
        PersistenceTask task = new PersistenceTask(callback) {
            @Override
            protected void doInBackground() {
                requestType = PersistenceRequestCode.COMMIT_CART;
                SQLiteDatabase database = dbHandler.getWritableDatabase();
                try {
                    Cursor c = database.query("Households", new String[]{"Version"}, "ID=?", new String[]{String.valueOf(householdID)}, null, null, null);
                    if (!c.moveToFirst()) {
                        status = PersistenceResponseCode.ERR_DB_DATA_NOT_FOUND;
                        c.close();
                        return;
                    }
                    long version = c.getLong(0);
                    c.close();

                    c = database.rawQuery("SELECT S.UPC, S.CartQuantity, S.CartFractional, I.Quantity, I.Fractional FROM ShoppingListItems S INNER JOIN InventoryItems I ON (I.UPC = S.UPC AND I.HouseholdID = S.HouseholdID) "
                            + "WHERE (S.ListID = ? AND S.CartQuantity>0 AND S.CartFractional>0);", new String[]{String.valueOf(listID)});
                    if (!c.moveToFirst()) {
                        status = PersistenceResponseCode.ERR_DB_DATA_NOT_FOUND;
                    }
                    List<JSONModels.UpdateInventoryRequest.UpdateInventoryItem> items = new ArrayList<JSONModels.UpdateInventoryRequest.UpdateInventoryItem>();
                    do {
                        int quantity = c.getInt(1) + c.getInt(3);
                        int fractional = c.getInt(2) + c.getInt(4);
                        if (fractional >= 100) {
                            fractional -= 100;
                            quantity ++;
                        }
                        items.add(new JSONModels.UpdateInventoryRequest.UpdateInventoryItem(c.getString(0), quantity, fractional));
                    } while (c.moveToNext());
                    c.close();

                    Request req = new Request(NetworkUtility.createUpdateInventoryString(householdID,
                            context.getSharedPreferences(PreferencesHelper.USER_INFO, Context.MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null)),
                            Request.POST, new JSONModels.UpdateInventoryRequest(version, items));
                    if (req.openConnection()) {
                        req.execute();
                        JSONModels.UpdateInventoryResponse uir = parseWebResponse(req, JSONModels.UpdateInventoryResponse.class);
                        if (uir == null) return;
                        database.beginTransaction();
                        try {
                            ContentValues params = new ContentValues();
                            for (JSONModels.UpdateInventoryRequest.UpdateInventoryItem item : items) {
                                params.clear();
                                params.put("Quantity", item.quantity);
                                params.put("Fractional", item.fractional);
                                if (1 != database.update("InventoryItems", params, "UPC=? AND HouseholdID=? AND ListID=?", new String[] {item.UPC, String.valueOf(householdID), String.valueOf(listID)})) {
                                    status = PersistenceResponseCode.ERR_DB_DATA_NOT_FOUND;
                                    return;
                                }

                            }
                            params.clear();
                            params.put("Version", uir.version);
                            if (1 != database.update("Households", params, "ID=?", new String[] {String.valueOf(householdID)})) {
                                status = PersistenceResponseCode.ERR_DB_INTERNAL;
                                return;
                            }

                            params.clear();
                            params.put("CartQuantity", 0);
                            params.put("CartFractional", 0);
                            if (0 >= database.update("ShoppingListItems", params, "ListID=?", new String[] {String.valueOf(listID)}));

                            database.setTransactionSuccessful();
                        } finally {
                            database.endTransaction();
                        }
                    }
                } finally {
                    database.close();
                }
            }
        };
        task.execute((Void)null);
    }

    /**
     * Attempts to delete a list on the server and update the local database.  All cart items will be lost if this method is successful
     * @param householdID The household that contains the list
     * @param listID The list that should be deleted
     * @param callback Called after execution is complete.  The returnType and returnValue are always null.
     */
    public void deleteList(final int householdID, final int listID, PersistenceCallback callback) {
        throw new UnsupportedOperationException();
    }


}
