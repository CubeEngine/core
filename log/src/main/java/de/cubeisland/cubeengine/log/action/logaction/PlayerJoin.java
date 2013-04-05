package de.cubeisland.cubeengine.log.action.logaction;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import de.cubeisland.cubeengine.log.Log;

import com.fasterxml.jackson.databind.node.ArrayNode;

public class PlayerJoin extends SimpleLogActionType
{
    public PlayerJoin(Log module)
    {
        super(module, 0xA3, "player-join");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if (this.isActive(event.getPlayer().getWorld()))
        {
            ArrayNode data = null;
            if (false) //TODO config
            {
                data = this.om.createArrayNode();
                data.add(event.getPlayer().getAddress().getAddress().getHostAddress());
            }
            this.logSimple(event.getPlayer(), data == null ? null : data.toString());
        }
    }
}
