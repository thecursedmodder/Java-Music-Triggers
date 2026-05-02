package net.cursedmodder.javatriggers.event;


import net.cursedmodder.javatriggers.triggers.FoundationTriggerHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void playerClientTickEvent(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END)
            FoundationTriggerHandler.tick();
    }

    @SubscribeEvent
    public static void playerClientGameTickEvent(TickEvent.PlayerTickEvent event) {
        if(event.side.isClient()) if(event.phase == TickEvent.Phase.START)
            FoundationTriggerHandler.gameTick(event.player);
    }


}
