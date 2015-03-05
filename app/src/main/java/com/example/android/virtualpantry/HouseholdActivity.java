package com.example.android.virtualpantry;

import android.content.Intent;
import android.os.AsyncTask;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.virtualpantry.Data.HouseholdBrief;
import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Data.UserInfo;

import java.io.IOException;
import java.util.ArrayList;


public class HouseholdActivity extends Activity {

    private static final String LOG_TAG = "Household Activity";
    protected TextView mScreenHeader;
    protected TextView mScreenSubtitle;
    protected TextView mMembersText;
    protected ListView mShoppingListView;
    private ArrayAdapter<String> mShoppingListsAdapter;
    private String householdStr;
    private long householdID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_household);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new HouseholdFragment())
                    .commit();

        }
    }

    @Override
    public void onResume(){
        super.onResume();
        mScreenHeader = (TextView) findViewById(R.id.Household_screen_header);
        mScreenSubtitle = (TextView) findViewById(R.id.Household_screen_subtitle);
        mMembersText = (TextView) findViewById(R.id.Household_members_text);
        if(householdID == -1) {
            Intent myIntent = getIntent();
            if (myIntent.hasExtra("household")) {
                householdStr = myIntent.getStringExtra("household");
            } else {
                Log.e(LOG_TAG, "calling intent did not have a household designated");
            }
            UserInfo userInfo = UserInfo.getUserInfo();
            for (HouseholdBrief householdBriefObj : userInfo.getHouseholdBriefs()) {
                if (householdBriefObj.getHouseholdName().equals(householdStr)) {
                    householdID = householdBriefObj.getHouseholdID();
                }
            }
        }
        GetHouseholdDataTask householdDataTask = new GetHouseholdDataTask(householdID);
        householdDataTask.execute((Void) null);

        mShoppingListsAdapter = new ArrayAdapter<String>(
                this,
                R.layout.basic_list_item,
                R.id.listview_household,
                new ArrayList<String>());
        mShoppingListView = (ListView) findViewById(R.id.listview_household);
        mShoppingListView.setAdapter(mShoppingListsAdapter);
        mShoppingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ShoppingList = mShoppingListsAdapter.getItem(position);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_household, menu);
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

    public void updateTextFields(JSONModels.HouseholdJSON householdJSON){
        mScreenHeader.setText(householdJSON.householdName);
        mScreenSubtitle.setText(householdJSON.householdDescription);
        String members = "";
        for(JSONModels.HouseholdMemberJSON householdMemberJSON : householdJSON.members){
            if(members.equals("")){
                members = members + householdMemberJSON.firstName + " " + householdMemberJSON.lastName;
            } else {
                members = members + "," + householdMemberJSON.firstName + " " + householdMemberJSON.lastName;
            }
        }
        mMembersText.setText(members);
        ArrayList<String> listNames = new ArrayList<String>();
        for(JSONModels.HouseholdListJSON householdListJSON : householdJSON.lists){
            listNames.add(householdJSON.householdName);
        }
        mShoppingListsAdapter = new ArrayAdapter<String>(
            this,
            R.layout.basic_list_item,
            R.id.listview_household,
            listNames);
        mShoppingListView.setAdapter(mShoppingListsAdapter);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class HouseholdFragment extends Fragment {

        private static final String LOG_TAG = "Household fragment";

        public HouseholdFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_household, container, false);

            return rootView;
        }

    }

    public class GetHouseholdDataTask extends AsyncTask<Void, Void, Boolean>{

        private static final String LOG_TAG = "GetHouseholdDataTask";
        private long householdID;
        JSONModels.HouseholdJSON householdJSON;

        public GetHouseholdDataTask(long householdID) {
            this.householdID = householdID;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            int rcode;
            householdJSON = null;
            try{
                householdJSON = ConnectionManager.getHousehold(householdID);
            } catch (IOException e){
                Log.e(LOG_TAG, "Failed to get household JSON", e);
                return false;
            }
            if(householdJSON != null){
                return true;
            }
            Log.e(LOG_TAG, "Got null JSON:");
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success) {
                updateTextFields(householdJSON);
            }
        }
    }
}
