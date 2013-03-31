package de.cubeisland.cubeengine.basics.command.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

import gnu.trove.map.hash.THashMap;

public class DisplayOnlinePlayerListEvent extends Event implements Cancellable
{
    private final Basics basics;
    private THashMap<User, String> userStrings = new THashMap<User, String>();
    private THashMap<String, List<User>> grouped = new THashMap<String, List<User>>();
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

    public DisplayOnlinePlayerListEvent(Basics basics, CommandSender sender, THashMap<User, String> userStrings, List<User> defaultList)
    {
        this.basics = basics;
        this.sender = sender;
        this.userStrings = userStrings;
        this.grouped.put("&6Players: ",defaultList);
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

    public THashMap<User, String> getUserStrings()
    {
        return userStrings;
    }

    public THashMap<String, List<User>> getGrouped()
    {
        return grouped;
    }

    public CommandSender getCommandSender()
    {
        return this.sender;
    }
}
