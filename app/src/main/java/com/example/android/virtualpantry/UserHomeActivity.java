package com.example.android.virtualpantry;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.virtualpantry.Data.JSONModels;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;


public class UserHomeActivity extends Activity {

    private static final String LOG_TAG = "UserHomeActivity";
    private JSONModels.UserInfoResJSON userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        GetUserInfoTask getInfoTask = new GetUserInfoTask();
        getInfoTask.execute((Void) null);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_home, menu);
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

    public void goToHouseholdsListScreen(View view){
        Intent intent = new Intent(this, HouseholdListActivity.class);
        intent.putExtra("userInfo", userInfo);
        startActivity(intent);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_user_home, container, false);
            return rootView;
        }
    }

    public class GetUserInfoTask extends AsyncTask<Void, Void, Boolean> {

        private static final String LOG_TAG = "GetUserInfoTask";
        private JSONModels.UserInfoResJSON userInfo = null;

        public GetUserInfoTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try{
                userInfo = ConnectionManager.getUserInfo();
            } catch (IOException e){
                Log.e(LOG_TAG, "Failed to get user info", e);
                return false;
            }
            return true;

        }

        @Override
        protected void onPostExecute(Boolean sucess) {
            UserHomeActivity.this.userInfo = userInfo;
           // TextView userHomeMessage = (TextView) findViewById(R.id.user_home_text_field);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Log.v(LOG_TAG, "succefully loaded userInfo:\n" + gson.toJson(userInfo));
            //userHomeMessage.setText(gson.toJson(userInfo));
        }
    }
}
