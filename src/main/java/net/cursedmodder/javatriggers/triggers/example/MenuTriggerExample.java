package net.cursedmodder.javatriggers.triggers.example;

import net.cursedmodder.javatriggers.util.ClientContext;
import net.cursedmodder.javatriggers.triggers.base.TriggerBase;
import net.cursedmodder.javatriggers.triggers.songs.Song;
import net.cursedmodder.javatriggers.util.debug.watch.DebugUI;

public class MenuTriggerExample extends TriggerBase {
    public MenuTriggerExample() {
        super(3, 100, 0, false, 1F, 1F, TriggerSongs.menuSongs().toArray(new Song[0]));
    }

    @Override
    public boolean canPlay() {
        return ClientContext.isInMenu() && super.canPlay();
    }
}
