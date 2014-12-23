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

import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.CommandSource;
import de.cubeisland.engine.command.parameter.FlagParameter;
import de.cubeisland.engine.command.parameter.Parameter;
import de.cubeisland.engine.command.parameter.ParameterGroup;
import de.cubeisland.engine.command.parameter.ParameterUsageGenerator;
import de.cubeisland.engine.command.parameter.property.Required;
import de.cubeisland.engine.core.command.property.PermissionProvider;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;

public class CommandUsageGenerator extends ParameterUsageGenerator
{
    @Override
    public String generateParameterUsage(CommandInvocation invocation, ParameterGroup parameters)
    {
        return super.generateParameterUsage(invocation, parameters);
    }

    @Override
    protected String generateFlagUsage(CommandInvocation invocation, FlagParameter parameter)
    {
        if (invocation != null)
        {
            checkPermission(invocation.getCommandSource(), parameter);
        }
        return super.generateFlagUsage(invocation, parameter);
    }

    private void checkPermission(CommandSource source, Parameter parameter)
    {
        if (parameter.hasProperty(PermissionProvider.class) && source instanceof Permissible)
        {
            if (!parameter.valueFor(PermissionProvider.class).isAuthorized((Permissible)(source)))
            {
                throw new PermissionDeniedException(parameter.valueFor(PermissionProvider.class));
            }
        }
    }

    @Override
    protected String generateParameterUsage(CommandInvocation invocation, Parameter parameter)
    {
        if (invocation != null && !parameter.valueFor(Required.class))
        {
            checkPermission(invocation.getCommandSource(), parameter);
        }
        return super.generateParameterUsage(invocation, parameter);
    }

    @Override
    protected String valueLabel(CommandInvocation invocation, String valueLabel)
    {
        if (invocation != null && invocation.getCommandSource() instanceof CommandSender)
        {
            return ((CommandSender)invocation.getCommandSource()).getTranslation(MessageType.NONE, valueLabel);
        }
        return valueLabel;
    }

    @Override
    protected String getPrefix(CommandInvocation invocation)
    {
        if (invocation != null && invocation.getCommandSource() instanceof User)
        {
            return "/";
        }
        return "";
    }
}
