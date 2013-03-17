package de.cubeisland.cubeengine.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.cubeengine.core.bukkit.EventManager;
import de.cubeisland.cubeengine.core.bukkit.TaskManager;
import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.filesystem.TestFileManager;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import de.cubeisland.cubeengine.core.module.TestModuleManager;
import de.cubeisland.cubeengine.core.permission.PermissionManager;
import de.cubeisland.cubeengine.core.storage.TableManager;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.world.WorldManager;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.InventoryGuardFactory;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.core.webapi.ApiServer;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author Phillip Schichtel
 */
public class TestCore implements Core
{
    private final Logger logger = Logger.getAnonymousLogger();
    private ObjectMapper jsonObjectMapper = null;
    private CoreConfiguration config = null;
    private FileManager fileManager = null;
    private ModuleManager moduleManager = null;

    {
        CubeEngine.initialize(this);
    }

    @Override
    public ApiServer getApiServer()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CommandManager getCommandManager()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CoreConfiguration getConfiguration()
    {
        if (this.config == null)
        {
            this.config = Configuration.load(CoreConfiguration.class, new File(this.getFileManager().getDataFolder(), "core.yml"));
        }
        return this.config;
    }

    @Override
    public Logger getCoreLogger()
    {
        return this.logger;
    }

    @Override
    public Database getDB()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public I18n getI18n()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public EventManager getEventManager()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileManager getFileManager()
    {
        if (this.fileManager == null)
        {
            try
            {
                this.fileManager = new TestFileManager(this);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        return this.fileManager;
    }

    @Override
    public ModuleManager getModuleManager()
    {
        if (this.moduleManager == null)
        {
            this.moduleManager = new TestModuleManager(this);
        }
        return this.moduleManager;
    }

    @Override
    public TableManager getTableManger()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PermissionManager getPermissionManager()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TaskManager getTaskManager()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UserManager getUserManager()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDebug()
    {
        return false;
    }

    @Override
    public WorldManager getWorldManager()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Match getMatcherManager()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InventoryGuardFactory getInventoryGuard()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
