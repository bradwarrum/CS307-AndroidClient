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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.virtualpantry.Data.JSONModels;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class HouseholdListActivity extends Activity {

    JSONModels.UserInfoResJSON userInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_household_list_);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        Intent callerIntent = getIntent();
        userInfo = (JSONModels.UserInfoResJSON) callerIntent.getParcelableExtra("userInfo");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        TextView message = (TextView) findViewById(R.id.household_list_message);
        message.setText("Some Text");
        message.setText(gson.toJson(userInfo));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_household__list_, menu);
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
    public static class PlaceholderFragment extends Fragment {

        private ArrayAdapter<String> mHouseholdAdapater;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            String[] data = {
                    "Household 1",
                    "Household 2",
                    "Household 3"
            };
            List<String> households = new ArrayList<String>(Arrays.asList(data));
            mHouseholdAdapater = new ArrayAdapter<String>(
                    getActivity(),
                    R.layout.list_item_household,
                    R.id.list_item_household_textview,
                    households);

            View rootView = inflater.inflate(R.layout.fragment_household_list, container, false);

            ListView listView = (ListView) rootView.findViewById(R.id.listview_households);
            listView.setAdapter(mHouseholdAdapater);
            return rootView;
        }
    }
}
