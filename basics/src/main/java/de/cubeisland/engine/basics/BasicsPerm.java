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


import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;
import de.cubeisland.engine.core.util.Profiler;
import de.cubeisland.engine.basics.command.teleport.TpWorldPermissions;

import static de.cubeisland.engine.core.permission.PermDefault.FALSE;

public class BasicsPerm extends PermissionContainer<Basics>
{
    public BasicsPerm(Basics module)
    {
        super(module);
        module.getLog().trace("{} ms - Basics.Permission-register", Profiler.getCurrentDelta("basicsEnable", TimeUnit.MILLISECONDS));
        this.bindToModule(COMMAND,ITEM_BLACKLIST,TELEPORT,COMPASS_JUMPTO,SIGN_COLORED,CHANGEPAINTING,KICK_RECEIVEMESSAGE, BAN_RECEIVEMESSAGE,
                          COMMAND_KILL_PREVENT, COMMAND_CLEARINVENTORY_PREVENT, COMMAND_INVSEE_MODIFY_PREVENT,
                          COMMAND_INVSEE_ENDERCHEST, COMMAND_KICK_NOREASON, TELEPORT_PREVENT, COMMAND_AFK_PREVENT,
                          SIGN_COLORED_BLACK, SIGN_COLORED_DARK_BLUE, SIGN_COLORED_DARK_GREEN,
                          SIGN_COLORED_DARK_AQUA, SIGN_COLORED_DARK_RED, SIGN_COLORED_DARK_PURPLE,
                          SIGN_COLORED_GOLD, SIGN_COLORED_GRAY, SIGN_COLORED_DARK_GRAY,
                          SIGN_COLORED_BLUE, SIGN_COLORED_GREEN, SIGN_COLORED_AQUA,
                          SIGN_COLORED_RED, SIGN_COLORED_LIGHT_PURPLE, SIGN_COLORED_YELLOW,
                          SIGN_COLORED_WHITE, SIGN_COLORED_OBFUSCATED, SIGN_COLORED_BOLD,
                          SIGN_COLORED_STRIKE, SIGN_COLORED_UNDERLINE, SIGN_COLORED_ITALIC, SIGN_COLORED_RESET,
                          OVERSTACKED_ANVIL_AND_BREWING
                          );

        this.registerAllPermissions();

        new TpWorldPermissions(module); // per world permissions
    }

    public static final Permission COMMAND = Permission.createAbstractPermission("command");

    public static final Permission COMMAND_ENCHANT_UNSAFE = COMMAND.createAbstractChild("enchant").createChild("unsafe");

    public static final Permission COMMAND_LAG_RESET = COMMAND.createAbstractChild("lag").createChild("reset");

    /**
     * Allows to create items that are blacklisted
     */
    public static final Permission ITEM_BLACKLIST = Permission.createPermission("item-blacklist");

    private static final Permission COMMAND_ITEM = COMMAND.createAbstractChild("item");
    public static final Permission COMMAND_ITEM_ENCHANTMENTS = COMMAND_ITEM.createChild("enchantments");
    public static final Permission COMMAND_ITEM_ENCHANTMENTS_UNSAFE = COMMAND_ITEM.createChild("enchantments.unsafe");

    private static final Permission COMMAND_GAMEMODE = COMMAND.createAbstractChild("gamemode");
    /**
     * Allows to change the game-mode of other players too
     */
    public static final Permission COMMAND_GAMEMODE_OTHER = COMMAND_GAMEMODE.createChild("other");
    /**
     * Without this permission the players game-mode will be reset when leaving the server or changing the world
     */
    public static final Permission COMMAND_GAMEMODE_KEEP = COMMAND_GAMEMODE.createChild("keep");

    public static final Permission COMMAND_PTIME_OTHER = COMMAND.createChild("ptime.other");

