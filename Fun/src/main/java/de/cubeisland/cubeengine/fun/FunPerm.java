package de.cubeisland.cubeengine.fun;

import de.cubeisland.cubeengine.core.permission.PermDefault;

import java.util.Locale;
import org.bukkit.permissions.Permissible;

public enum FunPerm implements Permission
{
    COMMAND_EXPLOSION_OTHER,
    COMMAND_EXPLOSION_PLAYER_DAMAGE,
    COMMAND_EXPLOSION_BLOCK_DAMAGE,
    COMMAND_EXPLOSION_FIRE,
    COMMAND_HAT_OTHER,
    COMMAND_HAT_ITEM,
    COMMAND_HAT_QUIET,
    COMMAND_HAT_NOTIFY(PermDefault.TRUE),
    COMMAND_LIGHTNING_PLAYER_DAMAGE,
    COMMAND_LIGHTNING_UNSAFE,
    COMMAND_THROW_UNSAFE;

    private String permission;
    private PermDefault def;

    public static final String BASE = "cubeengine.fun.";

    private FunPerm()
    {
        this(PermDefault.OP);
    }

    private FunPerm(PermDefault def)
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
