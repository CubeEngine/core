package de.cubeisland.cubeengine.core.command.reflected;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandFactory;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.parameterized.CommandFlag;
import de.cubeisland.cubeengine.core.command.parameterized.CommandParameter;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.module.Module;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Logger;

import static de.cubeisland.cubeengine.core.util.Misc.arr;

public class ReflectedCommandFactory<T extends CubeCommand> implements CommandFactory<T>
{
    private static final Logger LOGGER = CubeEngine.getLogger();

    public Class<T> getCommandType()
    {
        return (Class<T>)ReflectedCommand.class;
    }

    protected Class<? extends Annotation> getAnnotationType()
    {
        return Command.class;
    }

    protected boolean validateSignature(Object holder, Method method)
    {
        Class<?>[] methodParams = method.getParameterTypes();
        if (methodParams.length != 1 || !CommandContext.class.isAssignableFrom(methodParams[0]))
        {
            LOGGER.log(LogLevel.WARNING, "The method ''{0}.{1}'' does not match the required method signature: public void {2}(CommandContext context)", arr(holder.getClass().getSimpleName(), method.getName(), method.getName()));
            return false;
        }
        return true;
    }

    protected T buildCommand(Module module, Object holder, Method method, Annotation rawAnnotation)
    {
        Command annotation = (Command)rawAnnotation;


        String[] commandNames = annotation.names();
        if (commandNames.length == 0)
        {
            commandNames = new String[]
                {
                    method.getName()
                };
        }

        String name = commandNames[0].trim().toLowerCase(Locale.ENGLISH);
        List<String> aliases = new ArrayList<String>(commandNames.length - 1);
        for (int i = 1; i < commandNames.length; ++i)
        {
            aliases.add(commandNames[i].toLowerCase(Locale.ENGLISH));
        }

        Set<CommandFlag> flags = new HashSet<CommandFlag>(annotation.flags().length);
        for (Flag flag : annotation.flags())
        {
            flags.add(new CommandFlag(flag.name(), flag.longName()));
        }

        Set<CommandParameter> params = new HashSet<CommandParameter>(annotation.params().length);
        for (Param param : annotation.params())
        {
            String[] names = param.names();
            if (names.length < 1)
            {
                continue;
            }
            String[] paramAliases;
            if (names.length > 1)
            {
                paramAliases = Arrays.copyOfRange(names, 1, names.length - 1);
            }
            else
            {
                paramAliases = new String[0];
            }
            params.add(new CommandParameter(names[0], paramAliases, param.type(), param.required()));
        }

        ReflectedCommand cmd = new ReflectedCommand(
            module,
            holder,
            method,
            name,
            annotation.desc(),
            annotation.usage(),
            aliases,
            flags,
            params
        );
        cmd.setAsync(annotation.async());
        cmd.setLoggable(annotation.loggable());
        return (T)cmd;
    }
    
    @Override
    public List<T> parseCommands(Module module, Object holder)
    {
        List<T> commands = new ArrayList<T>();

        for (Method method : holder.getClass().getDeclaredMethods())
        {
            if (Modifier.isStatic(method.getModifiers()))
            {
                continue;
            }

            Annotation annotation = method.getAnnotation(this.getAnnotationType());
            if (annotation == null)
            {
                continue;
            }
            if (!this.validateSignature(holder, method))
            {
                continue;
            }

            commands.add(this.buildCommand(module, holder, method, annotation));
        }

        return commands;
    }
}
