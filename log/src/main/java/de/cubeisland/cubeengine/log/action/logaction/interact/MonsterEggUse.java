package de.cubeisland.cubeengine.log.action.logaction.interact;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class MonsterEggUse extends LogActionType
{
    public MonsterEggUse(Log module)
    {
        super(module, 0x80, "monsteregg-use");
    }
}
