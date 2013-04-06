package de.cubeisland.cubeengine.log.action.logaction;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.node.ArrayNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * player joins
 * <p>Events: {@link PlayerJoinEvent}</p>
 */
public class PlayerJoin extends SimpleLogActionType
{
    public PlayerJoin(Log module)
    {
        super(module, "player-join",true, PLAYER);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {

        if (this.isActive(event.getPlayer().getWorld()))
        {
            ArrayNode data = null;
            if (this.lm.getConfig(event.getPlayer().getWorld()).PLAYER_JOIN_ip)
            {
                data = this.om.createArrayNode();
                data.add(event.getPlayer().getAddress().getAddress().getHostAddress());
            }
            this.logSimple(event.getPlayer(), data == null ? null : data.toString());
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        //TODO attach multiple join at same loc
        user.sendTranslated("%s&2%s&a joined the server%s&a!",
                            time, logEntry.getCauserUser().getDisplayName(),loc);
        //TODO ip if known
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.world == other.world
            && logEntry.location.equals(other.location)
            && logEntry.causer == other.causer;
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).PLAYER_JOIN_enable;
    }
}
