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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandFactory;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.parameterized.CommandFlag;
import de.cubeisland.engine.core.command.parameterized.CommandParameter;
import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexed;
import de.cubeisland.engine.core.command.parameterized.Completer;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContextFactory;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.permission.Permission;

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
        Set<String> aliases = new HashSet<>(commandNames.length - 1);
        for (int i = 1; i < commandNames.length; ++i)
        {
            aliases.add(commandNames[i].toLowerCase(Locale.ENGLISH));
        }

        String permNode = annotation.permNode();
        if (permNode.isEmpty())
        {
            permNode = name;
        }
        PermDefault permDefault = annotation.permDefault();
        if (!annotation.checkPerm())
        {
            permDefault = PermDefault.TRUE;
        }
        Permission permission = Permission.detachedPermission(permNode, permDefault);

        Set<CommandFlag> flags = new HashSet<>(annotation.flags().length);
        for (Flag flag : annotation.flags())
        {
            Permission flagPerm = null;
            if (!flag.permission().isEmpty())
            {
                flagPerm = permission.child(flag.permission(), flag.permDefault());
            }
            flags.add(new CommandFlag(flag.name(), flag.longName(), flagPerm));
        }

        Set<CommandParameter> params = new LinkedHashSet<>(annotation.params().length);
        for (Param param : annotation.params())
        {
            // TODO multivalue param
            // TODO greedy param (take args until next keyword)

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

            Permission paramPerm = null;
            if (!param.permission().isEmpty())
            {
                paramPerm = permission.child(param.permission(), param.permDefault());
            }
            final CommandParameter cParam = new CommandParameter(names[0], param.label(), param.type(), paramPerm);
            cParam.addAliases(paramAliases);
            cParam.setRequired(param.required());
            cParam.setCompleter(getCompleter(module, param.completer()));
            params.add(cParam);
        }

        List<CommandParameterIndexed> indexedParams = new ArrayList<>(annotation.indexed().length);
        int index = 0;
        for (Grouped arg : annotation.indexed())
        {
            Indexed[] indexed = arg.value();
            if (indexed.length == 0)
            {
                throw new IllegalArgumentException("You have to define at least one Indexed!");
            }
            Indexed aIndexed = indexed[0];
            String[] labels = aIndexed.value();
            if (labels.length == 0)
            {
                labels = new String[]{String.valueOf(index)};
            }

            int greed = indexed.length;
            if (arg.greedy())
            {
                greed = -1;
            }
            CommandParameterIndexed indexedParam = new CommandParameterIndexed(labels, aIndexed.type(), arg.req(), aIndexed.req(), greed);
            indexedParam.setCompleter(getCompleter(module, aIndexed.completer()));

            Set<String> staticLabels = new HashSet<>();
            for (String label : labels)
            {
                if (label.startsWith("!"))
                {
                    staticLabels.add(label.substring(1));
                }
            }

            if (!staticLabels.isEmpty())
            {
                indexedParam.setCompleter(new IndexedParameterCompleter(indexedParam.getCompleter(), staticLabels));
            }

            indexedParams.add(indexedParam);

            if (indexed.length > 1)
            {
                for (int i = 1; i < indexed.length; i++)
                {
                    index++;
                    aIndexed = indexed[i];
                    labels = aIndexed.value();
                    if (labels.length == 0)
                    {
                        labels = new String[]{String.valueOf(index)};
                    }
                    indexedParam = new CommandParameterIndexed(labels, aIndexed.type(), arg.req(), aIndexed.req(),0);
                    indexedParam.setCompleter(getCompleter(module, aIndexed.completer()));
                    indexedParams.add(indexedParam);
                }
            }
            index++;
        }
        ReflectedCommand cmd = new ReflectedCommand(module, holder, method, name, annotation.desc(),
                this.createContextFactory(indexedParams, flags, params), permission);

        cmd.setAliases(aliases);
        cmd.setLoggable(annotation.loggable());

        cmd.setAsynchronous(annotation.async());
        return (T)cmd;
    }

    private Completer getCompleter(Module module, Class<? extends Completer> completerClass)
    {
        if (completerClass == Completer.class)
        {
            return null;
        }
        try
        {
            return completerClass.newInstance();
        }
        catch (Exception ex)
        {
            module.getLog().error(ex, "Failed to create the completer '{}'", completerClass.getName());
            return null;
        }
    }

    protected ParameterizedContextFactory createContextFactory(List<CommandParameterIndexed> indexed, Set<CommandFlag> flags, Set<CommandParameter> params)
    {
        return new ParameterizedContextFactory(indexed, flags, params);
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
