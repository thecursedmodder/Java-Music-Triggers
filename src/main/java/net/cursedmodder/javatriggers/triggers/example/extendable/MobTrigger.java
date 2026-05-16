package net.cursedmodder.javatriggers.triggers.example.extendable;

import net.cursedmodder.javatriggers.util.ClientContext;
import net.cursedmodder.javatriggers.triggers.base.TriggerBase;
import net.cursedmodder.javatriggers.triggers.songs.Song;
import net.cursedmodder.javatriggers.util.GeneralConversionUtil;
import net.cursedmodder.javatriggers.util.debug.watch.DebugUI;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MobTrigger extends TriggerBase {
    public int minCount = 30;
    public int winCount = 15;
    public int maxCount = Integer.MAX_VALUE;
    public int radius = 30;
    public int height = 20;
    public boolean mustTarget = true;
    public boolean canTargetAnyone = true;
    public int currentMobCount;
    protected boolean hasWon;
    public Set<String> allowedMobs;

    public MobTrigger(int Priority, Song[] songs, String... allowedMobs) {
        super(Priority, songs);
        this.allowedMobs = Set.of(allowedMobs);
        DebugUI.watch(this.getClass(), "mobCount", () -> {
            return this.currentMobCount;
        });
    }

    public MobTrigger(int Priority, int TimeTillActive, int TimeTillDeactivate, boolean UnderWaterDSP, float baseVol, float pauseVol, Song[] songs, String... allowedMobs) {
        super(Priority, TimeTillActive, TimeTillDeactivate, UnderWaterDSP, baseVol, pauseVol, songs);
        this.allowedMobs = Set.of(allowedMobs);
        DebugUI.watch(this.getClass(), "mobCount", () -> {
            return this.currentMobCount;
        });
    }

    public void setWinPercentage(float percentage) {
        if(percentage > 1f) {
            winCount = minCount;
            return;
        }
        winCount = (int) (minCount * percentage);
    }

    public boolean canPlay() {
        if (Minecraft.getInstance().player == null) {
            return false;
        } else {
            List<Mob> mobs = ClientContext.getMobsInAreaAroundPlayer((float)this.radius, (float)this.height);
            if (!this.allowedMobs.isEmpty()) {
                mobs = mobs.stream().filter((mob) -> {
                    return this.allowedMobs.contains(GeneralConversionUtil.getEntityStringId(mob));
                }).collect(Collectors.toList());
            }

            if (this.mustTarget) {
                if (!this.canTargetAnyone) {
                    mobs.removeIf((mob1) -> {
                        return mob1.getTarget() != Minecraft.getInstance().player;
                    });
                } else {
                    mobs.removeIf((mob1) -> {
                        return !(mob1.getTarget() instanceof Player);
                    });
                }
            }

            this.currentMobCount = mobs.size();
            if(mobs.size() >= this.minCount) {
                hasWon = true;
            }

            if(mobs.size() <= this.winCount) {
                hasWon = false;
            }

            return hasWon && mobs.size() <= this.maxCount && super.canPlay();
        }
    }
}
