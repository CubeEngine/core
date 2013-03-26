package de.cubeisland.cubeengine.chat;

import de.cubeisland.cubeengine.core.permission.PermDefault;

import org.bukkit.permissions.Permissible;

import java.util.Locale;

public enum ChatPerm implements Permission
{
    COLOR;

    private final String permission;
    private final PermDefault def;

    private ChatPerm()
    {
        this(PermDefault.OP);
    }

    private ChatPerm(PermDefault def)
    {
        this.permission = "cubeengine.fun." + this.name().toLowerCase(Locale.ENGLISH).replace('_', '.');
        this.def = def;
    }

    @Override
    public boolean isAuthorized(Permissible permissible)
    {
        return permissible.hasPermission(this.permission);
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
