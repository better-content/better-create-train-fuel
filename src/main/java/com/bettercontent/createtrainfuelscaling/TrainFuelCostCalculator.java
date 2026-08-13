package com.bettercontent.createtrainfuelscaling;

public final class TrainFuelCostCalculator {
    private TrainFuelCostCalculator() {
    }

    public static double speedBps(double trainSpeedBlocksPerTick) {
        double speed = Math.abs(trainSpeedBlocksPerTick) * 20.0;
        return Double.isFinite(speed) ? speed : 0.0;
    }

    public static double speedRatio(double speedBps, double normalMaxSpeedBps) {
        if (!Double.isFinite(speedBps) || speedBps <= 0.0) {
            return 0.0;
        }
        if (!Double.isFinite(normalMaxSpeedBps) || normalMaxSpeedBps <= 0.0) {
            return 0.0;
        }
        return speedBps / normalMaxSpeedBps;
    }

    public static double costPerTick(double speedBps, double normalMaxSpeedBps, double baseCost, double k,
            double minimumCost, double maximumCost, boolean consumeWhileStopped, double minimumMovingSpeedBps) {
        if (!consumeWhileStopped && speedBps < Math.max(0.0, minimumMovingSpeedBps)) {
            return 0.0;
        }
        double safeBase = sanitizeNonNegative(baseCost);
        double safeK = sanitizeNonNegative(k);
        double min = sanitizeNonNegative(minimumCost);
        double max = Math.max(min, sanitizeNonNegative(maximumCost));
        double ratio = speedRatio(speedBps, normalMaxSpeedBps);
        double cost = safeBase * Math.exp(safeK * (ratio - 1.0));
        if (!Double.isFinite(cost)) {
            cost = max;
        }
        return clamp(cost, min, max);
    }

    public static FuelDebtResult applyDebt(int fuelTicks, double existingDebt, double costPerTick,
            boolean resetDebtWhenFuelEmpty) {
        double debt = Double.isFinite(existingDebt) ? Math.max(0.0, existingDebt) : 0.0;
        double cost = Double.isFinite(costPerTick) ? Math.max(0.0, costPerTick) : 0.0;
        debt += cost;
        int consume = (int) Math.floor(debt);
        if (consume <= 0) {
            return new FuelDebtResult(fuelTicks, debt, 0);
        }
        int consumed = Math.min(Math.max(fuelTicks, 0), consume);
        int remainingFuel = Math.max(fuelTicks, 0) - consumed;
        debt -= consumed;
        if (remainingFuel <= 0 && resetDebtWhenFuelEmpty) {
            debt = 0.0;
        }
        return new FuelDebtResult(remainingFuel, debt, consumed);
    }

    private static double sanitizeNonNegative(double value) {
        return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public record FuelDebtResult(int remainingFuelTicks, double remainingDebt, int consumedFuelTicks) {
    }
}
