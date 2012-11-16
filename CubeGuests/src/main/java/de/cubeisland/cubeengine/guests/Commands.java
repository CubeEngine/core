package de.cubeisland.cubeengine.guests;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import de.cubeisland.cubeengine.guests.prevention.Prevention;
import de.cubeisland.cubeengine.guests.prevention.PreventionManager;
import gnu.trove.set.hash.THashSet;
import java.util.Set;
import org.bukkit.command.CommandSender;

import static de.cubeisland.cubeengine.core.i18n.I18n._;

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
        if (context.hasIndexed(0))
        {
            final Prevention prevention = this.pm.getPrevention(context.getString(0));
            if (prevention != null)
            {
                this.pm.disablePrevention(prevention);
                prevention.reloadConfig();
                this.pm.enablePrevention(prevention);
                context.sendMessage("guests", "preventionReloaded", prevention.getName());
            }
            else
            {
                context.sendMessage("guests", "preventionNotFound");
            }
        }
        else
        {
            // TODO reload module
            // this.guests.getModuleManager().reloadModule(this.guests);

            context.sendMessage("guests", "reloaded", guests.getName());
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

            context.sendMessage("guests", "configsResetted");
        }
        else
        {
            resetRequest.add(sender);
            context.sendMessage("guests", "resetRequested");
            if (sender instanceof User)
            {
                this.broadcastResetNotice(_("guests", "playerRequestedReset", sender.getName()), context);
            }
            else
            {
                this.broadcastResetNotice(_("guests", "consoleRequestedReset"), context);
            }
        }
    }

    @Command(desc = "", usage = "<prevention|*> [-t]")
    public void enable(CommandContext context)
    {
        if (context.hasIndexed(0))
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

                context.sendMessage("guests", "preventionsEnabled");
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
                            context.sendMessage("guests", "preventionEnabled");
                            if (!temporary)
                            {
                                prevention.getConfig().set("enable", true);
                                prevention.saveConfig();
                            }
                        }
                        else
                        {
                            context.sendMessage("guests", "somethingFailed");
                        }
                    }
                    else
                    {
                        context.sendMessage("guests", "alreadyEnabled");
                    }
                }
                else
                {
                    context.sendMessage("guests", "preventionNotFound");
                }
            }
        }
        else
        {
            context.sendMessage("guests", "noPrevention");
        }
    }

    @Command(desc = "", usage = "<prevention> [-t]")
    public void disable(CommandContext context)
    {
        if (context.hasIndexed(0))
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

                context.sendMessage("guests", "preventionsDisabled");
            }
            else
            {
                Prevention prevention = this.pm.getPrevention(context.getString(0));
                if (prevention != null)
                {
                    if (prevention.isEnabled())
                    {
                        this.pm.disablePrevention(prevention);
                        context.sendMessage("guests", "preventionDisabled");
                        if (!temporary)
                        {
                            prevention.getConfig().set("enable", false);
                            prevention.saveConfig();
                        }
                    }
                    else
                    {
                        context.sendMessage("guests", "alreadyDisabled");
                    }
                }
                else
                {
                    context.sendMessage("guests", "preventionNotFound");
                }
            }
        }
        else
        {
            context.sendMessage("guests", "noPrevention");
        }
    }

    @Command(desc = "", usage = "<prevention>")
    public void enabled(CommandContext context)
    {
        if (context.hasIndexed(0))
        {
            Prevention prevention = this.pm.getPrevention(context.getString(0));
            if (prevention != null)
            {
                if (prevention.isEnabled())
                {
                    context.sendMessage("guests", "enabled");
                }
                else
                {
                    context.sendMessage("guests", "disabled");
                }
            }
            else
            {
                context.sendMessage("guests", "preventionNotFound");
            }
        }
        else
        {
            context.sendMessage("guests", "noPrevention");
        }
    }

    @Command(desc = "", usage = "[-a]")
    public void list(CommandContext context)
    {
        if (context.hasFlag("a"))
        {
            context.sendMessage("guests", "registeredPreventions");
            context.sendMessage("");
            for (Prevention prevention : this.pm.getPreventions())
            {
                context.sendMessage(" - " + (prevention.isEnabled() ? ChatFormat.BRIGHT_GREEN : ChatFormat.RED) + prevention.getName());
            }
            context.sendMessage("");
        }
        else
        {
            context.sendMessage("guests", "activePreventions");
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
        User target = context.getSenderAsUser();
        Prevention prevention;
        if (context.hasIndexed(1))
        {
            target = context.getUser(0);
            prevention = this.pm.getPrevention(context.getString(1));
        }
        else if (context.hasIndexed(0) && target != null)
        {
            prevention = this.pm.getPrevention(context.getString(0));
        }
        else
        {
            context.sendMessage("guests", "tooFewArguments");
            context.sendMessage("guests", "see " + context.getLabels() + " help");
            return;
        }

        if (target != null)
        {
            if (prevention != null)
            {
                if (prevention.can(target))
                {
                    if (context.getSenderAsUser() == target)
                    {
                        context.sendMessage("guests", "you_ableToPass");
                    }
                    else
                    {
                        context.sendMessage("guests", "other_ableToPass");
                    }
                }
                else
                {
                    if (context.getSenderAsUser() == target)
                    {
                        context.sendMessage("guests", "you_unableToPass");
                    }
                    else
                    {
                        context.sendMessage("guests", "other_unableToPass");
                    }
                }
            }
            else
            {
                context.sendMessage("guests", "preventionNotFound");
            }
        }
        else
        {
            context.sendMessage("guests", "playerNotFound");
        }
    }

    @Command(desc = "", usage = "<prevention> <message>")
    public void setMessage(CommandContext context)
    {
        if (context.hasIndexed(0))
        {
            Prevention prevention = this.pm.getPrevention(context.getString(0));
            if (prevention != null)
            {
                String message = context.getString(1);
                prevention.setMessage(message);
                prevention.getConfig().set("message", message);
                prevention.saveConfig();

                context.sendMessage("guests", "messageSet");
            }
            else
            {
                context.sendMessage("guests", "preventionNotFound");
            }
        }
        else
        {
            context.sendMessage("guests", "tooFewArguments");
        }
    }

    private void broadcastResetNotice(String message, CommandContext context)
    {
        if (context != null)
        {
            this.guests.getLogger().log(LogLevel.INFO, message);
        }
        final User sender = context.getSenderAsUser();
        for (User user : this.guests.getCore().getUserManager().getOnlineUsers())
        {
            if (sender != user && user.hasPermission(context.getCommand().getPermission()))
            {
                user.sendMessage(message);
            }
        }
    }
}
