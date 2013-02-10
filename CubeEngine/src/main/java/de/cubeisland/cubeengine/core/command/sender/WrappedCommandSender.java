package de.cubeisland.cubeengine.core.command.sender;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.permission.Permission;
import org.bukkit.Server;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.Set;

import static de.cubeisland.cubeengine.core.i18n.I18n._;

public class WrappedCommandSender implements CommandSender
{
    private final org.bukkit.command.CommandSender wrapped;

    public WrappedCommandSender(org.bukkit.command.CommandSender sender)
    {
        this.wrapped = sender;
    }

    @Override
    public String getName()
    {
        return this.wrapped.getName();
    }

    @Override
    public boolean isAuthorized(Permission perm)
    {
        return this.wrapped.hasPermission(perm.getPermission());
    }

    @Override
    public String getLanguage()
    {
        return CubeEngine.getI18n().getDefaultLanguage();
    }

    @Override
    public void sendMessage(String message)
    {
        this.wrapped.sendMessage(message);
    }

    @Override
    public void sendMessage(String[] messages)
    {
        this.wrapped.sendMessage(messages);
    }

    @Override
    public Server getServer()
    {
        return this.wrapped.getServer();
    }

    @Override
    public void sendMessage(String category, String message, Object... params)
    {
        this.sendMessage(_(this, category, message, params));
    }

    @Override
    public boolean isPermissionSet(String name)
    {
        return this.wrapped.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(org.bukkit.permissions.Permission perm)
    {
        return this.wrapped.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(String name)
    {
        return this.wrapped.hasPermission(name);
    }

    @Override
    public boolean hasPermission(org.bukkit.permissions.Permission perm)
    {
        return this.wrapped.hasPermission(perm);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value)
    {
        return this.wrapped.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin)
    {
        return this.wrapped.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks)
    {
        return this.wrapped.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks)
    {
        return this.wrapped.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment)
    {
        this.wrapped.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions()
    {
        this.wrapped.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        return this.wrapped.getEffectivePermissions();
    }

    @Override
    public boolean isOp()
    {
        return this.wrapped.isOp();
    }

    @Override
    public void setOp(boolean value)
    {
        this.wrapped.setOp(value);
    }

    public org.bukkit.command.CommandSender getWrappedSender()
    {
        return this.wrapped;
    }
}
