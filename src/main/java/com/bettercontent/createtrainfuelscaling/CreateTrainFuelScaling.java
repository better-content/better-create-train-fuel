package com.bettercontent.createtrainfuelscaling;

import com.bettercontent.createtrainfuelscaling.command.TrainFuelScalingCommands;
import com.bettercontent.createtrainfuelscaling.config.TrainFuelScalingConfig;
import com.bettercontent.createtrainfuelscaling.gametest.TrainFuelScalingGameTests;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.gametest.ForgeGameTestHooks;
import net.minecraft.gametest.framework.GameTestRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CreateTrainFuelScaling.MOD_ID)
public final class CreateTrainFuelScaling {
    public static final String MOD_ID = "create_train_fuel_scaling";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public CreateTrainFuelScaling() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TrainFuelScalingConfig.SPEC);
        modBus.addListener(TrainFuelScalingConfig::onLoad);
        modBus.addListener(TrainFuelScalingConfig::onReload);
        modBus.addListener(TrainFuelScalingGameTests::register);
        if (ForgeGameTestHooks.isGametestEnabled()) {
            GameTestRegistry.register(TrainFuelScalingGameTests.class);
        }
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        TrainFuelScalingCommands.register(event.getDispatcher());
    }
}
