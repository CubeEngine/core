/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.basics;

import java.util.concurrent.TimeUnit;


import de.cubeisland.engine.core.permission.ParentPermission;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;
import de.cubeisland.engine.core.permission.WildcardPermission;
import de.cubeisland.engine.core.util.Profiler;
import de.cubeisland.engine.basics.command.teleport.TpWorldPermissions;

import static de.cubeisland.engine.core.permission.PermDefault.FALSE;

public class BasicsPerm extends PermissionContainer<Basics>
{
    public BasicsPerm(Basics module)
    {
        module.getLog().trace("{} ms - Basics.Permission-register", Profiler.getCurrentDelta("basicsEnable", TimeUnit.MILLISECONDS));
        this.registerAllPermissions(module);
        new TpWorldPermissions(module); // per world permissions
    }

    public static final WildcardPermission COMMAND = Permission.createWildcard("command");

    public static final Permission COMMAND_ENCHANT_UNSAFE = COMMAND.childWildcard("enchant").child("unsafe");

    public static final Permission COMMAND_LAG_RESET = COMMAND.childWildcard("lag").child("reset");

    /**
     * Allows to create items that are blacklisted
     */
    public static final Permission ITEM_BLACKLIST = Permission.create("item-blacklist");

    private static final WildcardPermission COMMAND_ITEM = COMMAND.childWildcard("item");
    public static final Permission COMMAND_ITEM_ENCHANTMENTS = COMMAND_ITEM.child("enchantments");
    public static final Permission COMMAND_ITEM_ENCHANTMENTS_UNSAFE = COMMAND_ITEM.child("enchantments.unsafe");

    private static final WildcardPermission COMMAND_GAMEMODE = COMMAND.childWildcard("gamemode");
    /**
     * Allows to change the game-mode of other players too
     */
    public static final Permission COMMAND_GAMEMODE_OTHER = COMMAND_GAMEMODE.child("other");
    /**
     * Without this permission the players game-mode will be reset when leaving the server or changing the world
     */
    public static final Permission COMMAND_GAMEMODE_KEEP = COMMAND_GAMEMODE.child("keep");

    public static final Permission COMMAND_PTIME_OTHER = COMMAND.child("ptime.other");

    private static final WildcardPermission COMMAND_CLEARINVENTORY = COMMAND.childWildcard("clearinventory");
    /**
     * Allows clearing the inventory of other players
     */
    public static final Permission COMMAND_CLEARINVENTORY_OTHER = COMMAND_CLEARINVENTORY.child("notify");
    /**
     * Notifies you if your inventory got cleared by someone else
     */
    public static final Permission COMMAND_CLEARINVENTORY_NOTIFY = COMMAND_CLEARINVENTORY.child("other");
    /**
     * Prevents the other player being notified when his inventory got cleared
     */
    public static final Permission COMMAND_CLEARINVENTORY_QUIET = COMMAND_CLEARINVENTORY.child("quiet");
    /**
     * Prevents your inventory from being cleared unless forced
     */
    public static final Permission COMMAND_CLEARINVENTORY_PREVENT = COMMAND_CLEARINVENTORY.newPerm("prevent", FALSE);
    /**
     * Clears an inventory even if the player has the prevent permission
     */
    public static final Permission COMMAND_CLEARINVENTORY_FORCE = COMMAND_CLEARINVENTORY.child("force");

    private static final WildcardPermission COMMAND_KILL = COMMAND.childWildcard("kill");
    /**
     * Prevents from being killed by the kill command unless forced
     */
    public static final Permission COMMAND_KILL_PREVENT = COMMAND_KILL.newPerm("prevent", FALSE);
    /**
     * Kills a player even if the player has the prevent permission
     */
    public static final Permission COMMAND_KILL_FORCE = COMMAND_KILL.child("force");
    /**
     * Allows killing all players currently online
     */
    public static final Permission COMMAND_KILL_ALL = COMMAND_KILL.child("all");
    /**
     * Allows killing a player with a lightning strike
     */
    public static final Permission COMMAND_KILL_LIGHTNING = COMMAND_KILL.child("lightning");
    /**
     * Prevents the other player being notified who killed him
     */
    public static final Permission COMMAND_KILL_QUIET = COMMAND_KILL.child("quiet");
    /**
     * Shows who killed you
     */
    public static final Permission COMMAND_KILL_NOTIFY = COMMAND_KILL.child("notify");

