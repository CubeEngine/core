package de.cubeisland.cubeengine.core.command.reflected;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.HelpContext;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedCommand;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContextFactory;
import de.cubeisland.cubeengine.core.command.result.ErrorResult;
import de.cubeisland.cubeengine.core.module.Module;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ReflectedCommand extends ParameterizedCommand
{
    private final Object holder;
    private final Method method;
    private final Class<? extends CommandContext> contextType;

    public ReflectedCommand(Module module, Object holder, Method method, String name, String description, String usage, List<String> aliases, ParameterizedContextFactory factory)
    {
        super(module, name, description, usage, aliases, factory);

        this.holder = holder;
        this.method = method;
        this.method.setAccessible(true);
        this.contextType = (Class<? extends CommandContext>)method.getParameterTypes()[0];

        Alias annotation = method.getAnnotation(Alias.class);
        if (annotation != null)
        {
            this.registerAlias(annotation.names(), annotation.parents(), annotation.prefix(), annotation.suffix());
        }
    }

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        if (this.contextType.isInstance(context))
        {
            try
            {
                Object result = this.method.invoke(this.holder, context);
                if (result instanceof CommandResult)
                {
                    return (CommandResult)result;
                }
            }
            catch (InvocationTargetException e)
            {
                if (e.getCause() instanceof Exception)
                {
                    throw (Exception)e.getCause();
                }
                return new ErrorResult(e);
            }
        }
        return null;
    }

    @Override
    public void help(HelpContext context) throws Exception
    {
        context.sendTranslated("Description: &f%s", this.getDescription());
        context.sendTranslated("Usage: &f%s", this.getUsage(context));
    }
}
