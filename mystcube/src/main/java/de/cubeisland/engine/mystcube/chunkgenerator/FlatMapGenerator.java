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
package de.cubeisland.engine.mystcube.chunkgenerator;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;

public class FlatMapGenerator extends AbstractWorldGenerator
{
    @Override
    public void generateBlockSections(World world, Random random, int x, int z, BiomeGrid biomes, byte[][] result)
    {
        int layer = 0;
        layer = this.generateLayers(result, Material.BEDROCK, layer, 1);
        layer = this.generateLayers(result, Material.OBSIDIAN, layer, 3);
        layer = this.generateLayers(result, Material.QUARTZ_BLOCK, layer, 4);
        layer = this.generateLayers(result, Material.SANDSTONE, layer, 5);
        this.generateLayers(result, Material.SMOOTH_BRICK, layer, 1);
        this.setBiomeForChunk(biomes, Biome.DESERT);
    }
}
