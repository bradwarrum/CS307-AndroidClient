package com.example.android.virtualpantry;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Database.HouseholdDataSource;
import com.example.android.virtualpantry.Database.PersistenceCallback;
import com.example.android.virtualpantry.Database.PersistenceRequestCode;
import com.example.android.virtualpantry.Database.PersistenceResponseCode;
import com.example.android.virtualpantry.Database.PreferencesHelper;
import com.example.android.virtualpantry.Network.NetworkUtility;
import com.example.android.virtualpantry.Network.Request;

import java.lang.reflect.Type;


public class CreateHouseholdActivity extends ActionBarActivity {

    private Button mCreateHouseholButton;
    private AutoCompleteTextView mHouseholdName;
    private AutoCompleteTextView mHouseholdDescription;
    private TextView mStatusText;
    //private CreateHouseholdTask mCreateHouseholdTask = null;

    private PersistenceCallback pcb = new PersistenceCallback() {
        @Override
        public void callback(PersistenceRequestCode requestType, PersistenceResponseCode status, Object returnValue, Type returnType) {
            if (status == PersistenceResponseCode.SUCCESS) {
                householdCreated();
            } else {
                creationFailed();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_household);
        mCreateHouseholButton = (Button) findViewById(R.id.CreateHouseActionButton);
        mHouseholdName = (AutoCompleteTextView) findViewById(R.id.CreateHouseholdNameText);
        mHouseholdDescription = (AutoCompleteTextView) findViewById(R.id.CreateHouseholdDescriptionText);
        mStatusText = (TextView) findViewById(R.id.CreateHouseholdStatusMessage);

        mCreateHouseholButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createHousehold();
            }
        });
    }

    private synchronized void createHousehold(){
        /*if(mCreateHouseholdTask != null){
            return;
        }*/
        mStatusText.setText("Creating new household");
        String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null);
        if(token == null){
            Intent intent = new Intent(this, LoginRegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        /*
        mCreateHouseholdTask = new CreateHouseholdTask(token,
                mHouseholdName.getText().toString(),
                mHouseholdDescription.getText().toString());
        mCreateHouseholdTask.execute((Void) null);*/
        HouseholdDataSource hdatasource = new HouseholdDataSource(this);
        hdatasource.createHousehold(mHouseholdName.getText().toString(), mHouseholdDescription.getText().toString(), pcb);


    }

    private void householdCreated(){
        mStatusText.setText("New household created");
        finish();
    }

    private void creationFailed(){
        mStatusText.setText("Failed to create new household");
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_household, menu);
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
    public class CreateHouseholdTask extends AsyncTask<Void, Void, Integer>{

        private static final String LOG_TAG = "CreateHouseholdTask";
        private String mToken;
        private String mHouseholdName;
        private String mHouseholdDescription;
        private Request request;

        public CreateHouseholdTask(String token, String householdName, String householdDescription) {
            mToken = token;
            mHouseholdName = householdName;
            mHouseholdDescription = householdDescription;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            request = new Request(
                NetworkUtility.createCreateHouseholdString(mToken),
                Request.POST,
                new JSONModels.HouseholdCreateRequest(mHouseholdName, mHouseholdDescription)
            );
            if(request.openConnection()){
                request.executeNoResponse();
                if(request.getResponseCode() == 403)
                    //login again
                    if(NetworkUtility.loginSequence(CreateHouseholdActivity.this) == 1) {
                        mToken = CreateHouseholdActivity.this.getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null);
                        if (mToken != null) {
                            request = new Request(
                                    NetworkUtility.createCreateHouseholdString(mToken),
                                    Request.GET
                            );
                            return doInBackground((Void) null);
                        } else {
                            Log.e(LOG_TAG, "Token was null after re-login");
                            return -1;
                        }
                    } else {
                        Log.e(LOG_TAG, "Unable to log in again");
                        return -1;
                    }
            } else {
                Log.e(LOG_TAG, "Unable to open connection");
                return -1;
            }
            return request.getResponseCode();
        }

        @Override
        protected void onPostExecute(Integer result) {
            mCreateHouseholdTask = null;
            switch(result){
                case 201:
                    householdCreated();
                    break;
                default:
                    Log.e(LOG_TAG, "Failed to create new household: " +
                            result + "\nResponse: " + request.getResponse());
                    creationFailed();
                    break;
            }
        }


    }*/
}
