package de.cubeisland.cubeengine.core.abstraction;

import java.io.File;
import java.util.Set;

/**
 *
 * @author CodeInfection
 */
public interface Server
{
    public void registerCommand(Plugin plugin, String name, CommandExecutor command);

    public String getVersion();

    public PluginManager getPluginManager();

    public Scheduler getScheduler();

    public String getName();

    public String getIp();

    public int getPort();

    public Player[] getOnlinePlayers();

    public int getMaxPlayers();

    public World[] getWorlds();

    public boolean getOnlineMode();

    public boolean isWhitelisted();

    public int getSpawnRadius();

    public int getViewDistance();

    public GameMode getDefaultGameMode();

    public boolean isEndAllwed();

    public boolean isNetherAllowed();

    public boolean isFlyingAllowed();

    public File getWorldContainer();

    public File getUpdateFolder();

    public void shutdown();

    public void broadcast(String message);
}
