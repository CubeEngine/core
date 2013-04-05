package de.cubeisland.cubeengine.log.action.logaction.interact;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

public class EntityDye extends SimpleLogActionType
{
    public EntityDye(Log module)
    {
        super(module, 0x88, "entity-dye");
    }
}
