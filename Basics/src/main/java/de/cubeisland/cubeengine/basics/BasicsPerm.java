package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionContainer;

import org.bukkit.permissions.Permissible;

import java.util.Locale;

import static de.cubeisland.cubeengine.core.permission.PermDefault.FALSE;
import static de.cubeisland.cubeengine.core.permission.PermDefault.OP;

public class BasicsPerm extends PermissionContainer
{
    public BasicsPerm(Module module)
    {
        super(module);
        this.registerAllPermissions();
    }

    public static final Permission BASICS = BASEPERM.createAbstractChild("basics");
    public static final Permission KITS = BASICS.createAbstractChild("kits");
    public static final Permission COMMAND = BASICS.createAbstractChild("command");

    public static final Permission COMMAND_ENCHANT_UNSAFE = COMMAND.createChild("enchant.unsafe");
    public static final Permission COMMAND_GIVE_BLACKLIST = COMMAND.createChild("give.blacklist");

    private static final Permission COMMAND_ITEM = COMMAND.createAbstractChild("item");
    public static final Permission COMMAND_ITEM_BLACKLIST = COMMAND_ITEM.createChild("blacklist");
    public static final Permission COMMAND_ITEM_ENCHANTMENTS = COMMAND_ITEM.createChild("enchantments");
    public static final Permission COMMAND_ITEM_ENCHANTMENTS_UNSAFE = COMMAND_ITEM.createChild("enchantments.unsafe");

    public static final Permission COMMAND_GAMEMODE_OTHER = COMMAND.createChild("gamemode.other");
    public static final Permission COMMAND_PTIME_OTHER = COMMAND.createChild("ptime.other");
    public static final Permission COMMAND_CLEARINVENTORY_OTHER = COMMAND.createChild("clearinventory.notify");
    public static final Permission COMMAND_CLEARINVENTORY_NOTIFY = COMMAND.createChild("clearinventory.other");

    private static final Permission COMMAND_KILL = COMMAND.createAbstractChild("kill");
    public static final Permission COMMAND_KILL_PREVENT = COMMAND_KILL.createChild("prevent",FALSE);
    public static final Permission COMMAND_KILL_ALL = COMMAND_KILL.createChild("all");
    public static final Permission COMMAND_KILL_FORCE = COMMAND_KILL.createChild("force");
    public static final Permission COMMAND_KILL_LIGHTNING = COMMAND_KILL.createChild("lightning");

    private static final Permission COMMAND_INVSEE = COMMAND.createAbstractChild("invsee");
    public static final Permission COMMAND_INVSEE_MODIFY = COMMAND_INVSEE.createChild("modify");
    public static final Permission COMMAND_INVSEE_MODIFY_PREVENT = COMMAND_INVSEE.createChild("modify.prevent");
    public static final Permission COMMAND_INVSEE_MODIFY_FORCE = COMMAND_INVSEE.createChild("modify.force");
    public static final Permission COMMAND_INVSEE_NOTIFY = COMMAND_INVSEE.createChild("notify");

    public static final Permission COMMAND_KICK_ALL = COMMAND.createChild("kick.all");

    private static final Permission COMMAND_SPAWN = COMMAND.createAbstractChild("spawn");
    public static final Permission COMMAND_SPAWN_ALL = COMMAND_SPAWN.createChild("all");
    public static final Permission COMMAND_SPAWN_PREVENT = COMMAND_SPAWN.createChild("prevent");
    public static final Permission COMMAND_SPAWN_FORCE = COMMAND_SPAWN.createChild("force");

    private static final Permission COMMAND_TP = COMMAND.createAbstractChild("tp");
    public static final Permission COMMAND_TP_FORCE = COMMAND_TP.createChild("force");
    public static final Permission COMMAND_TP_FORCE_TPALL = COMMAND_TP.createChild("force.tpall");
    public static final Permission COMMAND_TP_OTHER = COMMAND_TP.createChild("other");
    public static final Permission COMMAND_TP_PREVENT_TP = COMMAND_TP.createChild("prevent.tp",FALSE);
    public static final Permission COMMAND_TP_PREVENT_TPTO = COMMAND_TP.createChild("prevent.tpto",FALSE);

    private static final Permission COMMAND_TPHERE = COMMAND.createAbstractChild("tphere");
    public static final Permission COMMAND_TPHERE_FORCE = COMMAND_TPHERE.createChild("force");
    public static final Permission COMMAND_TPHERE_PREVENT = COMMAND_TPHERE.createChild("prevent",FALSE);

