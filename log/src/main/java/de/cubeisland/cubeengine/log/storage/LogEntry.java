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
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.core.util.math.BlockVector3;
import de.cubeisland.cubeengine.log.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import sun.misc.Signal;

public class LogEntry implements Comparable<LogEntry>
{
    private final Log module;

    private final long entryID;
    private final Timestamp timestamp;
    private final World world;
    private final BlockVector3 location;

    private User causer_user;
    private EntityType causer_entity;

    private BlockData oldBlock;
    private BlockData newBlock;

    private ItemData itemData;
    private final ActionType actionType;
    private Set<LogEntry> attached = new HashSet<LogEntry>();
    private EntityType entity;
    private User user;
    private JsonNode additional;

    private final UserManager um;

    public LogEntry(Log module, long entryID, Timestamp timestamp, int action, long worldId, int x, int y, int z,
                    long causer, String block, Long data, String newBlock, Integer newData, String additionalData)
    {
        this.module = module;
        this.um = module.getCore().getUserManager();

        this.entryID = entryID;
        this.timestamp = timestamp;
        this.world = module.getCore().getWorldManager().getWorld(worldId);
        this.location = new BlockVector3(x,y,z);

        this.actionType = ActionType.getById(action);

        if (causer > 0)
        {
            this.causer_user = um.getUser(causer);
        }
        else if (causer < 0)
        {
            this.causer_entity = EntityType.fromId((int)-causer);
        }
        this.additional = this.readJson(additionalData);

        switch (actionType)
        {
        case BLOCK_BREAK: // additional on Sign (sign) / NoteBlock noJson rawNote / JukeBox noJson MaterialName playing/ Attached falls -> has cause (cause) can be sign too (sign)
        case BLOCK_BURN:
        case BLOCK_FADE:
        case LEAF_DECAY:
        case WATER_BREAK:
        case LAVA_BREAK:
        case ENTITY_BREAK:
        case ENDERMAN_PICKUP:
        case BUCKET_FILL:
        case CROP_TRAMPLE:
        case ENTITY_EXPLODE:
        case CREEPER_EXPLODE:
        case TNT_EXPLODE:
        case FIREBALL_EXPLODE:
        case ENDERDRAGON_EXPLODE:
        case WITHER_EXPLODE:
        case TNT_PRIME:
        case BLOCK_PLACE:
        case LAVA_BUCKET:
        case WATER_BUCKET:
        case NATURAL_GROW:
        case PLAYER_GROW:
        case BLOCK_FORM:
        case ENDERMAN_PLACE:
        case ENTITY_FORM:
        case FIRE_SPREAD:
        case FIREBALL_IGNITE:
        case LIGHTER:
        case LAVA_IGNITE:
        case LIGHTNING:
        case BLOCK_SPREAD:
        case WATER_FLOW:
        case LAVA_FLOW:
        case OTHER_IGNITE:
        case BLOCK_SHIFT:
        case BLOCK_FALL: // additional (cause)
        case SIGN_CHANGE: // additional (sign)(oldSign)
        case SHEEP_EAT:
        case BONEMEAL_USE: // additional noJson MaterialName used on
        case LEVER_USE:
        case REPEATER_CHANGE:
        case NOTEBLOCK_CHANGE: // data is not correct used for clicks here
        case DOOR_USE: //currently using additional but actually not needed!
        case CAKE_EAT:
        case COMPARATOR_CHANGE:
        case WORLDEDIT:
        // Hangings
        case HANGING_BREAK: // BlockData of Painting is Art-Id | additional serialized ItemData possible
        case HANGING_PLACE: // BlockData of Painting is Art-Id
            // causer can be +User -Entity 0Other
            this.oldBlock = new BlockData(Material.getMaterial(block),data.byteValue());
            this.newBlock = new BlockData(Material.getMaterial(newBlock),newData.byteValue());
            if (actionType.equals(ActionType.HANGING_BREAK) && this.additional != null)
            {
                this.itemData = ItemData.deserialize(this.additional);
            }
        break;
        case CONTAINER_ACCESS: //OldBlock is ContainerMaterial
        case BUTTON_USE: // OldBlock is ButtonType
        case PLATE_STEP:
            this.oldBlock = new BlockData(Material.getMaterial(block),data.byteValue());
        break;
        case POTION_SPLASH: // additional (effects)(amount)(affected)
        case FIREWORK_USE: // TODO additional itemdata ?
        case MILK_FILL:
        case SOUP_FILL:
            break;
        case VEHICLE_ENTER: // data is negative vehicle EntityTypeID
        case VEHICLE_EXIT:  // data is negative vehicle EntityTypeID
        case VEHICLE_PLACE: // data is negative vehicle EntityTypeID
        case VEHICLE_BREAK: // data is negative vehicle EntityTypeID
            this.entity = EntityType.fromId(-data.intValue());
        break;
        case PLAYER_DEATH: // data is killed
            this.user = this.um.getUser(data);
            break;
        case PET_DEATH: // data is killed
        case MONSTER_DEATH: // data is killed
        case ANIMAL_DEATH: // data is killed
        case BOSS_DEATH: // data is killed
        case NPC_DEATH: // data is killed
        case OTHER_DEATH: // data is killed
            //has Additional: (dmgC)
            //nonPlayer has Additional: (isAdult)(isSit)(color)(prof)(owner)
        case ENTITY_SHEAR: // data is sheared | has Additional (isAdult) sheep:(color)
        case ENTITY_DYE:  // data is dyed | has Additional (isAdult)(color)(nColor) wolf:(isSit)(owner)
            this.entity = EntityType.fromId(-data.intValue());
        break;
        case NATURAL_SPAWN:
        case SPAWNER_SPAWN:
        case OTHER_SPAWN: // sadly player is not given in event
        break;
        case ITEM_INSERT: // hasItemData | newBlock is Container
        case ITEM_REMOVE: // hasItemData | newBlock is Container
            this.newBlock = new BlockData(Material.getMaterial(newBlock));
        case MONSTER_EGG_USE: // hasItemData
        case ITEM_DROP: // hasItemData
        case ITEM_PICKUP: // hasItemData
        case ITEM_TRANSFER: // hasItemData
        case ENCHANT_ITEM: // hasItemData
        case CRAFT_ITEM: // hasItemData
            this.itemData = ItemData.deserialize(this.additional);
        break;
        case XP_PICKUP: // has Additional Xp-Amount
        case PLAYER_COMMAND: // has Additional string
        case PLAYER_CHAT: // has Additional string
        case PLAYER_TELEPORT: // has Additional (dir)form|to (world)id (x)(y)(z)
        case PLAYER_JOIN:
        case PLAYER_QUIT:
            break;
        }
        // data can be killed ID
    }

    private JsonNode readJson(String string)
    {
        if (string == null)
        {
            return null;
        }
        try
        {
            return this.module.getObjectMapper().readTree(string);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Could not read additional data: "+ string, e);
        }
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

    public EntityType getEntity()
    {
        return entity;
    }

    public ItemData getItemData()
    {
        return itemData;
    }

    public User getUser()
    {
        return user;
    }

    public JsonNode getAdditional()
    {
        return additional;
    }

    public Timestamp getTimestamp()
    {
        return this.timestamp;
    }

    public Set<LogEntry> getAttached()
    {
        return attached;
    }

    public boolean hasCauserUser()
    {
        return true; //TODO
    }

    public boolean hasReplacedBlock()
    {
        return true; //TODO if oldBlock is not null and not AIR
    }
}
