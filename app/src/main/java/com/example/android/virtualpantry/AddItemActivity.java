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

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class AddItemActivity extends ActionBarActivity {

    private static final String LOG_TAG = "AddItemActivity";

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

    private long mHouseholdID;
    private long mListID;

    private AddItemTask mAddItemTask = null;


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
        if(mAddItemTask != null){
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

        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(unitType)){
            mItemUnitType.setError("Cannot be empty");
            focusView = mItemUnitType;
            cancel = true;
        }
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
        if(TextUtils.isEmpty(userDescription)){
            mItemUserDescription.setError("Must have a description");
            focusView = mItemUserDescription;
            cancel = true;
        }
        if(TextUtils.isEmpty(barcodeText)){
            mBarcode.setError("Must have a barcode");
            focusView = mBarcode;
            cancel = true;
        }
        if(cancel){
            focusView.requestFocus();
        } else {
            mAddItemTask = new LinkAndAddTask(barcodeText, userDescription, Integer.parseInt(unitCount), Integer.parseInt(unitFraction), unitType, householdID, version, listID);
            mAddItemTask.execute((Void) null);
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

    private class AddItemTask extends AsyncTask<Void, Void, Integer>{
        public AddItemTask() {
            super();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }
    }
}
