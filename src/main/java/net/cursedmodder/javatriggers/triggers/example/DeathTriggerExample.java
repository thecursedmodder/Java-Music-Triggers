package net.cursedmodder.javatriggers.triggers.example;

import net.cursedmodder.javatriggers.util.ClientContext;
import net.cursedmodder.javatriggers.triggers.base.TriggerBase;
import net.cursedmodder.javatriggers.triggers.songs.Song;
import net.cursedmodder.javatriggers.util.debug.watch.DebugUI;

public class DeathTriggerExample extends TriggerBase {
    public DeathTriggerExample() {
        super(4, 0, 100, true, 1F, 0.4F, TriggerSongs.deathSongs().toArray(new Song[0]));

        this.canForceInterrupted = true;
    }

    @Override
    public boolean canPlay() {
        return ClientContext.isDeadOrDying() && super.canPlay();
    }
}
