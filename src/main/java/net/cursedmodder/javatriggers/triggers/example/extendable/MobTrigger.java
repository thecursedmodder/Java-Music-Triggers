package net.cursedmodder.javatriggers.triggers.example.extendable;

import net.cursedmodder.javatriggers.util.ClientContext;
import net.cursedmodder.javatriggers.triggers.base.TriggerBase;
import net.cursedmodder.javatriggers.triggers.songs.Song;
import net.cursedmodder.javatriggers.util.GeneralConversionUtil;
import net.cursedmodder.javatriggers.util.debug.watch.DebugUI;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MobTrigger extends TriggerBase {
    public int minCount = 30;
    public int maxCount = Integer.MAX_VALUE;
    public int radius = 30;
    public int height = 20;
    public boolean mustTarget = true;
    public boolean canTargetAnyone = true;
    public int currentMobCount;
    public Set<String> allowedMobs;

    public MobTrigger(int Priority, Song[] songs, String... allowedMobs) {
        super(Priority, songs);
        this.allowedMobs = Set.of(allowedMobs);
        DebugUI.watch(this.getClass(), "mobCount", () -> currentMobCount);
    }

    public MobTrigger(int Priority, int TimeTillActive, int TimeTillDeactivate, boolean UnderWaterDSP, float baseVol, float pauseVol, Song[] songs, String... allowedMobs) {
        super(Priority, TimeTillActive, TimeTillDeactivate, UnderWaterDSP, baseVol, pauseVol, songs);
        this.allowedMobs = Set.of(allowedMobs);
        DebugUI.watch(this.getClass(), "mobCount", () -> currentMobCount);
    }

    //TODO: add mob nbt checks
    @Override
    public boolean canPlay() {
        if(Minecraft.getInstance().player == null) return false;
        List<net.minecraft.world.entity.Mob> mobs = ClientContext.getMobsInAreaAroundPlayer(radius, height);
        if(!allowedMobs.isEmpty())  {
            mobs = mobs.stream()
                    .filter(mob -> allowedMobs.contains(GeneralConversionUtil.getEntityStringId(mob)))
                    .collect(Collectors.toList());
        }

        if(mustTarget) {
            if(!canTargetAnyone) {
                mobs.removeIf(mob1 -> mob1.getTarget() != Minecraft.getInstance().player);
            } else {
                mobs.removeIf(mob1 -> !(mob1.getTarget() instanceof Player));
            }
        }
        currentMobCount = mobs.size();
        if(mobs.size() >= minCount && mobs.size() <= maxCount) {
            return super.canPlay();
        }
        return false;
    }
}
