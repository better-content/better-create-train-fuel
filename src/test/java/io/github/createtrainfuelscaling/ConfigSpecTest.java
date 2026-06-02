package io.github.createtrainfuelscaling;

import io.github.createtrainfuelscaling.config.TrainFuelScalingConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigSpecTest {
    @Test
    void defaultsAreValid() {
        assertTrue(TrainFuelScalingConfig.BASE_COST.getDefault() instanceof Double);
        assertTrue((Double) TrainFuelScalingConfig.MAXIMUM_COST.getDefault()
                >= (Double) TrainFuelScalingConfig.MINIMUM_COST.getDefault());
    }
}
