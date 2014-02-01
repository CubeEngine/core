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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.command.exception.CommandException;
import de.cubeisland.engine.core.command.exception.IncorrectUsageException;
import de.cubeisland.engine.core.command.exception.MissingParameterException;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;
import de.cubeisland.engine.core.command.parameterized.ParameterizedCommand;
import de.cubeisland.engine.core.command.sender.BlockCommandSender;
import de.cubeisland.engine.core.command.sender.WrappedCommandSender;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;


import static de.cubeisland.engine.core.contract.Contract.expectNotNull;
import static de.cubeisland.engine.core.util.StringUtils.startsWithIgnoreCase;

/**
 * This class is the base for all of our commands
 * it implements the execute() method which provides error handling and calls the
 * run() method which should be implemented by the extending classes
 */
public abstract class CubeCommand extends Command
{
    private CubeCommand parent;
    private final Module module;
    private final Map<String, CubeCommand> children;
    protected final List<String> childrenAliases;
    private final ContextFactory contextFactory;
    private boolean loggable;
    private boolean asynchronous = false;
    private boolean generatePermission;
    private PermDefault generatedPermissionDefault;

    public CubeCommand(Module module, String name, String description, ContextFactory contextFactory)
    {
        this(module, name, description, "", new ArrayList<String>(0), contextFactory);
    }

    public CubeCommand(Module module, String name, String description, String usage, List<String> aliases, ContextFactory contextFactory)
    {
        super(name, description, usage.trim(), aliases);
        if ("?".equals(name))
        {
            throw new IllegalArgumentException("Invalid command name: " + name);
        }
        this.parent = null;
        this.module = module;
        this.contextFactory = contextFactory;

        this.children = new THashMap<>();
        this.childrenAliases = new ArrayList<>();
        this.loggable = true;
        this.generatePermission = false;
        this.generatedPermissionDefault = PermDefault.DEFAULT;
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

    @Override
    public void setPermission(String permission)
    {
        this.generatePermission = false;
        super.setPermission(permission);
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
        super.setPermission(perm.getName());
        this.getModule().getCore().getPermissionManager().registerPermission(this.getModule(), perm);
    }

    protected void registerAlias(String[] names, String[] parents, String prefix, String suffix)
    {
        if (names.length == 0)
        {
            throw new IllegalArgumentException("You have to specify at least 1 name!");
        }
        List<String> aliases = Collections.emptyList();
        if (names.length > 1)
        {
            aliases = new ArrayList<>(names.length - 1);
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

        return StringUtils.implode(delimiter, cmds);
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

    @Override
    public CubeCommand setAliases(List<String> aliases)
    {
        super.setAliases(aliases);
        return this;
    }

    @Override
    public CubeCommand setPermissionMessage(String permissionMessage)
    {
        super.setPermissionMessage(permissionMessage);
        return this;
    }

    @Override
    public CubeCommand setDescription(String description)
    {
        super.setDescription(description);
        return this;
    }

    @Override
    public CubeCommand setUsage(String usage)
    {
        super.setUsage(usage);
        return this;
    }

    @Override
    public String getUsage()
    {
        return "/" + this.implodeCommandParentNames(" ") + " " + this.getModule().getCore().getI18n().translate(this.usageMessage);
    }

    /**
     * This overload returns the usage translated for the given CommandSender
     *
     * @param sender the command sender
     * @return the usage string
     */
    public String getUsage(CommandSender sender)
    {
        return (sender instanceof User ? "/" : "") + this
            .implodeCommandParentNames(" ") + ' ' + replaceSemiOptionalArgs(sender, sender.translate(super.getUsage()));
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
        return (sender instanceof User ? "/" : "") + StringUtils
            .implode(" ", context.getLabels()) + ' ' + replaceSemiOptionalArgs(sender, sender
            .translate(super.getUsage()));
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
        StringBuilder usage = new StringBuilder(sender instanceof User ? "/" : "");
        usage.append(StringUtils.implode(" ", parentLabels)).append(' ')
            .append(this.getName()).append(' ')
            .append(sender.translate(super.getUsage()));
        return usage.toString();
    }

    /**
     * Returns a child command by name without typo correction
     *
     * @param name the child name
     * @return the child or null if not found
     */
    public CubeCommand getChild(String name)
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
        this.onRegister();
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
        return new THashSet<>(this.children.values());
    }

    /**
     * Returns a Set of the children's names
     *
     * @return a set of children's names
     */
    public Set<String> getChildrenNames()
    {
        return new THashSet<>(this.children.keySet());
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
        cmd.onRemove();
        cmd.parent = null;
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

    /**
     * This is the entry point from Bukkit into our own command handling code
     *
     * @param bukkitSender the Bukkit command sender which will be wrapped
     * @param label the command label
     * @param args the command arguments
     * @return true if the command succeeded
     */
    @Override
    public final boolean execute(org.bukkit.command.CommandSender bukkitSender, String label, String[] args)
    {
        CommandSender sender = wrapSender(this.getModule().getCore(), bukkitSender);
        this.getModule().getCore().getCommandManager().logExecution(sender, this, args);
        return this.execute(sender, args, label, new Stack<String>());
    }

    /**
     * This method should only ever be overwritten in really special cases!
     * One example for this: the AliasCommand
     *
     * @param sender the CE command server
     * @param args the args array
     * @param label the command label
     * @param labels the label stack
     * @return true on success
     */
    protected boolean execute(final CommandSender sender, String[] args, String label, Stack<String> labels)
    {
        if (!this.testPermissionSilent(sender))
        {
            this.permissionDenied(sender);
            return true;
        }
        labels.push(label);
        if (args.length > 0)
        {
            if ("?".equals(args[0]))
            {
                HelpContext ctx = new HelpContext(this, sender, labels, args);
                try
                {
                    this.help(ctx);
                }
                catch (Exception e)
                {
                    this.handleCommandException(ctx.getSender(), e);
                }
                return true;
            }
            CubeCommand child = this.getChild(args[0]);
            if (child != null)
            {
                return child.execute(sender, Arrays.copyOfRange(args, 1, args.length), args[0], labels);
            }
        }
        try
        {
            final CommandContext ctx = this.getContextFactory().parse(this, sender, labels, args);
            if (this.isAsynchronous())
            {
                ctx.getCore().getTaskManager().getThreadFactory().newThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        run0(ctx);
                    }
                }).start();
            }
            else
            {
                this.run0(ctx);
            }
        }
        catch (CommandException e)
        {
            this.handleCommandException(sender, e);
        }

        return true;
    }

