package com.example.android.virtualpantry;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Data.MeasurementUnit;
import com.example.android.virtualpantry.Database.InventoryDataSource;
import com.example.android.virtualpantry.Database.ListDataSource;
import com.example.android.virtualpantry.Database.PersistenceRequestCode;
import com.example.android.virtualpantry.Database.PersistenceResponseCode;
import com.example.android.virtualpantry.Database.PreferencesHelper;
import com.example.android.virtualpantry.Database.UnitTypes;
import com.example.android.virtualpantry.Network.NetworkUtility;
import com.example.android.virtualpantry.Network.Request;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import com.example.android.virtualpantry.Data.JSONModels.LinkRequest;
import com.example.android.virtualpantry.Data.JSONModels.UpdateListRequest.UpdateListItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class AddItemActivity extends UserActivity {

    private static final String LOG_TAG = "AddItemActivity";

    public static final int LIST_MODE = 0;
    public static final int INVENTORY_MODE = 1;

    private Button mSwitchModeButton;
    private LinearLayout mBarcodeSection;
    private EditText mBarcode;
    private Button mScanBarcode;
    private TextView mItemDescriptionText;
    private EditText mItemUserDescription;
    private Spinner mItemUnitType;
    private Button mAddItemButton;
    private TextView mPackagePreview;

    private EditText mPackageName;
    private EditText mPackageSize;

    private int mHouseholdID;
    private int mListID;
    private int mVersion;
    private int mMode;

    private ListDataSource listDataSource;
    private InventoryDataSource invDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        mSwitchModeButton = (Button) findViewById(R.id.SwitchBarcodeModeButton);
        mBarcodeSection = (LinearLayout) findViewById(R.id.AddItemBarcodeSection);
        mBarcode = (EditText) findViewById(R.id.BarcodeEditText);
        mScanBarcode = (Button) findViewById(R.id.ScanBarcodeButton);
        mItemDescriptionText = (TextView) findViewById(R.id.ItemDescriptionText);
        mItemUserDescription = (EditText) findViewById(R.id.UserItemDescription);
        mItemUnitType = (Spinner) findViewById(R.id.ItemUnitType);
        mAddItemButton = (Button) findViewById(R.id.PushItemButton);
        mPackagePreview = (TextView) findViewById(R.id.PackagingPreview);

        mPackageName = (EditText) findViewById(R.id.ItemPackageName);
        mPackageSize = (EditText) findViewById(R.id.ItemPackageSize);

        listDataSource = new ListDataSource(this);
        invDataSource = new InventoryDataSource(this);

        Intent myIntent = getIntent();
        if(myIntent.hasExtra("householdID")){
            mHouseholdID = myIntent.getIntExtra("householdID", -1);
        } else{
            Log.e(LOG_TAG, "Calling intent did not have a household ID");
        }/*
        if(myIntent.hasExtra("listID")){
            mListID = myIntent.getIntExtra("listID", -1);
        } else {
            Log.e(LOG_TAG, "Calling intent did not have a household ID");
        }
        if(myIntent.hasExtra("mode")){
            mMode = myIntent.getIntExtra("mode", -1);
        } else {
            Log.e(LOG_TAG, "Calling intent did not have a mode");
        }
        if(myIntent.hasExtra("version")){
            mVersion = (int)myIntent.getLongExtra("version", -1);
        } else {
            Log.e(LOG_TAG, "Calling intent did not have a mode");
        }*/

        //populate spinner
        mItemUnitType.setAdapter(new ArrayAdapter<MeasurementUnit>(this, android.R.layout.simple_spinner_item, MeasurementUnit.values()));

        //listeners
        mSwitchModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchBarcodeMode();
            }
        });
        mScanBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanBarcode();
            }
        });
        mAddItemButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                addItem();
            }
        });
        mItemUserDescription.addTextChangedListener(twatch);
        mPackageName.addTextChangedListener(twatch);
        mPackageSize.addTextChangedListener(twatch);
        //mItemUnitCount.addTextChangedListener(twatch);
        mItemUnitType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePackagePreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private TextWatcher twatch = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updatePackagePreview();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void updatePackagePreview() {
        String preview = "";
        String temp;
        if (!(temp = mPackageSize.getText().toString()).equals("")) {
            preview = preview + temp + " ";
        } else {
            mPackagePreview.setText("");
            return;
        }
        UnitTypes utype = UnitTypes.fromID(mItemUnitType.getSelectedItemPosition() + 1);
        preview += utype.getUnitAbbrev() + " ";
        if (!(temp = mPackageName.getText().toString()).equals("")) {
            preview = preview + temp + " of ";
        } else {
            mPackagePreview.setText("");
            return;
        }
        if (!(temp = mItemUserDescription.getText().toString()).equals("")) {
            preview = preview + temp;
        } else {
            mPackagePreview.setText("");
            return;
        }
        mPackagePreview.setText(preview);
    }
    private void switchBarcodeMode() {
        if (mBarcodeSection.getVisibility() == View.VISIBLE) {
            mBarcodeSection.setVisibility(View.GONE);
            mSwitchModeButton.setText(getString(R.string.SwitchBarcodeModeButtonText_ToNoBarcode));
        } else if (mBarcodeSection.getVisibility() == View.GONE) {
            mBarcodeSection.setVisibility(View.VISIBLE);
            mSwitchModeButton.setText(getString(R.string.SwitchBarcodeModeButtonText_ToBarcode));
        }
    }

    private void scanBarcode(){
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.initiateScan();
    }

    //catch scan barcode action
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(intentResult != null){
            String barcode = intentResult.getContents();
            mBarcode.setText(barcode);
            //todo: get description task
            Toast toast = Toast.makeText(this.getApplicationContext(), "Getting item data.", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this.getApplicationContext(), "No data.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    private void addItem(){
        mBarcode.setError(null);
        mItemUserDescription.setError(null);


        String barcodeText = mBarcode.getText().toString();
        String userDescription = mItemUserDescription.getText().toString();
        int unitType = mItemUnitType.getSelectedItemPosition() + 1;
        String packageName = mPackageName.getText().toString();
        String packageSize = mPackageSize.getText().toString();


        if(mBarcodeSection.getVisibility() == View.GONE){
            barcodeText = null;
        }

        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(packageSize) | !isFloat(packageSize)){
            mPackageSize.setError("Must be float value");
            focusView = mPackageSize;
            cancel = true;
        }
        if(TextUtils.isEmpty(packageName)){
            mPackageName.setError("Must have a name");
            focusView = mPackageName;
            cancel = true;
        }
        if(TextUtils.isEmpty(userDescription)){
            mItemUserDescription.setError("Must have a description");
            focusView = mItemUserDescription;
            cancel = true;
        }
        if(mBarcodeSection.getVisibility() == View.VISIBLE && TextUtils.isEmpty(barcodeText)){
            mBarcode.setError("Must have a barcode");
            focusView = mBarcode;
            cancel = true;
        }
        if(cancel){
            focusView.requestFocus();
        } else {
            String token = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE)
                    .getString(PreferencesHelper.TOKEN, null);
            if(token == null){
                cancelToLoginPage();
            }
            Toast.makeText(this, "Adding item", Toast.LENGTH_SHORT).show();
            //UpdateListItem listItem = new UpdateListItem(barcodeText, Integer.valueOf(quantity), Integer.valueOf(fractional));
            //LinkRequest linkRequest = new LinkRequest(userDescription, packageName, unitType, Integer.valueOf(packageSize), mVersion);
            if(barcodeText == null){
                invDataSource.generateUPC(mHouseholdID, userDescription, packageName, unitType, Integer.valueOf(packageSize), this);
            } else {
                invDataSource.linkUPC(mHouseholdID, barcodeText, userDescription, packageName, unitType, Integer.valueOf(packageSize), this);
            }
            /*linkTask = new LinkTask(barcodeText, linkRequest, listItem, token, mMode);
            linkTask.execute((Void) null);*/
            /*mLinkAddItemTask = new LinkAndAddItemTask(barcodeText, userDescription,
                   packageName, new Float(packageSize),
                   new Integer(unitCount), new Integer(unitFraction),
                   14, mHouseholdID,
                   mVersion, mListID,
                   token, mMode);
                mLinkAddItemTask.execute((Void) null);*/
        }
    }

    @Override
    public void callback(PersistenceRequestCode request, PersistenceResponseCode status, Object returnValue, Type returnType) {
        super.callback(request, status, returnValue, returnType);
        if(status == PersistenceResponseCode.SUCCESS) {
            itemAdded();
        } else {
            Toast.makeText(this, "Error in data access of " + request + " result in " + status, Toast.LENGTH_LONG).show();
        }
    }

    private boolean isInt(String str){
        try{
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }

    private boolean isFloat(String flt){
        try{
            Float.parseFloat(flt);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }

    private void itemAdded(){
        Toast toast = Toast.makeText(this.getApplicationContext(), "Item added.", Toast.LENGTH_SHORT);
        toast.show();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_item, menu);
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

    private class GetItemDescriptionTask extends AsyncTask<Void, Void, Integer>{
        public GetItemDescriptionTask() {

        }

        @Override
        protected Integer doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {

        }
    }

    /*
    private class LinkTask extends  AsyncTask<Void, Void, Integer>{

        private LinkRequest mLinkRequest;
        private String mToken;
        private String mUPC;
        private Request request;
        private int mMode;
        private UpdateListItem mListItem;

        public LinkTask(String UPC, LinkRequest linkRequest, UpdateListItem listItem, String token, int mode) {
            mLinkRequest = linkRequest;
            mUPC = UPC;
            mToken = token;
            mMode = mode;
            mListItem = listItem;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Log.e(LOG_TAG, "Starting background");
            if (mUPC != null) {
                request = new Request(
                        NetworkUtility.createLinkUPCString(mHouseholdID, mUPC, mToken),
                        Request.POST,
                        mLinkRequest.toString()
                );
            } else {
                request = new Request(
                        NetworkUtility.createLinkNoUPCString(mHouseholdID, mToken),
                        Request.POST,
                        mLinkRequest.toString()
                );
            }
            if(request.openConnection()){
                request.execute();
                if(request.getResponseCode() <400 && request.getResponseCode() >= 200) {
                    if (mUPC == null) {
                        mUPC = JSONModels.gson.fromJson(request.getResponse(), JSONModels.CreateUPCResponse.class).UPC;
                    }
                    if (mMode == LIST_MODE) {
                        List<UpdateListItem> updateRequestList = new ArrayList<>();
                        updateRequestList.add(mListItem);
                        listDataSource.updateList(mListID, updateRequestList, AddItemActivity.this);
                    } else {
                        //INVENTORY MODE
                        List<JSONModels.UpdateInventoryRequest.UpdateInventoryItem> items = new ArrayList<>();
                        items.add(new JSONModels.UpdateInventoryRequest.UpdateInventoryItem(mUPC, mListItem.quantity, mListItem.fractional));
                        JSONModels.UpdateInventoryRequest updateJSON = new JSONModels.UpdateInventoryRequest(mVersion, items);
                        request = new Request(
                                NetworkUtility.createUpdateInventoryString(mHouseholdID, mToken),
                                Request.POST,
                                updateJSON
                        );
                        if(request.openConnection()){
                            request.execute();
                            return request.getResponseCode();
                        } else {
                            Log.e(LOG_TAG, "Unable to open connection");
                            return -1;
                        }
                    }
                }else{
                    Log.e(LOG_TAG, "Destination: " + request.getFilePath());
                    Log.e(LOG_TAG, "Request: " + request.getSendJSON());
                    Log.e(LOG_TAG, "Response: " + request.getResponse());
                    return request.getResponseCode();
                }
            }
            return -1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            linkTask = null;
            Log.e(LOG_TAG, "Request finished");
            if(result == -2){
                Toast.makeText(AddItemActivity.this, "Error in Linking item: " + result, Toast.LENGTH_SHORT).show();
            } else if(result == -1){
                Toast.makeText(AddItemActivity.this, "Error in adding item", Toast.LENGTH_SHORT).show();
            } else if (result == 200){
                itemAdded();
            } else {
                Toast.makeText(AddItemActivity.this, "Unknown return code: " + result, Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    /*
    private class LinkAndAddItemTask extends AsyncTask<Void, Void, Integer>{

        private static final String LOG_TAG = "LinkAndAddItemTask";

        private String mUPC;
        private final String mDescription;
        private final String mPackageName;
        private final float mPackageSize;
        private final int mQuantity;
        private final int mFraction;
        private final int mUnitType;

        private final long mHouseholdID;
        private final long mVersionNo;
        private final long mListID;
        private String mToken;

        private final int mMode;

        private Request request;

        public LinkAndAddItemTask(String UPC, String description,
                                  String packageName, float packageSize,
                                  int quantity, int fraction,
                                  int unitType, long householdID,
                                  long version, long listID, String token,
                                  int mode) {
            mUPC = UPC;
            mDescription = description;
            mPackageName = packageName;
            mPackageSize = packageSize;
            mQuantity = quantity;
            mFraction = fraction;
            mUnitType = unitType;
            mHouseholdID = householdID;
            mVersionNo = version;
            mListID = listID;
            mToken = token;
            mMode = mode;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if (mUPC != null) {
                request = new Request(
                        NetworkUtility.createLinkUPCString(mHouseholdID, mUPC, mToken),
                        Request.POST,
                        new JSONModels.LinkRequest(mDescription, mPackageName,
                                mUnitType, mPackageSize).toString()
                );
            } else {
                request = new Request(
                        NetworkUtility.createLinkNoUPCString(mHouseholdID, mToken),
                        Request.POST,
                        new JSONModels.LinkRequest(mDescription, mPackageName,
                                mUnitType, mPackageSize).toString()
                );
            }
            if(request.openConnection()){
                request.execute();
                if(request.getResponseCode() == 403){
                    //login again
                    if(NetworkUtility.loginSequence(AddItemActivity.this) == 1) {
                        mToken = AddItemActivity.this.getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE).getString(PreferencesHelper.TOKEN, null);
                        if(mToken != null) {
                            request = new Request(
                                    NetworkUtility.createGetUserInfoString(mToken),
                                    Request.GET);
                            return doInBackground((Void) null);
                        } else {
                            Log.e(LOG_TAG, "Token was null after re-login");
                            return -1;
                        }
                    } else {
                        Log.e(LOG_TAG, "Unable to log in again");
                        return -1;
                    }
                } else if (request.getResponseCode() == 200){
                    //push item to list
                    if(mUPC == null){
                        mUPC = JSONModels.gson.fromJson(request.getResponse(), JSONModels.CreateUPCResponse.class).UPC;
                    }
                    if(mMode == LIST_MODE) {
                        List<JSONModels.UpdateListRequest.UpdateListItem> items = new ArrayList<>();
                        items.add(new JSONModels.UpdateListRequest.UpdateListItem(mUPC, mQuantity, mFraction));
                        JSONModels.UpdateListRequest updateListRequest = new JSONModels.UpdateListRequest(mVersionNo, items);
                        request = new Request(
                                NetworkUtility.createUpdateShoppingListString(mHouseholdID, mListID, mToken),
                                Request.POST,
                                updateListRequest
                        );
                    } else {
                        //INVENTORY MODE
                        List<JSONModels.UpdateInventoryRequest.UpdateInventoryItem> items = new ArrayList<>();
                        items.add(new JSONModels.UpdateInventoryRequest.UpdateInventoryItem(mUPC, mQuantity, mFraction));
                        JSONModels.UpdateInventoryRequest updateJSON = new JSONModels.UpdateInventoryRequest(mVersion, items);
                        request = new Request(
                                NetworkUtility.createUpdateInventoryString(mHouseholdID, mToken),
                                Request.POST,
                                updateJSON
                        );
                    }
                    if(request.openConnection()){
                        request.execute();
                        return request.getResponseCode();
                    } else {
                        Log.e(LOG_TAG, "Unable to open connection");
                        return -1;
                    }
                } else {
                    return request.getResponseCode();
                }
            } else {
                Log.e(LOG_TAG, "Unable to open connection");
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            switch(responseCode){
                case 200:
                    itemAdded();
                    break;
                default:
                    Log.e(LOG_TAG, "Failed to create item. Response code: " +
                            responseCode + "\nResponse: " + request.getResponse()
                    + "at: " + request.getFilePath() + "\nsent: " + request.getMessage());
                    break;
            }
        }
    }*/
}
