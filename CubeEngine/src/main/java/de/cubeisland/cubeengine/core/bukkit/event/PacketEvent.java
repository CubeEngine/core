package de.cubeisland.cubeengine.core.bukkit.event;

import net.minecraft.server.v1_4_R1.Packet;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class PacketEvent extends Event implements Cancellable
{
    private final Player player;
    private final int packetId;
    private final Packet packet;
    private boolean cancelled;

    public PacketEvent(Player player, Packet packet)
    {
        this.player = player;
        this.packetId = packet.k();
        this.packet = packet;
        this.cancelled = false;
    }


    /**
     * Returns the user of this event
     *
     * @return the user
     */
    public Player getPlayer()
    {
        return player;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }

    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
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
