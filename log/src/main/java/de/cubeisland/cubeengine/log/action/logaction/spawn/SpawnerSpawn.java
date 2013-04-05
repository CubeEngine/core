package de.cubeisland.cubeengine.log.action.logaction.spawn;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

public class SpawnerSpawn extends SimpleLogActionType

{
    public SpawnerSpawn(Log module)
    {
        super(module, 0x82, "spawner-spawn");
    }
}
