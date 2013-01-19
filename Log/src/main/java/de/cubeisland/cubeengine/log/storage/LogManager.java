package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.log.Log;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;

public class LogManager
{
    public static final int BLOCK_PLACE = 0x00;
    public static final int BLOCK_BREAK = 0x01;
    public static final int BLOCK_CHANGE = 0x02; //changing blockdata / interactlog
    public static final int BLOCK_SIGN = 0x03; //sign change
    public static final int KILL_PVP = 0x10; //player killed by player
    public static final int KILL_PVE = 0x11; //player killed by environement
    public static final int KILL_EVE = 0x12; //mob killed by environement
    public static final int KILL_EVP = 0x13; //mob killed by player
    public static final int CHAT = 0x20;
    public static final int COMMAND = 0x21;
    public static final int CHEST_PUT = 0x30;
    public static final int CHEST_TAKE = 0x31;
    private final Database database;
    private final Log module;

    public LogManager(Log module)
    {
        this.database = module.getDatabase();
        this.module = module;
        try
        {
            // Main table:
            QueryBuilder builder = database.getQueryBuilder();
            String sql = builder.createTable("log_logs", true).beginFields()
                    .field("key", AttrType.INT, true).autoIncrement()
                    .field("date", AttrType.TIMESTAMP)
                    .field("world_id", AttrType.INT, true, false)
                    .field("x", AttrType.INT, false, false)
                    .field("y", AttrType.INT, false, false)
                    .field("z", AttrType.INT, false, false)
                    .field("action", AttrType.TINYINT)
                    .field("causer", AttrType.BIGINT)
                    .foreignKey("world_id").references("worlds", "key")
                    .primaryKey("key").endFields()
                    .engine("innoDB").defaultcharset("utf8")
                    .end().end();
            this.database.execute(sql);
            sql = builder.insert().into("log_logs")
                    .cols("date", "world_id", "x", "y", "z", "action", "causer")
                    .end().end();
            this.database.storeStatement(this.getClass(), "storeLog", sql);
            //Block logging:
            sql = builder.createTable("log_blockLogs", true).beginFields()
                    .field("key", AttrType.INT, true)
                    .field("oldBlock", AttrType.VARCHAR, 32, false)
                    .field("newBlock", AttrType.VARCHAR, 32, false)
                    .foreignKey("key").references("log_logs", "key").onDelete("CASCADE")
                    .primaryKey("key").endFields()
                    .engine("innoDB").defaultcharset("utf8")
                    .end().end();
            this.database.execute(sql);
            sql = builder.insert().into("log_blockLogs")
                    .cols("key", "oldBlock", "newBlock")
                    .end().end();
            this.database.storeStatement(this.getClass(), "storeBlockLog", sql);
            //Sign logging:
            sql = builder.createTable("log_signLogs", true).beginFields()
                    .field("key", AttrType.INT, true)
                    .field("oldLine1", AttrType.VARCHAR, 16)
                    .field("oldLine2", AttrType.VARCHAR, 16)
                    .field("oldLine3", AttrType.VARCHAR, 16)
                    .field("oldLine4", AttrType.VARCHAR, 16)
                    .field("newLine1", AttrType.VARCHAR, 16)
                    .field("newLine2", AttrType.VARCHAR, 16)
                    .field("newLine3", AttrType.VARCHAR, 16)
                    .field("newLine4", AttrType.VARCHAR, 16)
                    .foreignKey("key").references("log_logs", "key").onDelete("CASCADE")
                    .primaryKey("key").endFields()
                    .engine("innoDB").defaultcharset("utf8")
                    .end().end();
            this.database.execute(sql);
            sql = builder.insert().into("log_signLogs")
                    .cols("key", "oldLine1", "oldLine2", "oldLine3",
                    "oldLine4", "newLine1", "newLine2", "newLine3", "newLine4")
                    .end().end();
            this.database.storeStatement(this.getClass(), "storeSignLog", sql);
            //Kill logging:
            sql = builder.createTable("log_killLogs", true).beginFields()
                    .field("key", AttrType.INT, true)
                    .field("killed", AttrType.BIGINT)
                    .foreignKey("key").references("log_logs", "key").onDelete("CASCADE")
                    .primaryKey("key").endFields()
                    .engine("innoDB").defaultcharset("utf8")
                    .end().end();
            this.database.execute(sql);
            sql = builder.insert().into("log_killLogs")
                    .cols("key", "killed")
                    .end().end();
            this.database.storeStatement(this.getClass(), "storeKillLog", sql);
            //Message (Chat/Command) logging:
            sql = builder.createTable("log_messageLogs", true).beginFields()
                    .field("key", AttrType.INT, true)
                    .field("message", AttrType.VARCHAR, 100)
                    .foreignKey("key").references("log_logs", "key").onDelete("CASCADE")
                    .primaryKey("key").endFields()
                    .engine("innoDB").defaultcharset("utf8")
                    .end().end();
            this.database.execute(sql);
            sql = builder.insert().into("log_messageLogs")
                    .cols("key", "message")
                    .end().end();
            this.database.storeStatement(this.getClass(), "storeMessageLogs", sql);
            //Chest logging:
            sql = builder.createTable("log_chestLogs", true).beginFields()
                    .field("key", AttrType.INT, true)
                    .field("item", AttrType.VARCHAR, 16)
                    .field("name", AttrType.VARCHAR, 50)
                    .field("amount", AttrType.INT)
                    .field("containerType", AttrType.INT)
                    .foreignKey("key").references("log_logs", "key").onDelete("CASCADE")
                    .primaryKey("key").endFields()
                    .engine("innoDB").defaultcharset("utf8")
                    .end().end();
            this.database.execute(sql);
            sql = builder.insert().into("log_chestLogs")
                    .cols("key", "item", "name", "amount", "containerType")
                    .end().end();
            this.database.storeStatement(this.getClass(), "storeChestLogs", sql);

        }
        catch (SQLException ex)
        {
            throw new StorageException("Error during initialization of log-tables", ex);
        }
    }

