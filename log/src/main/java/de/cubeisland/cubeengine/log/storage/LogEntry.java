package de.cubeisland.cubeengine.log.storage;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.math.BlockVector3;
import de.cubeisland.cubeengine.log.Log;

import com.fasterxml.jackson.databind.node.ObjectNode;
import sun.misc.Signal;

public class LogEntry implements Comparable<LogEntry>
{
    private final Log module;

    private final long entryID;
    private final Timestamp timestamp;
    private final boolean hasBlock;
    private final boolean hasUser;
    private final boolean isKill;
    private final World world;
    private final BlockVector3 location;

    private User causer_user;
    private EntityType causer_entity;

    private BlockData oldBlock;
    private BlockData newBlock;

    private ItemData itemData;
    private ItemStack item;
    private InventoryType inventoryType;
    private final ActionType actionType;
    private Set<LogEntry> attached = new HashSet<LogEntry>();
    private EntityType entity;
    private User user;
    private DamageCause damageCause;
    private ObjectNode additional;

    public LogEntry(Log module, long entryID, Timestamp timestamp, int action, long worldId, int x, int y, int z,
                    long causer, String block, Long data, String newBlock, Integer newData, String additionalData)
    {
        this.module = module;
        this.entryID = entryID;
        this.timestamp = timestamp;

        this.actionType = ActionType.getById(action);
        this.hasBlock = ActionType.LOOKUP_BLOCK.contains(actionType);
        this.hasUser = ActionType.LOOKUP_PLAYER.contains(actionType);
        this.isKill = ActionType.LOOKUP_KILLS.contains(actionType);

        this.world = module.getCore().getWorldManager().getWorld(worldId);
        this.location = new BlockVector3(x,y,z);

        if (causer < 0) // entity
        {
            //TODO check if i used this for other than negative entity ID
            this.causer_entity = EntityType.fromId((int)-causer);
        }
        else if (causer > 0) // causer > 0 player
        {
            this.causer_user = this.module.getCore().getUserManager().getUser(causer);
        } // else field in database was NULL or 0
        if (hasBlock)
        {
            this.oldBlock = new BlockData(Material.getMaterial(block),data.byteValue());
            this.newBlock = new BlockData(Material.getMaterial(newBlock),newData.byteValue());
        }
        else
        {
            switch (actionType)
            {
            case ITEM_INSERT:
            case ITEM_REMOVE:
            case ITEM_TRANSFER:
                try
                {
                    this.inventoryType = InventoryType.valueOf(newBlock);
                }
                catch (IllegalArgumentException ex)
                {
                    throw new IllegalStateException(newBlock + " is not a valid ContainerType");
                }
            case ITEM_DROP:
            case MONSTER_EGG_USE:
            case ITEM_PICKUP:
            case HANGING_BREAK:
                if (additionalData == null)
                    break;
                this.itemData = ItemData.deserialize(additionalData, this.module.getLogManager().mapper);
                this.item = itemData.toItemStack();
                break;
            case CONTAINER_ACCESS:
                this.oldBlock = new BlockData(Material.getMaterial(block));
                break;
            case VEHICLE_ENTER:
            case VEHICLE_EXIT:
            case VEHICLE_BREAK:
            case VEHICLE_PLACE:
                this.entity = EntityType.fromId(data.intValue());
                break;
            case PLAYER_DEATH:
                this.user = this.module.getCore().getUserManager().getUser(data);
            case MONSTER_DEATH:
            case ANIMAL_DEATH:
            case PET_DEATH:
            case NPC_DEATH:
            case BOSS_DEATH:
            case OTHER_DEATH:
                if (data < 0)
                {
                    this.entity = EntityType.fromId(-data.intValue());
                }
                try
                {
                    ObjectNode json = (ObjectNode)this.module.getLogManager().mapper.readTree(additionalData);
                    this.damageCause = DamageCause.valueOf(json.get("DmgC").asText());
                    this.additional = json;
                }
                catch (IOException e)
                {
                    throw new IllegalArgumentException("Error while reading killdata from database",e);
                }
            }
        }
        System.out.print(actionType.name +" "+ timestamp + "C:"+ (hasUser ? this.causer_user.getName() : this.causer_entity)
                        + " " + (hasBlock ? (oldBlock + "->" + this.newBlock + " ") : "") + (item != null ? item : ""));
        // data can be killed ID
    }

    @Override
    public int compareTo(LogEntry o)
    {
        return (int)(this.entryID - o.entryID); // correct order ?
    }

    public ActionType getType()
    {
        return this.actionType;
    }

    public void attach(LogEntry next)
    {
        this.attached.add(next);
    }

    public boolean isSimilar(LogEntry other)
    {
        if (this.actionType != other.actionType)
            return false;
        //TODO
        return true;
    }

    public boolean hasAttached()
    {
        return !this.attached.isEmpty();
    }

    public User getCauserUser()
    {
        return this.causer_user;
    }

    public BlockData getOldBlock()
    {
        return oldBlock;
    }

    public BlockData getNewBlock()
    {
        return newBlock;
    }

    public EntityType getCauserEntity()
    {
        return this.causer_entity;
    }

    public InventoryType getContainerType()
    {
        return this.inventoryType;
    }

    public EntityType getEntity()
    {
        return entity;
    }

    public ItemStack getItem()
    {
        return item;
    }

    public ItemData getItemData()
    {
        return itemData;
    }

    public User getUser()
    {
        return user;
    }

    public DamageCause getDamageCause()
    {
        return damageCause;
    }

    public ObjectNode getAdditional()
    {
        return additional;
    }
}
