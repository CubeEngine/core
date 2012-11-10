package de.cubeisland.cubeengine.core.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AfterJoinEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();

    private String joinMessage;
    private Player player;
    
    public AfterJoinEvent(Player player, String joinMessage)
    {
        this.joinMessage = joinMessage;
        this.player = player;
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

    /**
     * @return the joinMessage
     */
    public String getJoinMessage()
    {
        return joinMessage;
    }

    /**
     * @return the player
     */
    public Player getPlayer()
    {
        return player;
    }
}
