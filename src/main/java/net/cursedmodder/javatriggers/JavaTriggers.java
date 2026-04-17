package net.cursedmodder.javatriggers;

import com.mojang.logging.LogUtils;
import net.cursedmodder.javatriggers.audio.AudioPlayer;
import net.cursedmodder.javatriggers.triggers.FoundationTriggerHandler;
import net.cursedmodder.javatriggers.network.TriggerMessages;
import net.cursedmodder.javatriggers.triggers.MobTriggerZombie;
import net.cursedmodder.javatriggers.triggers.base.TriggerBase;
import net.cursedmodder.javatriggers.triggers.example.*;
import net.cursedmodder.javatriggers.util.debug.ModLogger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(JavaTriggers.MODID)
public class JavaTriggers {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "javatriggers";
    public static List<AudioPlayer> players = new ArrayList<>();
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static boolean TriggerDebugScreen = false; //If enabled, it will pop up a separate window with realTime information on variables.

    public JavaTriggers() {
        ModLogger.setupCustomLogger(MODID);
        TriggerMessages.register();
        Config.register();
        //By default, the only trigger that has songs is General as an example;
        //There are few different ways of registering triggers, or simply put adding triggers. You can add them to a List and then register the list as seen bellow.
        List<TriggerBase> triggers = new ArrayList<>();
        //triggers.add(new Rain());
        //triggers.add(new General());
        //triggers.add(new Menu());
        //triggers.add(new Night());
        //triggers.add(new Death());
        //FoundationTriggerHandler.registerTriggers(triggers);

        //Or you can register the triggers individually
        //FoundationTriggerHandler.registerTrigger(new MobTriggerZombie());

        MinecraftForge.EVENT_BUS.register(this);
    }




}
