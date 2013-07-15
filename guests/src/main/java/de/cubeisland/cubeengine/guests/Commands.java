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
package de.cubeisland.cubeengine.guests;

import java.util.Set;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.guests.prevention.Prevention;
import de.cubeisland.cubeengine.guests.prevention.PreventionManager;

import gnu.trove.set.hash.THashSet;

public class Commands extends ContainerCommand
{
    private final Guests guests;
    private final PreventionManager pm;
    private final Set<CommandSender> resetRequest;

    public Commands(Guests guests)
    {
        super(guests, "guests", "Manage the guests module ingame.");
        this.guests = guests;
        this.pm = guests.getPreventionManager();
        this.resetRequest = new THashSet<CommandSender>();
    }

    @Command(desc = "", usage = "[prevention]")
    public void reload(CommandContext context)
    {
        if (context.hasArg(0))
        {
            final Prevention prevention = this.pm.getPrevention(context.getString(0));
            if (prevention != null)
            {
                this.pm.disablePrevention(prevention);
                prevention.reloadConfig();
                this.pm.enablePrevention(prevention);
                context.sendTranslated("preventionReloaded", prevention.getName());
            }
            else
            {
                context.sendTranslated("preventionNotFound");
            }
        }
        else
        {
            // TODO reload module
            // this.guests.getModuleManager().reloadModule(this.guests);

            context.sendTranslated("reloaded", guests.getName());
        }
    }

    @Command(desc = "")
    public void reset(CommandContext context)
    {
        final CommandSender sender = context.getSender();
        if (resetRequest.contains(sender))
        {

            for (Prevention prevention : this.pm.getPreventions())
            {
                prevention.resetConfig();
            }

            context.sendTranslated("configsResetted");
        }
        else
        {
            resetRequest.add(sender);
            context.sendTranslated("resetRequested");
            if (sender instanceof User)
            {
                this.broadcastResetNotice(sender.translate("playerRequestedReset", sender.getDisplayName()), context);
            }
            else
            {
                this.broadcastResetNotice(sender.translate("consoleRequestedReset"), context);
            }
        }
    }

    @Command(desc = "", usage = "<prevention|*> [-t]")
    public void enable(ParameterizedContext context)
    {
        if (context.hasArg(0))
        {
            boolean temporary = context.hasFlag("t");
            if ("*".equals(context.getString(0)))
            {
                for (Prevention prevention : this.pm.getPreventions())
                {
                    this.pm.enablePrevention(prevention);
                    if (!temporary)
                    {
                        prevention.getConfig().set("enable", true);
                        prevention.saveConfig();
                    }
                }

                context.sendTranslated("preventionsEnabled");
            }
            else
            {
                Prevention prevention = this.pm.getPrevention(context.getString(0));
                if (prevention != null)
                {
                    if (!prevention.isEnabled())
                    {
                        if (this.pm.enablePrevention(prevention))
                        {
                            context.sendTranslated("preventionEnabled");
                            if (!temporary)
                            {
                                prevention.getConfig().set("enable", true);
                                prevention.saveConfig();
                            }
                        }
                        else
                        {
                            context.sendTranslated("somethingFailed");
                        }
                    }
                    else
                    {
                        context.sendTranslated("alreadyEnabled");
                    }
                }
                else
                {
                    context.sendTranslated("preventionNotFound");
                }
            }
        }
        else
        {
            context.sendTranslated("noPrevention");
        }
    }

    @Command(desc = "", usage = "<prevention> [-t]")
    public void disable(ParameterizedContext context)
    {
        if (context.hasArg(0))
        {
            boolean temporary = context.hasFlag("t");
            if ("*".equals(context.getString(0)))
            {
                for (Prevention prevention : pm.getPreventions())
                {
                    pm.disablePrevention(prevention);
                    if (!temporary)
                    {
                        prevention.getConfig().set("enable", false);
                        prevention.saveConfig();
                    }
                }

                context.sendTranslated("preventionsDisabled");
            }
            else
            {
                Prevention prevention = this.pm.getPrevention(context.getString(0));
                if (prevention != null)
                {
                    if (prevention.isEnabled())
                    {
                        this.pm.disablePrevention(prevention);
                        context.sendTranslated("preventionDisabled");
                        if (!temporary)
                        {
                            prevention.getConfig().set("enable", false);
                            prevention.saveConfig();
                        }
                    }
                    else
                    {
                        context.sendTranslated("alreadyDisabled");
                    }
                }
                else
                {
                    context.sendTranslated("preventionNotFound");
                }
            }
        }
        else
        {
            context.sendTranslated("noPrevention");
        }
    }

