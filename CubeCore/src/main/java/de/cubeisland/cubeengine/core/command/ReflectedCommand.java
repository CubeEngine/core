package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.module.Module;
import java.lang.reflect.Method;
import java.util.List;

/**
 *
 * @author Phillip Schichtel
 */
public class ReflectedCommand extends CubeCommand
{
    private final Method commandMethod;
    
    
    public ReflectedCommand(Module module, Method method, Command annotation)
    {
        this(module, method, "", "", "", null);
    }

    public ReflectedCommand(Module module, Method method, String name, String description, String usageMessage, List<String> aliases)
    {
        super(module, name, description, usageMessage, aliases);
        this.commandMethod = method;
    }

    @Override
    public void run(CommandContext context)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
