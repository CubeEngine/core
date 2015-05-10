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
package de.cubeisland.engine.module.core.command;

import java.lang.annotation.Annotation;
import de.cubeisland.engine.butler.parametric.Command;
import de.cubeisland.engine.butler.parametric.BasicParametricCommand;
import de.cubeisland.engine.butler.parametric.ParametricBuilder;
import de.cubeisland.engine.butler.parameter.Parameter;
import de.cubeisland.engine.module.core.command.annotation.CommandPermission;
import de.cubeisland.engine.module.core.command.annotation.ParameterPermission;
import de.cubeisland.engine.module.core.command.annotation.Unloggable;
import de.cubeisland.engine.module.core.command.property.PermissionProvider;
import de.cubeisland.engine.module.core.permission.PermDefault;
import de.cubeisland.engine.module.core.permission.Permission;

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
                Permission paramPerm = descriptor.getPermission().child(annot.value(), annot.permDefault());
                parameter.setProperty(new PermissionProvider(paramPerm));
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
        boolean checkPerm = true;
        PermDefault def = PermDefault.DEFAULT;
        CommandPermission perm = this.getClass().getAnnotation(CommandPermission.class);
        if (perm != null)
        {
            permName = perm.value().isEmpty() ? permName : perm.value();
            def = perm.permDefault();
            checkPerm = perm.checkPermission();
        }
        Permission permission = origin.getModule().getProvided(Permission.class).childWildcard("command").child(permName, def);

        descriptor.setPermission(permission, checkPerm);

        if (checkPerm)
        {
            descriptor.addFilter(new PermissionFilter(permission));
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
