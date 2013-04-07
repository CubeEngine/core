package de.cubeisland.cubeengine.log.storage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.Location;
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
    Set<ActionType> actions = new CopyOnWriteArraySet<ActionType>();
    volatile boolean includeActions = true;
    // Users
    Set<Long> users = new CopyOnWriteArraySet<Long>();
    volatile boolean includeUsers = true;
    // Entity
    Set<Integer> entities = new CopyOnWriteArraySet<Integer>();
    volatile boolean includeEntity = true;
    // Blocks
    Set<BlockData> blocks = new CopyOnWriteArraySet<BlockData>();
    volatile boolean includeBlocks = true;

    boolean compress = true;
    boolean showDate = false;
    boolean showLoc = false;
    boolean showID = false;

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
        this.actions.addAll(actions);
        this.includeActions = include;
    }

    public void includeAction(ActionType action)
    {
        if (this.includeActions)
        {
            this.actions.add(action);
        }
        else
        {
            this.actions.remove(action);
        }
    }

    public void excludeAction(ActionType action)
    {
        if (this.includeActions)
        {
            this.actions.remove(action);
        }
        else
        {
            this.actions.add(action);
        }
    }

    public void clearActions()
    {
        actions.clear();
    }

    public void setUsers(Set<Long> users, boolean include)
    {
        this.users.clear();
        this.users.addAll(users);
        this.includeUsers = include;
    }

    public void includeUser(Long userId)
    {
        if (this.includeUsers)
        {
            this.users.add(userId);
        }
        else
        {
            this.users.remove(userId);
        }
    }

    public void excludeUser(Long userId)
    {
        if (this.includeUsers)
        {
            this.users.remove(userId);
        }
        else
        {
            this.users.add(userId);
        }
    }

    public void clearUsers()
    {
        users.clear();
    }

    public void setEntities(Set<EntityType> entities, boolean include)
    {
        this.entities.clear();
        this.includeEntity = include;
        for (EntityType entityType : entities)
        {
            this.entities.add(-entityType.getTypeId());
        }
    }

    public void includeEntity(EntityType type)
    {
        if (this.includeEntity)
        {
            this.entities.add(-type.getTypeId());
        }
        else
        {
            this.entities.remove(-type.getTypeId());
        }
    }

    public void excludeEntity(EntityType type)
    {
        if (this.includeEntity)
        {
            this.entities.remove(-type.getTypeId());
        }
        else
        {
            this.entities.add(-type.getTypeId());
        }
    }

    public void clearEntities()
    {
        users.clear();
    }

    public void setBlocks(Set<BlockData> entities, boolean include)
    {
        this.blocks.clear();
        this.blocks.addAll(entities);
        this.includeBlocks = include;
    }

    public void includeBlock(BlockData data)
    {
        if (this.includeBlocks)
        {
            this.blocks.add(data);
        }
        else
        {
            this.blocks.remove(data);
        }
    }

    public void excludeBlock(BlockData data)
    {
        if (this.includeBlocks)
        {
            this.blocks.remove(data);
        }
        else
        {
            this.blocks.add(data);
        }
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
                user.sendTranslated("&eNo logs found in between &6%d&f:&6%d&f:&6%d&e and &6%d&f:&6%d&f:&6%d&e in &6%s&e!",
                                    this.location1.x, this.location1.y, this.location1.z,
                                    this.location2.x, this.location2.y, this.location2.z,
                                    this.module.getCore().getWorldManager().getWorld(worldID).getName());
            }
            else if (radius == null)
            {
                user.sendTranslated("&eNo logs found at &6%d&f:&6%d&f:&6%d&e in &6%s&e!",
                                    this.location1.x, this.location1.y, this.location1.z,
                                    this.module.getCore().getWorldManager().getWorld(worldID).getName());
            }
            else if (user.getLocation().equals(location1))
            {
                user.sendTranslated("&eNo logs found in a radius of &6%d&e around you!", radius);
            }
            else
            {
                user.sendTranslated("&eNo logs found in a radius of &6%d around %d&f:&6%d&f:&6%d&e in &6%s&e!",
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
        params.actions = new CopyOnWriteArraySet<ActionType>(actions);
        params.includeActions = includeActions;
        params.users = new CopyOnWriteArraySet<Long>(users);
        params.includeUsers = includeUsers;
        params.entities = new CopyOnWriteArraySet<Integer>(entities);
        params.includeEntity = includeEntity;
        params.blocks = new CopyOnWriteArraySet<BlockData>(blocks);
        params.includeBlocks = includeBlocks;
        params.compress = compress;
        params.showDate = showDate;
        params.showLoc = showLoc;
        params.showID = showID;
        return params;
    }

    public boolean hasTime()
    {
        return from_since != null || to_before != null;
    }
}