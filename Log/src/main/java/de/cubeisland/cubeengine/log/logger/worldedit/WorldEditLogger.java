package de.cubeisland.cubeengine.log.logger.worldedit;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.logger.BlockLogger;
import de.cubeisland.cubeengine.log.logger.config.WorldEditConfig;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class WorldEditLogger extends BlockLogger<WorldEditConfig>
{
    public WorldEditLogger(Log module)
    {
        super(module, WorldEditConfig.class);
    }

    public void logWorldEditChange(Player player, BlockState oldState, BlockState newState)
    {
        this.logBlockChange(BlockChangeCause.WORLDEDIT, oldState.getWorld(), player, oldState, newState);
        //TODO sign data
            //module.getLoggerManager().getLogger(SignChangeLogger.class).logSignPlaceWithData(player, (Sign)newState);
    }
}
