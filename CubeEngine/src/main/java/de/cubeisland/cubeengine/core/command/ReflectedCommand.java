package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.util.StringUtils;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import static de.cubeisland.cubeengine.core.i18n.I18n._;

/**
 * The reflected command invokes a method that being parsed from a class
 */
public class ReflectedCommand extends CubeCommand
{
    private final Object commandContainer;
    private final Method commandMethod;
    private final int min;
    private final int max;
    private final boolean checkPermision;
    private final String permissionNode;
    private final PermDefault permissionDefault;
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
            module.getCore().getPermissionManager().registerPermission(module, this.permissionNode, this.permissionDefault);
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
    public void run(CommandContext context) throws Exception
    {
        if (context.indexedCount() < this.min)
        {
            invalidUsage(context, "core", "&cToo few parameters! Minimum is &e%d&c.", this.min);
        }
        else
        {
            if (this.max > -1 && context.indexedCount() > this.max)
            {
                invalidUsage(context, "core", "&cToo many parameters! Maximum is &e%d&c.", this.max);
            }
        }
        if (this.checkPermision && !context.getSender().hasPermission(this.permissionNode))
        {
            denyAccess(context, "core", "&cYou are not allowed to do this.");
        }

        try
        {
            this.commandMethod.invoke(this.commandContainer, context);
        }
        catch (InvocationTargetException e)
        {
            if (e.getCause() instanceof Exception)
            {
                throw (Exception)e.getCause();
            }
            throw e;
        }
    }

    @Override
    public void showHelp(CommandContext context)
    {
        CommandSender sender = context.getSender();
        context.sendMessage(this.getUsage(sender));

        context.sendMessage("core", "&eDescription: &f%s", _(sender, this.getModule().getId(), this.getDescription()));

        List<String> aliases = this.getAliases();
        if (!aliases.isEmpty())
        {
            context.sendMessage("core", "&eAliases: &f%s", "/" + StringUtils.implode(", /", aliases));
        }

        if (this.hasChildren())
        {
            context.sendMessage("core", "Sub commands:");
            for (CubeCommand child : this.getChildren())
            {
                context.sendMessage(child.getModule().getId(), child.getUsage(context.getSender(), context.getLabels()));
            }
        }
    }

    private String generatePermissionNode()
    {
        return "cubeengine." + this.getModule().getId() + ".command." + this.implodeCommandPathNames(".");
    }

    @Override
    public Flag[] getFlags()
    {
        return this.flags;
    }

    @Override
    public Param[] getParams()
    {
        return this.params;
    }
}
