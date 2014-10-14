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

import de.cubeisland.engine.command.ImmutableCommandDescriptor;
import de.cubeisland.engine.command.methodic.MethodicCommandContainer;
import de.cubeisland.engine.core.command.annotation.CommandPermission;
import de.cubeisland.engine.core.command.annotation.Unloggable;
import de.cubeisland.engine.core.command.property.Loggable;
import de.cubeisland.engine.core.command.property.PermissionProvider;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.PermDefault;

import static de.cubeisland.engine.core.command.property.CheckPermission.CHECK;
import static de.cubeisland.engine.core.command.property.CheckPermission.NOT_CHECK;

public class CommandContainer extends MethodicCommandContainer<Module, CommandOrigin>
{
    public CommandContainer(Module module)
    {
        super(module.getCore().getCommandManager().getCommandBuilder(), module);
        ImmutableCommandDescriptor descriptor = (ImmutableCommandDescriptor)this.getDescriptor();
        String permName = descriptor.getName();
        boolean checkPerm = true;
        PermDefault def = PermDefault.DEFAULT;
        CommandPermission perm = this.getClass().getAnnotation(CommandPermission.class);
        if (perm != null)
        {
            permName = perm.value().isEmpty() ? permName : perm.value();
            def = perm.permDefault();
            checkPerm = perm.checkPermission();
        }
        descriptor.setProperty(new PermissionProvider(module.getBasePermission().childWildcard(permName, def)));
        descriptor.setProperty(checkPerm ? CHECK : NOT_CHECK);
        descriptor.setProperty(new ModuleProvider(module));
        this.addCommand(new HelpCommand(this));
    }

    @Override
    public ImmutableCommandDescriptor selfDescribe()
    {
        ImmutableCommandDescriptor descriptor = super.selfDescribe();
        descriptor.setProperty(Loggable.of(!this.getClass().isAnnotationPresent(Unloggable.class)));
        return descriptor;
    }

    @Override
    protected CommandOrigin getSubOrigin(Method method, Module origin)
    {
        return new CommandOrigin(method, this, origin);
    }
}
