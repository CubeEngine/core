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
package de.cubeisland.cubeengine.core.world;

import java.util.Set;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.Cleanable;

public interface WorldManager extends Cleanable
{
    long getWorldId(World world);
    Long getWorldId(String name);
    long[] getAllWorldIds();
    World getWorld(long id);
    World getWorld(String name);
    World getWorld(UUID uid);
    Set<World> getWorlds();
    void registerGenerator(Module module, String id, ChunkGenerator generator);
    ChunkGenerator getGenerator(Module module, String id);
    void removeGenerator(Module module, String id);
    void removeGenerators(Module module);
}
