package de.cubeisland.cubeengine.log.action.logaction.block.ignite;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.*;

/**
 * fireball-ignite
 * <p>Events: {@link IgniteActionType}</p>
 */
public class FireballIgnite extends BlockActionType
{
    public FireballIgnite(Log module)
    {
        super(module, BLOCK, ENTITY, PLAYER);
    }

    @Override
    public String getName()
    {
        return "fireball-ignite";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasCauserUser())
        {
            user.sendTranslated("%s&aFire got set by a FireBall shot at &2%s%s&a!",
                                time,logEntry.getCauserUser().getDisplayName(),loc);
        }
        else
        {
            user.sendTranslated("%s&aFire got set by a FireBall%s&a!",time,loc);
        }
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).FIREBALL_IGNITE_enable;
    }
}
