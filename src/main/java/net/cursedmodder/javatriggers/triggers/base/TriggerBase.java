package net.cursedmodder.javatriggers.triggers.base;

import net.cursedmodder.javatriggers.triggers.songs.Song;
import net.cursedmodder.javatriggers.triggers.songs.SongList;
import net.cursedmodder.javatriggers.util.debug.AudioLogger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public abstract class TriggerBase implements Comparable<TriggerBase> {
    private TriggerState state = TriggerState.IDLE;
    //Your changeable variables
    protected int priority;
    protected int timeTillActive = 0; //Adds time before the trigger can play
    protected int timeTillDeactivate = 0;
    protected boolean underWaterEffect = true;
    public float baseVolume = 1F; //Uh Useless?
    protected boolean canForceInterrupted = false; //Allows the trigger to interrupt any songs with a lesser priority;
    protected boolean canBeInterrupted = true;
    protected int lowPassAmount = 1500; //Default
    public int tillDeactivationCounter;
    public int timeTillActivationCounter;
    private float pauseVolumePercentage = 0.5F;
    public boolean playInBackGround = false;
    //End
    protected SongList songList;

    public TriggerBase(int Priority, int TimeTillActive, int TimeTillDeactivate, boolean UnderWaterDSP, float baseVol, float pauseVol, Song... songs) {
        priority = Priority;
        underWaterEffect = UnderWaterDSP;
        timeTillActive = TimeTillActive;
        timeTillDeactivate = TimeTillDeactivate;
        baseVolume = baseVol;
        pauseVolumePercentage = pauseVol;
        registerSongs(songs);
    }

    public TriggerBase(int Priority, Song... songs) {
        priority = Priority;
        registerSongs(songs);
    }

    public int getPriority() {
        return priority;
    }

    public float getPauseVolumePercentage() {
        return pauseVolumePercentage;
    }

    public void setTriggerState(TriggerState state) {
        AudioLogger.info("Trigger " + this.getClass().getSimpleName() + " is being set to " + state.name() + " from " + this.state.name());
        this.state = state;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public void registerSongs(Song... songs) {
        List<Song> songs1 = List.of(songs);
        songs1.forEach(song -> song.AttachTrigger(this));
        songList = new SongList(songs1, this);
    }

    protected void setPauseVolumePercentage(float volume) {
        pauseVolumePercentage = volume;
    }
    public boolean is(TriggerBase triggerBase) {
        return triggerBase == this;
    }

    public Song getSong() {
        Random random = new Random();
        if(songList.getSongs().isEmpty()) return null;
        Song song = songList.getSongs().get(random.nextInt(songList.getSongs().size()));
        int count = 0;
        int playedSongs = 0;

        while (song.hasPlayed && count <= songList.getSongs().size()) {
            count++;
            if(song.hasPlayed) playedSongs++;
            if(song.playOnce >= 1 && song.hasPlayed || !song.canSongPlay) continue;
            song = songList.getSongs().get(random.nextInt(songList.getSongs().size()));
        }

        if(songList.getSongs().size() >= playedSongs) songList.getSongs().forEach(Song::resetHasPlayed);
        return song;
    }

    public float PauseVolume() {
        //0.001f or below is paused
        return pauseVolumePercentage;
    }

    public int timeTillActive() {
        return timeTillActive;
    }

    public int getTimeTillDeactivate() {
        return timeTillDeactivate;
    }


    public float getLowPassAmount() {
        return lowPassAmount;
    }

    public boolean canPlay() {
        return getSong() != null;
    }

    public boolean canBeInterrupted() {
        if (this.isTriggerState(TriggerState.PLAYING) && canBeInterrupted) {
            return true;
        } else if (!this.isTriggerState(TriggerState.PLAYING) && !canBeInterrupted) {
            return true;
        }
        return false;
    }

    public boolean isTriggerState(TriggerState state) {
        return this.state == state;
    }

    public TriggerState triggerState() {
        return state;
    }

    public boolean canForceInterrupt() {
        return canForceInterrupted;
    }

    public boolean useUnderWaterEffect() {
        return underWaterEffect;
    }

    @Override
    public int compareTo(@NotNull TriggerBase other) {
        return Integer.compare(this.priority, other.priority);
    }
}

