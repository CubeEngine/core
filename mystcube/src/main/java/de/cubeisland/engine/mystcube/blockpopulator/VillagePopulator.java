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
package de.cubeisland.engine.mystcube.blockpopulator;

import java.util.Random;

import net.minecraft.server.v1_7_R3.StructureBoundingBox;
import net.minecraft.server.v1_7_R3.WorldGenVillageStart;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;

import org.bukkit.Chunk;

public class VillagePopulator extends AbstractBlockPopulator
{
    @Override
    public void populate(CraftWorld world, Random random, Chunk source)
    {
        WorldGenVillageStart start = new WorldGenVillageStart(world.getHandle(), random, source.getX(), source.getZ(), 0);
        int x = (source.getX() << 4) + 8;
        int z = (source.getZ() << 4) + 8;
        final int RADIUSVALUE = 500;
        StructureBoundingBox sbb = new StructureBoundingBox(x - RADIUSVALUE, z - RADIUSVALUE, x + RADIUSVALUE, z + RADIUSVALUE);
        start.a(world.getHandle(), random, sbb);
    }
}
