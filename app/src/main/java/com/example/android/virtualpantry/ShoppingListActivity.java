package com.example.android.virtualpantry;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Database.PreferencesHelper;
import com.example.android.virtualpantry.Network.NetworkUtility;
import com.example.android.virtualpantry.Network.Request;
import com.example.android.virtualpantry.Data.JSONModels.GetShoppingListResJSON;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ShoppingListActivity extends ActionBarActivity {

    private static final String LOG_TAG = "ShoppingListActivity";
    private TextView mHeader;
    private TextView mVersion;
    private Button mAddItemButton;
    private ListView mShoppingList;
    private long mListID;
    private long mHouseholdID;

    private GetShoppingListResJSON shoppingListJSON;

    private List<Map<String, String>> mListData;
    private SimpleAdapter mListDataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);
        Intent myIntent = getIntent();
        if(myIntent.hasExtra("householdID")){
            mHouseholdID = myIntent.getLongExtra("householdID", -1);
        } else{
            Log.e(LOG_TAG, "Calling intent did not have a household ID");
        }
        if(myIntent.hasExtra("listID")){
            mListID = myIntent.getLongExtra("listID", -1);
        } else {
            Log.e(LOG_TAG, "Calling intent did not have a household ID");
        }
    }

    @Override
    protected  void onResume(){
        super.onResume();
        mHeader = (TextView) findViewById(R.id.ShoppingListTitle);
        mVersion = (TextView) findViewById(R.id.ShoppingListVersionNo);
        mAddItemButton = (Button) findViewById(R.id.AddItemButton);
        mShoppingList = (ListView) findViewById(R.id.ShoppingItemList);



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
        shoppingListJSON = JSONModels.gson.fromJson(response, GetShoppingListResJSON.class);
        mHeader.setText(shoppingListJSON.name);
        mVersion.setText(new Long(shoppingListJSON.version).toString());
        mListData = new ArrayList<Map<String, String>>();
        for(GetShoppingListResJSON.ItemJSON item : shoppingListJSON.items){
            Map<String, String> listItem = new HashMap<>(2);
            listItem.put("itemName", item.description);
            String subtitle = "";
            subtitle += item.UPC + ": " + item.quantity;
            if(item.fractional != 0){
                subtitle += "/" + item.fractional;
            }
            subtitle += " " + item.unitName;
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
                Intent intent = new Intent(ShoppingListActivity.this, AddItemActivity.class);
                intent.putExtra("householdID", mHouseholdID);
                intent.putExtra("listID", mListID);
                intent.putExtra("mode", AddItemActivity.LIST_MODE);
                intent.putExtra("version", new Long(shoppingListJSON.version).longValue());
                startActivity(intent);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shopping_list, menu);
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
            //Log.d("LOG_TAG", "Response code is: " + request.getResponseCode() + " result is: " + result);
            if(result != -1) {
                switch (request.getResponseCode()) {
                    case 200:
                        updateDisplay(request.getResponse());
                        break;
                    default:
                        Log.e(LOG_TAG, "Failed to get list info 1: " +
                                request.getResponseCode() + "\nResponse: " + request.getResponse() + "\nAt: " + request.getFilePath());
                        break;
                }
            } else{
                Log.e(LOG_TAG, "Failed to get list info 2: " +
                        request.getResponseCode() + "\nResponse: " + request.getResponse() + "\nAt: " + request.getFilePath());
            }
        }
    }
}
