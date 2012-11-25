package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import org.bukkit.Location;
import org.bukkit.block.BlockState;

public class LogManager extends BasicStorage<LogModel>
{
    private static LogManager instance;
    private static final int REVISION = 1;

    private LogManager(Database database)
    {
        super(database, LogModel.class, REVISION);
        this.initialize();
        this.notAssignKey();
    }

    public static void init(Database database)
    {
        instance = new LogManager(database);
    }

    public static void logKillLog(int killer, Location loc, int killed)
    {
        instance.store(new LogModel(killer, loc, killed));
    }

    public static void logBlockLog(int causeId, BlockState newState, BlockState oldState)
    {
        instance.store(new LogModel(causeId, newState, oldState));
    }

    public static void logChestLog(int userId, Location loc, ItemData itemData, int amount, int containerType)
    {
        instance.store(new LogModel(userId, loc, itemData, amount, containerType));
    }

    public static void logSignLog(int userId, Location loc, String[] oldLines, String[] newLines)
    {
        instance.store(new LogModel(userId, loc, oldLines, newLines));
    }

    public static void logChatLog(int userId, String chat, boolean isChat)
    {
        //TODO instance.store(new LogModel());
    }

    public static void logInteractLog()
    {
        //TODO instance.store(new LogModel());
    }
}