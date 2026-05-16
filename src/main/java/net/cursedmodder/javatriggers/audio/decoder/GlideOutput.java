package net.cursedmodder.javatriggers.audio.decoder;

import ddf.minim.AudioOutput;
import ddf.minim.UGen;
import ddf.minim.ugens.Gain;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GlideOutput {
    private AudioOutput gain;
    private float currentValue;
    private float previousValue;
    private float targetValue;
    private int glideTime;
    private int countSinceGlide;
    private boolean gliding;
    private boolean nothingChanged;

    public GlideOutput(AudioOutput gain, float initialValue, int glideTime) {
        this.gain = gain;
        this.currentValue = initialValue;
        this.targetValue = initialValue;
        this.previousValue = initialValue;
        this.glideTime = glideTime;
        this.gliding = false;
        this.nothingChanged = true;
        startLoop();
    }

    public void changeGlide(AudioOutput gain, float initialValue, int glideTime) {
        this.gain = gain;
        this.currentValue = initialValue;
        this.targetValue = initialValue;
        this.previousValue = initialValue;
        this.glideTime = glideTime;
        this.gliding = false;
        this.nothingChanged = true;
    }

    public void setValue(int glideTime, float volume) {
        this.previousValue = currentValue;     // important!
        this.targetValue = Math.max(0.0f, Math.min(1.0f, volume));
        this.glideTime = glideTime;
        this.countSinceGlide = 0;
        this.gliding = true;
        this.nothingChanged = false;
    }

    private void startLoop() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1);

                    if (gliding) {
                        countSinceGlide += 1;

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
                        gain.setGain(db);
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Optional: instant change
    public void setImmediate(float volume) {
        this.targetValue = Math.max(0.0f, Math.min(1.0f, volume));
        this.currentValue = targetValue;
        this.previousValue = targetValue;
        this.gliding = false;

        float db = (currentValue <= 0.0001f) ? -80f : (float) (20 * Math.log10(currentValue));
        gain.setVolume(db);
    }

}
