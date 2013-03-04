package de.cubeisland.cubeengine.travel;

import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommand;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.travel.storage.InviteManager;
import de.cubeisland.cubeengine.travel.storage.TelePointManager;
import de.cubeisland.cubeengine.travel.command.HomeCommands;
import de.cubeisland.cubeengine.travel.command.WarpCommands;
import de.cubeisland.cubeengine.travel.command.subcommand.HomeAdminSub;
import de.cubeisland.cubeengine.travel.command.subcommand.HomeSubCommands;
import de.cubeisland.cubeengine.travel.command.subcommand.WarpSubCommands;

public class Travel extends Module
{
    private TelePointManager tpManager;
    private InviteManager inviteManager;

    private TravelConfig config;
    private HomeCommands homeCommands;
    private HomeSubCommands homeSubCommands;
    private HomeAdminSub homeAdminSub;
    private WarpCommands warpCommands;
    private WarpSubCommands warpSubCommands;

    @Override
    public void onEnable()
    {
        this.tpManager = new TelePointManager(this.getDatabase(), this);
        this.inviteManager = new InviteManager(this.getDatabase());
        this.tpManager.load(inviteManager);

        this.homeCommands = new HomeCommands(this, tpManager);
        this.homeSubCommands = new HomeSubCommands(this, this.tpManager);
        this.homeAdminSub = new HomeAdminSub(this, this.tpManager);
        this.warpCommands = new WarpCommands(tpManager);
        this.warpSubCommands = new WarpSubCommands(tpManager);

        this.registerCommands(this.homeCommands, ReflectedCommand.class);
        this.registerCommands(this.homeSubCommands, ReflectedCommand.class, "home");
        this.registerCommands(this.homeAdminSub, ReflectedCommand.class, "home", "admin");
        this.registerCommands(this.warpCommands, ReflectedCommand.class);
        this.registerCommands(this.warpSubCommands,ReflectedCommand.class, "warp");
    }

    public TravelConfig getConfig()
    {
        return config;
    }

}
