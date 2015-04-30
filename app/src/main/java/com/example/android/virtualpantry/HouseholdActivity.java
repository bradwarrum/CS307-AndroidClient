package com.example.android.virtualpantry;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Database.HouseholdDataSource;
import com.example.android.virtualpantry.Database.ListDataSource;
import com.example.android.virtualpantry.Database.PersistenceRequestCode;
import com.example.android.virtualpantry.Database.PersistenceResponseCode;
import com.example.android.virtualpantry.Database.PreferencesHelper;
import com.example.android.virtualpantry.Data.JSONModels.Household;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class HouseholdActivity extends UserActivity {

    private static final String LOG_TAG = "HouseholdActivity";
    private TextView mHeader;
    private TextView mSubtitle;
    private TextView mMembers;
    private Button mCreateListButton;
    private Button mAddItemButton;
    private ListView mShoppingLists;
    private Household mHousehold = null;
    private int mHouseholdID;
    private Button mViewInventoryButton;

    private SimpleAdapter mListAdapter;
    private List<Map<String, String>> lists;

    private HouseholdDataSource householdDataSource;
    private ListDataSource listDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_household);
        Intent myIntent = getIntent();
        if(myIntent.hasExtra("householdID")){
            mHouseholdID = (int)myIntent.getLongExtra("householdID", -1);
        } else {
            Log.e(LOG_TAG, "Calling intent did not have a household ID");
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        //grab handles
        mHeader = (TextView) findViewById(R.id.HouseholdHeader);
        mSubtitle = (TextView) findViewById(R.id.HouseholdSubtitle);
        mMembers = (TextView) findViewById(R.id.HouseholdMembers);
        mCreateListButton = (Button) findViewById(R.id.CreateNewShoppingListButton);
        mShoppingLists = (ListView) findViewById(R.id.ListviewHousehold);
        mViewInventoryButton = (Button) findViewById(R.id.ViewInventoryButton);
        mAddItemButton = (Button) findViewById(R.id.LinkItemButton);
        //event listeners
        mCreateListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewListDialog();
            }
        });
        ((Button) findViewById(R.id.GoToShoppingCartButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<Long> listIDs = new HashSet<Long>();
                for(Household.HouseholdList list : mHousehold.lists){
                    listIDs.add(list.listID);
                }
                if(listIDs.contains(getSharedPreferences(PreferencesHelper.SHOPPING_CART, MODE_PRIVATE).getLong(PreferencesHelper.SHOPPING_CART_LIST_ID, -1))){
                    Intent intent = new Intent(HouseholdActivity.this, ActiveListActivity.class);
                    startActivity(intent);
                }
            }
        });
        mAddItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HouseholdActivity.this, AddItemActivity.class);
                intent.putExtra("householdID", mHouseholdID);
                startActivity(intent);
            }
        });
        //get information
        householdDataSource = new HouseholdDataSource(this);
        listDataSource = new ListDataSource(this);
        String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE)
                .getString(PreferencesHelper.TOKEN, null);
        if(token == null){
            cancelToLoginPage();
        } else {
            householdDataSource.getHouseholdInfo(mHouseholdID, true, this);
        }
        //mHouseholdTask = new GetHouseholdInfoTask(mHouseholdID, token);
        //mHouseholdTask.execute((Void) null);
    }

    @Override
    public void callback(PersistenceRequestCode request, PersistenceResponseCode status, Object returnValue, Type returnType) {
        super.callback(request, status, returnValue, returnType);
        if(status == PersistenceResponseCode.SUCCESS){
            switch(request){
                case FETCH_HOUSEHOLD:
                    if(returnType == Household.class);
                    updateDisplay((Household) returnValue);
                    break;
                case CREATE_LIST:
                    householdDataSource.getHouseholdInfo(mHouseholdID, false, this);
                    Toast.makeText(this, "List Created", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    private void createNewListDialog(){
        //http://stackoverflow.com/questions/10903754/input-text-dialog-android
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.CreateNewListButtonText));
        //set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        //set up buttons
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String listName = input.getText().toString();
                String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE)
                        .getString(PreferencesHelper.TOKEN, null);
                if(token == null){
                    cancelToLoginPage();
                }
                listDataSource.createShoppingList(mHouseholdID, listName, HouseholdActivity.this);
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

    private void confirmNewActiveList(final int position){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Make new active shopping list");
        final TextView msg = new TextView(this);
        msg.setText("By making this list the active shopping list you will overwrite the current active shopping list");
        alertBuilder.setView(msg);
        alertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = getSharedPreferences(PreferencesHelper.SHOPPING_CART, MODE_PRIVATE).edit();
                editor.putString(PreferencesHelper.SHOPPING_CART_ITEMS_IN_CART, " ");
                editor.putLong(PreferencesHelper.SHOPPING_CART_HOUSEHOLD_ID, mHouseholdID);
                editor.putLong(PreferencesHelper.SHOPPING_CART_LIST_ID, mHousehold.lists.get(position).listID);
                editor.commit();
                Intent intent = new Intent(HouseholdActivity.this, ActiveListActivity.class);
                startActivity(intent);
            }
        });
        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertBuilder.show();
    }

    private void updateDisplay(final Household response){
        mHousehold = response;
        mHeader.setText(mHousehold.householdName);
        mSubtitle.setText(mHousehold.householdDescription);
        lists = new ArrayList<Map<String, String>>();
        for(Household.HouseholdList list : mHousehold.lists){
            Map<String, String> listEntry = new HashMap<>(2);
            listEntry.put("list", list.listName);
            listEntry.put("ID", "ID: " + new Long(list.listID).toString());
            lists.add(listEntry);
        }
        mListAdapter = new SimpleAdapter(
                this,
                lists,
                android.R.layout.simple_list_item_2,
                new String[]{"list", "ID"},
                new int[]{android.R.id.text1, android.R.id.text2});
        mShoppingLists.setAdapter(mListAdapter);
        mShoppingLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Household.HouseholdList list = mHousehold.lists.get(position);
                Intent intent = new Intent(HouseholdActivity.this, ShoppingListActivity.class);
                intent.putExtra("householdID", mHouseholdID);
                intent.putExtra("listID", list.listID);
                startActivity(intent);
            }
        });
        mViewInventoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HouseholdActivity.this, InventoryActivity.class);
                intent.putExtra("householdID", mHouseholdID);
                intent.putExtra("householdName", mHousehold.householdName);
                startActivity(intent);
            }
        });
        mShoppingLists.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                confirmNewActiveList(position);
                return true;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_household, menu);
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
                    if(NetworkUtility.loginSequence(HouseholdActivity.this) == 1) {
                        mToken = HouseholdActivity.this.getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null);
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
                    updateDisplay(request.getResponse());
                    break;
                default:
                    Log.e(LOG_TAG, "Failed to get household info: " +
                            result + "\nResponse: " + request.getResponse() + "\nAt: " + request.getFilePath());
                    break;
            }

        }
    }

    private class CreateListTask extends AsyncTask<Void, Void, Integer>{

        private String mToken;
        private final String mListName;
        private Request request;

        public CreateListTask(String listName, String token) {
            this.mListName = listName;
            this.mToken = token;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            request = new Request(
                    NetworkUtility.createCreateListString(mHouseholdID, mToken),
                    Request.POST,
                    new JSONModels.ListCreateRequest(mListName)
            );
            if(request.openConnection()){
                request.execute();
                if(request.getResponseCode() == 403){
                    //login again
                    if(NetworkUtility.loginSequence(HouseholdActivity.this) == 1) {
                        mToken = HouseholdActivity.this.getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null);
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
            switch(result) {
                case 201:
                    new GetHouseholdInfoTask(mHouseholdID, mToken).execute((Void) null);
                    break;
                default:
                    Log.e(LOG_TAG, "Failed to create list. Response code: " +
                            result + "\nResponse: " + request.getResponse());
                    break;
            }

        }
    }*/
}
