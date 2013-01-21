package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.SelectBuilder;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.lookup.BlockLog;
import de.cubeisland.cubeengine.log.lookup.BlockLookup;
import java.sql.ResultSet;
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
            //TODO add index action / causer
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
                    .index("x")
                    .index("y")
                    .index("z")
                    .index("action")
                    .index("causer")
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
                    .field("oldBlock", AttrType.INT, false, false)
                    .field("oldBlockData", AttrType.TINYINT, false, false)
                    .field("newBlock", AttrType.INT, false, false)
                    .field("newBlockData", AttrType.TINYINT, false, false)
                    .foreignKey("key").references("log_logs", "key").onDelete("CASCADE")
                    .primaryKey("key").endFields()
                    .engine("innoDB").defaultcharset("utf8")
                    .end().end();
            this.database.execute(sql);
            sql = builder.insert().into("log_blockLogs")
                    .cols("key", "oldBlock", "oldBlockData", "newBlock", "newBlockData")
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

            /* //INSERTING 160k TESTOBJECTS
             Location locc = new Location(Bukkit.getWorlds().get(0), 1, 1, 1);
             String[] ar1 =
             {
             "alte", "Zeilen", "toll", ":)"
             };
             String[] ar2 =
             {
             "neue", "Zeilen", "besser", ":)"
             };
        
             long a = System.currentTimeMillis();
             database.getConnection().setAutoCommit(false);
             for (int j = 1; j < 50; j++)
             {
             for (int i = 1; i < 2000; i++)
             {
             this.logSignLog(1, locc, ar1, ar2);
             }
             database.getConnection().commit();
             System.out.println(j*2000);
             }
             database.getConnection().commit();
             database.getConnection().setAutoCommit(true);
             System.out.println((System.currentTimeMillis() - a) / 1000 + "sec");
             Bukkit.shutdown();
             //*/
            /*//TEST FOR querybuilding:
             this.getBlockLogs(
             Bukkit.getWorlds().get(0),
             new Location(null, -1000, 0, -1000),
             new Location(null, 1000, 120, 1000),
             new Integer[]
             {
             1, 2
             }, new Long[]
             {
             }, new BlockData[]
             {
             },
             new Timestamp(System.currentTimeMillis() - 50000),
             new Timestamp(System.currentTimeMillis()));
             //*/
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
            this.database.preparedExecute(this.getClass(), "storeBlockLog", logID,
                    oldData == null ? null : oldData.mat.getId(), oldData == null ? null : oldData.data,
                    newData == null ? null : newData.mat.getId(), newData == null ? null : newData.data);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while storing block Log-Entry", ex);
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

    private void buildWorldAndLocation(SelectBuilder builder, World world, Location loc1, Location loc2)
    {
        if (world != null)
        {
            builder.field("world_id").isEqual().value(this.module.getCore().getWorldManager().getWorldId(world));
            if (loc1 != null)
            {
                if (loc2 == null) // single location
                {
                    builder.and().field("x").isEqual().value(loc1.getBlockX())
                            .and().field("y").isEqual().value(loc1.getBlockY())
                            .and().field("z").isEqual().value(loc1.getBlockZ());
                }
                else // range of locations
                {
                    builder.and().field("x").between(loc1.getBlockX(), loc2.getBlockX())
                            .and().field("y").between(loc1.getBlockY(), loc2.getBlockY())
                            .and().field("z").between(loc1.getBlockZ(), loc2.getBlockZ());
                }
            }
            builder.and();
        }
    }

    private void buildDates(SelectBuilder builder, Timestamp fromDate, Timestamp toDate)
    {
        builder.beginSub().field("date").between(fromDate, toDate).endSub();
    }

    public BlockLookup getBlockLogs(World world, Location loc1, Location loc2,
            Integer[] actions,//blocks only
            Long[] causers, boolean exludeCausers,
            BlockData[] blockdatas, boolean exludeDatas,
            String text, boolean excludeText, //signs only            
            Timestamp fromDate, Timestamp toDate)
    {
        for (int action : actions) // Check actions
        {
            switch (action)
            {
                case BLOCK_PLACE:
                case BLOCK_BREAK:
                case BLOCK_CHANGE:
                    break;
                default:
                    throw new IllegalStateException("Not a Block-Log!");
            }
        }
        QueryBuilder builder = this.database.getQueryBuilder();
        SelectBuilder sbuilder = builder.select().wildcard()
                .from("log_logs")
                .joinOnEqual("log_blocklogs", "key", "log_logs", "key").where();
        this.buildWorldAndLocation(sbuilder, world, loc1, loc2);
        if (actions.length > 0)
        {
            sbuilder.beginSub().field("action").in().valuesInBrackets(actions);
            sbuilder.endSub().and();
        }
        if (causers.length > 0)
        {
            sbuilder.beginSub();
            if (exludeCausers)
            {
                sbuilder.not();
            }
            sbuilder.field("causer").in().valuesInBrackets(causers);
            sbuilder.endSub().and();
        }
        if (blockdatas.length > 0)
        {
            if (exludeDatas)
            {
                sbuilder.not();
            }
            sbuilder.beginSub();
            if (blockdatas[0].data == null)
            {
                sbuilder.beginSub()
                        .field("oldBlock").isEqual().value(blockdatas[0].mat.getId()).or()
                        .field("newBlock").isEqual().value(blockdatas[0].mat.getId())
                        .endSub();
            }
            else
            {
                sbuilder.beginSub().beginSub()
                        .field("oldBlock").isEqual().value(blockdatas[0].mat.getId()).and()
                        .field("oldBlockData").isEqual().value(blockdatas[0].data)
                        .endSub().or()
                        .beginSub()
                        .field("newBlock").isEqual().value(blockdatas[0].mat.getId()).and()
                        .field("newBlockData").isEqual().value(blockdatas[0].data)
                        .endSub().endSub();
            }
            for (int i = 1; i < blockdatas.length; ++i)
            {
                sbuilder.or().beginSub();
                if (blockdatas[i].data == null)
                {
                    sbuilder.field("oldBlock").isEqual().value().or()
                            .field("newBlock").isEqual().value();

                }
                else
                {
                    sbuilder.beginSub()
                            .field("oldBlock").isEqual().value().and()
                            .field("oldBlockData").isEqual().value()
                            .endSub().or()
                            .beginSub()
                            .field("newBlock").isEqual().value().and()
                            .field("newBlockData").isEqual().value()
                            .endSub();
                }
                sbuilder.endSub();
            }
            sbuilder.endSub().and();
        }
        this.buildDates(sbuilder, fromDate, toDate);

        String sql = sbuilder.end().end();

        System.out.println("\n\n" + sql); //<---TODO remove this
        BlockLookup lookup = new BlockLookup();
        try
        {
            ResultSet result = this.database.query(sql);

            while (result.next())
            {
                Long key = result.getLong("key");
                Timestamp date = result.getTimestamp("date");
                //Long world_id = result.getLong("world_id");
                Integer action = result.getInt("action");
                Integer x = result.getInt("x");
                Integer y = result.getInt("y");
                Integer z = result.getInt("z");
                Long causer = result.getLong("causer");
                Integer oldBlock = result.getInt("oldBlock");
                Byte oldBlockData = result.getByte("oldBlockData");
                Integer newBlock = result.getInt("newBlock");
                Byte newBlockData = result.getByte("newBlockData");

                Location loc = new Location(world, x, y, z); // world is already known in params
                //TODO if world == null or xyz null no location

                lookup.addEntry(new BlockLog(key, action, date, loc, causer,
                        BlockData.get(oldBlock, oldBlockData),
                        BlockData.get(newBlock, newBlockData)));
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("Could not execute query for block-logs!", ex);
        }
        // SIGN QUERY:
        sbuilder = builder.select().wildcard()
                .from("log_logs")
                .joinOnEqual("log_signlogs", "key", "log_logs", "key").where();
        this.buildWorldAndLocation(sbuilder, world, loc1, loc2);
        sbuilder.field("action").isEqual().value(BLOCK_SIGN).and();
        if (causers.length > 0)
        {
            sbuilder.beginSub();
            if (exludeCausers)
            {
                sbuilder.not();
            }
            sbuilder.field("causer").in().valuesInBrackets(causers);
            sbuilder.endSub().and();
        }
        if (text != null)
        {
            if (excludeText)
            {
                sbuilder.not();
            }
            sbuilder.beginSub()
                    .field("oldLine1").like().value("%" + text + "%").or()
                    .field("oldLine2").like().value("%" + text + "%").or()
                    .field("oldLine3").like().value("%" + text + "%").or()
                    .field("oldLine4").like().value("%" + text + "%").or()
                    .field("newLine1").like().value("%" + text + "%").or()
                    .field("newLine2").like().value("%" + text + "%").or()
                    .field("newLine3").like().value("%" + text + "%").or()
                    .field("newLine4").like().value("%" + text + "%").endSub();
        }
        this.buildDates(sbuilder, fromDate, toDate);
        sql = sbuilder.end().end();
        System.out.println("\n\n" + sql); //<---TODO remove this
        try
        {
            ResultSet result = this.database.query(sql);
            while (result.next())
            {
                Long key = result.getLong("key");
                Timestamp date = result.getTimestamp("date");
                //Long world_id = result.getLong("world_id");
                Integer action = result.getInt("action");
                Integer x = result.getInt("x");
                Integer y = result.getInt("y");
                Integer z = result.getInt("z");
                Long causer = result.getLong("causer");
                String[] oldLines = new String[4];
                String[] newLines = new String[4];
                oldLines[0] = result.getString("oldLine1");
                oldLines[1] = result.getString("oldLine2");
                oldLines[2] = result.getString("oldLine3");
                oldLines[3] = result.getString("oldLine4");

                newLines[0] = result.getString("newLine1");
                newLines[1] = result.getString("newLine2");
                newLines[2] = result.getString("newLine3");
                newLines[3] = result.getString("newLine4");

                Location loc = new Location(world, x, y, z); // world is already known in params
                //TODO if world == null or xyz null no location

                lookup.addEntry(new BlockLog(key, action, date, loc, causer,
                        oldLines, newLines));
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("Could not execute query for sign-logs!", ex);
        }
        return lookup;

    }
}
