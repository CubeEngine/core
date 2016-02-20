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
package org.cubeengine.service.command;

import java.lang.annotation.Annotation;
import org.cubeengine.butler.builder.DescriptorFiller;
import org.cubeengine.butler.parameter.property.Filters;
import org.cubeengine.butler.parameter.property.Properties;
import org.cubeengine.butler.parametric.BasicParametricCommand;
import org.cubeengine.butler.parametric.Command;
import org.cubeengine.butler.parametric.InvokableMethod;
import org.cubeengine.butler.parametric.ParametricCommandDescriptor;
import org.cubeengine.butler.parametric.builder.ParametricBuilder;
import org.cubeengine.butler.parametric.builder.parameter.ParametricParametersFiller;
import org.cubeengine.service.command.annotation.CommandPermission;
import org.cubeengine.service.command.annotation.ParameterPermission;
import org.cubeengine.service.command.annotation.Unloggable;
import org.cubeengine.service.command.property.RawPermission;
import org.cubeengine.service.i18n.I18n;

import static org.cubeengine.butler.parameter.property.Requirement.isRequired;

public class ParametricCommandBuilder extends ParametricBuilder
{
    private I18n i18n;

    public ParametricCommandBuilder(I18n i18n)
    {
        super(CubeCommandDescriptor::new, new CommandUsageGenerator(i18n));
        this.i18n = i18n;

        getParameterFiller().addFiller((parameter, type, annotations) -> {
            for (Annotation annotation : annotations)
            {
                if (annotation instanceof ParameterPermission)
                {
                    if (isRequired(parameter))
                    {
                        throw new IllegalArgumentException("A Parameter cannot be required and have a permission"); // TODO custom execption
                    }
                    ParameterPermission annot = (ParameterPermission)annotation;
                    RawPermission value = new RawPermission(annot.value(), annot.desc());
                    parameter.offer(RawPermission.class, value);
                    Filters filters = parameter.getProperty(Properties.FILTERS);
                    if (filters == null)
                    {
                        filters = new Filters();
                        parameter.offer(Properties.FILTERS, filters);
                    }
                    filters.addFilter(new PermissionFilter(value));
                    return;
                }
            }
        });

        this.addFiller((descriptor, origin) -> {
            String permName = descriptor.getName();
            String permDesc = null;
            boolean checkPerm = true;
            String[] group = null;
            CommandPermission perm = origin.getMethod().getAnnotation(CommandPermission.class);
            if (perm != null)
            {
                permName = perm.value().isEmpty() ? permName : perm.value();
                permDesc = perm.desc().isEmpty() ? "Allows using the command " + descriptor.getName() : perm.desc();
                checkPerm = perm.checkPermission();
                group = perm.group();
            }

            CubeCommandDescriptor cDescriptor = (CubeCommandDescriptor) descriptor;

            cDescriptor.setPermission(new RawPermission(permName + ".use", permDesc).assign(group), checkPerm);

            if (checkPerm)
            {
                descriptor.addFilter(new PermissionFilter(cDescriptor.getPermission()));
            }

            cDescriptor.setLoggable(!origin.getMethod().isAnnotationPresent(Unloggable.class));

            cDescriptor.setModule(((CommandOrigin) origin).getModule());
        });
    }

    @Override
    protected BasicParametricCommand build(ParametricCommandDescriptor descriptor)
    {
        BasicParametricCommand command = super.build(descriptor);
        command.addCommand(new HelpCommand(command, i18n));
        return command;
    }
}
