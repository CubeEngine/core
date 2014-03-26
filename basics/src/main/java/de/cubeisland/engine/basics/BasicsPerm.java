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

import de.cubeisland.engine.basics.command.teleport.TpWorldPermissions;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;
import de.cubeisland.engine.core.util.Profiler;

import static de.cubeisland.engine.core.permission.PermDefault.FALSE;

@SuppressWarnings("all")
public class BasicsPerm extends PermissionContainer<Basics>
{
    public TpWorldPermissions tpWorld()
    {
        return tpWorld;
    }

    private final TpWorldPermissions tpWorld;

    public BasicsPerm(Basics module)
    {
        super(module);
        module.getLog().trace("{} ms - Basics.Permission-register", Profiler.getCurrentDelta("basicsEnable", TimeUnit.MILLISECONDS));
        SIGN_COLORED.attach(SIGN_COLORED_BLACK, SIGN_COLORED_DARK_BLUE, SIGN_COLORED_DARK_GREEN,
                            SIGN_COLORED_DARK_AQUA, SIGN_COLORED_DARK_RED, SIGN_COLORED_DARK_PURPLE,
                            SIGN_COLORED_GOLD, SIGN_COLORED_GRAY, SIGN_COLORED_DARK_GRAY,
                            SIGN_COLORED_BLUE, SIGN_COLORED_GREEN, SIGN_COLORED_AQUA,
                            SIGN_COLORED_RED, SIGN_COLORED_LIGHT_PURPLE, SIGN_COLORED_YELLOW,
                            SIGN_COLORED_WHITE, SIGN_COLORED_OBFUSCATED, SIGN_COLORED_BOLD,
                            SIGN_COLORED_STRIKE, SIGN_COLORED_UNDERLINE, SIGN_COLORED_ITALIC, SIGN_COLORED_RESET);
        this.registerAllPermissions();
        tpWorld = new TpWorldPermissions(module, this); // per world permissions
    }

    public final Permission COMMAND = getBasePerm().childWildcard("command");

    public final Permission COMMAND_ENCHANT_UNSAFE = COMMAND.childWildcard("enchant").child("unsafe");

    public final Permission COMMAND_LAG_RESET = COMMAND.childWildcard("lag").child("reset");

    /**
     * Allows to create items that are blacklisted
     */
    public final Permission ITEM_BLACKLIST = getBasePerm().child("item-blacklist");

    private final Permission COMMAND_ITEM = COMMAND.childWildcard("item");
    public final Permission COMMAND_ITEM_ENCHANTMENTS = COMMAND_ITEM.child("enchantments");
    public final Permission COMMAND_ITEM_ENCHANTMENTS_UNSAFE = COMMAND_ITEM.child("enchantments.unsafe");

    private final Permission COMMAND_GAMEMODE = COMMAND.childWildcard("gamemode");
    /**
     * Allows to change the game-mode of other players too
     */
    public final Permission COMMAND_GAMEMODE_OTHER = COMMAND_GAMEMODE.child("other");
    /**
     * Without this permission the players game-mode will be reset when leaving the server or changing the world
     */
    public final Permission COMMAND_GAMEMODE_KEEP = COMMAND_GAMEMODE.child("keep");

    public final Permission COMMAND_PTIME_OTHER = COMMAND.child("ptime.other");

    private final Permission COMMAND_CLEARINVENTORY = COMMAND.childWildcard("clearinventory");
    /**
     * Allows clearing the inventory of other players
     */
    public final Permission COMMAND_CLEARINVENTORY_OTHER = COMMAND_CLEARINVENTORY.child("notify");
    /**
     * Notifies you if your inventory got cleared by someone else
     */
    public final Permission COMMAND_CLEARINVENTORY_NOTIFY = COMMAND_CLEARINVENTORY.child("other");
    /**
     * Prevents the other player being notified when his inventory got cleared
     */
    public final Permission COMMAND_CLEARINVENTORY_QUIET = COMMAND_CLEARINVENTORY.child("quiet");
    /**
     * Prevents your inventory from being cleared unless forced
     */
    public final Permission COMMAND_CLEARINVENTORY_PREVENT = COMMAND_CLEARINVENTORY.newPerm("prevent", FALSE);
    /**
     * Clears an inventory even if the player has the prevent permission
     */
    public final Permission COMMAND_CLEARINVENTORY_FORCE = COMMAND_CLEARINVENTORY.child("force");

