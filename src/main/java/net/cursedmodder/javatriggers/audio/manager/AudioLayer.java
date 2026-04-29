package net.cursedmodder.javatriggers.audio.manager;

import ddf.minim.AudioOutput;
import ddf.minim.Minim;
import ddf.minim.effects.LowPassFS;
import ddf.minim.ugens.FilePlayer;
import ddf.minim.ugens.Gain;
import ddf.minim.ugens.Summer;
import net.cursedmodder.javatriggers.audio.AudioPlayer;
import net.cursedmodder.javatriggers.audio.MinimHelper;
import net.cursedmodder.javatriggers.audio.PlayerAudioStatus;
import net.cursedmodder.javatriggers.audio.decoder.Glide;
import net.cursedmodder.javatriggers.triggers.songs.layer.LayerCondition;
import net.cursedmodder.javatriggers.triggers.songs.layer.LayeredSong;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.File;

@OnlyIn(Dist.CLIENT)
public class AudioLayer {
    private Channel channel;
    public AudioPlayer player;
    private int layerNumber;

    Minim minim;
    public FilePlayer filePlayer;
    private LayerCondition condition;
    private Gain gain;
    private Glide glide;
    private Summer summer;
    public LowPassFS lpf;

    public AudioLayer(LayerCondition condition, LowPassFS lpf, AudioPlayer player, Channel channel, int layerNumber) {
        this.player = player;
        this.channel = channel;
        this.layerNumber = layerNumber;
        this.minim = new Minim(new MinimHelper(this));
        this.filePlayer = new FilePlayer(minim.loadFileStream(condition.mainSong));
        this.gain = new Gain(-80);
        this.glide = new Glide(gain, 0f, 50);
        this.summer = player.getAudioMixer();
        this.condition = condition;
        this.lpf = lpf;
        this.filePlayer.patch(gain).patch(lpf).patch(summer);
        //if(!player.getSong().playOnce) filePlayer.loop();
        this.filePlayer.pause();
    }

    public void tick() {
        if(player.getSong() != null && player.getSong() instanceof LayeredSong song && !player.isStatus(PlayerAudioStatus.FADING_OUT)) {
            this.condition.tick();
            if(condition.canPlayLayer()) {
                glide.setValue(condition.fadeIn * 50, condition.maxVolume());
            } else if(!player.isStatus(PlayerAudioStatus.FADING_OUT)) {
                glide.setValue(condition.fadeOut * 50, 0F);
            }
        }
    }

    public void sync() {
        filePlayer.pause();
        filePlayer.cue(player.getSong().startTime);
    }

    public void sync(int millis) {
        filePlayer.pause();
        filePlayer.cue(millis);
    }

    public void resume() {
        filePlayer.play();
    }

    public void fadeOut() {
        glide.setValue(condition.fadeOut * 50, 0F);
    }

    public void silentPlay() {
        glide.setImmediate(0f);
        filePlayer.play();
    }

    public int getLayerNumber() {
        return layerNumber;
    }

    public Channel getBoundedChannel() {
        return channel;
    }
}
