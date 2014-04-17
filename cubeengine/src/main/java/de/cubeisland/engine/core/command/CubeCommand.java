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
import java.util.Set;
import java.util.Stack;

import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.core.command.exception.CommandException;
import de.cubeisland.engine.core.command.exception.IncorrectUsageException;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;
import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexed;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.StringUtils;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;
import static de.cubeisland.engine.core.permission.PermDefault.OP;
import static de.cubeisland.engine.core.util.ChatFormat.*;
import static de.cubeisland.engine.core.util.StringUtils.implode;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEUTRAL;
import static de.cubeisland.engine.core.util.formatter.MessageType.NONE;

/**
 * This class is the base for all of our commands
 * it implements the execute() method which provides error handling and calls the
 * run() method which should be implemented by the extending classes
 */
public abstract class CubeCommand
{
    private final Module module;
    private final String name;
    private String label;
    private final Set<String> aliases;
    private String description;

    private CubeCommand parent;
    private final Map<String, CubeCommand> children;
    protected final List<String> childrenAliases;
    private final ContextFactory contextFactory;
    private boolean loggable;
    private boolean asynchronous = false;
    private final Permission permission;

    private boolean permRegistered = false;

    private String onlyIngame = null;

    public CubeCommand(Module module, String name, String description, ContextFactory contextFactory, Permission permission)
    {
        if ("?".equals(name))
        {
            throw new IllegalArgumentException("Invalid command name: " + name);
        }
        this.module = module;
        this.name = name;
        this.aliases = new HashSet<>();
        this.description = description;
        
        this.contextFactory = contextFactory;

        this.children = new HashMap<>();
        this.childrenAliases = new ArrayList<>();
        this.loggable = true;
        this.permission = permission;
    }

