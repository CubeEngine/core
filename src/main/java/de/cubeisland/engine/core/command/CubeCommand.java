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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.command.BaseCommand;
import de.cubeisland.engine.command.Completer;
import de.cubeisland.engine.command.context.ArgBounds;
import de.cubeisland.engine.command.context.ContextParser;
import de.cubeisland.engine.command.context.CtxDescriptor;
import de.cubeisland.engine.command.context.Flag;
import de.cubeisland.engine.command.context.IndexedParameter;
import de.cubeisland.engine.command.context.NamedParameter;
import de.cubeisland.engine.command.exception.CommandException;
import de.cubeisland.engine.command.exception.IncorrectUsageException;
import de.cubeisland.engine.command.exception.MissingParameterException;
import de.cubeisland.engine.core.command.context.CubeContext;
import de.cubeisland.engine.core.command.context.CubeContextFactory;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;
import de.cubeisland.engine.core.command.exception.TooFewArgumentsException;
import de.cubeisland.engine.core.command.exception.TooManyArgumentsException;
import de.cubeisland.engine.core.command.parameterized.CommandFlag;
import de.cubeisland.engine.core.command.parameterized.CommandParameterNamed;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;

import static de.cubeisland.engine.command.context.ContextParser.Type.*;
import static de.cubeisland.engine.core.contract.Contract.expectNotNull;
import static de.cubeisland.engine.core.permission.PermDefault.OP;
import static de.cubeisland.engine.core.util.StringUtils.implode;
import static de.cubeisland.engine.core.util.StringUtils.startsWithIgnoreCase;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static java.util.Locale.ENGLISH;

/**
 * This class is the base for all of our commands
 * it implements the execute() method which provides error handling and calls the
 * run() method which should be implemented by the extending classes
 */
public abstract class CubeCommand extends BaseCommand<CubeCommand>
{
    final Module module;
    private String label;
    private final Set<String> aliases;

    private final Map<String, CubeCommand> children;
    protected final List<String> childrenAliases;
    private boolean loggable;
    private boolean asynchronous = false;
    private final Permission permission;

    private boolean permRegistered = false;

    private String onlyIngame = null;
    private boolean checkperm;

    public CubeCommand(Module module, String name, String description, CubeContextFactory contextFactory, Permission permission, boolean checkperm)
    {
        super(name, description, contextFactory);
        this.checkperm = checkperm;
        if ("?".equals(name) && !HelpCommand.class.isAssignableFrom(this.getClass()))
        {
            throw new IllegalArgumentException("Invalid command name: " + name);
        }
        this.module = module;
        this.aliases = new HashSet<>();

        this.children = new HashMap<>();
        this.childrenAliases = new ArrayList<>();
        this.loggable = true;
        this.permission = permission;

        if (!HelpCommand.class.isAssignableFrom(this.getClass()))
        {
            this.addHelp();
        }
    }

    protected void addHelp()
    {
        this.addChild(new HelpCommand(this));
    }

    public CubeCommand(Module module, String name, String description, CubeContextFactory cFactory)
    {
        this(module, name, description, cFactory, Permission.detachedPermission(name, OP), true);
    }

