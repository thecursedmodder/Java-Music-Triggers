package net.cursedmodder.javatriggers.triggers.example;

import net.cursedmodder.javatriggers.util.ClientContext;
import net.cursedmodder.javatriggers.triggers.base.TriggerBase;
import net.cursedmodder.javatriggers.triggers.songs.Song;
import net.cursedmodder.javatriggers.util.debug.watch.DebugUI;

public class NightTriggerExample extends TriggerBase {
    public NightTriggerExample() {
        super(2, 0, 0, true, 1F, 0.4F, TriggerSongs.nightSongs().toArray(new Song[0]));
    }

    @Override
    public boolean canPlay() {
        return ClientContext.isNight() && super.canPlay();
    }
}
