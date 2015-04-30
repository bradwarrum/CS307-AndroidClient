package com.example.android.virtualpantry;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Database.HouseholdDataSource;
import com.example.android.virtualpantry.Database.InventoryDataSource;
import com.example.android.virtualpantry.Database.ListDataSource;
import com.example.android.virtualpantry.Database.PantryDbHelper;
import com.example.android.virtualpantry.Database.PersistenceRequestCode;
import com.example.android.virtualpantry.Database.PersistenceResponseCode;

import com.example.android.virtualpantry.Data.JSONModels.GetInventoryResponse.InventoryItem;
import com.example.android.virtualpantry.Data.JSONModels.Household;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MyItemsActivity extends UserActivity {

    public static final int LIST_MODE = 1;
    public static final int INVENTORY_MODE = 2;

    public static final String LOG_TAG = "MyItemsActivity";

    private int mMode;
    private int mListID;
    private int mHouseholdID;

    private ListView mMyItems;

    private ListDataSource listDataSource;
    private InventoryDataSource invDataSource;
    private HouseholdDataSource householdDataSource;

    private List<InventoryItem> inventoryItems;
    private List<Map<String, String>> mInventoryData;
    private SimpleAdapter mInventoryDataAdapter;
    private Household mHousehold;
    private String listName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_items);
        mMyItems = (ListView) findViewById(R.id.MyItemList);
        Intent myIntent = getIntent();
        if(myIntent.hasExtra("householdID")){
            mHouseholdID = myIntent.getIntExtra("householdID", -1);
        } else{
            Log.e(LOG_TAG, "Calling intent did not have a household ID");
        }
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
        invDataSource = new InventoryDataSource(this);
        listDataSource = new ListDataSource(this);
        householdDataSource = new HouseholdDataSource(this);
        householdDataSource.getHouseholdInfo(mHouseholdID, false, this);
        invDataSource.getInventory(mHouseholdID, true, this);
        if(mMode == LIST_MODE) {
            listDataSource.getListItems(mHouseholdID, mListID, true, this);
        }
    }

    @Override
    public void callback(PersistenceRequestCode request, PersistenceResponseCode status, Object returnValue, Type returnType) {
        super.callback(request, status, returnValue, returnType);
        if(status == PersistenceResponseCode.SUCCESS){
            switch(request){
                case FETCH_LIST:
                    //do nothing: safety feature
                    listName = ((JSONModels.GetShoppingListResponse)returnValue).name;
                    break;
                case FETCH_INVENTORY:
                    inventoryItems = (List<InventoryItem>) returnValue;
                    updateDisplay();
                    break;
                case FETCH_HOUSEHOLD:
                    mHousehold = (Household) returnValue;
                    break;
                case UPDATE_INVENTORY:
                    finish();
                    break;
                case UPDATE_LIST:
                    finish();
                    break;
                default:
                    Toast.makeText(this, "Unknown callback" + request + " result in " + status, Toast.LENGTH_LONG).show();

            }
        }
    }

    private void updateDisplay(){
        mInventoryData = new ArrayList<Map<String, String>>();
        for(InventoryItem item : inventoryItems){
            Map<String, String> inventoryItem = new HashMap<>(2);
            inventoryItem.put("itemName", item.description);
            String subtitle = "";
            subtitle += "UPC:" + item.UPC + " - " + item.packaging.packageSize + " " + item.packaging.unitName + " " + item.packaging.packageName;
            inventoryItem.put("info", subtitle);
            mInventoryData.add(inventoryItem);
        }
        mInventoryDataAdapter = new SimpleAdapter(
                this,
                mInventoryData,
                android.R.layout.simple_list_item_2,
                new String[]{"itemName", "info"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        mMyItems.setAdapter(mInventoryDataAdapter);
        mMyItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                promptAddItem(position);
            }
        });
    }

    private void promptAddItem(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(mMode == LIST_MODE){
            builder.setTitle("Add item to " + listName);
        } else {
            builder.setTitle("Add item to inventory");
        }
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String totalQuantity = input.getText().toString();
                String quantity, fractional;
                if(totalQuantity.contains(".")){
                    quantity = totalQuantity.split(".")[0];
                    fractional = totalQuantity.split(".")[1];
                } else {
                    quantity = totalQuantity;
                    fractional = "0";
                }
                if(mMode == LIST_MODE){
                    List<JSONModels.UpdateListRequest.UpdateListItem> updateItems = new ArrayList<JSONModels.UpdateListRequest.UpdateListItem>();
                    updateItems.add(new JSONModels.UpdateListRequest.UpdateListItem(inventoryItems.get(position).UPC, Integer.valueOf(quantity), Integer.valueOf(fractional)));
                    listDataSource.updateList(mListID, updateItems, MyItemsActivity.this);
                } else {
                    List<JSONModels.UpdateInventoryRequest.UpdateInventoryItem> updateItems = new ArrayList<JSONModels.UpdateInventoryRequest.UpdateInventoryItem>();
                    updateItems.add(new JSONModels.UpdateInventoryRequest.UpdateInventoryItem(inventoryItems.get(position).UPC, Integer.valueOf(quantity), Integer.valueOf(fractional)));
                    invDataSource.updateInventoryQuantity(mHouseholdID, updateItems, MyItemsActivity.this);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_items, menu);
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
}
