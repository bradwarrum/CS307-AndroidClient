package com.example.android.virtualpantry;

import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.virtualpantry.Data.JSONModels;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.w3c.dom.Text;

import java.io.IOException;
import java.sql.Connection;


public class AddItemToListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item_to_list);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new AddItemToListFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_item_to_list, menu);
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
    public static class AddItemToListFragment extends Fragment {

        private EditText mBarcodeText;
        private Button mScanBarcodeButton;
        private TextView mProductDescription;
        private EditText mUserDescription;
        private EditText mUnitCount;
        private EditText mUnitFraction;
        private EditText mUnitType;
        private Button mAddItem;
        private TextView mItemStatusText;
        private long householdID;
        private long listID;
        private long version;
        private LinkAndAddTask mAddItemTask;

        public AddItemToListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_add_item_to_list, container, false);
            Intent myIntent = getActivity().getIntent();
            householdID = myIntent.getLongExtra("householdID", -1);
            listID = myIntent.getLongExtra("listID", -1);
            version = myIntent.getLongExtra("version", -1);
            if(householdID == -1 || listID == -1 || version == -1){
                Log.e("AddItemToListFrag", "Failed to retrieve intents");
                getActivity().finish();
            }
            mBarcodeText = (EditText) rootView.findViewById(R.id.barcode_edit_text);
            mScanBarcodeButton = (Button) rootView.findViewById(R.id.scan_barcode_button);
            mProductDescription = (TextView) rootView.findViewById(R.id.item_description_text);
            mUserDescription = (EditText) rootView.findViewById(R.id.user_description);
            mUnitCount = (EditText) rootView.findViewById(R.id.item_unit_count);
            mUnitFraction = (EditText) rootView.findViewById(R.id.item_fraction);
            mUnitType = (EditText) rootView.findViewById(R.id.item_unit_type);
            mAddItem = (Button) rootView.findViewById(R.id.push_item_button);
            mItemStatusText = (TextView) rootView.findViewById(R.id.add_item_status_text);
            mScanBarcodeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scanBarcode();
                }
            });
            mAddItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addItem();
                }
            });
            return rootView;
        }

        private void addItem(){
            if(mAddItemTask != null){
                return;
            }

            mBarcodeText.setError(null);
            mUserDescription.setError(null);
            mUnitCount.setError(null);
            mUnitFraction.setError(null);
            mUnitType.setError(null);
            mItemStatusText.setText("");

            String barcodeText = mBarcodeText.getText().toString();
            String userDescription = mUserDescription.getText().toString();
            String unitCount = mUnitCount.getText().toString();
            String unitFraction = mUnitFraction.getText().toString();
            String unitType = mUnitType.getText().toString();

            boolean cancel = false;
            View focusView = null;

            if(TextUtils.isEmpty(unitType)){
                mUnitType.setError("Cannot be empty");
                focusView = mUnitType;
                cancel = true;
            }
            if(TextUtils.isEmpty(unitFraction) || !isInt(unitFraction)){
                mUnitFraction.setError("Must be an integer value");
                focusView = mUnitFraction;
                cancel = true;
            }
            if(TextUtils.isEmpty(unitCount) || !isInt(unitCount)){
                mUnitCount.setError("Must be an integer value");
                focusView = mUnitCount;
                cancel = true;
            }
            if(TextUtils.isEmpty(userDescription)){
                mUserDescription.setError("Must have a description");
                focusView = mUserDescription;
                cancel = true;
            }
            if(TextUtils.isEmpty(barcodeText)){
                mBarcodeText.setError("Must have a barcode");
                focusView = mUserDescription;
                cancel = true;
            }
            if(cancel){
                focusView.requestFocus();
            } else {
                mAddItemTask = new LinkAndAddTask(barcodeText, userDescription, Integer.parseInt(unitCount), Integer.parseInt(unitFraction), unitType, householdID, version, listID);
                mAddItemTask.execute((Void) null);
            }
        }

        public void fillDescriptionCategory(JSONModels.GetDescriptionResJSON json){
            mProductDescription.setVisibility(View.VISIBLE);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            mProductDescription.setText(gson.toJson(json).toString());
        }

        public void itemAdded(){
            getActivity().finish();
        }

        public void errorAddingItem(){
            mItemStatusText.setVisibility(View.VISIBLE);
            mItemStatusText.setText("Failed to add item");
        }

        private boolean isInt(String str){
            try{
                Integer.parseInt(str);
                return true;
            } catch (NumberFormatException e){
                return false;
            }
        }

        private void scanBarcode(){
            IntentIntegrator intentIntegrator = new IntentIntegrator(this);
            intentIntegrator.initiateScan();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent intent) {
            IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if(intentResult != null){
                String barcode = intentResult.getContents();
                mBarcodeText.setText(barcode);
                GetDescriptionTask descriptionTask = new GetDescriptionTask(householdID, barcode);
                descriptionTask.execute((Void) null);
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Getting item data.", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                mItemStatusText.setText("Barcode scan failed");
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "No data.", Toast.LENGTH_SHORT);
                toast.show();
            }

        }

        public class GetDescriptionTask extends AsyncTask<Void, Void, Boolean>{

            private long householdID;
            private String UPC;
            private JSONModels.GetDescriptionResJSON descriptionResJSON;

            public GetDescriptionTask(long householdID, String UPC) {
                this.householdID = householdID;
                this.UPC = UPC;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                descriptionResJSON = null;
                try{
                    descriptionResJSON = ConnectionManager.getDescriptions(householdID, UPC);
                } catch (IOException e){
                    Log.e("GetDescriptionTask", "Error getting descriptions", e);
                    return false;
                }
                if(descriptionResJSON == null){
                    Log.e("GetDescriptionTasl", "Failed to properly parse JSON");
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                fillDescriptionCategory(descriptionResJSON);
            }
        }

        public class LinkAndAddTask extends AsyncTask<Void, Void, Boolean>{

            private String UPC;
            private String description;
            private int quantity;
            private int fraction;
            private String unitType;
            private long householdID;
            private long version;
            private long listID;

            public LinkAndAddTask(String UPC, String description, int quantity, int fraction, String unitType, long householdID, long version, long listID) {
                this.UPC = UPC;
                this.description = description;
                this.quantity = quantity;
                this.fraction = fraction;
                this.unitType = unitType;
                this.householdID = householdID;
                this.version = version;
                this.listID = listID;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                int rcode = 0;
                try{
                    rcode = ConnectionManager.linkItem(householdID, UPC, description, unitType);
                } catch (IOException e){
                    Log.e("LinkAndAddTask", "Failed to link", e);
                    return false;
                }
                if(rcode != ConnectionManager.OK){
                    Log.e("LinkAndAddTasK", "Got bad return code link: " + rcode);
                    return false;
                }
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
                    itemAdded();
                }
                else {
                    errorAddingItem();
                }
            }
        }
    }
}
