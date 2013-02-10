package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import org.bukkit.permissions.Permissible;

import java.util.Locale;

import static de.cubeisland.cubeengine.core.permission.PermDefault.OP;

public enum MarketSignPerm implements Permission
{
    SIGN_DESTROY_OWN,
    SIGN_DESTROY_ADMIN,
    SIGN_DESTROY_OTHER,
    SIGN_INVENTORY_SHOW,
    SIGN_INVENTORY_ACCESS_OTHER,
    SIGN_INVENTORY_TAKE_OTHER,
    SIGN_INVENTORY_TAKE_ADMIN,
    SIGN_CREATE_USER,
    SIGN_CREATE_ADMIN,
    ;
    private String permission;
    private PermDefault def;

    private MarketSignPerm()
    {
        this(OP);
    }

    private MarketSignPerm(PermDefault def)
    {
        this.permission = "cubeengine.basics." + this.name().
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
