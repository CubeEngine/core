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
package org.cubeengine.service.command;

import org.cubeengine.butler.parametric.ContainerCommandDescriptor;
import de.cubeisland.engine.modularity.core.Module;
import org.cubeengine.service.command.property.RawPermission;
import org.cubeengine.service.permission.PermissionManager;
import org.spongepowered.api.service.permission.PermissionDescription;

public class CubeContainerCommandDescriptor extends ContainerCommandDescriptor implements CubeDescriptor
{
    private boolean loggable;
    private RawPermission permission;
    private boolean checkPerm;
    private Module module;

    public void setLoggable(boolean loggable)
    {
        this.loggable = loggable;
    }

    @Override
    public boolean isLoggable()
    {
        return loggable;
    }

    public void setPermission(RawPermission permission, boolean checkPerm)
    {
        this.permission = permission;
        this.checkPerm = checkPerm;
    }

    @Override
    public PermissionDescription registerPermission(PermissionManager pm, PermissionDescription parent)
    {
        if (!getPermission().isRegistered())
        {
            getPermission().fallbackDescription("Allows using the command " + getName()).registerPermission(module, pm, parent);
        }
        return getPermission().getRegistered();
    }

    @Override
    public RawPermission getPermission()
    {
        return permission;
    }

    @Override
    public boolean isCheckPerm()
    {
        return checkPerm;
    }

    public void setModule(Module module)
    {
        this.module = module;
    }

    @Override
    public Module getModule()
    {
        return module;
    }

}