    @Command(desc = "", usage = "<prevention>")
    public void enabled(CommandContext context)
    {
        if (context.hasArg(0))
        {
            Prevention prevention = this.pm.getPrevention(context.getString(0));
            if (prevention != null)
            {
                if (prevention.isEnabled())
                {
                    context.sendTranslated("enabled");
                }
                else
                {
                    context.sendTranslated("disabled");
                }
            }
            else
            {
                context.sendTranslated("preventionNotFound");
            }
        }
        else
        {
            context.sendTranslated("noPrevention");
        }
    }

    @Command(desc = "", usage = "[-a]")
    public void list(ParameterizedContext context)
    {
        if (context.hasFlag("a"))
        {
            context.sendTranslated("registeredPreventions");
            context.sendMessage("");
            for (Prevention prevention : this.pm.getPreventions())
            {
                context.sendMessage(" - " + (prevention.isEnabled() ? ChatFormat.BRIGHT_GREEN : ChatFormat.RED) + prevention.getName());
            }
            context.sendMessage("");
        }
        else
        {
            context.sendTranslated("activePreventions");
            context.sendMessage("");
            int i = 0;
            for (Prevention prevention : this.pm.getPreventions())
            {
                if (prevention.isEnabled())
                {
                    ++i;
                    context.sendMessage(" - " + ChatFormat.BRIGHT_GREEN + prevention.getName());
                }
            }
            context.sendMessage("");
        }
    }

    @Command(desc = "", usage = "[player] <prevention>")
    public void can(CommandContext context)
    {
        User target = null;
        if (context.getSender() instanceof User)
        {
            target = (User)context.getSender();
        }
        Prevention prevention;
        if (context.hasArg(1))
        {
            target = context.getUser(0);
            prevention = this.pm.getPrevention(context.getString(1));
        }
        else if (context.hasArg(0) && target != null)
        {
            prevention = this.pm.getPrevention(context.getString(0));
        }
        else
        {
            context.sendTranslated("tooFewArguments");
            context.sendTranslated("see " + context.getLabels() + " help");
            return;
        }

        if (target != null)
        {
            if (prevention != null)
            {
                if (prevention.can(target))
                {
                    if (context.getSender() == target)
                    {
                        context.sendTranslated("you_ableToPass");
                    }
                    else
                    {
                        context.sendTranslated("other_ableToPass");
                    }
                }
                else
                {
                    if (context.getSender() == target)
                    {
                        context.sendTranslated("you_unableToPass");
                    }
                    else
                    {
                        context.sendTranslated("other_unableToPass");
                    }
                }
            }
            else
            {
                context.sendTranslated("preventionNotFound");
            }
        }
        else
        {
            context.sendTranslated("playerNotFound");
        }
    }

    @Command(desc = "", usage = "<prevention> <message>")
    public void setMessage(CommandContext context)
    {
        if (context.hasArg(0))
        {
            Prevention prevention = this.pm.getPrevention(context.getString(0));
            if (prevention != null)
            {
                String message = context.getString(1);
                prevention.setMessage(message);
                prevention.getConfig().set("message", message);
                prevention.saveConfig();

                context.sendTranslated("messageSet");
            }
            else
            {
                context.sendTranslated("preventionNotFound");
            }
        }
        else
        {
            context.sendTranslated("tooFewArguments");
        }
    }

    private void broadcastResetNotice(String message, CommandContext context)
    {
        if (context != null)
        {
            this.guests.getLog().info(message);
        }
        final CommandSender sender = context.getSender();
        for (User user : this.guests.getCore().getUserManager().getOnlineUsers())
        {
            if (sender != user && user.hasPermission(context.getCommand().getPermission()))
            {
                user.sendMessage(message);
            }
        }
    }
}
