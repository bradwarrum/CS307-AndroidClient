package com.example.android.virtualpantry;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Database.PreferencesHelper;
import com.example.android.virtualpantry.Network.NetworkUtility;
import com.example.android.virtualpantry.Network.Request;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;


public class AddItemActivity extends ActionBarActivity {

    private static final String LOG_TAG = "AddItemActivity";

    public static final int LIST_MODE = 0;
    public static final int INVENTORY_MODE = 1;

    private Button mSwitchModeButton;
    private LinearLayout mBarcodeSection;
    private EditText mBarcode;
    private Button mScanBarcode;
    private TextView mItemDescriptionText;
    private EditText mItemUserDescription;
    private EditText mItemUnitCount;
    private EditText mItemFraction;
    private EditText mItemUnitType;
    private Button mAddItemButton;

    private EditText mPackageName;
    private EditText mPackageSize;

    private long mHouseholdID;
    private long mListID;
    private long mVersion;
    private int mMode;


    private LinkAndAddItemTask mLinkAddItemTask = null;


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
        mItemUnitCount = (EditText) findViewById(R.id.ItemUnitCount);
        mItemFraction = (EditText) findViewById(R.id.ItemFraction);
        mItemUnitType = (EditText) findViewById(R.id.ItemUnitType);
        mAddItemButton = (Button) findViewById(R.id.PushItemButton);

        mPackageName = (EditText) findViewById(R.id.ItemPackageName);
        mPackageSize = (EditText) findViewById(R.id.ItemPackageSize);


        Intent myIntent = getIntent();
        if(myIntent.hasExtra("householdID")){
            mHouseholdID = myIntent.getLongExtra("householdID", -1);
        } else{
            Log.e(LOG_TAG, "Calling intent did not have a household ID");
        }
        if(myIntent.hasExtra("listID")){
            mListID = myIntent.getLongExtra("listID", -1);
        } else {
            Log.e(LOG_TAG, "Calling intent did not have a household ID");
        }
        if(myIntent.hasExtra("mode")){
            mMode = myIntent.getIntExtra("mode", -1);
        } else {
            Log.e(LOG_TAG, "Calling intent did not have a mode");
        }
        if(myIntent.hasExtra("version")){
            mVersion = myIntent.getLongExtra("version", -1);
        } else {
            Log.e(LOG_TAG, "Calling intent did not have a mode");
        }

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
        Toast toast = Toast.makeText(this.getApplicationContext(), "Item added.", Toast.LENGTH_SHORT);
        toast.show();
        if(mLinkAddItemTask != null){
            return;
        }

        mBarcode.setError(null);
        mItemUserDescription.setError(null);
        mItemUnitCount.setError(null);
        mItemFraction.setError(null);
        mItemUnitType.setError(null);

        String barcodeText = mBarcode.getText().toString();
        String userDescription = mItemUserDescription.getText().toString();
        String unitCount = mItemUnitCount.getText().toString();
        String unitFraction = mItemFraction.getText().toString();
        String unitType = mItemUnitType.getText().toString();
        String packageName = mPackageName.getText().toString();
        String packageSize = mPackageSize.getText().toString();

        if(mBarcodeSection.getVisibility() == View.GONE){
            barcodeText = null;
        }

        boolean cancel = false;
        View focusView = null;

        /*if(TextUtils.isEmpty(unitType)){
            mItemUnitType.setError("Cannot be empty");
            focusView = mItemUnitType;
            cancel = true;
        }*/
        if(TextUtils.isEmpty(unitFraction) || !isInt(unitFraction)){
            mItemFraction.setError("Must be an integer value");
            focusView = mItemFraction;
            cancel = true;
        }
        if(TextUtils.isEmpty(unitCount) || !isInt(unitCount)){
            mItemUnitCount.setError("Must be an integer value");
            focusView = mItemUnitCount;
            cancel = true;
        }
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
                Intent intent = new Intent(this, LoginRegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
           mLinkAddItemTask = new LinkAndAddItemTask(barcodeText, userDescription,
                   packageName, new Float(packageSize),
                   new Integer(unitCount), new Integer(unitFraction),
                   14, mHouseholdID,
                   mVersion, mListID,
                   token, mMode);
            mLinkAddItemTask.execute((Void) null);
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
                        new JSONModels.LinkReqJSON(mDescription, mPackageName,
                                mUnitType, mPackageSize).toString()
                );
            } else {
                request = new Request(
                        NetworkUtility.createLinkNoUPCString(mHouseholdID, mToken),
                        Request.POST,
                        new JSONModels.LinkReqJSON(mDescription, mPackageName,
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
                        mUPC = JSONModels.gson.fromJson(request.getResponse(), JSONModels.CreateUPCResJSON.class).UPC;
                    }
                    if(mMode == LIST_MODE) {
                        List<JSONModels.UpdateListJSON.UpdateListItem> items = new ArrayList<>();
                        items.add(new JSONModels.UpdateListJSON.UpdateListItem(mUPC, mQuantity));
                        JSONModels.UpdateListJSON updateListJSON = new JSONModels.UpdateListJSON(mVersionNo, items);
                        request = new Request(
                                NetworkUtility.createUpdateShoppingListString(mHouseholdID, mListID, mToken),
                                Request.POST,
                                updateListJSON
                        );
                    } else {
                        //INVENTORY MODE
                        List<JSONModels.UpdateInventoryReqJSON.UpdateInventoryItem> items = new ArrayList<>();
                        items.add(new JSONModels.UpdateInventoryReqJSON.UpdateInventoryItem(mUPC, mQuantity, mFraction));
                        JSONModels.UpdateInventoryReqJSON updateJSON = new JSONModels.UpdateInventoryReqJSON(mVersion, items);
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
    }
}
