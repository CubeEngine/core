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
package de.cubeisland.engine.log.storage;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.block.ActionBlock.BlockSection;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEUTRAL;

public class QueryParameter implements Cloneable
{
    private final Log module;

    // When (since/before/from-to)
    volatile Date from_since;
    volatile Date to_before;
    // Where (in world / at location1 / in between location1 and location2)
    volatile World world;
    volatile BlockVector3 location1;
    volatile BlockVector3 location2;
    Integer radius;
    Set<Location> singleBlockLocations;
    // The actions to look for
    Map<Class<? extends BaseAction>, Boolean> actions = new ConcurrentHashMap<>();
    // Users
    Map<UUID, Boolean> users = new ConcurrentHashMap<>();
    // Entity
    Map<Integer, Boolean> entities = new ConcurrentHashMap<>();
    // Blocks
    Map<BlockSection, Boolean> blocks = new ConcurrentHashMap<>();

    public QueryParameter(Log module)
    {
        this.module = module;
    }

    private void resetLocations()
    {
        this.world = null;
        this.location1 = null;
        this.location2 = null;
        this.singleBlockLocations = null;
        this.radius = null;
    }

    public void setWorld(World world)
    {
        this.resetLocations();
        this.world = world;
    }

    public void setSingleLocations(Location... locations)
    {
        this.resetLocations();
        if (locations.length == 0)
        {
            throw new IllegalArgumentException("No location given");
        }
        if (locations.length == 1)
        {
            this.world = locations[0].getWorld();
            this.location1 = new BlockVector3(locations[0].getBlockX(), locations[0].getBlockY(),
                                              locations[0].getBlockZ());
        }
        else
        {
            this.singleBlockLocations = new HashSet<>(Arrays.asList(locations));
        }
    }

    public void setLocationRange(Location loc1, Location loc2)
    {
        this.resetLocations();
        this.world = loc1.getWorld();
        this.location1 = new BlockVector3(loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ());
        this.location2 = new BlockVector3(loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ());
    }

    public void setLocationRadius(Location loc, int radius)
    {
        this.setSingleLocations(loc);
        this.radius = radius;
    }

    public void since(Date date)
    {
        this.from_since = date;
        this.to_before = null;
    }

    public void before(Date date)
    {
        this.from_since = null;
        this.to_before = date;
    }

    public void range(Date from, Date to)
    {
        this.from_since = from;
        this.to_before = to;
    }

    public void setActions(Set<Class<? extends BaseAction>> actions, boolean include)
    {
        this.actions.clear();
        for (Class<? extends BaseAction> action : actions)
        {
            this.actions.put(action, include);
        }
    }

    public void includeAction(Class<? extends BaseAction> action)
    {
        this.actions.put(action, true);
    }

    public void excludeAction(Class<? extends BaseAction> action)
    {
        this.actions.put(action, false);
    }

    public void clearActions()
    {
        this.actions.clear();
    }

    public void setUsers(Set<UUID> users, boolean include)
    {
        this.users.clear();
        for (UUID user : users)
        {
            this.users.put(user, include);
        }
    }

    public void includeUser(UUID userId)
    {
        this.users.put(userId, true);
    }

    public void excludeUser(UUID userId)
    {
        this.users.put(userId, false);
    }

    public void clearUsers()
    {
        this.users.clear();
    }

    public void setEntities(Set<EntityType> entities, boolean include)
    {
        this.entities.clear();
        for (EntityType entityType : entities)
        {
            this.entities.put(-entityType.getTypeId(), include);
        }
    }

    public void includeEntity(EntityType type)
    {
        this.entities.put(-type.getTypeId(), true);
    }

    public void excludeEntity(EntityType type)
    {
        this.entities.put(-type.getTypeId(), false);
    }

    public void clearEntities()
    {
        this.entities.clear();
    }

    public void setBlocks(Set<BlockSection> blockDatas, boolean include)
    {
        this.blocks.clear();
        for (BlockSection blockData : blockDatas)
        {
            this.blocks.put(blockData, include);
        }
    }

    public void includeBlock(BlockSection data)
    {
        this.blocks.put(data, true);
    }

    public void excludeBlock(BlockSection data)
    {
        this.blocks.put(data, false);
    }

    public void clearBlocks()
    {
        this.blocks.clear();
    }

    public void showNoLogsFound(User user)
    {
        if (this.location1 != null)
        {
            if (this.location2 != null)
            {
                user.sendTranslated(NEUTRAL, "No logs found in between {vector} and {vector} in {world}!",
                                    new BlockVector3(this.location1.x, this.location1.y, this.location1.z),
                                    new BlockVector3(this.location2.x, this.location2.y, this.location2.z), world);
            }
            else if (this.radius == null)
            {
                user.sendTranslated(NEUTRAL, "No logs found at {vector} in {world}!", new BlockVector3(
                    this.location1.x, this.location1.y, this.location1.z), world);
            }
            else if (user.getLocation().getBlockX() == location1.x && user.getLocation().getBlockY() == location1.y
                && user.getLocation().getBlockZ() == location1.z)
            {
                user.sendTranslated(NEUTRAL, "No logs found in a radius of {amount} around you!", radius);
            }
            else
            {
                user.sendTranslated(NEUTRAL,
                                    "No logs found in a radius of {amount} around {vector} in {world}!", this.radius,
                                    new BlockVector3(this.location1.x, this.location1.y, this.location1.z), world);
            }
        }
        else
        {
            user.sendTranslated(NEUTRAL, "No logs found for your given parameters");
        }
    }

    public QueryParameter clone()
    {
        QueryParameter params = new QueryParameter(this.module);

        params.from_since = this.from_since;
        params.to_before = this.to_before;
        params.world = this.world;
        params.location1 = this.location1;
        params.location2 = this.location2;
        params.radius = this.radius;
        params.singleBlockLocations = this.singleBlockLocations;
        params.actions = new ConcurrentHashMap<>(this.actions);
        params.users = new ConcurrentHashMap<>(this.users);
        params.entities = new ConcurrentHashMap<>(this.entities);
        params.blocks = new ConcurrentHashMap<>(this.blocks);
        return params;
    }

    public boolean hasTime()
    {
        return this.from_since != null || this.to_before != null;
    }

    public boolean includeActions()
    {
        for (Boolean include : this.actions.values())
        {
            if (include)
            {
                return true; // if one is included exclusion do not matter
            }
        }
        return false; // all excluded
    }

    public boolean includeBlocks()
    {
        for (Boolean include : this.blocks.values())
        {
            if (include)
            {
                return true; // if one is included exclusion do not matter
            }
        }
        return false; // all excluded
    }

    public boolean includeUsers()
    {
        for (Boolean include : this.users.values())
        {
            if (include)
            {
                return true; // if one is included exclusion do not matter
            }
        }
        return false; // all excluded
    }

    public boolean containsAction(Class<? extends BaseAction> actionType)
    {
        Boolean set = this.actions.get(actionType);
        if (set == null)
        {
            return false;
        }
        return set;
    }
}
