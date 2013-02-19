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
    SIGN_CREATE_USER,
    SIGN_CREATE_ADMIN, SIGN_CREATE_ADMIN_BUY, SIGN_CREATE_USER_BUY, SIGN_CREATE_USER_SELL, SIGN_CREATE_ADMIN_SELL, SIGN_CREATE_USER_OTHER, SIGN_CREATE_ADMIN_STOCK, SIGN_CREATE_ADMIN_NOSTOCK, SIGN_CREATE, SIGN_EDIT;
    private String permission;
    private PermDefault def;

    private MarketSignPerm()
    {
        this(OP);
    }

    private MarketSignPerm(PermDefault def)
    {
        this.permission = "cubeengine.signmarket." + this.name().
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
