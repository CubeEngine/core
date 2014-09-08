package de.cubeisland.engine.core.command;

import java.lang.reflect.Method;

import de.cubeisland.engine.command.CommandRunner;
import de.cubeisland.engine.command.base.method.MethodCommandBuilder;
import de.cubeisland.engine.command.base.method.MethodCommandRunner;
import de.cubeisland.engine.command.context.ContextFactory;
import de.cubeisland.engine.command.context.CtxDescriptor;
import de.cubeisland.engine.core.command.context.CubeContext;
import de.cubeisland.engine.core.command.context.CubeContextFactory;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.CallAsync;
import de.cubeisland.engine.core.command.reflected.CommandPermission;
import de.cubeisland.engine.core.command.reflected.Unloggable;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.permission.Permission;

import static de.cubeisland.engine.core.permission.Permission.detachedPermission;

public class ReflectedMethodCommandFactory<CmdT extends CubeCommand> extends MethodCommandBuilder<CmdT>
{
    public ReflectedMethodCommandFactory(Class<CmdT> clazz)
    {
        super(clazz);
    }

    @Override
    protected CubeContextFactory newCtxFactory(CtxDescriptor descriptor)
    {
        return new CubeContextFactory(descriptor);
    }

    @Override
    public ReflectedMethodCommandFactory<CmdT> build(MethodCommandRunner runner)
    {
        super.build(runner);
        String permNode = cmd().getName();
        PermDefault permDefault = PermDefault.DEFAULT;
        boolean checkPermission = true;
        Method method = method(runner);
        if (method.isAnnotationPresent(CommandPermission.class))
        {
            CommandPermission permAnnot = method.getAnnotation(CommandPermission.class);
            if (!permAnnot.value().isEmpty())
            {
                permNode = permAnnot.value();
            }
            permDefault = permAnnot.permDefault();
            checkPermission = permAnnot.checkPermission();
        }
        Permission cmdPermission = detachedPermission(permNode, permDefault);

        setPermission(cmdPermission, checkPermission);
        cmd().loggable = !method.isAnnotationPresent(Unloggable.class);
        cmd().asynchronous = method.isAnnotationPresent(CallAsync.class);

        // TODO
        Alias aliasAnnot = method.getAnnotation(Alias.class);
        if (aliasAnnot != null)
        {
            cmd().registerAlias(aliasAnnot.names(), aliasAnnot.parents(), aliasAnnot.prefix(), aliasAnnot.suffix());
        }

        // TODO @Restricted Annotation

        return this;
    }

    public ReflectedMethodCommandFactory buildCommand(Module module, Object holder, Method method)
    {
        return this.build(new MethodCommandRunner<CubeContext>(holder, method)).setModule(module);
    }

    protected void setPermission(Permission permission, boolean checkPerm)
    {
        cmd().permission = permission;
        cmd().checkperm = checkPerm;
    }

    @Override
    public void init(String name, String description, ContextFactory ctxFactory, CommandRunner runner)
    {
        if ("?".equals(name) && !HelpCommand.class.isAssignableFrom(this.getClass()))
        {
            throw new IllegalArgumentException("Invalid command name: " + name);
        }
        super.init(name, description, ctxFactory, runner);
    }

    protected ReflectedMethodCommandFactory setModule(Module module)
    {
        cmd().module = module;
        return this;
    }
}
