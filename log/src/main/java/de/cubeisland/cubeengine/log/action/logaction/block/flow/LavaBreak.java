package de.cubeisland.cubeengine.log.action.logaction.block.flow;

import java.util.EnumSet;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENVIRONEMENT;

/**
 * Lava-break
 * <p>Events: {@link LavaFlow}</p>
 */
public class LavaBreak extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, ENVIRONEMENT);
    }

    @Override
    public String getName()
    {
        return "lava-break";
    }


    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&6%s &agot destroyed by lava%s!",
                            time,logEntry.getOldBlock(),loc);
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).LAVA_BREAK_enable;
    }
}
