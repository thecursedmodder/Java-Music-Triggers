package net.cursedmodder.javatriggers.triggers.songs.layer;

import ddf.minim.UGen;
import net.cursedmodder.javatriggers.audio.manager.AudioLayer;
import net.cursedmodder.javatriggers.triggers.songs.Song;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerCondition {
    public final String mainSong;
    public final int ID;
    public float volume;
    public int fadeIn;
    public int fadeOut;
    private ConditionInterface condition;
    protected Song song;
    private AudioLayer layer;

    public LayerCondition(String mainSong, String extension, int number) {
        this.mainSong = mainSong + "_layer" + number + extension;
        this.ID = number;
    }
    //TODO make this more user friendly
    public void setAudioLayer(AudioLayer layer) {
        this.layer = layer;
    }

    public AudioLayer audioLayerPlayer() {
        return layer;
    }

    public LayerCondition(String mainSong, int number, int fadeIn, int fadeOut) {
        this.mainSong = mainSong + "_layer" + number;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
        ID = number;
    }

    public LayerCondition setStats(int fadeIn, int fadeOut, float volume) {
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
        this.volume = volume;
        return this;
    }

    public LayerCondition setPlayCondition(ConditionInterface condition) {
        this.condition = condition;
        return this;
    }

    public Song getSong() {
        return song;
    }

    public float maxVolume() {
        return volume;
    }

    public void tick() {
        //Extend this class and override this method to do some advanced layer logic.
        //Example: Changing the max volume with a boolean or a numerical value
    }

    public boolean canPlayLayer() {
        return condition.playableContext();
    }

    public void attachSong(Song song) {
        this.song = song;
    }
}
