package com.ikarsoft.rd;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public abstract class Packet {
    private final int id = getPacketId();

    public final FMLProxyPacket getPacket() {
        PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());

        packetBuffer.writeByte(id);
        writePacketData(packetBuffer);

        return new FMLProxyPacket(packetBuffer, PacketHandler.CHANNEL_ID);
    }

    public abstract int getPacketId();

    public abstract void writePacketData(PacketBuffer buf);
}