    /**
     * Main log entry
     *
     * @param world
     * @param location
     * @param action
     * @param causer
     * @return
     */
    private Long storeLog(World world, Location location, int action, long causer)
    {
        try
        {
            Long world_id = world == null ? null : this.module.getCore().getWorldManager().getWorldId(world);
            Integer x = location == null ? null : location.getBlockX();
            Integer y = location == null ? null : location.getBlockY();
            Integer z = location == null ? null : location.getBlockZ();
            return (Long) this.database.getLastInsertedId(this.getClass(), "storeLog",
                    new Timestamp(System.currentTimeMillis()), world_id, x, y, z, action, causer);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while storing main Log-Entry", ex);
        }
    }

    /**
     * Block log entry
     *
     * @param logID
     * @param oldData
     * @param newData
     */
    private void storeBlockLog(long logID, BlockData oldData, BlockData newData)
    {
        try
        {
            this.database.preparedExecute(this.getClass(), "storeBlockLog", logID, Convert.toObject(oldData), Convert.toObject(newData));
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while storing block Log-Entry", ex);
        }
        catch (ConversionException ignored) // cannot happen
        {
        }
    }

    /**
     * Sign log entry
     *
     * @param logID
     * @param oldLines
     * @param newLines
     */
    private void storeSignLog(long logID, String[] oldLines, String[] newLines)
    {
        try
        {
            this.database.preparedExecute(this.getClass(), "storeSignLog", logID,
                    oldLines[0], oldLines[1], oldLines[2], oldLines[3],
                    newLines[0], newLines[1], newLines[2], newLines[3]);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while storing sign Log-Entry", ex);
        }
    }

