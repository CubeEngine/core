/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.log.action.logaction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static de.cubeisland.engine.log.action.ActionTypeCategory.PLAYER;

/**
 * player teleports
 * <p>Events: {@link PlayerTeleportEvent}</p>
 */
public class PlayerTeleport extends SimpleLogActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(PLAYER));
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
        json.put("world", this.module.getCore().getWorldManager().getWorldId(location.getWorld()));
        json.put("x",location.getBlockX());
        json.put("y",location.getBlockY());
        json.put("z",location.getBlockZ());
        return json.toString();
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        JsonNode json = logEntry.getAdditional();
        World world = this.module.getCore().getWorldManager().getWorld(json.get("world").asLong());
        if (logEntry.hasAttached())
        {
            Location locFrom = logEntry.getLocation();
            Location locTo = new Location(world,0,0,0);
            locTo.setX(json.get("x").asInt());
            locTo.setY(json.get("y").asInt());
            locTo.setZ(json.get("z").asInt());
            if (json.get("dir").asText().equals("from"))
            {
                Location temp = locTo;
                locTo = locFrom;
                locFrom = temp;
            }
            user.sendTranslated(MessageType.POSITIVE, "{}{user} teleported from {vector} in {world} to {vector} in {world}{}",
                        time, logEntry.getCauserUser().getName(), new BlockVector3(locFrom.getBlockX(), locFrom.getBlockY(), locFrom.getBlockZ()), locFrom.getWorld(),
                                                                  new BlockVector3(locTo.getBlockX(), locTo.getBlockY(), locTo.getBlockZ()), locTo.getWorld());
        }
        else
        {
            if (json.get("dir").asText().equals("from"))
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{user} teleported from {vector} in {world}", time, logEntry.getCauserUser().getName(),
                                    new BlockVector3(json.get("x").asInt(), json.get("y").asInt(), json.get("z").asInt()), world, loc);
            }
            else
            {

                user.sendTranslated(MessageType.POSITIVE, "{}{user} teleported to {vector} in {world}!", time, logEntry.getCauserUser().getDisplayName(),
                                    new BlockVector3(json.get("x").asInt(), json.get("y").asInt(), json.get("z").asInt()), world, loc);
            }
        }
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        if (super.isSimilar(logEntry, other) && logEntry.getCauser() == other.getCauser() &&
            Math.abs(logEntry.getTimestamp().getTime() - other.getTimestamp().getTime()) < 5)
        {
            if (logEntry.getAdditional().get("dir").asText().equals(other.getAdditional().get("dir").asText()))
            {
                return false;
            }
            int x = logEntry.getAdditional().get("x").asInt();
            int y = logEntry.getAdditional().get("y").asInt();
            int z = logEntry.getAdditional().get("z").asInt();
            long worldID = logEntry.getAdditional().get("world").asLong();
            if (x == other.getVector().x
                && y == other.getVector().y
                && z == other.getVector().z
                && worldID == other.getWorldID().longValue())
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).PLAYER_TELEPORT_enable;
    }

    @Override
    public boolean canRedo()
    {
        return false;
    }

    @Override
    public boolean canRollback()
    {
        return false;
    }
}
