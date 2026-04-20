package net.cursedmodder.javatriggers.audio;

import ddf.minim.AudioOutput;
import ddf.minim.Minim;
import ddf.minim.effects.LowPassFS;
import ddf.minim.ugens.FilePlayer;
import ddf.minim.ugens.Gain;
import ddf.minim.ugens.Summer;
import net.cursedmodder.javatriggers.JavaTriggers;
import net.cursedmodder.javatriggers.audio.decoder.Glide;
import net.cursedmodder.javatriggers.audio.decoder.GlideOutput;
import net.cursedmodder.javatriggers.audio.manager.AudioLayer;
import net.cursedmodder.javatriggers.audio.manager.Channel;
import net.cursedmodder.javatriggers.triggers.FoundationTriggerHandler;
import net.cursedmodder.javatriggers.triggers.base.TriggerState;
import net.cursedmodder.javatriggers.triggers.songs.Song;
import net.cursedmodder.javatriggers.triggers.songs.layer.LayeredSong;
import net.cursedmodder.javatriggers.util.debug.AudioLogger;
import net.cursedmodder.javatriggers.util.debug.watch.DebugUI;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@OnlyIn(Dist.CLIENT)
public class AudioPlayer  {
    private static int currentID;
    private final int sampleRate = 44100;
    Minim minim;
    public final int ID;
    private final Summer summer;
    private final GlideOutput masterGlider;
    private LowPassFS lpf;
    protected AudioOutput output;

    //Main Player
    public volatile FilePlayer player;
    protected List<AudioLayer> layers = new ArrayList<>();
    public Gain gain;
    private Glide glide;
    private volatile Song song;
    //Background loader
    public volatile FilePlayer queuedPlayer;
    protected volatile List<AudioLayer> queuedLayers = new ArrayList<>();
    private Glide queuedVolumeGlide;
    private Gain queuedFadeGain;
    private Song queuedSong;



    public int fadeIn;
    public int fadeOut;
    private volatile PlayerAudioStatus audioStatus;
    public boolean fading;
    private float maxVolume = 1F;
    public Channel channel;
    private boolean paused;
    public AtomicBoolean loading = new AtomicBoolean(false); //HELL YEAH! WE'RE GOING ATOMIC! Increases reliability over volatile boolean.
    private int tickCount;
    public boolean switching;
    public boolean layer;

    public AudioPlayer(int id) {
        minim = new Minim(new MinimHelper(this));
        JavaTriggers.players.add(this);
        output = minim.getLineOut(Minim.STEREO, 4096);
        masterGlider = new GlideOutput(output, 1F, 100);
        summer = new Summer();
        summer.patch(output);


        DebugUI.watch(this.getClass().getSimpleName(), "queuedSong", () -> queuedSong.getSongName());
        DebugUI.watch(this.getClass().getSimpleName(), "currentSong", () -> song.getSongName());
        DebugUI.watch(this.getClass().getSimpleName(), "switchingStatus", () -> switching);
        DebugUI.watch(this.getClass().getSimpleName(), "audioStatus", () -> this.audioStatus, Color.RED);
        //DebugUI.watch(this.getClass().getSimpleName(), "fadeTime", () -> this.fadeTime, Color.cyan);
        DebugUI.watch(this.getClass().getSimpleName(), "position", () -> (this.player.position() / 1000 + "/" + this.player.length() / 1000));
        DebugUI.watch(this.getClass().getSimpleName(), "currentVolume", () -> glide.getValue());
        DebugUI.watch(this.getClass().getSimpleName(), "isPlayerPlaying", () -> player.isPlaying());
        ID = id;
    }

    private float lastVolumeChange;


    public float getVolume() { return FoundationTriggerHandler.masterVolume; }

    public void changeSong(Song song) {
        if(switching && song == queuedSong) return;
        AudioLogger.info("Passed switching check: " + song.getSongName());
        changeToNextSong(song.getFadeIn() > 0, song);
    }

    private volatile boolean isCancelable;
    private volatile boolean cancel;

    public void continueSong(Song song) {
        AudioLogger.info("Trying to bring it back!");
        if(this.song == song && isCancelable) {
            AudioLogger.info("Bringing back currentSong");
            cancelSongChange();
            resetQueueAll();
            song.getAttachedTrigger().setTriggerState(TriggerState.PLAYING);
            playFadeIn();
        }
    }

