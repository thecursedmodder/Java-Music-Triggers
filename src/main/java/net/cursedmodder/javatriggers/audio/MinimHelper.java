package net.cursedmodder.javatriggers.audio;

import net.cursedmodder.javatriggers.JavaTriggers;
import net.cursedmodder.javatriggers.util.debug.AudioLogger;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.InputStream;

@OnlyIn(Dist.CLIENT)
public class MinimHelper {
    private final Object parent;  // your main mod class instance

    public MinimHelper(Object parent) {
        this.parent = parent;
    }

    // Minim calls this automatically
    public InputStream createInput(String fileName) {
        // filename can be "/sounds/my_sound.mp3" (leading / = root of classpath)
        InputStream is = JavaTriggers.origin.getResourceAsStream("/assets/javatriggers/audio/" + fileName);

        if (is == null) {
            AudioLogger.error("[Minim] RESOURCE NOT FOUND: " + fileName);
            AudioLogger.error("[Minim] Make sure the file is in src/main/resources/ and copied to the JAR root (or subfolder).");
            // Optional: also try the other lookup method
            is = parent.getClass().getResourceAsStream("/" + fileName);
            if (is != null) {
                AudioLogger.error("[Minim] Found using getResourceAsStream with leading /");
            }
        } else {
            AudioLogger.info("[Minim] Successfully loaded resource: " + fileName);
        }

        //AudioLogger.debug("Path of the file " + is.toString());
        return is;
    }

    // Optional – Minim may call this for some paths
    public String sketchPath(String filename) {
        return filename;  // not really used for classpath loading
    }


}