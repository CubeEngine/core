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

import java.util.Collection;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.configuration.YamlConfiguration;
import de.cubeisland.engine.configuration.annotations.Comment;
import de.cubeisland.engine.configuration.annotations.Name;
import de.cubeisland.engine.core.util.time.Duration;

public class BasicsConfiguration extends YamlConfiguration
{
    @Name("commands.spawnmob-limit")
    public int spawnmobLimit = 20;
    @Name("commands.remove-defaultradius")
    public int removeCmdDefaultRadius = 20;
    @Name("commands.butcher-defaultradius")
    public int butcherCmdDefaultRadius = 20;
    @Comment("The world to teleport to when using /spawn"
        + "\nUse {} if you want to use the spawn of the world the player is in.")
    @Name("mainworld")
    public World mainWorld = Bukkit.getServer().getWorld("world");
    @Comment("The seconds until a teleportrequest is automaticly denied."
        + "\nUse -1 to never automaticly deny. (Will loose information after some time when disconecting)")
    @Name("commands.teleport-request-wait")
    public int tpRequestWait = -1;
    @Name("commands.near-defaultradius")
    public int nearDefaultRadius = 20;
    @Name("afk.automatic-afk")
    public String autoAfk = "5m";
    @Name("afk.afk-check-delay")
    public String afkCheck = "1s";
    @Name("command.mute.default-mute-time")
    public Duration defaultMuteTime = new Duration(-1);
    @Name("commands.item-blacklist")
    public Collection<ItemStack> blacklist = new LinkedList<ItemStack>()
    {

        {
            this.add(new ItemStack(Material.BEDROCK));
            this.add(new ItemStack(Material.WATER));
            this.add(new ItemStack(Material.STATIONARY_WATER));
            this.add(new ItemStack(Material.LAVA));
            this.add(new ItemStack(Material.STATIONARY_LAVA));
            this.add(new ItemStack(Material.BED_BLOCK));
            this.add(new ItemStack(Material.PISTON_EXTENSION));
            this.add(new ItemStack(Material.PISTON_MOVING_PIECE));
            this.add(new ItemStack(Material.REDSTONE_WIRE));
            this.add(new ItemStack(Material.CROPS));
            this.add(new ItemStack(Material.SIGN_POST));
            this.add(new ItemStack(Material.WOODEN_DOOR));
            this.add(new ItemStack(Material.WALL_SIGN));
            this.add(new ItemStack(Material.IRON_DOOR_BLOCK));
            this.add(new ItemStack(Material.REDSTONE_TORCH_OFF));
            this.add(new ItemStack(Material.PORTAL));
            this.add(new ItemStack(Material.CAKE_BLOCK));
            this.add(new ItemStack(Material.DIODE_BLOCK_OFF));
            this.add(new ItemStack(Material.DIODE_BLOCK_ON));
            this.add(new ItemStack(Material.LOCKED_CHEST));
            this.add(new ItemStack(Material.PUMPKIN_STEM));
            this.add(new ItemStack(Material.MELON_STEM));
            this.add(new ItemStack(Material.NETHER_WARTS));
            this.add(new ItemStack(Material.BREWING_STAND));
            this.add(new ItemStack(Material.CAULDRON));
            this.add(new ItemStack(Material.ENDER_PORTAL));
            this.add(new ItemStack(Material.REDSTONE_LAMP_ON));
            this.add(new ItemStack(Material.COCOA));
            this.add(new ItemStack(Material.TRIPWIRE));
            this.add(new ItemStack(Material.COMMAND));
            this.add(new ItemStack(Material.FLOWER_POT));
            this.add(new ItemStack(Material.CARROT));
            this.add(new ItemStack(Material.POTATO));
            this.add(new ItemStack(Material.SKULL));
        }
    };

    @Name("navigation.thru.max-range")
    public int jumpThruMaxRange = 15;
    @Name("navigation.thru.max-wall-thickness")
    public int jumpThruMaxWallThickness = 15;
    @Name("navigation.jumpto.max-range")
    public int jumpToMaxRange = 300;
    @Name("commands.ban.disallow-if-offline-mode")
    public boolean disallowBanIfOfflineMode;
    @Name("changepainting.max.distance")
    public int maxChangePaintingDistance = 10;
    
    @Name("commands.door.max.radius")
    public int maxDoorRadius = 10;

    @Name("overstacked.prevent-anvil-and-brewing")
    public boolean preventOverstackedItems = true;
}
