package com.example.android.virtualpantry.Database;

import java.lang.reflect.Type;

/**
 * Created by Brad on 4/20/2015.
 */
public interface PersistenceCallback {
    /**
     * Called upon completion of any asynchronous persistence layer data retrieval or modification request
     * @param request An enumeration value specifying which request generated the callback.
     * @param status See PersistenceResponseCode below for more information.
     * @param returnValue The JSON Model returned by the persistence request.  If the persistence request fails with no explicit error model, this field is null.  In the case that the server returns an error model, this field will be populated with the contents of the error model.
     * @param returnType The type of returnValue.  This field may be null if returnValue is null.
     * @see com.example.android.virtualpantry.Database.PersistenceResponseCode
     */
    public void callback(PersistenceRequestCode request, PersistenceResponseCode status,  Object returnValue, Type returnType);
}
