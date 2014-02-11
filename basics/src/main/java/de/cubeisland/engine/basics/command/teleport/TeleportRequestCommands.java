/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.basics.command.teleport;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsAttachment;

/**
 * Contains Teleport-Request commands.
 * /tpa
 * /tpahere
 * /tpaccept
 * /tpdeny
 */
public class TeleportRequestCommands
{
    private final Basics basics;

    public TeleportRequestCommands(Basics basics)
    {
        this.basics = basics;
    }

    @Command(desc = "Requests to teleport to a player.", usage = "<player>", min = 1, max = 1)
    public void tpa(CommandContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        if (sender == null)
        {
            context.sendTranslated("&6ProTip: &cTeleport does not work IRL!");
            return;
        }
        sender.get(BasicsAttachment.class).removeTpRequestCancelTask();
        final User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
            return;
        }
        user.sendTranslated("&2%s &awants to teleport to you!\nUse &e/tpaccept &ato accept or &c/tpdeny &ato deny the request!", sender.getName());
        user.get(BasicsAttachment.class).setPendingTpToRequest(sender.getName());
        user.get(BasicsAttachment.class).removePendingTpFromRequest();
        context.sendTranslated("&aTeleport request send to &2%s&a!", user.getName());
        int waitTime = this.basics.getConfiguration().commands.teleportRequestWait * 20;
        if (waitTime > 0)
        {
            final User sendingUser = sender;
            final int taskID = context.getCore().getTaskManager().runTaskDelayed(this.basics, new Runnable()
            {
                public void run()
                {
                    user.get(BasicsAttachment.class).removeTpRequestCancelTask();
                    user.get(BasicsAttachment.class).removePendingTpToRequest();
                    sendingUser.sendTranslated("&2%s &cdid not accept your teleport-request.", user.getName());
                    user.sendTranslated("&cTeleport-request of &2%s &ctimed out.", sendingUser.getName());
                }
            }, waitTime); // wait x - seconds
            Integer oldtaskID = user.get(BasicsAttachment.class).getTpRequestCancelTask();
            if (oldtaskID != null)
            {
                context.getCore().getTaskManager().cancelTask(this.basics, oldtaskID);
            }
            user.get(BasicsAttachment.class).setTpRequestCancelTask(taskID);
        }
    }

    @Command(desc = "Requests to teleport a player to you.", usage = "<player>", min = 1, max = 1)
    public void tpahere(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            sender.get(BasicsAttachment.class).removeTpRequestCancelTask();
            final User user = context.getUser(0);
            if (user == null)
            {
                context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
                return;
            }
            user.sendTranslated("&2%s &awants to teleport you to them!\nUse &e/tpaccept &ato accept or &c/tpdeny &ato deny the request!", sender.getName());
            user.get(BasicsAttachment.class).setPendingTpFromRequest(sender.getName());
            user.get(BasicsAttachment.class).removePendingTpToRequest();
            context.sendTranslated("&aTeleport request send to &2%s!", user.getName());
            int waitTime = this.basics.getConfiguration().commands.teleportRequestWait * 20;
            if (waitTime > 0)
            {
                final User sendingUser = sender;
                final int taskID = context.getCore().getTaskManager().runTaskDelayed(this.basics, new Runnable()
                {
                    public void run()
                    {
                        user.get(BasicsAttachment.class).removeTpRequestCancelTask();
                        user.get(BasicsAttachment.class).removePendingTpFromRequest();
                        sendingUser.sendTranslated("&2%s &cdid not accept your teleport-request.", user.getName());
                        user.sendTranslated("&cTeleport-request of &2%s &ctimed out.", sendingUser.getName());
                    }
                }, waitTime); // wait x - seconds
                Integer oldtaskID = user.get(BasicsAttachment.class).getTpRequestCancelTask();
                if (oldtaskID != null)
                {
                    context.getCore().getTaskManager().cancelTask(this.basics, oldtaskID);
                }
                user.get(BasicsAttachment.class).setTpRequestCancelTask(taskID);
            }
            return;
        }
        context.sendTranslated("&6ProTip: &cTeleport does not work IRL!");
    }

    @Command(names = {
        "tpac", "tpaccept"
    }, desc = "Accepts any pending teleport-request.", max = 0)
    public void tpaccept(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            String name = sender.get(BasicsAttachment.class).getPendingTpToRequest();
            if (name == null)
            {
                name = sender.get(BasicsAttachment.class).getPendingTpFromRequest();
                if (name == null)
                {
                    context.sendTranslated("&cYou don't have any pending requests!");
                    return;
                }
                sender.get(BasicsAttachment.class).removePendingTpFromRequest();
                User user = this.basics.getCore().getUserManager().getUser(name, false);
                if (user == null || !user.isOnline())
                {
                    context.sendTranslated("&2%s &cseems to have disappeared.", name);
                    return;
                }
                if (!TeleportCommands.teleport(sender, user.getLocation(), true, false, true))
                    return;
                user.sendTranslated("&2%s &aaccepted your teleport-request!", sender.getName());
                context.sendTranslated("&aYou accepted to get teleported to &2%s&a!", user.getName());
            }
            else
            {
                sender.get(BasicsAttachment.class).removePendingTpToRequest();
                User user = this.basics.getCore().getUserManager().getUser(name, false);
                if (user == null || !user.isOnline())
                {
                    context.sendTranslated("&2%s &cseems to have disappeared.", name);
                    return;
                }
                if (!TeleportCommands.teleport(user, sender.getLocation(), true, false, true))
                    return;
                user.sendTranslated("&2%s &aaccepted your teleport-request!", sender.getName());
                context.sendTranslated("&aYou accepted to teleport to &2%s&a!", user.getName());
            }
            Integer taskID = sender.get(BasicsAttachment.class).getTpRequestCancelTask();
            if (taskID != null)
            {
                sender.get(BasicsAttachment.class).removeTpRequestCancelTask();
                context.getCore().getTaskManager().cancelTask(this.basics, taskID);
            }
            return;
        }
        context.sendTranslated("&cNo one wants to teleport to you!");
    }

    @Command(desc = "Denies any pending teleport-request.", max = 0)
    public void tpdeny(CommandContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        if (sender == null)
        {
            context.sendTranslated("&cNo one wants to teleport to you!");
            return;
        }
        String tpa =  sender.get(BasicsAttachment.class).getPendingTpToRequest();
        String tpahere = sender.get(BasicsAttachment.class).getPendingTpFromRequest();
        if (tpa != null)
        {
            sender.get(BasicsAttachment.class).removePendingTpToRequest();
            User user = this.basics.getCore().getUserManager().getUser(tpa, false);
            if (user == null)
            {
                throw new IllegalStateException("User saved in \"pendingTpToRequest\" was not found!");
            }
            user.sendTranslated("&2%s &cdenied your teleport-request!", sender.getName());
            context.sendTranslated("&cYou denied &a%s's &cteleport-request!", user.getName());
        }
        else if (tpahere != null)
        {
            sender.get(BasicsAttachment.class).removePendingTpFromRequest();
            User user = this.basics.getCore().getUserManager().getUser(tpahere, false);
            if (user == null)
            {
                throw new IllegalStateException("User saved in \"pendingTpFromRequest\" was not found!");
            }
            user.sendTranslated("&2%s &cdenied your request!", sender.getName());
            context.sendTranslated("&cYou denied &2%s's &cteleport-request", user.getName());
        }
        else
        {
            context.sendTranslated("&cYou don't have any pending requests!");
            return;
        }
        Integer taskID = sender.get(BasicsAttachment.class).getTpRequestCancelTask();
        if (taskID != null)
        {
            sender.get(BasicsAttachment.class).removeTpRequestCancelTask();
            context.getCore().getTaskManager().cancelTask(this.basics, taskID);
        }
    }
}
