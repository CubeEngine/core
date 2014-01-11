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
package de.cubeisland.engine.portals;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.Pair;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.portals.config.PortalConfig;

public class Portal
{
    private Portals module;
    private PortalManager manager;
    private String name;
    protected final PortalConfig config;

    public Portal(Portals module, PortalManager manager, String name, PortalConfig config)
    {
        this.module = module;
        this.manager = manager;
        this.name = name;
        this.config = config;
    }

    public String getName()
    {
        return name;
    }

    public boolean has(Location location)
    {
        return location.getWorld().getName().equals(config.world.getName()) &&
            isBetween(config.location.from.x, config.location.to.x, location.getBlockX()) &&
            isBetween(config.location.from.y, config.location.to.y, location.getBlockY()) &&
            isBetween(config.location.from.z, config.location.to.z, location.getBlockZ());
    }

    private static boolean isBetween(int a, int b, int x)
    {
        return b > a ? x >= a && x <= b : x >= b && x <= a;
    }

    public void teleport(Entity entity)
    {
        if (this.config.destination == null)
        {
            if (entity instanceof User)
            {
                ((User)entity).sendTranslated("&eThis portal \"&6%s&e\" has no destination yet!", this.getName());
                ((User)entity).attachOrGet(PortalsAttachment.class, module).setInPortal(true);
            }
        }
        else
        {
            this.config.destination.teleport(entity, this.manager, this.config.safeTeleport);
        }
    }

    public Location getPortalPos()
    {
        if (this.config.location.destination == null)
        {
            BlockVector3 midpoint = this.config.location.to.midpoint(this.config.location.from);
            return new Location(this.config.world.getWorld(), midpoint.x + 0.5, midpoint.y, midpoint.z + 0.5);
        }
        return this.config.location.destination.getLocationIn(this.config.world.getWorld());
    }

    public void delete()
    {
        this.manager.removePortal(this);
        this.config.getFile().delete();
    }

    public void showInfo(CommandSender user)
    {
        user.sendMessage("TODO");// TODO
    }

    public List<Pair<Integer,Integer>> getChunks()
    {
        List<Pair<Integer,Integer>> result = new ArrayList<>();
        int chunkXFrom = config.location.from.x >> 4;
        int chunkZFrom =  config.location.from.z >> 4;
        int chunkXTo =  config.location.to.x >> 4;
        int chunkZTo = config.location.to.z >> 4;
        if (chunkXFrom > chunkXTo) // if from is greater swap
        {
            chunkXFrom = chunkXFrom + chunkXTo;
            chunkXTo = chunkXFrom - chunkXTo;
            chunkXFrom = chunkXFrom - chunkXTo;
        }
        if (chunkZFrom > chunkZTo) // if from is greater swap
        {
            chunkZFrom = chunkZFrom + chunkZTo;
            chunkZTo = chunkZFrom - chunkZTo;
            chunkZFrom = chunkZFrom - chunkZTo;
        }
        for (int x = chunkXFrom; x <= chunkXTo; x++)
        {
            for (int z = chunkZFrom; z <= chunkZTo; z++)
            {
                result.add(new Pair<>(x,z));
            }
        }
        return result;
    }

    public World getWorld()
    {
        return this.config.world.getWorld();
    }
}
