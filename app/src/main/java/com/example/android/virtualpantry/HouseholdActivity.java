package com.example.android.virtualpantry;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;


public class HouseholdActivity extends ActionBarActivity {

    private TextView mHeader;
    private TextView mSubtitle;
    private TextView mMembers;
    private Button mCreateListButton;
    private ListView mShoppingLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_household);
    }

    @Override
    protected void onResume(){
        super.onResume();
        mHeader = (TextView) findViewById(R.id.HouseholdHeader);
        mSubtitle = (TextView) findViewById(R.id.HouseholdSubtitle);
        mMembers = (TextView) findViewById(R.id.HouseholdMembers);
        mCreateListButton = (Button) findViewById(R.id.CreateNewShoppingListButton);
        mShoppingLists = (ListView) findViewById(R.id.ListviewHousehold);

        mCreateListButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                createNewListDialog();
            }
        });
    }

    private void createNewListDialog(){
        //http://stackoverflow.com/questions/10903754/input-text-dialog-android
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.CreateNewListButtonText));

        //set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        //set up buttons
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createNewList(input.getText().toString());
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

    private void createNewList(String listName){

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_household, menu);
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

    private class GetHouseholdInfoTask extends AsyncTask<Void, Void, Integer>{

        private final int mHouseholdID;
        private String mToken;

        public GetHouseholdInfoTask(int householdID, String token) {
            mHouseholdID = householdID;
            mToken = token;
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
