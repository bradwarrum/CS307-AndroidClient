package com.example.android.virtualpantry;

import android.app.AlertDialog;
import android.app.Dialog;
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
import com.example.android.virtualpantry.Database.PreferencesHelper;
import com.example.android.virtualpantry.Network.NetworkUtility;
import com.example.android.virtualpantry.Network.Request;
import com.example.android.virtualpantry.Data.JSONModels.GetInventoryResJSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryActivity extends ActionBarActivity {

    private static final String LOG_TAG = "InventoryActivity";
    private TextView mHeader;
    private TextView mVersion;
    private Button mAddItemButton;
    private ListView mInventory;
    private long mHouseholdID;

    private GetInventoryResJSON mInventoryJSON;
    private JSONModels.HouseholdJSON mHouseholdJSON;

    private String householdName;

    private List<Map<String, String>> mInventoryData;
    private SimpleAdapter mInventoryDataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        Intent myIntent = getIntent();
        if(myIntent.hasExtra("householdID")){
            mHouseholdID = myIntent.getLongExtra("householdID", -1);
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
        new GetInventoryTask(mHouseholdID, token).execute((Void) null);
        //Toast toast = Toast.makeText(this.getApplicationContext(), "Fetching Inventory", Toast.LENGTH_SHORT);
        //toast.show();
        new GetHouseholdInfoTask(mHouseholdID, token).execute((Void) null);
    }

    private void updateDisplay(String response){
        mInventoryJSON = JSONModels.gson.fromJson(response, GetInventoryResJSON.class);
        mVersion.setText(new Long(mInventoryJSON.version).toString());
        mInventoryData = new ArrayList<Map<String, String>>();
        List<GetInventoryResJSON.InventoryItemJSON> emptyItems = new ArrayList<>();
        for(GetInventoryResJSON.InventoryItemJSON item : mInventoryJSON.items){
            if(item.quantity == 0){
                emptyItems.add(item);
            }
        }
        for(GetInventoryResJSON.InventoryItemJSON item : emptyItems){
            mInventoryJSON.items.remove(item);
        }
        for(GetInventoryResJSON.InventoryItemJSON item : mInventoryJSON.items){
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
                intent.putExtra("version", new Long(mInventoryJSON.version).longValue());
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
        ((TextView) dialog.findViewById(R.id.InventoryItemDialogTitle)).setText(mInventoryJSON.items.get(position).description);
        newQuantity.setText(String.valueOf(mInventoryJSON.items.get(position).quantity));
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
                moveItem(mInventoryJSON.items.get(position).UPC, Integer.valueOf(newQuantity.getText().toString()));
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

    private void updateQuantity(int position, String quantity){
        String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE)
                .getString(PreferencesHelper.TOKEN, null);
        if(token == null){
            Intent intent = new Intent(this, LoginRegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        new UpdateInventoryQuantityTask(mHouseholdID, mInventoryJSON.version,
                mInventoryJSON.items.get(position).UPC,
                new Integer(quantity).intValue(),
                mInventoryJSON.items.get(position).fractional,
                 token).execute((Void) null);
    }

    private void removeItem(int position){
        String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE)
                .getString(PreferencesHelper.TOKEN, null);
        if(token == null){
            Intent intent = new Intent(this, LoginRegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        new UpdateInventoryQuantityTask(mHouseholdID, mInventoryJSON.version,
                mInventoryJSON.items.get(position).UPC,
                0,
                mInventoryJSON.items.get(position).fractional,
                token).execute((Void) null);
    }

    private void moveItem(final String UPC, final int quantity){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Move Item to Shopping list");
        final Spinner spinner = new Spinner(this);
        List<String> lists = new ArrayList<String>();
        for(JSONModels.HouseholdListJSON list : mHouseholdJSON.lists){
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
                moveItemToList(UPC, quantity, mHouseholdJSON.lists.get(spinner.getSelectedItemPosition()).listID);
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

    private void moveItemToList(String UPC, int quantity, long listID){
        String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE)
                .getString(PreferencesHelper.TOKEN, null);
        if(token == null){
            Intent intent = new Intent(this, LoginRegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        new GetListTask(mHouseholdID, listID, UPC, quantity, token).execute((Void) null);
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
            List<JSONModels.UpdateInventoryReqJSON.UpdateInventoryItem> items = new ArrayList<>();
            items.add(new JSONModels.UpdateInventoryReqJSON.UpdateInventoryItem(mUPC, mQuantity, mFractional));
            JSONModels.UpdateInventoryReqJSON updateJSON = new JSONModels.UpdateInventoryReqJSON(mVersion, items);
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
                    mHouseholdJSON = JSONModels.gson.fromJson(request.getResponse(), JSONModels.HouseholdJSON.class);
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
                    long version = JSONModels.gson.fromJson(request.getResponse(), JSONModels.GetShoppingListResJSON.class).version;
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
            List<JSONModels.UpdateInventoryReqJSON.UpdateInventoryItem> items = new ArrayList<>();
            items.add(new JSONModels.UpdateInventoryReqJSON.UpdateInventoryItem(mUPC, mQuantity, mFractional));
            JSONModels.UpdateInventoryReqJSON updateJSON = new JSONModels.UpdateInventoryReqJSON(mVersion, items);
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
    }
}
