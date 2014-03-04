/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.log.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.storage.QueryManager.QueryAction;

public class LogManager
{
    public final ObjectMapper mapper;
    private final Log module;

    private final LoggingConfiguration globalConfig;
    private final Path worldsFolder;
    private final Map<World, LoggingConfiguration> worldConfigs = new HashMap<>();

    private final QueryManager queryManager;

    public LogManager(Log module)
    {
        this.module = module;
        this.mapper = new ObjectMapper();
        this.worldsFolder = module.getFolder().resolve("worlds");
        try
        {
            Files.createDirectories(this.worldsFolder);
        }
        catch (IOException e)
        {
            throw new FolderNotFoundException("Couldn't create the worlds folder: " + this.worldsFolder.toAbsolutePath(), e);
        }
        this.globalConfig = this.module.getCore().getConfigFactory().
            load(LoggingConfiguration.class, module.getFolder().resolve("globalconfig.yml").toFile());
        for (World world : ((BukkitCore)module.getCore()).getServer().getWorlds())
        {
            this.initWorldConfig(world);
        }
        this.queryManager = new QueryManager(module);
    }

    private LoggingConfiguration initWorldConfig(World world)
    {
        Path worldFolder = this.worldsFolder.resolve(world.getName());
        try
        {
            Files.createDirectories(worldFolder);
        }
        catch (IOException e)
        {
            throw new FolderNotFoundException("Failed to create the world folder for " + world.getName(), e);
        }
        LoggingConfiguration config = this.globalConfig.loadChild(worldFolder.resolve("config.yml").toFile());
        this.worldConfigs.put(world, config);
        return config;
    }

    public void disable()
    {
        this.queryManager.disable();
    }

    public int getQueueSize()
    {
        return this.queryManager.queuedLogs.size();
    }

    public void fillLookupAndShow(Lookup lookup, User user)
    {
        this.queryManager.prepareLookupQuery(lookup.clone(), user, QueryAction.SHOW);
    }

    public void fillLookupAndRollback(Lookup lookup, User user)
    {
        this.queryManager.prepareLookupQuery(lookup.clone(), user, QueryAction.ROLLBACK);
    }

    public void fillLookupAndPreviewRollback(Lookup lookup, User user)
    {
        this.queryManager.prepareLookupQuery(lookup.clone(), user, QueryAction.ROLLBACK_PREVIEW);
    }

    public void fillLookupAndPreviewRedo(Lookup lookup, User user)
    {
        this.queryManager.prepareLookupQuery(lookup.clone(), user, QueryAction.REDO_PREVIEW);
    }

    public void fillLookupAndRedo(Lookup lookup, User user)
    {
        this.queryManager.prepareLookupQuery(lookup.clone(), user, QueryAction.REDO);
    }

    public LoggingConfiguration getConfig(World world)
    {
        if (world == null)
        {
            return this.globalConfig;
        }
        LoggingConfiguration config = this.worldConfigs.get(world);
        if (config == null)
        {
            config = this.initWorldConfig(world);
        }
        return config;
    }

    public void queueLog(QueuedLog log)
    {
        this.queryManager.queueLog(log);
    }

    public QueryManager getQueryManager()
    {
        return this.queryManager;
    }
}
