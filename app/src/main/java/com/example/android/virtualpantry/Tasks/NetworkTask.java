package com.example.android.virtualpantry.Tasks;

import android.os.AsyncTask;

import com.example.android.virtualpantry.Network.Request;

/**
 * Created by Garrett on 4/19/2015.
 */
public class NetworkTask extends AsyncTask<Void, Void, Integer>{

    private Request request;


    public NetworkTask() {
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
