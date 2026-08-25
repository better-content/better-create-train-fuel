package com.bettercontent.createtrainfuelscaling.mixin;

import com.simibubi.create.content.contraptions.minecart.TrainCargoManager;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.bettercontent.createtrainfuelscaling.CreateTrainFuelScaling;
import com.bettercontent.createtrainfuelscaling.TrainFuelCostCalculator;
import com.bettercontent.createtrainfuelscaling.command.TrainFuelScalingCommands;
import com.bettercontent.createtrainfuelscaling.config.TrainFuelScalingConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(value = Train.class, remap = false)
public abstract class TrainMixin {
    @Unique
    private static final String CREATE_TRAIN_FUEL_SCALING_DEBT_TAG = "CreateTrainFuelScalingFuelDebt";
    @Unique
    private double createTrainFuelScaling$fuelDebt;
    @Unique
    private int createTrainFuelScaling$debugBurnCalls;

    @Inject(method = "burnFuel", at = @At("HEAD"), cancellable = true, remap = false)
    private void createTrainFuelScaling$burnFuel(CallbackInfo ci) {
        if (!TrainFuelScalingConfig.ENABLED.get()) {
            return;
        }
        ci.cancel();

        Train train = (Train) (Object) this;
        double speedBps = TrainFuelCostCalculator.speedBps(train.speed);
        double normalMax = TrainFuelScalingCommands.normalMaxSpeedBps();
        double cost = TrainFuelCostCalculator.costPerTick(speedBps, normalMax,
                TrainFuelScalingConfig.BASE_COST.get(),
                TrainFuelScalingConfig.K.get(),
                TrainFuelScalingConfig.MINIMUM_COST.get(),
                TrainFuelScalingConfig.MAXIMUM_COST.get(),
                TrainFuelScalingConfig.CONSUME_WHILE_STOPPED.get(),
                TrainFuelScalingConfig.MINIMUM_MOVING_SPEED_BPS.get());

        if (train.fuelTicks > 0) {
            TrainFuelCostCalculator.FuelDebtResult result = TrainFuelCostCalculator.applyDebt(train.fuelTicks,
                    createTrainFuelScaling$fuelDebt, cost, TrainFuelScalingConfig.RESET_DEBT_WHEN_FUEL_EMPTY.get());
            train.fuelTicks = result.remainingFuelTicks();
            createTrainFuelScaling$fuelDebt = result.remainingDebt();
            createTrainFuelScaling$debug(train, speedBps, normalMax, cost);
            return;
        }

        if (TrainFuelScalingConfig.RESET_DEBT_WHEN_FUEL_EMPTY.get()) {
            createTrainFuelScaling$fuelDebt = 0.0;
        }

        createTrainFuelScaling$refuel(train);
        createTrainFuelScaling$debug(train, speedBps, normalMax, cost);
    }

    @Unique
    private void createTrainFuelScaling$refuel(Train train) {
        boolean iterateFromBack = train.speed < 0;
        List<Carriage> carriages = train.carriages;
        int carriageCount = carriages == null ? 0 : carriages.size();

        for (int index = 0; index < carriageCount; index++) {
            int i = iterateFromBack ? carriageCount - 1 - index : index;
            Carriage carriage = carriages.get(i);
            if (carriage == null) {
                continue;
            }
            TrainCargoManager storage = carriage.storage;
            if (storage == null) {
                continue;
            }
            IItemHandlerModifiable fuelItems = storage.getFuelItems();
            if (fuelItems == null) {
                continue;
            }

            for (int slot = 0; slot < fuelItems.getSlots(); slot++) {
                ItemStack stack = fuelItems.extractItem(slot, 1, true);
                if (stack.isEmpty()) {
                    continue;
                }
                int burnTime = ForgeHooks.getBurnTime(stack, null);
                if (burnTime <= 0) {
                    continue;
                }

                stack = fuelItems.extractItem(slot, 1, false);
                if (stack.isEmpty()) {
                    continue;
                }
                train.fuelTicks += burnTime * stack.getCount();
                ItemStack containerItem = stack.getCraftingRemainingItem();
                if (!containerItem.isEmpty()) {
                    ItemHandlerHelper.insertItemStacked(fuelItems, containerItem, false);
                }
                return;
            }
        }
    }

    @Unique
    private void createTrainFuelScaling$debug(Train train, double speedBps, double normalMax, double cost) {
        if (!TrainFuelScalingConfig.DEBUG_LOGGING.get()) {
            return;
        }
        createTrainFuelScaling$debugBurnCalls++;
        if (createTrainFuelScaling$debugBurnCalls % 100 != 0) {
            return;
        }
        double ratio = TrainFuelCostCalculator.speedRatio(speedBps, normalMax);
        CreateTrainFuelScaling.LOGGER.debug("train={} speedBps={} ratio={} cost={} debt={} fuelTicks={}",
                train.id, speedBps, ratio, cost, createTrainFuelScaling$fuelDebt, train.fuelTicks);
    }

    @Inject(method = "write", at = @At("RETURN"), remap = false)
    private void createTrainFuelScaling$write(DimensionPalette dimensions, CallbackInfoReturnable<CompoundTag> cir) {
        if (Double.isFinite(createTrainFuelScaling$fuelDebt)) {
            cir.getReturnValue().putDouble(CREATE_TRAIN_FUEL_SCALING_DEBT_TAG, createTrainFuelScaling$fuelDebt);
        }
    }

    @Inject(method = "read", at = @At("RETURN"), remap = false)
    private static void createTrainFuelScaling$read(CompoundTag tag, Map<UUID, ?> trackNetworks, DimensionPalette dimensions,
            CallbackInfoReturnable<Train> cir) {
        Train train = cir.getReturnValue();
        if (train == null || !tag.contains(CREATE_TRAIN_FUEL_SCALING_DEBT_TAG)) {
            return;
        }
        double debt = tag.getDouble(CREATE_TRAIN_FUEL_SCALING_DEBT_TAG);
        if (Double.isFinite(debt)) {
            ((TrainMixin) (Object) train).createTrainFuelScaling$fuelDebt = Math.max(0.0, debt);
        }
    }
}
