package com.example.android.virtualpantry;

import android.content.Intent;
import android.os.AsyncTask;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.virtualpantry.Data.HouseholdBrief;
import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Data.UserInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class HouseholdActivity extends Activity {

    private static final String LOG_TAG = "Household Activity";

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



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class HouseholdFragment extends Fragment {

        private static final String LOG_TAG = "Household fragment";
        protected TextView mScreenHeader;
        protected TextView mScreenSubtitle;
        protected TextView mMembersText;
        protected ListView mShoppingListView;
        private ArrayAdapter<String> mShoppingListsAdapter;
        private AutoCompleteTextView mNewShoppingListText;
        private Button mCreateNewListButton;
        private JSONModels.HouseholdJSON householdJSON;
        private String householdStr;
        public static long householdID = -1;

        public HouseholdFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_household, container, false);
            mScreenHeader = (TextView) rootView.findViewById(R.id.Household_screen_header);
            mScreenSubtitle = (TextView) rootView.findViewById(R.id.Household_screen_subtitle);
            mMembersText = (TextView) rootView.findViewById(R.id.Household_members_text);
            mNewShoppingListText = (AutoCompleteTextView) rootView.findViewById(R.id.new_list_name_text);
            mCreateNewListButton = (Button) rootView.findViewById(R.id.create_new_shopping_list_button);
            mCreateNewListButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            createNewList();
                        }
                    });

            //get household ID
            Intent myIntent = getActivity().getIntent();
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

            mShoppingListsAdapter = new ArrayAdapter<String>(
                    getActivity(),
                    R.layout.basic_list_item,
                    R.id.basic_list_item_textview,
                    new ArrayList<String>());
            mShoppingListView = (ListView) rootView.findViewById(R.id.listview_household);
            mShoppingListView.setAdapter(mShoppingListsAdapter);


            GetHouseholdDataTask householdDataTask = new GetHouseholdDataTask(householdID);
            householdDataTask.execute((Void) null);
            return rootView;
        }

        @Override
        public void onResume(){
            super.onResume();
        }

        public void createNewList(){
            String newListName = mNewShoppingListText.getText().toString();
            if(TextUtils.isEmpty(newListName)){
                return;
            }
            HouseholdFragment.CreateListTask createListTask = new CreateListTask(HouseholdFragment.householdID, newListName);
            createListTask.execute((Void) null);
            mNewShoppingListText.setText("");
        }

        public void updateTextFields(JSONModels.HouseholdJSON householdJSON){
            this.householdJSON = householdJSON;
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
                if(householdListJSON.listName == null){
                    Log.e("updateTextFields", "list name is null");
                } else {
                    listNames.add(new String(householdListJSON.listName));
                    Log.v(LOG_TAG, "Adding item to list view: " + householdListJSON.listName);
                }
            }
            mShoppingListsAdapter.clear();
            mShoppingListsAdapter.addAll(listNames);
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


        public class CreateListTask extends AsyncTask<Void, Void, Boolean>{

            private long householdID;
            private String listName;

            public CreateListTask(long householdID, String listName) {
                this.householdID = householdID;
                this.listName = listName;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                int rcode = 0;
                try{
                    rcode = ConnectionManager.createList(listName, householdID);
                } catch(IOException e){
                    Log.e("CreateListTask", "failed to create new list", e);
                }
                if(rcode == ConnectionManager.CREATED){
                    try{
                        JSONModels.UserInfoResJSON newUserInfo = ConnectionManager.getUserInfo();
                        UserInfo.updateUserInfo(newUserInfo);
                    } catch (IOException e){
                        Log.e("CreateListTask", "Failed to update user info");
                    }
                    return true;
                }
                Log.e("CreateListTask", "Failed to properly create list. Response code: " + rcode);
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if(success){
                    GetHouseholdDataTask householdDataTask = new GetHouseholdDataTask(householdID);
                    householdDataTask.execute((Void) null);
                }
            }
        }

    }


}
