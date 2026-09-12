package com.bettercontent.createtrainfuelscaling.api;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.Event;
import java.util.UUID;
/** Native speed scaling consumed more fuel than the same operation at baseline speed. */
public final class TrainFuelConsumedEvent extends Event {
    public final MinecraftServer server;
    public final UUID operator, train;
    public final int consumed, baseline;
    public final double speed;
    public TrainFuelConsumedEvent(MinecraftServer server, UUID operator, UUID train, int consumed, int baseline, double speed) {
        this.server=server; this.operator=operator; this.train=train; this.consumed=consumed; this.baseline=baseline; this.speed=speed;
    }
}
