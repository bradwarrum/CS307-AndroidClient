package com.example.android.virtualpantry;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Database.PreferencesHelper;
import com.example.android.virtualpantry.Network.NetworkUtility;
import com.example.android.virtualpantry.Network.Request;


public class HouseholdsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_households);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new HouseholdsFragment())
                    .commit();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class HouseholdsFragment extends Fragment {

        public HouseholdsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_households, container, false);

            return rootView;
        }

        private class GetUserInfoTask extends AsyncTask<Void, Void, Integer>{

            private static final String LOG_TAG = "GetUserInfoTask";
            private String mToken;
            private Request request;


            public GetUserInfoTask(String token) {
                mToken = token;
            }

            //todo: finish
            @Override
            protected Integer doInBackground(Void... params) {
                request = new Request(
                        NetworkUtility.createGetUserInfoString(mToken),
                        Request.GET);
                if(request.openConnection()){
                    request.execute();
                    if(request.getResponseCode() == 403){
                        //login again
                        if(NetworkUtility.loginSequence(getActivity()) == 1) {
                            mToken = getActivity().getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null);
                            if(mToken != null) {
                                request = new Request(
                                        NetworkUtility.createGetUserInfoString(mToken),
                                        Request.GET);
                            } else {
                                Log.e(LOG_TAG, "Token was null after re-login");
                                return -1;
                            }
                        } else {
                            Log.e(LOG_TAG, "Unable to log in again");
                            return -1;
                        }
                    }
                }
            }

            //todo:
            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
            }
        }
    }
}
