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
package de.cubeisland.engine.core.command_old.exception;

import de.cubeisland.engine.command.CommandException;
import de.cubeisland.engine.core.permission.Permission;

/**
 * This exception is thrown when a user is not allowed to perform an action.
 * Use denyAccess to throw an exception insinde a command. The exception will be caught.
 */
public class PermissionDeniedException extends CommandException
{
    private final String permission;

    public PermissionDeniedException(String permission)
    {
        this.permission = permission;
    }
    
    public PermissionDeniedException(String message, String permission)
    {
        super(message);
        this.permission = permission;
    }

    public PermissionDeniedException(String message, Permission permission)
    {
        super(message);
        this.permission = permission.getName();
    }
    
    public PermissionDeniedException(Permission permission)
    {
        this(permission.getName());
    }

    public String getPermission()
    {
        return permission;
    }
}
