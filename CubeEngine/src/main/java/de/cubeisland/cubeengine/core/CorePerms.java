package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import org.bukkit.permissions.Permissible;

import java.util.Locale;

import static de.cubeisland.cubeengine.core.permission.PermDefault.OP;

public enum CorePerms implements Permission
{
    SPAM,
    COMMAND_CLEARPASSWORD_ALL,
    COMMAND_CLEARPASSWORD_OTHER,
    COMMAND_SETPASSWORD_OTHER;

    private String permission;
    private PermDefault def;
    private static final String BASE = "cubeengine.core.";

    private CorePerms()
    {
        this(OP);
    }

    private CorePerms(PermDefault def)
    {
        this.permission = BASE + this.name().toLowerCase(Locale.ENGLISH).replace('_', '.');
        this.def = def;
    }

    @Override
    public boolean isAuthorized(Permissible player)
    {
        return player.hasPermission(permission);
    }

    @Override
    public String getPermission()
    {
        return this.permission;
    }

    @Override
    public PermDefault getPermissionDefault()
    {
        return this.def;
    }
}
