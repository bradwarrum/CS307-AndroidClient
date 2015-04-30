package com.example.android.virtualpantry;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.virtualpantry.Database.PersistenceCallback;
import com.example.android.virtualpantry.Database.PersistenceRequestCode;
import com.example.android.virtualpantry.Database.PersistenceResponseCode;
import com.example.android.virtualpantry.Database.PreferencesHelper;
import com.example.android.virtualpantry.Database.VPDatabaseHandler;

import java.lang.reflect.Type;

/**
 * Created by Garrett on 4/26/2015.
 */
public class UserActivity extends ActionBarActivity implements PersistenceCallback{

    protected void cancelToLoginPage(){
        Intent intent = new Intent(this, LoginRegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void callback(PersistenceRequestCode request, PersistenceResponseCode status, Object returnValue, Type returnType) {
        switch(status){
            case SUCCESS:
                break;
            case ERR_CLIENT_CONNECT:
                break;
            case ERR_SERVER_INTERNAL:
                break;
            case ERR_TOKEN_EXPIRED:
                break;
            case ERR_INVALID_PAYLOAD:
                break;
            case ERR_DB_DATA_NOT_FOUND:
                break;
            case ERR_SERVER_MALFORMED_RESPONSE:
                break;
            default:
                Toast.makeText(this, "Error in data access of " + request + " result in " + status, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false); // disable the button
            actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
            actionBar.setDisplayShowHomeEnabled(false); // remove the icon
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.action_logout:
                VPDatabaseHandler.purgeInfo(this);
                SharedPreferences user_info = getSharedPreferences(PreferencesHelper.USER_INFO, MODE_PRIVATE);
                SharedPreferences.Editor editor = user_info.edit();
                editor.putString(PreferencesHelper.USERNAME, PreferencesHelper.NULL_PREFERENCE_VALUE);
                editor.putString(PreferencesHelper.PASSWORD, PreferencesHelper.NULL_PREFERENCE_VALUE);
                editor.putString(PreferencesHelper.TOKEN, PreferencesHelper.TOKEN);
                editor.commit();
                Intent intent = new Intent(this, LoginRegisterActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.action_settings:
                break;

        }

        return super.onOptionsItemSelected(item);
    }

}