    public void changeToNextSong(boolean fade, Song song) {
        if(switching && song != queuedSong) {
            if(isCancelable) {
                cancelSongChange();
                resetQueueAll();
            }
        }

        switching = true;
        isCancelable = true;
        if(fade) {
            //AudioLogger.info("Passed fade boolean");
            preloadNextSong(song);
            if(this.song != null && !this.song.mustFinish()) {
                this.fadeOut(false);
            }
            new Thread(() -> {
                    while (isStatus(PlayerAudioStatus.FADING_OUT) && !song.getAttachedTrigger().canForceInterrupt() || loading.get() || this.song != null && this.song.mustFinish() && this.isPlaying()) {
                        //System.out.println("Spinning");
                        if(cancel) break;
                        Thread.onSpinWait();
                    }

                    isCancelable = false;
                    if(cancel) {
                        switching = false;
                        cancel = false;
                        return;
                    }
                FoundationTriggerHandler.currentTrigger = queuedSong.getAttachedTrigger();
                    if(this.player != null) this.player.pause();
                if(this.song != null) {
                    this.song.getAttachedTrigger().setTriggerState(TriggerState.IDLE);
                    this.song.hasPlayed = true;
                }
                this.player = queuedPlayer;
                    Minecraft.getInstance().execute(() -> {

                        this.setSong(queuedSong);
                        this.lpf = new LowPassFS(20000, sampleRate);
                        this.gain = queuedFadeGain;
                        this.glide = queuedVolumeGlide;
                        player.patch(gain).patch(lpf).patch(summer);
                        AudioLogger.info("Fading in " + song.getSongName());
                        song.getAttachedTrigger().setTriggerState(TriggerState.PLAYING);
                        if(song instanceof LayeredSong) {
                            this.layers = queuedLayers;
                            this.layers.forEach(AudioLayer::sync);
                            this.layers.forEach(AudioLayer::silentPlay);
                        }
                       // if(!song.playOnce) player.loop();
                        playFadeIn();
                        resetQueue();
                    });
            }, "Audio_Switcher").start();
        } else {
            preloadNextSong(song);
            if(this.song != null && !this.song.mustFinish()) {
                this.fadeOut(false);
            }
            new Thread(() -> {
                while (isStatus(PlayerAudioStatus.FADING_OUT) && !song.getAttachedTrigger().canForceInterrupt() || loading.get() || this.song != null && this.song.mustFinish() && this.isPlaying()) {
                    if(cancel) break;
                    Thread.onSpinWait();
                }


                isCancelable = false;
                if(cancel) {
                    switching = false;
                    cancel = false;
                    return;
                }
                FoundationTriggerHandler.currentTrigger = queuedSong.getAttachedTrigger();
                if(this.player != null) this.player.pause();
                if(this.song != null) {
                    this.song.getAttachedTrigger().setTriggerState(TriggerState.IDLE);
                    this.song.hasPlayed = true;
                }
                this.player = queuedPlayer;
                Minecraft.getInstance().execute(() -> {
                    this.setSong(queuedSong);
                    this.lpf = new LowPassFS(20000, sampleRate);
                    this.gain = queuedFadeGain;
                    this.glide = queuedVolumeGlide;
                    player.patch(gain).patch(lpf).patch(summer);
                    AudioLogger.info("Playing " + song.getSongName());
                    song.getAttachedTrigger().setTriggerState(TriggerState.PLAYING);
                    if(song instanceof LayeredSong) {
                        this.layers = queuedLayers;
                        this.layers.forEach(AudioLayer::sync);
                        this.layers.forEach(AudioLayer::silentPlay);
                    }
                    // if(!song.playOnce) player.loop();
                    glide.setImmediate(maxVolume);
                    playAt(song.startTime);
                    resetQueue();
                });
            }, "Audio_Switcher").start();
        }
    }

    private void cancelSongChange() {
        AudioLogger.info("Canceling song change");
        cancel = true;
        isCancelable = false;
    }

    private void resetQueueAll() {
        if(FoundationTriggerHandler.queuedTrigger != null) {
            FoundationTriggerHandler.queuedTrigger.setTriggerState(TriggerState.IDLE);
        }
        resetQueue();
    }

