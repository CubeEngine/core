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
    public String generateUsage(CommandSource source, ParameterGroup parameters)
    {
        return super.generateUsage(source, parameters);
    }

    @Override
    protected String generateFlagUsage(CommandSource source, FlagParameter parameter)
    {
        checkPermission(source, parameter); // TODO instead return boolean
        return super.generateFlagUsage(source, parameter);
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
    protected String generateParameterUsage(CommandSource source, Parameter parameter)
    {
        if (!parameter.valueFor(Required.class))
        {
            checkPermission(source, parameter);
        }
        return super.generateParameterUsage(source, parameter);
    }

    @Override
    protected String valueLabel(CommandSource source, String valueLabel)
    {
        if (source instanceof CommandSender)
        {
            return ((CommandSender)source).getTranslation(MessageType.NONE, valueLabel);
        }
        return valueLabel;
    }

    @Override
    protected String getPrefix(CommandSource source)
    {
        if (source instanceof User)
        {
            return "/";
        }
        return "";
    }
}
