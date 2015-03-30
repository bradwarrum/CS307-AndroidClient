package com.example.android.virtualpantry.Network;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.MalformedInputException;

/**
 * Created by Garrett on 3/29/2015.
 */
public class Request {

    private static final String LOG_TAG = "Request_base";

    protected static final String protocol = "http";
    private static final int port = 8000;
    private static final String host = "104.236.87.206";

    public static final int ERR_INTERNAL_ERROR = 3;
    public static final int ERR_TOKEN_EXPIRED = 4;
    public static final int ERR_INVALID_TOKEN = 5;
    public static final int ERR_EMAIL_TAKEN = 6;
    public static final int ERR_INVALID_PASSWORD = 7;
    public static final int ERR_INVALID_PAYLOAD = 8;
    public static final int ERR_USER_NOT_FOUND = 9;
    public static final int ERR_HOUSEHOLD_NOT_FOUND = 10;
    public static final int ERR_LIST_NOT_FOUND = 11;
    public static final int ERR_ITEM_NOT_FOUND = 12;
    public static final int ERR_UPC_FORMAT_NOT_SUPPORTED = 13;
    public static final int ERR_UPC_CHECKSUM_INVALID = 14;
    public static final int ERR_INSUFFICIENT_PERMISSIONS = 15;
    public static final int ERR_OUTDATED_TIMESTAMP = 16;

    private HttpURLConnection connection;

    //user input
    private final String file;
    private final String json;
    private final String connectionMethod;

    //state tracking
    private boolean connectionError = false;
    private boolean dataError = false;
    private int responseCode = 0;
    private String response = null;


    private Request(String file, String connectionMethod){
        this.file = file;
        this.connectionMethod = connectionMethod;
        this.json = null;
    }

    private Request(String file, String connectionMethod, String json){
        this.file = file;
        this.connectionMethod = connectionMethod;
        this.json = json;
    }

    public boolean openConnection(){
        try{
            connection = (HttpURLConnection) new URL(protocol, host, port, file).openConnection();
            if(this.connectionMethod.equals("POST")){
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoInput(true);
                connection.setDoOutput(true);
            } else if(this.connectionMethod.equals("GET")){
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
            } else {
                throw new IllegalArgumentException("Did not set connection method to GET or POST");
            }
        } catch (MalformedURLException e){
            Log.e(LOG_TAG, "Bad request URL in openConnection()", e);
            connectionError = true;
            this.close();
            return false;
        } catch (IOException e){
            Log.e(LOG_TAG, "IO error in open connection in openConnection", e);
            connectionError = true;
            this.close();
            return false;
        } catch (IllegalArgumentException e){
            Log.e("LOG_TAG", "GET or POST was not set", e);
            this.close();
            return false;
        }
        return true;
    }

    public int getResponseCode(){
        return this.responseCode;
    }

    public String getResponse(){
        return this.response;
    }

    public boolean errorOccured(){
        return connectionError || dataError;
    }

    public boolean dataErrorOccured(){
        return dataError;
    }

    public boolean connectionErrorOccured(){
        return connectionError;
    }

    private void send(){
        try {
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(json);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e){
            Log.e(LOG_TAG, "Error writing to output stream in send()", e);
            connectionError = true;
        }
    }

    //TODO: execute function
    public void execute(){
        
    }

    private void receive(){
        BufferedReader reader = null;
        try {
            InputStream inputStream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuffer bufferedResponse = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                bufferedResponse.append(line);
                bufferedResponse.append("\n");
            }
        } catch(IOException e){
            Log.e(LOG_TAG, "failed to read in receive()", e);
            connectionError = true;
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e){
                    Log.e(LOG_TAG, "failed in close in receive()", e);
                    connectionError = true;
                }
            }
        }
    }

    private void close(){
        if(connection != null){
            connection.disconnect();
        }
    }

}


