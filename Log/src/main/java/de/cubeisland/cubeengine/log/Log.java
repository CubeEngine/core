package de.cubeisland.cubeengine.log;

import com.sk89q.worldedit.WorldEdit;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.log.commands.LogCommands;
import de.cubeisland.cubeengine.log.logger.LoggerManager;
import de.cubeisland.cubeengine.log.logger.worldedit.LogEditSessionFactory;
import de.cubeisland.cubeengine.log.storage.*;
import de.cubeisland.cubeengine.log.tool.ToolListener;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Log extends Module
{
    private static Log instance;
    private LogConfiguration globalConfig;
    private Map<World, LogConfiguration> worldConfigs = new HashMap<World, LogConfiguration>();
    private LogManager logManager;
    private LoggerManager loggerManager;

    static
    {
        Convert.registerConverter(BlockData.class, new BlockDataConverter());
        Convert.registerConverter(ItemData.class, new ItemDataConverter());
    }

    public Log()
    {
        instance = this;
    }

    @Override
    public void onEnable()
    {
        //        TODO when sending logs to player
        //        if same player and blocktype do not use 1 line for each block
        //        but instead smth like this:
        //        <Player> BlockBreak <BlockType> x<times> at <cuboid> 
        // perhaps make possible to select this cuboid to rollback later
        //flag to ignore what block
        //possibility to select the region containing the last search results
        this.logManager = new LogManager(this);
        this.registerCommand(new LogCommands(this));
        File file = new File(this.getFolder(), "worlds");
        file.mkdir();

        this.globalConfig = Configuration.load(LogConfiguration.class, new File(this.getFolder(), "globalconfig.yml"));
        for (World world : Bukkit.getServer().getWorlds())
        {
            //TODO config to disable logging in the entire world
            file = new File(this.getFolder(), "worlds" + File.separator + world.getName());
            file.mkdir();
            this.worldConfigs.put(world, (LogConfiguration)globalConfig.loadChild(new File(file, "config.yml")));
        }

        try
        {
            Class.forName("com.sk89q.worldedit.WorldEdit");
            LogEditSessionFactory.initialize(WorldEdit.getInstance(), this);
        }
        catch (ClassNotFoundException ignored)
        {
            System.out.print("No WorldEdit found!");
        }

        this.loggerManager = new LoggerManager(this);

        this.registerListener(new ToolListener(this));

    }

    @Override
    public void onDisable()
    {
        this.logManager.disable();
        super.onDisable();
    }

    public Map<World, LogConfiguration> getConfigurations()
    {
        return this.worldConfigs;
    }

    public LogManager getLogManager()
    {
        return this.logManager;
    }

    public LogConfiguration getGlobalConfiguration()
    {
        return this.globalConfig;
    }

    public LoggerManager getLoggerManager()
    {
        return loggerManager;
    }
}
