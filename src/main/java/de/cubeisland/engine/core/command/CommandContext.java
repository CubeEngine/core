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

import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.parametric.context.ParameterizedContext;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.util.formatter.MessageType;
import org.spongepowered.api.text.Texts;

public class CommandContext extends ParameterizedContext
{
    public CommandContext(CommandInvocation invocation)
    {
        super(invocation);
        if (!(invocation.getCommandSource() instanceof CommandSender))
        {
            throw new IllegalArgumentException("CommandSource is not a CommandSender");
        }
    }

    @Override
    public CommandSender getSource()
    {
        return (CommandSender)super.getSource();
    }

    public Module getModule()
    {
        return ((CubeDescriptor)this.getInvocation().getCommand().getDescriptor()).getModule();
    }

    public void sendMessage(String message)
    {
        ((CommandSender)this.getInvocation().getCommandSource()).sendMessage(message);
    }

    public void sendTranslated(MessageType type, String message, Object... args)
    {
        ((CommandSender)this.getInvocation().getCommandSource()).sendTranslated(type, message, args);
    }

    public void sendTranslatedN(MessageType type, int count, String sMessage, String pMessage, Object... args)
    {
        ((CommandSender)this.getInvocation().getCommandSource()).sendTranslatedN(type, count, sMessage, pMessage, args);
    }

    public void ensurePermission(Permission permission) throws PermissionDeniedException
    {
        if (!permission.isAuthorized((CommandSender)this.getInvocation().getCommandSource()))
        {
            throw new PermissionDeniedException(permission);
        }
    }
}
