/*
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

import java.util.Locale;

import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.parametric.context.ParameterizedContext;
import org.cubeengine.libcube.service.command.exception.PermissionDeniedException;
import org.cubeengine.libcube.service.command.property.RawPermission;
import org.cubeengine.libcube.service.i18n.I18n;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextFormat;

public class CommandContext extends ParameterizedContext
{
    private final CommandSource sender;
    private I18n i18n;

    public CommandContext(CommandInvocation invocation, I18n i18n)
    {
        super(invocation);
        this.i18n = i18n;
        if (!(invocation.getCommandSource() instanceof CommandSource))
        {
            throw new IllegalArgumentException("CommandSource is not an accepted CommandSource: " + invocation.getCommandSource().getClass().getName());
        }
        sender = (CommandSource)invocation.getCommandSource();
    }

    @Override
    public CommandSource getSource()
    {
        return (CommandSource)super.getSource();
    }

    public void sendMessage(String message)
    {
        ((CommandSource)this.getInvocation().getCommandSource()).sendMessage(Text.of(message));
    }

    public void sendTranslated(TextFormat type, String message, Object... args)
    {
        i18n.send(sender, type, message, args);
    }

    public void sendTranslatedN(TextFormat type, int count, String sMessage, String pMessage, Object... args)
    {
        i18n.sendN(sender, type, count, sMessage, pMessage, args);
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
        throw new PermissionDeniedException(new RawPermission(permission.getId(), permission.getDescription().toPlain()));
    }

    public Locale getLocale()
    {
        return getInvocation().getContext(Locale.class);
    }
}
