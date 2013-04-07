package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.command.CommandManager;
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
