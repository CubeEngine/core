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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.CubeContext;
import de.cubeisland.engine.core.command.CubeContextFactory;
import de.cubeisland.engine.core.command.parameterized.CommandFlag;
import de.cubeisland.engine.core.command.parameterized.CommandParameter;
import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexed;
import de.cubeisland.engine.core.command.parameterized.Completer;
import de.cubeisland.engine.core.command.reflected.context.Flag;
import de.cubeisland.engine.core.command.reflected.context.Flags;
import de.cubeisland.engine.core.command.reflected.context.Grouped;
import de.cubeisland.engine.core.command.reflected.context.IParams;
import de.cubeisland.engine.core.command.reflected.context.Indexed;
import de.cubeisland.engine.core.command.reflected.context.NParams;
import de.cubeisland.engine.core.command.reflected.context.Named;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.permission.Permission;

public class ReflectedCommandFactory
{
    protected boolean validateSignature(Module module, Object holder, Method method)
    {
        Class<?>[] methodParams = method.getParameterTypes();
        if (methodParams.length != 1 || !CubeContext.class.isAssignableFrom(methodParams[0]))
        {
            module.getLog().warn("The method ''{}.{}'' does not match the required method signature: public void {}(CubeContext context)",
                                 holder.getClass().getSimpleName(), method.getName(), method.getName());
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    protected CubeCommand buildCommand(Module module, Object holder, Method method, Command cmdAnnot)
    {
        String name = method.getName();
        if (!cmdAnnot.name().isEmpty())
        {
            name = cmdAnnot.name();
        }
        name = name.trim().toLowerCase();
        Set<String> aliases = new HashSet<>();
        for (String alias : cmdAnnot.alias())
        {
            aliases.add(alias.trim().toLowerCase());
        }

        String permNode = name;
        PermDefault permDefault = PermDefault.DEFAULT;
        boolean checkPermission = true;
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
        Permission cmdPermission = Permission.detachedPermission(permNode, permDefault);

        List<CommandParameterIndexed> indexedParams = new ArrayList<>();
        if (method.isAnnotationPresent(IParams.class))
        {
            int index = 0;
            for (Grouped arg : method.getAnnotation(IParams.class).value())
            {
                Indexed[] indexed = arg.value();
                if (indexed.length == 0)
                {
                    throw new IllegalArgumentException("You have to define at least one Indexed!");
                }
                Indexed aIndexed = indexed[0];
                String[] labels = aIndexed.label();
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
                indexedParam.setCompleter(getCompleter(module, aIndexed.completer(), aIndexed.type()));

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
                        labels = aIndexed.label();
                        if (labels.length == 0)
                        {
                            labels = new String[]{String.valueOf(index)};
                        }
                        indexedParam = new CommandParameterIndexed(labels, aIndexed.type(), arg.req(), aIndexed.req(),0);
                        indexedParam.setCompleter(getCompleter(module, aIndexed.completer(), aIndexed.type()));
                        indexedParams.add(indexedParam);
                    }
                }
                index++;
            }
        }

        Set<CommandParameter> params = new LinkedHashSet<>();
        if (method.isAnnotationPresent(NParams.class))
        {
            for (Named param : method.getAnnotation(NParams.class).value())
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
                    paramPerm = cmdPermission.child(param.permission(), param.permDefault());
                }
                final CommandParameter cParam = new CommandParameter(names[0], param.label(), param.type(), paramPerm);
                cParam.addAliases(paramAliases);
                cParam.setRequired(param.required());
                cParam.setCompleter(getCompleter(module, param.completer(), param.type()));
                params.add(cParam);
            }
        }
        Set<CommandFlag> flags = new HashSet<>();
        if (method.isAnnotationPresent(Flags.class))
        {
            for (Flag flag : method.getAnnotation(Flags.class).value())
            {
                Permission flagPerm = null;
                if (!flag.permission().isEmpty())
                {
                    flagPerm = cmdPermission.child(flag.permission(), flag.permDefault());
                }
                flags.add(new CommandFlag(flag.name(), flag.longName(), flagPerm));
            }
        }

        ReflectedCommand cmd = new ReflectedCommand(module, holder, method, name, cmdAnnot.desc(),
                this.createContextFactory(indexedParams, params, flags), cmdPermission, checkPermission);

        cmd.setAliases(aliases);
        cmd.setLoggable(!method.isAnnotationPresent(Unloggable.class));
        cmd.setAsynchronous(method.isAnnotationPresent(CallAsync.class));

        if (method.isAnnotationPresent(OnlyIngame.class))
        {
            cmd.setOnlyIngame(method.getAnnotation(OnlyIngame.class).value());
        }

        return cmd;
    }

    private Completer getCompleter(Module module, Class<? extends Completer> completerClass, Class... types)
    {
        if (completerClass == Completer.class)
        {
            return module.getCore().getCommandManager().getDefaultCompleter(types);
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

    protected CubeContextFactory createContextFactory(List<CommandParameterIndexed> indexed,
                                                      Set<CommandParameter> named, Set<CommandFlag> flags)
    {
        CubeContextFactory ctxFactory = new CubeContextFactory();
        ctxFactory.addIndexed(indexed);
        ctxFactory.addNamed(named);
        ctxFactory.addFlags(flags);
        ctxFactory.calculateArgBounds();
        return ctxFactory;
    }

    public List<CubeCommand> parseCommands(Module module, Object holder)
    {
        List<CubeCommand> commands = new ArrayList<>();

        for (Method method : holder.getClass().getDeclaredMethods())
        {
            if (Modifier.isStatic(method.getModifiers()))
            {
                continue;
            }

            Command annotation = method.getAnnotation(Command.class);
            if (annotation == null)
            {
                continue;
            }
            if (!this.validateSignature(module, holder, method))
            {
                continue;
            }

            CubeCommand command = this.buildCommand(module, holder, method, annotation);
            if (command != null)
            {
                commands.add(command);
            }
        }

        return commands;
    }
}
