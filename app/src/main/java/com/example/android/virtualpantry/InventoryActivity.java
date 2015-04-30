package com.example.android.virtualpantry;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Database.HouseholdDataSource;
import com.example.android.virtualpantry.Database.InventoryDataSource;
import com.example.android.virtualpantry.Database.ListDataSource;
import com.example.android.virtualpantry.Database.PersistenceRequestCode;
import com.example.android.virtualpantry.Database.PersistenceResponseCode;
import com.example.android.virtualpantry.Database.PreferencesHelper;
import com.example.android.virtualpantry.Network.NetworkUtility;
import com.example.android.virtualpantry.Network.Request;

import com.example.android.virtualpantry.Data.JSONModels.UpdateInventoryRequest.UpdateInventoryItem;
import com.example.android.virtualpantry.Data.JSONModels.GetInventoryResponse.InventoryItem;
import com.example.android.virtualpantry.Data.JSONModels.UpdateListRequest.UpdateListItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryActivity extends UserActivity {

    private static final String LOG_TAG = "InventoryActivity";
    private TextView mHeader;
    private TextView mVersion;
    private Button mAddItemButton;
    private ListView mInventory;
    private int mHouseholdID;

    //private JSONModels.GetInventoryResponse mInventoryJSON;
    private List<InventoryItem> mInventoryItems;
    private JSONModels.Household mHousehold;

    private String householdName;

    private List<Map<String, String>> mInventoryData;
    private SimpleAdapter mInventoryDataAdapter;

    private ListDataSource listDataSource;
    private InventoryDataSource invDataSource;
    private HouseholdDataSource householdDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        Intent myIntent = getIntent();
        if(myIntent.hasExtra("householdID")){
            mHouseholdID = myIntent.getIntExtra("householdID", -1);
        } else{
            Log.e(LOG_TAG, "Calling intent did not have a household ID");
        }
        if(myIntent.hasExtra("householdName")){
            householdName = myIntent.getStringExtra("householdName");
        }
    }

    @Override
    protected  void onResume() {
        super.onResume();
        mHeader = (TextView) findViewById(R.id.InventoryTitle);
        mVersion = (TextView) findViewById(R.id.InventoryVersionNo);
        mAddItemButton = (Button) findViewById(R.id.InventoryAddItemButton);
        mInventory = (ListView) findViewById(R.id.InventoryItemList);

        if(householdName != null){
            mHeader.setText(householdName + " Inventory");
        }
        String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE)
                .getString(PreferencesHelper.TOKEN, null);
        if(token == null){
            Intent intent = new Intent(this, LoginRegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        householdDataSource = new HouseholdDataSource(this);
        listDataSource = new ListDataSource(this);
        invDataSource = new InventoryDataSource(this);
        invDataSource.getInventory(mHouseholdID, true, this);
        householdDataSource.getHouseholdInfo(mHouseholdID, true, this);
        /*new GetInventoryTask(mHouseholdID, token).execute((Void) null);
        new GetHouseholdInfoTask(mHouseholdID, token).execute((Void) null);*/
    }

    @Override
    public void callback(PersistenceRequestCode request, PersistenceResponseCode status, Object returnValue, Type returnType) {
        super.callback(request, status, returnValue, returnType);
        if(status == PersistenceResponseCode.SUCCESS){
            switch(request){
                case FETCH_INVENTORY:
                    List<InventoryItem> inventoryItems = (List<InventoryItem>) returnValue;
                    updateDisplay(inventoryItems);
                    break;
                case UPDATE_INVENTORY:
                    invDataSource.getInventory(mHouseholdID, true, this);
                    break;
                case UPDATE_LIST:
                    invDataSource.getInventory(mHouseholdID, true, this);
                    break;
                case FETCH_HOUSEHOLD:
                    mHousehold = (JSONModels.Household) returnValue;
                    break;
                default:
                    Toast.makeText(this, "Unknown callback: " + request + " result in " + status, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Error in data access of " + request + " result in " + status, Toast.LENGTH_LONG).show();
        }
    }

    private void updateDisplay(List<InventoryItem> inventoryItems){
        mInventoryItems = inventoryItems;
        mInventoryData = new ArrayList<Map<String, String>>();
        List<JSONModels.GetInventoryResponse.InventoryItem> emptyItems = new ArrayList<>();
        for(JSONModels.GetInventoryResponse.InventoryItem item : inventoryItems){
            if(item.quantity == 0){
                emptyItems.add(item);
            }
        }
        for(JSONModels.GetInventoryResponse.InventoryItem item : emptyItems){
            inventoryItems.remove(item);
        }
        for(JSONModels.GetInventoryResponse.InventoryItem item : inventoryItems){
            Map<String, String> inventoryItem = new HashMap<>(2);
            inventoryItem.put("itemName", item.description);
            String subtitle = "";
            subtitle += "UPC:" + item.UPC + " - " + item.quantity;
            subtitle += " " + item.packaging.packageName;
            inventoryItem.put("info", subtitle);
            mInventoryData.add(inventoryItem);
        }
        mInventoryDataAdapter = new SimpleAdapter(
                this,
                mInventoryData,
                android.R.layout.simple_list_item_2,
                new String[]{"itemName", "info"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        mInventory.setAdapter(mInventoryDataAdapter);
        mAddItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InventoryActivity.this, AddItemActivity.class);
                intent.putExtra("householdID", mHouseholdID);
                intent.putExtra("mode", AddItemActivity.INVENTORY_MODE);
                startActivity(intent);
            }
        });
        mInventory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                changeItemProperties(position);
            }
        });
    }

    private void changeItemProperties(final int position){
        Button removeItemButton;
        Button addToListButton;
        final EditText newQuantity;
        final Button updateButton;
        Button cancelButton;
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        alertBuilder.setView(inflater.inflate(R.layout.dialog_change_inventory, null));

        //make the dialog
        final AlertDialog dialog = alertBuilder.show();

        //grab elements
        removeItemButton = (Button) dialog.findViewById(R.id.InventoryRemoveItemButton);
        addToListButton = (Button) dialog.findViewById(R.id.InventoryToListButton);
        newQuantity = (EditText) dialog.findViewById(R.id.ChangeQuantityInput);
        updateButton = (Button) dialog.findViewById(R.id.InventoryItemUpdateButton);
        cancelButton = (Button) dialog.findViewById(R.id.InventoryItemCancelButton);

        //setreactions
        ((TextView) dialog.findViewById(R.id.InventoryItemDialogTitle)).setText(mInventoryItems.get(position).description);
        newQuantity.setText(String.valueOf(mInventoryItems.get(position).quantity));
        removeItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(position);
                dialog.cancel();
            }
        });
        addToListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveItem(mInventoryItems.get(position).UPC, newQuantity.getText().toString());
                dialog.cancel();
            }
        });
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateQuantity(position, newQuantity.getText().toString());
                dialog.cancel();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

    }

    private void updateQuantity(int position, String totalQuantity){
        String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE)
                .getString(PreferencesHelper.TOKEN, null);
        if(token == null){
            cancelToLoginPage();
        }
        String quantity, fractional;
        if(totalQuantity.contains(".")){
            quantity = totalQuantity.split(".")[0];
            fractional = totalQuantity.split(".")[1];
        } else {
            quantity = totalQuantity;
            fractional = "0";
        }
        UpdateInventoryItem updateInventoryItem = new UpdateInventoryItem(mInventoryItems.get(position).UPC, Integer.valueOf(quantity), Integer.valueOf(fractional));
        List<UpdateInventoryItem> updateList = new ArrayList<>();
        updateList.add(updateInventoryItem);
        invDataSource.updateInventoryQuantity(mHouseholdID, updateList, this);
    }

    private void removeItem(int position){
        String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE)
                .getString(PreferencesHelper.TOKEN, null);
        if(token == null){
            cancelToLoginPage();
        }
        UpdateInventoryItem updateInventoryItem = new UpdateInventoryItem(mInventoryItems.get(position).UPC, 0, 0);
        List<UpdateInventoryItem> updateList = new ArrayList<>();
        invDataSource.updateInventoryQuantity(mHouseholdID, updateList, this);
    }

    private void moveItem(final String UPC, final String quantity){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Move Item to Shopping list");
        final Spinner spinner = new Spinner(this);
        List<String> lists = new ArrayList<String>();
        for(JSONModels.Household.HouseholdList list : mHousehold.lists){
            lists.add(list.listName);
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item, lists);
        spinner.setAdapter(spinnerAdapter);
        builder.setView(spinner);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                moveItemToList(UPC, quantity, (int)mHousehold.lists.get(spinner.getSelectedItemPosition()).listID);
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void moveItemToList(String UPC, String totalQuantity, int listID){
        String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE)
                .getString(PreferencesHelper.TOKEN, null);
        if(token == null){
            cancelToLoginPage();
        }
        String quantity, fractional;
        if(totalQuantity.contains(".")){
            quantity = totalQuantity.split(".")[0];
            fractional = totalQuantity.split(".")[1];
        } else {
            quantity = totalQuantity;
            fractional = "0";
        }
        List<UpdateListItem> updateList = new ArrayList<>();
        updateList.add(new UpdateListItem(UPC, Integer.valueOf(quantity), Integer.valueOf(fractional)));
        listDataSource.updateList(listID, updateList, this);
        //listDataSource.getListItems(mHouseholdID, listID, true, this);
        //new GetListTask(mHouseholdID, listID, UPC, quantity, token).execute((Void) null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    private class GetInventoryTask extends AsyncTask<Void, Void, Integer>{

        private static final String LOG_TAG = "GetInventoryTask";
        private final long mHouseholdID;
        private String mToken;
        private Request request;

        public GetInventoryTask(long householdID, String token) {
            mHouseholdID = householdID;
            mToken = token;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            request = new Request(
                    NetworkUtility.createGetInventoryString(mHouseholdID, mToken),
                    Request.GET
            );
            if(request.openConnection()){
                request.execute();
                if(request.getResponseCode() == 403){
                    //login again
                    if(NetworkUtility.loginSequence(InventoryActivity.this) == 1) {
                        mToken = InventoryActivity.this.getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null);
                        if(mToken != null) {
                            request = new Request(
                                    NetworkUtility.createGetUserInfoString(mToken),
                                    Request.GET);
                            return doInBackground((Void) null);
                        } else {
                            Log.e(LOG_TAG, "Token was null after re-login");
                            return -1;
                        }
                    } else {
                        Log.e(LOG_TAG, "Unable to log in again");
                        return -1;
                    }
                }
            } else {
                Log.e(LOG_TAG, "Unable to open connection");
                return -1;
            }
            return request.getResponseCode();
        }

        @Override
        protected void onPostExecute(Integer result) {
            switch (result) {
                case 200:
                    updateDisplay(request.getResponse());
                    break;
                default:
                    Log.e(LOG_TAG, "Failed to get inventory info: " +
                            request.getResponseCode() + "\nResponse: " + request.getResponse() + "\nAt: " + request.getFilePath());
                    break;
            }
        }
    }


    private class UpdateInventoryQuantityTask extends AsyncTask<Void, Void, Integer>{

        private static final String LOG_TAG = "UpdateInvQtyTask";
        private final long mVersion;
        private String mToken;
        private final long mHouseholdID;
        private final String mUPC;
        private final int mQuantity;
        private final int mFractional;

        private Request request;


        public UpdateInventoryQuantityTask(long householdID, long version, String UPC, int quantity, int fractional, String token){
            mVersion = version;
            mHouseholdID = householdID;
            mUPC = UPC;
            mQuantity = quantity;
            mFractional = fractional;
            mToken = token;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            //INVENTORY MODE
            List<JSONModels.UpdateInventoryRequest.UpdateInventoryItem> items = new ArrayList<>();
            items.add(new JSONModels.UpdateInventoryRequest.UpdateInventoryItem(mUPC, mQuantity, mFractional));
            JSONModels.UpdateInventoryRequest updateJSON = new JSONModels.UpdateInventoryRequest(mVersion, items);
            request = new Request(
                    NetworkUtility.createUpdateInventoryString(mHouseholdID, mToken),
                    Request.POST,
                    updateJSON
            );
            if(request.openConnection()){
                request.execute();
                if(request.getResponseCode() == 403){
                    //login again
                    if(NetworkUtility.loginSequence(InventoryActivity.this) == 1) {
                        mToken = InventoryActivity.this.getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null);
                        if(mToken != null) {
                            request = new Request(
                                    NetworkUtility.createGetUserInfoString(mToken),
                                    Request.GET);
                            return doInBackground((Void) null);
                        } else {
                            Log.e(LOG_TAG, "Token was null after re-login");
                            return -1;
                        }
                    } else {
                        Log.e(LOG_TAG, "Unable to log in again");
                        return -1;
                    }
                }
            } else {
                Log.e(LOG_TAG, "Unable to open connection");
                return -1;
            }
            return request.getResponseCode();
        }

        @Override
        protected void onPostExecute(Integer result) {
            switch(result){
                case 200:
                    new GetInventoryTask(mHouseholdID, mToken).execute((Void) null);
                    break;
                default:
                    Log.e(LOG_TAG, "Failed to update inventory info: " +
                            result + "\nResponse: " + request.getResponse() + "\nAt: " + request.getFilePath());
                    break;
            }
        }
    }

    private class GetHouseholdInfoTask extends AsyncTask<Void, Void, Integer>{

        private static final String LOG_TAG = "GetHouseholdInfoTask";
        private final long mHouseholdID;
        private String mToken;
        private Request request;

        public GetHouseholdInfoTask(long householdID, String token) {
            mHouseholdID = householdID;
            mToken = token;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            request = new Request(
                    NetworkUtility.createGetHouseholdString(mHouseholdID, mToken),
                    Request.GET
            );
            if(request.openConnection()){
                request.execute();
                if(request.getResponseCode() == 403){
                    //login again
                    if(NetworkUtility.loginSequence(InventoryActivity.this) == 1) {
                        mToken = InventoryActivity.this.getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null);
                        if(mToken != null) {
                            request = new Request(
                                    NetworkUtility.createGetUserInfoString(mToken),
                                    Request.GET);
                            return doInBackground((Void) null);
                        } else {
                            Log.e(LOG_TAG, "Token was null after re-login");
                            return -1;
                        }
                    } else {
                        Log.e(LOG_TAG, "Unable to log in again");
                        return -1;
                    }
                }
            } else {
                Log.e(LOG_TAG, "Unable to open connection");
                return -1;
            }
            return request.getResponseCode();
        }

        @Override
        protected void onPostExecute(Integer result) {
            switch(result){
                case 200:
                    mHousehold = JSONModels.gson.fromJson(request.getResponse(), JSONModels.Household.class);
                    break;
                default:
                    Log.e(LOG_TAG, "Failed to get household info: " +
                            result + "\nResponse: " + request.getResponse() + "\nAt: " + request.getFilePath());
                    break;
            }

        }
    }

    private class GetListTask extends AsyncTask<Void, Void, Integer> {

        private static final String LOG_TAG = "GetListTask";
        private final long mHouseholdID;
        private final long mListID;
        private String mToken;
        private Request request;
        private final String mUPC;
        private final int mQuantity;

        public GetListTask(long householdID, long listID, String upc, int quantity, String token) {
            mHouseholdID = householdID;
            mListID = listID;
            mToken = token;
            mUPC = upc;
            mQuantity = quantity;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            request = new Request(
                    NetworkUtility.createGetListString(mHouseholdID, mListID, mToken),
                    Request.GET
            );
            if(request.openConnection()){
                request.execute();
                if(request.getResponseCode() == 403){
                    //login again
                    if(NetworkUtility.loginSequence(InventoryActivity.this) == 1) {
                        mToken = InventoryActivity.this.getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null);
                        if(mToken != null) {
                            request = new Request(
                                    NetworkUtility.createGetUserInfoString(mToken),
                                    Request.GET);
                            return doInBackground((Void) null);
                        } else {
                            Log.e(LOG_TAG, "Token was null after re-login");
                            return -1;
                        }
                    } else {
                        Log.e(LOG_TAG, "Unable to log in again");
                        return -1;
                    }
                }
            } else {
                Log.e(LOG_TAG, "Unable to open connection");
                return -1;
            }
            return request.getResponseCode();
        }

        @Override
        protected void onPostExecute(Integer result) {
            switch (result) {
                case 200:
                    long version = JSONModels.gson.fromJson(request.getResponse(), JSONModels.GetShoppingListResponse.class).version;
                    new UpdateListQuantityTask(mHouseholdID, mListID, version, mUPC, mQuantity, 0, mToken).execute((Void) null);
                    break;
                default:
                    Log.e(LOG_TAG, "Failed to get list info 1: " +
                            request.getResponseCode() + "\nResponse: " + request.getResponse() + "\nAt: " + request.getFilePath());
                    break;
            }
        }
    }

    private class UpdateListQuantityTask extends AsyncTask<Void, Void, Integer>{

        private static final String LOG_TAG = "UpdateListQtyTask";
        private final long mVersion;
        private String mToken;
        private final long mHouseholdID;
        private final String mUPC;
        private final int mQuantity;
        private final int mFractional;
        private final long mListID;

        private Request request;


        public UpdateListQuantityTask(long householdID, long listID, long version, String UPC, int quantity, int fractional, String token){
            mVersion = version;
            mHouseholdID = householdID;
            mUPC = UPC;
            mQuantity = quantity;
            mFractional = fractional;
            mToken = token;
            mListID = listID;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            //INVENTORY MODE
            List<JSONModels.UpdateInventoryRequest.UpdateInventoryItem> items = new ArrayList<>();
            items.add(new JSONModels.UpdateInventoryRequest.UpdateInventoryItem(mUPC, mQuantity, mFractional));
            JSONModels.UpdateInventoryRequest updateJSON = new JSONModels.UpdateInventoryRequest(mVersion, items);
            request = new Request(
                    NetworkUtility.createUpdateShoppingListString(mHouseholdID, mListID, mToken),
                    Request.POST,
                    updateJSON
            );
            if(request.openConnection()){
                request.execute();
                if(request.getResponseCode() == 403){
                    //login again
                    if(NetworkUtility.loginSequence(InventoryActivity.this) == 1) {
                        mToken = InventoryActivity.this.getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null);
                        if(mToken != null) {
                            request = new Request(
                                    NetworkUtility.createGetUserInfoString(mToken),
                                    Request.GET);
                            return doInBackground((Void) null);
                        } else {
                            Log.e(LOG_TAG, "Token was null after re-login");
                            return -1;
                        }
                    } else {
                        Log.e(LOG_TAG, "Unable to log in again");
                        return -1;
                    }
                }
            } else {
                Log.e(LOG_TAG, "Unable to open connection");
                return -1;
            }
            return request.getResponseCode();
        }

        @Override
        protected void onPostExecute(Integer result) {
            switch(result){
                case 200:
                    //todo:
                    break;
                default:
                    Log.e(LOG_TAG, "Failed to update inventory info: " +
                            result + "\nResponse: " + request.getResponse() + "\nAt: " + request.getFilePath());
                    Log.e(LOG_TAG, "Sent:\n" + request.getSendJSON());
                    break;
            }
        }
    }*/
}
