package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.command.exception.InvalidUsageException;
import de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException;
import de.cubeisland.cubeengine.core.module.CoreModule;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static de.cubeisland.cubeengine.core.util.log.LogLevel.ERROR;
import static de.cubeisland.cubeengine.core.i18n.I18n._;

/**
 * This class is the base for all of our commands
 * it implements the execute() method which provides error handling and calls the
 * run() method which should be implemented by the extending classes
 */
public abstract class CubeCommand extends Command
{
    protected static final Flag[] NO_FLAGS = new Flag[0];
    protected static final Param[] NO_PARAMS = new Param[0];
    private CubeCommand parent;
    private final Module module;
    private final Map<String, CubeCommand> children;
    private final String usageBase;

    public CubeCommand(Module module, String name, String description)
    {
        this(module, name, description, null);
    }

    public CubeCommand(Module module, String name, String description, CubeCommand parent)
    {
        this(module, name, "", description, new ArrayList<String>(0), parent);
    }

    public CubeCommand(Module module, String name, String description, String usageMessage, List<String> aliases)
    {
        this(module, name, description, usageMessage, aliases, null);
    }

    public CubeCommand(Module module, String name, String description, String usage, List<String> aliases, CubeCommand parent)
    {
        super(name, description, usage.trim(), aliases);
        this.parent = parent;
        this.module = module;

        this.children = new LinkedHashMap<String, CubeCommand>();

        this.usageBase = "/" + this.implodeCommandPathNames(" ") + " ";
    }

    /**
     * This method implodes the path of this command, so the name of the command and the name of every parent
     *
     * @param delim the delimiter
     * @return the imploded path
     */
    protected final String implodeCommandPathNames(String delim)
    {
        List<String> cmds = new LinkedList<String>();
        CubeCommand cmd = this;
        do
        {
            cmds.add(cmd.getName());
        }
        while ((cmd = this.getParent()) != null);
        Collections.reverse(cmds);

        return StringUtils.implode(delim, cmds);
    }

    /**
     * Returns the minimum number of indexed parameters this command requires
     *
     * @return minimum params
     */
    public int getMinimumParams()
    {
        return 0;
    }

    /**
     * Returns the maximum number of indexed parameters this command allowes.
     * A value lower than 0 indicates that there is no limit
     *
     * @return maximum params
     */
    public int getMaximumParams()
    {
        return -1;
    }

    @Override
    public String getUsage()
    {
        return this.usageBase + _(this.module, super.getUsage());
    }

    /**
     * This overload returns the usage translated for the given CommandSender
     *
     * @param sender the command sender
     * @return the usage string
     */
    public String getUsage(CommandSender sender)
    {
        return this.usageBase + _(sender, this.module, super.getUsage());
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
        return "/" + StringUtils.implode(" ", context.getLabels()) + " " + _(context.getSender(), this.module, super.getUsage());
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
        return "/" + StringUtils.implode(" ", parentLabels) + " " + this.getName() + " " + _(sender, this.module, super.getUsage());
    }

    /**
     * Returns a child command by name without typo correction
     *
     * @param name the child name
     * @return the child or null if not found
     */
    public CubeCommand getChild(String name)
    {
        return this.getChild(name, false);
    }

    /**
     * Returns a child command and tries to correct the name of specified.
     *
     * @param name    the name
     * @param correct whether to correct the name
     * @return the child or null if not found
     */
    public CubeCommand getChild(String name, boolean correct)
    {
        if (name == null)
        {
            return null;
        }

        name = name.toLowerCase(Locale.ENGLISH);

        CubeCommand child = this.children.get(name);
        if (correct && child == null)
        {
            List<String> matches = StringUtils.getBestMatches(name, this.children.keySet(), 1);
            if (matches.size() == 1)
            {
                child = this.getChild(matches.get(0), false);
            }
        }

        return child;
    }

    /**
     * Adds a child to this command
     *
     * @param command the command to add
     */
    public void addChild(CubeCommand command)
    {
        Validate.notNull(command, "The command must not be null!");

        final String name = command.getName();
        this.children.put(name, command);
        for (String alias : command.getAliases())
        {
            this.children.put(alias.toLowerCase(Locale.ENGLISH), command);
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
        if (name != null)
        {
            return this.children.containsKey(name.toLowerCase());
        }
        return false;
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
        return new LinkedHashSet<CubeCommand>(this.children.values());
    }

    /**
     * Returns a Set of the children's names
     *
     * @return a set of children's names
     */
    public Set<String> getChildrenNames()
    {
        return this.children.keySet();
    }

    /**
     * Removes a child from this command
     *
     * @param name the name fo the child
     */
    public void removeChild(String name)
    {
        CubeCommand cmd = this.getChild(name);
        Iterator<CubeCommand> iter = this.children.values().iterator();

        while (iter.hasNext())
        {
            if (iter.next() == cmd)
            {
                iter.remove();
            }
        }
    }

    @Override
    public final boolean execute(CommandSender sender, String label, String[] args)
    {
        return this.execute(sender, args, label, new Stack<String>());
    }

    private boolean execute(CommandSender sender, String[] args, String label, Stack<String> labels)
    {
        labels.push(label);
        if (args.length > 0)
        {
            CubeCommand child = this.getChild(args[0], true);
            if (child != null)
            {
                return child.execute(sender, Arrays.copyOfRange(args, 1, args.length), args[0], labels);
            }
        }

        CommandContext context = new CommandContext(this.module.getCore(), sender, this, labels);
        try
        {
            context.parseCommandArgs(args, this.getFlags(), this.getParams());

            if (context.isHelpCall())
            {
                this.showHelp(context);
            }
            else
            {
                this.run(context);
            }
        }
        catch (InvalidUsageException e)
        {
            context.sendMessage(e.getMessage());
            if (e.showUsage())
            {
                context.sendMessage("core", "&eProper usage: &f%s", this.getUsage(context));
            }
        }
        catch (PermissionDeniedException e)
        {
            context.sendMessage(e.getMessage());
        }
        catch (Exception e)
        {
            context.sendMessage("core", "&4An unknown error occurred while executing this command!");
            context.sendMessage("core", "&4Please report this error to an administrator.");
            if (this.module instanceof CoreModule)
            {
                CubeEngine.getLogger().log(ERROR, e.getLocalizedMessage(), e);
            }
            else
            {
                this.module.getLogger().log(ERROR, e.getLocalizedMessage(), e);
            }
        }

        return true;
    }

    /**
     * Returns the module this command was registered by
     *
     * @return a module
     */
    public final Module getModule()
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
     * Returns an array of the defined flags
     *
     * @return the defined flags
     */
    public Flag[] getFlags()
    {
        return NO_FLAGS;
    }

    /**
     * Returns an array of the defined named parameters
     *
     * @return the defined named parameters
     */
    public Param[] getParams()
    {
        return NO_PARAMS;
    }

    /**
     * This method handles the command execution
     *
     * @param context The CommandContext containg all the necessary information
     * @throws Exception if an error occures
     */
    public abstract void run(CommandContext context) throws Exception;

    /**
     * This method is called if the help page of this command was requested by the ?-action
     *
     * @param context The CommandContext containg all the necessary information
     * @throws Exception if an error occures
     */
    public abstract void showHelp(CommandContext context) throws Exception;
}
