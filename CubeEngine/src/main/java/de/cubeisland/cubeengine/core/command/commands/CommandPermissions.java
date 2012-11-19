package de.cubeisland.cubeengine.core.command.commands;

import de.cubeisland.cubeengine.core.permission.Permission;
import java.util.Locale;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;

public enum CommandPermissions implements Permission
{
    COMMAND_CLEARPASS_ALL,
    COMMAND_CLEARPASS_OTHER,
    ;
    
    private String permission;
    private PermissionDefault def;

    private CommandPermissions()
    {
        this(PermissionDefault.OP);
    }

    private CommandPermissions(PermissionDefault def)
    {
        this.permission = "cubeengine.core" + this.name().
                toLowerCase(Locale.ENGLISH).replace('_', '.');
        this.def = def;
    }

    public boolean isAuthorized(Permissible player)
    {
        return player.hasPermission(permission);
    }

    public String getPermission()
    {
        return this.permission;
    }

    public PermissionDefault getPermissionDefault()
    {
        return this.def;
    }
}