    public static final Permission COMMAND_CLEARINVENTORY = COMMAND.createChild("clearinventory");
    /**
     * Allows clearing the inventory of other players
     */
    public static final Permission COMMAND_CLEARINVENTORY_OTHER = COMMAND_CLEARINVENTORY.createChild("notify");
    /**
     * Notifies you if your inventory got cleared by someone else
     */
    public static final Permission COMMAND_CLEARINVENTORY_NOTIFY = COMMAND_CLEARINVENTORY.createChild("other");
    /**
     * Prevents the other player being notified when his inventory got cleared
     */
    public static final Permission COMMAND_CLEARINVENTORY_QUIET = COMMAND_CLEARINVENTORY.createChild("quiet");
    /**
     * Prevents your inventory from being cleared unless forced
     */
    public static final Permission COMMAND_CLEARINVENTORY_PREVENT = COMMAND_CLEARINVENTORY.createNew("prevent",FALSE);
    /**
     * Clears an inventory even if the player has the prevent permission
     */
    public static final Permission COMMAND_CLEARINVENTORY_FORCE = COMMAND_CLEARINVENTORY.createChild("force");

    private static final Permission COMMAND_KILL = COMMAND.createAbstractChild("kill");
    /**
     * Prevents from being killed by the kill command unless forced
     */
    public static final Permission COMMAND_KILL_PREVENT = COMMAND_KILL.createNew("prevent",FALSE);
    /**
     * Kills a player even if the player has the prevent permission
     */
    public static final Permission COMMAND_KILL_FORCE = COMMAND_KILL.createChild("force");
    /**
     * Allows killing all players currently online
     */
    public static final Permission COMMAND_KILL_ALL = COMMAND_KILL.createChild("all");
    /**
     * Allows killing a player with a lightning strike
     */
    public static final Permission COMMAND_KILL_LIGHTNING = COMMAND_KILL.createChild("lightning");
    /**
     * Prevents the other player being notified who killed him
     */
    public static final Permission COMMAND_KILL_QUIET = COMMAND_KILL.createChild("quiet");
    /**
     * Shows who killed you
     */
    public static final Permission COMMAND_KILL_NOTIFY = COMMAND_KILL.createChild("notify");

    private static final Permission COMMAND_INVSEE = COMMAND.createAbstractChild("invsee");
    /**
     * Allows to modify the inventory of other players
     */
    public static final Permission COMMAND_INVSEE_MODIFY = COMMAND_INVSEE.createChild("modify");
    public static final Permission COMMAND_INVSEE_ENDERCHEST = COMMAND_INVSEE.createChild("ender");
    /**
     * Prevents an inventory from being modified unless forced
     */
    public static final Permission COMMAND_INVSEE_MODIFY_PREVENT = COMMAND_INVSEE.createNew("modify.prevent",FALSE);
    /**
     * Allows modifying an inventory even if the player has the prevent permission
     */
    public static final Permission COMMAND_INVSEE_MODIFY_FORCE = COMMAND_INVSEE.createChild("modify.force");
    /**
     * Notifies you when someone is looking into your inventory
     */
    public static final Permission COMMAND_INVSEE_NOTIFY = COMMAND_INVSEE.createChild("notify");
    /**
     * Prevents the other player from being notified when looking into his inventory
     */
    public static final Permission COMMAND_INVSEE_QUIET = COMMAND_INVSEE.createChild("quiet");

    private static final Permission COMMAND_SPAWN = COMMAND.createAbstractChild("spawn");
    /**
     * Allows to teleport all online players to the spawn of the main world
     */
    public static final Permission COMMAND_SPAWN_ALL = COMMAND_SPAWN.createChild("all");
    /**
     * Prevents from being teleported to spawn by someone else
     */
    public static final Permission COMMAND_SPAWN_PREVENT = COMMAND_SPAWN.createChild("prevent");
    /**
     * Allows teleporting a player to spawn even if the player has the prevent permission
     */
    public static final Permission COMMAND_SPAWN_FORCE = COMMAND_SPAWN.createChild("force");

    private static final Permission COMMAND_TP = COMMAND.createAbstractChild("tp");
    /**
     * Ignores all prevent permissions when using the /tp command
     */
    public static final Permission COMMAND_TP_FORCE = COMMAND_TP.createChild("force");
    /**
     * Allows teleporting another player
     */
    public static final Permission COMMAND_TP_OTHER = COMMAND_TP.createChild("other");

