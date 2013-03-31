package de.cubeisland.cubeengine.log.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.math.BlockVector3;

import gnu.trove.set.hash.THashSet;

import static de.cubeisland.cubeengine.log.storage.ActionType.LOOKUP_CONTAINER;
import static de.cubeisland.cubeengine.log.storage.ActionType.LOOKUP_KILLS;


public class Lookup
{
    private final Module module;

    // Lookup Types:
    // Full lookup / all action types / can set time & location
    // Player lookup / all player related / time / location / users
    // Block lookup / all block related / time / location / block MAT:id  | < WORLDEDIT 0x4B || 0x61 || 0x63 (hangings)

    // The actions to look for
    private Set<ActionType> actions = new THashSet<ActionType>();
    private boolean includeActions = true;
    // When (since/before/from-to)
    private Long from_since;
    private Long to_before;
    // Where (in world / at location1 / in between location1 and location2)
    private BlockVector3 location1;
    private BlockVector3 location2;
    private Long worldID;
    // users
    private Set<Long> users;
    private boolean includeUsers = true;
    //Block-Logs:
    private Set<BlockData> blocks;
    private boolean includeBlocks = true;
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
        while (entries.hasNext())
        {
            LogEntry next = entries.next();
            if (entry.isSimilar(next)) // can be compressed ?
            {
                entry.attach(next);
            }
            else // no more compression -> move on to next entry
            {
                compressedEntries.add(entry);
                entry = next;
            }
        }
        for (LogEntry logEntry : compressedEntries)
        {
            switch (logEntry.getType())
            {
                case BLOCK_BREAK:
                    if (logEntry.hasAttached())
                    {
                        //TODO
                    }
                    else // single
                    {
                        user.sendTranslated("&2%s &abroke &6%s&a!",
                            entry.getCauserUser().getDisplayName(),
                            this.getPrettyName(entry.getOldBlock()));
                    }
                break;
                case BLOCK_BURN:
                    user.sendTranslated("&6%s &awent up into flames!",
                            this.getPrettyName(entry.getOldBlock()));
                break;
                case BLOCK_FADE:
                    user.sendTranslated("&6%s &afaded away!",
                           this.getPrettyName(entry.getOldBlock()));
                break;
                case LEAF_DECAY:
                    user.sendTranslated("&6%s &adecayed!",
                                        this.getPrettyName(entry.getOldBlock()));
                break;
                case WATER_BREAK:
                    user.sendTranslated("&6%s &agot flushed away by water!",
                                        this.getPrettyName(entry.getOldBlock()));
                break;
                case LAVA_BREAK:
                    user.sendTranslated("&6%s &agot destroyed by lava!",
                                        this.getPrettyName(entry.getOldBlock()));
                break;
                case ENTITY_BREAK:
                    user.sendTranslated("&aA &6%s &adestroyed &6%s&a!",
                                        this.getPrettyName(entry.getCauserEntity()),
                                        this.getPrettyName(entry.getOldBlock()));
                break;
                case ENDERMAN_PICKUP:
                    user.sendTranslated("&6%s &agot picked up by an enderman!",
                                        this.getPrettyName(entry.getOldBlock()));
                break;
                case BUCKET_FILL:
                    // TODO attached
                    if (entry.getOldBlock().material.equals(Material.LAVA) ||
                        entry.getOldBlock().material.equals(Material.STATIONARY_LAVA))
                    {
                        user.sendTranslated("&2%s &afilled a bucket with lava!",
                                            entry.getCauserUser().getDisplayName());
                    }
                    else if (entry.getOldBlock().material.equals(Material.WATER) ||
                        entry.getOldBlock().material.equals(Material.STATIONARY_WATER))
                    {
                        user.sendTranslated("&2%s &afilled a bucket with water!",
                                            entry.getCauserUser().getDisplayName());
                    }
                    else
                    {
                        user.sendTranslated("&2%s &afilled a bucket with some random fluids&r&a!",
                                            entry.getCauserUser().getDisplayName());
                    }
                break;
                case CROP_TRAMPLE:
                    // TODO attached log only show the crop trampled down then
                    user.sendTranslated("&2%s &atrampeled down &6%s&a!",
                                        entry.getCauserUser().getDisplayName(),
                                        this.getPrettyName(entry.getOldBlock()));
                break;
            case ENTITY_EXPLODE:
                //TODO attached
                user.sendTranslated("&aSomething unknown blew up &6%s&a!",
                                    this.getPrettyName(entry.getOldBlock()));
                break;
            case CREEPER_EXPLODE:
                //TODO attached
                if (entry.getCauserUser() == null)
                {
                    user.sendTranslated("&aA Creeper-Explosion wrecked &6%s&a!",
                                        this.getPrettyName(entry.getOldBlock()));
                }
                else
                {
                    user.sendTranslated("&2%s &alet a Creeper detonate and destroy &6%s&a!",
                                        entry.getCauserUser().getDisplayName(),
                                        this.getPrettyName(entry.getOldBlock()));
                }
                break;
            case TNT_EXPLODE:
                //TODO attached
                //TODO if player ignited show it!
                user.sendTranslated("&aA TNT-Explosion got rid of &6%s&a!",
                                    this.getPrettyName(entry.getOldBlock()));
                break;
            case FIREBALL_EXPLODE:
                //TODO attached
                user.sendTranslated("&aAn Fireball blasted away &6%s&a!",
                                    this.getPrettyName(entry.getOldBlock()));
                break;
            case ENDERDRAGON_EXPLODE:
                //TODO attached
                user.sendTranslated("&aAn enderdragon changed the integrity of &6%s&a!",
                                    this.getPrettyName(entry.getOldBlock()));
                break;
            case WITHER_EXPLODE:
                //TODO attached
                user.sendTranslated("&6%s&a got destroyed by a WitherBoss-Explosion!",
                                    this.getPrettyName(entry.getOldBlock()));
                break;
            case TNT_PRIME:
                //TODO attached
                user.sendTranslated("&2%s &aignited TNT!",
                                    entry.getCauserUser().getDisplayName());
                break;
            case BLOCK_PLACE:
                if (logEntry.hasAttached())
                {
                    //TODO
                }
                else // single
                {
                    if (entry.getOldBlock() != null)
                    {
                        user.sendTranslated("&2%s &areplaced &6%s&a!",
                                            entry.getCauserUser().getDisplayName(),
                                            this.getPrettyName(entry.getNewBlock()));
                    }
                    else
                    {
                        user.sendTranslated("&2%s &aplaced &6%s&a!",
                                            entry.getCauserUser().getDisplayName(),
                                            this.getPrettyName(entry.getNewBlock()));
                    }
                }
                break;
            case LAVA_BUCKET:
                user.sendTranslated("&2%s &aemptied a lava-bucket!",
                                    entry.getCauserUser().getDisplayName());
                break;
            case WATER_BUCKET:
                user.sendTranslated("&2%s &aemptied a water-bucket!",
                                    entry.getCauserUser().getDisplayName());
                break;
            case NATURAL_GROW:
                //TODO attach / replace
                user.sendTranslated("&6%s &agrew naturally!",
                                    this.getPrettyName(entry.getNewBlock()));
                break;
            case PLAYER_GROW:
                //TODO attach / replace
                user.sendTranslated("&2%s &alet grow &6%s&a!",
                                    entry.getCauserUser().getDisplayName(),
                                    this.getPrettyName(entry.getNewBlock()));
                break;
            case BLOCK_FORM:
                //TODO attach
                user.sendTranslated("&6%s &aformed naturally!",
                                    this.getPrettyName(entry.getNewBlock()));
                break;
            case ENDERMAN_PLACE:
                user.sendTranslated("&6%s &agot placed by an enderman!",
                                    this.getPrettyName(entry.getNewBlock()));
                break;
            case ENTITY_FORM:
                //TODO attach
                user.sendTranslated("&6%s &aformed &6%s&a!",
                                    this.getPrettyName(entry.getCauserEntity()),
                                    this.getPrettyName(entry.getNewBlock()));
                break;
            case FIRE_SPREAD:
                //TODO attach
                user.sendTranslated("&aFire spreaded!");
                break;
            case FIREBALL_IGNITE:
                //TODO attach
                user.sendTranslated("&aFire got set by a FireBall!");
                break;
            case LIGHTER:
                //TODO attach
                user.sendTranslated("&2%s &aset fire!",
                            entry.getCauserUser().getDisplayName());
                break;
            case LAVA_IGNITE:
                user.sendTranslated("&aFire got set by lava!");
                break;
            case LIGHTNING:
                user.sendTranslated("&aFire got set by a lightning strike!");
                break;
            case BLOCK_SPREAD:
                //TODO attach
                user.sendTranslated("&6%s&a spreaded!",
                                    this.getPrettyName(entry.getNewBlock()));
                break;
            case WATER_FLOW:
                //TODO attach
                user.sendTranslated("&aJust some water flowing.");
                break;
            case LAVA_FLOW:
                //TODO attach
                user.sendTranslated("&aLava flowing.");
                break;
            case OTHER_IGNITE:
                user.sendTranslated("&aFire got set by an explosion or smth else!");
                break;
            case BLOCK_SHIFT:
                //TODO attach
                user.sendTranslated("&6%s&a got moved away by a Piston!",
                                    this.getPrettyName(entry.getOldBlock()));
                break;
            case BLOCK_FALL:
                user.sendTranslated("&6%s&a did fall to a lower place!",
                                    this.getPrettyName(entry.getOldBlock()));
                break;
            case SIGN_CHANGE:
                user.sendTranslated("&aThis is a Signchange"); //TODO
                break;
            case SHEEP_EAT:
                user.sendTranslated("&aA sheep ate all the grass!");
                break;
            case BONEMEAL_USE:
                //TODO attach
                user.sendTranslated("&2%s &aused bonemeal!", //TODO getMaterial from additionalData / or better put it into block and also add byte data
                                    entry.getCauserUser().getDisplayName());
                break;
            case LEVER_USE:
                // TODO get data from old block and say if switch on or off
                user.sendTranslated("&2%s &aused the lever!",
                                    entry.getCauserUser().getDisplayName());
                break;
            case REPEATER_CHANGE:
                // TODO attach (show the actual change no change -> fiddled around but did not change anything)
                user.sendTranslated("&2%s &amanipulated the repeater!", //TODO data to what?
                                    entry.getCauserUser().getDisplayName());
                break;
            case NOTEBLOCK_CHANGE:
                // TODO attach (show the actual change no change -> fiddled around but did not change anything)
                user.sendTranslated("&2%s &amanipulated the noteblock!", //TODO data to what?
                                    entry.getCauserUser().getDisplayName());
                break;
            case DOOR_USE:
                //TODO open / close   attach
                user.sendTranslated("&2%s &aused the door!",
                                    entry.getCauserUser().getDisplayName());
                break;
            case CAKE_EAT:
                //TODO attach / newstate
                user.sendTranslated("&2%s &aate a bit of cake!",
                                    entry.getCauserUser().getDisplayName());
                break;
            case COMPARATOR_CHANGE:
                //TODO attach / newstate
                user.sendTranslated("&2%s &amanipulated a comparator!",
                                    entry.getCauserUser().getDisplayName());
                break;
            case WORLDEDIT:
                if (entry.getNewBlock().material.equals(Material.AIR))
                {
                    user.sendTranslated("&2%s &aused worldedit to remove &6%s&a!",
                                        entry.getCauserUser().getDisplayName(),
                                       this.getPrettyName(entry.getOldBlock()));
                }
                else if (entry.getOldBlock().material.equals(Material.AIR))
                {
                    user.sendTranslated("&2%s &aused worldedit to place &6%s&a!",
                                        entry.getCauserUser().getDisplayName(),
                                        this.getPrettyName(entry.getNewBlock()));
                }
                else
                {
                    user.sendTranslated("&2%s &aused worldedit to replace &6%s&a with &6%s&a!",
                                        entry.getCauserUser().getDisplayName(),
                                        this.getPrettyName(entry.getOldBlock()),
                                        this.getPrettyName(entry.getNewBlock()));
                }
                break;
            //TODO more
            default:
                user.sendMessage("Something happened there for sure!");
            }
        }
        user.sendMessage("Yeah thats all for now!");
    }

    private String getPrettyName(EntityType entityType)
    {
        return entityType.name(); //TODO
    }

    private String getPrettyName(BlockData blockData)
    {
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
