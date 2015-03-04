package com.example.android.virtualpantry;

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
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Data.UserInfo;

import java.io.IOException;


public class CreateHouseholdActivity extends Activity {

    private TextView mStatusMessage;
    public AutoCompleteTextView mHouseholdName;
    public AutoCompleteTextView mHouseholdDescription;
    public CreateHouseholdTask mCreateTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_household);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new CreateHouseholdFragment())
                    .commit();
        }
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

    public void createHousehold(View view){
        //TODO: this is bad, move them
        mHouseholdName = (AutoCompleteTextView) findViewById(R.id.createHouseholdName);
        mHouseholdDescription = (AutoCompleteTextView) findViewById(R.id.createHouseholdDescription);
        mStatusMessage = (TextView) findViewById(R.id.create_household_status_message);

        //protect against spamming the button
        if(mCreateTask != null){
            return;
        }

        mHouseholdName.setError(null);
        mHouseholdDescription.setError(null);

        String name = mHouseholdName.getText().toString();
        String description = mHouseholdDescription.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(description)){
            mHouseholdDescription.setError("Must have a description");
            focusView = mHouseholdDescription;
            cancel = true;
        }
        if(TextUtils.isEmpty(name)){
            mHouseholdName.setError("Must have a name");
            focusView = mHouseholdName;
            cancel = true;
        }
        if(cancel){
            focusView.requestFocus();
        } else {
            mCreateTask = new CreateHouseholdTask(name, description);
            mCreateTask.execute((Void) null);
        }

    }

    private void updateStatusMessageText(String msg){
        mStatusMessage.setText(msg);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class CreateHouseholdFragment extends Fragment {

        public CreateHouseholdFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_create_household, container, false);
            return rootView;
        }
    }

    public class CreateHouseholdTask extends AsyncTask<Void, Void, Boolean> {

        private static final String LOG_TAG = "createHouseholdTask";
        private String name;
        private String description;
        private int rcode_create;
        private int rcode_userInfo;

        public CreateHouseholdTask(String name, String description) {
            this.name = name;
            this.description = description;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                rcode_create = ConnectionManager.createHousehold(name, description);
            } catch (IOException e){
                Log.e(LOG_TAG, "failed to create household due to network error", e);
                return false;
            }
            if( rcode_create == ConnectionManager.CREATED  ){
                try{
                    JSONModels.UserInfoResJSON newUserInfo = ConnectionManager.getUserInfo();
                    UserInfo.updateUserInfo(newUserInfo);
                } catch (IOException e){
                    Log.e(LOG_TAG, "getting new user info failed", e);
                    return false;
                }
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success){
               updateStatusMessageText("Household Created");
                finish();
            }
            if(rcode_create == ConnectionManager.CREATED){
                //user info failure
                updateStatusMessageText("Failed to get new user info" + ConnectionManager.lastResponseSaved);
            } else {
                //creation failure
                updateStatusMessageText("Failed: Bad data" + ConnectionManager.lastResponseSaved);
            }
        }
    }
}
