package net.cursedmodder.javatriggers.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MobTargetingReturn {
    public int mobId;
    public int targetId;

    //Every time a zombie chooses a new target it sends a packet to the client
    public MobTargetingReturn(int ID, int target) {
        mobId = ID;
        targetId = target;
    }

    public MobTargetingReturn(FriendlyByteBuf buf) {
        mobId = buf.readInt();
        targetId = buf.readInt();
    }

    public void ToBytes(FriendlyByteBuf buf) {
        buf.writeInt(mobId);
        buf.writeInt(targetId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if(level.getEntity(mobId) instanceof Mob mob) {
                if (targetId < 0) {
                    mob.setTarget(null);
                } else {
                    if(level.getEntity(targetId) instanceof LivingEntity entity) mob.setTarget(entity);
                }
            }
        });
        return true;
    }

}
