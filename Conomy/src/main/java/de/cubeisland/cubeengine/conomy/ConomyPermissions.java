package de.cubeisland.cubeengine.conomy;

import de.cubeisland.cubeengine.core.permission.PermDefault;
import static de.cubeisland.cubeengine.core.permission.PermDefault.OP;
import de.cubeisland.cubeengine.core.permission.Permission;
import java.util.Locale;
import org.bukkit.permissions.Permissible;

public enum ConomyPermissions implements Permission
{
    ACCOOUNT_ALLOWUNDERMIN, // Allows the user to have less money then the minimum (default 0)

    ;
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
