package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.module.Module;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Phillip Schichtel
 */
public abstract class CubeCommand extends Command
{
    protected static final Flag[] NO_FLAGS = new Flag[0];
    protected static final Param[] NO_PARAMS = new Param[0];
    private CubeCommand parent;
    private final Module module;
    private Map<String, CubeCommand> children;
    private Map<String, String> childrenAliases;

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

    public CubeCommand(Module module, String name, String description, String usageMessage, List<String> aliases, CubeCommand parent)
    {
        super(name, description, usageMessage, aliases);
        this.parent = parent;
        this.module = module;

        this.children = new LinkedHashMap<String, CubeCommand>();
        this.childrenAliases = new THashMap<String, String>();
    }
    
    public int getMinimumParams()
    {
        return 0;
    }
    
    public int getMaximumParams()
    {
        return -1;
    }

    public CubeCommand getChild(String name)
    {
        return this.getChild(name, false);
    }
    
    public String getUsage(CommandContext context)
    {
        return ""; // TODO implement me
    }

    public CubeCommand getChild(String name, boolean ignoreAliases)
    {
        if (name == null)
        {
            return null;
        }

        name = name.toLowerCase(Locale.ENGLISH);
        if (ignoreAliases)
        {
            return this.children.get(name);
        }

        String actualName = this.childrenAliases.get(name);
        if (actualName == null)
        {
            actualName = name;
        }

        return this.children.get(actualName);
    }

    public void addChild(CubeCommand command)
    {
        Validate.notNull(command, "The command must not be null!");

        final String name = command.getName();
        this.children.put(name, command);
        for (String alias : command.getAliases())
        {
            this.childrenAliases.put(alias.toLowerCase(Locale.ENGLISH), name);
        }
    }

    public boolean hasChild(String name)
    {
        if (name != null)
        {
            return this.children.containsKey(name.toLowerCase());
        }
        return false;
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
            CubeCommand child = this.getChild(args[0].toLowerCase(Locale.ENGLISH));
            if (child != null)
            {
                return child.execute(sender, Arrays.copyOfRange(args, 1, args.length - 1), args[0], labels);
            }
        }

        CommandContext context = new CommandContext(this.module.getCore(), sender, this, labels);
        context.parseCommandArgs(args, this.getFlags(), this.getParams());

        if (context.isHelpCall())
        {
            this.showHelp(context);
        }
        else
        {
            this.run(context);
        }

        return true;
    }
    
    public boolean hasChildren()
    {
        return !this.children.isEmpty();
    }

    public List<CubeCommand> getChildren()
    {
        return new LinkedList<CubeCommand>(this.children.values());
    }

    public final Module getModule()
    {
        return this.module;
    }

    public void removeChild(String name)
    {
        name = name.toLowerCase(Locale.ENGLISH);
        if (this.children.remove(name) != null)
        {
            for (Map.Entry<String, String> entry : this.childrenAliases.entrySet())
            {
                if (entry.getValue().equals(name))
                {
                    this.children.remove(entry.getKey());
                }
            }
        }
    }
    
    public CubeCommand getParent()
    {
        return this.parent;
    }
    
    public Flag[] getFlags()
    {
        return NO_FLAGS;
    }
    
    public Param[] getParams()
    {
        return NO_PARAMS;
    }

    public abstract void run(CommandContext context);
    
    public abstract void showHelp(CommandContext context);
}