    public String getName()
    {
        return name;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label == null ? this.name : label;
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
        CubeCommand cmd = this;
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

    public void setAsynchronous(boolean asynchronous)
    {
        this.asynchronous = asynchronous;
    }

    public void setLoggable(boolean state)
    {
        this.loggable = state;
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
        this.getModule().getCore().getCommandManager().registerCommand(new AliasCommand(this, names[0], aliases, prefix, suffix), parents);
    }



    /**
     * This method implodes the path of this command, so the name of the command and the name of every parent
     *
     * @param delimiter the delimiter
     * @return the imploded path
     */
    protected final String implodeCommandParentNames(String delimiter)
    {
        LinkedList<String> cmds = new LinkedList<>();
        CubeCommand cmd = this;
        do
        {
            cmds.addFirst(cmd.getName());
        }
        while ((cmd = cmd.getParent()) != null);

        return implode(delimiter, cmds);
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

    public CubeCommand setAliases(Set<String> aliases)
    {
        synchronized (this.aliases)
        {
            this.aliases.clear();
            this.aliases.addAll(aliases);
        }
        return this;
    }
    
    public Set<String> getAliases()
    {
        synchronized (this.aliases)
        {
            return new HashSet<>(this.aliases);
        }
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
     * @return the usage string
     */
    public String getUsage(CommandSender sender)
    {
        String usage = this.getUsage(sender.getLocale(), sender);
        return (sender instanceof User ? "/" : "") + this.implodeCommandParentNames(" ") + ' ' + replaceSemiOptionalArgs(sender, usage);
    }

        /**
         * This overload returns the usage translated for the given CubeContext
         * using the correct labels
         *
         * @param context the command context
         * @return the usage string
         */

    public String getUsage(CubeContext context)
    {
        return this.getUsage(context.getSender(), context.getLabels());
    }

    /**
     * This overload returns the usage translated for the given CommandSender
     * using the correct labels
     *
     * @param sender       the command sender
     * @param parentLabels a list of labels
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

    /**
     * Returns a child command by name without typo correction
     *
     * @param name the child name
     * @return the child or null if not found
     */
    public CubeCommand getChild(String name)
    {
        return name == null ? null : this.children.get(name.toLowerCase(ENGLISH));
    }

    /**
     * Adds a child to this command
     *
     * @param command the command to add
     */
    public void addChild(CubeCommand command)
    {
        expectNotNull(command, "The command must not be null!");

        if (this == command)
        {
            throw new IllegalArgumentException("You can't register a command as a child of itself!");
        }

        if (command.getParent() != null)
        {
            throw new IllegalArgumentException("The given command is already registered! Use aliases instead!");
        }

        this.children.put(command.getName(), command);
        command.parent = this;
        for (String alias : command.getAliases())
        {
            alias = alias.toLowerCase(ENGLISH);
            this.children.put(alias, command);
            this.childrenAliases.add(alias);
        }
    }

    /**
     * Checks whether this command has a child with the given name
     *
     * @param name the name to check for
     * @return true if a matching command was found
     */
    public boolean hasChild(String name)
    {
        return name != null && this.children.containsKey(name.toLowerCase());
    }

    /**
     * Checks whether this command has children
     *
     * @return true if that is the case
     */
    public boolean hasChildren()
    {
        return !this.children.isEmpty();
    }

    /**
     * Returns a Set of all children
     *
     * @return a Set of children
     */
    public Set<CubeCommand> getChildren()
    {
        return new HashSet<>(this.children.values());
    }

    /**
     * Removes a child from this command
     *
     * @param name the name fo the child
     */
    public void removeChild(String name)
    {
        CubeCommand cmd = this.getChild(name);
        Iterator<Map.Entry<String, CubeCommand>> it = this.children.entrySet().iterator();

        while (it.hasNext())
        {
            if (it.next().getValue() == cmd)
            {
                it.remove();
            }
        }
        this.childrenAliases.removeAll(cmd.getAliases());
        cmd.parent = null;
    }

    public List<String> tabComplete(CubeContext context)
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

    private List<String> tabCompleteParamValue(CubeContext context, CtxDescriptor descriptor)
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

    private void tabCompleteParam(CubeContext context, CtxDescriptor descriptor, List<String> result, String last)
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

    private void tabCompleteIndexed(CubeContext context, CtxDescriptor descriptor,
                                    List<String> result, int index, String last)
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

    private void tabCompleteFlags(CubeContext context, CtxDescriptor descriptor, List<String> result, String last)
    {
        if (!last.isEmpty())
        {
            last = last.substring(1);
        }
        for (Flag commandFlag : descriptor.getFlags())
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

    /**
     * This method handles the command execution
     *
     * @param context The CubeContext containing all the necessary information
     */
    public abstract CommandResult run(CubeContext context);

    public boolean isOnlyIngame()
    {
        return onlyIngame != null;
    }

    public void setOnlyIngame(String onlyIngame)
    {
        this.onlyIngame = onlyIngame;
    }

    public String getOnlyIngame()
    {
        return this.onlyIngame;
    }

    public void checkContext(CubeContext ctx) throws CommandException
    {
        if (ctx.getCommand().isCheckperm() && !ctx.getCommand().isAuthorized(ctx.getSender()))
        {
            throw new PermissionDeniedException(ctx.getCommand().getPermission());
        }
        CtxDescriptor descriptor = ctx.getCommand().getContextFactory().descriptor();
        ArgBounds bounds = descriptor.getArgBounds();
        if (ctx.getIndexedCount() < bounds.getMin())
        {
            throw new TooFewArgumentsException(ctx.getSender());
        }
        if (bounds.getMax() > ArgBounds.NO_MAX && ctx.getIndexedCount() > bounds.getMax())
        {
            throw new TooManyArgumentsException(ctx.getSender());
        }
        if (ctx.getCommand().isOnlyIngame() && !(ctx.isSender(User.class)))
        {
            // TODO disallow usage for SENDER Classes
            String onlyIngame = ctx.getCommand().getOnlyIngame();
            if (onlyIngame.isEmpty())
            {
                throw new IncorrectUsageException(ctx.getSender().getTranslation(NEGATIVE, "This command can only be used ingame!"), false);
            }
            throw new IncorrectUsageException(onlyIngame, false);
        }
        for (NamedParameter named : descriptor.getNamedGroups().listAll())
        {
            if (named.isRequired() && named.isInRequiredGroup())
            {
                if (!(ctx.hasNamed(named.getName())))
                {
                    throw new MissingParameterException(named.getName());
                }
            }
            if (named instanceof CommandParameterNamed)
            {
                if (ctx.hasNamed(named.getName()) &&
                    !((CommandParameterNamed)named).checkPermission(ctx.getSender()))
                {
                    throw new PermissionDeniedException(((CommandParameterNamed)named).getPermission());
                }
            }

        }
        for (Flag flag : descriptor.getFlags())
        {
            if (flag instanceof CommandFlag)
            {
                if (ctx.hasFlag(flag.getName())
                    && !((CommandFlag)flag).checkPermission(ctx.getSender()))
                {
                    throw new PermissionDeniedException(((CommandFlag)flag).getPermission());
                }
            }
        }
    }

    @Override
    public CubeContextFactory getContextFactory()
    {
        return (CubeContextFactory)super.getContextFactory();
    }
}
