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

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Database.PreferencesHelper;
import com.example.android.virtualpantry.Network.NetworkUtility;
import com.example.android.virtualpantry.Network.Request;
import com.example.android.virtualpantry.Data.JSONModels.GetShoppingListResJSON;

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
                putItemInCart(position);
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
    }

    private void putItemInCart(int position){
        String itemsInCartStr = getSharedPreferences(PreferencesHelper.SHOPPING_CART, MODE_PRIVATE)
                .getString(PreferencesHelper.SHOPPING_CART_ITEMS_IN_CART, null);
        itemsInCartStr = itemsInCartStr.trim();
        String[] itemsInCart = itemsInCartStr.split(",");
        ArrayList<String> newCart = new ArrayList<String>(Arrays.asList(itemsInCart));
        newCart.add(mShoppingListJSON.items.get(position).UPC);
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
}
