package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.module.Module;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Phillip Schichtel
 */
public abstract class CubeCommand extends Command
{
    private final Module module;
    private Map<String, CubeCommand> children;
    private Map<String, String> childrenAliases;

    public CubeCommand(Module module, String name)
    {
        this(module, name, null);
    }

    public CubeCommand(Module module, String name, CubeCommand parent)
    {
        this(module, name, "", "", new ArrayList<String>(0), parent);
    }

    public CubeCommand(Module module, String name, String description, String usageMessage, List<String> aliases)
    {
        this(module, name, description, usageMessage, aliases, null);
    }

    public CubeCommand(Module module, String name, String description, String usageMessage, List<String> aliases, CubeCommand parent)
    {
        super(name, description, usageMessage, aliases);
        this.module = module;

        this.children = new LinkedHashMap<String, CubeCommand>();
        this.childrenAliases = new THashMap<String, String>();
    }

    public CubeCommand getChild(String name)
    {
        return this.getChild(name, false);
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
        if (args.length > 0)
        {
            CubeCommand child = this.getChild(args[0].toLowerCase(Locale.ENGLISH));
            if (child != null)
            {
                return child.execute(sender, args[0], Arrays.copyOfRange(args, 1, args.length - 1));
            }
        }

        CommandContext context = new CommandContext(this.module.getCore(), sender, this, label);
        context.parseCommandArgs(args);

        this.run(context);

        return context.getResult();
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

    public abstract void run(CommandContext context);
}