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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.cubeisland.engine.command.context.CtxBuilder;
import de.cubeisland.engine.command.context.Group;
import de.cubeisland.engine.command.context.IndexedParameter;
import de.cubeisland.engine.command.context.NamedParameter;
import de.cubeisland.engine.command.context.ParameterGroup;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.context.CubeContext;
import de.cubeisland.engine.core.command.context.CubeContextFactory;
import de.cubeisland.engine.core.command.parameterized.CommandFlag;
import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexed;
import de.cubeisland.engine.core.command.parameterized.CommandParameterNamed;
import de.cubeisland.engine.command.Completer;
import de.cubeisland.engine.core.command.reflected.commandparameter.CommandParameters;
import de.cubeisland.engine.core.command.reflected.commandparameter.Description;
import de.cubeisland.engine.core.command.reflected.commandparameter.Optional;
import de.cubeisland.engine.core.command.reflected.commandparameter.ParamFlag;
import de.cubeisland.engine.core.command.reflected.commandparameter.ParamGroup;
import de.cubeisland.engine.core.command.reflected.commandparameter.ParamIndexed;
import de.cubeisland.engine.core.command.reflected.commandparameter.ParamNamed;
import de.cubeisland.engine.core.command.reflected.commandparameter.Required;
import de.cubeisland.engine.core.command.reflected.commandparameter.ValueLabel;
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

import static de.cubeisland.engine.core.command.reflected.ReflectedCommandFactory.SignatureType.*;

public class ReflectedCommandFactory
{
    protected enum SignatureType
    {
        INVALID, CONTEXT, CONTEXT_PARAMETER;
    }

    protected SignatureType validateSignature(Module module, Object holder, Method method)
    {
        Class<?>[] methodParams = method.getParameterTypes();

        if (methodParams.length >= 1 && CubeContext.class.isAssignableFrom(methodParams[0]))
        {
            if (methodParams.length == 1)
            {
                return CONTEXT;
            }
            else if (methodParams.length == 2 && CommandParameters.class.isAssignableFrom(methodParams[1]))
            {
                return CONTEXT_PARAMETER;
            }
        }

        module.getLog().warn("The method ''{}.{}'' does not match the required method signature: public void {}(CubeContext context)",
                             holder.getClass().getSimpleName(), method.getName(), method.getName());
        return INVALID;
    }

    @SuppressWarnings("unchecked")
    protected CubeCommand buildCommandWithParameter(Module module, Object holder, Method method, Command annotation)
    {
        Class<? extends CommandParameters> paramClass = (Class<? extends CommandParameters>)method.getParameterTypes()[1];

        FieldGroup index = new FieldGroup();
        FieldGroup named = new FieldGroup();
        FieldGroup flags = new FieldGroup();

        this.findParameters(paramClass, index, named, flags);

        CtxBuilder builder = new CtxBuilder().addIndexed(readIndexed(null, index)).addNamed(readNamed(null, named));
        for (Field field : flags.fieldMap.values())
        {
            ParamFlag fAnnot = field.getAnnotation(ParamFlag.class);
            // TODO perm
            CommandFlag commandFlag = new CommandFlag(fAnnot.value(), fAnnot.longName());
            builder.addFlag(commandFlag);
        }

        // TODO build cmd with CommandParameter in method signature
        // TODO named & flags
        return null;
    }

