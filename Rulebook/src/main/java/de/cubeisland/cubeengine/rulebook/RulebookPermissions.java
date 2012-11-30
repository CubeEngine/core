package de.cubeisland.cubeengine.rulebook;

import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import org.bukkit.permissions.Permissible;

import java.util.Locale;

public enum RulebookPermissions implements Permission
{
    COMMAND_GET_OTHER; // TODO serious? a permission enum for ONE permission?!

    private String permission;
    private PermDefault def;
        
    private RulebookPermissions()
    {
        this(PermDefault.OP);
    }
    
    private RulebookPermissions(PermDefault def)
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
    public PermDefault getPermissionDefault()
    {
        return this.def;
    }
    
}
