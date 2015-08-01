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
package de.cubeisland.engine.service.command;

import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.parametric.context.ParameterizedContext;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.service.command.exception.PermissionDeniedException;
import de.cubeisland.engine.service.command.property.RawPermission;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.BaseFormatting;
import org.spongepowered.api.util.command.CommandSource;

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

    public void sendTranslated(BaseFormatting type, String message, Object... args)
    {
        ((CommandSender)this.getInvocation().getCommandSource()).sendTranslated(type, message, args);
    }

    public void sendTranslatedN(BaseFormatting type, int count, String sMessage, String pMessage, Object... args)
    {
        ((CommandSender)this.getInvocation().getCommandSource()).sendTranslatedN(type, count, sMessage, pMessage, args);
    }

    public void ensurePermission(PermissionDescription permission) throws PermissionDeniedException
    {
        if (getInvocation().getCommandSource() instanceof Subject)
        {
            if (((Subject)getInvocation().getCommandSource()).hasPermission(permission.getId()))
            {
                return;
            }
        }
        throw new PermissionDeniedException(new RawPermission(permission.getId(), Texts.toPlain(permission.getDescription()))); // TODO
    }
}
