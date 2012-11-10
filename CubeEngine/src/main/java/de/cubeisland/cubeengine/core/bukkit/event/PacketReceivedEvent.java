package de.cubeisland.cubeengine.core.bukkit.event;

import net.minecraft.server.Packet;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Phillip Schichtel
 */
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
