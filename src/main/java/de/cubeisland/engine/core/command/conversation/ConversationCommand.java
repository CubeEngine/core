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
package de.cubeisland.engine.core.command.conversation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

import de.cubeisland.engine.command.context.parameter.FlagParameter;
import de.cubeisland.engine.command.context.parameter.NamedParameter;
import de.cubeisland.engine.command.exception.IncorrectArgumentException;
import de.cubeisland.engine.command.exception.IncorrectUsageException;
import de.cubeisland.engine.command.exception.MissingParameterException;
import de.cubeisland.engine.command.exception.ReaderException;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.command.result.CommandResult;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.HelpCommand;
import de.cubeisland.engine.core.command.context.CubeContext;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.formatter.MessageType;
import gnu.trove.set.hash.TLongHashSet;

import static de.cubeisland.engine.core.util.formatter.MessageType.*;

public abstract class ConversationCommand extends CubeCommand implements Listener
{
    private final TLongHashSet usersInMode = new TLongHashSet();

    protected ConversationCommand(Module module, ConversationContextFactory contextFactory)
    {
        super(module, "", "", contextFactory, null, false);
        module.getCore().getEventManager().registerListener(module, this);
    }

    @Override
    public Module getModule()
    {
        return super.getModule();
    }

    public boolean hasUser(User user)
    {
        return usersInMode.contains(user.getId());
    }

