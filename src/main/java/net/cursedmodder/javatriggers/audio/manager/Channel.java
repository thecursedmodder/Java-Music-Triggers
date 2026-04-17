package net.cursedmodder.javatriggers.audio.manager;

import ddf.minim.effects.LowPassFS;
import net.cursedmodder.javatriggers.audio.AudioPlayer;
import net.cursedmodder.javatriggers.triggers.base.TriggerBase;
import net.cursedmodder.javatriggers.triggers.base.TriggerState;
import net.cursedmodder.javatriggers.triggers.songs.Song;
import net.cursedmodder.javatriggers.triggers.songs.layer.LayerCondition;
import net.cursedmodder.javatriggers.triggers.songs.layer.LayeredSong;
import net.cursedmodder.javatriggers.util.debug.AudioLogger;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class Channel implements Runnable {
    public volatile AudioPlayer audioPlayer;
    public List<AudioLayer> layers = new ArrayList<>();
    public int songLayerCount; //Starts from 1
    private Song song;


    public Channel() {
        this.audioPlayer = new AudioPlayer(AudioPlayer.generateNewID());
        this.audioPlayer.channel = this;
    }

    public Song getSong() {
        return this.song;
    }

    public void tick() {
        audioPlayer.tick();
    }

    public void setAudio(Song song) {
        new Thread(() -> {
            AudioLogger.info("Passed Loading Check");

            audioPlayer.changeSong(song);

            this.song = song;
            //player.playFadeIn(layers);
        }, "Audio_Channel").start();
    }

    public void setAudio(Song song, TriggerBase trigger) {
        audioPlayer.changeSong(song);
        AudioLogger.info("Passed Loading Check");
        this.song = song;
    }

    public void swapAudio(AudioPlayer player) {
        this.audioPlayer.player = player.player;
        player.player = null;
    }

    public void continueSong(Song song) {
        //TODO This isn't ever activated when needed
        if(audioPlayer.getSong() != null && audioPlayer.getSong() == song && song.getAttachedTrigger().isTriggerState(TriggerState.FADING_OUT)) {
            audioPlayer.continueSong(song);
        } else AudioLogger.warn("Failed to continue song");
    }

    public void playAudioLayer(int layer) {
        AudioLayer layer1 = getAudioLayer(layer);
        layer1.player.playFadeIn();
    }

    private AudioLayer getAudioLayer(int layer) {
        for(AudioLayer layer1 : layers) {
            if(layer1.getLayerNumber() == layer) {
                return layer1;
            }
        }
        return null;
    }

    public List<AudioLayer> createNewAudioLayers(List<LayerCondition> condition, LayeredSong song, AudioPlayer player) {
        List<AudioLayer> audioLayers = new ArrayList<>();
        for (LayerCondition c : condition) {
            AudioLayer audioLayer = new AudioLayer(c, new LowPassFS(20000, 44100), player, this, c.ID);
           audioLayers.add(audioLayer);
        }
        return audioLayers;
    }

    @Override
    public void run() {

    }

}
