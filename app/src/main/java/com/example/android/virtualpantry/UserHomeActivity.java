package com.example.android.virtualpantry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.virtualpantry.Data.UserInfo;


public class UserHomeActivity extends Activity {

    private static final String LOG_TAG = "UserHomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new UserHomeActivityContentFragment())
                    .commit();
        }
        GetUserInfoTask getInfoTask = new GetUserInfoTask(this);
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
        startActivity(intent);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class UserHomeActivityContentFragment extends Fragment {

        public UserHomeActivityContentFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_user_home, container, false);
            return rootView;
        }
    }

}
