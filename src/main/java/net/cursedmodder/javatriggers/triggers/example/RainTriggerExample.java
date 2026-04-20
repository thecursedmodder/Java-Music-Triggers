package net.cursedmodder.javatriggers.triggers.example;

import net.cursedmodder.javatriggers.util.ClientContext;
import net.cursedmodder.javatriggers.triggers.base.TriggerBase;
import net.cursedmodder.javatriggers.triggers.songs.Song;
import net.cursedmodder.javatriggers.util.debug.watch.DebugUI;

public class RainTriggerExample extends TriggerBase {
    public RainTriggerExample() {
        super(3, 0, 100, true, 1F, 0.5F, TriggerSongs.rainSongs().toArray(new Song[0]));
        canBeInterrupted = true;
        setPauseVolumePercentage(0);
    }

    @Override
    public boolean canPlay() {
        return ClientContext.isRaining() && super.canPlay();
    }

}
