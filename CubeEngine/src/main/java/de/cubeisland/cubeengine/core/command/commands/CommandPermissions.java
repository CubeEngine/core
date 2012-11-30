package de.cubeisland.cubeengine.core.command.commands;

import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import org.bukkit.permissions.Permissible;

import java.util.Locale;

public enum CommandPermissions implements Permission
{
    COMMAND_CLEARPASSWORD_ALL,
    COMMAND_CLEARPASSWORD_OTHER,
    COMMAND_SETPASSWORD_OTHER, ;

    private String permission;
    private PermDefault def;

    private CommandPermissions()
    {
        this(PermDefault.OP);
    }

    private CommandPermissions(PermDefault def)
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

    public PermDefault getPermissionDefault()
    {
        return this.def;
    }
}