    public CubeCommand(Module module, String name, String description, BasicContextFactory cFactory)
    {
        this(module, name, description, cFactory, Permission.detachedPermission(name, OP));
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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public boolean isAuthorized(CommandSender sender)
    {
        return this.permission == null || sender == null || this.permission.isAuthorized(sender);
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

        Permission perm = this.getModule().getBasePermission().childWildcard("command");

        while (!cmds.isEmpty())
        {
            perm = perm.childWildcard(cmds.pop());
        }
        this.permission.setParent(perm);
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

    public ContextFactory getContextFactory()
    {
        return this.contextFactory;
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
            this.module.getCore().getI18n().getDefaultLanguage().getLocale(), null);
    }

    public final String getUsage(Locale locale, Permissible permissible)
    {
        return this.getUsage0(locale, permissible);
    }

    protected String getUsage0(Locale locale, Permissible permissible)
    {
        StringBuilder sb = new StringBuilder();
        int inGroup = 0;
        for (CommandParameterIndexed indexedParam : this.contextFactory.getIndexedParameters())
        {
            if (indexedParam.getCount() == 1 || indexedParam.getCount() < 0)
            {
                sb.append(convertLabel(indexedParam.isGroupRequired(), StringUtils.implode("|", convertLabels(indexedParam))));
                sb.append(' ');
                inGroup = 0;
            }
            else if (indexedParam.getCount() > 1)
            {
                sb.append(indexedParam.isGroupRequired() ? '<' : '[');
                sb.append(convertLabel(indexedParam.isRequired(), StringUtils.implode("|", convertLabels(indexedParam))));
                sb.append(' ');
                inGroup = indexedParam.getCount() - 1;
            }
            else if (indexedParam.getCount() == 0)
            {
                sb.append(convertLabel(indexedParam.isRequired(), StringUtils.implode("|", convertLabels(indexedParam))));
                inGroup--;
                if (inGroup == 0)
                {
                    sb.append(indexedParam.isGroupRequired() ? '>' : ']');
                }
                sb.append(' ');
            }
        }
        return sb.toString().trim();
    }

    private String[] convertLabels(CommandParameterIndexed indexedParam)
    {
        String[] labels = indexedParam.getLabels().clone();
        String[] rawLabels = indexedParam.getLabels();
        for (int i = 0; i < rawLabels.length; i++)
        {
            if (rawLabels.length == 1)
            {
                labels[i] = convertLabel(true, "!" + rawLabels[i]);
            }
            else
            {
                labels[i] = convertLabel(true, rawLabels[i]);
            }
        }
        return labels;
    }

    private String convertLabel(boolean req, String label)
    {
        if (label.startsWith("!"))
        {
            return label.substring(1);
        }
        else if (req)
        {
            return "<" + label + ">";
        }
        else
        {
            return "[" + label + "]";
        }
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
         * This overload returns the usage translated for the given CommandContext
         * using the correct labels
         *
         * @param context the command context
         * @return the usage string
         */

    public String getUsage(CommandContext context)
    {
        final CommandSender sender = context.getSender();
        String usage = this.getUsage(sender.getLocale(), context.getSender());
        return (sender instanceof User ? "/" : "") + implode(" ", context.getLabels()) + ' ' + replaceSemiOptionalArgs(sender, usage);
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
        return sender instanceof User ? "/" : "" + implode(" ", parentLabels) + ' ' + name + ' ' + usage;
    }

    /**
     * Returns a child command by name without typo correction
     *
     * @param name the child name
     * @return the child or null if not found
     */
    public final CubeCommand getChild(String name)
    {
        if (name == null)
        {
            return null;
        }

        return this.children.get(name.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Adds a child to this command
     *
     * @param command the command to add
     */
    public final void addChild(CubeCommand command)
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
            alias = alias.toLowerCase(Locale.ENGLISH);
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
    public final boolean hasChild(String name)
    {
        return name != null && this.children.containsKey(name.toLowerCase());
    }

    /**
     * Checks whether this command has children
     *
     * @return true if that is the case
     */
    public final boolean hasChildren()
    {
        return !this.children.isEmpty();
    }

    /**
     * Returns a Set of all children
     *
     * @return a Set of children
     */
    public final Set<CubeCommand> getChildren()
    {
        return new HashSet<>(this.children.values());
    }

    /**
     * Removes a child from this command
     *
     * @param name the name fo the child
     */
    public final void removeChild(String name)
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

    public List<String> tabComplete(CommandContext context)
    {
        return null;
        // TODO indexed Completers
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
     * Returns the parent of this command or null if there is none
     *
     * @return the parent command or null
     */
    public CubeCommand getParent()
    {
        return this.parent;
    }

    /**
     * This method handles the command execution
     *
     * @param context The CommandContext containing all the necessary information
     */
    public abstract CommandResult run(CommandContext context);

    /**
     * This method is called if the help page of this command was requested by the ?-action
     *
     * @param context The CommandContext containing all the necessary information
     */
    public void help(HelpContext context)
    {
        context.sendTranslated(NONE, "{text:Description:color=GREY}: {input}", this.getDescription());
        context.sendTranslated(NONE, "{text:Usage:color=GREY}: {input}", this.getUsage(context));

        if (this.hasChildren())
        {
            context.sendMessage(" ");
            context.sendTranslated(NEUTRAL, "The following subcommands are available:");
            context.sendMessage(" ");

            final CommandSender sender = context.getSender();
            for (CubeCommand command : context.getCommand().getChildren())
            {
                if (command.isAuthorized(sender))
                {
                    context.sendMessage(YELLOW + command.getName() + WHITE + ": " + GREY + sender.getTranslation(NONE, command.getDescription()));
                }
            }
        }
        context.sendMessage(" ");
        context.sendTranslated(NONE, "{text:Detailed help:color=GREY}: {input#link:color=INDIGO}", "http://engine.cubeisland.de/c/" + this.getModule().getId() + "/" + this.implodeCommandParentNames("/"));
    }

    public void addIndexed(CommandParameterIndexed indexed)
    {
        this.getContextFactory().addIndexed(indexed);
    }

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

    public void checkContext(CommandContext ctx) throws CommandException
    {
        ArgBounds bounds = ctx.getCommand().getContextFactory().getArgBounds();
        if (ctx.getArgCount() < bounds.getMin())
        {
            throw new IncorrectUsageException(ctx.getSender().getTranslation(NEGATIVE, "You've given too few arguments."));
        }
        if (bounds.getMax() > ArgBounds.NO_MAX && ctx.getArgCount() > bounds.getMax())
        {
            throw new IncorrectUsageException(ctx.getSender().getTranslation(NEGATIVE, "You've given too many arguments."));
        }
        if (!ctx.getCommand().isAuthorized(ctx.getSender()))
        {
            throw new PermissionDeniedException(ctx.getCommand().getPermission());
        }
        if (ctx.getCommand().isOnlyIngame() && !(ctx.isSender(User.class)))
        {
            String onlyIngame = ctx.getCommand().getOnlyIngame();
            if (onlyIngame.isEmpty())
            {
                throw new IncorrectUsageException(ctx.getSender().getTranslation(NEGATIVE, "This command can only be used ingame!"), false);
            }
            throw new IncorrectUsageException(onlyIngame, false);
        }
    }
}
