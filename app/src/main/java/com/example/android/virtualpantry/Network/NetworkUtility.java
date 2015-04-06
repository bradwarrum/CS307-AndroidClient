package com.example.android.virtualpantry.Network;

import android.util.Log;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Garrett on 4/6/2015.
 */
public class NetworkUtility {

    public static final String LOGIN_FILE_PATH = "users/login";
    public static final String REGISTER_FILE_PATH = "users/register";

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


}
