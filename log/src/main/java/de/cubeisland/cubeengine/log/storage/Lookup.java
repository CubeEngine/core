package de.cubeisland.cubeengine.log.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.core.util.math.BlockVector3;

import com.fasterxml.jackson.databind.JsonNode;

import static de.cubeisland.cubeengine.log.storage.ActionType.LOOKUP_CONTAINER;
import static de.cubeisland.cubeengine.log.storage.ActionType.LOOKUP_KILLS;


public class Lookup implements Cloneable
{
    private final Module module;

    // Lookup Types:
    // Full lookup / all action types / can set time & location
    // Player lookup / all player related / time / location / users
    // Block lookup / all block related / time / location / block MAT:id  | < WORLDEDIT 0x4B || 0x61 || 0x63 (hangings)

    // The actions to look for
    private Set<ActionType> actions = new CopyOnWriteArraySet<ActionType>();
    private volatile boolean includeActions = true;
    // When (since/before/from-to)
    private volatile Long from_since;
    private volatile Long to_before;
    // Where (in world / at location1 / in between location1 and location2)
    private volatile BlockVector3 location1;
    private volatile BlockVector3 location2;
    private volatile Long worldID;
    // users
    private Set<Long> users = new CopyOnWriteArraySet<Long>();
    private volatile boolean includeUsers = true;
    //Block-Logs:
    private Set<BlockData> blocks = new CopyOnWriteArraySet<BlockData>();
    private volatile boolean includeBlocks = true;
    // smaller than WORLDEDIT (0x4B) OR HANGING_PLACE/HANGING_BREAK (0x61/0x63)

    private TreeSet<LogEntry> logEntries = new TreeSet<LogEntry>();

    private Lookup(Module module)
    {
        this.module = module;
    }

    public Lookup setUsers(Set<Long> userIds, boolean include)
    {
        this.users.clear();
        this.users.addAll(userIds);
        this.includeUsers = include;
        return this;
    }

    public Lookup includeUser(Long userId)
    {
        if (this.includeUsers)
        {
            this.users.add(userId);
        }
        else
        {
            this.users.remove(userId);
        }
        return this;
    }

    public Lookup excludeUser(Long userId)
    {
        if (this.includeUsers)
        {
            this.users.remove(userId);
        }
        else
        {
            this.users.add(userId);
        }
        return this;
    }

    public Lookup includeAction(ActionType action)
    {
        if (this.includeActions)
        {
            this.actions.add(action);
        }
        else
        {
            this.actions.remove(action);
        }
        return this;
    }

    public Lookup excludeAction(ActionType action)
    {
        if (this.includeActions)
        {
            this.actions.remove(action);
        }
        else
        {
            this.actions.add(action);
        }
        return this;
    }

    public Lookup includeActions(Collection<ActionType> actions)
    {
        if (this.includeActions)
        {
            this.actions.addAll(actions);
        }
        else
        {
            this.actions.removeAll(actions);
        }
        return this;
    }

    public Lookup excludeActions(Collection<ActionType> actions)
    {
        if (this.includeActions)
        {
            this.actions.removeAll(actions);
        }
        else
        {
            this.actions.addAll(actions);
        }
        return this;
    }

    public Lookup clearActions()
    {
        actions.clear();
        return this;
    }

    public Lookup since(long date)
    {
        this.from_since = date;
        this.to_before = null;
        return this;
    }

    public Lookup before(long date)
    {
        this.from_since = null;
        this.to_before = date;
        return this;
    }

    public Lookup range(long from, long to)
    {
        this.from_since = from;
        this.to_before = to;
        return this;
    }

    public Lookup setWorld(World world)
    {
        this.worldID = this.module.getCore().getWorldManager().getWorldId(world);
        return this;
    }

    public Lookup setLocation(Location location)
    {
        this.location1 = new BlockVector3(location.getBlockX(),location.getBlockY(),location.getBlockZ());
        this.location2 = null;
        return this.setWorld(location.getWorld());
    }

    public Lookup setSelection(Location location1, Location location2)
    {
        if (location1.getWorld() != location2.getWorld())
        {
            throw new IllegalArgumentException("Both locations must be in the same world!");
        }
        this.location1 = new BlockVector3(location1.getBlockX(),location1.getBlockY(),location1.getBlockZ());
        this.location2 = new BlockVector3(location2.getBlockX(),location2.getBlockY(),location2.getBlockZ());
        return this.setWorld(location1.getWorld());
    }

    public void clear()
    {
        this.logEntries.clear();
    }

    /**
     * Lookup excluding nothing
     * @return
     */
    public static Lookup general(Module module)
    {
        Lookup lookup = new Lookup(module);
        lookup.includeActions = false;
        lookup.clearActions();
        return lookup;
    }
    /**
     * Lookup only including container-actions
     */
    public static Lookup container(Module module)
    {
        Lookup lookup = new Lookup(module);
        lookup.includeActions = true;
        lookup.clearActions();
        lookup.includeActions(LOOKUP_CONTAINER);
        return lookup;
    }

    /**
     * Lookup only including kill-actions
     */
    public static Lookup kills(Module module)
    {
        Lookup lookup = new Lookup(module);
        lookup.includeActions = true;
        lookup.clearActions();
        lookup.includeActions(LOOKUP_KILLS);
        return lookup;
    }

