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

import java.lang.reflect.Method;
import de.cubeisland.engine.command.CommandBase;
import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.alias.AliasCommand;
import de.cubeisland.engine.command.parametric.ParametricContainerCommand;
import de.cubeisland.engine.core.command.annotation.CommandPermission;
import de.cubeisland.engine.core.command.annotation.Unloggable;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.permission.Permission;

public class ContainerCommand extends ParametricContainerCommand<CommandOrigin>
{
    public ContainerCommand(Module module)
    {
        super(new CubeContainerCommandDescriptor(), module.getCore().getCommandManager().getCommandBuilder());

        String permName = getDescriptor().getName();
        boolean checkPerm = true;
        PermDefault def = PermDefault.DEFAULT;
        CommandPermission perm = this.getClass().getAnnotation(CommandPermission.class);
        if (perm != null)
        {
            permName = perm.value().isEmpty() ? permName : perm.value();
            def = perm.permDefault();
            checkPerm = perm.checkPermission();
        }
        getDescriptor().setPermission(module.getBasePermission().childWildcard("command").child(permName, def), checkPerm);
        getDescriptor().setModule(module);
        getDescriptor().setLoggable(!this.getClass().isAnnotationPresent(Unloggable.class));

        this.addCommand(new HelpCommand(this));
    }

    @Override
    protected CommandOrigin originFor(Method method)
    {
        return new CommandOrigin(method, this, getDescriptor().getModule());
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
        if (super.addCommand(command))
        {
            if (!(command instanceof AliasCommand) && command.getDescriptor() instanceof CubeDescriptor)
            {
                CubeDescriptor descriptor = (CubeDescriptor)command.getDescriptor();
                Module module = descriptor.getModule();
                Permission permission = descriptor.getPermission();
                permission.setParent(this.getDescriptor().getPermission());
                module.getCore().getPermissionManager().registerPermission(module, permission);
            }
            return true;
        }
        return false;
    }

    public Permission getPermission(String... alias)
    {
        CommandBase command = this.getCommand(alias);
        if (command.getDescriptor() instanceof CubeDescriptor)
        {
            return ((CubeDescriptor)command.getDescriptor()).getPermission();
        }
        return null;
    }
}
