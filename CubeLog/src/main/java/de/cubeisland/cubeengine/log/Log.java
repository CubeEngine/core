package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.config.annotations.From;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import de.cubeisland.cubeengine.log.commands.LogCommands;
import de.cubeisland.cubeengine.log.storage.BlockData;
import de.cubeisland.cubeengine.log.storage.BlockDataConverter;
import de.cubeisland.cubeengine.log.storage.ItemData;
import de.cubeisland.cubeengine.log.storage.ItemDataConverter;

public class Log extends Module
{
    private LogManager lm;
    private static Log instance;
    @From("config")
    protected LogConfiguration config;

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
        this.lm = new LogManager(this);
        this.registerCommand(new LogCommands(this));
        Convert.registerConverter(BlockData.class, new BlockDataConverter());
        Convert.registerConverter(ItemData.class, new ItemDataConverter());
    }

    public LogManager getLogManager()
    {
        return this.lm;
    }

    public LogConfiguration getConfiguration()
    {
        return this.config;
    }

    public static Log getInstance()
    {
        return instance;
    }
}