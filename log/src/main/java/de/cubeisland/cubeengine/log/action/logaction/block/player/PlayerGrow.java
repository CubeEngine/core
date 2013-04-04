package de.cubeisland.cubeengine.log.action.logaction.block.player;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class PlayerGrow extends BlockActionType
{
    public PlayerGrow(Log module)
    {
        super(module, 0x24, "player-grow");
    }
}
