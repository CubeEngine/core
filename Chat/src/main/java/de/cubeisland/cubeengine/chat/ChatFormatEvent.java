package de.cubeisland.cubeengine.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChatFormatEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final String message;
    private final String format;
    final Map<String, String> variables;

    public ChatFormatEvent(Player player, String message, String format,boolean async)
    {
        super(async);
        this.player = player;
        this.message = message;
        this.format = format;
        this.variables = new HashMap<String, String>();
    }

    public Player getPlayer()
    {
        return player;
    }

    public String getMessage()
    {
        return message;
    }

    public String getFormat()
    {
        return this.format;
    }

    public void setVariable(String name, String value)
    {
        if (name == null)
        {
            return;
        }
        name = name.toUpperCase(Locale.ENGLISH);
        if (value == null)
        {
            this.variables.remove(name);
        }
        else
        {
            this.variables.put(name, value);
        }
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