    /**
     * Killed log entry
     *
     * @param logID
     * @param killed
     */
    private void storeKillLog(long logID, long killed)
    {
        try
        {
            this.database.preparedExecute(this.getClass(), "storeKillLog", logID, killed);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while storing kill Log-Entry", ex);
        }
    }

    /**
     * Chat or command entry
     *
     * @param logID
     * @param message
     */
    private void storeMessageLog(long logID, String message)
    {
        try
        {
            this.database.preparedExecute(this.getClass(), "storeMessageLogs", logID, message);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while storing message Log-Entry", ex);
        }
    }

    /**
     * Chest entry
     *
     * @param logID
     * @param itemData
     * @param amount
     * @param containerType
     */
    private void storeChestLog(long logID, ItemData itemData, int amount, int containerType)
    {
        try
        {

            this.database.preparedExecute(this.getClass(), "storeChestLogs",
                    logID, Convert.toObject(itemData), (itemData.name == null ? null : itemData.name),
                    amount, containerType);


        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while storing message Log-Entry", ex);
        }
        catch (ConversionException ignored)
        {
        }

    }

    /**
     * Blocks placed or destroyed
     *
     * @param causeId
     * @param newState
     * @param oldState
     */
    public void logBlockLog(long causerId, BlockState newState, BlockState oldState)
    {
        Location location;
        int type;
        if (newState != null)
        {
            type = BLOCK_PLACE;
            location = newState.getLocation();
        }
        else if (oldState != null)
        {
            type = BLOCK_BREAK;
            location = oldState.getLocation();
        }
        else
        {
            throw new IllegalArgumentException("Both states cannot be null!");
        }
        long logID = this.storeLog(location.getWorld(), location, type, causerId);
        this.storeBlockLog(logID, BlockData.get(oldState), BlockData.get(newState));
    }

    /**
     * Blocks changed (interaction)
     *
     * @param key
     * @param state
     * @param newData
     */
    public void logBlockChange(long causerId, BlockState state, byte newData)
    {
        Location location = state.getLocation();
        long logID = this.storeLog(location.getWorld(), location, BLOCK_CHANGE, causerId);
        this.storeBlockLog(logID, BlockData.get(state), BlockData.get(state, newData));
    }

    /**
     * Sign changed
     *
     * @param causerId
     * @param location
     * @param oldlines
     * @param newlines
     */
    public void logSignLog(long causerId, Location location, String[] oldlines, String[] newlines)
    {
        long logID = this.storeLog(location.getWorld(), location, BLOCK_SIGN, causerId);
        this.storeSignLog(logID, oldlines, newlines);
    }

    /**
     * Entity killed
     *
     * @param killer
     * @param location
     * @param killed
     */
    public void logKillLog(long killer, Location location, long killed)
    {
        int type;
        if (killer > 0)
        {
            if (killed > 0)
            {
                type = KILL_PVP;
            }
            else
            {
                type = KILL_PVE;
            }
        }
        else
        {
            if (killed > 0)
            {
                type = KILL_EVP;
            }
            else
            {
                type = KILL_EVE;
            }
        }
        long logID = this.storeLog(location.getWorld(), location, type, killer);
        this.storeKillLog(logID, killed);
    }

    /**
     * Message typed
     *
     * @param userId
     * @param loc
     * @param chat
     * @param isChat
     */
    public void logChatLog(long causerId, Location location, String message, boolean isChat)
    {
        long logID = this.storeLog(location == null ? null : location.getWorld(), location, isChat ? CHAT : COMMAND, causerId);
        this.storeMessageLog(logID, message);
    }

    /**
     * Chest transactions
     *
     * @param userId
     * @param location
     * @param itemData
     * @param amount
     * @param containerType
     */
    public void logChestLog(long userId, Location location, ItemData itemData, int amount, int containerType)
    {
        long logID = this.storeLog(location.getWorld(), location, (amount > 0 ? CHEST_PUT : CHEST_TAKE), userId);
        this.storeChestLog(logID, itemData, amount, containerType);
    }
}
