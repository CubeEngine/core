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
                NETHER_WARTS, COCOA, TRIPWIRE_HOOK, TRIPWIRE, FLOWER_POT, CARROT, POTATO, SKULL);

    public static boolean isNonFluidProofBlock(Material mat)
    {
        return NON_FLUID_PROOF_BLOCKS.contains(mat);
    }
}
