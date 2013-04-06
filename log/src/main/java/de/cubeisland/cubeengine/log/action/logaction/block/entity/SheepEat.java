package de.cubeisland.cubeengine.log.action.logaction.block.entity;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENTITY;

/**
 * Sheeps eating grass.
 * <p>Events: {@link EntityChangeActionType}</p>
 */
public class SheepEat extends BlockActionType
{
    public SheepEat(Log module)
    {
        super(module, BLOCK, ENTITY);
    }

    @Override
    public String getName()
    {
        return "sheep-eat";
    }


    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&aA sheep ate all the grass%s&a!",time,loc);
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).SHEEP_EAT_enable;
    }
}
