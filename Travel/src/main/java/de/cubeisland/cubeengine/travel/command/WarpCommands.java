package de.cubeisland.cubeengine.travel.command;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.travel.storage.TelePointManager;
import de.cubeisland.cubeengine.travel.storage.Warp;
import org.bukkit.event.player.PlayerTeleportEvent;

public class WarpCommands
{
    private final TelePointManager tpManager;

    public WarpCommands(TelePointManager tpManager)
    {
        this.tpManager = tpManager;
    }

    @Command(desc = "Teleport to a warp", min = 1, max = 1)
    public void warp(CommandContext context)
    {
        if (context.getSender() instanceof  User)
        {
            User sender = (User) context.getSender();
            Warp warp = tpManager.getWarp(sender, context.getString(0).toLowerCase());
            if (warp == null)
            {
                context.sendMessage("travel", "&4You don't have access to any warp with that name");
                return;
            }

            sender.teleport(warp.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
            context.sendMessage("travel", "&6You have been teleported to the warp &9%s", context.getString(0));
        }
        else
        {
            context.sendMessage("travel", "&4This command can only be used by users!");
        }

    }
}
