package de.cubeisland.cubeengine.core.command.sender;

import de.cubeisland.cubeengine.core.permission.Permission;
import org.bukkit.Server;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public class TestConsoleSender implements CommandSender
{
    @Override
    public String getName()
    {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getDisplayName()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isAuthorized(Permission perm)
    {
        return false; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getLanguage()
    {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendMessage(String message)
    {
    //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendMessage(String category, String message, Object... params)
    {
    //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendMessage(String[] messages)
    {
    //To change body of implemented methods use File | Settings | File Templates.
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
