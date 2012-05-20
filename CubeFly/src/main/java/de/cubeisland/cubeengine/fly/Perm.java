package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.permission.CubePermission;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author Faithcaio
 */
public enum Perm
{
    COMMAND_FLY("command.fly",PermissionDefault.OP),
    COMMAND_FLY_BYPASS("command.fly.bypass",PermissionDefault.OP)
    ;
    
    private String BASE = "cubeengine.fly.";
    private CubePermission permission;
    
    private Perm(String permission, PermissionDefault def)
    {
        this.permission = new CubePermission(BASE+permission, def);
        CubeCore.getInstance().getPermissionRegistration().registerPermission(this.permission);
    }
    
    public boolean check(Player player)
    {
        return permission.isAuthorized(player);
    }
}
