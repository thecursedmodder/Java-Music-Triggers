package net.cursedmodder.javatriggers.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class JTMath {
    private static BlockHitResult rayCast(Level level, Player player, double distance) {
        Vec3 start = player.getEyePosition(); // where the ray starts
        Vec3 look = player.getLookAngle();    // direction player is looking
        Vec3 end = start.add(look.scale(distance)); // end point

        ClipContext context = new ClipContext(
                start,
                end,
                ClipContext.Block.OUTLINE, // what counts as a hit
                ClipContext.Fluid.NONE,    // ignore fluids (or use ANY)
                player
        );

        return level.clip(context);
    }

}
