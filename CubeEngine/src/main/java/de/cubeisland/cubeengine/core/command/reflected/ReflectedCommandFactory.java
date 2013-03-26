package de.cubeisland.cubeengine.core.command.reflected;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.cubeisland.cubeengine.core.command.ArgBounds;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandFactory;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.parameterized.CommandFlag;
import de.cubeisland.cubeengine.core.command.parameterized.CommandParameter;
import de.cubeisland.cubeengine.core.command.parameterized.Completer;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContextFactory;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.module.Module;

import static de.cubeisland.cubeengine.core.command.ArgBounds.NO_MAX;
import static de.cubeisland.cubeengine.core.logger.LogLevel.ERROR;
import static de.cubeisland.cubeengine.core.util.Misc.arr;

public class ReflectedCommandFactory<T extends CubeCommand> implements CommandFactory<T>
{
    public Class<T> getCommandType()
    {
        return (Class<T>)ReflectedCommand.class;
    }

    protected Class<? extends Annotation> getAnnotationType()
    {
        return Command.class;
    }

    protected boolean validateSignature(Module module, Object holder, Method method)
    {
        Class<?>[] methodParams = method.getParameterTypes();
        if (methodParams.length != 1 || !CommandContext.class.isAssignableFrom(methodParams[0]))
        {
            module.getLogger().log(LogLevel.WARNING, "The method ''{0}.{1}'' does not match the required method signature: public void {2}(CommandContext context)", arr(holder.getClass().getSimpleName(), method.getName(), method.getName()));
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    protected T buildCommand(Module module, Object holder, Method method, Annotation rawAnnotation)
    {
        Command annotation = (Command)rawAnnotation;

        String[] commandNames = annotation.names();
        if (commandNames.length == 0)
        {
            commandNames = new String[] {
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
            final CommandParameter commandParameter = new CommandParameter(names[0], param.type());
            commandParameter.addAliases(paramAliases);
            commandParameter.setRequired(param.required());

            Class<? extends Completer> completerClass = param.completer();
            if (completerClass != Completer.class)
            {
                try
                {
                    commandParameter.setCompleter(completerClass.newInstance());
                }
                catch (Exception e)
                {
                    module.getLogger().log(ERROR, "Failed to create the completer '" + completerClass.getName() + "'", e);
                }
            }

            params.add(commandParameter);
        }

        if (annotation.max() > NO_MAX && annotation.max() < annotation.min())
        {
            module.getLogger().log(ERROR, "{0}.{1}: The the maximum args must not be less than the minimum", arr(holder.getClass().getSimpleName(), method.getName()));
            return null;
        }
        ReflectedCommand cmd = new ReflectedCommand(
            module,
            holder,
            method,
            name,
            annotation.desc(),
            annotation.usage(),
            aliases,
            this.createContextFactory(new ArgBounds(annotation.min(), annotation.max()), flags, params)
        );
        cmd.setLoggable(annotation.loggable());
        if (annotation.checkPerm())
        {
            String node = annotation.permNode();
            if (node == null || node.isEmpty())
            {
                cmd.setGeneratedPermissionDefault(annotation.permDefault());
            }
            else
            {
                module.getCore().getPermissionManager().registerPermission(module, node, annotation.permDefault(), null, null, true);
                cmd.setPermission(node);
            }
        }
        return (T)cmd;
    }

    protected ParameterizedContextFactory createContextFactory(ArgBounds bounds, Set<CommandFlag> flags, Set<CommandParameter> params)
    {
        return new ParameterizedContextFactory(bounds, flags, params);
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
            if (!this.validateSignature(module, holder, method))
            {
                continue;
            }

            T command = this.buildCommand(module, holder, method, annotation);
            if (command != null)
            {
                commands.add(command);
            }
        }

        return commands;
    }
}
