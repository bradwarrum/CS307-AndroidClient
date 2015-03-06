package com.example.android.virtualpantry;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.virtualpantry.Data.JSONModels;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.ArrayList;


public class ShoppingListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
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
        private ArrayAdapter<String> mListAdapter;
        private Button mScanCheckoff;



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
            mScanCheckoff = (Button) rootView.findViewById(R.id.scan_checkoff);
            mList = (ListView) rootView.findViewById(R.id.shopping_item_list);
            mListAdapter = new ArrayAdapter<String>(
                    getActivity(),
                    R.layout.basic_list_item,
                    R.id.basic_list_item_textview,
                    new ArrayList<String>()
            );
            mList.setAdapter(mListAdapter);

            return rootView;
        }

        @Override
        public void onResume(){
            super.onResume();
            GetListTask getList = new GetListTask(householdID, listID);
            getList.execute((Void) null);
        }



        public void updateListInfo(final JSONModels.GetShoppingListResJSON listJSON){
            this.listJSON = listJSON;
            mShoppingListTitle.setText(listJSON.name);
            mListDeleteButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DeleteListTask deleteListTask = new DeleteListTask(householdID, listID);
                            deleteListTask.execute((Void) null);
                        }
                    }
            );
            mShoppingListVersion.setText("" + listJSON.version);
            mAddItemButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), AddItemToListActivity.class);
                            intent.putExtra("householdID", householdID);
                            intent.putExtra("listID", listID);
                            intent.putExtra("version", listJSON.version);
                            startActivity(intent);
                        }
                    }
            );
            mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String item = mListAdapter.getItem(position);
                    String UPC = item.split(":")[0];
                    DeleteItemTask deleteItem = new DeleteItemTask(householdID, listID, Long.parseLong(mShoppingListVersion.getText().toString()), UPC, 0, 0);
                    deleteItem.execute((Void) null);
                }
            });
            mScanCheckoff.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v){
                     IntentIntegrator intentIntegrator = new IntentIntegrator(ShoppingListFragment.this);
                     intentIntegrator.initiateScan();
                 }
            });
            ArrayList<String> listItems = new ArrayList<String>();
            String item;
            for(JSONModels.GetShoppingListResJSON.Item itemObj : listJSON.items){
                item = "";
                item = item + itemObj.UPC + ":" + itemObj.description + " - " + itemObj.quantity;
                if(itemObj.fractional != 1){
                    item = item + "/" + itemObj.fractional;
                }
                item = item + " " + itemObj.unitName;
                listItems.add(item);
            }
            mListAdapter.clear();
            mListAdapter.addAll(listItems);

        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent intent) {
            IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if(intentResult != null){
                String barcode = intentResult.getContents();
                DeleteItemTask deleteItem = new DeleteItemTask(householdID, listID, Long.parseLong(mShoppingListVersion.getText().toString()), barcode, 0, 0);
                deleteItem.execute((Void) null);
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Removing Item.", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "No data.", Toast.LENGTH_SHORT);
                toast.show();
            }

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
                    Log.e(LOG_TAG, "Shopping list response was null");
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if(success){
                    updateListInfo(shoppingListJSON);
                }
            }
        }

        public class DeleteListTask extends AsyncTask<Void, Void, Boolean>{

            private long listID;
            private long householdID;

            public DeleteListTask(long householdID, long listID) {
                this.listID = listID;
                this.householdID = householdID;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    return ConnectionManager.removeList(householdID, listID);
                } catch (IOException e){
                    Log.e("deleteListTask", "Failed to delete list", e);
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if(success){
                    getActivity().finish();
                }
            }
        }

        public class DeleteItemTask extends AsyncTask<Void, Void, Boolean>{

            private long householdID;
            private long listID;
            private long version;
            private String UPC;
            private int quantity;
            private int fraction;

            public DeleteItemTask(long householdID, long listID, long version, String UPC, int quanity, int fraction) {
                this.householdID = householdID;
                this.listID = listID;
                this.version = version;
                this.UPC = UPC;
                this.quantity = quanity;
                this.fraction = fraction;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                int rcode = 0;
                try{
                    rcode = ConnectionManager.addItem(householdID, listID, version, UPC, quantity, fraction);
                } catch (IOException e){
                    Log.e("LinkAndAddTask", "Failed to add", e);
                    return false;
                }
                if(rcode != ConnectionManager.OK){
                    Log.e("LinkAndAddTasK", "Got bad return code add: " + rcode);
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if(success){
                    GetListTask getList = new GetListTask(householdID, listID);
                    getList.execute((Void) null);
                }
            }
        }
    }

}
