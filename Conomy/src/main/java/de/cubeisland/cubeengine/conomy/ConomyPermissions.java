package de.cubeisland.cubeengine.conomy;

import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import org.bukkit.permissions.Permissible;

import java.util.Locale;

import static de.cubeisland.cubeengine.core.permission.PermDefault.OP;

public enum ConomyPermissions implements Permission
{
    ACCOUNT_ALLOWUNDERMIN, // Allows the user to have less money then the minimum (default 0)

    ACCOUNT_SHOWHIDDEN,
    COMMAND_PAY_FORCE;
    private String permission;
    private PermDefault def;

    private ConomyPermissions()
    {
        this(OP);
    }

    private ConomyPermissions(PermDefault def)
    {
        this.permission = "cubeengine.conomy." + this.name().
                toLowerCase(Locale.ENGLISH).replace('_', '.');
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
