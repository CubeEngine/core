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
package de.cubeisland.engine.core.webapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.bukkit.BukkitCore;

public class ApiCommandSender implements CommandSender
{
    private final String name;
    private final Core core;
    private final List<String> messages;

    public ApiCommandSender(final Core server)
    {
        this("ApiCommandSender", server);
    }

    public ApiCommandSender(final String name, final Core core)
    {
        this.name = name;
        this.core = core;
        this.messages = new ArrayList<>();
    }

    public Core getCore()
    {
        return this.core;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public void sendMessage(String message)
    {
    // TODO implement
    }

    @Override
    public void sendMessage(String[] strings)
    {
        for (String string : strings)
        {
            this.sendMessage(string);
        }
    }

    @Override
    public boolean isOp()
    {
        return true;
    }

    @Override
    public Server getServer()
    {
        return ((BukkitCore)this.core).getServer();
    }

    @Override
    public boolean isPermissionSet(String name)
    {
        return true;
    }

    @Override
    public boolean isPermissionSet(Permission perm)
    {
        return true;
    }

    @Override
    public boolean hasPermission(String name)
    {
        return true;
    }

    @Override
    public boolean hasPermission(Permission perm)
    {
        return true;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value)
    {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin)
    {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks)
    {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks)
    {
        return null;
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment)
    {}

    @Override
    public void recalculatePermissions()
    {}

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        return Collections.emptySet();
    }

    @Override
    public void setOp(boolean value)
    {}
}
