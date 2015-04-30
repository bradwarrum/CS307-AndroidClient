package com.example.android.virtualpantry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.android.virtualpantry.Database.VPDatabaseHandler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HouseholdsActivity extends UserActivity implements PersistenceCallback{

    private Button mCreateHouseholdButton;
    private Button mLogoutButton;
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
        mLogoutButton = (Button) findViewById(R.id.HouseholdsLogoutButton);
        //event listeners
        mCreateHouseholdButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(HouseholdsActivity.this, CreateHouseholdActivity.class);
                startActivity(intent);
            }
        });
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VPDatabaseHandler.purgeInfo(HouseholdsActivity.this);
                SharedPreferences user_info = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE);
                SharedPreferences.Editor editor = user_info.edit();
                editor.putString(PreferencesHelper.USERNAME, PreferencesHelper.NULL_PREFERENCE_VALUE);
                editor.putString(PreferencesHelper.PASSWORD, PreferencesHelper.NULL_PREFERENCE_VALUE);
                editor.putString(PreferencesHelper.TOKEN, PreferencesHelper.TOKEN);
                editor.commit();
                Intent intent = new Intent(HouseholdsActivity.this, LoginRegisterActivity.class);
                startActivity(intent);
                finish();
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
        switch(id){
            case R.id.add_household_action:
                Intent intent = new Intent(HouseholdsActivity.this, CreateHouseholdActivity.class);
                startActivity(intent);
            break;
            case R.id.action_settings:
            break;

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
}