    private static final Permission TELEPORT = Permission.createAbstractPermission("teleport");
    private static final Permission TELEPORT_PREVENT = TELEPORT.createAbstract("prevent");
    /**
     * Prevents from being teleported by someone else
     */
    public static final Permission TELEPORT_PREVENT_TP = TELEPORT_PREVENT.createChild("tp",FALSE);
    /**
     * Prevents from teleporting to you
     */
    public static final Permission TELEPORT_PREVENT_TPTO = TELEPORT_PREVENT.createChild("tpto",FALSE);

    private static final Permission COMMAND_TPALL = COMMAND.createAbstractChild("tpall");
    /**
     * Ignores all prevent permissions when using the /tpall command
     */
    public static final Permission COMMAND_TPALL_FORCE = COMMAND_TPALL.createChild("force");

    private static final Permission COMMAND_TPHERE = COMMAND.createAbstractChild("tphere");
    /**
     * Ignores all prevent permissions when using the /tphere command
     */
    public static final Permission COMMAND_TPHERE_FORCE = COMMAND_TPHERE.createChild("force");

    private static final Permission COMMAND_TPHEREALL = COMMAND.createAbstractChild("tphereall");
    /**
     * Ignores all prevent permissions when using the /tphereall command
     */
    public static final Permission COMMAND_TPHEREALL_FORCE = COMMAND_TPHEREALL.createChild("force");

    /**
     * Allows using the back command
     */
    public static final Permission COMMAND_BACK = COMMAND.createChild("back");
    /**
     * Allows using the back command after dieing (if this is not set you won't be able to tp back to your deathpoint)
     */
    public static final Permission COMMAND_BACK_ONDEATH = COMMAND_BACK.createChild("ondeath");

    private static final Permission COMMAND_GOD = COMMAND.createAbstractChild("god");
    /**
     * Allows to enable god-mode for other players
     */
    public static final Permission COMMAND_GOD_OTHER = COMMAND_GOD.createChild("other");
    /**
     * Without this permission the player will loose god-mode leaving the server or changing the world
     */
    public static final Permission COMMAND_GOD_KEEP = COMMAND_GOD.createChild("keep");

    private static final Permission COMMAND_AFK = COMMAND.createAbstractChild("afk");
    private static final Permission COMMAND_AFK_PREVENT = COMMAND_AFK.createAbstract("prevent");
    /**
     * Prevents from being displayed as no longer afk automatically unless using chat
     */
    public static final Permission PREVENT_AUTOUNAFK = COMMAND_AFK_PREVENT.createChild("autounafk");
    /**
     * Prevents from being displayed as afk automatically
     */
    public static final Permission PREVENT_AUTOAFK = COMMAND_AFK_PREVENT.createChild("autoafk");

    /**
     * Allows to set or unset the afk status of other players
     */
    public static final Permission COMMAND_AFK_OTHER = COMMAND_AFK.createChild("other");

    public static final Permission COMMAND_IGNORE_PREVENT = COMMAND.createAbstractChild("ignore").createChild("prevent",FALSE);

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
    public static final Permission COMMAND_BUTCHER_FLAG_MONSTER = COMMAND_BUTCHER_FLAG.createChild("monster");
    public static final Permission COMMAND_BUTCHER_FLAG_BOSS = COMMAND_BUTCHER_FLAG.createChild("boss");

    private static final Permission COMMAND_FEED = COMMAND.createAbstractChild("feed");
    public static final Permission COMMAND_FEED_OTHER = COMMAND_FEED.createChild("other");

    private static final Permission COMMAND_STARVE = COMMAND.createAbstractChild("starve");
    public static final Permission COMMAND_STARVE_OTHER = COMMAND_STARVE.createChild("other");

    private static final Permission COMMAND_HEAL = COMMAND.createAbstractChild("heal");
    public static final Permission COMMAND_HEAL_OTHER = COMMAND_HEAL.createChild("other");

    private static final Permission COMMAND_FLY = COMMAND.createAbstractChild("fly");
    public static final Permission COMMAND_FLY_KEEP = COMMAND_FLY.createChild("keep");
    public static final Permission COMMAND_FLY_OTHER = COMMAND_FLY.createChild("other");

