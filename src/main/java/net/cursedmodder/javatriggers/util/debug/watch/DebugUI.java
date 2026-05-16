package net.cursedmodder.javatriggers.util.debug.watch;

import net.cursedmodder.javatriggers.JavaTriggers;
import net.cursedmodder.javatriggers.triggers.base.TriggerBase;
import net.cursedmodder.javatriggers.util.debug.AudioLogger;

import java.awt.*;
import java.util.function.Supplier;

public final class DebugUI {
    private static DebugWindow WINDOW;

    public static void init() {
        if (GraphicsEnvironment.isHeadless()) {
            AudioLogger.warn("Headless environment detect! Disabling DEBUG_UI");
        } else {

        }

        System.setProperty("java.awt.headless", "false");
        WINDOW = new DebugWindow();
    }

    public static void addTrigger(TriggerBase triggerBase) {
        watch(triggerBase.getName(), "state", triggerBase::triggerState);
        watch(triggerBase.getName(), "status", triggerBase::triggerState);
    }

    private static void ensureInit() {

        System.setProperty("java.awt.headless", "false");
        if (WINDOW == null) {
            init();
        }
    }

    // ===== BASIC WATCH =====
    public static void watch(String group, String key, Supplier<Object> supplier) {
        watch(group, key, supplier, () -> Color.WHITE);
    }

    public static void watch(String group, String key, Supplier<Object> supplier, Color color) {
        watch(group, key, supplier, () -> color);
    }

    public static void watch(String group, String key, Supplier<Object> supplier, Supplier<Color> colorSupplier) {
        //AudioLogger.info("Ui status: " + JavaTriggers.TriggerDebugScreen);
        if(!JavaTriggers.TriggerDebugScreen) return;
        ensureInit();
        if (WINDOW == null) return;

        WINDOW.watch(group, key, supplier, colorSupplier);
    }

    // ===== CLASS GROUPING =====
    public static void watch(Class<?> clazz, String key, Supplier<Object> supplier) {
        watch(clazz.getSimpleName(), key, supplier);
    }

    public static void watch(Class<?> clazz, String key, Supplier<Object> supplier, Color color) {
        watch(clazz.getSimpleName(), key, supplier, color);
    }

    public static void watch(Class<?> clazz, String key, Supplier<Object> supplier, Supplier<Color> colorSupplier) {
        watch(clazz.getSimpleName(), key, supplier, colorSupplier);
    }

    // ===== QUICK COLOR HELPERS =====
    public static void good(String group, String key, Supplier<Object> supplier) {
        watch(group, key, supplier, () -> Color.GREEN);
    }

    public static void warn(String group, String key, Supplier<Object> supplier) {
        watch(group, key, supplier, () -> Color.ORANGE);
    }

    public static void bad(String group, String key, Supplier<Object> supplier) {
        watch(group, key, supplier, () -> Color.RED);
    }
}