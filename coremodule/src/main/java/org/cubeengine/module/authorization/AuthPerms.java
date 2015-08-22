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
package org.cubeengine.module.authorization;

import org.cubeengine.service.permission.PermissionContainer;
import org.spongepowered.api.service.permission.PermissionDescription;

public class AuthPerms extends PermissionContainer<Authorization>
{
    public AuthPerms(Authorization module)
    {
        super(module);
    }

    private final PermissionDescription COMMAND = register("command.", "Base Commands Permission", null);

    public final PermissionDescription COMMAND_CLEARPASSWORD_ALL = register("clearpassword.all", "Allows clearing all passwords", COMMAND);
    public final PermissionDescription COMMAND_CLEARPASSWORD_OTHER = register("clearpassword.other", "Allows clearing passwords of other players", COMMAND);
    public final PermissionDescription COMMAND_SETPASSWORD_OTHER = register("setpassword.other", "Allows setting passwords of other players", COMMAND);
}
