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
package de.cubeisland.engine.core.sponge;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Locale;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.module.Module;

import org.apache.logging.log4j.LogManager;

import org.spongepowered.api.data.properties.BurningFuelProperty;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.entity.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.world.Location;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * This class contains various methods to access sponge-related stuff.
 */
public class BukkitUtils
{
    private static CommandLogFilter commandFilter = null;
    private static Field dragonTarget;

    private BukkitUtils()
    {}

    public static Locale getLocaleFromSender(CommandSource sender)
    {

        if (sender instanceof de.cubeisland.engine.core.command.CommandSender)
        {
            return ((de.cubeisland.engine.core.command.CommandSender)sender).getLocale();
        }
        Locale locale = null;
        if (sender instanceof Player)
        {
            locale = ((Player)sender).getLocale();
        }
        if (locale == null)
        {
            return Locale.getDefault();
        }
        return locale;
    }

    public static void disableCommandLogging()
    {
        org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger)LogManager.getLogger("Minecraft");
        if (commandFilter == null)
        {
            commandFilter = new CommandLogFilter(); // TODO configurable filter
        }
        logger.addFilter(commandFilter);
    }

    static void resetCommandLogging()
    {
        if (commandFilter != null)
        {
            org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger)LogManager.getLogger("Minecraft");
            logger.getContext().removeFilter(commandFilter);
        }
    }
    public static synchronized void cleanup()
    {
        dragonTarget = null;
        resetCommandLogging();
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
        if (item.getType() == ItemTypes.FISH)
        {
            return item.getDurability() == 3; // pufferfish
        }
        return getItem(item.getType()).j(null) != null; // Items that can be brewed return a String here else null
    }

    public static boolean isFuel(ItemStack item)
    {
        return item.getItem().getDefaultProperty(BurningFuelProperty.class).isPresent();
    }

    public static boolean isSmeltable(ItemStack item)
    {
        net.minecraft.server.v1_8_R2.ItemStack nmss = CraftItemStack.asNMSCopy(item);
        // TileEntityFurnace private canBurn() checks this first for null
        // If the result of that item being cooked is null, it is not cookable
        return RecipesFurnace.getInstance().getResult(nmss) != null;
    }

    static void setSignalHandlers(final SpongeCore core)
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
                        core.getTaskManager().runTask(core.getModuleManager().getCoreModule(), () -> {
                            core.getGame().getServer().shutdown(Texts.of()); // tODO default message?
                            lastReceived = -1;
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
                            core.getTaskManager().runTask(core.getModuleManager().getCoreModule(), () -> {
                                core.getServer().reload();
                                core.getLog().info("Done reloading the server!");
                                reloading = false;
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

    public static Player getOfflinePlayerAsPlayer(User player)
    {
        MinecraftServer minecraftServer = DedicatedServer.getServer();

        //Create and load the target EntityPlayer
        EntityPlayer entityPlayer = new EntityPlayer(DedicatedServer.getServer(), minecraftServer.getWorldServer(0), new GameProfile(player.getUniqueId(), player.getName()),
                             new PlayerInteractManager(minecraftServer.getWorldServer(0)));
        entityPlayer.getBukkitEntity().loadData();
        return entityPlayer.getBukkitEntity();
    }

    public static Living getTarget(Living hunter) {
        if (hunter == null) return null;
        EntityLiving entity = ((CraftLivingEntity) hunter).getHandle();
        if (entity == null) return null;
        try
        {
            if (entity instanceof EntityGhast)
            {
                return (LivingEntity)((EntityGhast)entity).getGoalTarget().getBukkitEntity();
            }
            if(entity instanceof EntityEnderDragon)
            {
                if (dragonTarget == null)
                {
                    dragonTarget = entity.getClass().getDeclaredField("by"); // used by EntityTargetEvent in EntityEnderDragon
                    dragonTarget.setAccessible(true);
                }
                return (LivingEntity)((Entity)dragonTarget.get(entity)).getBukkitEntity();
            }
            return null;
        }
        catch (Exception ex)
        {
            CubeEngine.getCore().getLog().warn(ex, "Could not get Target of Ghast or Enderdragon");
            return null;
        }
    }

    public static double getEntitySpeed(Living entity)
    {
        return (((CraftLivingEntity)entity).getHandle()).getAttributeInstance(GenericAttributes.d).getValue();
    }

    public static void setEntitySpeed(Living entity, double value)
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
                            passenger.mount(entity);
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
