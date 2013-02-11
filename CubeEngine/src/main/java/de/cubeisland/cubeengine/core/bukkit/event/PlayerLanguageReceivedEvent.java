package de.cubeisland.cubeengine.core.bukkit.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * The event gets called when ever a player receives the paket 204.
 * This is the case right after the player joins the server
 * and when the user changes his language or view distance settings
 */
public class PlayerLanguageReceivedEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private final String language;
    private final Player player;

    public PlayerLanguageReceivedEvent(Player player, String language)
    {
        this.language = language;
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

    /**
     * Returns the locale string of the player
     *
     * @return the locale string
     */
    public String getLanguage()
    {
        return this.language;
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
