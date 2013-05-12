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
package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.log.action.ActionTypeManager;
import de.cubeisland.cubeengine.log.action.logaction.container.ContainerType;
import de.cubeisland.cubeengine.log.action.logaction.container.ContainerTypeConverter;
import de.cubeisland.cubeengine.log.action.logaction.worldedit.LogEditSessionFactory;
import de.cubeisland.cubeengine.log.commands.LogCommands;
import de.cubeisland.cubeengine.log.commands.LookupCommands;
import de.cubeisland.cubeengine.log.storage.LogManager;
import de.cubeisland.cubeengine.log.tool.ToolListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk89q.worldedit.WorldEdit;

public class Log extends Module
{
    private LogManager logManager;
    private LogConfiguration config;
    private ObjectMapper objectMapper = null;
    private ActionTypeManager actionTypeManager;

    @Override
    public void onEnable()
    {
        this.config = Configuration.load(LogConfiguration.class, this);
        Convert.registerConverter(ContainerType.class, new ContainerTypeConverter());
        //        TODO when sending logs to player
        //        if same player and block type do not use 1 line for each block
        //        but instead something like this:
        //        <Player> BlockBreak <BlockType> x<times> at <cuboid> 
        // perhaps make possible to select this cuboid to rollback later
        //flag to ignore what block
        //possibility to select the region containing the last search results
        this.logManager = new LogManager(this);
        this.actionTypeManager = new ActionTypeManager(this);
        this.actionTypeManager.registerLogActionTypes();

        final CommandManager cm = this.getCore().getCommandManager();
        cm.registerCommand(new LookupCommands(this));
        cm.registerCommand(new LogCommands(this));

        try
        {
            Class.forName("com.sk89q.worldedit.WorldEdit");
            LogEditSessionFactory.initialize(WorldEdit.getInstance(), this);
        }
        catch (ClassNotFoundException ignored)
        {
            System.out.print("No WorldEdit found!");
        }

        this.getCore().getEventManager().registerListener(this, new ToolListener(this));

    }

    @Override
    public void onDisable()
    {
        this.logManager.disable();
        super.onDisable();
    }

    public LogManager getLogManager()
    {
        return this.logManager;
    }

    public LogConfiguration getConfiguration() {
        return this.config;
    }

    public ObjectMapper getObjectMapper()
    {
        if (this.objectMapper == null)
        {
            this.objectMapper = new ObjectMapper();
        }
        return objectMapper;
    }

    public ActionTypeManager getActionTypeManager()
    {
        return actionTypeManager;
    }
}