    private void run0(CommandContext ctx)
    {
        try
        {
            CommandResult result = this.run(ctx);
            if (result != null)
            {
                result.show(ctx);
            }
        }
        catch (Exception e)
        {
            handleCommandException(ctx.getSender(), e);
        }
    }

    private void permissionDenied(CommandSender sender)
    {
        sender.sendTranslated("&cYou're not allowed to do this!");
        sender.sendTranslated("&cContact an administrator if you think this is a mistake!");
    }

    private void handleCommandException(final CommandSender sender, Throwable t)
    {
        if (!CubeEngine.isMainThread())
        {
            final Throwable tmp = t;
            sender.getCore().getTaskManager().callSync(new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    handleCommandException(sender, tmp);
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
            sender.sendTranslated("&cThe parameter &6%s&c is missing!", t.getMessage());
        }
        else if (t instanceof IncorrectUsageException)
        {
            sender.sendMessage(t.getMessage());
            sender.sendTranslated("&eProper usage: &f%s", this.getUsage(sender));
        }
        else if (t instanceof PermissionDeniedException)
        {
            this.permissionDenied(sender);
        }
        else
        {
            sender.sendTranslated("&4An unknown error occurred while executing this command!");
            sender.sendTranslated("&4Please report this error to an administrator.");
            this.module.getLog().debug(t, t.getLocalizedMessage());
        }
    }

    private List<String> tabCompleteFallback(org.bukkit.command.CommandSender bukkitSender, String alias, String[] args) throws IllegalArgumentException
    {
        return super.tabComplete(bukkitSender, alias, args);
    }

    private static final int TAB_LIMIT_THRESHOLD = 50;

    @Override
    public final List<String> tabComplete(org.bukkit.command.CommandSender bukkitSender, String alias, String[] args) throws IllegalArgumentException
    {
        CubeCommand completer = this;
        if (args.length > 0 && !args[0].isEmpty())
        {
            CubeCommand child = this.getChild(args[0]);
            if (child != null)
            {
                args = Arrays.copyOfRange(args, 1, args.length);
                completer = child;
            }
        }
        CommandSender sender = wrapSender(this.getModule().getCore(), bukkitSender);

        this.getModule().getCore().getCommandManager().logTabCompletion(sender, this, args);
        List<String> result = completer.tabComplete(sender, alias, args);
        if (result == null)
        {
            result = completer.tabCompleteFallback(bukkitSender, alias, args);
        }
        final int max = this.getModule().getCore().getConfiguration().commands.maxTabCompleteOffers;
        if (result.size() > max)
        {
            if (StringUtils.implode(", ", result).length() < TAB_LIMIT_THRESHOLD)
            {
                return result;
            }
            result = result.subList(0, max);
        }
        return result;
    }

    public List<String> tabComplete(CommandSender sender, String label, String[] args)
    {
        if (this.hasChildren() && args.length == 1 && !((this instanceof ParameterizedCommand) && !args[0].isEmpty() && args[0].charAt(0) == '-'))
        {
            List<String> actions = new ArrayList<>();
            String token = args[0].toLowerCase(Locale.ENGLISH);

            Set<CubeCommand> names = this.getChildren();
            names.removeAll(this.childrenAliases);
            for (CubeCommand child : names)
            {
                if (startsWithIgnoreCase(child.getName(), token) && child.testPermissionSilent(sender))
                {
                    actions.add(child.getName());
                }
            }
            Collections.sort(actions, String.CASE_INSENSITIVE_ORDER);

            return actions;
        }
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
     * @throws Exception if an error occurs
     */
    public abstract CommandResult run(CommandContext context) throws Exception;

    /**
     * This method is called if the help page of this command was requested by the ?-action
     *
     * @param context The CommandContext containing all the necessary information
     * @throws Exception if an error occurs
     */
    public void help(HelpContext context) throws Exception
    {
        context.sendTranslated("&7Description: &f%s", this.getDescription());
        context.sendTranslated("&7Usage: &f%s", this.getUsage(context));

        if (this.hasChildren())
        {
            context.sendMessage(" ");
            context.sendTranslated("The following sub commands are available:");
            context.sendMessage(" ");

            final CommandSender sender = context.getSender();
            for (CubeCommand command : context.getCommand().getChildren())
            {
                if (command.testPermissionSilent(sender))
                {
                    context.sendMessage(ChatFormat.YELLOW + command.getName() + ChatFormat.WHITE + ": " + ChatFormat.GREY + sender.translate(command.getDescription()));
                }
            }
        }
        context.sendMessage(" ");
        context.sendTranslated("&7Detailed help: &9%s", "http://engine.cubeisland.de/c/" + this.getModule().getId() + "/" + this.implodeCommandParentNames("/"));
    }

    public void onRegister()
    {}

    public void onRemove()
    {}
}
