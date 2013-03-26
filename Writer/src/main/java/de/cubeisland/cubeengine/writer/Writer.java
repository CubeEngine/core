package de.cubeisland.cubeengine.writer;

import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommand;
import de.cubeisland.cubeengine.core.module.Module;

public class Writer extends Module
{
    @Override
    public void onEnable()
    {
        this.getCore().getFileManager().dropResources(WriterResource.values());
        this.getCore().getCommandManager().registerCommands(this, new EditCommand(), ReflectedCommand.class);
    }

    @Override
    public void onDisable()
    {}
}