    private static final Permission COMMAND_TPHEREALL = COMMAND.createAbstractChild("tphereall");
    public static final Permission COMMAND_TPHEREALL_FORCE = COMMAND_TPHEREALL.createChild("force");
    public static final Permission COMMAND_TPHEREALL_PREVENT = COMMAND_TPHEREALL.createChild("prevent",FALSE);

    public static final Permission COMMAND_BACK_ONDEATH = COMMAND.createChild("back.ondeath");

    public static final Permission COMMAND_KIT_GIVE_FORCE = COMMAND.createChild("kit.give.force");
    public static final Permission COMMAND_STACK_FULLSTACK = COMMAND.createChild("stack.fullstack");
    public static final Permission COMMAND_WALKSPEED_OTHER = COMMAND.createChild("walkspeed.other");

    private static final Permission COMMAND_GOD = COMMAND.createAbstractChild("god");
    public static final Permission COMMAND_GOD_OTHER = COMMAND_GOD.createChild("other");
    public static final Permission COMMAND_GOD_KEEP = COMMAND_GOD.createChild("keep");

    private static final Permission COMMAND_BUTCHER = COMMAND.createAbstractChild("butcher");
    private static final Permission COMMAND_BUTCHER_FLAG = COMMAND_BUTCHER.createAbstractChild("flag");
    public static final Permission COMMAND_BUTCHER_FLAG_PET = COMMAND_BUTCHER_FLAG.createChild("pet");
    public static final Permission COMMAND_BUTCHER_FLAG_ANIMAL = COMMAND_BUTCHER_FLAG.createChild("animal");
    public static final Permission COMMAND_BUTCHER_FLAG_LIGHTNING = COMMAND_BUTCHER_FLAG.createChild("lightning");
    public static final Permission COMMAND_BUTCHER_FLAG_GOLEM = COMMAND_BUTCHER_FLAG.createChild("golem");
    public static final Permission COMMAND_BUTCHER_FLAG_ALLTYPE = COMMAND_BUTCHER_FLAG.createChild("alltype");
    public static final Permission COMMAND_BUTCHER_FLAG_ALL = COMMAND_BUTCHER_FLAG.createChild("all");
    public static final Permission COMMAND_BUTCHER_FLAG_OTHER = COMMAND_BUTCHER_FLAG.createChild("other");
    public static final Permission COMMAND_BUTCHER_FLAG_NPC = COMMAND_BUTCHER_FLAG.createChild("npc");

    private static final Permission COMMAND_FEED = COMMAND.createAbstractChild("feed");
    public static final Permission COMMAND_FEED_ALL = COMMAND_FEED.createChild("all");
    public static final Permission COMMAND_FEED_OTHER = COMMAND_FEED.createChild("other");

    private static final Permission COMMAND_STARVE = COMMAND.createAbstractChild("starve");
    public static final Permission COMMAND_STARVE_ALL = COMMAND_STARVE.createChild("all");
    public static final Permission COMMAND_STARVE_OTHER = COMMAND_STARVE.createChild("other");

    public static final Permission COMMAND_HEAL_ALL = COMMAND.createChild("heal.all");
    public static final Permission COMMAND_FLY_OTHER = COMMAND.createChild("fly.other");

    private static final Permission COMPASS_JUMPTO = BASICS.createAbstractChild("compass.jumpto");
    public static final Permission COMPASS_JUMPTO_LEFT = COMPASS_JUMPTO.createChild("left");
    public static final Permission COMPASS_JUMPTO_RIGHT = COMPASS_JUMPTO.createChild("right");

    public static final Permission POWERTOOL_USE = BASICS.createChild("powertool.use");
    public static final Permission AFK_PREVENT_AUTOUNAFK = BASICS.createChild("afk.prevent.autounafk");//TODO prevent autoafk
    public static final Permission WALKSPEED_ISALLOWED = BASICS.createChild("walkspeed.isallowed");
    public static final Permission SIGN_COLORED = BASICS.createChild("sign.colored");
    public static final Permission CHANGEPAINTING = BASICS.createChild("changepainting");
    public static final Permission KICK_RECEIVEMESSAGE = BASICS.createChild("kick.receivemessage");
    public static final Permission FLY_CANFLY = BASICS.createChild("fly.canfly");
}
