package net.cursedmodder.javatriggers.network.packets;

import net.cursedmodder.javatriggers.util.ClientContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SpawnPosition {
    private int X;
    private int Y;
    private int Z;

    public SpawnPosition(int Stage, int y, int z) {
        X = Stage;
        Y = y;
        Z = z;
    }

    public SpawnPosition(FriendlyByteBuf buf) {
        X = buf.readInt();
        Y = buf.readInt();
        Z = buf.readInt();
    }

    public void ToBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.X);
        buf.writeInt(this.Y);
        buf.writeInt(this.Z);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientContext.playerSpawn = new BlockPos(X, Y, Z);
        });
        return true;
    }
}