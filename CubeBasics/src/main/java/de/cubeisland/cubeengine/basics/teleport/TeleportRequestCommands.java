package de.cubeisland.cubeengine.basics.teleport;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;

import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;

/**
 * Contains Teleport-Request commands.
 * /tpa
 * /tpahere
 * /tpaccept
 * /tpdeny
 */
public class TeleportRequestCommands
{
    private Basics basics;

    public TeleportRequestCommands(Basics basics)
    {
        this.basics = basics;
    }

    @Command(desc = "Requests to teleport to a player.", usage = "<player>", min = 1, max = 1)
    public void tpa(CommandContext context)
    {
        final User sender = context.getSenderAsUser("basics", "&6ProTip: &cTeleport does not work IRL!");
        sender.removeAttribute(basics, "tpRequestCancelTask");
        final User user = context.getUser(0);
        if (user == null)
        {
            paramNotFound(context, "basics", "&cUser &2%s &cnot found!", context.getString(0));
        }
        user.sendMessage("basics", "&2%s &awants to teleport to you!\nUse &e/tpaccept &ato accept or &c/tpdeny &ato deny the request!", sender.getName());
        user.setAttribute(basics, "pendingTpToRequest", sender.getName());
        user.removeAttribute(basics, "pendingTpFromRequest");
        context.sendMessage("basics", "&aTeleport request send to &2%s&a!", user.getName());
        int waitTime = this.basics.getConfiguration().tpRequestWait * 20;
        if (waitTime > 0)
        {
            final int taskID = context.getCore().getTaskManager().scheduleSyncDelayedTask(basics, new Runnable()
            {
                public void run()
                {
                    user.removeAttribute(basics, "tpRequestCancelTask");
                    user.removeAttribute(basics, "pendingTpToRequest");
                    sender.sendMessage("basics", "&2%s &cdid not accept your teleport-request.", user.getName());
                    user.sendMessage("basics", "&cTeleport-request of &2%s &ctimed out.", sender.getName());
                }
            }, waitTime); // wait x - seconds
            Integer oldtaskID = (Integer)user.getAttribute(basics, "tpRequestCancelTask");
            if (oldtaskID != null)
            {
                context.getCore().getTaskManager().cancelTask(basics, oldtaskID);
            }
            user.setAttribute(basics, "tpRequestCancelTask", taskID);
        }
    }

    @Command(desc = "Requests to teleport a player to you.", usage = "<player>", min = 1, max = 1)
    public void tpahere(CommandContext context)
    {
        final User sender = context.getSenderAsUser("basics", "&6ProTip: &cTeleport does not work IRL!");
        sender.removeAttribute(basics, "tpRequestCancelTask");
        final User user = context.getUser(0);
        if (user == null)
        {
            paramNotFound(context.getSender(), "basics", "&cUser &2%s &cnot found!", context.getString(0));
        }
        user.sendMessage("basics", "&2%s &awants to teleport you to him!\nUse &e/tpaccept &ato accept or &c/tpdeny &ato deny the request!", sender.getName());
        user.setAttribute(basics, "pendingTpFromRequest", sender.getName());
        user.removeAttribute(basics, "pendingTpToRequest");
        context.sendMessage("basics", "&aTeleport request send to &2%s!", user.getName());
        int waitTime = this.basics.getConfiguration().tpRequestWait * 20;
        if (waitTime > 0)
        {
            final int taskID = context.getCore().getTaskManager().scheduleSyncDelayedTask(basics, new Runnable()
            {
                public void run()
                {
                    user.removeAttribute(basics, "tpRequestCancelTask");
                    user.removeAttribute(basics, "pendingTpFromRequest");
                    sender.sendMessage("basics", "&2%s &cdid not accept your teleport-request.", user.getName());
                    user.sendMessage("basics", "&cTeleport-request of &2%s &ctimed out.", sender.getName());
                }
            }, waitTime); // wait x - seconds
            Integer oldtaskID = (Integer)user.getAttribute(basics, "tpRequestCancelTask");
            if (oldtaskID != null)
            {
                context.getCore().getTaskManager().cancelTask(basics, oldtaskID);
            }
            user.setAttribute(basics, "tpRequestCancelTask", taskID);
        }
    }

    @Command(names = {
        "tpac", "tpaccept"
    }, desc = "Accepts any pending teleport-request.", max = 0)
    public void tpaccept(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&cNo one wants to teleport to you!");
        String name = sender.getAttribute(basics, "pendingTpToRequest");
        if (name == null)
        {
            name = sender.getAttribute(basics, "pendingTpFromRequest");
            if (name == null)
            {
                blockCommand(context, "basics", "&cYou don't have any pending requests!");
            }
            sender.removeAttribute(basics, "pendingTpFromRequest");
            User user = basics.getUserManager().getUser(name, false);
            if (user == null || !user.isOnline())
            {
                blockCommand(context, "basics", "&2%s &cseems to have disappeared.", user.getName());
            }
            TeleportCommands.teleport(sender, user.getLocation(), true, false);
            user.sendMessage("bascis", "&2%s &aaccepted your teleport-request!", sender.getName());
            context.sendMessage("basics", "&aYou accepted to get teleported to &2%s&a!", user.getName());
        }
        else
        {
            sender.removeAttribute(basics, "pendingTpToRequest");
            User user = basics.getUserManager().getUser(name, false);
            if (user == null || !user.isOnline())
            {
                blockCommand(context, "basics", "&2%s &cseems to have disappeared.", user.getName());
            }
            TeleportCommands.teleport(user, sender.getLocation(), true, false);

            user.sendMessage("bascis", "&2%s &aaccepted your teleport-request!", sender.getName());
            context.sendMessage("basics", "&aYou accepted to teleport to &2%s&a!", user.getName());
        }
        Integer taskID = (Integer)sender.getAttribute(basics, "tpRequestCancelTask");
        sender.removeAttribute(basics, "tpRequestCancelTask");
        if (taskID != null)
        {
            context.getCore().getTaskManager().cancelTask(basics, taskID);
        }
    }

    @Command(desc = "Denies any pending teleport-request.", max = 0)
    public void tpdeny(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&cNo one wants to teleport to you!");
        String tpa = sender.getAttribute(basics, "pendingTpToRequest");
        String tpahere = sender.getAttribute(basics, "pendingTpFromRequest");
        if (tpa != null)
        {
            sender.removeAttribute(basics, "pendingTpToRequest");
            User user = basics.getUserManager().getUser(tpa, false);
            if (user == null)
            {
                throw new IllegalStateException("User saved in \"pendingTpToRequest\" was not found!");
            }
            user.sendMessage("basics", "&2%s &cdenied your teleport-request!", sender.getName());
            context.sendMessage("basics", "&cYou denied &a%s's &cteleport-request!", user.getName());
        }
        else if (tpahere != null)
        {
            sender.removeAttribute(basics, "pendingTpFromRequest");
            User user = basics.getUserManager().getUser(tpahere, false);
            if (user == null)
            {
                throw new IllegalStateException("User saved in \"pendingTpFromRequest\" was not found!");
            }
            user.sendMessage("basics", "&2%s &cdenied your request!", sender.getName());
            context.sendMessage("basics", "&cYou denied &2%s's &cteleport-request", user.getName());
        }
        else
        {
            blockCommand(context, "basics", "&cYou don't have any pending requests!");
        }
        Integer taskID = (Integer)sender.getAttribute(basics, "tpRequestCancelTask");
        sender.removeAttribute(basics, "tpRequestCancelTask");
        if (taskID != null)
        {
            context.getCore().getTaskManager().cancelTask(basics, taskID);
        }
    }
}
