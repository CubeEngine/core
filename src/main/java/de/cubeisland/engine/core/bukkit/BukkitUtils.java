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
package de.cubeisland.engine.core.bukkit;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Locale;

import net.minecraft.server.v1_7_R4.DedicatedServer;
import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.EntityLiving;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.GenericAttributes;
import net.minecraft.server.v1_7_R4.Item;
import net.minecraft.server.v1_7_R4.JsonList;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.minecraft.server.v1_7_R4.PlayerInteractManager;
import net.minecraft.server.v1_7_R4.RecipesFurnace;
import net.minecraft.server.v1_7_R4.TileEntityFurnace;
import net.minecraft.server.v1_7_R4.WhiteList;
import net.minecraft.server.v1_7_R4.WorldServer;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.i18n.I18nUtil;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.apache.logging.log4j.LogManager;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * This class contains various methods to access bukkit-related stuff.
 */
public class BukkitUtils
{
    private static Field entityPlayerLocaleField;
    private static CommandLogFilter commandFilter = null;
    private static Field dragonTarget;
    private static Field ghastTarget;

    private BukkitUtils()
    {}

    static boolean init(BukkitCore core)
    {
        try
        {
            entityPlayerLocaleField = EntityPlayer.class.getDeclaredField("locale");
            entityPlayerLocaleField.setAccessible(true);
        }
        catch (Exception ex)
        {
            core.getLog().error(ex, "Failed to initialize the required hacks!");
            return false;
        }
        return true;
    }

    public static boolean isCompatible(BukkitCore core)
    {
        String serverClassName = core.getServer().getClass().getName();
        return (serverClassName.startsWith("org.bukkit.craftbukkit.") && serverClassName.endsWith(".CraftServer"));
    }

    public static Locale getLocaleFromSender(CommandSender sender)
    {
        if (sender instanceof de.cubeisland.engine.core.command.CommandSender)
        {
            return ((de.cubeisland.engine.core.command.CommandSender)sender).getLocale();
        }
        Locale locale = null;
        if (sender instanceof Player)
        {
            locale = getLocaleFromUser((Player)sender);
        }
        if (locale == null)
        {
            return Locale.getDefault();
        }
        return locale;
    }

    /**
     * Returns the locale string of a player.
     *
     * @param player the Player instance
     * @return the locale string of the player
     */
    private static Locale getLocaleFromUser(Player player)
    {
        if (player.getClass() == CraftPlayer.class)
        {
            try
            {
                final String localeString = (String)entityPlayerLocaleField.get(((CraftPlayer)player).getHandle());
                return I18nUtil.stringToLocale(localeString);
            }
            catch (Exception ignored)
            {}
        }
        return null;
    }

    public static CommandMap getCommandMap(final Server server)
    {
        return ((CraftServer)server).getCommandMap();
    }

