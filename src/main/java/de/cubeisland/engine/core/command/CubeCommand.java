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
package de.cubeisland.engine.core.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.command.BaseCommand;
import de.cubeisland.engine.command.Completer;
import de.cubeisland.engine.command.context.CommandContext;
import de.cubeisland.engine.command.context.ContextParser;
import de.cubeisland.engine.command.context.CtxDescriptor;
import de.cubeisland.engine.command.context.parameter.FlagParameter;
import de.cubeisland.engine.command.context.parameter.IndexedParameter;
import de.cubeisland.engine.command.context.parameter.NamedParameter;
import de.cubeisland.engine.command.exception.CommandException;
import de.cubeisland.engine.core.command.context.CubeContext;
import de.cubeisland.engine.core.command.context.CubeContextFactory;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;
import de.cubeisland.engine.core.command.parameterized.PermissibleFlag;
import de.cubeisland.engine.core.command.parameterized.PermissibleNamedParameter;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;

import static de.cubeisland.engine.command.context.ContextParser.Type.*;
import static de.cubeisland.engine.core.command.HelpCommand.newHelpCommand;
import static de.cubeisland.engine.core.util.StringUtils.implode;
import static de.cubeisland.engine.core.util.StringUtils.startsWithIgnoreCase;

/**
 * This class is the base for all of our commands
 * it implements the execute() method which provides error handling and calls the
 * run() method which should be implemented by the extending classes
 */
public class CubeCommand extends BaseCommand<CubeContext, CubeContextFactory, CubeCommand>
{
    Module module;
    Permission permission;
    boolean checkperm;
    boolean loggable = true;
    boolean asynchronous = false;

    private String label;

    private boolean permRegistered = false;

    public CubeCommand()
    {
        this.addHelp();
    }

    private static String replaceSemiOptionalArgs(CommandSender sender, String usage)
    {
        if (sender instanceof User)
        {
            return usage.replace('(', '[').replace(')', ']');
        }
        else
        {
            return usage.replace('(', '<').replace(')', '>');
        }
    }

    protected void addHelp()
    {
        if (!HelpCommand.class.isAssignableFrom(this.getClass()))
        {
            this.addChild(newHelpCommand(this));
        }
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label == null ? this.getName() : label;
    }

    public boolean isAuthorized(CommandSender sender)
    {
        return this.permission == null || sender == null || this.permission.isAuthorized(sender);
    }

    public boolean isCheckperm()
    {
        return checkperm;
    }

    public Permission getPermission()
    {
        return permission;
    }

    public void registerPermission()
    {
        if (this.permission == null)
        {
            return;
        }
        if (permRegistered)
        {
            this.module.getCore().getPermissionManager().removePermission(module, this.permission);
        }
        Stack<String> cmds = new Stack<>();
        BaseCommand cmd = this;
        while ((cmd = cmd.getParent()) != null)
        {
            cmds.push(cmd.getName());
        }

        Permission cmdPerm = this.getModule().getBasePermission().childWildcard("command");

        while (!cmds.isEmpty())
        {
            cmdPerm = cmdPerm.childWildcard(cmds.pop());
        }
        this.permission.setParent(cmdPerm);
        this.module.getCore().getPermissionManager().registerPermission(module, this.permission);
        this.permRegistered = true;
    }

    public boolean isAsynchronous()
    {
        return this.asynchronous;
    }

    public boolean isLoggable()
    {
        return this.loggable;
    }

    protected void registerAlias(String[] names, String[] parents)
    {
        this.registerAlias(names, parents, "", "");
    }

    protected void registerAlias(String[] names, String[] parents, String prefix, String suffix)
    {
        if (names.length == 0)
        {
            throw new IllegalArgumentException("You have to specify at least 1 name!");
        }
        Set<String> aliases = Collections.emptySet();
        if (names.length > 1)
        {
            aliases = new HashSet<>(names.length - 1);
            aliases.addAll(Arrays.asList(names).subList(1, names.length));
        }
        this.getModule().getCore().getCommandManager().registerCommand(new AliasCommand(this, names[0], aliases, prefix,
                                                                                        suffix), parents);
    }

    /**
     * This method implodes the path of this command, so the name of the command and the name of every parent
     *
     * @param delimiter the delimiter
     *
     * @return the imploded path
     */
    protected final String implodeCommandParentNames(String delimiter)
    {
        LinkedList<String> cmds = new LinkedList<>();
        BaseCommand cmd = this;
        do
        {
            cmds.addFirst(cmd.getName());
        }
        while ((cmd = cmd.getParent()) != null);

        return implode(delimiter, cmds);
    }

    public String getUsage()
    {
        return "/" + this.implodeCommandParentNames(" ") + " " + this.getUsage(
            this.module.getCore().getConfiguration().defaultLocale, null);
    }

    public final String getUsage(Locale locale, Permissible permissible)
    {
        return UsageGenerator.generateUsage(this.getContextFactory().descriptor(), locale, permissible);
    }

    /**
     * This overload returns the usage translated for the given CommandSender
     *
     * @param sender the command sender
     *
     * @return the usage string
     */
    public String getUsage(CommandSender sender)
    {
        String usage = this.getUsage(sender.getLocale(), sender);
        return (sender instanceof User ? "/" : "") + this.implodeCommandParentNames(" ") + ' '
            + replaceSemiOptionalArgs(sender, usage);
    }

