package de.cubeisland.cubeengine.core.command;

import static de.cubeisland.cubeengine.core.CubeEngine._;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.command.exception.InvalidUsageException;
import de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.StringUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.command.CommandSender;
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
    public int getMinimumParams()
    {
        return this.min;
    }

    @Override
    public int getMaximumParams()
    {
        return this.max;
    }

    @Override
    public void run(CommandContext context)
    {
        try
        {
            if (context.indexedCount() < this.min || (this.max != -1 && context.indexedCount() > this.max))
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
            Throwable t = e;
            if (t instanceof InvocationTargetException)
            {
                t = t.getCause();
            }
            String message = t.getMessage();
            if (message == null)
            {
                message = t.getClass().getSimpleName() + " occurred while executing this command!";
            }
            context.getSender().sendMessage(_("core", message));
            t.printStackTrace(System.err); // TODO handle properly
            context.setResult(false);
        }
    }
    
    @Override
    public void showHelp(CommandContext context)
    {
        CommandSender sender = context.getSender();
        String commandLine = "/" + StringUtils.implode(" ", context.getLabels());
        
        
        
        sender.sendMessage(commandLine + " " + this.getUsage());
        
        sender.sendMessage(_(sender, "core", "Description: %s", _(sender, this.getModule().getName(), this.getDescription())));
        sender.sendMessage(_(sender, "core", "Aliases: %s", this.getAliases().isEmpty() ? _(sender, "core", "none") : StringUtils.implode(", ", this.getAliases())));
        
        
        if (this.hasChildren())
        {
            sender.sendMessage(_(sender, "core", "Sub commands:"));
            for (CubeCommand child : this.getChildren())
            {
                sender.sendMessage(commandLine + " " + child.getName());
            }
        }
    }
    
    private String generatePermissionNode()
    {
        String permission = "cubeengine." + this.getModule() + ".";
        
        LinkedList<String> cmds = new LinkedList<String>();
        CubeCommand cmd = this;
        do
        {
            cmds.add(cmd.getName());
        }
        while ((cmd = this.getParent()) != null);
        Collections.reverse(cmds);
        
        return permission + StringUtils.implode(".", cmds);
    }
}