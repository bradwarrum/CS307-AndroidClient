package com.example.android.virtualpantry.Network;

import android.util.Log;

import com.example.android.virtualpantry.Data.JSONModels;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Garrett on 3/29/2015.
 */
public class Request {

    private static final String LOG_TAG = "Request_base";

    protected static final String protocol = "http";
    private static final int port = 80;
    private static final String host = "104.236.87.206";
    private static final String file_base = "/api";

    public static final String GET = "GET";
    public static final String POST = "POST";

    //personal error
    public static final int ERR_REQUEST_FAILED = 1;
    //from server docs
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


    public Request(String file, String connectionMethod){
        this.file = file_base + file;
        this.connectionMethod = connectionMethod;
        this.json = null;
    }

    public Request(String file, String connectionMethod, String json){
        this.file = file_base + file;
        this.connectionMethod = connectionMethod;
        this.json = json;
    }

    public Request(String file, String connectionMethod, JSONModels.JSONModel json){
        this(file, connectionMethod, json.toString());
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

    public String getSendJSON(){
        return json;
    }

    public String getFilePath(){
        return file;
    }

    public String getMessage() {return json;}

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

    //TODO: verify this is proper
    public void execute(){
        if(json != null){
            send();
        }
            receive(true);
        try {
            responseCode = connection.getResponseCode();
        } catch (IOException e){
            Log.e(LOG_TAG, "Error getting response code", e);
        } finally {
            this.close();
        }
    }

    public void executeNoResponse(){
        if(json != null){
            send();
        }
        receive(false);
        try {
            responseCode = connection.getResponseCode();
        } catch (IOException e){
            Log.e(LOG_TAG, "Error getting response code", e);
        } finally {
            this.close();
        }
    }

    private void receive(boolean logError){
        BufferedReader reader = null;
        try {
        	InputStream inputStream;
        	//Fetch error stream if you don't get a successful response
        	if (connection.getResponseCode() >= 400 || connection.getResponseCode() < 200) {
        		inputStream = connection.getErrorStream();
        	} else {
        		inputStream = connection.getInputStream();
        	}
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuffer bufferedResponse = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                bufferedResponse.append(line);
                bufferedResponse.append("\n");
            }
            this.response = bufferedResponse.toString();
        } catch(IOException e){
            if(logError) { Log.e(LOG_TAG, "failed to read in receive()", e);}
            connectionError = true;
            response = null;
        }
        finally {
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


