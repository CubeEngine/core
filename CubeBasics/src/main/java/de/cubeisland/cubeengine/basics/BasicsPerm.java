package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.core.permission.Permission;
import java.util.Locale;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author Anselm Brehme
 */
public enum BasicsPerm implements Permission
{
    COMMAND_ENCHANT_UNSAFE,
    COMMAND_GIVE_BLACKLIST,
    COMMAND_ITEM_BLACKLIST,
    COMMAND_GAMEMODE_OTHER,
    COMMAND_PTIME_OTHER,
    COMMAND_CLEARINVENTORY_OTHER,
    COMMAND_KILL_EXEMPT,
    COMMAND_INVSEE_MODIFY,
    COMMAND_INVSEE_PREVENTMODIFY,
    COMMAND_KICK_ALL,;
    private String permission;
    private PermissionDefault def;

    private BasicsPerm()
    {
        this(PermissionDefault.OP);
    }

    private BasicsPerm(PermissionDefault def)
    {
        this.permission = "cubeengine.basics." + this.name().
            toLowerCase(Locale.ENGLISH).replace('_', '.');
        this.def = def;
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
        return this.def;
    }
}