    private void resetQueue() {
        resetAudioQueue();
        FoundationTriggerHandler.queuedTrigger = null;
    }

    private void resetAudioQueue() {
        switching = false;
        queuedPlayer = null;
        queuedSong = null;
        queuedFadeGain = null;
    }

    public void setSong(Song song) {
        if(song != null) {
            fadeIn = song.getFadeIn();
            fadeOut = song.getFadeOut();
            maxVolume = song.getVolume();
        }
        this.song = song;
    }
    private Thread audioQueue;
    public void preloadNextSong(Song song) {
        queuedSong = song;
        if(this.loading.get() || queuedPlayer != null) {
            audioQueue.interrupt();
            loading.compareAndSet(false, true); //Should fix thread de sync
            audioQueue = null;
            resetAudioQueue();
        }
        loading.compareAndSet(false, true);

        audioQueue = new Thread(() -> {
            AudioLogger.info("Queueing song " + song.getSongName() + " for " + song.getAttachedTrigger());
            queuedPlayer = new FilePlayer(minim.loadFileStream(song.getSongName()));
            queuedFadeGain = new Gain(-80);
            queuedVolumeGlide = new Glide(queuedFadeGain, 0F, 50);
            if(queuedSong instanceof LayeredSong song1) {
               queuedLayers = channel.createNewAudioLayers(song1.getLayers(), song1, this);
            }
            queuedPlayer.pause();
            loading.compareAndSet(true, false);
        }, "Audio_Queue");
        audioQueue.start();
        audioQueue = null;
    }


    public double getPosition() {
        return player.position();
    }
    public boolean isPaused() {
        return player.isPaused();
    }
    public AudioOutput getOutput() {
        return this.output;
    }
    public Summer getAudioMixer() {
        return this.summer;
    }
    public Song getSong() {
        return song;
    }
    public boolean isStatus(PlayerAudioStatus status) {
        if(status == PlayerAudioStatus.PLAYING && this.audioStatus == PlayerAudioStatus.FADING_IN || this.audioStatus == PlayerAudioStatus.FADING_OUT) {
            return true;
        }
        return status == this.audioStatus;
    }

    public void play() {
        if(player != null) {
            if (isPaused()) {
                AudioLogger.info("Resuming Player!");
                paused = false;
                player.play();
                this.layers.forEach(AudioLayer::silentPlay);
            } else if (!player.isPlaying()) {
                if(queuedPlayer == null) {
                    AudioLogger.info("Starting player");
                    player.play();
                    this.layers.forEach(AudioLayer::silentPlay);
                }
            }
        }
    }

    public void pause(int loggingID) {
        if(player != null) {
            paused = true;
            switch (loggingID) {
               case 1 -> AudioLogger.info("Pausing player for layer");
                case 2 ->  { }//Menu Pause
                default -> AudioLogger.info("Pausing player");
            }
        }
    }

    public void playAt(int millis) {
        if(player != null) {
            if (isPaused()) {
                AudioLogger.info("Resuming Player!");
                paused = false;
                player.play(millis);
                this.layers.forEach(AudioLayer::silentPlay);
            } else if (!player.isPlaying()) {
                if(queuedPlayer == null) {
                    AudioLogger.info("Starting player");
                    this.player.play(millis);
                    this.layers.forEach(AudioLayer::silentPlay);
                }
            }
        }
    }

    public boolean isPlaying() {
        if(loading.get()) return true;
        if(player != null) {
            if(player.isPaused()) return true;
            return player.isPlaying();
        } else AudioLogger.warn("Player is null");
        return false;
    }

    public void setVolume(float volume) {
        masterGlider.setValue(100, volume);
    }

    public void setAudioStatus(PlayerAudioStatus status) {
        audioStatus = status;
    }

    public void adjustLowPass(float value) {
        if(player != null && lpf != null) {
            if (value != lpf.frequency()) {
                lpf.setFreq(value);
                layers.forEach((l) -> {
                    l.lpf.setFreq(value);
                });
            }
        }
    }

    public void cue(int millis) {
        layers.forEach((l) -> {
            l.sync(millis);
        });
        this.player.cue(millis);
        this.glide.setImmediate(0F);
        this.player.pause();
    }

