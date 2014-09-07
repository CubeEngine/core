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
package de.cubeisland.engine.core.command.parameterized;

import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.command.Completer;
import de.cubeisland.engine.command.context.parameter.NamedParameter;
import de.cubeisland.engine.core.permission.Permission;

public class PermissibleNamedParameter extends NamedParameter
{
    protected final Permission permission;

    public PermissibleNamedParameter(String name, Class<?> type, Class<?> reader, int greed, boolean required,
                                     String valueLabel, String description, Permission permission)
    {
        super(name, type, reader, greed, required, valueLabel, description);
        this.permission = permission;
    }

    public PermissibleNamedParameter(String name, Class<?> type, boolean required, String label, String description,
                                     Permission permission)
    {
        this(name, type, type, 1, required, label, description, permission);
    }


    public PermissibleNamedParameter(String name, String label, Class<?> type)
    {
        this(name, type, type, 1, false, label, null, null);
    }

    public boolean checkPermission(Permissible permissible)
    {
        return this.permission == null || permissible == null || this.permission.isAuthorized(permissible);
    }

    public PermissibleNamedParameter withCompleter(Completer completer)
    {
        this.setCompleter(completer);
        return this;
    }

    public Permission getPermission()
    {
        return permission;
    }
}