    private static final WildcardPermission COMMAND_INVSEE = COMMAND.childWildcard("invsee");
    /**
     * Allows to modify the inventory of other players
     */
    public static final Permission COMMAND_INVSEE_MODIFY = COMMAND_INVSEE.child("modify");
    public static final Permission COMMAND_INVSEE_ENDERCHEST = COMMAND_INVSEE.child("ender");
    /**
     * Prevents an inventory from being modified unless forced
     */
    public static final Permission COMMAND_INVSEE_MODIFY_PREVENT = COMMAND_INVSEE.newPerm("modify.prevent", FALSE);
    /**
     * Allows modifying an inventory even if the player has the prevent permission
     */
    public static final Permission COMMAND_INVSEE_MODIFY_FORCE = COMMAND_INVSEE.child("modify.force");
    /**
     * Notifies you when someone is looking into your inventory
     */
    public static final Permission COMMAND_INVSEE_NOTIFY = COMMAND_INVSEE.child("notify");
    /**
     * Prevents the other player from being notified when looking into his inventory
     */
    public static final Permission COMMAND_INVSEE_QUIET = COMMAND_INVSEE.child("quiet");

    private static final WildcardPermission COMMAND_SPAWN = COMMAND.childWildcard("spawn");
    /**
     * Allows to teleport all online players to the spawn of the main world
     */
    public static final Permission COMMAND_SPAWN_ALL = COMMAND_SPAWN.child("all");
    /**
     * Prevents from being teleported to spawn by someone else
     */
    public static final Permission COMMAND_SPAWN_PREVENT = COMMAND_SPAWN.child("prevent");
    /**
     * Allows teleporting a player to spawn even if the player has the prevent permission
     */
    public static final Permission COMMAND_SPAWN_FORCE = COMMAND_SPAWN.child("force");

    private static final WildcardPermission COMMAND_TP = COMMAND.childWildcard("tp");
    /**
     * Ignores all prevent permissions when using the /tp command
     */
    public static final Permission COMMAND_TP_FORCE = COMMAND_TP.child("force");
    /**
     * Allows teleporting another player
     */
    public static final Permission COMMAND_TP_OTHER = COMMAND_TP.child("other");

    private static final Permission TELEPORT = Permission.createWildcard("teleport");
    private static final WildcardPermission TELEPORT_PREVENT = TELEPORT.newWildcard("prevent");
    /**
     * Prevents from being teleported by someone else
     */
    public static final Permission TELEPORT_PREVENT_TP = TELEPORT_PREVENT.child("tp",FALSE);
    /**
     * Prevents from teleporting to you
     */
    public static final Permission TELEPORT_PREVENT_TPTO = TELEPORT_PREVENT.child("tpto",FALSE);

    private static final WildcardPermission COMMAND_TPALL = COMMAND.childWildcard("tpall");
    /**
     * Ignores all prevent permissions when using the /tpall command
     */
    public static final Permission COMMAND_TPALL_FORCE = COMMAND_TPALL.child("force");

    private static final WildcardPermission COMMAND_TPHERE = COMMAND.childWildcard("tphere");
    /**
     * Ignores all prevent permissions when using the /tphere command
     */
    public static final Permission COMMAND_TPHERE_FORCE = COMMAND_TPHERE.child("force");

    private static final WildcardPermission COMMAND_TPHEREALL = COMMAND.childWildcard("tphereall");
    /**
     * Ignores all prevent permissions when using the /tphereall command
     */
    public static final Permission COMMAND_TPHEREALL_FORCE = COMMAND_TPHEREALL.child("force");

    private static final WildcardPermission COMMAND_BACK = COMMAND.childWildcard("back");

    /**
     * Allows using the back command
     */
    public static final Permission COMMAND_BACK_USE = COMMAND_BACK.child("use");
    /**
     * Allows using the back command after dieing (if this is not set you won't be able to tp back to your deathpoint)
     */
    public static final Permission COMMAND_BACK_ONDEATH = COMMAND_BACK.child("ondeath");


    private static final WildcardPermission COMMAND_GOD = COMMAND.childWildcard("god");
    /**
     * Allows to enable god-mode for other players
     */
    public static final Permission COMMAND_GOD_OTHER = COMMAND_GOD.child("other");
    /**
     * Without this permission the player will loose god-mode leaving the server or changing the world
     */
    public static final Permission COMMAND_GOD_KEEP = COMMAND_GOD.child("keep");

