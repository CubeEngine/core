package de.cubeisland.cubeengine.core.bukkit;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Phillip Schichtel
 */
public class LanguageReceivedEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private final String language;

    public LanguageReceivedEvent(String language)
    {
        super(false);
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
