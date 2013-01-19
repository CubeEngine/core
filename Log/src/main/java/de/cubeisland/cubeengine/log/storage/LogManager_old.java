package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;

public class LogManager_old extends SingleKeyStorage<Long, LogModel_old>
{
    private static final int REVISION = 1;
    
    private BlockLogManager blockLogManager;

    public LogManager_old(Database database)
    {
        super(database, LogModel_old.class, REVISION);
        this.initialize();
        this.doStoreAsync();
        this.blockLogManager = new BlockLogManager(database, revision);
    }

    public void logKillLog(int killer, Location loc, int killed)
    {
        this.store(new LogModel_old(killer, loc, killed));
    }

    public void logBlockLog(int causeId, BlockState newState, BlockState oldState)
    {
        this.store(new LogModel_old(causeId, newState, oldState));
    }

    public void logChestLog(int userId, Location loc, ItemData itemData, int amount, int containerType)
    {
        this.store(new LogModel_old(userId, loc, itemData, amount, containerType));
    }

    public void logSignLog(int userId, Location loc, String[] oldLines, String[] newLines)
    {
        this.store(new LogModel_old(userId, loc, oldLines, newLines));
    }

    public void logChatLog(int userId, Location loc, String chat, boolean isChat)
    {
        this.store(new LogModel_old(userId, loc, chat, isChat));
    }

    public void logBlockChange(Long key, BlockState state, byte newData)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