    private Group<NamedParameter> readNamed(Field aField, FieldGroup named)
    {
        ParameterGroup<NamedParameter> group = new ParameterGroup<>(aField == null || !aField.isAnnotationPresent(Optional.class));
        Group<NamedParameter> param;
        for (Field field : named.fieldMap.values())
        {
            if (named.subGroups.get(field) != null)
            {
                // Group
                param = readNamed(field, named.subGroups.get(field));
            }
            else
            {
                ParamNamed nAnnot = field.getAnnotation(ParamNamed.class);
                ValueLabel lAnnot = field.getAnnotation(ValueLabel.class);
                Description desc = field.getAnnotation(Description.class);
                boolean required = field.isAnnotationPresent(Required.class);

                Class type = field.getType();
                Class reader = field.getType();
                Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType && List.class.isAssignableFrom((Class)((ParameterizedType)genericType).getRawType()))
                {
                    reader = List.class;
                    type = (Class)((ParameterizedType)genericType).getActualTypeArguments()[0];
                }

                // TODO perm
                param = new CommandParameterNamed(nAnnot.value().isEmpty() ? field.getName() : nAnnot.value(), type, reader, 1,
                          required, lAnnot == null ? field.getName() : lAnnot.value(), desc == null ? null : desc.value(), null);

            }
            group.list().add(param);
        }
        return group;
    }

    private Group<IndexedParameter> readIndexed(Field aField, FieldGroup index)
    {
        ParameterGroup<IndexedParameter> group = new ParameterGroup<>(aField == null || !aField.isAnnotationPresent(Optional.class));
        Group<IndexedParameter> param;
        for (Field field : index.fieldMap.values())
        {
            if (index.subGroups.get(field) != null)
            {
                // Group
                param = readIndexed(field, index.subGroups.get(field));
            }
            else
            {
                // IndexedParameter
                // TODO Collection Fields extract GenericType for Type + set Reader to SimpleListReader by Default
                // TODO perm
                ParamIndexed iAnnot = field.getAnnotation(ParamIndexed.class);
                ValueLabel lAnnot = field.getAnnotation(ValueLabel.class);
                Description desc = field.getAnnotation(Description.class);
                String label = lAnnot == null ? field.getName() : lAnnot.value();
                boolean required = !field.isAnnotationPresent(Optional.class);
                Class type = field.getType();
                Class reader = field.getType();
                Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType && List.class.isAssignableFrom((Class)((ParameterizedType)genericType).getRawType()))
                {
                    reader = List.class;
                    type = (Class)((ParameterizedType)genericType).getActualTypeArguments()[0];
                }
                param = new CommandParameterIndexed(type, reader, iAnnot.greed(), required,
                                    label, desc == null ? null : desc.value(), null);
                // TODO Reader / Tabcompleter
            }
            group.list().add(param);
        }
        return group;
    }

    private void findParameters(Class paramClass, FieldGroup index, FieldGroup named, FieldGroup flags)
    {
        for (Field field : paramClass.getFields())
        {
            if (field.isAnnotationPresent(ParamIndexed.class))
            {
                if (ParamGroup.class.isAssignableFrom(field.getType()))
                {
                    FieldGroup indexSub = new FieldGroup();
                    this.findParameters(field.getType(), indexSub, null, null);
                    if (indexSub.fieldMap.isEmpty())
                    {
                        throw new IllegalArgumentException("Empty SubGroups are not allowed");
                    }
                    index.subGroups.put(field, indexSub);
                }
                Field old = index.fieldMap.put(field.getAnnotation(ParamIndexed.class).value(), field);
                if (old != null)
                {
                    throw new IllegalArgumentException("Duplicated order value in " + paramClass.getName());
                }
            }
            else if (field.isAnnotationPresent(ParamNamed.class))
            {
                if (ParamGroup.class.isAssignableFrom(field.getType()))
                {
                    FieldGroup namedSub = new FieldGroup();
                    this.findParameters(field.getType(), null, namedSub, null);
                    if (namedSub.fieldMap.isEmpty())
                    {
                        throw new IllegalArgumentException("Empty SubGroups are not allowed");
                    }
                    named.subGroups.put(field, namedSub);
                }
                named.fieldMap.put(named.fieldMap.size(), field);
            }
            else if (field.isAnnotationPresent(ParamFlag.class))
            {
                flags.fieldMap.put(flags.fieldMap.size(), field);
            }
        }
    }

    public static class FieldGroup
    {
        public Map<Integer, Field> fieldMap = new TreeMap<>();
        public Map<Field, FieldGroup> subGroups = new HashMap<>();
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

        ParameterGroup<IndexedParameter> indexedParams = new ParameterGroup<>();
        if (method.isAnnotationPresent(IParams.class))
        {
            for (Grouped arg : method.getAnnotation(IParams.class).value())
            {
                Indexed[] indexed = arg.value();
                if (indexed.length == 0)
                {
                    throw new IllegalArgumentException("You have to define at least one Indexed!");
                }
                Indexed aIndexed = indexed[0];
                CommandParameterIndexed indexedParam = new CommandParameterIndexed(aIndexed.type(), aIndexed.reader(),
                                                                                   arg.greedy() ? -1 : 1, aIndexed.req(),
                                                                                   aIndexed.label(), null, null);
                indexedParam.setCompleter(getCompleter(module, aIndexed.completer(), aIndexed.type()));

                if (indexed.length > 1)
                {
                    ParameterGroup<IndexedParameter> group = new ParameterGroup<>(arg.req());
                    group.list().add(indexedParam);
                    for (int i = 1; i < indexed.length; i++)
                    {
                        aIndexed = indexed[i];
                        indexedParam = new CommandParameterIndexed(aIndexed.type(), aIndexed.reader(), 1, aIndexed.req(),
                                                                   aIndexed.label(), null, null);
                        for (String staticValue : aIndexed.staticValues())
                        {
                            indexedParam.addStaticReader(staticValue, aIndexed.staticReader());
                        }
                        indexedParam.setCompleter(getCompleter(module, aIndexed.completer(), aIndexed.type()));
                        group.list().add(indexedParam);
                    }
                    indexedParams.list().add(group);
                }
                else
                {
                    indexedParams.list().add(indexedParam);
                }
            }
        }

        ParameterGroup<NamedParameter> namedParams = new ParameterGroup<>();
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
                final CommandParameterNamed cParam = new CommandParameterNamed(names[0], param.type(), param.reader(), 1, param.required(), param.label(), null, paramPerm);
                cParam.addAliases(paramAliases);
                cParam.withCompleter(getCompleter(module, param.completer(), param.type()));
                namedParams.list().add(cParam);
            }
        }
        Set<de.cubeisland.engine.command.context.Flag> flags = new HashSet<>();
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

        CubeContextFactory ctxFactory = new CubeContextFactory(new CtxBuilder().addIndexed(indexedParams).addNamed(namedParams).addFlags(flags).get());
        ReflectedCommand cmd = new ReflectedCommand(module, holder, method, name, cmdAnnot.desc(), ctxFactory, cmdPermission, checkPermission);

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
            CubeCommand command;
            switch (this.validateSignature(module, holder, method))
            {
                case CONTEXT:
                    command = this.buildCommand(module, holder, method, annotation);
                    break;
                case CONTEXT_PARAMETER:
                    command = this.buildCommandWithParameter(module, holder, method, annotation);
                    break;
                default:
                    continue;
            }
            if (command != null)
            {
                commands.add(command);
            }
        }

        return commands;
    }
}
