package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.basics.cheat.CheatCommands;
import de.cubeisland.cubeengine.basics.general.GeneralCommands;
import de.cubeisland.cubeengine.basics.moderation.ModerationCommands;
import de.cubeisland.cubeengine.core.config.annotations.From;
import de.cubeisland.cubeengine.core.module.Module;

public class Basics extends Module
{
    @From("basics.yml")
    protected BasicsConfiguration config;
    
    @Override
    public void onEnable()
    {
        this.registerPermissions(BasicsPerm.values());
        this.registerCommands(new CheatCommands(this));
        this.registerCommands(new ModerationCommands(this));
        this.registerCommands(new GeneralCommands(this));
    }

    public BasicsConfiguration getConfiguration()
    {
        return this.config;
    }
}