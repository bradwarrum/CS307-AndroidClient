package com.example.android.virtualpantry;

import android.util.Log;

import com.example.android.virtualpantry.Data.JSONModels;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Created by Garrett on 3/2/2015.
 */
public class ConnectionManager {

    private static final String protocol = "http";
    private static final int port = 8000;
    private static final String host = "104.236.87.206";
    private static String response = null;

    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    //response codes
    public static final int BAD_REQUEST = 400;
    public static final int FORBIDDEN = 403;
    public static final int OK = 200;
    public static final int CREATED = 201;

    //store data


    //Transaction class
    public static class Transaction {
        private HttpURLConnection connection;

        public Transaction(String protocol, String host, int port, String file) throws MalformedURLException, IOException {
            connection = (HttpURLConnection) new URL(protocol, host, port, file).openConnection();
        }

        public String getRequestURL() {
            return connection.getURL().toString();
        }

        public void setGetMethod() throws ProtocolException {
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
        }

        public void setPostMethod() throws ProtocolException {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);
        }

        public void send(String request) throws IOException {
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(request);
            wr.flush(); wr.close();
        }

        public int getResponseCode() throws IOException {
            return connection.getResponseCode();
        }

        public String getResponse() throws IOException {
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append("\n");
            }
            rd.close();
            return response.toString();
        }

        public void close() {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    public static int register(String emailAddress, String password, String first, String last)
            throws MalformedURLException, IOException {
        int rcode;

        //encrypt here!
        MessageDigest digest = null;
        try{
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e){
            Log.e("Register", "Encryption failed", e);
        }
        digest.reset();
        byte[] encryptedData = digest.digest(password.getBytes());
        password = String.format("%0" + (encryptedData.length*2) + "X", new BigInteger(1, encryptedData));

        //begin transaction
        Transaction request = new Transaction(protocol, host, port, "/users/register");
        request.setPostMethod();
        String reqstr = gson.toJson(new JSONModels.RegisterReqJSON(emailAddress, password, first, last));
        request.send(reqstr);
        rcode = request.getResponseCode();
        request.close();
        if(rcode != CREATED || rcode != FORBIDDEN || rcode != BAD_REQUEST){
            Log.e("Register", "Register return code not expected: " + rcode);
        }
        return rcode;
    }

    public static int login(String emailAddress, String password) throws IOException {
        int rcode;

        //encrypt here!
        MessageDigest digest = null;
        try{
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e){
            Log.e("Register", "Encryption failed", e);
        }
        digest.reset();
        byte[] encryptedData = digest.digest(password.getBytes());
        password = String.format("%0" + (encryptedData.length*2) + "X", new BigInteger(1, encryptedData));

        //begin transaction
        Transaction request = new Transaction(protocol, host, port, "/users/login");
        request.setPostMethod();
        String reqstr = gson.toJson(new JSONModels.LoginReqJSON(emailAddress, password));
        request.send(reqstr);
        System.out.println("Response:");
        rcode = request.getResponseCode();
        System.out.println("HTTP " + rcode);
        try {
            response = request.getResponse();
        }catch (IOException e) {
            Log.e("Login", "Get Response from request failed", e);
        }
        request.close();
        return rcode;
    }
}
