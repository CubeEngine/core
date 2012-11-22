package de.cubeisland.cubeengine.core.bukkit;

import java.util.EnumSet;
import org.bukkit.Material;
import static org.bukkit.Material.*;

public class BlockUtil
{
    /**
     * All non solid blocks excluding fluids.
     */
    public static final EnumSet<Material> NON_SOLID_BLOCKS =
            EnumSet.of(AIR, SAPLING, POWERED_RAIL, DETECTOR_RAIL, WEB, LONG_GRASS, DEAD_BUSH, YELLOW_FLOWER,
            RED_ROSE, BROWN_MUSHROOM, RED_MUSHROOM, TORCH, FIRE, REDSTONE_WIRE, CROPS, SIGN_POST, RAILS, WALL_SIGN,
            LEVER, STONE_PLATE, WOOD_PLATE, REDSTONE_TORCH_OFF, REDSTONE_TORCH_ON, STONE_BUTTON, SNOW, SUGAR_CANE_BLOCK,
            PORTAL, DIODE_BLOCK_OFF, DIODE_BLOCK_ON, PUMPKIN_STEM, MELON_STEM, NETHER_WARTS, ENDER_PORTAL, COCOA,
            TRIPWIRE_HOOK, TRIPWIRE, CARROT, POTATO);
    /**
     * All fluid blocks
     */
    public static final EnumSet<Material> FLUID_BLOCKS = EnumSet.of(WATER, STATIONARY_WATER, LAVA, STATIONARY_LAVA);
    
    /**
     * Blocks that can get destroyed by fluids
     */
    public static final EnumSet<Material> NON_FLUID_PROOF_BLOCKS =
            EnumSet.of(SAPLING, POWERED_RAIL, DETECTOR_RAIL, WEB, LONG_GRASS, DEAD_BUSH, YELLOW_FLOWER,
            RED_ROSE, BROWN_MUSHROOM, RED_MUSHROOM, TORCH, FIRE, REDSTONE_WIRE, CROPS, LEVER, REDSTONE_TORCH_OFF,
            REDSTONE_TORCH_ON, SNOW, DIODE_BLOCK_OFF, DIODE_BLOCK_ON, PUMPKIN_STEM, MELON_STEM, VINE, WATER_LILY,
            NETHER_WARTS, COCOA, TRIPWIRE_HOOK, TRIPWIRE, FLOWER_POT, CARROT, POTATO, SKULL);
}
