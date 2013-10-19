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
package de.cubeisland.engine.core;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.engine.configuration.Configuration;
import de.cubeisland.engine.core.ban.BanManager;
import de.cubeisland.engine.core.bukkit.EventManager;
import de.cubeisland.engine.core.command.ArgumentReader;
import de.cubeisland.engine.core.command.CommandManager;
import de.cubeisland.engine.core.filesystem.FileManager;
import de.cubeisland.engine.core.filesystem.TestFileManager;
import de.cubeisland.engine.core.i18n.I18n;
import de.cubeisland.engine.core.module.ModuleManager;
import de.cubeisland.engine.core.module.TestModuleManager;
import de.cubeisland.engine.core.permission.PermissionManager;
import de.cubeisland.engine.core.service.ServiceManager;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.task.TaskManager;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.InventoryGuardFactory;
import de.cubeisland.engine.core.util.Version;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.core.webapi.ApiServer;
import de.cubeisland.engine.core.world.WorldManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Phillip Schichtel
 */
public class TestCore implements Core
{
    private final Version version = Version.ONE;
    private final String sourceVersion = "master-aaaaaaaa";
    private final Logger logger = LoggerFactory.getLogger("");
    private ObjectMapper jsonObjectMapper = null;
    private CoreConfiguration config = null;
    private FileManager fileManager = null;
    private ModuleManager moduleManager = null;

    {
        CubeEngine.initialize(this);
        ArgumentReader.init(this);
    }

    @Override
    public Version getVersion()
    {
        return this.version;
    }

    @Override
    public String getSourceVersion()
    {
        return "unknown-testing";
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
            this.config = Configuration.load(CoreConfiguration.class, this.getFileManager().getDataPath().resolve("core.yml"));
        }
        return this.config;
    }

    @Override
    public org.slf4j.Logger getLog()
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

    @Override
    public BanManager getBanManager()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ServiceManager getServiceManager()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
