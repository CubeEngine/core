package de.cubeisland.cubeengine.fun;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.fun.commands.FunCommands;

/**
 * Hello world!
 *
 */
public class Fun extends Module
{    
    @Override
    public void onEnable()
    {
        this.getCore().getFileManager().dropResources(FunResource.values());
        this.registerCommands(new FunCommands(this));
    }
}
