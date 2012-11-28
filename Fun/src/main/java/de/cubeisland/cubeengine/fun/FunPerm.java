package de.cubeisland.cubeengine.fun;

import de.cubeisland.cubeengine.core.permission.Permission;
import java.util.Locale;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;

public enum FunPerm implements Permission  
{
    EXPLOSION_OTHER,
    EXPLOSION_PLAYER_DAMAGE,
    EXPLOSION_BLOCK_DAMAGE,
    EXPLOSION_FIRE
    ;
        
    private String permission;
    private PermissionDefault def;

    private FunPerm()
    {
        this(PermissionDefault.OP);
    }

    private FunPerm(PermissionDefault def)
    {
        this.permission = "cubeengine.fun." + this.name().toLowerCase(Locale.ENGLISH).replace('_', '.');
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
    public PermissionDefault getPermissionDefault()
    {
        return this.def;
    }
}
