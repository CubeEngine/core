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

import org.bukkit.Server;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.util.formatter.MessageType;

public class TestConsoleSender implements CommandSender
{
    private final Core core;

    public TestConsoleSender(Core core)
    {
        this.core = core;
    }

    @Override
    public Core getCore()
    {
        return this.core;
    }

    @Override
    public String getName()
    {
        return "TestConsoleSender";
    }

    @Override
    public String getDisplayName()
    {
        return this.getName();
    }

    @Override
    public boolean isAuthorized(Permission perm)
    {
        return false; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Locale getLocale()
    {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendMessage(String message)
    {
    //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getTranslation(MessageType type, String message, Object... params)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendTranslatedN(MessageType type, int n, String singular, String plural, Object... params)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getTranslationN(MessageType type, int n, String singular, String plural, Object... params)
    {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendTranslated(MessageType type, String message, Object... params)
    {
        this.sendMessage(this.getTranslation(type, message, params));
    }

    @Override
    public void sendMessage(String[] messages)
    {
        for (String message : messages)
        {
            this.sendMessage(message);
        }
    }

    @Override
    public Server getServer()
    {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isPermissionSet(String name)
    {
        return false; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isPermissionSet(org.bukkit.permissions.Permission perm)
    {
        return false; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasPermission(String name)
    {
        return false; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasPermission(org.bukkit.permissions.Permission perm)
    {
        return false; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value)
    {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin)
    {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks)
    {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks)
    {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment)
    {
    //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void recalculatePermissions()
    {
    //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isOp()
    {
        return false; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setOp(boolean value)
    {
    //To change body of implemented methods use File | Settings | File Templates.
    }
}
