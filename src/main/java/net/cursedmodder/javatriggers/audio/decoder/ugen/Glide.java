package net.cursedmodder.javatriggers.audio.decoder.ugen;

import ddf.minim.UGen;

import java.util.Arrays;

public class Glide extends UGen {

    /** Patch to this to set the target value (usually a control signal). */
    public UGenInput target;

    /** Controls the glide time/speed. Higher value = slower glide. */
    public UGenInput time;

    private float currentValue;
    private float oneOverSampleRate;
    private float prevTime; // to avoid recalculating unnecessarily

    public Glide(float initialValue, float glideTime) {
        super();
        initialValue = (initialValue <= 0.0001f) ? -80f : (float) (20 * Math.log10(initialValue));
        target = new UGenInput(InputType.CONTROL);
        target.setLastValue(initialValue);

        time = new UGenInput(InputType.CONTROL);
        time.setLastValue(glideTime);

        currentValue = initialValue;
        prevTime = glideTime;
    }

    public Glide(float initialValue) {
        this(initialValue, 0.1f);
    }

    public void setTarget(float newTarget) {
        newTarget = (newTarget <= 0.0001f) ? -80f : (float) (20 * Math.log10(newTarget));
        target.setLastValue(newTarget);
    }

    public void setImmediate(float value) {
        currentValue = value;
        target.setLastValue(value);
    }

    public boolean fadingIn() {
        return this.target.getLastValue() > currentValue;
    }

    public boolean fadingOut() {
        return  this.target.getLastValue() < currentValue;
    }

    public void setGlideTime(float newTime) {
        time.setLastValue(Math.max(0.001f, newTime)); // avoid zero/negative
    }

    @Override
    protected void sampleRateChanged() {
        oneOverSampleRate = 1.0f / sampleRate();
    }

    @Override
    protected void uGenerate(float[] channels) {
        float tgt = target.getLastValue();
        float glideTime = time.getLastValue();

        // Update coefficient if time changed
        if (glideTime != prevTime) {
            prevTime = glideTime;
        }

        // Exponential smoothing: coefficient approaches 1 for very fast glide
        float coeff = (glideTime > 0)
                ? (float) Math.exp(-oneOverSampleRate / glideTime)
                : 0.0f;

        // Update current value
        currentValue = currentValue * coeff + tgt * (1.0f - coeff);

        // Output to all channels
        Arrays.fill(channels, currentValue);
    }

    public float getValue() {
        return currentValue;
    }

    public void setValue(float glide, float volume) {
        setGlideTime(glide);
        setTarget(volume);
    }
}


