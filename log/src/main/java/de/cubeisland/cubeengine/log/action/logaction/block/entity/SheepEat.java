package de.cubeisland.cubeengine.log.action.logaction.block.entity;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Type.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Type.ENTITY;

/**
 * Sheeps eating grass.
 * <p>Events: {@link EntityChangeActionType}</p>
 */
public class SheepEat extends BlockActionType
{
    public SheepEat(Log module)
    {
        super(module, "sheep-eat", BLOCK, ENTITY);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&aA sheep ate all the grass%s&a!",time,loc);
    }
}
