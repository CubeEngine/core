package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.sql.Timestamp;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;

@SingleKeyEntity(tableName = "logs", primaryKey = "key", autoIncrement = true)
public class LogModel_old implements Model<Long>
{
    //TODO trim chatlog and dont log if empty then
    @Attribute(type = AttrType.INT, unsigned = true)
    public Long key;
    @Attribute(type = AttrType.DATETIME)
    public Timestamp timestamp;
    @Attribute(type = AttrType.INT, notnull = false, unsigned = true)
    public Long worldID;
    @Attribute(type = AttrType.INT, notnull = false)
    public Integer x;
    @Attribute(type = AttrType.INT, notnull = false)
    public Integer y;
    @Attribute(type = AttrType.INT, notnull = false)
    public Integer z;
    @Attribute(type = AttrType.TINYINT)
    public int action;
    public static final int BLOCK_PLACE = 0x00;
    public static final int BLOCK_BREAK = 0x01;
    public static final int BLOCK_CHANGE = 0x02; //changing blockdata / interactlog
    public static final int BLOCK_SIGN = 0x03; //sign change
    public static final int KILL_PVP = 0x10; //player killed by player
    public static final int KILL_PVE = 0x11; //player killed by environement
    public static final int KILL_EVE = 0x12; //mob killed by environement
    public static final int KILL_EVP = 0x13; //mob killed by player
    public static final int CHEST_PUT = 0x20;
    public static final int CHEST_TAKE = 0x21;
    public static final int CHAT = 0x30;
    public static final int COMMAND = 0x31;
    @Attribute(type = AttrType.BIGINT)
    public int causerID;

    ////////////////////////////////////////////////////////////
    //actionTypes:
    public static final int BLOCKLOG = 1;
    public static final int CHESTLOG = 2;
    public static final int KILLLOG = 3;
    public static final int SIGNCHANGELOG = 4;
    public static final int CHATLOG = 5;
    public static final int COMMANDLOG = 6;
    public static final int INTERACTLOG = 7;
    // BlockLog OR SignChangeLog:
    @Attribute(type = AttrType.VARCHAR, length = 67, notnull = false)
    public String newBlockOrLines = null;
    @Attribute(type = AttrType.VARCHAR, length = 67, notnull = false)
    public String oldBlockOrLines = null;
    // ChestLog OR KillLog OR Chat OR Interact:
    @Attribute(type = AttrType.VARCHAR, length = 10, notnull = false)
    public String chestItemOrInteractItem = null; //ChestLog ID:DATA  |  Interact MaterialID
    @Attribute(type = AttrType.INT, notnull = false)
    public Integer amountOrInteractData = null; // ChestLog +-  |  Interact: Additional Data
    @Attribute(type = AttrType.INT, notnull = false)
    public Integer containerTypeOrKilledId = null; // ChestLog ContainerType | KillLog killed
    @Attribute(type = AttrType.VARCHAR, length = 100, notnull = false)
    public String itemNameOrChat = null;
    // BlockLog Methods & Fields:
    private BlockData newBlockData = null;
    private BlockData oldBlockData = null;

    private void initBlockData()
    {
        try
        {
            if (oldBlockData == null)
            {
                this.oldBlockData = Convert.fromObject(BlockData.class, this.oldBlockOrLines);
            }
            if (newBlockData == null)
            {
                this.newBlockData = Convert.fromObject(BlockData.class, this.newBlockOrLines);
            }
        }
        catch (ConversionException ignored)
        {
        }
    }

    public BlockData getNewBlockData()
    {
        this.initBlockData();
        return this.newBlockData;
    }

    public BlockData getOldBlockData()
    {
        this.initBlockData();
        return this.oldBlockData;
    }

    public boolean isBlockBreak()
    {
        this.initBlockData();
        return this.newBlockData.mat == Material.AIR;
    }

    public boolean isBlockPlace()
    {
        this.initBlockData();
        return (oldBlockData.mat == Material.AIR);
    }

