package de.cubeisland.cubeengine.log.storage;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.math.BlockVector3;
import de.cubeisland.cubeengine.log.Log;

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
            if (isKill)
            {
                //TODO data can be killed id / additional has info about entity
            }
            else if (!(actionType.equals(ActionType.HANGING_BREAK) && additionalData == null))
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
                    this.itemData = ItemData.deserialize(additionalData, this.module.getLogManager().mapper);
                    this.item = itemData.toItemStack();

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
}
