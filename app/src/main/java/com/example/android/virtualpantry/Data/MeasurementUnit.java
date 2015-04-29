package com.example.android.virtualpantry.Data;

/**
 * Created by Garrett on 4/28/2015.
 */
public enum MeasurementUnit {
    lb(1),
    oz(2),
    g(3),
    mg(4),
    L(5),
    mL(6),
    gal(7),
    qt(8),
    pt(9),
    c(10),
    tsp(11),
    tbsp(12),
    floz(13),
    units(14);

    private int ID;

    MeasurementUnit(int ID) {
        this.ID = ID;
    }

    public int getIntVal(){
        return this.ID;
    }

}
