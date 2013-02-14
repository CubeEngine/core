package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import org.bukkit.permissions.Permissible;

import java.util.Locale;

import static de.cubeisland.cubeengine.core.permission.PermDefault.FALSE;
import static de.cubeisland.cubeengine.core.permission.PermDefault.OP;

public enum BasicsPerm implements Permission
{
    COMMAND_ENCHANT_UNSAFE,
    COMMAND_GIVE_BLACKLIST,
    COMMAND_ITEM_BLACKLIST,
    COMMAND_ITEM_ENCHANTMENTS,
    COMMAND_ITEM_ENCHANTMENTS_UNSAFE,
    COMMAND_GAMEMODE_OTHER,
    COMMAND_PTIME_OTHER,
    COMMAND_CLEARINVENTORY_OTHER,
    COMMAND_CLEARINVENTORY_NOTIFY,
    COMMAND_KILL_PREVENT(FALSE),
    COMMAND_KILL_ALL,
    COMMAND_INVSEE_MODIFY, // allows to modify the inventory
    COMMAND_INVSEE_MODIFY_PREVENT(FALSE), // prevents from modifying the inventory
    COMMAND_INVSEE_MODIFY_FORCE,
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
    COMMAND_TP_PREVENT_TP(FALSE), // can not be tped except forced
    COMMAND_TP_PREVENT_TPTO(FALSE), // can not be tped to except forced
    COMMAND_TPHERE_PREVENT(FALSE), // can not tpedhere except forced
    COMMAND_TPHEREALL_PREVENT(FALSE), // can not tpedhere(all) except forced
    COMMAND_BACK_ONDEATH,
    POWERTOOL_USE, //allows to use powertools
    COMMAND_KIT_GIVE_FORCE,
    COMMAND_GOD_OTHER,
    COMMAND_GOD_KEEP,
    COMMAND_STACK_FULLSTACK,
    COMMAND_BUTCHER_FLAG_PET,
    COMMAND_BUTCHER_FLAG_ANIMAL,
    COMMAND_BUTCHER_FLAG_LIGHTNING,
    COMMAND_BUTCHER_FLAG_GOLEM,
    COMMAND_BUTCHER_FLAG_ALLTYPE,
    COMMAND_BUTCHER_FLAG_ALL,
    COMMAND_KILL_FORCE,
    COMMAND_KILL_LIGHTNING,
    COMPASS_JUMPTO_LEFT,
    COMPASS_JUMPTO_RIGHT,
    COMMAND_BUTCHER_FLAG_OTHER,
    COMMAND_BUTCHER_FLAG_NPC,
    COMMAND_WALKSPEED_OTHER,
    WALKSPEED_ISALLOWED,
    SIGN_COLORED,
    KICK_RECEIVEMESSAGE;

    private String permission;
    private PermDefault def;

    private BasicsPerm()
    {
        this(OP);
    }

    private BasicsPerm(PermDefault def)
    {
        this.permission = "cubeengine.basics." + this.name().
            toLowerCase(Locale.ENGLISH).replace('_', '.');
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
