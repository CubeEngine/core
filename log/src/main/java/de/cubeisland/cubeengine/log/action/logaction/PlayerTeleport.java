package de.cubeisland.cubeengine.log.action.logaction;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * player teleports
 * <p>Events: {@link PlayerTeleportEvent}</p>
 */
public class PlayerTeleport extends SimpleLogActionType
{
    public PlayerTeleport(Log module)
    {
        super(module, true, PLAYER);
    }

    @Override
    public String getName()
    {
        return "player-teleport";
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

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("PLAYER_TELEPORT"); //TODO
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return false;
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).PLAYER_TELEPORT_enable;
    }
}