    private final Permission COMMAND_KILL = COMMAND.childWildcard("kill");
    /**
     * Prevents from being killed by the kill command unless forced
     */
    public final Permission COMMAND_KILL_PREVENT = COMMAND_KILL.newPerm("prevent", FALSE);
    /**
     * Kills a player even if the player has the prevent permission
     */
    public final Permission COMMAND_KILL_FORCE = COMMAND_KILL.child("force");
    /**
     * Allows killing all players currently online
     */
    public final Permission COMMAND_KILL_ALL = COMMAND_KILL.child("all");
    /**
     * Allows killing a player with a lightning strike
     */
    public final Permission COMMAND_KILL_LIGHTNING = COMMAND_KILL.child("lightning");
    /**
     * Prevents the other player being notified who killed him
     */
    public final Permission COMMAND_KILL_QUIET = COMMAND_KILL.child("quiet");
    /**
     * Shows who killed you
     */
    public final Permission COMMAND_KILL_NOTIFY = COMMAND_KILL.child("notify");

    private final Permission COMMAND_INVSEE = COMMAND.childWildcard("invsee");
    /**
     * Allows to modify the inventory of other players
     */
    public final Permission COMMAND_INVSEE_MODIFY = COMMAND_INVSEE.child("modify");
    public final Permission COMMAND_INVSEE_ENDERCHEST = COMMAND_INVSEE.child("ender");
    /**
     * Prevents an inventory from being modified unless forced
     */
    public final Permission COMMAND_INVSEE_MODIFY_PREVENT = COMMAND_INVSEE.newPerm("modify.prevent", FALSE);
    /**
     * Allows modifying an inventory even if the player has the prevent permission
     */
    public final Permission COMMAND_INVSEE_MODIFY_FORCE = COMMAND_INVSEE.child("modify.force");
    /**
     * Notifies you when someone is looking into your inventory
     */
    public final Permission COMMAND_INVSEE_NOTIFY = COMMAND_INVSEE.child("notify");
    /**
     * Prevents the other player from being notified when looking into his inventory
     */
    public final Permission COMMAND_INVSEE_QUIET = COMMAND_INVSEE.child("quiet");

    private final Permission COMMAND_SPAWN = COMMAND.childWildcard("spawn");
    /**
     * Allows to teleport all online players to the spawn of the main world
     */
    public final Permission COMMAND_SPAWN_ALL = COMMAND_SPAWN.child("all");
    /**
     * Prevents from being teleported to spawn by someone else
     */
    public final Permission COMMAND_SPAWN_PREVENT = COMMAND_SPAWN.child("prevent");
    /**
     * Allows teleporting a player to spawn even if the player has the prevent permission
     */
    public final Permission COMMAND_SPAWN_FORCE = COMMAND_SPAWN.child("force");

    private final Permission COMMAND_TP = COMMAND.childWildcard("tp");
    /**
     * Ignores all prevent permissions when using the /tp command
     */
    public final Permission COMMAND_TP_FORCE = COMMAND_TP.child("force");
    /**
     * Allows teleporting another player
     */
    public final Permission COMMAND_TP_OTHER = COMMAND_TP.child("other");

    public final Permission COMMAND_TPPOS_SAFE = COMMAND.child("tppos").child("safe");

    private final Permission TELEPORT = getBasePerm().childWildcard("teleport");
    private final Permission TELEPORT_PREVENT = TELEPORT.newWildcard("prevent");
    /**
     * Prevents from being teleported by someone else
     */
    public final Permission TELEPORT_PREVENT_TP = TELEPORT_PREVENT.child("tp",FALSE);
    /**
     * Prevents from teleporting to you
     */
    public final Permission TELEPORT_PREVENT_TPTO = TELEPORT_PREVENT.child("tpto",FALSE);

    private final Permission COMMAND_TPALL = COMMAND.childWildcard("tpall");
    /**
     * Ignores all prevent permissions when using the /tpall command
     */
    public final Permission COMMAND_TPALL_FORCE = COMMAND_TPALL.child("force");

    private final Permission COMMAND_TPHERE = COMMAND.childWildcard("tphere");
    /**
     * Ignores all prevent permissions when using the /tphere command
     */
    public final Permission COMMAND_TPHERE_FORCE = COMMAND_TPHERE.child("force");

