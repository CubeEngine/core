package de.cubeisland.cubeengine.log.action.logaction;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * player quits
 * <p>Events: {@link PlayerQuitEvent}</p>
 */
public class PlayerQuit extends SimpleLogActionType
{
    public PlayerQuit(Log module)
    {
        super(module, "player-quit",true, PLAYER);
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        //TODO attach multiple quit at same loc
        if (this.isActive(event.getPlayer().getWorld()))
        {
            this.logSimple(event.getPlayer(),null);
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&2%s&a leaved the server%s&a!",
                            time,logEntry.getCauserUser().getDisplayName(),loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.world == other.world
            && logEntry.location.equals(other.location)
            && logEntry.causer == other.causer;
    }
}
