package de.cubeisland.cubeengine.log.action.logaction.block.explosion;

import java.util.EnumSet;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.*;

/**
 * Creeper-Explosions
 * <p>Events: {@link ExplodeActionType}</p>
 */
public class CreeperExplode extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, ENTITY, PLAYER);
    }

    @Override
    public String getName()
    {
        return "creeper-explode";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            int amount = logEntry.getAttached().size()+1;
            if (logEntry.getCauserUser() == null)
            {
                user.sendTranslated("%s&aA Creeper-Explosion wrecked &6%dx %s&a%s!",
                                    time,amount,logEntry.getOldBlock(),loc);
            }
            else
            {
                user.sendTranslated("%s&2%s &alet a Creeper detonate and destroy &6%dx &6%s&a%s!",
                                    time,
                                    logEntry.getCauserUser().getDisplayName(),
                                    amount,
                                    logEntry.getOldBlock(),
                                    loc);
            }
        }
        else
        {
            if (logEntry.getCauserUser() == null)
            {
                user.sendTranslated("%s&aA Creeper-Explosion wrecked &6%s&a%s!",
                                    time,logEntry.getOldBlock(),loc);
            }
            else
            {
                user.sendTranslated("%s&2%s &alet a Creeper detonate and destroy &6%s&a%s!",
                                    time,
                                    logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getOldBlock(),
                                    loc);
            }
        }
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).CREEPER_EXPLODE_enable;
    }
}
