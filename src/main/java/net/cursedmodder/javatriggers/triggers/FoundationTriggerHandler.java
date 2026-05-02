package net.cursedmodder.javatriggers.triggers;

import net.cursedmodder.javatriggers.Config;
import net.cursedmodder.javatriggers.audio.PlayerAudioStatus;
import net.cursedmodder.javatriggers.audio.manager.Channel;
import net.cursedmodder.javatriggers.triggers.base.TriggerBase;
import net.cursedmodder.javatriggers.triggers.base.TriggerState;
import net.cursedmodder.javatriggers.triggers.songs.Song;
import net.cursedmodder.javatriggers.util.debug.AudioLogger;
import net.cursedmodder.javatriggers.util.debug.watch.DebugUI;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class FoundationTriggerHandler {
    public static List<TriggerBase> list = new ArrayList<>();
    public static final Channel channel1 = new Channel();
    public volatile static TriggerBase currentTrigger;
    public static TriggerBase queuedTrigger;
    public static float masterVolume = 1F;
    private static Song Song;
    private static int tickCount;

    public static void registerTriggers(List<TriggerBase> List) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            list.addAll(List);
            DebugUI.watch(FoundationTriggerHandler.class.getSimpleName(), "queuedTrigger", () -> queuedTrigger.getName());
            DebugUI.watch(FoundationTriggerHandler.class.getSimpleName(), "currentTrigger", () -> currentTrigger.getName());
        });
    }

    public static void registerTrigger(TriggerBase trigger) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            list.add(trigger);
            DebugUI.watch(FoundationTriggerHandler.class.getSimpleName(), "queuedTrigger", () -> queuedTrigger.getName());
            DebugUI.watch(FoundationTriggerHandler.class.getSimpleName(), "currentTrigger", () -> currentTrigger.getName());
        });
    }

    public static TriggerBase selectBestPlayableTrigger(List<TriggerBase> triggers) {
        if (triggers == null || triggers.isEmpty()) {
            return null;
        }
        return triggers.stream()
                .filter(TriggerBase::canPlay)
                .max(Comparator.naturalOrder())
                .orElse(null);                     // if none can play → silence / fallback
    }

    public static void tick() {
        //Compare priorities
        channel1.tick();
        list.forEach(TriggerBase::tick);
        tickCount++;
        if (tickCount % Config.TPS.get() != 0) return;
        TriggerBase trigger = selectBestPlayableTrigger(list);
        if (trigger != null) {
            if (trigger.isTriggerState(TriggerState.IDLE) && trigger.canPlay() && !trigger.is(currentTrigger)) {
                if (!isCurrentTriggerInterruptible(trigger)) return;

                trigger.setTriggerState(TriggerState.QUEUED);
                if (queuedTrigger != null) {
                    trigger.timeTillActivationCounter = 0;
                }
                queuedTrigger = trigger;
            }
            //TODO fix the if statement hell
            if (trigger.isTriggerState(TriggerState.QUEUED)) {
                if (currentTrigger != null && !trigger.canForceInterrupt()) {
                    if (currentTrigger.tillDeactivationCounter++ >= currentTrigger.getTimeTillDeactivate()) {
                        if (trigger.timeTillActivationCounter++ >= trigger.timeTillActive()) {
                            if (currentTrigger == null || currentTrigger.canBeInterrupted()) {
                                currentTrigger.tillDeactivationCounter = 0;
                                setSong(channel1, trigger);
                                trigger.timeTillActivationCounter = 0;
                            }
                        }
                    }
                } else {
                    if (trigger.timeTillActivationCounter++ >= trigger.timeTillActive()) {
                        if (currentTrigger == null || currentTrigger.canBeInterrupted()) {
                            setSong(channel1, trigger);
                            trigger.timeTillActivationCounter = 0;
                        }
                    }
                }
            }
        } else queuedTrigger = null;

        if (currentTrigger != null) {
            if (currentTrigger.isTriggerState(TriggerState.PLAYING)) {
                if (channel1.audioPlayer.getSong() == null) {
                    return;
                }

                //
                if (currentTrigger.canPlay() && !channel1.audioPlayer.isPaused())
                    if (!channel1.audioPlayer.isPlaying()) {
                        setSong(channel1, currentTrigger);
                    }

                //Fade-out if current trigger cannot play and there is no replacement.
                if (!currentTrigger.canPlay() && queuedTrigger == null) {
                    if (currentTrigger != null) {
                        if (currentTrigger.tillDeactivationCounter++ >= currentTrigger.getTimeTillDeactivate()) {
                            if (currentTrigger == null || currentTrigger.canBeInterrupted()) {
                                currentTrigger.tillDeactivationCounter = 0;
                                channel1.audioPlayer.fadeOut(true);
                            }
                        }
                    }
                }
            }

            //Continue a fading out song if the trigger is playable again
            if (currentTrigger.isTriggerState(TriggerState.FADING_OUT)) {
                if (currentTrigger.canPlay() && isLowerPriority(trigger) || trigger != null && !trigger.canPlay()) {
                    channel1.continueSong(channel1.audioPlayer.getSong());
                }
            }
        }
        if (channel1.audioPlayer != null && Minecraft.getInstance().level == null) {
            channel1.audioPlayer.adjustLowPass(20000);
        }
        Minecraft mc = Minecraft.getInstance();
        audioSettingTick(mc);
    }

    private static void audioSettingTick(Minecraft mc) {
        if (channel1.audioPlayer != null && channel1.getSong() != null && !channel1.audioPlayer.isStatus(PlayerAudioStatus.FADING)) {
            if (mc.isPaused()) {
                float pauseVolume = channel1.getSong().getAttachedTrigger().getPauseVolumePercentage();
                if (pauseVolume > 0) {
                    masterVolume = (channel1.getSong().getVolume() * mc.options.getSoundSourceVolume(SoundSource.MUSIC) * pauseVolume * mc.options.getSoundSourceVolume(SoundSource.MASTER));
                } else {
                    masterVolume = 0;
                    channel1.audioPlayer.pause(2);
                }
            } else {
                if (channel1.getSong().getAttachedTrigger().playInBackGround && !Minecraft.getInstance().isWindowActive()) {
                    channel1.audioPlayer.pause(2);
                } else channel1.audioPlayer.play();
                masterVolume = mc.options.getSoundSourceVolume(SoundSource.MUSIC) * mc.options.getSoundSourceVolume(SoundSource.MASTER);
            }

        }

    }

    private static boolean isLowerPriority(TriggerBase trigger) {
        if(trigger == null) return true;
        return trigger.getPriority() <= currentTrigger.getPriority();
    }

    private static void setSong(Channel channel, TriggerBase trigger) {
        Song = trigger.getSong();
        if(Song == null) return;
        trigger.setTriggerState(TriggerState.LOADED);
        channel.setAudio(Song, trigger);
    }

    private static boolean isCurrentTriggerInterruptible(TriggerBase base) {
        if (currentTrigger == null) return true;
        if (currentTrigger.isTriggerState(TriggerState.IDLE)) {
            AudioLogger.info(base.getName() + " is replacing " + currentTrigger.getName());
            return true;
        } else if (!currentTrigger.getSong().mustFinish()) {
            return currentTrigger.canBeInterrupted() || base.canForceInterrupt() && !currentTrigger.canForceInterrupt();
        }
        return false;
    }

    private static boolean canInterruptCurrentTriggerFadeOut(TriggerBase base) {
        if (currentTrigger == null) return true;
        return base.canForceInterrupt() && !currentTrigger.canBeInterrupted();
    }

    public static void gameTick(Player level) {
        Song song = channel1.audioPlayer.getSong();
        if (level.isUnderWater()) {
            if (song != null && song.getAttachedTrigger().useUnderWaterEffect()) {
                channel1.audioPlayer.adjustLowPass(song.getAttachedTrigger().getLowPassAmount());
            }
        } else channel1.audioPlayer.adjustLowPass(20000);
    }
}
