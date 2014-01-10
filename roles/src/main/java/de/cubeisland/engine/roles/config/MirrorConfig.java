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
package de.cubeisland.engine.roles.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.World;

import de.cubeisland.engine.core.util.Triplet;
import de.cubeisland.engine.core.world.ConfigWorld;
import de.cubeisland.engine.roles.Roles;

public class MirrorConfig
{
    public ConfigWorld mainWorld;
    private final Roles module;
    //mirror roles / assigned / users
    protected Map<ConfigWorld, Triplet<Boolean, Boolean, Boolean>> mirrors = new HashMap<>();

    public MirrorConfig(Roles module, World world)
    {
        this(module, new ConfigWorld(module.getCore().getWorldManager(), world));
    }

    protected MirrorConfig(Roles module, ConfigWorld mainWorld)
    {
        this.module = module;
        this.mainWorld = mainWorld;
        this.mirrors.put(mainWorld, new Triplet<>(true, true, true));
    }

    /**
     * Returns a map of the mirrored worlds.
     * The mirrors are: roles | assigned roles | assigned permissions and metadata
     *
     * @return
     */
    public Map<World, Triplet<Boolean, Boolean, Boolean>> getWorldMirrors()
    {
        HashMap<World, Triplet<Boolean, Boolean, Boolean>> result = new HashMap<>();
        for (Entry<ConfigWorld, Triplet<Boolean, Boolean, Boolean>> entry : this.mirrors.entrySet())
        {
            World world = entry.getKey().getWorld();
            if (world == null)
            {
                module.getLog().warn("Configured world for mirror of {} does not exist! {}", mainWorld.getName(), entry.getKey().getName());
                continue;
            }
            result.put(world, entry.getValue());
        }
        return result;
    }

    public void setWorld(World world, boolean roles, boolean assigned, boolean users)
    {
        this.setWorld(new ConfigWorld(module.getCore().getWorldManager(), world), new Triplet<>(roles, assigned, users));
    }

    protected void setWorld(ConfigWorld world, Triplet<Boolean, Boolean, Boolean> t)
    {
        this.mirrors.put(world, t);
    }

    public World getMainWorld()
    {
        if (this.mainWorld.getWorld() == null)
        {
            module.getLog().warn("Configured main world for mirror does not exist! {}", this.mainWorld.getName());
            return null;
        }
        return this.mainWorld.getWorld();
    }
}
