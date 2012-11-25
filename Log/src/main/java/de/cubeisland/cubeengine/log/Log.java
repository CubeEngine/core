package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.log.commands.LogCommands;
import de.cubeisland.cubeengine.log.storage.BlockData;
import de.cubeisland.cubeengine.log.storage.BlockDataConverter;
import de.cubeisland.cubeengine.log.storage.ItemData;
import de.cubeisland.cubeengine.log.storage.ItemDataConverter;
import de.cubeisland.cubeengine.log.storage.LogManager;

public class Log extends Module
{
    private static Log instance;
    private LogConfiguration mainconfig;

    static
    {
        Convert.registerConverter(BlockData.class, new BlockDataConverter());
        Convert.registerConverter(ItemData.class, new ItemDataConverter());
    }

    public Log()
    {
        instance = this;
    }

    //TODO config for each world -> config should be empty!
    //but inherit from global config how to do this???
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
        //this.lm = new LogManager(this);
        LogManager.init(this.getDatabase());
        this.registerCommand(new LogCommands(this));

    }

    public LogConfiguration getConfiguration()
    {
        return null;
    }

    public static Log getInstance()
    {
        return instance;
    }
}
