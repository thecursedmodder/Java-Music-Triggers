package net.cursedmodder.javatriggers.audio.decoder;

import ddf.minim.AudioPlayer;
import ddf.minim.ugens.Gain;
import net.cursedmodder.javatriggers.JavaTriggers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Glide {
    private Gain gain;
    private float currentValue;
    private float previousValue;
    private float targetValue;
    private int glideTime;
    private int countSinceGlide;
    private boolean gliding;
    private boolean nothingChanged;
    private volatile boolean discarded;

    public Glide(Gain gain, float initialValue, int glideTime) {
        this.gain = gain;
        this.currentValue = initialValue;
        this.targetValue = initialValue;
        this.previousValue = initialValue;
        this.glideTime = glideTime;
        this.gliding = false;
        this.nothingChanged = true;
        startLoop();
    }

    public void changeGlide(Gain gain, float initialValue, int glideTime) {
        this.gain = gain;
        this.currentValue = initialValue;
        this.targetValue = initialValue;
        this.previousValue = initialValue;
        this.glideTime = glideTime;
        this.gliding = false;
        this.nothingChanged = true;
    }

    public void setValue(int glideTime, float volume) {
        //JavaTriggers.LOGGER.info("Value changed! GlideTime: " + glideTime + " Volume: " + volume);
        this.previousValue = currentValue;     // important!
        this.targetValue = Math.max(0.0f, Math.min(1.1f, volume));
        this.glideTime = glideTime;
        this.countSinceGlide = 0;
        this.gliding = true;
        this.nothingChanged = false;
    }

    private void startLoop() {
        new Thread(() -> {
            while (!discarded) {
                try {
                    Thread.sleep(10);

                    if (gliding) {
                        ///JavaTriggers.LOGGER.info("Gliding volume");
                        countSinceGlide += 10;

                        float progress = Math.min(1.0f, (float) countSinceGlide / glideTime);

                        // Proper lerp: move from previousValue toward targetValue
                        currentValue = previousValue + (targetValue - previousValue) * progress;

                        // Stop when close enough
                        if (progress >= 1.0f || Math.abs(currentValue - targetValue) < 0.001f) {
                            currentValue = targetValue;
                            gliding = false;
                            nothingChanged = true;
                        }

                        // Apply to Minim Gain (linear → dB)
                        float db = (currentValue <= 0.0001f) ? -80f : (float) (20 * Math.log10(currentValue));
                        gain.setValue(db);
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Auto_Glide").start();
    }

    // Optional: instant change
    public void setImmediate(float volume) {
        this.targetValue = Math.max(0.0f, Math.min(1.0f, volume));
        this.currentValue = targetValue;
        this.previousValue = targetValue;
        this.gliding = false;

        float db = (currentValue <= 0.0001f) ? -80f : (float) (20 * Math.log10(currentValue));
        gain.setValue(db);
    }
    public float getTarget() {
        return targetValue;
    }

    public boolean fading() {
        return gliding;
    }

    public boolean fadingIn() {
        return this.targetValue > currentValue && gliding;
    }

    public boolean fadingOut() {
        return this.targetValue < currentValue && gliding;
    }

    public float getValue() {
        return currentValue;
    }

    public void silentSet(float volume) {
        this.targetValue = Math.max(0.0f, Math.min(1.0f, volume));
    }

    public void discard() {
        discarded = true;
    }
}
