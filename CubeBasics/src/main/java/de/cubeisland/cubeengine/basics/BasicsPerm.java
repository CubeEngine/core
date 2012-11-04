package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.core.permission.Permission;
import java.util.Locale;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;

public enum BasicsPerm implements Permission
{
    COMMAND_ENCHANT_UNSAFE,
    COMMAND_GIVE_BLACKLIST,
    COMMAND_ITEM_BLACKLIST,
    COMMAND_GAMEMODE_OTHER,
    COMMAND_PTIME_OTHER,
    COMMAND_CLEARINVENTORY_OTHER,
    COMMAND_KILL_PREVENT(PermissionDefault.FALSE),
    COMMAND_KILL_ALL,
    COMMAND_INVSEE_MODIFY, // allows to modify the inventory
    COMMAND_INVSEE_PREVENTMODIFY(PermissionDefault.FALSE), // prevents from modifying the inventory
    COMMAND_INVSEE_NOTIFY, // notify if someone looks into your inventory
    COMMAND_KICK_ALL,
    COMMAND_SPAWN_ALL, // spawn all players
    COMMAND_SPAWN_PREVENT, //prevents from being spawned
    COMMAND_SPAWN_FORCE, //forces someone to spawn
    COMMAND_TP_FORCE, // ignore tp prevent perms
    COMMAND_TP_FORCE_TPALL, // ignore tpall prevent perms
    COMMAND_TPHERE_FORCE, // ignore tphere prevent perms
    COMMAND_TPHEREALL_FORCE, // ignore tphere prevent perms
    COMMAND_TP_OTHER, // can tp other person
    COMMAND_TP_PREVENT_TP(PermissionDefault.FALSE), // can not be tped except forced
    COMMAND_TP_PREVENT_TPTO(PermissionDefault.FALSE), // can not be tped to except forced
    COMMAND_TPHERE_PREVENT(PermissionDefault.FALSE), // can not tpedhere except forced
    COMMAND_TPHEREALL_PREVENT(PermissionDefault.FALSE), // can not tpedhere(all) except forced
    COMMAND_BACK_ONDEATH,
    POWERTOOL_USE, //allows to use powertools
    ;
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