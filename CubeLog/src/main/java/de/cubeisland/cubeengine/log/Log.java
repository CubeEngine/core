package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.config.annotations.From;
import de.cubeisland.cubeengine.log.listeners.LogListener;
import de.cubeisland.cubeengine.core.module.Module;

public class Log extends Module
{
    private LogManager lm;
    
    @From("config")
    protected LogConfiguration config;
    
    
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
        this.lm = new LogManager();

    }

    public LogManager getLogManager()
    {
        return this.lm;
    }
    
    public LogConfiguration getConfiguration()
    {
        return this.config;
    }
}
