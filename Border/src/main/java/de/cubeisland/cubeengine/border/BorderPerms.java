package de.cubeisland.cubeengine.border;

import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import org.bukkit.permissions.Permissible;

import java.util.Locale;

public enum BorderPerms implements Permission
{
    BYPASS(PermDefault.FALSE);

    private String permission;
    private PermDefault def;

    public static final String BASE = "cubeengine.border.";

    private BorderPerms()
    {
        this(PermDefault.OP);
    }

    private BorderPerms(PermDefault def)
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
