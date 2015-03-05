package com.example.android.virtualpantry.Data;

/**
 * Created by Garrett on 3/3/2015.
 */
public class HouseholdBrief {

    private long householdID;
    private String householdName;
    private String householdDescription;

    public HouseholdBrief(JSONModels.HouseholdShortJSON householdShortJSON){
        householdID = householdShortJSON.householdID;
        householdName = householdShortJSON.householdName;
        householdDescription = householdShortJSON.householdDescription;
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