    public boolean isBlockRePlace()
    {
        this.initBlockData();
        return ((oldBlockData.mat != Material.AIR) && (newBlockData.mat != Material.AIR));
    }
    //-end of BlockLog Methods & Fields
    // ChestLog Methods & Fields:
    private ItemData itemData = null;

    public ItemData getItemData()
    {
        this.initItemData();
        return this.itemData;
    }

    private void initItemData()
    {
        if (this.itemData == null)
        {
            try
            {
                this.itemData = Convert.fromObject(ItemData.class, chestItemOrInteractItem);
                this.itemData.name = this.itemNameOrChat;
            }
            catch (ConversionException ingored)
            {
            }
        }
    }

    //-end of ChestLog Methods & Fields
    private LogModel_old(int type, int causeID, Location loc)
    {
        this.timestamp = new Timestamp(System.currentTimeMillis());
//        this.type = type;
//        this.causeID = causeID;
        if (loc != null)
        {
            this.worldID = CubeEngine.getCore().getWorldManager().getWorldId(loc.getWorld());
            this.x = loc.getBlockX();
            this.y = loc.getBlockY();
            this.z = loc.getBlockZ();
        }
    }

    /**
     * BlockLog Constructor
     *
     * @param causeID
     * @param newBlock
     * @param oldBlock
     */
    public LogModel_old(int causeID, BlockState newBlock, BlockState oldBlock)
    {
        this(BLOCKLOG, causeID, newBlock == null ? oldBlock.getLocation() : newBlock.getLocation());
        try
        {oldBlock.getRawData();
            this.newBlockOrLines = newBlock == null
                    ? (String) Convert.toObject(new BlockData(Material.AIR, (byte) 0))
                    : (String) Convert.toObject(new BlockData(newBlock.getType(), newBlock.getRawData()));
            this.oldBlockOrLines = oldBlock == null
                    ? (String) Convert.toObject(new BlockData(Material.AIR, (byte) 0))
                    : (String) Convert.toObject(new BlockData(oldBlock.getType(), oldBlock.getRawData()));
        }
        catch (ConversionException ignored)
        {
            //TODO handle ?
        }
    }

    /**
     * ChestLog Constructor
     *
     * @param userId
     * @param loc
     * @param item
     * @param amount
     * @param containerType
     */
    public LogModel_old(Integer userId, Location loc, ItemData item, int amount, int containerType)
    {
        this(CHESTLOG, userId, loc);
        try
        {
            this.chestItemOrInteractItem = (String) Convert.toObject(item);
            this.amountOrInteractData = amount;
            this.itemNameOrChat = item.name;
            this.containerTypeOrKilledId = containerType;
        }
        catch (ConversionException ingored)
        {
        }
    }

    /**
     * SignChangeLog-Constructor
     *
     * @param userID
     * @param state
     * @param oldLines
     * @param newLines
     */
    public LogModel_old(int userID, Location loc, String[] oldLines, String[] newLines)
    {
        this(SIGNCHANGELOG, userID, loc);
        this.newBlockOrLines = "";//TODO separator for lines
        this.oldBlockOrLines = "";
    }

    /**
     * KillLog-Constructor
     *
     * @param killerId
     * @param loc
     * @param killedId
     */
    public LogModel_old(int killerId, Location loc, int killedId)
    {
        this(KILLLOG, killerId, loc);
        this.containerTypeOrKilledId = killedId;
    }

    /**
     * ChatLog-Constructor
     *
     * @param senderId
     * @param loc
     * @param chat
     * @param isChat
     */
    public LogModel_old(int senderId, Location loc, String chat, boolean isChat)
    {
        this(isChat ? CHATLOG : COMMANDLOG, senderId, loc);
        this.itemNameOrChat = chat;
    }

    @Override
    public Long getKey()
    {
        return key;
    }

    @Override
    public void setKey(Long key)
    {
        this.key = key;
    }
}
