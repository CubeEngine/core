package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;

public class LogManager extends SingleKeyStorage<Long, LogModel>
{
    private static final int REVISION = 1;

    public LogManager(Database database)
    {
        super(database, LogModel.class, REVISION);
        this.initialize();
        this.doStoreAsync();
    }

    public void logKillLog(int killer, Location loc, int killed)
    {
        this.store(new LogModel(killer, loc, killed));
    }

    public void logBlockLog(int causeId, BlockState newState, BlockState oldState)
    {
        this.store(new LogModel(causeId, newState, oldState));
    }

    public void logChestLog(int userId, Location loc, ItemData itemData, int amount, int containerType)
    {
        this.store(new LogModel(userId, loc, itemData, amount, containerType));
    }

    public void logSignLog(int userId, Location loc, String[] oldLines, String[] newLines)
    {
        this.store(new LogModel(userId, loc, oldLines, newLines));
    }

    public void logChatLog(int userId, Location loc, String chat, boolean isChat)
    {
        this.store(new LogModel(userId, loc, chat, isChat));
    }

    public void logInteractLog(int userId, Location loc, Material mat, Integer data)
    {
        this.store(new LogModel(userId, loc, mat, data));
    }
}