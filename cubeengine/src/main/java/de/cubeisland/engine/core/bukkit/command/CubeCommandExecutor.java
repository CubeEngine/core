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
package de.cubeisland.engine.core.bukkit.command;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.command.AliasCommand;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.ContainerCommand.DelegatingContextFilter;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.HelpContext;
import de.cubeisland.engine.core.command.exception.CommandException;
import de.cubeisland.engine.core.command.exception.IncorrectUsageException;
import de.cubeisland.engine.core.command.exception.InvalidArgumentException;
import de.cubeisland.engine.core.command.exception.MissingParameterException;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;
import de.cubeisland.engine.core.command.sender.BlockCommandSender;
import de.cubeisland.engine.core.command.sender.WrappedCommandSender;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.formatter.MessageType;

import static de.cubeisland.engine.core.util.StringUtils.startsWithIgnoreCase;
import static de.cubeisland.engine.core.util.formatter.MessageType.CRITICAL;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;

public class CubeCommandExecutor implements CommandExecutor, TabCompleter
{
    private static final int TAB_LIMIT_THRESHOLD = 50;
    
    private final CubeCommand command;

    public CubeCommandExecutor(CubeCommand command)
    {
        assert command != null: "The command may not be null!";
        
        this.command = command;
    }

    public CubeCommand getCommand()
    {
        return command;
    }
    
    private static CommandContext toCommandContext(CubeCommand command, CommandSender sender, String label, String[] args, boolean tabComplete)
    {
        Stack<String> labels = new Stack<>();
        labels.push(label);
        
        if (args.length > 0 && !args[0].isEmpty())
        {
            while (args.length > 0)
            {
                if ("?".equals(args[0]))
                {
                    return new HelpContext(command, sender, labels, Arrays.copyOfRange(args, 1, args.length));
                }
                CubeCommand child = command.getChild(args[0]);
                if (child == null)
                {
                    break;
                }
                command = child;
                labels.push(args[0]);
                args = Arrays.copyOfRange(args, 1, args.length);
            }
        }
        
        if (command instanceof AliasCommand)
        {
            AliasCommand aliasCommand = (AliasCommand)command;
            String[] prefix = aliasCommand.getPrefix();
            String[] suffix = aliasCommand.getSuffix();

            String[] newArgs = new String[prefix.length + args.length + suffix.length];
            System.arraycopy(prefix, 0, newArgs, 0, prefix.length);
            System.arraycopy(args, 0, newArgs, prefix.length, args.length);
            System.arraycopy(suffix, 0, newArgs, prefix.length + args.length, suffix.length);

            args = newArgs;
        }

        CommandContext ctx;
        if (tabComplete)
        {
             ctx = command.getContextFactory().tabCompleteParse(command, sender, labels, args);
        }
        else
        {
            ctx = command.getContextFactory().parse(command, sender, labels, args);
        }
        if (command instanceof ContainerCommand)
        {
            if (!(ctx.getArgCount() == 1 && "".equals(ctx.getString(0))))
            {
                DelegatingContextFilter delegation = ((ContainerCommand)command).getDelegation();
                if (delegation != null)
                {
                    String child = delegation.delegateTo(ctx);
                    if (child != null)
                    {
                        CubeCommand target = command.getChild(child);
                        if (target != null)
                        {

                            if (tabComplete)
                            {
                                return target.getContextFactory().tabCompleteParse(target, sender, labels, args);
                            }
                            else
                            {
                                return target.getContextFactory().parse(target, sender, labels, args);
                            }
                        }
                        command.getModule().getLog().warn("Child delegation failed: child '{}' not found!", child);
                    }
                }
            }
        }
        return ctx;
    }

