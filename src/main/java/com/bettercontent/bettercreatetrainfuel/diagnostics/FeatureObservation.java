package com.bettercontent.bettercreatetrainfuel.diagnostics;

/** Pure projection of values already observed by the owning native decision. */
public final class FeatureObservation {
    private FeatureObservation() {}
    public static String fields(java.util.UUID train, int carriage, boolean reverse, int added, int fuel, int remainder) { return PlaytestLog.fields("train", train, "carriage", carriage, "reverse", reverse, "added_fuel_ticks", added, "fuel_ticks", fuel, "remainder_returned_count", remainder); }
    
}
