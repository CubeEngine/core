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
package de.cubeisland.engine.core.command;

import java.lang.annotation.Annotation;

import de.cubeisland.engine.command.CommandDescriptor;
import de.cubeisland.engine.command.ImmutableCommandDescriptor;
import de.cubeisland.engine.command.filter.Filters;
import de.cubeisland.engine.command.methodic.Command;
import de.cubeisland.engine.command.methodic.parametric.BasicParametricCommand;
import de.cubeisland.engine.command.methodic.parametric.ParametricBuilder;
import de.cubeisland.engine.command.parameter.Parameter;
import de.cubeisland.engine.command.parameter.property.Required;
import de.cubeisland.engine.core.command.annotation.CommandPermission;
import de.cubeisland.engine.core.command.annotation.ParameterPermission;
import de.cubeisland.engine.core.command.property.PermissionProvider;
import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.permission.Permission;

import static de.cubeisland.engine.core.command.property.CheckPermission.CHECK;
import static de.cubeisland.engine.core.command.property.CheckPermission.NOT_CHECK;

public class ParametricCommandBuilder extends ParametricBuilder<CommandOrigin>
{
    public ParametricCommandBuilder()
    {
        this.usageGenerator = new CommandUsageGenerator();
    }

    @Override
    protected Parameter createParameter(CommandDescriptor descriptor, Class<?> clazz, Annotation[] annotations,
                                              CommandOrigin origin)
    {
        Parameter parameter = super.createParameter(descriptor, clazz, annotations, origin);

        for (Annotation annotation : annotations)
        {
            if (annotation instanceof ParameterPermission)
            {
                if (parameter.valueFor(Required.class))
                {
                    throw new IllegalArgumentException("A Parameter cannot be required and have a permission"); // TODO custom execption
                }
                Permission paramPerm = descriptor.valueFor(PermissionProvider.class).child(
                    ((ParameterPermission)annotation).value(), ((ParameterPermission)annotation).permDefault());
                parameter.setProperty(new PermissionProvider(paramPerm));
            }
        }

        return parameter;
    }

    @Override
    protected ImmutableCommandDescriptor buildCommandDescriptor(Command annotation, CommandOrigin origin)
    {
        ImmutableCommandDescriptor descriptor = super.buildCommandDescriptor(annotation, origin);

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
        Permission permission = origin.getModule().getBasePermission().childWildcard("command").child(permName, def);
        descriptor.setProperty(new PermissionProvider(permission));
        descriptor.setProperty(checkPerm ? CHECK : NOT_CHECK);
        if (checkPerm)
        {
            descriptor.valueFor(Filters.class).addFilter(new PermissionFilter(permission));
        }

        return descriptor;
    }

    @Override
    protected BasicParametricCommand build(Command annotation, CommandOrigin origin)
    {
        ImmutableCommandDescriptor descriptor = buildCommandDescriptor(annotation, origin);
        descriptor.setProperty(buildParameters(descriptor, origin));
        descriptor.setProperty(new ModuleProvider(origin.getModule()));
        ParametricCommand cmd = new ParametricCommand(descriptor);
        cmd.addCommand(new HelpCommand(cmd));
        return cmd;
    }
}
