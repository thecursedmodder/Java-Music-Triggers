package net.cursedmodder.javatriggers.triggers.example;

import net.cursedmodder.javatriggers.triggers.base.TriggerBase;
import net.cursedmodder.javatriggers.triggers.songs.Song;
import net.cursedmodder.javatriggers.util.debug.watch.DebugUI;
import net.minecraft.client.Minecraft;

public class GeneralTriggerExample extends TriggerBase {
    public GeneralTriggerExample() {
        super(1, 0, 0, true, 1F, 0.5F, TriggerSongs.generalSongs().toArray(new Song[0]));
        canBeInterrupted = true;
    }

    @Override
    public boolean canPlay() {
        return Minecraft.getInstance().level != null && super.canPlay();
    }
}