    private static final WildcardPermission COMMAND_AFK = COMMAND.childWildcard("afk");
    private static final WildcardPermission COMMAND_AFK_PREVENT = COMMAND_AFK.newWildcard("prevent");
    /**
     * Prevents from being displayed as no longer afk automatically unless using chat
     */
    public static final Permission PREVENT_AUTOUNAFK = COMMAND_AFK_PREVENT.child("autounafk", FALSE);
    /**
     * Prevents from being displayed as afk automatically
     */
    public static final Permission PREVENT_AUTOAFK = COMMAND_AFK_PREVENT.child("autoafk", FALSE);

    /**
     * Allows to set or unset the afk status of other players
     */
    public static final Permission COMMAND_AFK_OTHER = COMMAND_AFK.child("other");

    public static final Permission COMMAND_IGNORE_PREVENT = COMMAND.childWildcard("ignore").child("prevent",FALSE);

    private static final WildcardPermission COMMAND_BUTCHER = COMMAND.childWildcard("butcher");
    private static final WildcardPermission COMMAND_BUTCHER_FLAG = COMMAND_BUTCHER.childWildcard("flag");
    public static final Permission COMMAND_BUTCHER_FLAG_PET = COMMAND_BUTCHER_FLAG.child("pet");
    public static final Permission COMMAND_BUTCHER_FLAG_ANIMAL = COMMAND_BUTCHER_FLAG.child("animal");
    public static final Permission COMMAND_BUTCHER_FLAG_LIGHTNING = COMMAND_BUTCHER_FLAG.child("lightning");
    public static final Permission COMMAND_BUTCHER_FLAG_GOLEM = COMMAND_BUTCHER_FLAG.child("golem");
    public static final Permission COMMAND_BUTCHER_FLAG_ALLTYPE = COMMAND_BUTCHER_FLAG.child("alltype");
    public static final Permission COMMAND_BUTCHER_FLAG_ALL = COMMAND_BUTCHER_FLAG.child("all");
    public static final Permission COMMAND_BUTCHER_FLAG_OTHER = COMMAND_BUTCHER_FLAG.child("other");
    public static final Permission COMMAND_BUTCHER_FLAG_NPC = COMMAND_BUTCHER_FLAG.child("npc");
    public static final Permission COMMAND_BUTCHER_FLAG_MONSTER = COMMAND_BUTCHER_FLAG.child("monster");
    public static final Permission COMMAND_BUTCHER_FLAG_BOSS = COMMAND_BUTCHER_FLAG.child("boss");

    private static final WildcardPermission COMMAND_FEED = COMMAND.childWildcard("feed");
    public static final Permission COMMAND_FEED_OTHER = COMMAND_FEED.child("other");

    private static final WildcardPermission COMMAND_STARVE = COMMAND.childWildcard("starve");
    public static final Permission COMMAND_STARVE_OTHER = COMMAND_STARVE.child("other");

    private static final WildcardPermission COMMAND_HEAL = COMMAND.childWildcard("heal");
    public static final Permission COMMAND_HEAL_OTHER = COMMAND_HEAL.child("other");

    private static final WildcardPermission COMMAND_FLY = COMMAND.childWildcard("fly");
    public static final Permission COMMAND_FLY_KEEP = COMMAND_FLY.child("keep");
    public static final Permission COMMAND_FLY_OTHER = COMMAND_FLY.child("other");

    private static final WildcardPermission COMPASS_JUMPTO = Permission.createWildcard("compass.jumpto");
    public static final Permission COMPASS_JUMPTO_LEFT = COMPASS_JUMPTO.child("left");
    public static final Permission COMPASS_JUMPTO_RIGHT = COMPASS_JUMPTO.child("right");

    private static final WildcardPermission COMMAND_KICK = COMMAND.childWildcard("kick");
    public static final Permission COMMAND_KICK_ALL = COMMAND_KICK.child("all");
    public static final Permission COMMAND_KICK_NOREASON = COMMAND_KICK.newPerm("noreason");

    public static final Permission COMMAND_STACK_FULLSTACK = COMMAND.childWildcard("stack").child("fullstack");

