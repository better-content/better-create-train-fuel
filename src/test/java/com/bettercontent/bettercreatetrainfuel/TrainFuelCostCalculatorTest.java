package com.bettercontent.bettercreatetrainfuel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrainFuelCostCalculatorTest {
    private static final double EPSILON = 1.0e-9;

    @Test
    void speedBpsConvertsBlocksPerTick() {
        assertEquals(10.0, TrainFuelCostCalculator.speedBps(0.5), EPSILON);
    }

    @Test
    void speedBpsUsesAbsoluteSpeed() {
        assertEquals(10.0, TrainFuelCostCalculator.speedBps(-0.5), EPSILON);
    }

    @Test
    void nonFiniteSpeedBecomesZero() {
        assertEquals(0.0, TrainFuelCostCalculator.speedBps(Double.NaN), EPSILON);
    }

    @Test
    void ratioAtNormalMaxIsOne() {
        assertEquals(1.0, TrainFuelCostCalculator.speedRatio(40.0, 40.0), EPSILON);
    }

    @Test
    void ratioAtHalfSpeedIsHalf() {
        assertEquals(0.5, TrainFuelCostCalculator.speedRatio(20.0, 40.0), EPSILON);
    }

    @Test
    void invalidNormalMaxRatioIsZero() {
        assertEquals(0.0, TrainFuelCostCalculator.speedRatio(20.0, 0.0), EPSILON);
    }

    @Test
    void normalSpeedCostEqualsBaseCost() {
        assertEquals(1.0, TrainFuelCostCalculator.costPerTick(40.0, 40.0, 1.0, 2.0, 0.0, 16.0, false, 0.05), EPSILON);
    }

    @Test
    void halfSpeedCostIsExponential() {
        assertEquals(Math.exp(-1.0), TrainFuelCostCalculator.costPerTick(20.0, 40.0, 1.0, 2.0, 0.0, 16.0, false, 0.05), EPSILON);
    }

    @Test
    void doubleSpeedCostIsExponential() {
        assertEquals(Math.exp(2.0), TrainFuelCostCalculator.costPerTick(80.0, 40.0, 1.0, 2.0, 0.0, 16.0, false, 0.05), EPSILON);
    }

    @Test
    void fastOperationIsExpensiveWithoutForbiddingIt() {
        double normal = TrainFuelCostCalculator.costPerTick(40.0, 40.0, 1.0, 2.0, 0.0, 16.0, false, 0.05);
        double fast = TrainFuelCostCalculator.costPerTick(80.0, 40.0, 1.0, 2.0, 0.0, 16.0, false, 0.05);
        double veryFast = TrainFuelCostCalculator.costPerTick(120.0, 40.0, 1.0, 2.0, 0.0, 16.0, false, 0.05);

        assertEquals(1.0, normal, EPSILON);
        assertEquals(Math.exp(2.0), fast, EPSILON);
        assertEquals(16.0, veryFast, EPSILON);
    }

    @Test
    void reversePropulsionHasTheSameFuelCostAsForward() {
        double forward = TrainFuelCostCalculator.costPerTick(
                TrainFuelCostCalculator.speedBps(2.0), 40.0, 1.0, 2.0, 0.0, 16.0, false, 0.05);
        double reverse = TrainFuelCostCalculator.costPerTick(
                TrainFuelCostCalculator.speedBps(-2.0), 40.0, 1.0, 2.0, 0.0, 16.0, false, 0.05);

        assertEquals(forward, reverse, EPSILON);
    }

    @Test
    void stoppedTrainDoesNotDrainByDefault() {
        assertEquals(0.0, TrainFuelCostCalculator.costPerTick(0.0, 40.0, 1.0, 2.0, 0.0, 16.0, false, 0.05), EPSILON);
    }

    @Test
    void stoppedTrainCanDrainWhenEnabled() {
        assertEquals(Math.exp(-2.0), TrainFuelCostCalculator.costPerTick(0.0, 40.0, 1.0, 2.0, 0.0, 16.0, true, 0.05), EPSILON);
    }

    @Test
    void costRespectsMinimum() {
        assertEquals(0.5, TrainFuelCostCalculator.costPerTick(1.0, 40.0, 1.0, 2.0, 0.5, 16.0, true, 0.05), EPSILON);
    }

    @Test
    void costRespectsMaximum() {
        assertEquals(2.0, TrainFuelCostCalculator.costPerTick(120.0, 40.0, 1.0, 2.0, 0.0, 2.0, false, 0.05), EPSILON);
    }

    @Test
    void fractionalDebtAccumulatesWithoutConsuming() {
        TrainFuelCostCalculator.FuelDebtResult result = TrainFuelCostCalculator.applyDebt(10, 0.25, 0.5, true);
        assertEquals(10, result.remainingFuelTicks());
        assertEquals(0.75, result.remainingDebt(), EPSILON);
        assertEquals(0, result.consumedFuelTicks());
    }

    @Test
    void wholeDebtConsumesFuelTicks() {
        TrainFuelCostCalculator.FuelDebtResult result = TrainFuelCostCalculator.applyDebt(10, 0.75, 0.5, true);
        assertEquals(9, result.remainingFuelTicks());
        assertEquals(0.25, result.remainingDebt(), EPSILON);
        assertEquals(1, result.consumedFuelTicks());
    }

    @Test
    void multiTickDebtConsumesMultipleFuelTicks() {
        TrainFuelCostCalculator.FuelDebtResult result = TrainFuelCostCalculator.applyDebt(10, 0.1, 2.4, true);
        assertEquals(8, result.remainingFuelTicks());
        assertEquals(0.5, result.remainingDebt(), EPSILON);
        assertEquals(2, result.consumedFuelTicks());
    }

    @Test
    void resetDebtWhenFuelEmptyClearsRemainingDebt() {
        TrainFuelCostCalculator.FuelDebtResult result = TrainFuelCostCalculator.applyDebt(1, 0.5, 2.0, true);
        assertEquals(0, result.remainingFuelTicks());
        assertEquals(0.0, result.remainingDebt(), EPSILON);
        assertEquals(1, result.consumedFuelTicks());
    }

    @Test
    void preservingDebtWhenFuelEmptyKeepsUnpaidDebt() {
        TrainFuelCostCalculator.FuelDebtResult result = TrainFuelCostCalculator.applyDebt(1, 0.5, 2.0, false);
        assertEquals(0, result.remainingFuelTicks());
        assertEquals(1.5, result.remainingDebt(), EPSILON);
        assertEquals(1, result.consumedFuelTicks());
    }

    @Test
    void sustainedNormalAndFastPropulsionConsumeTheirAccumulatedDebt() {
        int normalFuel = 100;
        int fastFuel = 100;
        double normalDebt = 0.0;
        double fastDebt = 0.0;
        double normalCost = TrainFuelCostCalculator.costPerTick(40.0, 40.0, 1.0, 2.0, 0.0, 16.0, false, 0.05);
        double fastCost = TrainFuelCostCalculator.costPerTick(80.0, 40.0, 1.0, 2.0, 0.0, 16.0, false, 0.05);

        for (int tick = 0; tick < 10; tick++) {
            TrainFuelCostCalculator.FuelDebtResult normal = TrainFuelCostCalculator.applyDebt(normalFuel, normalDebt, normalCost, true);
            normalFuel = normal.remainingFuelTicks();
            normalDebt = normal.remainingDebt();
            TrainFuelCostCalculator.FuelDebtResult fast = TrainFuelCostCalculator.applyDebt(fastFuel, fastDebt, fastCost, true);
            fastFuel = fast.remainingFuelTicks();
            fastDebt = fast.remainingDebt();
        }

        assertEquals(90, normalFuel);
        assertEquals(27, fastFuel);
        assertEquals(0.0, normalDebt, EPSILON);
        assertEquals(10 * Math.exp(2.0) - 73, fastDebt, EPSILON);
    }
}
