package de.cubeisland.cubeengine.core.bukkit.event;

import net.minecraft.server.v1_4_5.Packet;
import org.bukkit.event.Event;

public abstract class PacketEvent extends Event
{
    private final int packetId;
    private final Packet packet;

    public PacketEvent(Packet packet)
    {
        super(true);
        this.packetId = packet.k();
        this.packet = packet;
    }

    /**
     * Returns the packet of this event
     *
     * @return the packet
     */
    public Packet getPacket()
    {
        return this.packet;
    }

    /**
     * Returns the ID of the packet
     *
     * @return the packet ID
     */
    public int getPacketId()
    {
        return this.packetId;
    }
}
