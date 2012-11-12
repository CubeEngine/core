package de.cubeisland.cubeengine.core.webapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class ApiCommandSender implements CommandSender
{
    private final String name;
    private final Server server;
    private final List<String> messages;
    private boolean active;

    public ApiCommandSender(final Server server)
    {
        this("ApiCommandSender", server);
    }

    public ApiCommandSender(final String name, final Server server)
    {
        this.name = name;
        this.server = server;
        this.messages = new ArrayList<String>();
        this.active = false;
    }

    public void toggleActive()
    {
        if (active)
        {
            this.active = false;
        }
        else
        {
            this.messages.clear();
            this.active = true;
        }
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public void sendMessage(String message)
    {
        if (active)
        {
            this.messages.add(message.replaceAll("(?i)" + ChatColor.COLOR_CHAR + "([a-fk0-9])", "&$1"));
        }
    }

    @Override
    public void sendMessage(String[] strings)
    {
        for (String string : strings)
        {
            this.sendMessage(string);
        }
    }

    public List<String> getResponse()
    {
        if (!active)
        {
            return this.messages;
        }
        return null;
    }

    @Override
    public boolean isOp()
    {
        return true;
    }

    @Override
    public Server getServer()
    {
        return this.server;
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
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void recalculatePermissions()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setOp(boolean value)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}