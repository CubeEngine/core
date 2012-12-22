package de.cubeisland.cubeengine.core.bukkit.event;

import net.minecraft.server.v1_4_6.Packet;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class PacketReceivedEvent extends PacketEvent
{
    private static final HandlerList handlers = new HandlerList();
    private final Player player;

    public PacketReceivedEvent(Player player, Packet packet)
    {
        super(packet);
        this.player = player;
    }

    /**
     * Returns the player of this event
     *
     * @return the player
     */
    public Player getPlayer()
    {
        return this.player;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
