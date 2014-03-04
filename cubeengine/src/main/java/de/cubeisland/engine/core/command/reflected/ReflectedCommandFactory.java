/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.command.reflected;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.cubeisland.engine.core.command.ArgBounds;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandFactory;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.parameterized.CommandFlag;
import de.cubeisland.engine.core.command.parameterized.CommandParameter;
import de.cubeisland.engine.core.command.parameterized.Completer;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContextFactory;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;

import static de.cubeisland.engine.core.command.ArgBounds.NO_MAX;

public class ReflectedCommandFactory<T extends CubeCommand> implements CommandFactory<T>
{
    @SuppressWarnings("unchecked")
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
            module.getLog().warn("The method ''{}.{}'' does not match the required method signature: public void {}(CommandContext context)",
                                 holder.getClass().getSimpleName(), method.getName(), method.getName());
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
        List<String> aliases = new ArrayList<>(commandNames.length - 1);
        for (int i = 1; i < commandNames.length; ++i)
        {
            aliases.add(commandNames[i].toLowerCase(Locale.ENGLISH));
        }

        Set<CommandFlag> flags = new HashSet<>(annotation.flags().length);
        for (Flag flag : annotation.flags())
        {
            flags.add(new CommandFlag(flag.name(), flag.longName()));
        }

        Set<CommandParameter> params = new HashSet<>(annotation.params().length);
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
                paramAliases = Arrays.copyOfRange(names, 1, names.length);
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
                catch (Exception ex)
                {
                    module.getLog().error(ex, "Failed to create the completer '{}'", completerClass.getName());
                }
            }

            params.add(commandParameter);
        }

        if (annotation.max() > NO_MAX && annotation.max() < annotation.min())
        {
            module.getLog().error("{}.{}: The the maximum args must not be less than the minimum",
                                  holder.getClass().getSimpleName(), method.getName());
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
            if (node.isEmpty())
            {
                cmd.setGeneratedPermissionDefault(annotation.permDefault());
            }
            else
            {
                Permission perm = module.getBasePermission().childWildcard("command").child(node, annotation.permDefault());
                module.getCore().getPermissionManager().registerPermission(module, perm);
                cmd.setPermission(perm.getName());
            }
        }
        cmd.setAsynchronous(annotation.async());
        return (T)cmd;
    }

    protected ParameterizedContextFactory createContextFactory(ArgBounds bounds, Set<CommandFlag> flags, Set<CommandParameter> params)
    {
        return new ParameterizedContextFactory(bounds, flags, params);
    }

    @Override
    public List<T> parseCommands(Module module, Object holder)
    {
        List<T> commands = new ArrayList<>();

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
