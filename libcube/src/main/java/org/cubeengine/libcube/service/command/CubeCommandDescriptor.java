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
package org.cubeengine.libcube.service.command;

import de.cubeisland.engine.modularity.core.Module;
import org.cubeengine.butler.parameter.GroupParser;
import org.cubeengine.butler.parameter.Parameter;
import org.cubeengine.butler.parametric.ParametricCommandDescriptor;
import org.cubeengine.libcube.service.command.property.RawPermission;
import org.cubeengine.libcube.service.permission.PermissionManager;
import org.spongepowered.api.service.permission.PermissionDescription;

public class CubeCommandDescriptor extends ParametricCommandDescriptor implements CubeDescriptor
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
    public RawPermission getPermission()
    {
        return permission;
    }

    @Override
    public PermissionDescription registerPermission(PermissionManager pm, PermissionDescription parent)
    {
        if (!getPermission().isRegistered())
        {
            PermissionDescription thisPerm = getPermission().fallbackDescription("Allows using the command " + getName()).registerPermission(module, pm, parent);
            registerParameterPermissions(module, pm, thisPerm, getParameters());
        }
        return getPermission().getRegistered();
    }

    public static void registerParameterPermissions(Module module, PermissionManager pm, PermissionDescription thisPerm, Parameter parameter)
    {
        RawPermission rawPermission = parameter.getProperty(RawPermission.class);
        if (rawPermission != null)
        {
            rawPermission.registerPermission(module, pm, thisPerm);
        }

        if (parameter.getParser() instanceof GroupParser)
        {
            for (Parameter param : ((GroupParser)parameter.getParser()).getPositional())
            {
                registerParameterPermissions(module, pm, thisPerm, param);
            }
            for (Parameter param : ((GroupParser)parameter.getParser()).getNonPositional())
            {
                registerParameterPermissions(module, pm, thisPerm, param);
            }
            for (Parameter param : ((GroupParser)parameter.getParser()).getFlags())
            {
                registerParameterPermissions(module, pm, thisPerm, param);
            }
        }
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
