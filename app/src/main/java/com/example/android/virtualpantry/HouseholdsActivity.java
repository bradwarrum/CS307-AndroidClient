package com.example.android.virtualpantry;

import android.content.Intent;
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
import com.example.android.virtualpantry.Database.HouseholdDataSource;
import com.example.android.virtualpantry.Database.PersistenceCallback;
import com.example.android.virtualpantry.Database.PersistenceRequestCode;
import com.example.android.virtualpantry.Database.PersistenceResponseCode;
import com.example.android.virtualpantry.Database.PreferencesHelper;
import com.example.android.virtualpantry.Network.NetworkUtility;
import com.example.android.virtualpantry.Network.Request;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HouseholdsActivity extends UserActivity implements PersistenceCallback{

    private Button mCreateHouseholdButton;
    private ListView mHouseholdsList;
    private TextView mSubHeader;

    private SimpleAdapter mHouseholdsAdapter;
    private List<Map<String, String>> households;
    private HouseholdDataSource householdDataSource;

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
        mHouseholdsList = (ListView) findViewById(R.id.HouseholdsList);
        mSubHeader = (TextView) findViewById(R.id.HouseholdsSubHeader);

        //event listeners
        mCreateHouseholdButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(HouseholdsActivity.this, CreateHouseholdActivity.class);
                startActivity(intent);
            }
        });

        //data holding
        households = new ArrayList<Map<String, String>>();

        //database handler
        householdDataSource = new HouseholdDataSource(this);

        //send
        String token = PreferencesHelper.getToken(this);
        if(token == null){
            cancelToLoginPage();
        } else {
            householdDataSource.getUserInformation(true, this);
        }


        //GetUserInfoTask getUserInfoTask = new GetUserInfoTask(token);
        //getUserInfoTask.execute((Void) null);
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

    @Override
    public void callback(PersistenceRequestCode request, PersistenceResponseCode status, Object returnValue, Type returnType) {
        super.callback(request, status, returnValue, returnType);
        if(status == PersistenceResponseCode.SUCCESS){
            switch(request){
                case FETCH_USER_INFORMATION:
                    if(returnType == JSONModels.UserInfoResponse.class){
                        updateDisplay((JSONModels.UserInfoResponse)returnValue);
                    }
                    break;
                default:
                    Toast.makeText(this, "Unsuported request:" + request, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateDisplay(final JSONModels.UserInfoResponse userInfo){
        for(JSONModels.UserInfoResponse.Household household : userInfo.households){
            Map<String, String> householdMap = new HashMap<String, String>(2);
            householdMap.put("name", household.householdName);
            householdMap.put("description", household.householdDescription);
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
        mHouseholdsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONModels.UserInfoResponse.Household household = userInfo.households.get(position);
                Intent intent = new Intent(HouseholdsActivity.this, HouseholdActivity.class);
                intent.putExtra("householdID", household.householdID);
                startActivity(intent);
            }
        });
    }

    /*
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
    }*/
}
