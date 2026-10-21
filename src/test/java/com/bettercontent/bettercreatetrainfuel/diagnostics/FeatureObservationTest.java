package com.bettercontent.bettercreatetrainfuel.diagnostics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class FeatureObservationTest {
    @Test public void nativeScalarsHaveExplicitStage() { var fields = FeatureObservation.fields(new java.util.UUID(0, 1), 2, true, 1600, 1600, 0); assertTrue(fields.contains("reverse=true")); assertTrue(fields.contains("carriage=2")); assertTrue(fields.contains("added_fuel_ticks=1600")); }
    @Test public void uncertaintyAndPrivacyStayExplicit() { var fields = FeatureObservation.fields(new java.util.UUID(0, 1), 0, false, 100, 100, 1); assertTrue(fields.contains("remainder_returned_count=1")); assertFalse(fields.contains("remainder_inserted=true")); }
}
