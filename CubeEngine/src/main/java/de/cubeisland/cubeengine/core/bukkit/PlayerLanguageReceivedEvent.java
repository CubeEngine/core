package de.cubeisland.cubeengine.core.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * The event gets called when ever a player receives the paket 204.
 * This is the case right after the player joins the server
 * and when the user changes his language or view distance settings
 */
public class PlayerLanguageReceivedEvent extends PlayerEvent
{
    private static final HandlerList handlers = new HandlerList();
    private final String language;

    public PlayerLanguageReceivedEvent(Player player, String language)
    {
        super(player);
        this.language = language;
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