    private final Permission COMMAND_TPHEREALL = COMMAND.childWildcard("tphereall");
    /**
     * Ignores all prevent permissions when using the /tphereall command
     */
    public final Permission COMMAND_TPHEREALL_FORCE = COMMAND_TPHEREALL.child("force");

    private final Permission COMMAND_BACK = COMMAND.childWildcard("back");

    /**
     * Allows using the back command
     */
    public final Permission COMMAND_BACK_USE = COMMAND_BACK.child("use");
    /**
     * Allows using the back command after dieing (if this is not set you won't be able to tp back to your deathpoint)
     */
    public final Permission COMMAND_BACK_ONDEATH = COMMAND_BACK.child("ondeath");


    private final Permission COMMAND_GOD = COMMAND.childWildcard("god");
    /**
     * Allows to enable god-mode for other players
     */
    public final Permission COMMAND_GOD_OTHER = COMMAND_GOD.child("other");
    /**
     * Without this permission the player will loose god-mode leaving the server or changing the world
     */
    public final Permission COMMAND_GOD_KEEP = COMMAND_GOD.child("keep");

    private final Permission COMMAND_AFK = COMMAND.childWildcard("afk");
    private final Permission COMMAND_AFK_PREVENT = COMMAND_AFK.newWildcard("prevent");
    /**
     * Prevents from being displayed as no longer afk automatically unless using chat
     */
    public final Permission PREVENT_AUTOUNAFK = COMMAND_AFK_PREVENT.child("autounafk", FALSE);
    /**
     * Prevents from being displayed as afk automatically
     */
    public final Permission PREVENT_AUTOAFK = COMMAND_AFK_PREVENT.child("autoafk", FALSE);

    /**
     * Allows to set or unset the afk status of other players
     */
    public final Permission COMMAND_AFK_OTHER = COMMAND_AFK.child("other");

    public final Permission COMMAND_IGNORE_PREVENT = COMMAND.childWildcard("ignore").child("prevent",FALSE);

    private final Permission COMMAND_BUTCHER = COMMAND.childWildcard("butcher");
    private final Permission COMMAND_BUTCHER_FLAG = COMMAND_BUTCHER.childWildcard("flag");
    public final Permission COMMAND_BUTCHER_FLAG_PET = COMMAND_BUTCHER_FLAG.child("pet");
    public final Permission COMMAND_BUTCHER_FLAG_ANIMAL = COMMAND_BUTCHER_FLAG.child("animal");
    public final Permission COMMAND_BUTCHER_FLAG_LIGHTNING = COMMAND_BUTCHER_FLAG.child("lightning");
    public final Permission COMMAND_BUTCHER_FLAG_GOLEM = COMMAND_BUTCHER_FLAG.child("golem");
    public final Permission COMMAND_BUTCHER_FLAG_ALLTYPE = COMMAND_BUTCHER_FLAG.child("alltype");
    public final Permission COMMAND_BUTCHER_FLAG_ALL = COMMAND_BUTCHER_FLAG.child("all");
    public final Permission COMMAND_BUTCHER_FLAG_OTHER = COMMAND_BUTCHER_FLAG.child("other");
    public final Permission COMMAND_BUTCHER_FLAG_NPC = COMMAND_BUTCHER_FLAG.child("npc");
    public final Permission COMMAND_BUTCHER_FLAG_MONSTER = COMMAND_BUTCHER_FLAG.child("monster");
    public final Permission COMMAND_BUTCHER_FLAG_BOSS = COMMAND_BUTCHER_FLAG.child("boss");

    private final Permission COMMAND_FEED = COMMAND.childWildcard("feed");
    public final Permission COMMAND_FEED_OTHER = COMMAND_FEED.child("other");

    private final Permission COMMAND_STARVE = COMMAND.childWildcard("starve");
    public final Permission COMMAND_STARVE_OTHER = COMMAND_STARVE.child("other");

    private final Permission COMMAND_HEAL = COMMAND.childWildcard("heal");
    public final Permission COMMAND_HEAL_OTHER = COMMAND_HEAL.child("other");

    private final Permission COMMAND_FLY = COMMAND.childWildcard("fly");
    public final Permission COMMAND_FLY_KEEP = COMMAND_FLY.child("keep");
    public final Permission COMMAND_FLY_OTHER = COMMAND_FLY.child("other");

