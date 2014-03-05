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

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsAttachment;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;

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
            context.sendTranslated(MessageType.NEGATIVE, "{text:ProTip}: Teleport does not work IRL!");
            return;
        }
        sender.get(BasicsAttachment.class).removeTpRequestCancelTask();
        final User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated(MessageType.NEGATIVE, "User {user} not found!", context.getString(0));
            return;
        }
        user.sendTranslated(MessageType.POSITIVE, "{sender} wants to teleport to you!", sender);
        user.sendTranslated(MessageType.NEUTRAL, "Use {text:/tpaccept} to accept or {text:/tpdeny} to deny the request!");
        user.get(BasicsAttachment.class).setPendingTpToRequest(sender.getName());
        user.get(BasicsAttachment.class).removePendingTpFromRequest();
        context.sendTranslated(MessageType.POSITIVE, "Teleport request send to {user}!", user);
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
                    sendingUser.sendTranslated(MessageType.NEGATIVE, "{user} did not accept your teleport-request.", user);
                    user.sendTranslated(MessageType.NEGATIVE, "Teleport-request of {sender} timed out.", sendingUser);
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
                context.sendTranslated(MessageType.NEGATIVE, "User {user} not found!", context.getString(0));
                return;
            }
            user.sendTranslated(MessageType.POSITIVE, "{sender} wants to teleport you to them!", sender);
            user.sendTranslated(MessageType.NEUTRAL, "Use {text:/tpaccept} to accept or {text:/tpdeny} to deny the request!");
            user.get(BasicsAttachment.class).setPendingTpFromRequest(sender.getName());
            user.get(BasicsAttachment.class).removePendingTpToRequest();
            context.sendTranslated(MessageType.POSITIVE, "Teleport request send to {user}!", user);
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
                        sendingUser.sendTranslated(MessageType.NEGATIVE, "{user} did not accept your teleport-request.", user);
                        user.sendTranslated(MessageType.NEGATIVE, "Teleport-request of {sender} timed out.", sendingUser);
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
        context.sendTranslated(MessageType.NEGATIVE, "{text:ProTip}: Teleport does not work IRL!");
    }

    @Command(names = {"tpac", "tpaccept"}, desc = "Accepts any pending teleport-request.", max = 0)
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
                    context.sendTranslated(MessageType.NEGATIVE, "You don't have any pending requests!");
                    return;
                }
                sender.get(BasicsAttachment.class).removePendingTpFromRequest();
                User user = this.basics.getCore().getUserManager().getUser(name, false);
                if (user == null || !user.isOnline())
                {
                    context.sendTranslated(MessageType.NEGATIVE, "{user} seems to have disappeared.", name);
                    return;
                }
                if (!TeleportCommands.teleport(sender, user.getLocation(), true, false, true))
                    return;
                user.sendTranslated(MessageType.POSITIVE, "{user} accepted your teleport-request!", sender);
                context.sendTranslated(MessageType.POSITIVE, "You accepted to get teleported to {user}!", user);
            }
            else
            {
                sender.get(BasicsAttachment.class).removePendingTpToRequest();
                User user = this.basics.getCore().getUserManager().getUser(name, false);
                if (user == null || !user.isOnline())
                {
                    context.sendTranslated(MessageType.NEGATIVE, "{user} seems to have disappeared.", name);
                    return;
                }
                if (!TeleportCommands.teleport(user, sender.getLocation(), true, false, true))
                    return;
                user.sendTranslated(MessageType.POSITIVE, "{user} accepted your teleport-request!", sender);
                context.sendTranslated(MessageType.POSITIVE, "You accepted to teleport to {user}!", user);
            }
            Integer taskID = sender.get(BasicsAttachment.class).getTpRequestCancelTask();
            if (taskID != null)
            {
                sender.get(BasicsAttachment.class).removeTpRequestCancelTask();
                context.getCore().getTaskManager().cancelTask(this.basics, taskID);
            }
            return;
        }
        context.sendTranslated(MessageType.NEGATIVE, "No one wants to teleport to you!");
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
            context.sendTranslated(MessageType.NEGATIVE, "No one wants to teleport to you!");
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
            user.sendTranslated(MessageType.NEGATIVE, "{user} denied your teleport-request!", sender);
            context.sendTranslated(MessageType.NEGATIVE, "You denied {user}'s teleport-request!", user);
        }
        else if (tpahere != null)
        {
            sender.get(BasicsAttachment.class).removePendingTpFromRequest();
            User user = this.basics.getCore().getUserManager().getUser(tpahere, false);
            if (user == null)
            {
                throw new IllegalStateException("User saved in \"pendingTpFromRequest\" was not found!");
            }
            user.sendTranslated(MessageType.NEGATIVE, "{user} denied your request!", sender);
            context.sendTranslated(MessageType.NEGATIVE, "You denied {user}'s teleport-request", user);
        }
        else
        {
            context.sendTranslated(MessageType.NEGATIVE, "You don't have any pending requests!");
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
