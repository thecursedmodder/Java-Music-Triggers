package net.cursedmodder.javatriggers.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ClientContext {
    private static final Minecraft mc = Minecraft.getInstance();
    /* Client context that can be used in your triggers or other in other parts of your mod.
      Additionally, you won't have to worry about null values crashing the game. That's the point of the context.
     */
//Everything originates from the player and is for the player unless specified otherwise
//---------------------------------MISC STUFF------------------------------------------------
    public static boolean isInMenu() {
        Level level =  mc.level;
        return level == null;
    }
//---------------------------------PLAYER STUFF------------------------------------------------
    public static boolean isBellowHealth(float health) {
        Player player = mc.player;
        return player != null && player.getHealth() < health;
    }

    public static boolean canSeeSky() {
        Player player = mc.player;
        if(player == null) return false;
        return player.level().canSeeSky(player.blockPosition().offset(0, 1, 0));
    }

    public static boolean isArmorAtOrBeyond(int armorLevel) {
        Player player = mc.player;
        if(player == null) return false;
        return player.getArmorValue() == armorLevel;
    }

    public static boolean recentlyDamaged() {
        Player player = mc.player;
        if(player == null) return false;
        DamageSource source = player.getLastDamageSource();
        return source != null;
    }

    public static boolean isFlying() {
        Player player = mc.player;
        if(player == null) return false;
        return player.isFallFlying();
    }

    public static boolean isFalling() {
        Player player = mc.player;
        if(player == null) return false;
        return player.fallDistance >= 3;
    }

    public static boolean isUnderWater() {
        Player player = mc.player;
        if(player == null) return false;
        return player.isUnderWater();
    }

    public static boolean isSwimming() {
        Player player = mc.player;
        if(player == null) return false;
        return player.isSwimming();
    }

    public static boolean isOnFire() {
        Player player = mc.player;
        if(player == null) return false;
        return player.isOnFire();
    }

    public static boolean onGround() {
        Player player = mc.player;
        if(player == null) return false;
        return player.onGround();
    }

    public static boolean isClimbing() {
        Player player = mc.player;
        if(player == null) return false;
        return player.onClimbable();
    }

    public static boolean isSpeedAtOrBeyond(double bps) {
        Player player = mc.player;
        if(player == null) return false;
        Vec3 velocity = player.getDeltaMovement();

        double speed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);

        double blockps = speed * 20;
        return blockps >= bps;
    }

    public static boolean playerIsAtWorldHeight(double height) {
        Player player = mc.player;
        if(player == null) return false;
        return player.getY() >= height;
    }

    public static boolean playerIsAtWorldHeight(double minHeight, double maxHeight) {
        Player player = mc.player;
        if(player == null) return false;
        double playerHeight = player.getY();
        return playerHeight >= minHeight && playerHeight <= maxHeight;
    }

    public static boolean isPassenger() {
        Player player = mc.player;
        if(player == null) return false;
        return player.isPassenger();
    }

    public static boolean isRaining() {
        Level level =  mc.level;
        if(level == null) return false;
        return level.isRaining();
    }

    public static boolean isDeadOrDying() {
        Player player = mc.player;
        if(player == null) return false;
        return player.isDeadOrDying();
    }
//---------------------------------MOB STUFF------------------------------------------------
    public static List<Mob> getMobsInAreaRatio(float radius, float RatioY) {
        return getMobsInAreaAroundPlayer(radius, (int) (radius * RatioY));
    }

    public static List<Mob> getMobsInAreaAroundPlayer(float radius, float DistanceY) {
        Player player = mc.player;
        if(player == null) return new ArrayList<>();
        Level level = player.level();
        double diameter = radius * 2;
        AABB searchBox = new AABB(
                player.getX() - radius, player.getY() - DistanceY, player.getZ() - radius,  // min
                player.getX() + radius, player.getY() + DistanceY, player.getZ() + radius   // max
        );
        List<Entity> mobs = level.getEntities(player, searchBox);

        mobs.removeIf((mob) -> {
            double dx = mob.getX() - player.getX();
            double dz = mob.getZ() - player.getZ();
            return (dx * dx + dz * dz) > (radius * radius);
        });
        List<Mob> mobsFinal = new ArrayList<>();
        mobs.forEach((mob) -> {
            if(mob instanceof Mob mob1) mobsFinal.add(mob1);
        });
        return mobsFinal;
    }

    public static boolean isMobTargetingPlayer(Mob mob) {
        Player player = mc.player;
        if(player == null) return false;
        return mob.getTarget() == player;
    }

    public static boolean isMobTargetingAPlayer(Mob mob) {
        return mob.getTarget() instanceof Player;
    }
//---------------------------------WORLD STUFF------------------------------------------------
    public static boolean isPlayerInBiome(String biomeId) {
        Level level =  mc.level;
        Player player = mc.player;
        if (level != null && player != null) {
            Holder<Biome> biome = level.getBiome(player.blockPosition());
            ResourceLocation biomeName = level.registryAccess()
                    .registryOrThrow(Registries.BIOME)
                    .getKey(biome.value());
            if(biomeName == null) return false;
            return biomeName.toString().equals(biomeId);
        }
        return false;
    }

    public boolean isFullMoon() {
        return isMoonPhase(8);
    }

    //Accepts 1-8. 1 being a new moon and 8 being a full moon
    public boolean isMoonPhase(int phase) {
        Level level = mc.level;
        if (level != null) {
            return level.getMoonPhase() == phase;
        }
        return false;
    }

    public static boolean isNight() {
        Level level = mc.level;
        if (level != null) {
            return getTimeInDay(level) >= 13000 && getTimeInDay(level) <= 23000;
        }
        return false;
    }

    public static boolean isDay() {
        Level level = mc.level;
        if (level != null) {
            return getTimeInDay(level) >= 23000 && getTimeInDay(level) <= 13000;
        }
        return false;
    }

    private static int getTimeInDay(Level level){
        //System.out.println((int) (level.getDayTime() % 24000));
        return (int) (level.getDayTime() % 24000);
    }
    private static int getDay(Level level) {
        return (int) (level.getDayTime() / 24000);
    }

    public static boolean isDayCountAtOrBeyond(int dayCount) {
        Level level = mc.level;
        if(level == null) return false;
        return (level.getDayTime() / 24000) >= dayCount;
    }

    public static boolean isDayCountAtOrBeyond(int minDayCount, int maxDayCount) {
        Level level = mc.level;
        if(level == null) return false;
        return getDay(level) >= minDayCount && getDay(level) <= maxDayCount;
    }

}
