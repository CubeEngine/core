package de.cubeisland.cubeengine.log;

import org.bukkit.Location;
import org.bukkit.Material;

public class LogManager_old
{
    

    //TODO move this into Util
    private boolean canFall(Location loc)
    {
        Material mat = loc.getWorld().getBlockAt(loc.add(0, -1, 0)).getType();
        if (loc.getY() == 0)
        {
            return false;
        }
        switch (mat)
        {
            //fall
            case AIR:
            //fall and place
            case WATER:
            case STATIONARY_WATER:
            case LAVA:
            case STATIONARY_LAVA:
            case SNOW:
            case LONG_GRASS:
            //fall and or break
            case STEP:
            case WOOD_STEP:
            case CAKE_BLOCK:
            case DIODE_BLOCK_ON:
            case DIODE_BLOCK_OFF:
            case TRAP_DOOR:
            case TORCH:
            case SIGN:
            case SIGN_POST:
            case PORTAL:
            case RED_ROSE:
            case YELLOW_FLOWER:
            case RED_MUSHROOM:
            case BROWN_MUSHROOM:
            case SAPLING:
            case CROPS:
            case ENDER_PORTAL:
            case STONE_BUTTON:
            case LEVER:
            case TRIPWIRE_HOOK:
            case TRIPWIRE:
            case STONE_PLATE:
            case WOOD_PLATE:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
            case SUGAR_CANE_BLOCK:
            case MELON_STEM:
            case PUMPKIN_STEM:
            case VINE:
            case NETHER_WARTS:
                //TODO add 1.4 blocks
                //case WOOD_BUTTON: 
                return true;
            default: //else block
                loc.add(0, 1, 0); //is solid so cannot fall more
                return false;
        }
    }
}
