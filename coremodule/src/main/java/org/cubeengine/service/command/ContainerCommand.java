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

import java.lang.reflect.Method;
import org.cubeengine.butler.CommandBase;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.alias.AliasCommand;
import org.cubeengine.butler.parametric.ParametricContainerCommand;
import de.cubeisland.engine.modularity.core.Module;
import org.cubeengine.service.command.annotation.CommandPermission;
import org.cubeengine.service.command.annotation.Unloggable;
import org.cubeengine.service.command.property.RawPermission;
import org.cubeengine.service.i18n.I18n;
import org.cubeengine.service.permission.PermissionManager;
import org.spongepowered.api.service.permission.PermissionDescription;

public class ContainerCommand extends ParametricContainerCommand<CommandOrigin>
{
    private final PermissionManager pm;
    private Module module;

    public ContainerCommand(Module module)
    {
        super(new CubeContainerCommandDescriptor(), module.getModularity().provide(CommandManager.class).getCommandBuilder());
        this.module = module;
        pm = module.getModularity().provide(PermissionManager.class);
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
        getDescriptor().setModule(module);
        getDescriptor().setLoggable(!this.getClass().isAnnotationPresent(Unloggable.class));

        this.addCommand(new HelpCommand(this, module.getModularity().provide(I18n.class)));
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
        if (!(command instanceof AliasCommand) && command.getDescriptor() instanceof CubeDescriptor)
        {
            PermissionDescription thisPerm = getPermission();
            ((CubeDescriptor)command.getDescriptor()).registerPermission(pm, thisPerm);
        }
        return super.addCommand(command);
    }

    public PermissionDescription getPermission(String... alias)
    {
        CommandBase command = this.getCommand(alias);
        if (command.getDescriptor() instanceof CubeDescriptor)
        {
            PermissionDescription cmdPerm = pm.getPermission(pm.getModulePermission(module).getId() + ".command");
            if (cmdPerm == null)
            {
                cmdPerm = pm.register(module, "command", "", null);
            }
            return ((CubeDescriptor)command.getDescriptor()).registerPermission(pm, cmdPerm);// registers permission if not yet registered
        }
        return null;
    }
}
