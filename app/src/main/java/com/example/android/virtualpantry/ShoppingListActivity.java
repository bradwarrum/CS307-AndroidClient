package com.example.android.virtualpantry;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.virtualpantry.Data.JSONModels;

import java.io.IOException;


public class ShoppingListActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ShoppingListFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shopping_list, menu);
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
    public static class ShoppingListFragment extends Fragment {

        private static final String LOG_TAG = "ShoppingListFragment";

        public ShoppingListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_shopping_list, container, false);
            return rootView;
        }

        public class GetListTask extends AsyncTask<Void, Void, Boolean>{

            private long listID;
            private long householdID;

            public GetListTask(long householdID, long listID) {
                this.householdID = householdID;
                this.listID = listID;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                JSONModels.GetShoppingListResJSON shoppingListJSON = null;
                try{
                    shoppingListJSON = ConnectionManager.getList(householdID, listID);
                } catch (IOException e){
                    Log.e(LOG_TAG, "Failed to getting shooping list", e);
                }
                if(shoppingListJSON == null){
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {

            }
        }
    }

}
