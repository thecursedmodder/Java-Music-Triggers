package net.cursedmodder.javatriggers.event;

import net.cursedmodder.javatriggers.network.TriggerMessages;
import net.cursedmodder.javatriggers.network.packets.SpawnPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class WorldEvents {
    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if(!event.getEntity().level().isClientSide) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            BlockPos Pos = player.getRespawnPosition();
            if(Pos != null) {
                TriggerMessages.sendToPlayer(new SpawnPosition(Pos.getX(), Pos.getY(), Pos.getZ()), player);
            }
        }
    }

    @SubscribeEvent
    public void cancelSleep(PlayerSleepInBedEvent e) {
        ServerPlayer Player_Entity = (ServerPlayer) e.getEntity();
        BlockPos Pos = e.getPos();
        if(Pos == null) return;
        TriggerMessages.sendToPlayer(new SpawnPosition(Pos.getX(), Pos.getY(), Pos.getZ()), Player_Entity);
    }
}
