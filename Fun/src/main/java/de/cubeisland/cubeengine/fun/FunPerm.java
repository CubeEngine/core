package de.cubeisland.cubeengine.fun;

import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import org.bukkit.permissions.Permissible;

import java.util.Locale;

public enum FunPerm implements Permission  
{
    EXPLOSION_OTHER,
    EXPLOSION_PLAYER_DAMAGE,
    EXPLOSION_BLOCK_DAMAGE,
    EXPLOSION_FIRE
    ;
        
    private String permission;
    private PermDefault def;

    private FunPerm()
    {
        this(PermDefault.OP);
    }

    private FunPerm(PermDefault def)
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
    public PermDefault getPermissionDefault()
    {
        return this.def;
    }
}
