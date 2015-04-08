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
import com.example.android.virtualpantry.Data.JSONModels.UserInfoResJSON;
import com.example.android.virtualpantry.Data.JSONModels.HouseholdShortJSON;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HouseholdsActivity extends ActionBarActivity {

    private Button mCreateHouseholdButton;
    private Button mViewShoppingCartButton;
    private ListView mHouseholdsList;
    private TextView mSubHeader;

    private SimpleAdapter mHouseholdsAdapter;
    private List<Map<String, String>> households;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_households);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //grab handles
        mCreateHouseholdButton = (Button) findViewById(R.id.CreateHouseholdButton);
        mViewShoppingCartButton = (Button) findViewById(R.id.ViewShoppingCartButton);
        mHouseholdsList = (ListView) findViewById(R.id.HouseholdsList);
        mSubHeader = (TextView) findViewById(R.id.HouseholdsSubHeader);
        households = new ArrayList<Map<String, String>>();

        mCreateHouseholdButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(HouseholdsActivity.this, CreateHouseholdActivity.class);
                startActivity(intent);
            }
        });

        //send
        String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE)
                .getString(PreferencesHelper.TOKEN, null);
        if(token == null){
            Intent intent = new Intent(this, LoginRegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        GetUserInfoTask getUserInfoTask = new GetUserInfoTask(token);
        getUserInfoTask.execute((Void) null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_households, menu);
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

    private void updateDisplay(String response){
        UserInfoResJSON userInfo = JSONModels.gson.fromJson(response, UserInfoResJSON.class);
        for(HouseholdShortJSON household : userInfo.households){
            Map<String, String> householdMap = new HashMap<String, String>(2);
            householdMap.put("name", household.householdName);
            householdMap.put("date", household.householdDescription);
            households.add(householdMap);
        }
        mHouseholdsAdapter = new SimpleAdapter(
                this,
                households,
                android.R.layout.simple_list_item_2,
                new String[]{"name", "description"},
                new int[] {android.R.id.text1, android.R.id.text2});
        mHouseholdsList.setAdapter(mHouseholdsAdapter);
        mSubHeader.setText("" + userInfo.firstName + " " +userInfo.lastName + " households:");
    }

    public class GetUserInfoTask extends AsyncTask<Void, Void, Integer> {

        private static final String LOG_TAG = "GetUserInfoTask";
        private String mToken;
        private Request request;


        public GetUserInfoTask(String token) {
            mToken = token;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            request = new Request(
                    NetworkUtility.createGetUserInfoString(mToken),
                    Request.GET);
            if(request.openConnection()){
                request.execute();
                if(request.getResponseCode() == 403){
                    //login again
                    if(NetworkUtility.loginSequence(HouseholdsActivity.this) == 1) {
                        mToken = HouseholdsActivity.this.getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null);
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

        //todo:
        @Override
        protected void onPostExecute(Integer result) {
            switch(result){
                case 200:
                    updateDisplay(request.getResponse());
                    break;
                default:
                    Log.e(LOG_TAG, "Failed to get user info. Response code: " +
                            result + "\nResponse: " + request.getResponse());
                    break;
            }

        }
    }
}
