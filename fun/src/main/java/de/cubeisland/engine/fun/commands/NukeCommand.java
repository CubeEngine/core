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
package de.cubeisland.engine.fun.commands;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;
import de.cubeisland.engine.core.task.TaskManager;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.math.Vector3;
import de.cubeisland.engine.core.util.math.shape.Cuboid;
import de.cubeisland.engine.core.util.math.shape.Cylinder;
import de.cubeisland.engine.core.util.math.shape.Shape;
import de.cubeisland.engine.core.util.math.shape.Sphere;
import de.cubeisland.engine.fun.Fun;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

public class NukeCommand
{
    private final Fun module;
    private final NukeListener nukeListener;

    public NukeCommand(Fun module)
    {
        this.module = module;
        this.nukeListener = new NukeListener();

        module.getCore().getEventManager().registerListener(module, this.nukeListener);
    }

    @Command(
        desc = "Makes a carpet of TNT fall on a player or where you're looking",
        indexed = {
            @Grouped(req = false, value = @Indexed(label = "param1")),
            @Grouped(req = false, value = @Indexed(label = "param2")),
            @Grouped(req = false, value = @Indexed(label = "param3")),
        },
        params =
            {
                @Param(names = {"player", "p"}, type = User.class),
                @Param(names = {"height", "h"}, type = Integer.class),
                @Param(names = {"range", "r"}, type = Integer.class),
                @Param(names = {"shape", "s"}, type = String.class)
            },
        flags =
            {
                @Flag(longName = "unsafe", name = "u"),
                @Flag(longName = "quiet", name = "q")
            }
    )
    public void nuke(ParameterizedContext context)
    {
        Location location;
        User user = null;

        int explosionRange = context.getParam("range", 4);
        int height = context.getParam("height", 5);

        if(explosionRange != 4 && !module.perms().COMMAND_NUKE_CHANGE_RANGE.isAuthorized(context.getSender()))
        {
            context.sendTranslated(NEGATIVE, "You are not allowed to change the explosion range of the nuke carpet!");
            return;
        }
        if(explosionRange < 0 || explosionRange > this.module.getConfig().command.nuke.maxExplosionRange)
        {
            context.sendTranslated(NEGATIVE, "The explosion range can't be less than 0 or greater than {integer}", this.module.getConfig().command.nuke.maxExplosionRange);
            return;
        }

        if(context.hasParam("player"))
        {
            if(!module.perms().COMMAND_NUKE_OTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to specify a player!");
                return;
            }

            user = context.getUser("player");
            if(user == null)
            {
                context.sendTranslated(NEGATIVE, "Player not found");
                return;
            }
            location = user.getLocation();
        }
        else
        {
            if(context.getSender() instanceof User)
            {
                user = (User) context.getSender();
            }
            if(user == null)
            {
                context.sendTranslated(NEGATIVE, "This command can only be used by a player!");
                return;
            }
            location = user.getTargetBlock(null, this.module.getConfig().command.nuke.distance).getLocation();
        }

        Shape shape = this.getShape(context, location, height);
        if(shape == null)
        {
            return;
        }

        int blockAmount = this.spawnNuke(shape, user.getWorld(), explosionRange, context.hasFlag("u"));

        if(!context.hasFlag("q"))
        {
            context.sendTranslated(POSITIVE, "You spawned {integer} blocks of tnt.", blockAmount);
        }
    }

    private Shape getShape(ParameterizedContext context, Location location, int locationHeight)
    {
        String shapeName = context.getString("shape", "cylinder");

        switch (shapeName)
        {
        case "cylinder":
            location = this.getSpawnLocation(location, locationHeight);
            int radiusX = context.getArg(0, 1);
            return new Cylinder(new Vector3(location.getX(), location.getY(), location.getZ()), radiusX, context
                .getArg(2, radiusX), context.getArg(1, 1));
        case "cube":
        case "cuboid":
            int width = context.getArg(0, 1);
            int height = shapeName.equals("cube") ? width : context.getArg(1, width);
            int depth = shapeName.equals("cube") ? width : context.getArg(2, width);

            location = location.subtract(width / 2d, 0, depth / 2d);
            location = this.getSpawnLocation(location, locationHeight);
            return new Cuboid(new Vector3(location.getX(), location.getY(), location.getZ()), width, height, depth);
        case "sphere":
            int radius = context.getArg(0, 1);
            location = this.getSpawnLocation(location, locationHeight);
            return new Sphere(new Vector3(location.getX(), location.getY(), location.getZ()), radius);
        default:
            context.sendTranslated(NEGATIVE, "The shape {input} was not found!", shapeName);
            break;
        }
        return null;
    }

    private Location getSpawnLocation(Location location, int height)
    {
        int noBlock = 0;
        while (noBlock != Math.abs(height))
        {
            location.add(0, height > 0 ? 1 : -1, 0);
            if (location.getBlock().getType() == Material.AIR)
            {
                noBlock++;
            }
            else
            {
                noBlock = 0;
            }
        }
        return location;
    }

    /**
     * iterates through the points of the shape and spawns a tnt block at the positions.
     *
     * @return the number of spawned tnt blocks.
     */
    public int spawnNuke(Shape shape, World world, int range, boolean unsafe)
    {
        int numberOfBlocks = 0;
        for (Vector3 vector : shape)
        {
            TNTPrimed tnt = world.spawn(new Location(world, vector.x, vector.y, vector.z), TNTPrimed.class);
            tnt.setVelocity(new Vector(0, 0, 0));
            tnt.setYield(range);

            numberOfBlocks++;

            if (!unsafe)
            {
                this.nukeListener.add(tnt);
            }

            if(numberOfBlocks >= this.module.getConfig().command.nuke.maxTNTAmount)
            {
                return numberOfBlocks;
            }
        }
        return numberOfBlocks;
    }

    private class NukeListener implements Listener
    {
        private final Set<TNTPrimed> noBlockDamageSet;
        private final TaskManager taskManager;
        private int taskID;

        public NukeListener()
        {
            this.noBlockDamageSet = new HashSet<>();

            this.taskManager = module.getCore().getTaskManager();
            this.taskID = -1;
        }

        public void add(TNTPrimed tnt)
        {
            this.noBlockDamageSet.add(tnt);
        }

        public void removeDeadTNT()
        {
            Iterator<TNTPrimed> tntIterator = this.noBlockDamageSet.iterator();
            while(tntIterator.hasNext())
            {
                TNTPrimed tnt = tntIterator.next();
                if(tnt.isDead())
                {
                    tntIterator.remove();
                }
            }
        }

        public boolean contains(TNTPrimed tnt)
        {
            return this.noBlockDamageSet.contains(tnt);
        }

        @EventHandler
        public void onEntityExplode(final EntityExplodeEvent event)
        {
            try
            {
                if (event.getEntityType() == EntityType.PRIMED_TNT && this.contains((TNTPrimed)event.getEntity()))
                {
                    event.blockList().clear();

                    if(!this.taskManager.isQueued(this.taskID) && !this.taskManager.isCurrentlyRunning(this.taskID))
                    {
                        this.taskID = this.taskManager.runTaskDelayed(module, new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                removeDeadTNT();
                            }
                        }, 1);
                    }
                }
            }
            catch (NullPointerException ignored)
            {}
        }

        @EventHandler
        public void onEntityDamageByEntity(final EntityDamageByEntityEvent event)
        {
            if(event.getDamager() instanceof TNTPrimed && this.contains((TNTPrimed)event.getDamager()))
            {
                event.setCancelled(true);
            }
        }
    }
}
