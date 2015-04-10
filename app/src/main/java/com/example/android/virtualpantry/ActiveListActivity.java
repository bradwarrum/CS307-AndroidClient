package com.example.android.virtualpantry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Database.PreferencesHelper;
import com.example.android.virtualpantry.Network.NetworkUtility;
import com.example.android.virtualpantry.Network.Request;
import com.example.android.virtualpantry.Data.JSONModels.GetShoppingListResJSON;
import com.example.android.virtualpantry.Data.JSONModels.GetInventoryResJSON;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ActiveListActivity extends ActionBarActivity {

    private static final String LOG_TAG = "ActiveListActivity";
    private TextView mHeader;
    private TextView mSubtitle;
    private Button mCheckoutButton;
    private Button mScanButton;
    private ListView mActiveList;
    private Button mSwitchModesButton;
    private ListView mCartList;
    private TextView mCartModeText;

    private long mHouseholdID;
    private long mListID;

    private List<JSONModels.GetShoppingListResJSON.ItemJSON> mItemsInCart;

    private GetShoppingListResJSON mShoppingListJSON;
    private List<Map<String, String>> mListData;
    private SimpleAdapter mListDataAdapter;

    private List<Map<String, String>> mCartData;
    private SimpleAdapter mCartDataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_list);
    }

    @Override
    protected void onResume(){
        super.onResume();
        mHeader = (TextView) findViewById(R.id.ActiveListTitle);
        mSubtitle = (TextView) findViewById(R.id.ActiveListVersionNo);
        mCheckoutButton = (Button) findViewById(R.id.CheckoutButton);
        mScanButton = (Button) findViewById(R.id.ActiveListBarcodeButton);
        mActiveList = (ListView) findViewById(R.id.ActiveItemList);
        mSwitchModesButton = (Button) findViewById(R.id.ShoppingCartSwitchModeButton);
        mCartList = (ListView) findViewById(R.id.CartList);
        mCartModeText = (TextView) findViewById(R.id.ShoppingCartModeText);
        mSwitchModesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCartList.getVisibility() == View.GONE){
                    //switch to cart mode
                    mCartList.setVisibility(View.VISIBLE);
                    mActiveList.setVisibility(View.GONE);
                    mSwitchModesButton.setText(getString(R.string.ShoppingCartModeSwitchButton_ToList));
                    mCartModeText.setText(getString(R.string.ShoppingCartMode_Cart));
                } else {
                    //switch to list mode
                    mCartList.setVisibility(View.GONE);
                    mActiveList.setVisibility(View.VISIBLE);
                    mSwitchModesButton.setText(getString(R.string.ShoppingCartModeSwitchButton_ToCart));
                    mCartModeText.setText(getString(R.string.ShoppingCartMode_List));
                }
            }
        });
        mHouseholdID = getSharedPreferences(PreferencesHelper.SHOPPING_CART, MODE_PRIVATE).getLong(PreferencesHelper.SHOPPING_CART_HOUSEHOLD_ID, -1);
        mListID = getSharedPreferences(PreferencesHelper.SHOPPING_CART, MODE_PRIVATE).getLong(PreferencesHelper.SHOPPING_CART_LIST_ID, -1);
        final String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE)
                .getString(PreferencesHelper.TOKEN, null);
        if(token == null){
            Intent intent = new Intent(this, LoginRegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        new GetListTask(mHouseholdID, mListID, token).execute((Void) null);
        mCheckoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetInventoryTask(mHouseholdID, token).execute((Void) null);
            }
        });
    }

    private void updateDisplay(String response){
        String itemsInCartStr = getSharedPreferences(PreferencesHelper.SHOPPING_CART, MODE_PRIVATE)
                .getString(PreferencesHelper.SHOPPING_CART_ITEMS_IN_CART, null);
        itemsInCartStr = itemsInCartStr.trim();
        Set<String> itemsInCart = new HashSet<String>(Arrays.asList(itemsInCartStr.split(",")));
        mShoppingListJSON = JSONModels.gson.fromJson(response, JSONModels.GetShoppingListResJSON.class);
        mHeader.setText(mShoppingListJSON.name);
        mSubtitle.setText(new Long(mShoppingListJSON.version).toString());
        mListData = new ArrayList<Map<String, String>>();
        mCartData = new ArrayList<Map<String, String>>();
        mItemsInCart = new ArrayList<>();
        for(JSONModels.GetShoppingListResJSON.ItemJSON item : mShoppingListJSON.items) {
            if(itemsInCart.contains(item.UPC)){
                mItemsInCart.add(item);
            }
        }
        mShoppingListJSON.items.removeAll(mItemsInCart);
        for(JSONModels.GetShoppingListResJSON.ItemJSON item : mShoppingListJSON.items){
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
        for(JSONModels.GetShoppingListResJSON.ItemJSON item : mItemsInCart){
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
            mCartData.add(listItem);
        }
        mListDataAdapter = new SimpleAdapter(
                this,
                mListData,
                android.R.layout.simple_list_item_2,
                new String[]{"itemName", "info"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        mCartDataAdapter = new SimpleAdapter(
                this,
                mCartData,
                android.R.layout.simple_list_item_2,
                new String[]{"itemName", "info"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        mActiveList.setAdapter(mListDataAdapter);
        mCartList.setAdapter(mCartDataAdapter);
        mActiveList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                putItemInCart(mShoppingListJSON.items.get(position).UPC);
                return true;
            }
        });
        mCartList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                takeItemFromCart(mItemsInCart.get(position).UPC);
                return true;
            }
        });
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanBarcode();
            }
        });
    }

    private void putItemInCart(String UPC){
        String itemsInCartStr = getSharedPreferences(PreferencesHelper.SHOPPING_CART, MODE_PRIVATE)
                .getString(PreferencesHelper.SHOPPING_CART_ITEMS_IN_CART, null);
        itemsInCartStr = itemsInCartStr.trim();
        String[] itemsInCart = itemsInCartStr.split(",");
        ArrayList<String> newCart = new ArrayList<String>(Arrays.asList(itemsInCart));
        newCart.add(UPC);
        String outStr = "";
        for(String item : newCart){
            outStr += item + ",";
        }
        SharedPreferences.Editor editor = getSharedPreferences(PreferencesHelper.SHOPPING_CART, MODE_PRIVATE).edit();
        editor.putString(PreferencesHelper.SHOPPING_CART_ITEMS_IN_CART, outStr);
        editor.commit();
        String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE)
                .getString(PreferencesHelper.TOKEN, null);
        if(token == null){
            Intent intent = new Intent(this, LoginRegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        new GetListTask(mHouseholdID, mListID, token).execute((Void) null);
    }

    private void takeItemFromCart(String UPC){
        String itemsInCartStr = getSharedPreferences(PreferencesHelper.SHOPPING_CART, MODE_PRIVATE)
                .getString(PreferencesHelper.SHOPPING_CART_ITEMS_IN_CART, null);
        itemsInCartStr = itemsInCartStr.trim();
        String[] itemsInCart = itemsInCartStr.split(",");
        ArrayList<String> newCart = new ArrayList<String>(Arrays.asList(itemsInCart));
        newCart.remove(UPC);
        String outStr = "";
        for(String item : newCart){
            outStr += item + ",";
        }
        SharedPreferences.Editor editor = getSharedPreferences(PreferencesHelper.SHOPPING_CART, MODE_PRIVATE).edit();
        editor.putString(PreferencesHelper.SHOPPING_CART_ITEMS_IN_CART, outStr);
        editor.commit();
        String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE)
                .getString(PreferencesHelper.TOKEN, null);
        if(token == null){
            Intent intent = new Intent(this, LoginRegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        new GetListTask(mHouseholdID, mListID, token).execute((Void) null);
    }

    private void takeItemFromCartNoUIUpdate(String UPC){
        String itemsInCartStr = getSharedPreferences(PreferencesHelper.SHOPPING_CART, MODE_PRIVATE)
                .getString(PreferencesHelper.SHOPPING_CART_ITEMS_IN_CART, null);
        itemsInCartStr = itemsInCartStr.trim();
        String[] itemsInCart = itemsInCartStr.split(",");
        ArrayList<String> newCart = new ArrayList<String>(Arrays.asList(itemsInCart));
        newCart.remove(UPC);
        String outStr = "";
        for(String item : newCart){
            outStr += item + ",";
        }
        SharedPreferences.Editor editor = getSharedPreferences(PreferencesHelper.SHOPPING_CART, MODE_PRIVATE).edit();
        editor.putString(PreferencesHelper.SHOPPING_CART_ITEMS_IN_CART, outStr);
        editor.commit();
    }

    //todo:
    private void saveToInventory(String response){
        GetInventoryResJSON inventory = JSONModels.gson.fromJson(response, GetInventoryResJSON.class);
        List<JSONModels.UpdateInventoryReqJSON.UpdateInventoryItem> inventoryItems = new ArrayList<>();
        List<JSONModels.UpdateInventoryReqJSON.UpdateInventoryItem> listItems = new ArrayList<>();
        String itemsInCartStr = getSharedPreferences(PreferencesHelper.SHOPPING_CART, MODE_PRIVATE)
                .getString(PreferencesHelper.SHOPPING_CART_ITEMS_IN_CART, null);
        itemsInCartStr = itemsInCartStr.trim();
        String[] itemsInCart = itemsInCartStr.split(",");
        ArrayList<String> newCart = new ArrayList<String>(Arrays.asList(itemsInCart));
        for(String newInventoryItem : newCart){
            for(GetShoppingListResJSON.ItemJSON cartItem : mItemsInCart){
                if(cartItem.UPC.equals(newInventoryItem)){
                    inventoryItems.add(new JSONModels.UpdateInventoryReqJSON.UpdateInventoryItem(newInventoryItem, cartItem.quantity, cartItem.fractional));
                    listItems.add(new JSONModels.UpdateInventoryReqJSON.UpdateInventoryItem(newInventoryItem, 0, 0));
                    takeItemFromCartNoUIUpdate(cartItem.UPC);
                    break;
                }
            }
        }
        String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE)
                .getString(PreferencesHelper.TOKEN, null);
        if(token == null){
            Intent intent = new Intent(this, LoginRegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        JSONModels.UpdateInventoryReqJSON update = new JSONModels.UpdateInventoryReqJSON(inventory.version, inventoryItems);
        new UpdateInventoryQuantityTask(mHouseholdID, update, token).execute((Void) null);
        //fix the list now
        JSONModels.UpdateInventoryReqJSON updateJSON = new JSONModels.UpdateInventoryReqJSON(mShoppingListJSON.version, listItems);
        new UpdateListQuantityTask(mHouseholdID, updateJSON, token).execute((Void) null);
    }


    private void scanBarcode(){
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.initiateScan();
    }

    //catch scan barcode action
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(intentResult != null) {
            String barcode = intentResult.getContents();
            //todo: get description task
            putItemInCart(barcode);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_active_list, menu);
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
                    if(NetworkUtility.loginSequence(ActiveListActivity.this) == 1) {
                        mToken = ActiveListActivity.this.getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null);
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
                    if(NetworkUtility.loginSequence(ActiveListActivity.this) == 1) {
                        mToken = ActiveListActivity.this.getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null);
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
                    saveToInventory(request.getResponse());
                    break;
                default:
                    Log.e(LOG_TAG, "Failed to get inventory info: " +
                            request.getResponseCode() + "\nResponse: " + request.getResponse() + "\nAt: " + request.getFilePath());
                    break;
            }
        }
    }


    public class UpdateInventoryQuantityTask extends AsyncTask<Void, Void, Integer>{

        private static final String LOG_TAG = "UpdateInvQtyTask";
        private String mToken;
        private final long mHouseholdID;
        JSONModels.UpdateInventoryReqJSON mUpdate;

        private Request request;


        public UpdateInventoryQuantityTask(long householdID, JSONModels.UpdateInventoryReqJSON update, String token){
            mHouseholdID = householdID;
            mToken = token;
            mUpdate = update;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            //INVENTORY MODE
            request = new Request(
                    NetworkUtility.createUpdateInventoryString(mHouseholdID, mToken),
                    Request.POST,
                    mUpdate
            );
            if(request.openConnection()){
                request.execute();
                if(request.getResponseCode() == 403){
                    //login again
                    if(NetworkUtility.loginSequence(ActiveListActivity.this) == 1) {
                        mToken = ActiveListActivity.this.getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null);
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
                    ActiveListActivity.this.finish();
                    break;
                default:
                    Log.e(LOG_TAG, "Failed to update inventory info: " +
                            result + "\nResponse: " + request.getResponse() + "\nAt: " + request.getFilePath());
                    break;
            }
        }
    }

    public class UpdateListQuantityTask extends AsyncTask<Void, Void, Integer>{

        private static final String LOG_TAG = "UpdateListQtyTask";
        private String mToken;
        private final long mHouseholdID;
        private JSONModels.UpdateInventoryReqJSON mJSON;

        private Request request;


        public UpdateListQuantityTask(long householdID, JSONModels.UpdateInventoryReqJSON json, String token){
            mHouseholdID = householdID;
            mToken = token;
            mJSON = json;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            //INVENTORY MODE
            request = new Request(
                    NetworkUtility.createUpdateShoppingListString(mHouseholdID, mListID, mToken),
                    Request.POST,
                    mJSON
            );
            if(request.openConnection()){
                request.execute();
                if(request.getResponseCode() == 403){
                    //login again
                    if(NetworkUtility.loginSequence(ActiveListActivity.this) == 1) {
                        mToken = ActiveListActivity.this.getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null);
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
