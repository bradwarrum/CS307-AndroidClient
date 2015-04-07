package com.example.android.virtualpantry.Network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Database.PreferencesHelper;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Garrett on 4/6/2015.
 */
public class NetworkUtility {

    //public paths
    public static final String LOGIN_FILE_PATH = "/users/login";
    public static final String REGISTER_FILE_PATH = "/users/register";

    //private paths
    public static final String GET_USER_INFO_BASE_PATH = "/users/me?token=";

    public static int loginSequence(Context context){
        Request request;
        String email = context.getSharedPreferences(PreferencesHelper.USER_INFO,
                context.MODE_PRIVATE).getString(PreferencesHelper.USERNAME, null);
        String password = context.getSharedPreferences(PreferencesHelper.USER_INFO,
                context.MODE_PRIVATE).getString(PreferencesHelper.PASSWORD, null);
        String encryptedPassword = NetworkUtility.sha256(password);
        if(email != null && password != null) {
            request = new Request(
                    NetworkUtility.LOGIN_FILE_PATH,
                    Request.POST,
                    new JSONModels.LoginReqJSON(email, password));
            if(request.openConnection()){
                request.execute();
            } else {
                Log.e("Login Sequence", "Error opening connection in re-login");
                return -1;
            }
            if(request.getResponseCode() != 200) {
                if(request.getResponse() != null) {
                    JSONModels.LoginResJSON loginRes =
                            JSONModels.gson.fromJson(request.getResponse(), JSONModels.LoginResJSON.class);
                    SharedPreferences user_info =
                            context.getSharedPreferences(PreferencesHelper.USER_INFO, context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = user_info.edit();
                    editor.putString(PreferencesHelper.TOKEN, loginRes.token);
                    editor.commit();
                    return 1;
                } else {
                    Log.e("Login Sequence", "Got null response");
                    return -1;
                }
            } else {
                Log.e("Login Sequence", "Got wrong response code: " + request.getResponseCode());
                return -1;
            }
        } else {
            Log.e("Login Sequence", "Email or password was null");
            return -1;
            }
    }


    public static String sha256(String text){
        MessageDigest digest = null;
        try{
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e){
            Log.e("Register", "Encryption failed", e);
        }
        digest.reset();
        byte[] encryptedData = digest.digest(text.getBytes());
        return String.format("%0" + (encryptedData.length*2) + "X", new BigInteger(1, encryptedData));
    }

    public static String createGetUserInfoString(String token){
        return GET_USER_INFO_BASE_PATH + token;
    }


}
