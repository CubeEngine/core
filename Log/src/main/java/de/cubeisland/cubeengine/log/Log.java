package de.cubeisland.cubeengine.log;

import com.sk89q.worldedit.WorldEdit;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.log.commands.LogCommands;
import de.cubeisland.cubeengine.log.commands.LookupCommands;
import de.cubeisland.cubeengine.log.listeners.worldedit.LogEditSessionFactory;
import de.cubeisland.cubeengine.log.storage.LogManager;
import de.cubeisland.cubeengine.log.tool.ToolListener;
import org.bukkit.World;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Log extends Module
{
    private static Log instance;

    private LogManager logManager;

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
        this.registerCommand(new LookupCommands(this));
        this.registerCommand(new LogCommands(this));




        try
        {
            Class.forName("com.sk89q.worldedit.WorldEdit");
            LogEditSessionFactory.initialize(WorldEdit.getInstance(), this);
        }
        catch (ClassNotFoundException ignored)
        {
            System.out.print("No WorldEdit found!");
        }

        this.registerListener(new ToolListener(this));

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
}