    private static final Permission COMPASS_JUMPTO = Permission.createAbstractPermission("compass.jumpto");
    public static final Permission COMPASS_JUMPTO_LEFT = COMPASS_JUMPTO.createChild("left");
    public static final Permission COMPASS_JUMPTO_RIGHT = COMPASS_JUMPTO.createChild("right");

    private static final Permission COMMAND_KICK = COMMAND.createAbstractChild("kick");
    public static final Permission COMMAND_KICK_ALL = COMMAND_KICK.createChild("all");
    public static final Permission COMMAND_KICK_NOREASON = COMMAND_KICK.createNew("noreason");

    public static final Permission COMMAND_STACK_FULLSTACK = COMMAND.createAbstractChild("stack").createChild("fullstack");

    public static final Permission COMMAND_BAN_NOREASON = COMMAND.createAbstractChild("ban").createChild("noreason");
    public static final Permission COMMAND_IPBAN_NOREASON = COMMAND.createAbstractChild("ipban").createChild("noreason",FALSE);
    public static final Permission COMMAND_TEMPBAN_NOREASON = COMMAND.createAbstractChild("tempban").createChild("noreason",FALSE);
    
    /**
     * Allows to change the walkspeed of other players
     */
    public static final Permission COMMAND_WALKSPEED_OTHER = COMMAND.createAbstractChild("walkspeed").createChild("other");

    /**
     * Allows writing colored signs
     */
    public static final Permission SIGN_COLORED = Permission.createPermission("sign.colored");
    public static final Permission SIGN_COLORED_BLACK = SIGN_COLORED.createNew("black");
    public static final Permission SIGN_COLORED_DARK_BLUE = SIGN_COLORED.createNew("dark-blue");
    public static final Permission SIGN_COLORED_DARK_GREEN = SIGN_COLORED.createNew("dark-green");
    public static final Permission SIGN_COLORED_DARK_AQUA = SIGN_COLORED.createNew("dark-aqua");
    public static final Permission SIGN_COLORED_DARK_RED = SIGN_COLORED.createNew("dark-red");
    public static final Permission SIGN_COLORED_DARK_PURPLE = SIGN_COLORED.createNew("dark-purple");
    public static final Permission SIGN_COLORED_GOLD = SIGN_COLORED.createNew("gold");
    public static final Permission SIGN_COLORED_GRAY = SIGN_COLORED.createNew("gray");
    public static final Permission SIGN_COLORED_DARK_GRAY = SIGN_COLORED.createNew("dark-gray");
    public static final Permission SIGN_COLORED_BLUE = SIGN_COLORED.createNew("blue");
    public static final Permission SIGN_COLORED_GREEN = SIGN_COLORED.createNew("green");
    public static final Permission SIGN_COLORED_AQUA = SIGN_COLORED.createNew("aqua");
    public static final Permission SIGN_COLORED_RED = SIGN_COLORED.createNew("red");
    public static final Permission SIGN_COLORED_LIGHT_PURPLE = SIGN_COLORED.createNew("light-purple");
    public static final Permission SIGN_COLORED_YELLOW = SIGN_COLORED.createNew("yellow");
    public static final Permission SIGN_COLORED_WHITE = SIGN_COLORED.createNew("white");

    public static final Permission SIGN_COLORED_OBFUSCATED = SIGN_COLORED.createNew("obfuscated");
    public static final Permission SIGN_COLORED_BOLD = SIGN_COLORED.createNew("bold");
    public static final Permission SIGN_COLORED_STRIKE = SIGN_COLORED.createNew("strike");
    public static final Permission SIGN_COLORED_UNDERLINE = SIGN_COLORED.createNew("underline");
    public static final Permission SIGN_COLORED_ITALIC = SIGN_COLORED.createNew("italic");
    public static final Permission SIGN_COLORED_RESET = SIGN_COLORED.createNew("reset");

    public static final Permission CHANGEPAINTING = Permission.createPermission("changepainting");
    public static final Permission KICK_RECEIVEMESSAGE = Permission.createPermission("kick.receivemessage");
    public static final Permission BAN_RECEIVEMESSAGE = Permission.createPermission("ban.receivemessage");

    public static Permission OVERSTACKED_ANVIL_AND_BREWING = Permission.createPermission("allow-overstacked-anvil-and-brewing");

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
