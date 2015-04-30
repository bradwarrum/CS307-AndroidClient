package com.example.android.virtualpantry;

import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Database.ListDataSource;
import com.example.android.virtualpantry.Database.PersistenceRequestCode;
import com.example.android.virtualpantry.Database.PersistenceResponseCode;
import com.example.android.virtualpantry.Database.PreferencesHelper;
import com.example.android.virtualpantry.Network.NetworkUtility;
import com.example.android.virtualpantry.Network.Request;

import com.example.android.virtualpantry.Data.JSONModels.UpdateListRequest.UpdateListItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ShoppingListActivity extends UserActivity {

    private static final String LOG_TAG = "ShoppingListActivity";
    private TextView mHeader;
    private TextView mVersion;
    private Button mAddItemButton;
    private ListView mShoppingList;
    private int mListID;
    private int mHouseholdID;

    private ListDataSource listDataSource;

    private JSONModels.GetShoppingListResponse mShoppingListJSON;

    private List<Map<String, String>> mListData;
    private SimpleAdapter mListDataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);
        Intent myIntent = getIntent();
        if(myIntent.hasExtra("householdID")){
            mHouseholdID = myIntent.getIntExtra("householdID", -1);
        } else{
            Log.e(LOG_TAG, "Calling intent did not have a household ID");
        }
        if(myIntent.hasExtra("listID")){
            mListID = (int)myIntent.getLongExtra("listID", -1);
        } else {
            Log.e(LOG_TAG, "Calling intent did not have a household ID");
        }
    }

    @Override
    protected  void onResume(){
        super.onResume();
        //handles
        mHeader = (TextView) findViewById(R.id.ShoppingListTitle);
        mVersion = (TextView) findViewById(R.id.ShoppingListVersionNo);
        mAddItemButton = (Button) findViewById(R.id.AddItemButton);
        mShoppingList = (ListView) findViewById(R.id.ShoppingItemList);

        listDataSource = new ListDataSource(this);

        String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE)
                .getString(PreferencesHelper.TOKEN, null);
        if(token == null){
            cancelToLoginPage();
        }
        listDataSource.getListItems(mHouseholdID, mListID, true, this);
        //new GetListTask(mHouseholdID, mListID, token).execute((Void) null);
    }

    @Override
    public void callback(PersistenceRequestCode request, PersistenceResponseCode status, Object returnValue, Type returnType) {
        super.callback(request, status, returnValue, returnType);
        if(status == PersistenceResponseCode.SUCCESS){
            switch(request){
                case FETCH_LIST:
                    if(returnType == JSONModels.GetShoppingListResponse.class) {
                        JSONModels.GetShoppingListResponse shoppingListResponse = (JSONModels.GetShoppingListResponse) returnValue;
                        updateDisplay(shoppingListResponse);
                    } else {
                        Log.e(LOG_TAG, "Error wrong return type: " + returnType);
                    }
                    break;
                case UPDATE_LIST:
                    listDataSource.getListItems(mHouseholdID, mListID, true, this);
                    break;
                default:
                    Log.e(LOG_TAG, "Unknown database request callback: " + request + ", " + status);
            }
        }
    }

    private void updateDisplay(JSONModels.GetShoppingListResponse response){
        mShoppingListJSON = response;
        mHeader.setText(mShoppingListJSON.name);
        mVersion.setText(new Long(mShoppingListJSON.version).toString());
        mListData = new ArrayList<Map<String, String>>();
        for(JSONModels.GetShoppingListResponse.Item item : mShoppingListJSON.items){
            Map<String, String> listItem = new HashMap<>(2);
            listItem.put("itemName", item.description);
            String subtitle = "";
            subtitle += "UPC:" + item.UPC + " - " + item.quantity;
            /*if(item.fractional != 0){
                subtitle += "/" + item.fractional;
            }*/
            //subtitle += " " + item.unitName;
            subtitle += " " +  item.packaging.packageName;
            listItem.put("info", subtitle);
            mListData.add(listItem);
        }
        mListDataAdapter = new SimpleAdapter(
                this,
                mListData,
                android.R.layout.simple_list_item_2,
                new String[]{"itemName", "info"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        mShoppingList.setAdapter(mListDataAdapter);
        mAddItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShoppingListActivity.this, MyItemsActivity.class);
                intent.putExtra("householdID", mHouseholdID);
                intent.putExtra("listID", mListID);
                intent.putExtra("mode", MyItemsActivity.LIST_MODE);
                startActivity(intent);
            }
        });
        mShoppingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                changeItemProperties(position);
            }
        });
    }

    private void changeItemProperties(final int position) {
        Button removeItemButton;
        final EditText newQuantity;
        final Button updateButton;
        Button cancelButton;
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        alertBuilder.setView(inflater.inflate(R.layout.dialog_change_shopping_list, null));

        //make the dialog
        final AlertDialog dialog = alertBuilder.show();

        //grab elements
        removeItemButton = (Button) dialog.findViewById(R.id.ListRemoveItemButton);
        newQuantity = (EditText) dialog.findViewById(R.id.ListChangeQuantityInput);
        updateButton = (Button) dialog.findViewById(R.id.ListItemUpdateButton);
        cancelButton = (Button) dialog.findViewById(R.id.ListItemCancelButton);

        //setreactions
        ((TextView) dialog.findViewById(R.id.ListItemDialogTitle)).setText(mShoppingListJSON.items.get(position).description);
        newQuantity.setText(String.valueOf(mShoppingListJSON.items.get(position).quantity));
        removeItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(position);
                dialog.cancel();
            }
        });
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity, fraction;
                String value = newQuantity.getText().toString();
                if(value.contains(".")){
                    quantity = value.split(".")[0];
                    fraction = value.split(".")[1];
                } else {
                    quantity = value;
                    fraction = "0";
                }
                updateQuantity(position, quantity, fraction);
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

    private void removeItem(int position){
        String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE)
                .getString(PreferencesHelper.TOKEN, null);
        if(token == null){
            cancelToLoginPage();
        }
        UpdateListItem updateItem = new UpdateListItem(mShoppingListJSON.items.get(position).UPC, 0, 0);
        List<UpdateListItem> updateList = new ArrayList<>();
        updateList.add(updateItem);
        listDataSource.updateList(mListID, updateList, this);
        /*new UpdateListQuantityTask(mHouseholdID, mShoppingListJSON.version,
                mShoppingListJSON.items.get(position).UPC,
                0,
                0,
                token).execute((Void) null);*/
    }

    private void updateQuantity(int position, String quantity, String fractional){
        String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE)
                .getString(PreferencesHelper.TOKEN, null);
        if(token == null){
            cancelToLoginPage();
        }
        UpdateListItem updateItem = new UpdateListItem(mShoppingListJSON.items.get(position).UPC, new Integer(quantity).intValue(), new Integer(fractional).intValue());
        List<UpdateListItem> updateList = new ArrayList<>();
        updateList.add(updateItem);
        listDataSource.updateList(mListID, updateList, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shopping_list, menu);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false); // disable the button
            actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
            actionBar.setDisplayShowHomeEnabled(false); // remove the icon
        }
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
    private class GetListTask extends AsyncTask<Void, Void, Integer> {

        private static final String LOG_TAG = "GetListTask";
        private final long mHouseholdID;
        private final long mListID;
        private String mToken;
        private Request request;

        public GetListTask(long householdID, long listID, String token) {
            mHouseholdID = householdID;
            mListID = listID;
            mToken = token;
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
                    if(NetworkUtility.loginSequence(ShoppingListActivity.this) == 1) {
                        mToken = ShoppingListActivity.this.getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null);
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

        private Request request;


        public UpdateListQuantityTask(long householdID, long version, String UPC, int quantity, int fractional, String token){
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
                    NetworkUtility.createUpdateShoppingListString(mHouseholdID, mListID, mToken),
                    Request.POST,
                    updateJSON
            );
            if(request.openConnection()){
                request.execute();
                if(request.getResponseCode() == 403){
                    //login again
                    if(NetworkUtility.loginSequence(ShoppingListActivity.this) == 1) {
                        mToken = ShoppingListActivity.this.getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null);
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
                    new GetListTask(mHouseholdID, mListID, mToken).execute((Void) null);
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
