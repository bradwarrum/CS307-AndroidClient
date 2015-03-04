package com.example.android.virtualpantry;

import android.os.AsyncTask;
import android.util.Log;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Data.UserInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

/**
* Created by Garrett on 3/4/2015.
*/
public class GetUserInfoTask extends AsyncTask<Void, Void, Boolean> {

    private static final String LOG_TAG = "GetUserInfoTask";
    private UserHomeActivity userHomeActivity;
    private JSONModels.UserInfoResJSON userInfo = null;

    public GetUserInfoTask(UserHomeActivity userHomeActivity) {
        this.userHomeActivity = userHomeActivity;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try{
            userInfo = ConnectionManager.getUserInfo();
        } catch (IOException e){
            Log.e(LOG_TAG, "Failed to get user info", e);
            return false;
        }
        return true;

    }

    @Override
    protected void onPostExecute(Boolean sucess) {
        UserInfo.updateUserInfo(userInfo);
    }
}
