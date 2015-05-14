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
package de.cubeisland.engine.module.core.sponge;

import de.cubeisland.engine.module.service.task.TaskManager;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.api.data.properties.BurningFuelProperty;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Texts;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * This class contains various methods to access sponge-related stuff.
 */
public class BukkitUtils
{
    private static CommandLogFilter commandFilter = null;

    private BukkitUtils()
    {}

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
        resetCommandLogging();
    }


    /**
     * Returns true if given material is allowed to be placed in the top brewingstand slot
     *
     * @param item
     * @return
     */
    public static boolean canBePlacedInBrewingstand(ItemStack item)
    {
        return false; // TODO isBrewable
    }

    public static boolean isFuel(ItemStack item)
    {
        return item.getItem().getDefaultProperty(BurningFuelProperty.class).isPresent();
    }

    public static boolean isSmeltable(ItemStack item)
    {
        return false; // TODO isSmeltable?
    }

    static void setSignalHandlers(final CoreModule core)
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
                        core.getModularity().start(TaskManager.class).runTask(core, () -> {
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

        }
        catch (ClassNotFoundException ignored)
        {}
    }

    /**
     * Teleport player & entities crossworld (including passengers)
     *
     * Thanks to bergerkiller (https://forums.bukkit.org/threads/teleport-entity-including-passenger.55903/)
     */
    /*
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
                    module.getCore().getTaskManager().runTaskDelayed(module, () -> passenger.mount(entity), 0);
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
    */
}