    /**
     * This overload returns the usage translated for the given CubeContext
     * using the correct labels
     *
     * @param context the command context
     *
     * @return the usage string
     */

    public String getUsage(CubeContext context)
    {
        return this.getUsage(context.getSource(), context.getLabels());
    }

    /**
     * This overload returns the usage translated for the given CommandSender
     * using the correct labels
     *
     * @param sender       the command sender
     * @param parentLabels a list of labels
     *
     * @return the usage string
     */
    public String getUsage(CommandSender sender, List<String> parentLabels)
    {
        String usage = this.getUsage(sender.getLocale(), sender);
        if ("?".equals(parentLabels.get(parentLabels.size() - 1)))
        {
            parentLabels.remove(parentLabels.size() - 1);
        }
        return (sender instanceof User ? "/" : "") + implode(" ", parentLabels) + ' ' + usage;
    }

    public List<String> tabComplete(CommandContext context)
    {
        if (context.last == ContextParser.Type.NOTHING)
        {
            return null;
        }
        final CtxDescriptor descriptor = this.getContextFactory().descriptor();
        if (context.last == PARAM_VALUE)
        {
            return tabCompleteParamValue(context, descriptor);
        }
        List<String> result = new ArrayList<>();
        List<String> args = context.getRawIndexed();
        String last = args.get(args.size() - 1);
        if (context.last == FLAG_OR_INDEXED)
        {
            tabCompleteFlags(context, descriptor, result, last);
            tabCompleteIndexed(context, descriptor, result, args.size() - 1, last);
        }
        else if (context.last == INDEXED_OR_PARAM)
        {
            tabCompleteIndexed(context, descriptor, result, args.size() - 1, last);
            tabCompleteParam(context, descriptor, result, last);
        }
        else if (context.last == ANY)
        {
            tabCompleteIndexed(context, descriptor, result, args.size() - 1, last);
            tabCompleteParam(context, descriptor, result, last);
            tabCompleteFlags(context, descriptor, result, last);
        }
        return result;
    }

    private List<String> tabCompleteParamValue(CommandContext context, CtxDescriptor descriptor)
    {
        Iterator<Entry<String, String>> iterator = context.getRawNamed().entrySet().iterator();
        Entry<String, String> lastParameter;
        do
        {
            lastParameter = iterator.next();
        }
        while (iterator.hasNext());
        Completer completer = descriptor.getNamed(lastParameter.getKey()).getCompleter();
        if (completer != null)
        {
            return completer.complete(context, lastParameter.getValue());
        }
        return Collections.emptyList();
    }

    private void tabCompleteParam(CommandContext context, CtxDescriptor descriptor, List<String> result, String last)
    {
        for (NamedParameter parameter : descriptor.getNamedGroups().listAll())
        {
            if (!context.hasNamed(parameter.getName()))
            {
                if (startsWithIgnoreCase(parameter.getName(), last))
                {
                    result.add(parameter.getName());
                }
                if (!last.isEmpty())
                {
                    for (String alias : parameter.getAliases())
                    {
                        if (alias.length() > 2 && startsWithIgnoreCase(alias, last))
                        {
                            result.add(alias);
                        }
                    }
                }
            }
        }
    }

    private void tabCompleteIndexed(CommandContext context, CtxDescriptor descriptor, List<String> result, int index,
                                    String last)
    {
        IndexedParameter indexed = descriptor.getIndexed(index);
        if (indexed != null)
        {
            Completer indexedCompleter = indexed.getCompleter();
            if (indexedCompleter != null)
            {
                result.addAll(indexedCompleter.complete(context, last));
            }
        }
    }

    private void tabCompleteFlags(CommandContext context, CtxDescriptor descriptor, List<String> result, String last)
    {
        if (!last.isEmpty())
        {
            last = last.substring(1);
        }
        for (FlagParameter commandFlag : descriptor.getFlags())
        {
            if (!context.hasFlag(commandFlag.getName()) && startsWithIgnoreCase(commandFlag.getLongName(), last))
            {
                result.add("-" + commandFlag.getLongName());
            }
        }
    }

    /**
     * Returns the module this command was registered by
     *
     * @return a module
     */
    public Module getModule()
    {
        return this.module;
    }

    public void checkContext(CubeContext ctx) throws CommandException
    {
        if (ctx.getCommand().isCheckperm() && !ctx.getCommand().isAuthorized(ctx.getSource()))
        {
            throw new PermissionDeniedException(ctx.getCommand().getPermission());
        }
        super.checkContext(ctx); // After general perm check -> check bounds etc.
        CtxDescriptor descriptor = ctx.getCommand().getContextFactory().descriptor();
        // TODO also check perm for indexed Parameters
        for (NamedParameter named : descriptor.getNamedGroups().listAll())
        {
            if (named instanceof PermissibleNamedParameter && ctx.hasNamed(named.getName()) &&
                !((PermissibleNamedParameter)named).checkPermission(ctx.getSource()))
            {
                throw new PermissionDeniedException(((PermissibleNamedParameter)named).getPermission());
            }
        }

        for (FlagParameter flag : descriptor.getFlags())
        {
            if (flag instanceof PermissibleFlag && ctx.hasFlag(flag.getName())
                && !((PermissibleFlag)flag).checkPermission(ctx.getSource()))
            {
                throw new PermissionDeniedException(((PermissibleFlag)flag).getPermission());
            }
        }
    }
}
