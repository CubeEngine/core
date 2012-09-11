package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.module.Module;
import gnu.trove.map.hash.THashMap;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author Phillip Schichtel
 */
public class ReflectedCommand extends CubeCommand
{
    private final Object commandContainer;
    private final Method commandMethod;
    private final int min;
    private final int max;
    private Map<String[], Class<?>> namedParameters;
    private final boolean checkPermision;
    private final String permissionNode;
    private final PermissionDefault permissionDefault;
    private final Flag[] flags;
    private final Param[] params;

    public ReflectedCommand(Module module, Object commandContainer, Method method, Command annotation, String name, String description, String usage, List<String> aliases)
    {
        super(module, name, description, usage, aliases);
        this.commandMethod = method;
        this.commandContainer = commandContainer;
        
        this.min = annotation.min();
        this.max = annotation.max();
        this.checkPermision = annotation.checkPerm();
        this.namedParameters = new THashMap<String[], Class<?>>();
        
        this.flags = annotation.flags();
        this.params = annotation.params();
        
        this.permissionDefault = annotation.permDefault();
        if (this.checkPermision)
        {
            if ("".equals(annotation.permNode()))
            {
                this.permissionNode = this.generatePermissionNode();
            }
            else
            {
                this.permissionNode = annotation.permNode();
            }
            module.getCore().getPermissionRegistration().registerPermission(this.permissionNode, this.permissionDefault);
        }
        else
        {
            this.permissionNode = null;
        }
    }

    @Override
    public void run(CommandContext context)
    {
        try
        {
            if (context.size() < this.min || context.size() > this.max)
            {
                throw new InvalidUsageException(this.min, this.max);
            }
            if (this.checkPermision && context.getSender().hasPermission(usageMessage))
            {
                throw new PermissionDeniedException(context.getSender(), "You are not allowed to do this.");
            }

            this.commandMethod.invoke(this.commandContainer, context);
        }
        catch (Exception e)
        {
            context.setResult(false);
        }
    }
    
    protected String generatePermissionNode()
    {
        return ""; // TODO implement me
    }
}