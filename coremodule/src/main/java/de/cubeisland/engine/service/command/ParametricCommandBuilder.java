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
package de.cubeisland.engine.service.command;

import java.lang.annotation.Annotation;
import de.cubeisland.engine.butler.filter.Filters;
import de.cubeisland.engine.butler.parametric.Command;
import de.cubeisland.engine.butler.parametric.BasicParametricCommand;
import de.cubeisland.engine.butler.parametric.ParametricBuilder;
import de.cubeisland.engine.butler.parameter.Parameter;
import de.cubeisland.engine.service.command.annotation.CommandPermission;
import de.cubeisland.engine.service.command.annotation.ParameterPermission;
import de.cubeisland.engine.service.command.annotation.Unloggable;
import de.cubeisland.engine.service.command.property.PermissionProvider;
import de.cubeisland.engine.service.command.property.RawPermission;

import static de.cubeisland.engine.butler.parameter.property.Requirement.isRequired;

public class ParametricCommandBuilder extends ParametricBuilder<CommandOrigin, CubeCommandDescriptor>
{
    public ParametricCommandBuilder()
    {
        super(new CommandUsageGenerator());
    }

    @Override
    protected Parameter createParameter(CubeCommandDescriptor descriptor, Class<?> clazz, Annotation[] annotations,
                                        CommandOrigin origin, Object javaParameter)
    {
        Parameter parameter = super.createParameter(descriptor, clazz, annotations, origin, javaParameter);

        for (Annotation annotation : annotations)
        {
            if (annotation instanceof ParameterPermission)
            {
                if (isRequired(parameter))
                {
                    throw new IllegalArgumentException("A Parameter cannot be required and have a permission"); // TODO custom execption
                }
                ParameterPermission annot = (ParameterPermission)annotation;
                PermissionProvider provider = new PermissionProvider(new RawPermission(annot.value(), annot.desc()));
                parameter.setProperty(provider);

                Filters filters = parameter.valueFor(Filters.class);
                if (filters == null)
                {
                    filters = new Filters();
                    parameter.setProperty(filters);
                }
                filters.addFilter(new PermissionFilter(provider.value()));
            }
        }

        return parameter;
    }

    @Override
    protected CubeCommandDescriptor newDescriptor()
    {
        return new CubeCommandDescriptor();
    }

    @Override
    protected CubeCommandDescriptor fillDescriptor(CubeCommandDescriptor descriptor, Command annotation,
                                                   CommandOrigin origin)
    {
        super.fillDescriptor(descriptor, annotation, origin);

        String permName = descriptor.getName();
        String permDesc = null;
        boolean checkPerm = true;
        CommandPermission perm = this.getClass().getAnnotation(CommandPermission.class);
        if (perm != null)
        {
            permName = perm.value().isEmpty() ? permName : perm.value();
            permDesc = perm.desc().isEmpty() ? null : perm.desc();
            checkPerm = perm.checkPermission();
        }

        descriptor.setPermission(new RawPermission(permName + ".use", permDesc), checkPerm);

        if (checkPerm)
        {
            descriptor.addFilter(new PermissionFilter(descriptor.getPermission()));
        }

        descriptor.setLoggable(!origin.getMethod().isAnnotationPresent(Unloggable.class));

        descriptor.setModule(origin.getModule());
        return descriptor;
    }

    @Override
    protected BasicParametricCommand build(Command annotation, CommandOrigin origin)
    {
        BasicParametricCommand command = super.build(annotation, origin);
        command.addCommand(new HelpCommand(command));
        return command;
    }
}
