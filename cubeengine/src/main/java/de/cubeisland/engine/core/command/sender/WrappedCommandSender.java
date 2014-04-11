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
package de.cubeisland.engine.core.command.sender;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.util.formatter.MessageType;

public class WrappedCommandSender implements CommandSender
{
    public static final UUID NON_PLAYER_UUID = new UUID(0, 0);
    private final Core core;
    private final org.bukkit.command.CommandSender wrapped;

    public WrappedCommandSender(Core core, org.bukkit.command.CommandSender sender)
    {
        this.core = core;
        this.wrapped = sender;
    }

    @Override
    public UUID getUniqueId()
    {
        if (wrapped instanceof Player)
        {
            return ((Player)wrapped).getUniqueId();
        }
        return NON_PLAYER_UUID;
    }

    public Core getCore()
    {
        return this.core;
    }

    @Override
    public String getName()
    {
        return this.getWrappedSender().getName();
    }

    @Override
    public String getDisplayName()
    {
        return this.getName();
    }

    @Override
    public boolean isAuthorized(Permission perm)
    {
        return this.getWrappedSender().hasPermission(perm.getName());
    }

    @Override
    public Locale getLocale()
    {
        return Locale.getDefault();
    }

    @Override
    public void sendMessage(String message)
    {
        this.getWrappedSender().sendMessage(message);
    }

    @Override
    public void sendMessage(String[] messages)
    {
        this.getWrappedSender().sendMessage(messages);
    }

    @Override
    public Server getServer()
    {
        return this.getWrappedSender().getServer();
    }

    public String getTranslation(MessageType type, String message, Object... params)
    {
        return this.getCore().getI18n().translate(this.getLocale(), type, message, params);
    }

    @Override
    public void sendTranslated(MessageType type, String message, Object... params)
    {
        this.sendMessage(this.getTranslation(type, message, params));
    }


    @Override
    public String getTranslationN(MessageType type, int n, String singular, String plural, Object... params)
    {
        return this.getCore().getI18n().translateN(this.getLocale(), type, n, singular, plural, params);
    }


    @Override
    public void sendTranslatedN(MessageType type, int n, String singular, String plural, Object... params)
    {
        this.sendMessage(this.getTranslationN(type, n, singular, plural, params));
    }

    @Override
    public boolean isPermissionSet(String name)
    {
        return this.getWrappedSender().isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(org.bukkit.permissions.Permission perm)
    {
        return this.getWrappedSender().isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(String name)
    {
        return this.getWrappedSender().hasPermission(name);
    }

    @Override
    public boolean hasPermission(org.bukkit.permissions.Permission perm)
    {
        return this.getWrappedSender().hasPermission(perm);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value)
    {
        return this.getWrappedSender().addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin)
    {
        return this.getWrappedSender().addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks)
    {
        return this.getWrappedSender().addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks)
    {
        return this.getWrappedSender().addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment)
    {
        this.getWrappedSender().removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions()
    {
        this.getWrappedSender().recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        return this.getWrappedSender().getEffectivePermissions();
    }

    @Override
    public boolean isOp()
    {
        return this.getWrappedSender().isOp();
    }

    @Override
    public void setOp(boolean value)
    {
        this.getWrappedSender().setOp(value);
    }

    public org.bukkit.command.CommandSender getWrappedSender()
    {
        return this.wrapped;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o instanceof WrappedCommandSender)
        {
            return ((WrappedCommandSender)o).getName().equals(this.getWrappedSender().getName());
        }
        else if (o instanceof org.bukkit.command.CommandSender)
        {
            return ((org.bukkit.command.CommandSender)o).getName().equals(this.getWrappedSender().getName());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return this.getWrappedSender().hashCode();
    }
}
