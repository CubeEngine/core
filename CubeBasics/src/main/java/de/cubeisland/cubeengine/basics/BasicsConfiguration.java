package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import java.util.LinkedList;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Codec("yml")
public class BasicsConfiguration extends Configuration
{
    @Option("commands.spawnmob-limit")
    public int spawnmobLimit = 20;
    @Option("commands.remove-defaultradius")
    public int removeCmdDefaultRadius = 20;
    @Comment("The world to teleport to when using /spawn"
    + "\nUse {} if you want to use the spawn of the world the player is in.")
    @Option("commands.spawn-mainworld")
    public String spawnMainWorld = "world";
    @Comment(
    "The seconds until a teleportrequest is automaticly denied."
    + "\nUse -1 to never automaticly deny. (Will loose information after some time when disconecting)")
    @Option("commands.teleport-request-wait")
    public int tpRequestWait = -1;
    @Comment(
    "This message will be displayed to everyone with the permission on joining!")
    @Option("commands.motd")
    public String motd = "Welcome on our server. Have fun!";
    @Option("commands.near-defaultradius")
    public int nearDefaultRadius = 20;
    @Option("afk.automatic-afk")
    public String autoAfk = "5m";
    @Option("afk.afk-check-delay")
    public String afkCheck = "1s";
    @Option("command.mute.default-mute-time")
    public int defaultMuteTime = -1;
    @Option(value = "commands.item-blacklist", valueType= ItemStack.class)
    public LinkedList<ItemStack> blacklist = new LinkedList<ItemStack>()
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
}
