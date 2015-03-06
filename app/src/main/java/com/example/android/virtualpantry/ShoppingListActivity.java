package com.example.android.virtualpantry;

import android.content.Intent;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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
        private JSONModels.GetShoppingListResJSON listJSON = null;
        private long householdID = -1;
        private long listID = -1;

        private Button mListDeleteButton;
        private TextView mShoppingListTitle;
        private TextView mShoppingListVersion;
        private Button mAddItemButton;
        private ListView mList;



        public ShoppingListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_shopping_list, container, false);
            Intent myIntent = getActivity().getIntent();
            if(myIntent.hasExtra("householdID")){
                householdID = myIntent.getLongExtra("householdID", -1);
            }
            if(myIntent.hasExtra("listID")){
                listID = myIntent.getLongExtra("listID", -1);
            }
            if(householdID == -1 || listID == -1){
                Log.e(LOG_TAG, "Error retriving household and list ID");
                getActivity().finish();
            }
            mListDeleteButton = (Button) rootView.findViewById(R.id.delete_list_button);
            mShoppingListTitle = (TextView) rootView.findViewById(R.id.shopping_list_title);
            mShoppingListVersion = (TextView) rootView.findViewById(R.id.shopping_list_version_no);
            mAddItemButton = (Button) rootView.findViewById(R.id.add_item_button);
            mList = (ListView) rootView.findViewById(R.id.shopping_item_list);

            return rootView;
        }

        @Override
        public void onResume(){
            super.onResume();
            GetListTask getList = new GetListTask(householdID, listID);
            getList.execute((Void) null);
        }

        public void updateListInfo(JSONModels.GetShoppingListResJSON listJSON){
            this.listJSON = listJSON;
            mShoppingListTitle.setText(listJSON.name);
            mShoppingListVersion.setText("" + listJSON.version);
        }

        public class GetListTask extends AsyncTask<Void, Void, Boolean>{

            private long listID;
            private long householdID;
            private JSONModels.GetShoppingListResJSON shoppingListJSON;

            public GetListTask(long householdID, long listID) {
                this.householdID = householdID;
                this.listID = listID;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                shoppingListJSON = null;
                try{
                    shoppingListJSON = ConnectionManager.getList(householdID, listID);
                } catch (IOException e){
                    Log.e(LOG_TAG, "Failed to getting shooping list", e);
                }
                if(shoppingListJSON == null){
                    return false;
                }
                Log.e(LOG_TAG, "Shopping list response was null");
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if(success){
                    updateListInfo(shoppingListJSON);
                }
            }
        }
    }

}
