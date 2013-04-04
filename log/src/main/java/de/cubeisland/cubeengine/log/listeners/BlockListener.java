package de.cubeisland.cubeengine.log.listeners;

import org.bukkit.event.Listener;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.LogManager;

public class BlockListener implements Listener
{
    private LogManager manager;
    private Log module;

    public BlockListener(Log module, LogManager manager)
    {
        this.module = module;
        this.manager = manager;
    }

}
