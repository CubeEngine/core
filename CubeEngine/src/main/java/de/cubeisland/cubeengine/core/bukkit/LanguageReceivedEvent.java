package de.cubeisland.cubeengine.core.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 *
 * @author Phillip Schichtel
 */
public class LanguageReceivedEvent extends PlayerEvent
{
    private static final HandlerList handlers = new HandlerList();
    private final String language;

    public LanguageReceivedEvent(Player player, String language)
    {
        super(player);
        this.language = language;
    }
    
    public String getLanguage()
    {
        return this.language;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
