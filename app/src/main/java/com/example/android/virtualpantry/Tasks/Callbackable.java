package com.example.android.virtualpantry.Tasks;

/**
 * Created by Garrett on 4/19/2015.
 */
public interface Callbackable {

    public void callback(int taskType, int responseCode, String response, int internalResponseCode);
}