    public static final Permission COMMAND_BAN_NOREASON = COMMAND.childWildcard("ban").child("noreason");
    public static final Permission COMMAND_IPBAN_NOREASON = COMMAND.childWildcard("ipban").child("noreason",FALSE);
    public static final Permission COMMAND_TEMPBAN_NOREASON = COMMAND.childWildcard("tempban").child("noreason",FALSE);
    
    /**
     * Allows to change the walkspeed of other players
     */
    public static final Permission COMMAND_WALKSPEED_OTHER = COMMAND.childWildcard("walkspeed").child("other");

    /**
     * Allows writing colored signs
     */
    public static final ParentPermission SIGN_COLORED = Permission.createParent("sign.colored");
    public static final Permission SIGN_COLORED_BLACK = SIGN_COLORED.newPerm("black");
    public static final Permission SIGN_COLORED_DARK_BLUE = SIGN_COLORED.newPerm("dark-blue");
    public static final Permission SIGN_COLORED_DARK_GREEN = SIGN_COLORED.newPerm("dark-green");
    public static final Permission SIGN_COLORED_DARK_AQUA = SIGN_COLORED.newPerm("dark-aqua");
    public static final Permission SIGN_COLORED_DARK_RED = SIGN_COLORED.newPerm("dark-red");
    public static final Permission SIGN_COLORED_DARK_PURPLE = SIGN_COLORED.newPerm("dark-purple");
    public static final Permission SIGN_COLORED_GOLD = SIGN_COLORED.newPerm("gold");
    public static final Permission SIGN_COLORED_GRAY = SIGN_COLORED.newPerm("gray");
    public static final Permission SIGN_COLORED_DARK_GRAY = SIGN_COLORED.newPerm("dark-gray");
    public static final Permission SIGN_COLORED_BLUE = SIGN_COLORED.newPerm("blue");
    public static final Permission SIGN_COLORED_GREEN = SIGN_COLORED.newPerm("green");
    public static final Permission SIGN_COLORED_AQUA = SIGN_COLORED.newPerm("aqua");
    public static final Permission SIGN_COLORED_RED = SIGN_COLORED.newPerm("red");
    public static final Permission SIGN_COLORED_LIGHT_PURPLE = SIGN_COLORED.newPerm("light-purple");
    public static final Permission SIGN_COLORED_YELLOW = SIGN_COLORED.newPerm("yellow");
    public static final Permission SIGN_COLORED_WHITE = SIGN_COLORED.newPerm("white");

    public static final Permission SIGN_COLORED_OBFUSCATED = SIGN_COLORED.newPerm("obfuscated");
    public static final Permission SIGN_COLORED_BOLD = SIGN_COLORED.newPerm("bold");
    public static final Permission SIGN_COLORED_STRIKE = SIGN_COLORED.newPerm("strike");
    public static final Permission SIGN_COLORED_UNDERLINE = SIGN_COLORED.newPerm("underline");
    public static final Permission SIGN_COLORED_ITALIC = SIGN_COLORED.newPerm("italic");
    public static final Permission SIGN_COLORED_RESET = SIGN_COLORED.newPerm("reset");

    public static final Permission CHANGEPAINTING = Permission.create("changepainting");
    public static final Permission KICK_RECEIVEMESSAGE = Permission.create("kick.receivemessage");
    public static final Permission BAN_RECEIVEMESSAGE = Permission.create("ban.receivemessage");

    public static Permission OVERSTACKED_ANVIL_AND_BREWING = Permission.create("allow-overstacked-anvil-and-brewing");

    static
    {
        SIGN_COLORED.attach(SIGN_COLORED_BLACK, SIGN_COLORED_DARK_BLUE, SIGN_COLORED_DARK_GREEN,
                            SIGN_COLORED_DARK_AQUA, SIGN_COLORED_DARK_RED, SIGN_COLORED_DARK_PURPLE,
                            SIGN_COLORED_GOLD, SIGN_COLORED_GRAY, SIGN_COLORED_DARK_GRAY,
                            SIGN_COLORED_BLUE, SIGN_COLORED_GREEN, SIGN_COLORED_AQUA,
                            SIGN_COLORED_RED, SIGN_COLORED_LIGHT_PURPLE, SIGN_COLORED_YELLOW,
                            SIGN_COLORED_WHITE, SIGN_COLORED_OBFUSCATED, SIGN_COLORED_BOLD,
                            SIGN_COLORED_STRIKE, SIGN_COLORED_UNDERLINE, SIGN_COLORED_ITALIC, SIGN_COLORED_RESET);
    }
}
