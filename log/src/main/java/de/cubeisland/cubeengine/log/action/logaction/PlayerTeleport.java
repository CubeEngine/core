package de.cubeisland.cubeengine.log.action.logaction;

import java.util.EnumSet;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * player teleports
 * <p>Events: {@link PlayerTeleportEvent}</p>
 */
public class PlayerTeleport extends SimpleLogActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(PLAYER);
    }

    @Override
    public boolean canRollback()
    {
        return false;
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
        json.put("world", this.logModule.getCore().getWorldManager().getWorldId(location.getWorld()));
        json.put("x",location.getBlockX());
        json.put("y",location.getBlockY());
        json.put("z",location.getBlockZ());
        return json.toString();
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        JsonNode json = logEntry.additional;
        String world = this.logModule.getCore().getWorldManager().getWorld(json.get("world").asInt()).getName();
        if (json.get("dir").asText().equals("from"))
        {
            user.sendTranslated("%s&2%s&a teleported from &6%d&f:&6%d&f:&6%d&a in &6%s%s!",
                                time,logEntry.getCauserUser().getDisplayName(),
                                json.get("x").asInt(),json.get("y").asInt(),json.get("z").asInt(),
                                world, loc);
        }
        else
        {

            user.sendTranslated("%s&2%s&a teleported to &6%d&f:&6%d&f:&6%d&a in &6%s%s!",
                                time,logEntry.getCauserUser().getDisplayName(),
                                json.get("x").asInt(),json.get("y").asInt(),json.get("z").asInt(),
                                world, loc);
        }
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
