package de.cubeisland.cubeengine.log.action.logaction;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.cubeengine.log.Log;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class PlayerTeleport extends SimpleLogActionType
{
    public PlayerTeleport(Log module)
    {
        super(module, 0xA5, "player-teleport");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event)
    {
        if (this.isActive(event.getPlayer().getWorld()))
        {
            if (event.getFrom().equals(event.getTo())) return;
            String targetLocation = this.serializeLocation(false,event.getTo());
            String sourceLocation = this.serializeLocation(true, event.getFrom());
            this.logSimple(event.getFrom(),event.getPlayer(),targetLocation);
            this.logSimple(event.getTo(),event.getPlayer(),sourceLocation);
        }
    }

    private String serializeLocation(boolean from, Location location)
    {
        ObjectNode json = this.om.createObjectNode();
        json.put("dir", from ? "from" : "to");
        json.put("world", this.wm.getWorldId(location.getWorld()));
        json.put("x",location.getBlockX());
        json.put("y",location.getBlockY());
        json.put("z",location.getBlockZ());
        return json.toString();
    }
}
