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
package de.cubeisland.engine.core.command.property;

import java.util.Stack;

import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.command.CommandBase;
import de.cubeisland.engine.command.util.property.AbstractProperty;
import de.cubeisland.engine.command.util.property.Finalizable;
import de.cubeisland.engine.command.util.property.PropertyHolder;
import de.cubeisland.engine.core.command.ModuleProvider;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;

public class PermissionProvider extends AbstractProperty<Permission> implements Finalizable
{
    private boolean isRegistered = false;

    public PermissionProvider(Permission value)
    {
        super(value);
    }

    public boolean isAuthorized(Permissible permissible)
    {
        return value().isAuthorized(permissible);
    }

    public void doFinalize(PropertyHolder holder)
    {
        if (isRegistered)
        {
            return;
        }
        Module module = holder.valueFor(ModuleProvider.class);
        if (module == null)
        {
            throw new IllegalStateException("Property Holder is missing a ModuleProvider Property");
        }
        Stack<String> cmds = new Stack<>();
        CommandBase parent = holder.valueFor(ParentCommand.class);
        while (parent != null)
        {
            cmds.push(parent.getDescriptor().getName());
            parent = parent.getDescriptor().valueFor(ParentCommand.class);
        }

        Permission cmdPerm = module.getBasePermission().childWildcard("command");
        while (!cmds.isEmpty())
        {
            cmdPerm = cmdPerm.childWildcard(cmds.pop());
        }
        this.value().setParent(cmdPerm);
        module.getCore().getPermissionManager().registerPermission(module, this.value());
        this.isRegistered = true;
    }
}
