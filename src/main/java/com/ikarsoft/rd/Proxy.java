package com.ikarsoft.rd;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class Proxy {
    private FMLEventChannel channel;

    public void preInit(FMLPreInitializationEvent event) {
        PacketHandler packetHandler = new PacketHandler();
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(PacketHandler.CHANNEL_ID);
        channel.register(packetHandler);
    }

    public void init(FMLInitializationEvent event) {

    }

    public void loadComplete(FMLLoadCompleteEvent event) {

    }

    public void sendPacketToServer(Packet packet) {
        Log.get().error("Tried to send packet to the server from the server: {}", packet);
    }

    public void sendPacketToClient(Packet packet, EntityPlayerMP player) {
        if (channel != null) {
            channel.sendTo(packet.getPacket(), player);
        }
    }
}
