package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.basics.Basics;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DisplayOnlinePlayerListEvent extends Event implements Cancellable
{
    private final Basics basics;
    private List<Player> players;
    private static final HandlerList handlers = new HandlerList();
    private final CommandSender sender;
    private boolean cancelled = false;

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    public DisplayOnlinePlayerListEvent(Basics basics, CommandSender sender, List<Player> players)
    {

        this.basics = basics;
        this.sender = sender;
        this.players = players;
    }

    @Override
    public boolean isCancelled()
    {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean bln)
    {
        this.cancelled = bln;
    }

    /**
     * @return the basics
     */
    public Basics getBasics()
    {
        return basics;
    }

    /**
     * @return the players
     */
    public List<Player> getPlayers()
    {
        return players;
    }

    /**
     * @param players the players to set
     */
    public void setPlayers(List<Player> players)
    {
        this.players = players;
    }

    /**
     * @return the sender
     */
    public CommandSender getSender()
    {
        return sender;
    }
}
