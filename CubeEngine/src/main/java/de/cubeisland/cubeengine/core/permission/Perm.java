package de.cubeisland.cubeengine.core.permission;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author Anselm Brehme
 */
public enum Perm implements Permission
{
    USE("use", PermissionDefault.TRUE), //without this Permission a Player wont be able to use any CubeEngine module
    //If you wish someone NEVER to use a specific module take those:
    MODULE_AUCTIONS("module.auctions", PermissionDefault.TRUE),
    MODULE_BAN("module.ban", PermissionDefault.TRUE),
    MODULE_CHAT("module.chat", PermissionDefault.TRUE),
    MODULE_CONOMY("module.conomy", PermissionDefault.TRUE),
    MODULE_FLY("module.fly", PermissionDefault.TRUE),
    MODULE_FUN("module.fun", PermissionDefault.TRUE),
    MODULE_GREYLIST("module.greylist", PermissionDefault.TRUE),//RENAME ? CubeBlock
    MODULE_GUARD("module.guard", PermissionDefault.TRUE),
    MODULE_IRC("module.irc", PermissionDefault.TRUE),
    MODULE_LOG("module.log", PermissionDefault.TRUE),
    MODULE_MARKET("module.market", PermissionDefault.TRUE),
    MODULE_PERMISSIONS("module.permissions", PermissionDefault.TRUE),
    MODULE_RPG("module.rpg", PermissionDefault.TRUE),
    MODULE_STATS("module.stats", PermissionDefault.TRUE),
    MODULE_VANISH("module.vanish", PermissionDefault.TRUE),
    MODULE_WAR("module.war", PermissionDefault.TRUE),
    ;
    
    
    private String BASE = "cubeengine.core.";
    private String permission;
    private PermissionDefault permissionDefault;

    private Perm(String permission, PermissionDefault permissionDefault)
    {
        this.permission = BASE + permission;
        this.permissionDefault = permissionDefault;
    }

    public boolean isAuthorized(Permissible player)
    {
        return player.hasPermission(permission);
    }

    public String getPermission()
    {
        return this.permission;
    }

    public PermissionDefault getPermissionDefault()
    {
        return this.permissionDefault;
    }
}