    private int fadeTime;

    public void tick() {
        // Pause audio detection
        layers.forEach(AudioLayer::tick);
        if(glide != null) {
            if(glide.fadingIn()) {
                this.setAudioStatus(PlayerAudioStatus.FADING_IN);
            } else if(glide.fadingOut()) {
                this.setAudioStatus(PlayerAudioStatus.FADING_OUT);
            }
        }

        if(paused) {
            tickCount++;
            masterGlider.setValue(1000, 0);
            if(tickCount == 20 && song.getAttachedTrigger().PauseVolume() <= 0.001f) {
                player.pause();
            }
        } else {
            tickCount = 0;
        }

        if(isStatus(PlayerAudioStatus.FADING_OUT)) {
            fadeTime--;
            if(glide.getValue() <= glide.getTarget()) {
                this.setAudioStatus(PlayerAudioStatus.IDLE);
            }
        } else if(this.isStatus(PlayerAudioStatus.FADING_IN)) {
            fadeTime--;
            if(glide.getValue() >= glide.getTarget()) {
                this.setAudioStatus(PlayerAudioStatus.PLAYING);
            }
        }

        if(FoundationTriggerHandler.masterVolume != lastVolumeChange && masterGlider != null) {
            masterGlider.setValue(1000, (FoundationTriggerHandler.masterVolume));
            lastVolumeChange = FoundationTriggerHandler.masterVolume;
        }

        if(this.player == null) return;
        if(!this.player.isPlaying() && !Minecraft.getInstance().isPaused() && !paused && !switching) {
            song.hasPlayed = true;
            Song song1 = this.getSong().getAttachedTrigger().getSong();
            if(song1 != this.getSong()) {
                this.changeSong(song1);
            } else if(this.getSong().playOnce <= 0) {
                this.cue(song.startTime);
                this.playFadeIn();
            }
        }

    }

    public boolean gainIsActive() {
        return gain != null;
    }

    public static int generateNewID() {
       // AudioLogger.info("New ID generated");
        return currentID++;
    }


    public void playFadeIn() {
        playAt(song.startTime);
        this.setAudioStatus(PlayerAudioStatus.FADING_IN);
        if(!glide.fading()) {
            fadeTime = fadeIn + 10;
            glide.setValue(this.fadeIn * 50, maxVolume);
        } else {
            float current = glide.getValue();     // current volume
            float progress = current / maxVolume;  // 0 → 1

            int remainingTime = (int) (fadeIn * progress * 50);


            glide.setValue(remainingTime, maxVolume);


           // glide.setValue();
        }
        //glide.setValue(this.fadeIn * 50, maxVolume);
    }

    public void fadeOut(boolean layer) {
        if(this.isStatus(PlayerAudioStatus.FADING_OUT)) {
            AudioLogger.info("Player is fading out Already");
            return;
        }

        if(player == null || glide == null) {
            AudioLogger.info("Player is null!");
            return;
        }

        AudioLogger.info("Fading out " + song.getSongName());

        this.setAudioStatus(PlayerAudioStatus.FADING_OUT);

        if(!glide.fading()) {
            this.fadeTime = fadeOut + 10;
            this.glide.setValue(this.fadeOut * 50, 0F);
            this.layers.forEach(AudioLayer::fadeOut);
        } else {
            float current = glide.getValue();     // current volume
            float progress = 1 - (current / maxVolume);  // 0 → 1

            int remainingTime = (int) (fadeIn * progress * 50);

            glide.setValue(remainingTime, 0);
            layers.forEach(AudioLayer::fadeOut);
        }

        if(layer) {
            resetAudioQueue();
            switching = true;
            new Thread(() -> {
                while (this.glide.getValue() > 0) {
                    if(!switching) {
                        return;
                    }
                    Thread.onSpinWait();
                }
                FoundationTriggerHandler.currentTrigger.setTriggerState(TriggerState.IDLE);
                FoundationTriggerHandler.currentTrigger = null;
                setSong(null);
                this.setAudioStatus(PlayerAudioStatus.IDLE);
                switching = false;
                player.close();
                player = null;
            }, "Audio_Stop_Thread").start();
        }
    }


}


