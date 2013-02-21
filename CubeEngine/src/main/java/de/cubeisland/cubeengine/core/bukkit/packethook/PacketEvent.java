package de.cubeisland.cubeengine.core.bukkit.packethook;

import net.minecraft.server.v1_4_R1.Packet;
import org.bukkit.entity.Player;

public abstract class PacketEvent
{
    private final Player player;
    private Packet packet;
    private boolean cancelled;

    public PacketEvent(Player player, Packet packet)
    {
        this.player = player;
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

    public void setPacket(Packet newPacket)
    {
        if (newPacket == null)
        {
            throw new IllegalArgumentException("The packet must not be null!");
        }
        if (this.getPacket().getClass().isAssignableFrom(newPacket.getClass()))
        {
            throw new IllegalArgumentException("Must be of the same type!");
        }
        this.packet = newPacket;
    }

    /**
     * Returns the ID of the packet
     *
     * @return the packet ID
     */
    public int getPacketId()
    {
        return this.getPacket().k();
    }
}
