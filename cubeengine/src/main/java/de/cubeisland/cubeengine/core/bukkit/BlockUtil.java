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
package de.cubeisland.cubeengine.core.bukkit;

import org.bukkit.Material;

import java.util.EnumSet;

import static org.bukkit.Material.*;

public class BlockUtil
{
    /**
     * All fluid blocks
     */
    private static final EnumSet<Material> FLUID_BLOCKS = EnumSet.of(WATER, STATIONARY_WATER, LAVA, STATIONARY_LAVA);

    public static boolean isFluidBlock(Material mat)
    {
        return FLUID_BLOCKS.contains(mat);
    }

    /**
     * Blocks that can get destroyed by fluids
     */
    private static final EnumSet<Material> NON_FLUID_PROOF_BLOCKS =
            EnumSet.of(SAPLING, POWERED_RAIL, DETECTOR_RAIL, WEB, LONG_GRASS, DEAD_BUSH, YELLOW_FLOWER,
                RED_ROSE, BROWN_MUSHROOM, RED_MUSHROOM, TORCH, FIRE, REDSTONE_WIRE, CROPS, LEVER, REDSTONE_TORCH_OFF,
                REDSTONE_TORCH_ON, SNOW, DIODE_BLOCK_OFF, DIODE_BLOCK_ON, PUMPKIN_STEM, MELON_STEM, VINE, WATER_LILY,
                NETHER_WARTS, COCOA, TRIPWIRE_HOOK, TRIPWIRE, FLOWER_POT, CARROT, POTATO, SKULL,
                    ACTIVATOR_RAIL, REDSTONE_COMPARATOR_OFF, REDSTONE_COMPARATOR_ON);

    public static boolean isNonFluidProofBlock(Material mat)
    {
        return NON_FLUID_PROOF_BLOCKS.contains(mat);
    }

    private static final EnumSet<Material> NON_OBSTRUCTING_SOLID_BLOCKS =
        EnumSet.of(SIGN_POST, WOODEN_DOOR, WALL_SIGN, STONE_PLATE, IRON_DOOR_BLOCK,
                   WOOD_PLATE, GOLD_PLATE, IRON_PLATE);

    public static boolean isNonObstructingSolidBlock(Material material)
    {
        return NON_OBSTRUCTING_SOLID_BLOCKS.contains(material); 
    }
}
