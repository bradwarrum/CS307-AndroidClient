package com.example.android.virtualpantry;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.widget.Toast;

import com.example.android.virtualpantry.Database.PersistenceCallback;
import com.example.android.virtualpantry.Database.PersistenceRequestCode;
import com.example.android.virtualpantry.Database.PersistenceResponseCode;

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

}
