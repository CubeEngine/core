/*
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
package org.cubeengine.libcube.service.command;

import java.util.Locale;
import org.cubeengine.butler.CommandDescriptor;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.parameter.Parameter;
import org.cubeengine.butler.parameter.ParameterUsageGenerator;
import org.cubeengine.libcube.service.command.exception.PermissionDeniedException;
import org.cubeengine.libcube.service.command.property.RawPermission;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.i18n.formatter.MessageType;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.Subject;

import static org.cubeengine.butler.parameter.property.Requirement.isRequired;

public class CommandUsageGenerator extends ParameterUsageGenerator
{
    private final I18n i18n;

    public CommandUsageGenerator(I18n i18n)
    {
        this.i18n = i18n;
    }

    @Override
    public String generateParameterUsage(CommandInvocation invocation, CommandDescriptor parameters)
    {
        return super.generateParameterUsage(invocation, parameters);
    }


    @Override
    protected String generateFlagUsage(CommandInvocation invocation, Parameter parameter)
    {
        if (invocation != null)
        {
            checkPermission(invocation.getCommandSource(), parameter);
        }
        return super.generateFlagUsage(invocation, parameter);
    }

    private void checkPermission(Object source, Parameter parameter)
    {
        if (parameter.getProperty(RawPermission.class) != null && source instanceof Subject)
        {
            RawPermission rawPerm = parameter.getProperty(RawPermission.class);
            if (!((Subject)source).hasPermission(rawPerm.getName()))
            {
                throw new PermissionDeniedException(rawPerm);
            }
        }
    }

    @Override
    protected String generateParameterUsage(CommandInvocation invocation, Parameter parameter)
    {
        if (invocation != null && !isRequired(parameter))
        {
            checkPermission(invocation.getCommandSource(), parameter);
        }
        return super.generateParameterUsage(invocation, parameter);
    }

    @Override
    protected String valueLabel(CommandInvocation invocation, String valueLabel)
    {
        if (invocation != null && invocation.getCommandSource() instanceof CommandSource)
        {
            return i18n.getTranslation(invocation.getContext(Locale.class), MessageType.NONE, valueLabel).toPlain();
        }
        return valueLabel;
    }

    @Override
    protected String getPrefix(CommandInvocation invocation)
    {
        if (invocation != null && invocation.getCommandSource() instanceof Player)
        {
            return "/";
        }
        return "";
    }
}