    @Override
    public boolean onCommand(final org.bukkit.command.CommandSender bukkitSender, final Command bukkitCommand, final String label, final String[] args)
    {
        final CommandSender sender = wrapSender(this.command.getModule().getCore(), bukkitSender);

        final CommandContext context;
        try
        {
            context = toCommandContext(this.command, sender, label, args, false);
        }
        catch (CommandException e)
        {
            this.handleCommandException(null, sender, e);
            return true;
        }

        try
        {
            if (context instanceof HelpContext)
            {
                context.getCommand().help((HelpContext)context);
                return true;
            }
            if (this.command.isAsynchronous())
            {
                context.getCore().getTaskManager().getThreadFactory().newThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        run0(context);
                    }
                }).start();
            }
            else
            {
                this.run0(context);
            }
        }
        catch (CommandException e)
        {
            this.handleCommandException(context, sender, e);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(final org.bukkit.command.CommandSender bukkitSender, final Command bukkitCommand, final String label, final String[] args)
    {
        final Core core = this.command.getModule().getCore();
        final CommandSender sender = wrapSender(core, bukkitSender);

        final CommandContext context;
        try
        {
            context = toCommandContext(this.command, sender, label, args, true);
            if (!context.getCommand().isAuthorized(sender))
            {
                return Collections.emptyList();
            }
        }
        catch (CommandException e)
        {
            this.handleCommandException(null, sender, e);
            return null;
        }
        try
        {
            List<String> result = this.completeChild(context);
            if (result == null)
            {
                result = context.getCommand().tabComplete(context);
            }

            if (result != null)
            {
                final int max = core.getConfiguration().commands.maxTabCompleteOffers;
                if (result.size() > max && false) // TODO remove false
                {
                    if (StringUtils.implode(", ", result).length() < TAB_LIMIT_THRESHOLD)
                    {
                        return result;
                    }
                    result = result.subList(0, max);
                }
                return result;
            }
        }
        catch (Exception e)
        {
            this.handleCommandException(context, sender, e);
        }
        
        return null;
    }
    
    protected final List<String> completeChild(CommandContext context)
    {
        CubeCommand command = context.getCommand();
        if (command.hasChildren() && context.getArgCount() == 1)
        {
            List<String> actions = new ArrayList<>();
            String token = context.getString(0).toLowerCase(Locale.ENGLISH);

            CommandSender sender = context.getSender();
            Set<CubeCommand> names = command.getChildren();
            for (CubeCommand child : names)
            {
                if (startsWithIgnoreCase(child.getName(), token) && child.isAuthorized(sender))
                {
                    actions.add(child.getName());
                }
            }
            Collections.sort(actions, String.CASE_INSENSITIVE_ORDER);

            return actions;
        }
        return null;
    }


    private static CommandSender wrapSender(Core core, org.bukkit.command.CommandSender bukkitSender)
    {
        if (bukkitSender instanceof CommandSender)
        {
            return (CommandSender)bukkitSender;
        }
        else if (bukkitSender instanceof Player)
        {
            return core.getUserManager().getExactUser(bukkitSender.getName());
        }
        else if (bukkitSender instanceof org.bukkit.command.ConsoleCommandSender)
        {
            return core.getCommandManager().getConsoleSender();
        }
        else if (bukkitSender instanceof org.bukkit.command.BlockCommandSender)
        {
            return new BlockCommandSender(core, (org.bukkit.command.BlockCommandSender)bukkitSender);
        }
        else
        {
            return new WrappedCommandSender(core, bukkitSender);
        }
    }

    private void run0(CommandContext ctx)
    {
        try
        {
            ctx.getCommand().checkContext(ctx);
            CommandResult result = ctx.getCommand().run(ctx);
            if (result != null)
            {
                result.show(ctx);
            }
        }
        catch (Exception e)
        {
            handleCommandException(ctx, e);
        }
    }

    private void handleCommandException(final CommandContext context, Throwable t)
    {
        this.handleCommandException(context, context.getSender(), t);
    }

    private void handleCommandException(final CommandContext context, final CommandSender sender, Throwable t)
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
            sender.sendTranslated(NEGATIVE, "The parameter {name#parameter} is missing!", t.getMessage());
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
                    usage = this.command.getUsage(sender);
                }
                sender.sendTranslated(MessageType.NEUTRAL, "Proper usage: {message}", usage);
            }
        }
        else if (t instanceof InvalidArgumentException)
        {
            InvalidArgumentException e = (InvalidArgumentException)t;
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
        else
        {
            sender.sendTranslated(CRITICAL, "An unknown error occurred while executing this command!");
            sender.sendTranslated(CRITICAL, "Please report this error to an administrator.");
            this.command.getModule().getLog().debug(t, t.getLocalizedMessage());
        }
    }
}
