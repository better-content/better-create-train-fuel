package io.github.createtrainfuelscaling.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.simibubi.create.infrastructure.config.AllConfigs;
import io.github.createtrainfuelscaling.TrainFuelCostCalculator;
import io.github.createtrainfuelscaling.config.TrainFuelScalingConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class TrainFuelScalingCommands {
    private TrainFuelScalingCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("create_train_fuel_scaling")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("formula")
                        .then(Commands.argument("speedBps", DoubleArgumentType.doubleArg(0.0))
                                .executes(context -> {
                                    double speedBps = DoubleArgumentType.getDouble(context, "speedBps");
                                    double normalMax = normalMaxSpeedBps();
                                    double ratio = TrainFuelCostCalculator.speedRatio(speedBps, normalMax);
                                    double cost = TrainFuelCostCalculator.costPerTick(speedBps, normalMax,
                                            TrainFuelScalingConfig.BASE_COST.get(),
                                            TrainFuelScalingConfig.K.get(),
                                            TrainFuelScalingConfig.MINIMUM_COST.get(),
                                            TrainFuelScalingConfig.MAXIMUM_COST.get(),
                                            TrainFuelScalingConfig.CONSUME_WHILE_STOPPED.get(),
                                            TrainFuelScalingConfig.MINIMUM_MOVING_SPEED_BPS.get());
                                    context.getSource().sendSuccess(() -> Component.literal(String.format(
                                            "normalMax=%.3f bps, ratio=%.4f, cost=%.4f fuel ticks/tick",
                                            normalMax, ratio, cost)), false);
                                    return 1;
                                }))));
    }

    public static double normalMaxSpeedBps() {
        double configured = AllConfigs.server().trains.poweredTrainTopSpeed.getF();
        return Double.isFinite(configured) && configured > 0.0 ? configured : 40.0;
    }
}
