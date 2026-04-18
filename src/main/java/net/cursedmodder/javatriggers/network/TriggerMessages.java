package net.cursedmodder.javatriggers.network;

import net.cursedmodder.javatriggers.JavaTriggers;
import net.cursedmodder.javatriggers.network.packets.MobTargetingReturn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class TriggerMessages {
    private static SimpleChannel Instance;
    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(JavaTriggers.MODID, "messages")).networkProtocolVersion(() -> {
            return "1.1";
        }).clientAcceptedVersions((s) -> {
            return true;
        }).serverAcceptedVersions((s) -> {
            return true;
        }).simpleChannel();
        Instance = net;
        net.messageBuilder(MobTargetingReturn.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(MobTargetingReturn::new)
                .encoder(MobTargetingReturn::ToBytes)
                .consumerMainThread(MobTargetingReturn::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        Instance.sendToServer(message);
    }

    public static <MSG> void sendToAll(MSG msg, ServerLevel level) {
        level.getServer().getPlayerList().getPlayers().forEach((player -> {
            sendToPlayer(msg, player);
        }));
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        Instance.send(PacketDistributor.PLAYER.with(() -> {
            return player;
        }), message);
    }
}
