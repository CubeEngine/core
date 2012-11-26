package de.cubeisland.cubeengine.rulebook;

import de.cubeisland.cubeengine.core.permission.Permission;
import java.util.Locale;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;

public enum RulebookPermissions implements Permission
{
    COMMAND_GET_OTHER
    ;

    private String permission;
    private PermissionDefault def;
        
    private RulebookPermissions()
    {
        this(PermissionDefault.OP);
    }
    
    private RulebookPermissions(PermissionDefault def)
    {
        this.permission = "cubeengine.rulebook." + this.name().toLowerCase(Locale.ENGLISH).replace('_', '.');
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
