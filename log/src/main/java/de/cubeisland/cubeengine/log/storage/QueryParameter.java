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
package de.cubeisland.cubeengine.log.storage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.math.BlockVector3;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.ActionType;

public class QueryParameter implements Cloneable
{
    private final Log module;

    // When (since/before/from-to)
    volatile Long from_since;
    volatile Long to_before;
    // Where (in world / at location1 / in between location1 and location2)
    volatile Long worldID;
    volatile BlockVector3 location1;
    volatile BlockVector3 location2;
    Integer radius; //TODO
    Set<Location> singleBlockLocations;
    // The actions to look for
    Map<ActionType, Boolean> actions = new ConcurrentHashMap<ActionType, Boolean>();
    // Users
    Map<Long, Boolean> users = new ConcurrentHashMap<Long, Boolean>();
    // Entity
    Map<Integer, Boolean> entities = new ConcurrentHashMap<Integer, Boolean>();
    // Blocks
    Map<BlockData, Boolean> blocks = new ConcurrentHashMap<BlockData, Boolean>();

    boolean compress = true;
    boolean showDate = false;
    boolean showLoc = false;
    boolean showID = false;
    int pageLimit = -1; // -1 is no limit

    public QueryParameter(Log module)
    {
        this.module = module;
    }

    private void resetLocations()
    {
        worldID = null;
        location1 = null;
        location2 = null;
        singleBlockLocations = null;
        radius = null;
    }

    public void setWorld(World world)
    {
        this.resetLocations();
        this.worldID = module.getCore().getWorldManager().getWorldId(world);
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
            worldID = module.getCore().getWorldManager().getWorldId(locations[0].getWorld());
            location1 = new BlockVector3(locations[0].getBlockX(),locations[0].getBlockY(),locations[0].getBlockZ());
        }
        else
        {
            this.singleBlockLocations = new HashSet<Location>(Arrays.asList(locations));
        }
    }

    public void setLocationRange(Location loc1, Location loc2)
    {
        this.resetLocations();
        worldID = module.getCore().getWorldManager().getWorldId(loc1.getWorld());
        location1 = new BlockVector3(loc1.getBlockX(),loc1.getBlockY(),loc1.getBlockZ());
        location2 = new BlockVector3(loc2.getBlockX(),loc2.getBlockY(),loc2.getBlockZ());
    }

    public void setLocationRadius(Location loc, int radius)
    {
        this.setSingleLocations(loc);
        this.radius = radius;
    }

    public void since(long date)
    {
        this.from_since = date;
        this.to_before = null;
    }

    public void before(long date)
    {
        this.from_since = null;
        this.to_before = date;
    }

    public void range(long from, long to)
    {
        this.from_since = from;
        this.to_before = to;
    }

    public void setActions(Set<ActionType> actions, boolean include)
    {
        this.actions.clear();
        for (ActionType action : actions)
        {
            this.actions.put(action, include);
        }
    }

    public void includeAction(ActionType action)
    {
        this.actions.put(action, true);
    }

    public void excludeAction(ActionType action)
    {
        this.actions.put(action, false);
    }

    public void clearActions()
    {
        actions.clear();
    }

    public void setUsers(Set<Long> users, boolean include)
    {
        this.users.clear();
        for (Long user : users)
        {
            this.users.put(user, include);
        }
    }

    public void includeUser(Long userId)
    {
        this.users.put(userId, true);
    }

    public void excludeUser(Long userId)
    {
        this.users.put(userId, false);
    }

    public void clearUsers()
    {
        users.clear();
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
        users.clear();
    }

    public void setBlocks(Set<BlockData> blockDatas, boolean include)
    {
        this.blocks.clear();
        for (BlockData blockData : blockDatas)
        {
            this.blocks.put(blockData, include);
        }
    }

    public void includeBlock(BlockData data)
    {
        this.blocks.put(data, true);
    }

    public void excludeBlock(BlockData data)
    {
        this.blocks.put(data, false);
    }

    public void clearBlocks()
    {
        blocks.clear();
    }

    public void showNoLogsFound(User user)
    {
        if (this.location1 != null)
        {
            if (this.location2 != null)
            {
                user.sendTranslated("&eNo logs found in between &3%d&f:&3%d&f:&3%d&e and &3%d&f:&3%d&f:&3%d&e in &3%s&e!",
                                    this.location1.x, this.location1.y, this.location1.z,
                                    this.location2.x, this.location2.y, this.location2.z,
                                    this.module.getCore().getWorldManager().getWorld(worldID).getName());
            }
            else if (radius == null)
            {
                user.sendTranslated("&eNo logs found at &3%s&f:&3%d&f:&3%d&f:&3%d&e!",
                                    this.module.getCore().getWorldManager().getWorld(worldID).getName(),
                                    this.location1.x, this.location1.y, this.location1.z);
            }
            else if (user.getLocation().equals(location1))
            {
                user.sendTranslated("&eNo logs found in a radius of &3%d&e around you!", radius);
            }
            else
            {
                user.sendTranslated("&eNo logs found in a radius of &3%d around %d&f:&3%d&f:&3%d&e in &3%s&e!",
                                    radius,this.location1.x, this.location1.y, this.location1.z,
                                    this.module.getCore().getWorldManager().getWorld(worldID).getName());
            }
        }
        else
        {
            user.sendTranslated("&eNo logs found for your given parameters");
        }
    }

    public QueryParameter clone()
    {
        QueryParameter params = new QueryParameter(this.module);

        params.from_since = from_since;
        params.to_before = to_before;
        params.worldID = worldID;
        params.location1 = location1;
        params.location2 = location2;
        params.radius = radius;
        params.singleBlockLocations = singleBlockLocations;
        params.actions = new ConcurrentHashMap<ActionType, Boolean>(actions);
        params.users = new ConcurrentHashMap<Long, Boolean>(users);
        params.entities = new ConcurrentHashMap<Integer, Boolean>(entities);
        params.blocks = new ConcurrentHashMap<BlockData, Boolean>(blocks);
        params.compress = compress;
        params.showDate = showDate;
        params.showLoc = showLoc;
        params.showID = showID;
        params.pageLimit = pageLimit;
        return params;
    }

    public boolean hasTime()
    {
        return from_since != null || to_before != null;
    }

    public int getPageLimit()
    {
        return pageLimit;
    }

    public void setPageLimit(int pageLimit)
    {
        this.pageLimit = pageLimit;
    }

    public boolean includeActions()
    {
        for (Boolean include : this.actions.values())
        {
            if (include) return true; // if one is included exclusion do not matter
        }
        return false; // all excluded
    }

    public boolean includeBlocks()
    {
        for (Boolean include : this.blocks.values())
        {
            if (include) return true; // if one is included exclusion do not matter
        }
        return false; // all excluded
    }

    public boolean includeUsers()
    {
        for (Boolean include : this.users.values())
        {
            if (include) return true; // if one is included exclusion do not matter
        }
        return false; // all excluded
    }

    public boolean containsAction(ActionType actionType)
    {
        Boolean set = this.actions.get(actionType);
        if (set == null) return false;
        return set;
    }
}