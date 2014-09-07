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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.cubeisland.engine.command.Completer;
import de.cubeisland.engine.command.context.CtxBuilder;
import de.cubeisland.engine.command.context.Group;
import de.cubeisland.engine.command.context.parameter.IndexedParameter;
import de.cubeisland.engine.command.context.parameter.NamedParameter;
import de.cubeisland.engine.command.context.parameter.ParameterGroup;
import de.cubeisland.engine.command.methodbased.Command;
import de.cubeisland.engine.command.methodbased.Restricted;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.ReflectedMethodCommandFactory;
import de.cubeisland.engine.core.command.context.CubeContext;
import de.cubeisland.engine.core.command.context.CubeContextFactory;
import de.cubeisland.engine.core.command.parameterized.PermissibleFlag;
import de.cubeisland.engine.core.command.parameterized.PermissibleIndexedParameter;
import de.cubeisland.engine.core.command.parameterized.PermissibleNamedParameter;
import de.cubeisland.engine.core.command.reflected.commandparameter.CommandParameters;
import de.cubeisland.engine.core.command.reflected.commandparameter.Description;
import de.cubeisland.engine.core.command.reflected.commandparameter.Optional;
import de.cubeisland.engine.core.command.reflected.commandparameter.ParamFlag;
import de.cubeisland.engine.core.command.reflected.commandparameter.ParamGroup;
import de.cubeisland.engine.core.command.reflected.commandparameter.ParamIndexed;
import de.cubeisland.engine.core.command.reflected.commandparameter.ParamNamed;
import de.cubeisland.engine.core.command.reflected.commandparameter.Required;
import de.cubeisland.engine.core.command.reflected.commandparameter.ValueLabel;
import de.cubeisland.engine.core.module.Module;

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
            PermissibleFlag commandFlag = new PermissibleFlag(fAnnot.value(), fAnnot.longName());
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
                param = new PermissibleNamedParameter(nAnnot.value().isEmpty() ? field.getName() : nAnnot.value(), type, reader, 1,
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
                param = new PermissibleIndexedParameter(type, reader, iAnnot.greed(), required,
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
