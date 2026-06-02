package io.github.createtrainfuelscaling.gametest;

import com.simibubi.create.content.trains.entity.Train;
import io.github.createtrainfuelscaling.CreateTrainFuelScaling;
import io.github.createtrainfuelscaling.TrainFuelCostCalculator;
import io.github.createtrainfuelscaling.config.TrainFuelScalingConfig;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.UUID;

@GameTestHolder(CreateTrainFuelScaling.MOD_ID)
@PrefixGameTestTemplate(false)
@Mod.EventBusSubscriber(modid = CreateTrainFuelScaling.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TrainFuelScalingGameTests {
    private TrainFuelScalingGameTests() {
    }

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        event.register(TrainFuelScalingGameTests.class);
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void calculatorClassLoadsInForgeServer(GameTestHelper helper) {
        TrainFuelCostCalculator.costPerTick(40.0, 40.0, 1.0, 2.0, 0.0, 16.0, false, 0.05);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void configDefaultsLoad(GameTestHelper helper) {
        if (!TrainFuelScalingConfig.ENABLED.get() || TrainFuelScalingConfig.BASE_COST.get() != 1.0) {
            helper.fail("Unexpected train fuel scaling config defaults");
            return;
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void itemBurnTimeAvailable(GameTestHelper helper) {
        if (ForgeHooks.getBurnTime(new ItemStack(Items.COAL), null) <= 0) {
            helper.fail("Coal did not report a Forge burn time");
            return;
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void containerRemainderAvailable(GameTestHelper helper) {
        ItemStack remainder = new ItemStack(Items.LAVA_BUCKET).getCraftingRemainingItem();
        if (!remainder.is(Items.BUCKET)) {
            helper.fail("Lava bucket did not return a bucket remainder");
            return;
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void normalSpeedDrainConsumesAtVanillaRate(GameTestHelper helper) {
        Train train = minimalTrain();
        train.speed = 2.0;
        train.fuelTicks = 5;

        train.burnFuel();

        if (train.fuelTicks != 4) {
            helper.fail("Normal speed should consume exactly one fuel tick");
            return;
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void halfSpeedDrainConsumesSlowerViaDebt(GameTestHelper helper) {
        Train train = minimalTrain();
        train.speed = 1.0;
        train.fuelTicks = 5;

        train.burnFuel();
        train.burnFuel();

        if (train.fuelTicks != 5) {
            helper.fail("Half speed should not consume a whole fuel tick after two calls");
            return;
        }

        train.burnFuel();

        if (train.fuelTicks != 4) {
            helper.fail("Half speed should consume one fuel tick after accumulated debt crosses one");
            return;
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void stoppedTrainDoesNotDrainWhenDisabled(GameTestHelper helper) {
        Train train = minimalTrain();
        train.speed = 0.0;
        train.fuelTicks = 5;

        train.burnFuel();
        train.burnFuel();

        if (train.fuelTicks != 5) {
            helper.fail("Stopped train should not drain when consumeWhileStopped is false");
            return;
        }
        helper.succeed();
    }

    private static Train minimalTrain() {
        return new Train(UUID.randomUUID(), null, null, new ArrayList<>(), new ArrayList<>(), false);
    }
}
