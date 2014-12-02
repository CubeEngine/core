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
package de.cubeisland.engine.core.command.exception;

import de.cubeisland.engine.command.CommandException;
import de.cubeisland.engine.core.permission.Permission;

/**
 * This exception is thrown when a CommandSource is not allowed to perform an action.
 * If {@link #canCheck()} is false the CommandSource does not support checking permissions.
 */
public class PermissionDeniedException extends CommandException
{
    private final Permission permission;
    private final boolean canCheck;

    public PermissionDeniedException(Permission permission)
    {
        this(permission, true);
    }

    public PermissionDeniedException(Permission permission, boolean canCheck)
    {
        this.permission = permission;
        this.canCheck = canCheck;
    }

    public PermissionDeniedException(String message, Permission permission)
    {
        super(message);
        this.permission = permission;
        this.canCheck = true;
    }

    /**
     * Returns the permission
     *
     * @return the permission
     */
    public Permission getPermission()
    {
        return permission;
    }

    /**
     * Returns false if the CommandSource did not support checking permissions
     *
     * @return whether the permission could be checked
     */
    public boolean canCheck()
    {
        return canCheck;
    }
}
