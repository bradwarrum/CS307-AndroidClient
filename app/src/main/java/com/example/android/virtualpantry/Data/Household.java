package com.example.android.virtualpantry.Data;

/**
 * Created by Garrett on 3/3/2015.
 */
public class Household {

    private long householdID;
    private String householdName;
    private String householdDescription;

    public Household(JSONModels.HouseholdJSON householdJSON){
        householdID = householdJSON.householdID;
        householdName = householdJSON.householdName;
        householdDescription = householdJSON.householdDescription;
    }

    //region getters
    public long getHouseholdID() {
        return householdID;
    }

    public String getHouseholdName() {
        return householdName;
    }

    public String getHouseholdDescription() {
        return householdDescription;
    }
    //endregion
}
