package de.cubeisland.cubeengine.log.action.logaction.block.player;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;


/**
 * Filling buckets with lava or water
 * <p>Events: {@link de.cubeisland.cubeengine.log.action.logaction.block.NaturalGrow NaturalGrow}</p>
 */
public class PlayerGrow extends BlockActionType
{
    public PlayerGrow(Log module)
    {
        super(module, "player-grow", BLOCK, PLAYER);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            int amount = logEntry.getAttached().size()+1;
            user.sendTranslated("%s&2%s let grow &6%d %s%s&a!",
                                time,logEntry.getCauserUser().getDisplayName(),
                                amount,logEntry.getNewBlock(),loc);
        }
        else
        {
            if (logEntry.hasReplacedBlock())
            {
                user.sendTranslated("%s&2%s let grow &6%s&a into &6%s%s&a!",
                                    time,logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getNewBlock(),
                                    logEntry.getOldBlock(),loc);
            }
            else
            {
                user.sendTranslated("%s&2%s let grow &6%s%s&a!",
                                    time,logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getNewBlock(),loc);
            }
        }
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).PLAYER_GROW_enable;
    }
}
