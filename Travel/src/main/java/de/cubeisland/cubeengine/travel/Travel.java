package de.cubeisland.cubeengine.travel;

import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommand;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.travel.command.HomeCommands;
import de.cubeisland.cubeengine.travel.command.WarpCommands;
import de.cubeisland.cubeengine.travel.command.subcommand.HomeAdminSub;
import de.cubeisland.cubeengine.travel.command.subcommand.HomeSubCommands;
import de.cubeisland.cubeengine.travel.command.subcommand.WarpSubCommands;
import de.cubeisland.cubeengine.travel.storage.InviteManager;
import de.cubeisland.cubeengine.travel.storage.TelePointManager;

public class Travel extends Module
{
    private TelePointManager telePointManager;
    private InviteManager inviteManager;

    private TravelConfig config;

    @Override
    public void onEnable()
    {
        this.telePointManager = new TelePointManager(this);
        this.inviteManager = new InviteManager(this.getCore().getDB());
        this.telePointManager.load(this.inviteManager);

        final CommandManager cm = this.getCore().getCommandManager();
        cm.registerCommands(this, new HomeCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new HomeSubCommands(this), ReflectedCommand.class, "home");
        cm.registerCommands(this, new HomeAdminSub(this), ReflectedCommand.class, "home", "admin");
        cm.registerCommands(this, new WarpCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new WarpSubCommands(this),ReflectedCommand.class, "warp");
    }

    public TravelConfig getConfig()
    {
        return this.config;
    }

    public TelePointManager getTelepointManager()
    {
        return this.telePointManager;
    }

    public InviteManager getInviteManager()
    {
        return this.inviteManager;
    }
}
