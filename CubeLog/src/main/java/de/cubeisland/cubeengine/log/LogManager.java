package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.log.listeners.LogListener;
import gnu.trove.impl.hash.THash;
import gnu.trove.map.hash.THashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 *
 * @author Anselm Brehme
 */
public class LogManager
{
    private Map<LogAction, LogListener> loggers = new THashMap<LogAction, LogListener>();

    public void registerLogger(LogListener logger)
    {
    }

    public void logBreakBlock(BlockBreakCause cause, Player player, BlockState state)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void logEnderGrief(BlockState oldState, BlockState newState)
    {
        if (newState.getType() == Material.AIR)
        {
            this.logBreakBlock(BlockBreakCause.ENDERMAN, null, oldState);
        }
        else
        {
            this.logPlaceBlock(null, newState, oldState);
        }
    }

    public void logExplosion(List<Block> blockList, Entity entity)
    {
        //TODO
        /*
         if (logCreeperExplosionsAsPlayerWhoTriggeredThese)
         {
         final Entity target = ((Creeper) event.getEntity()).getTarget();
         name = target instanceof Player ? ((Player) target).getName() : "Creeper";
         }
         else
         {
         name = "Creeper";
         }*/
        Player player = null; //TODO getplayer if logCreeperExplosionsAsPlayerWhoTriggeredThese
        for (Block block : blockList)
        {
            this.logBreakBlock(BlockBreakCause.EXPLOSION, player, block.getState());
        }
    }

    private void logPlaceBlock(Player player, BlockState placedBlock, BlockState replacedBlock, Location finalLoc)
    {
        if (replacedBlock == null || replacedBlock.getType() == Material.AIR)
        {
            // no Replace
        }
        else
        {
            // Replaced Block
        }

        // in DB:
        // Id | Break or Place | Player | BlockType | Location | replacedTypeAtLoc | finalLocation | replacedTypeAtFinalLoc | TimeStamp
        // output when searching:
        // [timestamp] <player> break|place <type> [x<times>] [at <Location>] [in <world>] []
        // examples:
        // 6.10.2012 19:25 Faithcaio place DiamondBlock at 42:42:42
        // 19:26 Faithcaio break DiamondBlock x5 at 40:40:40 - 44:44:44 in world_creative
        // 6.10.2012 19:28:20 Faithcaio place Sand at 42:43:42 in world
    }

    public void logPlaceBlock(Player player, BlockState placedBlock, BlockState replacedBlock)
    {
        if (placedBlock.getType() == Material.SAND || placedBlock.getType() == Material.GRAVEL || placedBlock.getType() == Material.DRAGON_EGG)
        {

            Location finalLoc = placedBlock.getLocation();
            int y = finalLoc.getBlockY();
            while (this.canFall(finalLoc))
            {
            }
            switch (finalLoc.getBlock().getType())
            {
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
                case WATER_LILY:
                    if (y - 1 == finalLoc.getBlockY())
                    {
                        finalLoc.add(0, 1, 0); // Block did not fall!
                    }
                    else
                    {
                        // TODO trapdoor sand does not break if opened!
                        finalLoc = null; // Block was falling and will break
                    }
            }
            this.logPlaceBlock(player, placedBlock, replacedBlock, finalLoc);
        }

        this.logPlaceBlock(player, placedBlock, replacedBlock, placedBlock.getLocation());
    }

    public enum ExpSource
    {
        BLOCK, MOB
    }

    public enum BlockBreakCause
    {
        PLAYER, FIRE, ENDERMAN, EXPLOSION;
    }

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
