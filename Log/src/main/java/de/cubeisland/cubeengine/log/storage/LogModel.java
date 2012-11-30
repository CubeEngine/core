package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.PrimaryKey;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.sql.Timestamp;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;

@Entity(name = "logs")
public class LogModel implements Model<Integer>
{

    @PrimaryKey
    @Attribute(type = AttrType.INT, unsigned = true, ai = true)
    public int key;
    @Attribute(type = AttrType.TINYINT)
    public int type;
    //Types:
    public static final int BLOCKLOG = 1;
    public static final int CHESTLOG = 2;
    public static final int KILLLOG = 3;
    public static final int SIGNCHANGELOG = 4;
    public static final int CHATLOG = 5;
    public static final int COMMANDLOG = 6;
    public static final int INTERACTLOG = 7;
    @Attribute(type = AttrType.DATETIME)
    public Timestamp timestamp;
    @Attribute(type = AttrType.INT)
    public int causeID;
    @Attribute(type = AttrType.VARCHAR, length = 64, notnull = false)
    public String worldName;
    @Attribute(type = AttrType.VARCHAR, length = 64, notnull = false)
    public String worldUUID;
    @Attribute(type = AttrType.INT, notnull = false)
    public int x;
    @Attribute(type = AttrType.INT, notnull = false)
    public int y;
    @Attribute(type = AttrType.INT, notnull = false)
    public int z;
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

    private LogModel(int type, int causeID, Location loc)
    {
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.type = type;
        this.causeID = causeID;
        if (loc != null)
        {
            this.worldName = loc.getWorld().getName();
            this.worldUUID = loc.getWorld().getUID().toString();
            this.x = loc.getBlockX();
            this.y = loc.getBlockY();
            this.z = loc.getBlockZ();
        }
    }

    @DatabaseConstructor
    public LogModel(List<Object> args) throws ConversionException
    {
        this.key = Integer.valueOf(args.get(0).toString());
        this.type = Integer.valueOf(args.get(1).toString());
        this.timestamp = (Timestamp) args.get(2);
        this.causeID = Integer.valueOf(args.get(3).toString());
        this.worldName = args.get(4).toString();
        this.worldUUID = args.get(5).toString();
        this.x = Integer.valueOf(args.get(6).toString());
        this.y = Integer.valueOf(args.get(7).toString());
        this.z = Integer.valueOf(args.get(8).toString());

        this.newBlockOrLines = args.get(9).toString();
        this.oldBlockOrLines = args.get(10).toString();

        this.amountOrInteractData = Integer.valueOf(args.get(11).toString());
        this.containerTypeOrKilledId = Integer.valueOf(args.get(12).toString());
        this.itemNameOrChat = args.get(13).toString();
    }

    /**
     * BlockLog Constructor
     *
     * @param causeID
     * @param newBlock
     * @param oldBlock
     */
    public LogModel(int causeID, BlockState newBlock, BlockState oldBlock)
    {
        this(BLOCKLOG, causeID, newBlock == null ? oldBlock.getLocation() : newBlock.getLocation());
        try
        {
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
    public LogModel(Integer userId, Location loc, ItemData item, int amount, int containerType)
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
    public LogModel(int userID, Location loc, String[] oldLines, String[] newLines)
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
    public LogModel(int killerId, Location loc, int killedId)
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
    public LogModel(int senderId, Location loc, String chat, boolean isChat)
    {
        this(isChat ? CHATLOG : COMMANDLOG, senderId, loc);
        this.itemNameOrChat = chat;
    }

    /**
     * InteractLog-Constructor
     *
     * @param userId
     * @param loc
     * @param mat
     * @param data
     */
    public LogModel(int userId, Location loc, Material mat, Integer data)
    {
        this(INTERACTLOG, userId, loc);
        this.chestItemOrInteractItem = String.valueOf(mat.getId());
        this.amountOrInteractData = data;
    }

    @Override
    public Integer getKey()
    {
        return key;
    }

    @Override
    public void setKey(Integer key)
    {
        this.key = key;
    }
}
