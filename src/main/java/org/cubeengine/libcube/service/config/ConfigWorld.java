/*
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
package org.cubeengine.libcube.service.config;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.server.ServerWorld;

public class ConfigWorld
{
    private ServerWorld world;
    private String name;

    public ConfigWorld(String world)
    {
        this.name = world;
    }

    public ConfigWorld(ServerWorld world)
    {
        this.name = world.key().asString();
        this.world = world;
    }

    public String getName()
    {
        return this.name;
    }

    public ServerWorld getWorld()
    {
        if (this.world == null || !this.world.key().asString().equals(this.name))
        {
            final ResourceKey key = ResourceKey.resolve(name);
            this.world = Sponge.server().worldManager().world(key).orElseGet(() -> Sponge.server().worldManager().loadWorld(key).join());
        }
        return this.world;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ConfigWorld))
        {
            return false;
        }

        ConfigWorld that = (ConfigWorld) o;
        if (getWorld() != null ? !getWorld().uniqueId().equals(that.getWorld().uniqueId()) : that.getWorld() != null)
        {
            return false;
        }
        return getName().equals(that.getName());

    }

    @Override
    public int hashCode()
    {
        int result = 0/*getWorld() != null ? getWorld().getUniqueId().hashCode() : 0*/;
        result = 31 * result + getName().hashCode();
        return result;
    }
}