    @EventHandler
    public void onChatHandler(AsyncPlayerChatEvent event)
    {
        User user = this.getModule().getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
        if (this.hasUser(user))
        {
            user.sendMessage(ChatFormat.PURPLE + "[" + ChatFormat.WHITE + "ChatCommand" + ChatFormat.PURPLE + "] " + ChatFormat.WHITE + event.getMessage());
            Stack<String> labels = new Stack<>();
            labels.push(this.getLabel());
            CubeContext context = null;
            try
            {
                context = this.getContextFactory().parse(this, user, labels, StringUtils.explode(" ", event.getMessage()));
                this.getContextFactory().readContext(context, user.getLocale());
                if (event.getMessage().startsWith("?"))
                {
                    this.getChild("?").run(context);
                }
                else
                {
                    this.run(context);
                }
            }
            catch (Exception e)
            {
                this.handleCommandException(context, user, e);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTabComplete(PlayerChatTabCompleteEvent event)
    {
        User user = this.getModule().getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
        if (this.hasUser(user))
        {
            event.getTabCompletions().clear();

            Stack<String> labels = new Stack<>();
            labels.push(this.getLabel());
            CubeContext context = this.getContextFactory().parse(this, user, labels, StringUtils.explode(" ", event.getChatMessage()));
            this.getContextFactory().readContext(context, user.getLocale());
            event.getTabCompletions().addAll(this.tabComplete(context));
        }
    }

    @Override
    public List<String> tabComplete(CubeContext context)
    {
        List<String> list = new ArrayList<>();
        Set<String> flags = new HashSet<>();
        Set<String> params = new HashSet<>();
        for (FlagParameter flag : this.getContextFactory().descriptor().getFlags())
        {
            flags.add(flag.getLongName().toLowerCase());
        }
        for (NamedParameter param : this.getContextFactory().descriptor().getNamedGroups().listAll())
        {
            params.add(param.getName().toLowerCase());
        }
        List<Object> args = context.getIndexed();
        if (args.isEmpty())
        {
            list.addAll(flags);
            list.addAll(params);
        }
        else
        {
            final int argc = args.size();
            String lastArg = args.get(argc - 1).toString().toLowerCase();
            String beforeLastArg = argc - 2 >= 0 ? args.get(argc - 2).toString() : null;
            if (lastArg.isEmpty())
            {
                //check for named
                if (beforeLastArg != null && params.contains(beforeLastArg.toLowerCase()))
                {
                    return this.getContextFactory().descriptor().getNamed(beforeLastArg).getCompleter().complete(context, lastArg);
                }
                else
                {
                    list.addAll(flags);
                    list.addAll(params);
                }
            }
            else
            {
                //check for named
                if (beforeLastArg != null && params.contains(beforeLastArg.toLowerCase()))
                {
                    return this.getContextFactory().descriptor().getNamed(beforeLastArg).getCompleter().complete(context, lastArg);
                }
                else // check starting
                {
                    for (String flag : flags)
                    {
                        if (flag.startsWith(lastArg))
                        {
                            list.add(flag);
                        }
                    }
                    for (String param : params)
                    {
                        if (param.startsWith(lastArg))
                        {
                            list.add(param);
                        }
                    }
                }
            }
        }
        return list;
    }

    @Override
    public ConversationContextFactory getContextFactory()
    {
        return (ConversationContextFactory)super.getContextFactory();
    }

    /**
     * Adds a user to this chatcommands internal list
     *
     * @param user
     */
    public boolean addUser(User user)
    {
        return this.usersInMode.add(user.getId());
    }

    /**
     * Removes a user from this chatcommands internal list
     *
     * @param user
     */
    public void removeUser(User user)
    {
        this.usersInMode.remove(user.getId());
    }

    @Override
    protected void addHelp()
    {
        this.addChild(new ConversationHelpCommand(this));
    }

    public static class ConversationHelpCommand extends HelpCommand
    {
        public ConversationHelpCommand(CubeCommand target)
        {
            super(target);
        }

        @Override
        public CommandResult run(CubeContext context)
        {
            context.sendTranslated(NEUTRAL, "Flags:");
            Set<String> flags = new HashSet<>();
            for (FlagParameter flag : target.getContextFactory().descriptor().getFlags())
            {
                flags.add(flag.getLongName().toLowerCase());
            }
            context.sendMessage("    " + StringUtils.implode(ChatFormat.GREY + ", " + ChatFormat.WHITE, flags));
            context.sendTranslated(NEUTRAL, "Parameters:");
            Set<String> params  = new HashSet<>();
            for (NamedParameter param : target.getContextFactory().descriptor().getNamedGroups().listAll())
            {
                params.add(param.getName().toLowerCase());
            }
            context.sendMessage("    " + StringUtils.implode(ChatFormat.GREY + ", " + ChatFormat.WHITE, params));
            return null;
        }
    }

    // TODO REMOVE DUPLICATED CODE (CubeCommandExecuter)
    private void handleCommandException(final CubeContext context, final CommandSender sender, Throwable t)
    {
        if (!CubeEngine.isMainThread())
        {
            final Throwable tmp = t;
            sender.getCore().getTaskManager().callSync(new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    handleCommandException(context, sender, tmp);
                    return null;
                }
            });
            return;
        }
        if (t instanceof InvocationTargetException || t instanceof ExecutionException)
        {
            t = t.getCause();
        }
        if (t instanceof MissingParameterException)
        {
            if (t.getMessage().isEmpty())
            {
                sender.sendTranslated(NEGATIVE, "The parameter {name#parameter} is missing!", ((MissingParameterException)t).getParamName());
            }
            else
            {
                sender.sendMessage(t.getMessage());
            }
        }
        else if (t instanceof IncorrectUsageException)
        {
            IncorrectUsageException e = (IncorrectUsageException)t;
            if (e.getMessage() != null)
            {
                sender.sendMessage(t.getMessage());
            }
            else
            {
                sender.sendTranslated(NEGATIVE, "That seems wrong...");
            }
            if (e.getDisplayUsage())
            {
                final String usage;
                if (context != null)
                {
                    usage = context.getCommand().getUsage(context);
                }
                else
                {
                    // TODO can this happen at all?
                    usage = this.getUsage(sender);
                }
                sender.sendTranslated(MessageType.NEUTRAL, "Proper usage: {message}", usage);
            }
        }
        else if (t instanceof ReaderException)
        {
            ReaderException e = (ReaderException)t;
            if (e.getMessage() != null)
            {
                sender.sendMessage(t.getMessage());
            }
            else
            {
                sender.sendTranslated(NEGATIVE, "Invalid Argument...");
            }
        }
        else if (t instanceof PermissionDeniedException)
        {
            PermissionDeniedException e = (PermissionDeniedException)t;
            if (e.getMessage() != null)
            {
                sender.sendMessage(e.getMessage());
            }
            else
            {
                sender.sendTranslated(NEGATIVE, "You're not allowed to do this!");
                sender.sendTranslated(NEGATIVE, "Contact an administrator if you think this is a mistake!");
            }
            sender.sendTranslated(NEGATIVE, "Missing permission: {name}", e.getPermission());
        }
        else if (t instanceof IncorrectArgumentException)
        {
            if (((IncorrectArgumentException)t).isNamedArgument())
            {
                sender.sendTranslated(NEGATIVE, "Invalid Argument for {input#named}: {input#reason}", ((IncorrectArgumentException)t).getName(), t.getCause().getMessage());
            }
            else
            {
                sender.sendTranslated(NEGATIVE, "Invalid Argument at {integer#index}: {input#reason}", ((IncorrectArgumentException)t).getIndex(), t.getCause().getMessage());
            }
        }
        else
        {
            sender.sendTranslated(CRITICAL, "An unknown error occurred while executing this command!");
            sender.sendTranslated(CRITICAL, "Please report this error to an administrator.");
            this.getModule().getLog().debug(t, t.getLocalizedMessage());
        }
    }
}
