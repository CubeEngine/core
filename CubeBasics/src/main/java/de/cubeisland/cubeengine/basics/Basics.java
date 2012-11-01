package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.basics.cheat.CheatCommands;
import de.cubeisland.cubeengine.basics.cheat.CheatListener;
import de.cubeisland.cubeengine.basics.general.GeneralCommands;
import de.cubeisland.cubeengine.basics.general.MailCommand;
import de.cubeisland.cubeengine.basics.moderation.ModerationCommands;
import de.cubeisland.cubeengine.basics.moderation.ModerationListener;
import de.cubeisland.cubeengine.basics.teleport.TeleportCommands;
import de.cubeisland.cubeengine.basics.teleport.TeleportListener;
import de.cubeisland.cubeengine.core.config.annotations.From;
import de.cubeisland.cubeengine.core.module.Module;

public class Basics extends Module
{
    @From
    protected BasicsConfiguration config;
    private BasicUserManager basicUM;

    @Override
    public void onEnable()
    {
        this.basicUM = new BasicUserManager(this.getDatabase());
        this.registerPermissions(BasicsPerm.values());
        this.registerCommands(new CheatCommands(this));
        this.registerCommands(new ModerationCommands(this));
        this.registerCommands(new GeneralCommands(this));
        this.registerCommand(new ModuleCommands(this));
        this.registerCommands(new TeleportCommands(this));
        this.registerCommands(new MailCommand(this));

        this.registerListener(new TeleportListener(this));
        this.registerListener(new ModerationListener(this));
        this.registerListener(new CheatListener(this));
        //TODO register permissions of kits in config
    }

    public BasicsConfiguration getConfiguration()
    {
        return this.config;
    }

    public BasicUserManager getBasicUserManager()
    {
        return this.basicUM;
    }
}