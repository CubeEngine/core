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
            case BLOCK_BREAK:
            user.sendTranslated("&2%s &abroke &6%s&a!",
                                logEntry.getCauserUser().getDisplayName(),
                                this.getPrettyName(logEntry.getOldBlock()));
            break;
            case BLOCK_BURN:
            user.sendTranslated("&6%s &awent up into flames!",
                    this.getPrettyName(logEntry.getOldBlock()));
            break;
            case BLOCK_FADE:
            user.sendTranslated("&6%s &afaded away!",
                   this.getPrettyName(logEntry.getOldBlock()));
            break;
            case LEAF_DECAY:
            user.sendTranslated("&6%s &adecayed!",
                                this.getPrettyName(logEntry.getOldBlock()));
            break;
            case WATER_BREAK:
            user.sendTranslated("&6%s &agot flushed away by water!",
                                this.getPrettyName(logEntry.getOldBlock()));
            break;
            case LAVA_BREAK:
            user.sendTranslated("&6%s &agot destroyed by lava!",
                                this.getPrettyName(logEntry.getOldBlock()));
            break;
            case ENTITY_BREAK:
            user.sendTranslated("&aA &6%s &adestroyed &6%s&a!",
                                this.getPrettyName(logEntry.getCauserEntity()),
                                this.getPrettyName(logEntry.getOldBlock()));
            break;
            case ENDERMAN_PICKUP:
            user.sendTranslated("&6%s &agot picked up by an enderman!",
                                this.getPrettyName(logEntry.getOldBlock()));
            break;
            case BUCKET_FILL:
            // TODO attached
            if (logEntry.getOldBlock().material.equals(Material.LAVA) ||
                logEntry.getOldBlock().material.equals(Material.STATIONARY_LAVA))
            {
                user.sendTranslated("&2%s &afilled a bucket with lava!",
                                    logEntry.getCauserUser().getDisplayName());
            }
            else if (logEntry.getOldBlock().material.equals(Material.WATER) ||
                logEntry.getOldBlock().material.equals(Material.STATIONARY_WATER))
            {
                user.sendTranslated("&2%s &afilled a bucket with water!",
                                    logEntry.getCauserUser().getDisplayName());
            }
            else
            {
                user.sendTranslated("&2%s &afilled a bucket with some random fluids&r&a!",
                                    logEntry.getCauserUser().getDisplayName());
            }
            break;
            case CROP_TRAMPLE:
            // TODO attached log only show the crop trampled down then
            user.sendTranslated("&2%s &atrampeled down &6%s&a!",
                                logEntry.getCauserUser().getDisplayName(),
                                this.getPrettyName(logEntry.getOldBlock()));
            break;
            case ENTITY_EXPLODE:
            //TODO attached
            user.sendTranslated("&aSomething unknown blew up &6%s&a!",
                            this.getPrettyName(logEntry.getOldBlock()));
            break;
            case CREEPER_EXPLODE:
            //TODO attached
            if (logEntry.getCauserUser() == null)
            {
            user.sendTranslated("&aA Creeper-Explosion wrecked &6%s&a!",
                                this.getPrettyName(logEntry.getOldBlock()));
            }
            else
            {
            user.sendTranslated("&2%s &alet a Creeper detonate and destroy &6%s&a!",
                                logEntry.getCauserUser().getDisplayName(),
                                this.getPrettyName(logEntry.getOldBlock()));
            }
            break;
            case TNT_EXPLODE:
            //TODO attached
            //TODO if player ignited show it!
            user.sendTranslated("&aA TNT-Explosion got rid of &6%s&a!",
                            this.getPrettyName(logEntry.getOldBlock()));
            break;
            case FIREBALL_EXPLODE:
            //TODO attached
            user.sendTranslated("&aAn Fireball blasted away &6%s&a!",
                            this.getPrettyName(logEntry.getOldBlock()));
            break;
            case ENDERDRAGON_EXPLODE:
            //TODO attached
            user.sendTranslated("&aAn enderdragon changed the integrity of &6%s&a!",
                            this.getPrettyName(logEntry.getOldBlock()));
            break;
            case WITHER_EXPLODE:
            //TODO attached
            user.sendTranslated("&6%s&a got destroyed by a WitherBoss-Explosion!",
                            this.getPrettyName(logEntry.getOldBlock()));
            break;
            case TNT_PRIME:
            //TODO attached
            user.sendTranslated("&2%s &aignited TNT!",
                            logEntry.getCauserUser().getDisplayName());
            break;
            case BLOCK_PLACE:
            if (logEntry.hasAttached())
            {
            //TODO
            }
            else // single
            {
            if (logEntry.getOldBlock().material.equals(Material.AIR))
            {
                user.sendTranslated("&2%s &areplaced &6%s&a with &6%s&a!",
                                    logEntry.getCauserUser().getDisplayName(),
                                    this.getPrettyName(logEntry.getOldBlock()),
                                    this.getPrettyName(logEntry.getNewBlock()));
            }
            else
            {
                user.sendTranslated("&2%s &aplaced &6%s&a!",
                                    logEntry.getCauserUser().getDisplayName(),
                                    this.getPrettyName(logEntry.getNewBlock()));
            }
            }
            break;
            case LAVA_BUCKET:
            user.sendTranslated("&2%s &aemptied a lava-bucket!",
                            logEntry.getCauserUser().getDisplayName());
            break;
            case WATER_BUCKET:
            user.sendTranslated("&2%s &aemptied a water-bucket!",
                            logEntry.getCauserUser().getDisplayName());
            break;
            case NATURAL_GROW:
            //TODO attach / replace
            user.sendTranslated("&6%s &agrew naturally!",
                            this.getPrettyName(logEntry.getNewBlock()));
            break;
            case PLAYER_GROW:
            //TODO attach / replace
            user.sendTranslated("&2%s &alet grow &6%s&a!",
                            logEntry.getCauserUser().getDisplayName(),
                            this.getPrettyName(logEntry.getNewBlock()));
            break;
            case BLOCK_FORM:
            //TODO attach
            user.sendTranslated("&6%s &aformed naturally!",
                            this.getPrettyName(logEntry.getNewBlock()));
            break;
            case ENDERMAN_PLACE:
            user.sendTranslated("&6%s &agot placed by an enderman!",
                            this.getPrettyName(logEntry.getNewBlock()));
            break;
            case ENTITY_FORM:
            //TODO attach
            user.sendTranslated("&6%s &aformed &6%s&a!",
                            this.getPrettyName(logEntry.getCauserEntity()),
                            this.getPrettyName(logEntry.getNewBlock()));
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
                    logEntry.getCauserUser().getDisplayName());
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
                            this.getPrettyName(logEntry.getNewBlock()));
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
                            this.getPrettyName(logEntry.getOldBlock()));
            break;
            case BLOCK_FALL:
                if (logEntry.getCauserUser() == null)
                {
                    ActionType type = ActionType.getById(logEntry.getAdditional().get("cause").asInt());
                    user.sendTranslated("&6%s&a did fall to a lower place! This was caused by %s.",
                                        this.getPrettyName(logEntry.getOldBlock()), type.name);
                }
                else
                {
                    user.sendTranslated("&2%s &acaused &6%s&a to fall to a lower place!",
                                       logEntry.getCauserUser().getDisplayName(),
                                       this.getPrettyName(logEntry.getOldBlock()));
                }
            break;
            case SIGN_CHANGE:
                Iterator<JsonNode> oldSignIterator = logEntry.getAdditional().get("oldSign").iterator();
                Iterator<JsonNode> newSignIterator = logEntry.getAdditional().get("sign").iterator();
                boolean oldEmpty = true;
                ArrayList<String> oldLines = new ArrayList<String>();
                ArrayList<String> newLines = new ArrayList<String>();
                while (oldSignIterator.hasNext())
                {
                    String line = oldSignIterator.next().asText();
                    if (!line.isEmpty())
                    {
                        oldEmpty = false;
                    }
                    oldLines.add(line);
                }
                while (newSignIterator.hasNext())
                {
                    String line = newSignIterator.next().asText();
                    newLines.add(line);
                }
                String delim = ChatFormat.parseFormats("&7 | &f");
                if (oldEmpty)
                {
                    user.sendTranslated("&2%s &awrote &7[&f%s&7]&a on a sign!",
                                        logEntry.getCauserUser().getDisplayName(),
                                        StringUtils.implode(delim ,newLines));
                }
                else
                {
                    user.sendTranslated("&2%s &awrote &7[&f%s&7]&a on a sign! The old signtext was &7[&f%s&7]&a!",
                                        logEntry.getCauserUser().getDisplayName(),
                                        StringUtils.implode(delim,newLines),
                                        StringUtils.implode(delim,oldLines));
                }

            user.sendTranslated("&aThis is a Signchange"); //TODO
            break;
            case SHEEP_EAT:
            user.sendTranslated("&aA sheep ate all the grass!");
            break;
            case BONEMEAL_USE:
            //TODO attach
            Material mat = Material.getMaterial(logEntry.getAdditional().iterator().next().asText());
            user.sendTranslated("&2%s &aused bonemeal on &6%s&a!",
                            logEntry.getCauserUser().getDisplayName(),
                               this.getPrettyName(new BlockData(mat)));
            break;
            case LEVER_USE:
                if ((logEntry.getNewBlock().data & 0x8) == 0x8)
                {
                    user.sendTranslated("&2%s &aactivated the lever!",
                                        logEntry.getCauserUser().getDisplayName());
                }
                else
                {
                    user.sendTranslated("&2%s &adeactivated the lever!",
                                        logEntry.getCauserUser().getDisplayName());
                }
            break;
            case REPEATER_CHANGE:
                int delay = (logEntry.getNewBlock().data >> 2) + 1;
                user.sendTranslated("&2%s &aset the repeater to &6%d &aticks delay!",
                                    logEntry.getCauserUser().getDisplayName(), delay);
            // TODO attach (show the actual change no change -> fiddled around but did not change anything)
            break;
            case NOTEBLOCK_CHANGE:
                int clicks = logEntry.getNewBlock().data;
                user.sendTranslated("&2%s &aset the noteblock to &6%d&a clicks!",
                            logEntry.getCauserUser().getDisplayName(), clicks);
                // TODO attach (show the actual change no change -> fiddled around but did not change anything)
            break;
            case DOOR_USE:
                if (!((logEntry.getOldBlock().data & 0x4) == 0x4))
                {
                    user.sendTranslated("&2%s &aopened the &6%s&a!",
                                        logEntry.getCauserUser().getDisplayName(),
                                       this.getPrettyName(logEntry.getOldBlock()));
                }
                else
                {
                    user.sendTranslated("&2%s &aclosed the &6%s&a!",
                                        logEntry.getCauserUser().getDisplayName(),
                                        this.getPrettyName(logEntry.getOldBlock()));
                }
            break;
            case CAKE_EAT:
                int piecesLeft = 6 - logEntry.getNewBlock().data;
                if (piecesLeft == 0)
                {
                    user.sendTranslated("&aThe cake is a lie! Ask &2%s &ahe knows it!",
                                        logEntry.getCauserUser().getDisplayName());
                }
                else
                {
                    user.sendTranslated("&2%s &aate a piece of cake!",
                                        logEntry.getCauserUser().getDisplayName());
                }
            break;
            case COMPARATOR_CHANGE:
                if (logEntry.getNewBlock().material.equals(Material.REDSTONE_COMPARATOR_ON))
                {
                    user.sendTranslated("&2%s &aactivated the comparator!",
                                        logEntry.getCauserUser().getDisplayName());
                }
                else
                {
                    user.sendTranslated("&2%s &adeactivated the comparator!",
                                        logEntry.getCauserUser().getDisplayName());
                }
            break;
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
            case CONTAINER_ACCESS:
            user.sendTranslated("&2%s &alooked into a &6%s!",
                            logEntry.getCauserUser().getDisplayName(),
                           this.getPrettyName(logEntry.getOldBlock()));
            break;
            case BUTTON_USE:
            user.sendTranslated("&2%s &apressed a &6%s&a!",
                            logEntry.getCauserUser().getDisplayName(),
                            this.getPrettyName(logEntry.getOldBlock()));
            break;
            case FIREWORK_USE:
            user.sendTranslated("&2%s &aused a firework rocket!",
                            logEntry.getCauserUser().getDisplayName());
            break;
            case VEHICLE_ENTER:
                if (logEntry.getCauserUser() == null)
                {
                    user.sendTranslated("&6%s &aentered a &6%s&a!",
                                        this.getPrettyName(logEntry.getCauserEntity()),
                                        this.getPrettyName(logEntry.getEntity()));
                }
                else
                {
                    user.sendTranslated("&2%s &aentered a &6%s&a!",
                                        logEntry.getCauserUser().getDisplayName(),
                                        this.getPrettyName(logEntry.getEntity()));
                }
            break;
            case VEHICLE_EXIT:
                if (logEntry.getCauserUser() == null)
                {
                    user.sendTranslated("&6%s &aexited a &6%s&a!",
                                        this.getPrettyName(logEntry.getCauserEntity()),
                                        this.getPrettyName(logEntry.getEntity()));
                }
                else
                {
                    user.sendTranslated("&2%s &aexited a &6%s&a!",
                                        logEntry.getCauserUser().getDisplayName(),
                                        this.getPrettyName(logEntry.getEntity()));
                }
            break;
            case POTION_SPLASH:
            user.sendMessage("Potion stuff happened!");//TODO
            break;
            case PLATE_STEP:
                user.sendTranslated("&2%s &astepped on a &6%s&a!",
                                    logEntry.getCauserUser().getDisplayName(),
                                    this.getPrettyName(logEntry.getOldBlock()));
                break;
            case MILK_FILL:
                user.sendTranslated("&2%s &amilked a cow!",
                            logEntry.getCauserUser().getDisplayName());
                break;
            case SOUP_FILL:
                user.sendTranslated("&2%s &amade soup with a mooshroom!",
                            logEntry.getCauserUser().getDisplayName());
            case VEHICLE_PLACE:
                user.sendTranslated("&2%s &aplaced a &6%s&a!",
                            logEntry.getCauserUser().getDisplayName(),
                            this.getPrettyName(logEntry.getEntity()));
            break;
            case VEHICLE_BREAK:
                user.sendTranslated("&2%s &aebroke a &6%s&a!",
                            logEntry.getCauserUser() == null ?
                            this.getPrettyName(logEntry.getCauserEntity()) :
                            logEntry.getCauserUser().getDisplayName(),
                            this.getPrettyName(logEntry.getEntity()));
            break;
            case HANGING_BREAK:
                if (logEntry.getItemData() == null)
                {
                    user.sendTranslated("&6%s&a got removed by &2%s&a!",
                                    this.getPrettyName(logEntry.getOldBlock()),
                                    logEntry.getCauserUser().getDisplayName());
                }
                else
                {
                    user.sendTranslated("&2%s &abroke an &6itemframe&a containing &6%s&a!",
                                    logEntry.getCauserUser().getDisplayName(),
                                    this.getPrettyName(logEntry.getItemData()));
                }
            break;
            case HANGING_PLACE:
                user.sendTranslated("&6%s &agot hung up by &2%s&a!",
                            this.getPrettyName(logEntry.getNewBlock()),
                            logEntry.getCauserUser().getDisplayName());
            break;
            case PLAYER_DEATH:
            if (logEntry.getCauserUser() != null)
            {
            user.sendTranslated("&2%s &agot slaughtered by &2%s&a!",
                                logEntry.getUser().getDisplayName(),
                                logEntry.getCauserUser().getDisplayName());
            }
            else if (logEntry.getCauserEntity() != null)
            {
            user.sendTranslated("&2%s &acould not escape &6%s&a!",
                                logEntry.getUser().getDisplayName(),
                                this.getPrettyName(logEntry.getCauserEntity()));
            }
            else // something else
            {
                JsonNode json = logEntry.getAdditional();
                DamageCause dmgC = DamageCause.valueOf(json.get("dmgC").asText());
                user.sendTranslated("&2%s &adied! &f(&6%s&f)",
                                    logEntry.getUser().getDisplayName(),
                                    this.getPrettyName(dmgC));
            }
            break;
            case PET_DEATH:
                if (Match.entity().isTameable(logEntry.getEntity()))
                {
                    JsonNode json = logEntry.getAdditional();
                    if (json.get("owner") != null)
                    {
                        User owner = this.module.getCore().getUserManager().getExactUser(json.get("owner").asText());
                        if (logEntry.getCauserUser() != null)
                        {
                            user.sendTranslated("&aThe &6%s&a of &2%s &agot slaughtered by &2%s&a!",
                                                this.getPrettyName(logEntry.getEntity()),
                                                owner.getDisplayName(),
                                                logEntry.getCauserUser().getDisplayName());
                        }
                        else if (logEntry.getCauserEntity() != null)
                        {
                            user.sendTranslated("&aThe &6%s&a of &2%s &acould not escape &6%s&a!",
                                                this.getPrettyName(logEntry.getEntity()),
                                                owner.getDisplayName(),
                                                this.getPrettyName(logEntry.getCauserEntity()));
                        }
                        else // something else
                        {
                            user.sendTranslated("&aThe &6%s&a of &2%s &adied!",
                                                this.getPrettyName(logEntry.getEntity()),
                                                owner.getDisplayName());
                        }
                        break;
                    }
                }
                user.sendTranslated("&6%s &adied! &4(Pet without owner)", this.getPrettyName(logEntry.getEntity()));
            break;
            case MONSTER_DEATH:
            case ANIMAL_DEATH:
            case BOSS_DEATH:
            case NPC_DEATH:
            case OTHER_DEATH:
            if (logEntry.getCauserUser() != null)
            {
            user.sendTranslated("&6%s &agot slaughtered by &2%s&a!",
                                this.getPrettyName(logEntry.getEntity()),
                                logEntry.getCauserUser().getDisplayName());
            }
            else if (logEntry.getCauserEntity() != null)
            {
            user.sendTranslated("&6%s &acould not escape &6%s&a!",
                                this.getPrettyName(logEntry.getEntity()),
                                this.getPrettyName(logEntry.getCauserEntity()));
            }
            else // something else
            {
            user.sendTranslated("&6%s &adied! &f(&6%s&f)",
                                this.getPrettyName(logEntry.getEntity()),
                               "CAUSE"); //TODO get cause from json
            }
            break;
            case MONSTER_EGG_USE:
                EntityType entityType = EntityType.fromId(logEntry.getItemData().dura); // Dura is entityTypeId
                user.sendTranslated("&2%s &aspawned &6%s&a!",
                                    logEntry.getCauserUser().getDisplayName(),
                                    this.getPrettyName(entityType));
            break;
            case NATURAL_SPAWN:
                user.sendTranslated("&6%s &aspawned naturally!",
                                  this.getPrettyName(logEntry.getCauserEntity()));
            break;
            case SPAWNER_SPAWN:
                user.sendTranslated("&6%s &aspawned from a spawner!",
                                    this.getPrettyName(logEntry.getCauserEntity()));
            break;
            case OTHER_SPAWN:
                user.sendTranslated("&6%s &aspawned!",
                                    this.getPrettyName(logEntry.getCauserEntity())); //TODO get player in data
            break;
            case ITEM_DROP: //TODO itemdrops that are not caused by user
                user.sendTranslated("&2%s&a dropped %d &6%s!",
                                    logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getItemData().amount,
                                    this.getPrettyName(logEntry.getItemData()));
            break;
            case ITEM_PICKUP:
                user.sendTranslated("&2%s&a picked up %d &6%s!",
                                    logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getItemData().amount,
                                    this.getPrettyName(logEntry.getItemData()));
            break;
            case XP_PICKUP:
                int amount = logEntry.getAdditional().iterator().next().asInt();
                user.sendTranslated("&2%s&a earned &6%d experience!",
                                    logEntry.getCauserUser().getDisplayName(), amount);
            break;
            case ENTITY_SHEAR:
                user.sendTranslated("&2%s&a sheared &6%d&a!",
                                    logEntry.getCauserUser().getDisplayName(),
                                    this.getPrettyName(logEntry.getEntity()));
            break;
            case ENTITY_DYE:
                JsonNode json = logEntry.getAdditional();
                DyeColor color = DyeColor.valueOf(json.get("nColor").asText());
                user.sendTranslated("&2%s&a dyed a &6%s&a in &6%s&a!",
                                    logEntry.getCauserUser().getDisplayName(),
                                    this.getPrettyName(logEntry.getEntity()),
                                    this.getPrettyName(color));
            break;
            case ITEM_INSERT:
                user.sendTranslated("&2%s&a placed &6%d %s&a into &6%s&a!",
                                    logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getItemData().amount,
                                    this.getPrettyName(logEntry.getItemData()),
                                    this.getPrettyName(logEntry.getNewBlock()));
            break;
            case ITEM_REMOVE:
                user.sendTranslated("&2%s&a took &6%d %s&a out of &6%s&a!",
                                    logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getItemData().amount,
                                    this.getPrettyName(logEntry.getItemData()),
                                    this.getPrettyName(logEntry.getNewBlock()));
            break;
            case ITEM_TRANSFER:
                user.sendTranslated("&6%s&a got moved out of &6%s&a!",
                                    this.getPrettyName(logEntry.getItemData()),
                                    this.getPrettyName(logEntry.getNewBlock()));
            break;
            case PLAYER_COMMAND:
                user.sendTranslated("&2%s&a used the command &f\"&6%s&f\"&a!",
                                    logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getAdditional().iterator().next().asText());
            break;
            case PLAYER_CHAT:
                user.sendTranslated("&2%s&a chatted the following: &f\"&6%s&f\"",
                                    logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getAdditional().iterator().next().asText());
            break;
            case PLAYER_JOIN:
                user.sendTranslated("&2%s&a joined the server!",
                                    logEntry.getCauserUser().getDisplayName());
            break;
            case PLAYER_QUIT:
                user.sendTranslated("&2%s&a leaved the server!",
                                    logEntry.getCauserUser().getDisplayName());
            break;
            case PLAYER_TELEPORT:
                user.sendTranslated("PLAYER_TELEPORT"); //TODO
            break;
            case ENCHANT_ITEM:
                user.sendTranslated("&2%s&a enchanted &6%s!",
                                    logEntry.getCauserUser().getDisplayName(),
                                    this.getPrettyName(logEntry.getItemData()));
                //TODO list enchantments
            break;
            case CRAFT_ITEM:
                user.sendTranslated("&2%s&a crafted &6%s!",
                                    logEntry.getCauserUser().getDisplayName(),
                                    this.getPrettyName(logEntry.getItemData()));
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
        String result = itemData.material.name()+":"+itemData.dura;
        if (itemData.displayName != null)
        {
            result += " ("+ itemData.displayName+ ")";
        }
        return result;
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
