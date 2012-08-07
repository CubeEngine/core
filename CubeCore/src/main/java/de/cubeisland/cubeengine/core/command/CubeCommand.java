package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.Validate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Phillip Schichtel
 */
public abstract class CubeCommand extends Command
{
    private final Module module;
    private Map<String, CubeCommand> subCommands;
    
    public CubeCommand(Module module, String name)
    {
        super(name);
        this.module = module;
    }
    
    public CubeCommand(Module module, String name, String description, String usageMessage, List<String> aliases)
    {
        super(name, description, usageMessage, aliases);
        this.module = module;
    }
    
    public CubeCommand getSubCommand(String name)
    {
        if (name != null)
        {
            name = name.toLowerCase();
            if (this.subCommands.containsKey(name))
            {
                return this.subCommands.get(name);
            }
        }
        return null;
    }
    
    public void addSubCommand(CubeCommand command)
    {
        Validate.notNull(command, "The command must not be null!");
        
        this.subCommands.put(command.getName(), command);
        for (String alias : command.getAliases())
        {
            this.subCommands.put(alias, command);
        }
    }
    
    public boolean hasSubCommand(String name)
    {
        if (name != null)
        {
            return this.subCommands.containsKey(name.toLowerCase());
        }
        return false;
    }

    @Override
    public final boolean execute(CommandSender sender, String label, String[] args)
    {
        if (args.length > 0 && !args[0].startsWith("-") && this.hasSubCommand(args[0]))
        {
            String[] subArgs = new String[args.length - 1];
            System.arraycopy(args, 1, subArgs, 0, args.length - 1);
            return this.getSubCommand(args[0]).execute(sender, label, subArgs);
        }
        
        CommandContext context = new CommandContext(this, sender, label, args, null);
        
        return context.getResult();
    }

    public List<CubeCommand> getSubCommands()
    {
        return new LinkedList<CubeCommand>(this.subCommands.values());
    }
    
    public final Module getModule()
    {
        return this.module;
    }
    
    public void removeSubCommand(String command)
    {
        CubeCommand removedCommand = this.subCommands.remove(command);
        if (removedCommand != null)
        {
            for (Map.Entry<String, CubeCommand> entry : this.subCommands.entrySet())
            {
                if (entry.getValue() == removedCommand)
                {
                    this.subCommands.remove(entry.getKey());
                }
            }
        }
    }
    
    public abstract void run(CommandContext context);
}
