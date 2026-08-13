package com.bettercontent.createtrainfuelscaling.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public final class TrainFuelScalingConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.DoubleValue BASE_COST;
    public static final ForgeConfigSpec.DoubleValue K;
    public static final ForgeConfigSpec.DoubleValue MINIMUM_COST;
    public static final ForgeConfigSpec.DoubleValue MAXIMUM_COST;
    public static final ForgeConfigSpec.BooleanValue CONSUME_WHILE_STOPPED;
    public static final ForgeConfigSpec.DoubleValue MINIMUM_MOVING_SPEED_BPS;
    public static final ForgeConfigSpec.BooleanValue RESET_DEBT_WHEN_FUEL_EMPTY;
    public static final ForgeConfigSpec.BooleanValue DEBUG_LOGGING;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        ENABLED = builder.define("enabled", true);
        BASE_COST = builder.defineInRange("baseCost", 1.0, 0.0, Double.MAX_VALUE);
        K = builder.defineInRange("k", 2.0, 0.0, Double.MAX_VALUE);
        MINIMUM_COST = builder.defineInRange("minimumCost", 0.0, 0.0, Double.MAX_VALUE);
        MAXIMUM_COST = builder.defineInRange("maximumCost", 16.0, 0.0, Double.MAX_VALUE);
        CONSUME_WHILE_STOPPED = builder.define("consumeWhileStopped", false);
        MINIMUM_MOVING_SPEED_BPS = builder.defineInRange("minimumMovingSpeedBps", 0.05, 0.0, Double.MAX_VALUE);
        RESET_DEBT_WHEN_FUEL_EMPTY = builder.define("resetDebtWhenFuelEmpty", true);
        DEBUG_LOGGING = builder.define("debugLogging", false);
        SPEC = builder.build();
    }

    private TrainFuelScalingConfig() {
    }

    public static void onLoad(ModConfigEvent.Loading event) {
        validate();
    }

    public static void onReload(ModConfigEvent.Reloading event) {
        validate();
    }

    public static void validate() {
        if (MAXIMUM_COST.get() < MINIMUM_COST.get()) {
            throw new IllegalStateException("create_train_fuel_scaling maximumCost must be >= minimumCost");
        }
    }
}
