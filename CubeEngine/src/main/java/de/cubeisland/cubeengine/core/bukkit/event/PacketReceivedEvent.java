package de.cubeisland.cubeengine.core.bukkit.event;

import net.minecraft.server.v1_4_R1.Packet;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class PacketReceivedEvent extends PacketEvent
{
    private static final HandlerList handlers = new HandlerList();

    public PacketReceivedEvent(Player player, Packet packet)
    {
        super(player, packet);
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
