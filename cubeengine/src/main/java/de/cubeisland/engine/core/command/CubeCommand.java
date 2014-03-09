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

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.formatter.MessageType;


import static de.cubeisland.engine.core.contract.Contract.expectNotNull;
import static de.cubeisland.engine.core.util.ChatFormat.GREY;
import static de.cubeisland.engine.core.util.ChatFormat.WHITE;
import static de.cubeisland.engine.core.util.ChatFormat.YELLOW;
import static de.cubeisland.engine.core.util.StringUtils.startsWithIgnoreCase;
import static de.cubeisland.engine.core.util.StringUtils.implode;
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
    private String usage;
    private String description;
    private String permission;
    
    private CubeCommand parent;
    private final Map<String, CubeCommand> children;
    protected final List<String> childrenAliases;
    private final ContextFactory contextFactory;
    private boolean loggable;
    private boolean asynchronous = false;
    private boolean generatePermission;
    private PermDefault generatedPermissionDefault;

    public CubeCommand(Module module, String name, String description, ContextFactory contextFactory)
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
        this.generatePermission = false;
        this.generatedPermissionDefault = PermDefault.DEFAULT;
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
        final String permission = this.getPermission();
        if (permission == null || permission.isEmpty())
        {
            return true;
        }
        return sender.hasPermission(this.permission);
    }
    
    public CubeCommand setPermission(String permission)
    {
        this.generatePermission = false;
        this.permission = permission;
        return this;
    }
    
    public String getPermission()
    {
        return this.permission;
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

    public boolean isGeneratePermission()
    {
        return generatePermission;
    }

    public void setGeneratePermission(boolean generatePermission)
    {
        this.generatePermission = generatePermission;
    }

    public PermDefault getGeneratedPermissionDefault()
    {
        return generatedPermissionDefault;
    }

    public void setGeneratedPermissionDefault(PermDefault generatedPermissionDefault)
    {
        this.setGeneratePermission(true);
        this.generatedPermissionDefault = generatedPermissionDefault;
    }

    protected void registerAlias(String[] names, String[] parents)
    {
        this.registerAlias(names, parents, "", "");
    }

    protected Permission generatePermissionNode()
    {
        Permission commandBase = this.getModule().getBasePermission().childWildcard("command");
        LinkedList<String> cmds = new LinkedList<>();
        CubeCommand cmd = this;
        do
        {
            cmds.addFirst(cmd.getName());
        }
        while ((cmd = cmd.getParent()) != null);
        Permission perm = commandBase;
        Iterator<String> it = cmds.iterator();
        while (it.hasNext())
        {
            String permString = it.next();
            if (it.hasNext())
            {
                perm = perm.childWildcard(permString);
            }
            else
            {
                perm = perm.child(permString, this.getGeneratedPermissionDefault());
            }
        }
        return perm;
    }

    public void updateGeneratedPermission()
    {
        if (this.isGeneratePermission())
        {
            PermDefault def = null;
            String node = this.getPermission();
            if (node != null)
            {
                def = this.getModule().getCore().getPermissionManager().getDefaultFor(node);
            }
            if (def == null)
            {
                def = this.getGeneratedPermissionDefault();
            }
            this.setGeneratedPermission(def);
        }
    }

    public void setGeneratedPermission(PermDefault def)
    {
        this.generatePermission = true;
        this.setGeneratedPermissionDefault(def);
        Permission perm = this.generatePermissionNode();
        this.permission = perm.getName();
        this.getModule().getCore().getPermissionManager().registerPermission(this.getModule(), perm);
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
            return usage.replace('{', '[').replace('}', ']');
        }
        else
        {
            return usage.replace('{', '<').replace('}', '>');
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

    public CubeCommand setUsage(String usage)
    {
        this.usage = usage;
        return this;
    }

    public String getUsage()
    {
        return "/" + this.implodeCommandParentNames(" ") + " " + this.getModule().getCore().getI18n().translate(this.usage);
    }

    /**
     * This overload returns the usage translated for the given CommandSender
     *
     * @param sender the command sender
     * @return the usage string
     */
    public String getUsage(CommandSender sender)
    {
        return (sender instanceof User ? "/" : "") + implodeCommandParentNames(" ") + ' ' + replaceSemiOptionalArgs(sender, sender.composeMessage(NONE, this.usage));
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
        return (sender instanceof User ? "/" : "") +
            implode(" ", context.getLabels()) + ' ' + replaceSemiOptionalArgs(sender, sender
            .composeMessage(NONE, this.usage));
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
        return sender instanceof User ? "/" : "" + implode(" ", parentLabels) + ' ' + name + ' ' + sender.composeMessage(NONE, this.usage);
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
        context.sendTranslated(NEUTRAL, "Description: {message}", this.getDescription());
        context.sendTranslated(NEUTRAL, "Usage: {message}", this.getUsage(context));

        if (this.hasChildren())
        {
            context.sendMessage(" ");
            context.sendTranslated(NONE, "The following sub commands are available:");
            context.sendMessage(" ");

            final CommandSender sender = context.getSender();
            for (CubeCommand command : context.getCommand().getChildren())
            {
                if (command.isAuthorized(sender))
                {
                    context.sendMessage(YELLOW + command.getName() + WHITE + ": " + GREY + sender.composeMessage(NONE, command
                        .getDescription()));
                }
            }
        }
        context.sendMessage(" ");
        context.sendTranslated(NEUTRAL, "Detailed help: {message#link}", "http://engine.cubeisland.de/c/" + this.getModule().getId() + "/" + this.implodeCommandParentNames("/"));
    }
}
