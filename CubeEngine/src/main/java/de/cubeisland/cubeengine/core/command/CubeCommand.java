package de.cubeisland.cubeengine.core.command;

import static de.cubeisland.cubeengine.core.CubeEngine._;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.StringUtils;
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
    private Command parent;
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

    public CubeCommand(Module module, String name, String description, String usageMessage, List<String> aliases, Command parent)
    {
        super(name, description, usageMessage, aliases);
        this.parent = parent;
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
        context.parseCommandArgs(args);

        if (context.isHelpCall())
        {
            this.showHelp(context);
        }
        else
        {
            this.run(context);
        }

        return context.getResult();
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
    
    public Command getParent()
    {
        return this.parent;
    }
    
    public void showHelp(CommandContext context)
    {
        CommandSender sender = context.getSender();
        StringBuilder commandLine = new StringBuilder('/').append(StringUtils.implode(" ", context.getLabels()));
        if (this.hasChildren())
        {
            commandLine.append(' ').append('[').append(_(sender, "core", "sub command")).append(']');
        }
        sender.sendMessage(commandLine.toString());
        
        sender.sendMessage(_(sender, "core", "Description: %s", _(sender, this.getModule().getName(), this.getDescription())));
        
        for (Command child : this.getChildren())
        {
            // TODO implement
        }
    }

    public abstract void run(CommandContext context);
}