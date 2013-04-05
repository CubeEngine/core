package de.cubeisland.cubeengine.log.action.logaction;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.node.ArrayNode;

import static de.cubeisland.cubeengine.log.storage.ActionType.PLAYER_QUIT;

public class PlayerQuit extends SimpleLogActionType
{
    public PlayerQuit(Log module)
    {
        super(module, 0xA4, "player-quit");
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
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
}
