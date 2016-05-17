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

import org.cubeengine.butler.CommandBase;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.alias.AliasCommand;
import org.cubeengine.butler.parametric.ParametricContainerCommand;
import org.cubeengine.libcube.service.command.annotation.CommandPermission;
import org.cubeengine.libcube.service.command.annotation.Unloggable;
import org.cubeengine.libcube.service.command.property.RawPermission;
import org.cubeengine.libcube.service.permission.Permission;
import org.cubeengine.libcube.service.permission.PermissionManager;
import org.spongepowered.api.service.permission.PermissionDescription;

public class ContainerCommand extends ParametricContainerCommand
{
    private CommandManager manager;

    public ContainerCommand(CommandManager base, Class owner)
    {
        super(new CubeContainerCommandDescriptor(base), owner);
        this.manager = base;
        String permName = getDescriptor().getName();
        String permDesc = null;
        boolean checkPerm = true;
        String[] groups = null;
        CommandPermission perm = this.getClass().getAnnotation(CommandPermission.class);
        if (perm != null)
        {
            permName = perm.value().isEmpty() ? permName : perm.value();
            permDesc = perm.desc().isEmpty() ? null : perm.desc();
            checkPerm = perm.checkPermission();
            groups = perm.group();
        }
        getDescriptor().setPermission(new RawPermission(permName, permDesc).assign(groups), checkPerm);
        getDescriptor().setLoggable(!this.getClass().isAnnotationPresent(Unloggable.class));

        this.addCommand(new HelpCommand(this, base.getI18n()));
    }

    @Override
    protected boolean selfExecute(CommandInvocation invocation)
    {
        return this.getCommand("?").execute(invocation);
    }

    @Override
    public CubeContainerCommandDescriptor getDescriptor()
    {
        return (CubeContainerCommandDescriptor)super.getDescriptor();
    }

    @Override
    public boolean addCommand(CommandBase command)
    {
        if (!(command instanceof AliasCommand) && command.getDescriptor() instanceof CubeDescriptor)
        {
            PermissionManager pm = manager.getPermissionManager();
            Class owner = getDescriptor().getOwner();
            Permission basePerm = pm.getBasePermission(owner);
            Permission cmdPerm = pm.getPermission(basePerm.getId() + ".command");
            if (cmdPerm == null)
            {
                cmdPerm = pm.register(owner, "command", "Allows using all commands for this module", null); // TODO Description for BaseCommand Permission
            }
            Permission thisPerm = getDescriptor().registerPermission(pm, cmdPerm); // gets the container permission

            ((CubeDescriptor)command.getDescriptor()).registerPermission(pm, thisPerm); // register the added cmd permission
        }
        return super.addCommand(command);
    }
}
