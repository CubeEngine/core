package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
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
    public static final int CHEST_PUT = 0x20;
    public static final int CHEST_TAKE = 0x21;
    public static final int CHAT = 0x30;
    public static final int COMMAND = 0x31;
    private final Database database;
    private final Log module;

    public LogManager(Log module, Database database)
    {
        this.database = database;
        this.module = module;
        try
        {

            // Main table:
            QueryBuilder builder = database.getQueryBuilder();
            String sql = builder.createTable("logs", true).beginFields()
                    .field("key", AttrType.INT, true)
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
            //Block logging:
            sql = builder.createTable("blockLogs", true).beginFields()
                    .field("key", AttrType.INT, true)
                    .field("oldBlock", AttrType.VARCHAR, 32)
                    .field("newBlock", AttrType.VARCHAR, 32)
                    .foreignKey("key").references("logs", "key")
                    .primaryKey("key").endFields()
                    .engine("innoDB").defaultcharset("utf8")
                    .end().end();
            this.database.execute(sql);
            //Sign logging:
            sql = builder.createTable("signLogs", true).beginFields()
                    .field("key", AttrType.INT, true)
                    .field("oldLine1", AttrType.VARCHAR, 16)
                    .field("oldLine2", AttrType.VARCHAR, 16)
                    .field("oldLine3", AttrType.VARCHAR, 16)
                    .field("oldLine4", AttrType.VARCHAR, 16)
                    .field("newLine1", AttrType.VARCHAR, 16)
                    .field("newLine2", AttrType.VARCHAR, 16)
                    .field("newLine3", AttrType.VARCHAR, 16)
                    .field("newLine4", AttrType.VARCHAR, 16)
                    .foreignKey("key").references("logs", "key")
                    .primaryKey("key").endFields()
                    .engine("innoDB").defaultcharset("utf8")
                    .end().end();
            this.database.execute(sql);

            sql = builder.insert().into("logs")
                    .cols("date", "world_id", "x", "y", "z", "action", "causer")
                    .end().end();
            this.database.storeStatement(this.getClass(), "storeLog", sql);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error during initialization of log-table");
        }
    }

    public Long storeLog(World world, Location location, int action, long causer)
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
            throw new StorageException("Error while storing main Log-Entry");
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
            location = newState.getLocation();
        }
        else
        {
            throw new IllegalArgumentException("Both states cannot be null!");
        }
        long logID = this.storeLog(location.getWorld(), location, type, causerId);
        //TODO further logging
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
        //TODO further logging
    }

    /**
     * Sign changed
     *
     * @param causerId
     * @param location
     * @param oldlines
     * @param newlines
     */
    public void logBlockChange(long causerId, Location location, String[] oldlines, String[] newlines)
    {
        long logID = this.storeLog(location.getWorld(), location, BLOCK_SIGN, causerId);
        //TODO further logging
    }
}
