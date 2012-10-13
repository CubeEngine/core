package de.cubeisland.cubeengine.writer;

import de.cubeisland.cubeengine.core.module.Module;


public class Writer extends Module
{
    @Override
    public void onEnable()
    {
    	registerCommands(new EditCommand(this.getCore()));
    }


    @Override
    public void onDisable()
    {
    }


}
