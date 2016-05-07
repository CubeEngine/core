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
package org.cubeengine.libcube.service.command.exception;

import org.cubeengine.butler.exception.CommandException;
import org.cubeengine.libcube.service.command.property.RawPermission;
import org.cubeengine.libcube.service.permission.Permission;
import org.spongepowered.api.service.permission.PermissionDescription;

/**
 * This exception is thrown when a CommandSource is not allowed to perform an action.
 * If {@link #canCheck()} is false the CommandSource does not support checking permissions.
 */
public class PermissionDeniedException extends CommandException
{
    private final RawPermission permission;
    private final boolean canCheck;

    public PermissionDeniedException(RawPermission permission)
    {
        this(permission, true);
    }

    public PermissionDeniedException(RawPermission permission, boolean canCheck)
    {
        this.permission = permission;
        this.canCheck = canCheck;
    }

    public PermissionDeniedException(RawPermission permission, String message)
    {
        super(message);
        this.permission = permission;
        this.canCheck = true;
    }

    public PermissionDeniedException(Permission description)
    {
        this(new RawPermission(description.getId(), description.getDesc()));
    }

    /**
     * Returns the permission
     *
     * @return the permission
     */
    public RawPermission getPermission()
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
