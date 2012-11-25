package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.Prevention;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Prevents chatting.
 */
public class ChatPrevention extends Prevention
{
    public ChatPrevention(Guests guests)
    {
        super("chat", guests);
        setEnablePunishing(false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void chat(AsyncPlayerChatEvent event)
    {
        prevent(event, event.getPlayer());
    }
}
