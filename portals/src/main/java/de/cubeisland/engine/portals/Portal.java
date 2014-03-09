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
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.portals.config.PortalConfig;

public class Portal
{
    private final Portals module;
    private final PortalManager manager;
    private final String name;
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
                ((User)entity).sendTranslated(MessageType.NEUTRAL, "This portal {name} has no destination yet!", this.getName());
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
        user.sendTranslated(MessageType.POSITIVE, "Portal Information for {name#portal}", this.getName());
        if (this.config.safeTeleport)
        {
            user.sendTranslated(MessageType.POSITIVE, "This Portal has safe-teleport enabled");
        }
        if (this.config.teleportNonPlayers)
        {
            user.sendTranslated(MessageType.POSITIVE, "This Portal will teleport non-players too");
        }
        user.sendTranslated(MessageType.POSITIVE, "{user} is the owner of this portal", this.config.owner);
        user.sendTranslated(MessageType.POSITIVE, "Location: {vector} to {vector} in {world}",
                            new BlockVector3(this.config.location.from.x, this.config.location.from.y, this.config.location.from.z),
                            new BlockVector3(this.config.location.to.x, this.config.location.to.y, this.config.location.to.z), this.config.world.getName());
        if (this.config.destination == null)
        {
            user.sendTranslated(MessageType.POSITIVE, "This portal has no destination yet");
        }
        else
        {
            switch (config.destination.type)
            {
            case PORTAL:
                user.sendTranslated(MessageType.POSITIVE, "This portal teleports to another portal: {name#portal}", config.destination.portal);
                break;
            case WORLD:
                user.sendTranslated(MessageType.POSITIVE, "This portal teleports to the spawn of {world}", config.destination.world);
                break;
            case LOCATION:
                user.sendTranslated(MessageType.POSITIVE, "This portal teleports to {vector} in {world}",
                    new BlockVector3((int)config.destination.location.x, (int)config.destination.location.y, (int)config.destination.location.z), config.destination.world.getName());
                break;
            }

        }
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
