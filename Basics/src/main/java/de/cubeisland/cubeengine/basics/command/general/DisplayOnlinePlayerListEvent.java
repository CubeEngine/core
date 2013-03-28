package de.cubeisland.cubeengine.basics.command.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class DisplayOnlinePlayerListEvent extends Event implements Cancellable
{
    private final Basics basics;
    private List<User> users;
    private List<String> userNames;
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

    public DisplayOnlinePlayerListEvent(Basics basics, CommandSender sender, List<User> users, List<String> userNames)
    {
        this.basics = basics;
        this.sender = sender;
        this.users = users;
        this.userNames = userNames;
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
    public List<User> getUsers()
    {
        return users;
    }

    /**
     * @param users the players to set
     */
    public void setUsers(List<User> users)
    {
        this.users = users;
    }

    /**
     * @return the sender
     */
    public CommandSender getSender()
    {
        return sender;
    }

    public List<String> getUserNames()
    {
        return userNames;
    }
}
