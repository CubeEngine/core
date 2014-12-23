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

import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.CommandSource;
import de.cubeisland.engine.command.filter.Filter;
import de.cubeisland.engine.command.filter.FilterException;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;
import de.cubeisland.engine.core.permission.Permission;

/**
 * A Filter checking a CommandSenders Permission.
 * If the CommandSource is not a CommandSender ... what to do what to do
 */
public class PermissionFilter implements Filter
{
    private Permission permission;

    public PermissionFilter(Permission permission)
    {
        this.permission = permission;
    }

    @Override
    public void run(CommandInvocation invocation) throws FilterException
    {
        CommandSource source = invocation.getCommandSource();
        if (source instanceof CommandSender)
        {
            if (!((CommandSender)source).isAuthorized(permission))
            {
                throw new PermissionDeniedException(permission);
            }
            return;
        }
        throw new PermissionDeniedException(permission);
    }
}
