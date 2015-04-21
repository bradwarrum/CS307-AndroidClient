package com.example.android.virtualpantry.Database;

import android.os.AsyncTask;

import com.example.android.virtualpantry.Data.JSONModels;
import com.example.android.virtualpantry.Network.Request;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

/**
 * Created by Brad on 4/20/2015.
 */
public abstract class PersistenceTask extends AsyncTask<Void, Void, Void> {

    private PersistenceCallback callback;
    protected PersistenceResponseCode status;
    protected Object returnValue;
    protected Type returnType;
    protected PersistenceRequestCode requestType;

    public PersistenceTask(PersistenceCallback callback) {
        this.callback = callback;
        status = PersistenceResponseCode.SUCCESS;
        returnValue = null;
        returnType = null;
        requestType = null;
    }
    @Override
    protected final Void doInBackground(Void... params) {
        doInBackground();
        return null;
    }

    protected abstract void doInBackground();

    @Override
    protected final void onPostExecute(Void aVoid) {
        callback.callback(requestType, status, returnValue, returnType);
    }

    /**
     *
     * @param req
     * @param parseType
     * @param <T>
     * @return
     */
    protected final <T> T parseWebResponse(Request req, Class<T> parseType) {
        T parseObj = null;
        int code = req.getResponseCode();
        String content = req.getResponse();
        //If success code
        if (code >= 200 && code < 400) {
            if (content != null) {
                try {
                    parseObj = JSONModels.gson.fromJson(content, parseType);
                    return parseObj;
                } catch (JsonSyntaxException e) {
                    this.status = PersistenceResponseCode.ERR_SERVER_MALFORMED_RESPONSE;
                }
            } else {
                this.status = PersistenceResponseCode.ERR_SERVER_MALFORMED_RESPONSE;
            }
        } else {
            if (content != null) {
                try {
                    JSONModels.ErrorResponse errResp = JSONModels.gson.fromJson(content, JSONModels.ErrorResponse.class);
                    this.returnValue = errResp;
                    this.status = PersistenceResponseCode.fromBackingCode(errResp.errorCode);
                    this.returnType = JSONModels.ErrorResponse.class;

                } catch (JsonSyntaxException e) {
                    this.returnValue = null;
                    this.status = PersistenceResponseCode.ERR_SERVER_MALFORMED_RESPONSE;
                }
            } else this.status = PersistenceResponseCode.ERR_SERVER_MALFORMED_RESPONSE;
        }
        return null;
    }

}
