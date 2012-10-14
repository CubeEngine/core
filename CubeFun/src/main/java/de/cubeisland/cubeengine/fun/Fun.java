package de.cubeisland.cubeengine.fun;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.fun.commands.FunCommands;
import de.cubeisland.cubeengine.fun.listeners.NukeListener;
import de.cubeisland.cubeengine.fun.listeners.RocketListener;

/**
 * Hello world!
 *
 */
public class Fun extends Module
{
    RocketListener rocketListener;
    NukeListener nukeListener;

    @Override
    public void onEnable()
    {
        this.rocketListener = new RocketListener(this);
        this.nukeListener = new NukeListener(this);

        this.getCore().getFileManager().dropResources(FunResource.values());
        this.registerCommands(new FunCommands(this));
        this.registerListener(this.rocketListener);
        this.registerListener(this.nukeListener);
    }

    public RocketListener getRocketListener()
    {
        return this.rocketListener;
    }
    
    public NukeListener getNukeListener()
    {
        return this.nukeListener;
    }
}
