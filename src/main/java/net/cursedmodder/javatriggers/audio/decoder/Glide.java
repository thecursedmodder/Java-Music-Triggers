package net.cursedmodder.javatriggers.audio.decoder;

import ddf.minim.AudioPlayer;
import ddf.minim.UGen;
import ddf.minim.ugens.Gain;
import net.cursedmodder.javatriggers.JavaTriggers;
import net.cursedmodder.javatriggers.util.debug.AudioLogger;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
public class Glide extends UGen {

    public UGenInput target;

    private float currentValue;
    private float previousValue;
    private float targetValue;

    // glide duration in seconds
    private float glideTime = 0.05f;

    // sample tracking
    private int glideSamples;
    private int currentSample;

    private boolean gliding;

    public Glide(float initialValue, float glideTimeSeconds) {
        super();

        initialValue = clamp(initialValue);

        this.currentValue = initialValue;
        this.previousValue = initialValue;
        this.targetValue = initialValue;

        this.glideTime = glideTimeSeconds;

        target = new UGen.UGenInput(InputType.CONTROL);
        target.setLastValue(initialValue);

        recalcSamples();
    }

    public Glide(Gain gain, float initialValue, float glideTimeSeconds) {
        super();

        initialValue = clamp(initialValue);

        this.currentValue = initialValue;
        this.previousValue = initialValue;
        this.targetValue = initialValue;

        this.glideTime = glideTimeSeconds;

        target = new UGen.UGenInput(InputType.CONTROL);
        target.setLastValue(initialValue);

        recalcSamples();
    }

    public Glide(float initialValue) {
        this(initialValue, 0.05f);
    }

    private void recalcSamples() {
        glideSamples = Math.max(1,
                (int)(glideTime * sampleRate()));
    }

    @Override
    protected void sampleRateChanged() {
        recalcSamples();
    }

    public void setGlideTime(float seconds) {
        glideTime = Math.max(0.001f, seconds);
        recalcSamples();
    }

    public float getTarget() {
        return targetValue;
    }

    public void setTarget(float value) {
        value = clamp(value);
        if(value == currentValue) return;
        AudioLogger.info("Setting target");
        previousValue = currentValue;
        targetValue = value;

        currentSample = 0;
        gliding = true;
    }

    public void setValue(float time, float volume) {
        setGlideTime(time / 1000);
        setTarget(volume);
    }

    public void discard() {
        //Dummy
    }

    public void setImmediate(float value) {
        value = clamp(value);

        currentValue = value;
        previousValue = value;
        targetValue = value;

        gliding = false;
    }

    public float getValue() {
        return currentValue;
    }

    private float clamp(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    @Override
    protected void uGenerate(float[] channels) {

        if (gliding) {

            float progress =
                    (float) currentSample / glideSamples;

            progress = Math.min(progress, 1.0f);

            // smoothstep curve
            float curved =
                    progress * progress *
                            (3f - 2f * progress);

            currentValue =
                    previousValue +
                            (targetValue - previousValue) * curved;

            currentSample++;

            // finish glide
            if (currentSample >= glideSamples) {
                currentValue = targetValue;
                gliding = false;
            }
        }

        // convert linear -> dB
        float db =
                (currentValue <= 0.00001f)
                        ? -80f
                        : (float)(20.0 * Math.log10(currentValue));

        Arrays.fill(channels, db);
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
}