    /**
     * Lookup only including player-actions
     */
    public static Lookup player(Module module)
    {
        Lookup lookup = new Lookup(module);
        lookup.includeActions = true;
        lookup.clearActions();
        lookup.includeActions(ActionType.LOOKUP_PLAYER);
        return lookup;
    }

    /**
     * Lookup only including block-actions
     */
    public static Lookup block(Module module)
    {
        Lookup lookup = new Lookup(module);
        lookup.includeActions = true;
        lookup.clearActions();
        lookup.includeActions(ActionType.LOOKUP_BLOCK);
        return lookup;
    }

    public void show(User user)
    {
        if (this.logEntries.isEmpty())
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
                else
                {
                    user.sendTranslated("&eNo logs found at &6%d&f:&6%d&f:&6%d&e in &6%s&e!",
                                     this.location1.x, this.location1.y, this.location1.z,
                                     this.module.getCore().getWorldManager().getWorld(worldID).getName());
                }
            }
            else
            {
                user.sendTranslated("&eNo logs found for your given parameters");
            }
            return;
        }
        user.sendTranslated("&aFound %d logs:", this.logEntries.size());
        Iterator<LogEntry> entries = this.logEntries.iterator();
        // compressing data: //TODO add if it should be compressed or not
        LogEntry entry = entries.next();
        ArrayList<LogEntry> compressedEntries = new ArrayList<LogEntry>();
        /*
        while (entries.hasNext())
        {
            LogEntry next = entries.next();
            if (entry.isSimilar(next)) // can be compressed ?
            {
                System.out.print("attach");//TODO remove
                entry.attach(next);
            }
            else // no more compression -> move on to next entry
            {
                System.out.print(compressedEntries.size() + "+1 add ");//TODO remove
                compressedEntries.add(entry);
                entry = next;
            }
        }
        */
        compressedEntries.addAll(this.logEntries); //TODO do the compressing
        for (LogEntry logEntry : compressedEntries)
        {
            switch (logEntry.getType())
            {
                    case WORLDEDIT:
            if (logEntry.getNewBlock().material.equals(Material.AIR))
            {
            user.sendTranslated("&2%s &aused worldedit to remove &6%s&a!",
                                logEntry.getCauserUser().getDisplayName(),
                               this.getPrettyName(logEntry.getOldBlock()));
            }
            else if (logEntry.getOldBlock().material.equals(Material.AIR))
            {
            user.sendTranslated("&2%s &aused worldedit to place &6%s&a!",
                                logEntry.getCauserUser().getDisplayName(),
                                this.getPrettyName(logEntry.getNewBlock()));
            }
            else
            {
            user.sendTranslated("&2%s &aused worldedit to replace &6%s&a with &6%s&a!",
                                logEntry.getCauserUser().getDisplayName(),
                                this.getPrettyName(logEntry.getOldBlock()),
                                this.getPrettyName(logEntry.getNewBlock()));
            }
            break;

            default:
                user.sendMessage("Something happened there for sure! "+logEntry.getType().name);
            }
        }
        user.sendMessage("Yeah thats all for now!");
    }

    private String getPrettyName(DyeColor color)
    {
        return color.name(); //TODO
    }


    private String getPrettyName(DamageCause dmgC)
    {
        return dmgC.name(); //TODO
    }

    private String getPrettyName(ItemData itemData)
    {
        //TODO

    }

    private String getPrettyName(EntityType entityType)
    {
        return entityType.name(); //TODO
    }

    private String getPrettyName(BlockData blockData)
    {
        //TODO painting has special data (hanging)
        return blockData.material+":"+blockData.data; //TODO
    }

    public Set<ActionType> getActions()
    {
        return actions;
    }

    public boolean hasIncludeActions()
    {
        return this.includeActions;
    }

    public Long getWorld()
    {
        return worldID;
    }

    public BlockVector3 getLocation1()
    {
        return location1;
    }

    public BlockVector3 getLocation2()
    {
        return location2;
    }

    public boolean hasTime()
    {
        return !(this.from_since == null && this.to_before == null);
    }

    public Long getFromSince()
    {
        return this.from_since;
    }

    public Long getToBefore()
    {
        return this.to_before;
    }

    public void addLogEntry(LogEntry entry)
    {
        this.logEntries.add(entry);
    }

    public Lookup clone()
    {
        Lookup lookup = new Lookup(this.module);
        lookup.actions = new CopyOnWriteArraySet<ActionType>(this.actions);
        lookup.includeActions = this.includeActions;
        lookup.from_since = this.from_since;
        lookup.to_before = this.to_before;
        lookup.location1 = this.location1;
        lookup.location2 = this.location2;
        lookup.worldID = this.worldID;
        lookup.users = new CopyOnWriteArraySet<Long>(this.users);
        lookup.includeUsers = this.includeUsers;
        lookup.blocks = new CopyOnWriteArraySet<BlockData>(this.blocks);
        lookup.includeBlocks = this.includeBlocks;
        return lookup;
    }

    /**
     player [name1] <name2> <name3> ...
     area <radius>
     selection, sel
     block [type1] <type2> <type3> ..., type [type1] <type2> <type3> ...
     created, destroyed
     chestaccess
     kills
     since [timespec], time [timespec]
     before [timespec]
     limit [count]
     sum [none|blocks|players]
     world [worldname]
     asc, desc
     coords
     silent
     last
     chat
     search, match
     loc, location (v1.51+)
     */
}