    private final Permission COMPASS_JUMPTO = getBasePerm().childWildcard("compass").childWildcard("jumpto");
    public final Permission COMPASS_JUMPTO_LEFT = COMPASS_JUMPTO.child("left");
    public final Permission COMPASS_JUMPTO_RIGHT = COMPASS_JUMPTO.child("right");

    private final Permission COMMAND_KICK = COMMAND.childWildcard("kick");
    public final Permission COMMAND_KICK_ALL = COMMAND_KICK.child("all");
    public final Permission COMMAND_KICK_NOREASON = COMMAND_KICK.newPerm("noreason");

    public final Permission COMMAND_STACK_FULLSTACK = COMMAND.childWildcard("stack").child("fullstack");

    public final Permission COMMAND_BAN_NOREASON = COMMAND.childWildcard("ban").child("noreason");
    public final Permission COMMAND_IPBAN_NOREASON = COMMAND.childWildcard("ipban").child("noreason",FALSE);
    public final Permission COMMAND_TEMPBAN_NOREASON = COMMAND.childWildcard("tempban").child("noreason",FALSE);
    
    /**
     * Allows to change the walkspeed of other players
     */
    public final Permission COMMAND_WALKSPEED_OTHER = COMMAND.childWildcard("walkspeed").child("other");

    /**
     * Allows writing colored signs
     */
    public final Permission SIGN_COLORED = getBasePerm().childWildcard("sign").child("colored");
    public final Permission SIGN_COLORED_BLACK = SIGN_COLORED.newPerm("black");
    public final Permission SIGN_COLORED_DARK_BLUE = SIGN_COLORED.newPerm("dark-blue");
    public final Permission SIGN_COLORED_DARK_GREEN = SIGN_COLORED.newPerm("dark-green");
    public final Permission SIGN_COLORED_DARK_AQUA = SIGN_COLORED.newPerm("dark-aqua");
    public final Permission SIGN_COLORED_DARK_RED = SIGN_COLORED.newPerm("dark-red");
    public final Permission SIGN_COLORED_DARK_PURPLE = SIGN_COLORED.newPerm("dark-purple");
    public final Permission SIGN_COLORED_GOLD = SIGN_COLORED.newPerm("gold");
    public final Permission SIGN_COLORED_GRAY = SIGN_COLORED.newPerm("gray");
    public final Permission SIGN_COLORED_DARK_GRAY = SIGN_COLORED.newPerm("dark-gray");
    public final Permission SIGN_COLORED_BLUE = SIGN_COLORED.newPerm("blue");
    public final Permission SIGN_COLORED_GREEN = SIGN_COLORED.newPerm("green");
    public final Permission SIGN_COLORED_AQUA = SIGN_COLORED.newPerm("aqua");
    public final Permission SIGN_COLORED_RED = SIGN_COLORED.newPerm("red");
    public final Permission SIGN_COLORED_LIGHT_PURPLE = SIGN_COLORED.newPerm("light-purple");
    public final Permission SIGN_COLORED_YELLOW = SIGN_COLORED.newPerm("yellow");
    public final Permission SIGN_COLORED_WHITE = SIGN_COLORED.newPerm("white");

    public final Permission SIGN_COLORED_OBFUSCATED = SIGN_COLORED.newPerm("obfuscated");
    public final Permission SIGN_COLORED_BOLD = SIGN_COLORED.newPerm("bold");
    public final Permission SIGN_COLORED_STRIKE = SIGN_COLORED.newPerm("strike");
    public final Permission SIGN_COLORED_UNDERLINE = SIGN_COLORED.newPerm("underline");
    public final Permission SIGN_COLORED_ITALIC = SIGN_COLORED.newPerm("italic");
    public final Permission SIGN_COLORED_RESET = SIGN_COLORED.newPerm("reset");

    public final Permission CHANGEPAINTING = getBasePerm().child("changepainting");
    public final Permission KICK_RECEIVEMESSAGE = getBasePerm().childWildcard("kick").child("receivemessage");
    public final Permission BAN_RECEIVEMESSAGE = getBasePerm().childWildcard("ban").child("receivemessage");

    public final Permission OVERSTACKED_ANVIL_AND_BREWING = getBasePerm().child("allow-overstacked-anvil-and-brewing");
}