    public static void disableCommandLogging()
    {
        org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger)LogManager.getLogger("Minecraft");
        if (commandFilter == null)
        {
            commandFilter = new CommandLogFilter(); //  TODO configurable filter
        }
        logger.addFilter(commandFilter);
    }

    static void resetCommandLogging()
    {
        if (commandFilter != null)
        {
            org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger)LogManager.getLogger("Minecraft");
            logger.getContext().removeFilter(commandFilter); // TODO test if this is working?
        }
    }

    public static int getPing(Player onlinePlayer)
    {
        return ((CraftPlayer)onlinePlayer).getHandle().ping;
    }

    public static boolean isInvulnerable(Player player)
    {
        if (player != null)
        {
            if (player instanceof User)
            {
                player = ((User)player).getOfflinePlayer().getPlayer();
            }
            if (player != null && player instanceof CraftPlayer)
            {
                return ((CraftPlayer)player).getHandle().abilities.isInvulnerable;
            }
        }
        return false;
    }

    public static void setInvulnerable(Player player, boolean state)
    {
        if (player != null && player instanceof User)
        {
            player = ((User)player).getOfflinePlayer().getPlayer();
        }
        if (player != null && player instanceof CraftPlayer)
        {
            ((CraftPlayer)player).getHandle().abilities.isInvulnerable = state;
            ((CraftPlayer)player).getHandle().updateAbilities();
        }
    }

    public static synchronized void cleanup()
    {
        dragonTarget = null;
        ghastTarget = null;
        resetCommandLogging();
    }

    public static void setOnlineMode(boolean mode)
    {
        getCraftServer().getServer().setOnlineMode(mode);
        saveServerProperties();
    }

    private static CraftServer getCraftServer()
    {
        return ((CraftServer)Bukkit.getServer());
    }

    public static void saveServerProperties()
    {
        getCraftServer().getServer().getPropertyManager().savePropertiesFile();
    }

    public static void wipeWhitelist()
    {
        WhiteList whitelist = getCraftServer().getHandle().getWhitelist();
        new ClearJsonList(whitelist);
    }

    private static Item getItem(Material m)
    {
        return (Item)Item.REGISTRY.a(m.getId());
    }

    /**
     * Returns true if given material is allowed to be placed in the top brewingstand slot
     *
     * @param item
     * @return
     */
    public static boolean canBePlacedInBrewingstand(ItemStack item)
    {
        // TODO better check
        if (item.getType() == Material.RAW_FISH)
        {
            return item.getDurability() == 3; // pufferfish
        }
        return getItem(item.getType()).i(null) != null; // Items that can be brewed return a String here else null
    }

    public static boolean isFuel(ItemStack item)
    {
        // Create an NMS item stack
        net.minecraft.server.v1_7_R4.ItemStack nmss = CraftItemStack.asNMSCopy(item);
        // Use the NMS TileEntityFurnace to check if the item being clicked is a fuel
        return TileEntityFurnace.isFuel(nmss);
    }

    public static boolean isSmeltable(ItemStack item)
    {
        net.minecraft.server.v1_7_R4.ItemStack nmss = CraftItemStack.asNMSCopy(item);
        // TileEntityFurnace private canBurn() checks this first for null
        // If the result of that item being cooked is null, it is not cookable
        return RecipesFurnace.getInstance().getResult(nmss) != null;
    }

    static void setSignalHandlers(final BukkitCore core)
    {
        try
        {
            Class.forName("sun.misc.Signal");

            Signal.handle(new Signal("INT"), new SignalHandler()
            {
                private long lastReceived = 0;

                @Override
                public void handle(Signal signal)
                {
                    if (this.lastReceived == -1)
                    {
                        return;
                    }
                    final long time = System.currentTimeMillis();
                    if (time - this.lastReceived <= 5000)
                    {
                        core.getLog().info("Shutting down the server now!");
                        core.getTaskManager().runTask(core.getModuleManager().getCoreModule(), new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                core.getServer().shutdown();
                                lastReceived = -1;
                            }
                        });
                    }
                    else
                    {
                        this.lastReceived = time;
                        core.getLog().info("You can't copy content from the console using CTRL-C!");
                        core.getLog().info("If you really want shutdown the server use the stop command or press CTRL-C again within 5 seconds!");
                    }
                }
            });

            try
            {
                Signal.handle(new Signal("HUP"), new SignalHandler() {
                    private volatile boolean reloading = false;

                    @Override
                    public void handle(Signal signal)
                    {
                        if (!this.reloading)
                        {
                            this.reloading = true;
                            core.getLog().info("Reloading the server!");
                            core.getTaskManager().runTask(core.getModuleManager().getCoreModule(), new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    core.getServer().reload();
                                    core.getLog().info("Done reloading the server!");
                                    reloading = false;
                                }
                            });
                        }
                    }
                });
            }
            catch (IllegalArgumentException e)
            {
                core.getLog().info("You're OS does not support the HUP signal! This can be ignored.");
            }
        }
        catch (ClassNotFoundException ignored)
        {}
    }

    public static Player getOfflinePlayerAsPlayer(OfflinePlayer player)
    {
        MinecraftServer minecraftServer = DedicatedServer.getServer();

        //Create and load the target EntityPlayer
        EntityPlayer entityPlayer = new EntityPlayer(DedicatedServer.getServer(), minecraftServer.getWorldServer(0), new GameProfile(player.getUniqueId(), player.getName()),
                             new PlayerInteractManager(minecraftServer.getWorldServer(0)));
        entityPlayer.getBukkitEntity().loadData();
        return entityPlayer.getBukkitEntity();
    }

    public static LivingEntity getTarget(LivingEntity hunter) {
        if (hunter == null) return null;
        EntityLiving entity = ((CraftLivingEntity) hunter).getHandle();
        if (entity == null) return null;
        EntityLiving target;
        try
        {
            if(hunter instanceof EnderDragon)
            {
                if (dragonTarget == null)
                {
                    dragonTarget = entity.getClass().getDeclaredField("bD");
                    dragonTarget.setAccessible(true);
                }
                target = (EntityLiving)dragonTarget.get(entity);
            }
            else if (hunter instanceof Ghast)
            {
                if (ghastTarget == null)
                {
                    ghastTarget = entity.getClass().getDeclaredField("target");
                    ghastTarget.setAccessible(true);
                }
                target = (EntityLiving)ghastTarget.get(entity);
            }
            else
            {
                return null;
            }
            if(target == null)
            {
                return null;
            }
            return (LivingEntity) target.getBukkitEntity();
        }
        catch (Exception ex)
        {
            CubeEngine.getCore().getLog().warn(ex, "Could not get Target of Ghast or Enderdragon");
            return null;
        }
    }

    public static double getEntitySpeed(LivingEntity entity)
    {
        return (((CraftLivingEntity)entity).getHandle()).getAttributeInstance(GenericAttributes.d).getValue();
    }

    public static void setEntitySpeed(LivingEntity entity, double value)
    {
        (((CraftLivingEntity)entity).getHandle()).getAttributeInstance(GenericAttributes.d).setValue(value);
    }

    static boolean isAnsiSupported(Server server)
    {
        return ((CraftServer)server).getReader().getTerminal().isAnsiSupported();
    }

    /**
     * Teleport player & entities crossworld (including passengers)
     *
     * Thanks to bergerkiller (https://forums.bukkit.org/threads/teleport-entity-including-passenger.55903/)
     */
    public static boolean teleport(Module module, final Entity entity, final Location to)
    {
        WorldServer newworld = ((CraftWorld)to.getWorld()).getHandle();
        // Pre-load Chunks to tp to
        Chunk chunk = to.getChunk();
        chunk.getWorld().getChunkAt(chunk.getX(), chunk.getZ());
        for (int cx = chunk.getX() - 3; cx <= chunk.getX() + 3; cx++)
        {
            for (int cz = chunk.getZ() - 3; cz <= chunk.getZ() + 3; cz++)
            {
                to.getWorld().getChunkAt(cx, cz);
            }
        }

        if (entity.world != newworld && !(entity instanceof EntityPlayer))
        {
            if (entity.passenger != null)
            {
                final Entity passenger = entity.passenger;
                passenger.vehicle = null;
                entity.passenger = null;
                if (teleport(module, passenger, to))
                {
                    module.getCore().getTaskManager().runTaskDelayed(module, new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            passenger.setPassengerOf(entity);
                        }
                    }, 0);
                }
            }

            //teleport this entity
            //entity.world.getWorld().getHandle().tracker.untrackEntity(entity);
            entity.world.removeEntity(entity);
            entity.dead = false;
            entity.world = newworld;
            entity.setLocation(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
            entity.world.addEntity(entity, SpawnReason.CUSTOM);
            //entity.world.getWorld().getHandle().tracker.track(entity);
            return true;
        }
        else
        {
            return entity.getBukkitEntity().teleport(to);
        }
    }

    /**
     * Clears given JsonList
     */
    public static class ClearJsonList extends JsonList
    {
        private ClearJsonList(JsonList toClear)
        {
            super(toClear.c());
            try
            {
                this.save();
                toClear.load();
            }
            catch (IOException e)
            {
                throw new IllegalStateException(e);
            }
        }
    }
}
